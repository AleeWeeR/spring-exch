# Person Abroad API - Two Endpoints

This API provides two separate endpoints for checking and managing pension recipients who are abroad.

## Overview

The system has **TWO distinct endpoints** with different purposes:

1. **`/check-status`** - Just CHECK person status (read-only, no changes)
2. **`/restore-status`** - CHECK arrival and RESTORE person if they've returned

---

## Endpoint 1: Check Status

**URL**: `POST /api/v1/person-abroad/check-status`

**Purpose**: Just check if a person is in the pension list and their current status (no restoration)

**Security**: Requires `GET_PERSON_ABROAD_STATUS` authority

### Request

```json
{
    "Data": {
        "ws_id": 1,
        "pinfl": "12345678901234"
    }
}
```

### Response Codes

| Code | Meaning | Description |
|------|---------|-------------|
| **0** | Not found | Pensiya oluvchilar ro'yhatida mavjud emas |
| **1** | Active | Person is active in the system |
| **2** | Inactive (abroad) | Nofaol, close_desc=11 (chet elda 3 oydan ortiq) |
| **3** | Inactive (other) | Nofaol, boshqa sabablar bilan |

### Response Examples

**Not Found (0):**
```json
{
    "result": 0,
    "msg": "Pensiya oluvchilar ro'yhatida mavjud emas",
    "ws_id": 1
}
```

**Active (1):**
```json
{
    "result": 1,
    "msg": "",
    "ws_id": 1,
    "status": 1
}
```

**Inactive - Abroad (2):**
```json
{
    "result": 2,
    "msg": "",
    "ws_id": 1,
    "status": 0
}
```

**Inactive - Other Reason (3):**
```json
{
    "result": 3,
    "msg": "",
    "ws_id": 1,
    "status": 0
}
```

### Logic Flow

```
1. Check if person exists in Pf_Persons
   └─ NO  → Return 0 (Not found)
   └─ YES → Continue to step 2

2. Check if person is active (no close_reason, close_date, close_desc)
   └─ YES → Return 1 (Active)
   └─ NO  → Continue to step 3

3. Check close_desc value
   └─ close_desc = "11" → Return 2 (Abroad)
   └─ close_desc ≠ "11" → Return 3 (Other reason)
```

---

## Endpoint 2: Restore Status

**URL**: `POST /api/v1/person-abroad/restore-status`

**Purpose**: Check if person has returned to Uzbekistan and restore their pension status

**Security**: Requires `RESTORE_PERSON_ABROAD_STATUS` authority

### Request

```json
{
    "Data": {
        "ws_id": 1,
        "pinfl": "12345678901234"
    }
}
```

### Response Codes

| Code | Meaning | Description |
|------|---------|-------------|
| **0** | Not found | Pensiya oluvchilar ro'yhatida mavjud emas |
| **1** | Already active | Pensiya oluvchilar ro'yhatida mavjud (no action needed) |
| **2** | Restored | Oluvchi statusi faol xolatga keltirildi (person was restored) |
| **3** | Not returned | O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi |

### Response Examples

**Not Found (0):**
```json
{
    "result": 0,
    "msg": "Pensiya oluvchilar ro'yhatida mavjud emas",
    "ws_id": 1
}
```

**Already Active (1):**
```json
{
    "result": 1,
    "msg": "Pensiya oluvchilar ro'yhatida mavjud",
    "ws_id": 1,
    "status": 1
}
```

**Successfully Restored (2):**
```json
{
    "result": 2,
    "msg": "Oluvchi statusi faol xolatga keltirildi",
    "ws_id": 1
}
```

**Not Returned Yet (3):**
```json
{
    "result": 3,
    "msg": "O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi",
    "ws_id": 1,
    "status": 0
}
```

### Logic Flow

```
1. Check if person exists
   └─ NO  → Return 0 (Not found)
   └─ YES → Continue to step 2

2. Check if person is already active
   └─ YES → Return 1 (Already active, no action needed)
   └─ NO  → Continue to step 3

3. Call Pf_Person_Abroad.Citizen_Arrived(person_id, pinfl, birth_date)
   └─ Returned 1 (citizen arrived) → Continue to step 4
   └─ Returned 0 (not arrived)     → Return 3 (Not returned)

4. Call Restore_Person_Arrived(person_id)
   └─ Success → Return 2 (Restored)
   └─ Failed  → Return 3 (Restore failed)
```

---

## Comparison

| Aspect | `/check-status` | `/restore-status` |
|--------|----------------|-------------------|
| **Purpose** | Just check status | Check & restore |
| **Read-Only** | ✅ Yes | ❌ No (modifies data) |
| **Calls Citizen_Arrived** | ❌ No | ✅ Yes |
| **Calls Restore_Person_Arrived** | ❌ No | ✅ Yes |
| **Database Changes** | None | Updates person if restored |
| **Code 2 Meaning** | Inactive (abroad) | Restored successfully |
| **Authority** | GET_PERSON_ABROAD_STATUS | RESTORE_PERSON_ABROAD_STATUS |

---

## Use Cases

### Use Case 1: Just Check Person's Current Status

**Scenario**: You want to know if a person is in the pension list and their current status.

**Endpoint**: `POST /api/v1/person-abroad/check-status`

**Example**:
```bash
curl -X POST http://localhost:8080/api/v1/person-abroad/check-status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "Data": {
        "ws_id": 1,
        "pinfl": "12345678901234"
    }
  }'
```

**Response**:
- `0` = Person not in pension list
- `1` = Person active
- `2` = Person abroad (close_desc=11)
- `3` = Person inactive for other reasons

---

### Use Case 2: Restore Person Who Returned from Abroad

**Scenario**: A person was abroad (close_desc=11), you want to check if they've returned and restore their pension.

**Endpoint**: `POST /api/v1/person-abroad/restore-status`

**Example**:
```bash
curl -X POST http://localhost:8080/api/v1/person-abroad/restore-status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "Data": {
        "ws_id": 1,
        "pinfl": "12345678901234"
    }
  }'
```

**Response**:
- `0` = Person not found
- `1` = Person already active (no action needed)
- `2` = Person was abroad, has returned, pension restored ✅
- `3` = Person still abroad (not detected in Uzbekistan)

---

## Security Configuration

Add these authorities to your security configuration:

```
GET_PERSON_ABROAD_STATUS       - For /check-status endpoint
RESTORE_PERSON_ABROAD_STATUS   - For /restore-status endpoint
```

---

## Testing

### Test Check Status

```bash
# Test with active person
POST /api/v1/person-abroad/check-status
{
    "Data": {
        "ws_id": 1,
        "pinfl": "12345678901234"
    }
}
# Expected: result=1 (active)
```

### Test Restore Status

```bash
# Test with person abroad who has returned
POST /api/v1/person-abroad/restore-status
{
    "Data": {
        "ws_id": 1,
        "pinfl": "12345678901234"
    }
}
# Expected: result=2 (restored) if person has returned
# Expected: result=3 (not returned) if person still abroad
```

---

## Implementation Notes

### Endpoint 1: check-status
- **Repository calls**: `isPersonActive()`, `getPersonCloseStatus()`
- **Oracle functions**: `Pf_Person_Repository.Is_Person_Active`, `Pf_Person_Repository.Get_Person_Close_Status`
- **No database changes**

### Endpoint 2: restore-status
- **Repository calls**: `isPersonActive()`, `checkCitizenArrival()`, `restoreArrivedPerson()`
- **Oracle functions**:
  - `Pf_Person_Repository.Is_Person_Active`
  - `Pf_Person_Abroad_Repository.Check_Citizen_Arrival` (wraps `Pf_Person_Abroad.Citizen_Arrived`)
  - `Pf_Person_Abroad_Repository.Restore_Arrived_Person` (wraps `Restore_Person_Arrived`)
- **Database changes**: Updates person status if restoration is successful

---

## Summary

✅ **Two separate endpoints** for different purposes
✅ **Clear separation**: Check vs. Restore
✅ **Different security authorities** for each endpoint
✅ **Same request format** for both
✅ **Different response meanings** for code 2
✅ **Repository layer pattern** with focused functions
