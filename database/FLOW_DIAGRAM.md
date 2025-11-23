# WS ID Status Check - Flow Diagram

## Request Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Client Application                                             │
│  POST /api/v1/ws-id/status                                      │
│  {                                                               │
│    "Data": {                                                     │
│      "ws_id": 77,                                                │
│      "pinfl": "41006673910061"                                   │
│    }                                                             │
│  }                                                               │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  WsIdStatusController.java                                      │
│  - Validates request                                            │
│  - Checks GET_WS_ID_STATUS authority                            │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  WsIdStatusServiceImpl.java                                     │
│  - Extracts pinfl and ws_id                                     │
│  - Calls repository                                             │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  WsIdStatusRepository.java                                      │
│  - Creates SimpleJdbcCall                                       │
│  - Calls Oracle function                                        │
│  - PF_EXCHANGES_WS_ID.CHECK_PENSIONER_STATUS                    │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  Oracle Database - PF_EXCHANGES_WS_ID Package                   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Step 1: Check if pensioner exists in database           │  │
│  │ SELECT * FROM pf_pension_recipients WHERE pinfl = ?      │  │
│  └────────────────────┬─────────────────────────────────────┘  │
│                       │                                          │
│         ┌─────────────┴──────────────┐                          │
│         │                             │                          │
│   NOT FOUND                       FOUND                         │
│         │                             │                          │
│         ▼                             ▼                          │
│  ┌─────────────┐              ┌──────────────┐                 │
│  │ Return 0    │              │ Check status │                 │
│  │ Not in list │              │ is_active?   │                 │
│  └─────────────┘              └──────┬───────┘                 │
│                                       │                          │
│                         ┌─────────────┴──────────────┐          │
│                         │                             │          │
│                    ACTIVE='Y'                   ACTIVE='N'      │
│                         │                             │          │
│                         ▼                             ▼          │
│                  ┌─────────────┐         ┌──────────────────┐  │
│                  │ Return 1    │         │ Step 2: Check    │  │
│                  │ Already     │         │ citizen arrival  │  │
│                  │ active      │         │                  │  │
│                  └─────────────┘         └────────┬─────────┘  │
│                                                    │             │
│                                      ┌─────────────┴──────────┐ │
│                                      │                         │ │
│                              CITIZEN ARRIVED          NOT ARRIVED│
│                                      │                         │ │
│                                      ▼                         ▼ │
│                           ┌──────────────────┐    ┌────────────┐│
│                           │ Update is_active │    │ Return 3   ││
│                           │ to 'Y'           │    │ Not arrived││
│                           │ Return 2         │    └────────────┘│
│                           │ Activated        │                  │
│                           └──────────────────┘                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Response Codes Detail

### Response Code 0: Not Found
```json
{
  "result": 0,
  "msg": "Pensiya oluvchilar ro'yhatida mavjud emas",
  "ws_id": 77
}
```
**When:** Person with given PINFL not found in pension recipients database

---

### Response Code 1: Found and Active
```json
{
  "result": 1,
  "msg": "",
  "ws_id": 77,
  "status": 1
}
```
**When:** Person found in database and `is_active = 'Y'`

---

### Response Code 2: Activated
```json
{
  "result": 2,
  "msg": "O'zgartirildi",
  "ws_id": 77
}
```
**When:**
- Person found in database
- `is_active = 'N'`
- Citizen arrival check returns ARRIVED
- Database updated: `is_active` set to 'Y'

---

### Response Code 3: Arrival Not Detected
```json
{
  "result": 3,
  "msg": "O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi",
  "ws_id": 77,
  "status": 0
}
```
**When:**
- Person found in database
- `is_active = 'N'`
- Citizen arrival check returns NOT ARRIVED

---

## Citizen Arrival Check Integration

The citizen arrival check can be implemented in two ways:

### Option 1: External REST API Call
```sql
-- In PF_EXCHANGES_WS_ID.sql
v_url := 'http://your-endpoint/check-arrival?pinfl=' || p_Pinfl;
v_http_request := UTL_HTTP.BEGIN_REQUEST(v_url, 'GET');
-- Parse response to determine if citizen arrived
```

### Option 2: Local Database Table
```sql
-- In PF_EXCHANGES_WS_ID_SIMPLE.sql
SELECT arrival_status
FROM citizen_arrivals
WHERE pinfl = p_pinfl
AND arrival_date >= SYSDATE - 30;
```

### Option 3: Another PL/SQL Function
```sql
v_citizen_arrived := OTHER_PACKAGE.CHECK_ARRIVAL(p_Pinfl);
```

## Database State Changes

Only **Result Code 2** modifies the database:

```sql
UPDATE pf_pension_recipients
SET is_active = 'Y',
    last_updated = SYSDATE,
    updated_by = 'SYSTEM'
WHERE pinfl = p_Pinfl;

COMMIT;
```

All other result codes are read-only operations.
