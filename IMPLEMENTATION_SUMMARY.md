# Person Abroad Status Check Implementation Summary

## Overview

This implementation adds **TWO separate POST endpoints** for checking pension recipient status and restoring returned citizens. The system integrates with existing Oracle functions for citizen arrival verification.

## Architecture: Two Endpoints

### Endpoint 1: Check Status (Read-Only)
- **URL**: `POST /api/v1/person-abroad/check-status`
- **Security**: Requires `GET_PERSON_ABROAD_STATUS` authority
- **Purpose**: Check person status WITHOUT making any changes
- **Response Format**:
```json
{
    "result": 1,
    "msg": "",
    "ws_id": 77,
    "status": 1
}
```

**Response Fields:**
- `result`: 1=success (HTTP 200), 0=error
- `status`: 1=faol (active), 2=nofaol (abroad, close_desc=11), 3=nofaol (other reasons)
- `msg`: Error message if result=0, empty otherwise
- `ws_id`: Echo of request ws_id

### Endpoint 2: Restore Status (Check & Restore)
- **URL**: `POST /api/v1/person-abroad/restore-status`
- **Security**: Requires `RESTORE_PERSON_ABROAD_STATUS` authority
- **Purpose**: Check citizen arrival and restore if returned
- **Response Format**:
```json
{
    "result": 2,
    "msg": "Oluvchi statusi faol xolatga keltirildi",
    "ws_id": 77
}
```

**Response Fields:**
- `result`: 0=not found, 1=already active, 2=restored, 3=not arrived
- `msg`: Uzbek message describing the result
- `ws_id`: Echo of request ws_id
- **Note**: No `status` field (only result codes)

## Request Format (Same for Both Endpoints)

```json
{
    "Data": {
        "ws_id": 77,
        "pinfl": "41006673910061"
    }
}
```

## Response Code Comparison

### Check-Status Response Codes

| result | status | Meaning | Message |
|--------|--------|---------|---------|
| 1 | 1 | Person found and active | "" |
| 1 | 2 | Person abroad (close_desc=11) | "" |
| 1 | 3 | Person inactive (other reason) | "" |
| 0 | null | Error or not found | "Pensiya oluvchilar ro'yhatida mavjud emas" |

### Restore-Status Response Codes

| result | Meaning | Message |
|--------|---------|---------|
| 0 | Not in pension list | "Pensiya oluvchilar ro'yhatida mavjud emas" |
| 1 | Already active | "Pensiya oluvchilar ro'yhatida mavjud" |
| 2 | Successfully restored | "Oluvchi statusi faol xolatga keltirildi" |
| 3 | Citizen not arrived | "O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi" |

## Architecture: Single Package Pattern

```
┌─────────────────────────────────────────────────┐
│ PersonAbroadController                          │
│  - POST /check-status (GET_PERSON_ABROAD_STATUS)│
│  - POST /restore-status (RESTORE_PERSON_ABROAD) │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│ PersonAbroadService (Interface)                 │
│  - checkStatus() → CheckStatusResponseDto       │
│  - restoreStatus() → RestoreStatusResponseDto   │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│ PersonAbroadServiceImpl                         │
│  - Converts JSON request to XML                 │
│  - Calls Oracle functions                       │
│  - Parses JSON response from CLOB               │
│  - Maps to appropriate DTOs                     │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│ PersonAbroadRepository                          │
│  - checkPersonStatus(xmlData) → CLOB            │
│  - restorePersonStatus(xmlData) → CLOB          │
│  - clobToString(clob) → String                  │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│ Oracle: PF_EXCHANGES_ABROAD Package             │
│  - Check_Person_Status(P_Data, O_Data)          │
│  - Restore_Person_Status(P_Data, O_Data)        │
│  - Ensure_Json_Element helpers                  │
│  - All business logic & logging handled here    │
└──────────────┬──────────────────────────────────┘
               │
               ├─→ Pf_Persons (check existence & status)
               ├─→ Pf_Person_Abroad.Citizen_Arrived
               ├─→ Restore_Person_Arrived
               ├─→ Pf_Exchange_Person_Statuses (check logs)
               └─→ Pf_Exchange_Restore_Statuses (restore logs)
```

**Pattern:** Follows same architecture as `Get_Charges_Info` and `Get_Charged_Info`

## Database Layer: Oracle Package

### Package: PF_EXCHANGES_ABROAD

Single package following the same pattern as existing `Get_Charges_Info` and `Get_Charged_Info` functions.

**Function 1: Check_Person_Status** (for /check-status endpoint)
```sql
FUNCTION Check_Person_Status(
    O_Data OUT CLOB,    -- JSON response
    P_Data IN VARCHAR2  -- XML request: <Data><ws_id>77</ws_id><pinfl>...</pinfl></Data>
) RETURN NUMBER;
-- Returns: 0=error, 1=success
-- Response JSON: {"result": 1, "msg": "", "ws_id": 77, "status": 1}
-- Logs to: Pf_Exchange_Person_Statuses table
```

**Function 2: Restore_Person_Status** (for /restore-status endpoint)
```sql
FUNCTION Restore_Person_Status(
    O_Data OUT CLOB,    -- JSON response
    P_Data IN VARCHAR2  -- XML request: <Data><ws_id>77</ws_id><pinfl>...</pinfl></Data>
) RETURN NUMBER;
-- Returns: 0=error, 1=success
-- Response JSON: {"result": 2, "msg": "O'zgartirildi", "ws_id": 77}
-- Logs to: Pf_Exchange_Restore_Statuses table
```

**Key Features:**
- Uses `Ensure_Json_Element` helper functions for safe JSON construction
- XML input parsing via `Pf_Exchange_Online.Convert_To_Xml`
- Nested `Finish_Request` function handles logging and response building
- Integrates with existing `Pf_Person_Abroad.Citizen_Arrived` and `Restore_Person_Arrived`
- Automatic logging to separate tables per endpoint type
- Comprehensive error handling with `Data_Sqlerr` column

## Database Tables: Two Separate Logging Tables

### Table 1: Pf_Exchange_Person_Statuses (Check-Status Logs)

```sql
CREATE TABLE Pf_Exchange_Person_Statuses (
    Person_Status_Id NUMBER PRIMARY KEY,
    Ws_Id            NUMBER,
    Pinpp            VARCHAR2(14),
    In_Data          CLOB,              -- Request XML
    Result_Code      NUMBER,            -- 1=success, 0=error
    Msg              VARCHAR2(4000),    -- Error message if result=0
    Status           NUMBER,            -- 1=faol, 2=nofaol(abroad), 3=nofaol(other)
    Data_Sqlerr      VARCHAR2(4000),
    Creation_Date    DATE DEFAULT SYSDATE
);
```

### Table 2: Pf_Exchange_Restore_Statuses (Restore-Status Logs)

```sql
CREATE TABLE Pf_Exchange_Restore_Statuses (
    Restore_Status_Id NUMBER PRIMARY KEY,
    Ws_Id             NUMBER,
    Pinpp             VARCHAR2(14),
    In_Data           CLOB,              -- Request XML
    Result_Code       NUMBER,            -- 0/1/2/3
    Msg               VARCHAR2(4000),    -- Uzbek message
    Data_Sqlerr       VARCHAR2(4000),
    Creation_Date     DATE DEFAULT SYSDATE
);
```

## Files Created/Modified

### Java Components

**Controllers:**
- `src/main/java/uz/fido/pfexchange/controller/PersonAbroadController.java`
  - Two endpoints with different security authorities
  - Separate response DTOs per endpoint

**DTOs:**
- `PersonAbroadStatusRequestDto.java` - Request wrapper (shared)
- `PersonAbroadStatusDataDto.java` - Request data (shared)
- `PersonAbroadCheckStatusResponseDto.java` - Check-status response (result, msg, ws_id, status)
- `PersonAbroadRestoreStatusResponseDto.java` - Restore-status response (result, msg, ws_id)

**Services:**
- `PersonAbroadService.java` - Service interface
- `PersonAbroadServiceImpl.java` - Service implementation with separate methods

**Repositories:**
- `PersonAbroadRepository.java` - Oracle function caller

### Database Scripts

1. **`database/PF_EXCHANGES_ABROAD.sql`** - Main package (use this)
   - PF_EXCHANGES_ABROAD package with two functions
   - Check_Person_Status function
   - Restore_Person_Status function
   - Ensure_Json_Element helper functions
   - Follows same pattern as Get_Charges_Info

2. **`database/CREATE_PERSON_STATUS_TABLES.sql`** - Two logging tables
   - Pf_Exchange_Person_Statuses (check-status logs)
   - Pf_Exchange_Restore_Statuses (restore-status logs)

3. `database/PF_EXCHANGES_WS_ID_REPOSITORY_LAYER.sql` - **OLD** (repository layer pattern - not used)
4. `database/CREATE_WS_ID_TABLE.sql` - **OLD** (single table - not used)

### Documentation
- `IMPLEMENTATION_SUMMARY.md` - This file

## Key Design Decisions

### 1. Two Separate Endpoints
- **Reason**: Different operations require different permissions and produce different results
- `/check-status`: Read-only, requires lower privilege
- `/restore-status`: Write operation, requires higher privilege

### 2. Two Separate Response DTOs
- **Reason**: Different response structures
- Check-status includes `status` field (1/2/3)
- Restore-status does NOT include `status` field (only result codes 0/1/2/3)

### 3. Single Package Pattern (Similar to Charges)
- **Reason**: Consistency with existing codebase
- Follows same pattern as `Get_Charges_Info` and `Get_Charged_Info`
- All business logic in Oracle (better performance)
- Java layer is thin - just calls functions and parses responses
- Simpler architecture, easier to maintain

### 4. Two Separate Logging Tables
- **Reason**: Different schemas per endpoint type
- `Pf_Exchange_Person_Statuses` has `Status` column (1/2/3)
- `Pf_Exchange_Restore_Statuses` does NOT have `Status` column
- Cleaner separation, no nullable columns
- Easier to query and analyze per operation type

### 5. XML Input Format
- **Reason**: Consistency with existing exchange functions
- Oracle functions expect XML: `<Data><ws_id>77</ws_id><pinfl>...</pinfl></Data>`
- Java service converts JSON request to XML before calling Oracle
- Oracle returns JSON response in CLOB
- Matches pattern used by `Get_Charges_Info`

### 6. Shared Request Format (Java Layer)
- **Reason**: Both endpoints accept same input
- Single request DTO can be reused
- Java receives JSON, converts to XML for Oracle

## Integration with Existing Code

The implementation wraps your existing Oracle functions:

```sql
-- Existing function 1: Check if citizen arrived
Pf_Person_Abroad.Citizen_Arrived(
    o_Out_text   => o_Message,
    p_person_id  => p_Person_Id,
    p_pinpp      => p_Pinfl,
    p_birth_date => p_Birth_Date
)

-- Existing function 2: Restore person
Restore_Person_Arrived(
    o_Out_Text       => o_Message,
    p_Person_Id      => p_Person_Id,
    p_Restore_Reason => 'Adliya vazirligi...'
)
```

## Flow Diagrams

### Check-Status Flow

```
1. Request received with pinfl + ws_id
2. Call Is_Person_Active(pinfl)
   ├─ -1: Not found → {"result": 0, "status": null}
   ├─  1: Active → {"result": 1, "status": 1}
   └─  0: Inactive
       └─ Call Get_Person_Close_Status(pinfl)
          ├─ close_desc=11 → {"result": 1, "status": 2}
          └─ close_desc≠11 → {"result": 1, "status": 3}
3. Log request to database with status field
4. Return response
```

### Restore-Status Flow

```
1. Request received with pinfl + ws_id
2. Call Is_Person_Active(pinfl)
   ├─ -1: Not found → {"result": 0}
   ├─  1: Active → {"result": 1}
   └─  0: Inactive
       └─ Get person_id and birth_date
          └─ Call Check_Citizen_Arrival(person_id, pinfl, birth_date)
             ├─ 1: Arrived
             │  └─ Call Restore_Arrived_Person(person_id)
             │     ├─ 1: Success → {"result": 2}
             │     └─ 0: Failed → {"result": 3}
             └─ 0: Not arrived → {"result": 3}
3. Log request to database with status=NULL
4. Return response
```

## Installation Steps

### 1. Database Setup

```sql
-- Connect to database
sqlplus username/password@database

-- Step 1: Create two logging tables
@database/CREATE_PERSON_STATUS_TABLES.sql

-- Step 2: Create PF_EXCHANGES_ABROAD package
@database/PF_EXCHANGES_ABROAD.sql

-- Step 3: Grant permissions
GRANT EXECUTE ON PF_EXCHANGES_ABROAD TO your_app_user;
```

### 2. Application Configuration

Add security authorities to your configuration:
```
GET_PERSON_ABROAD_STATUS      # For check-status endpoint
RESTORE_PERSON_ABROAD_STATUS  # For restore-status endpoint
```

### 3. Testing

**Test check-status endpoint:**
```bash
curl -X POST http://localhost:8080/api/v1/person-abroad/check-status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "Data": {
        "ws_id": 77,
        "pinfl": "41006673910061"
    }
  }'
```

**Test restore-status endpoint:**
```bash
curl -X POST http://localhost:8080/api/v1/person-abroad/restore-status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "Data": {
        "ws_id": 77,
        "pinfl": "41006673910061"
    }
  }'
```

## Key Features

1. **Consistent Pattern**: Follows same architecture as `Get_Charges_Info` and `Get_Charged_Info`
2. **Two Distinct Operations**: Read-only check vs. check-and-restore
3. **Different Permissions**: Separate security authorities per endpoint
4. **Separate Logging**: Two dedicated tables for different operation types
5. **Integration**: Uses existing `Citizen_Arrived` and `Restore_Person_Arrived` functions
6. **Type Safety**: Separate response DTOs prevent field confusion
7. **Error Handling**: Comprehensive error handling in both Oracle and Java
8. **Validation**: Input validation for ws_id and pinfl in Oracle package
9. **JSON Response**: Oracle builds JSON response using `Ensure_Json_Element` helpers
10. **XML Request**: Consistent with existing exchange functions

## Git Branch

All changes committed to:
```
claude/add-ws-id-post-controller-01X9kxBzCptWCx4NnUDVk4Kg
```

## Implementation Evolution

The implementation went through several iterations:

1. **Initial**: WsIdStatus* (single endpoint, monolithic Oracle function)
2. **Second**: PersonAbroad* (two separate endpoints, repository layer pattern)
   - Multiple Oracle packages (Pf_Person_Repository, Pf_Person_Abroad_Repository)
   - Java orchestrates business logic
   - Single logging table with optional Status column
3. **Final**: Single Package Pattern (PF_EXCHANGES_ABROAD)
   - Two main functions following `Get_Charges_Info` pattern
   - Two separate response DTOs
   - Two separate logging tables
   - All business logic in Oracle
   - Java layer is thin (convert XML, parse JSON)

## Next Steps

1. Deploy Oracle package to your database:
   - Run `CREATE_PERSON_STATUS_TABLES.sql` to create both logging tables
   - Run `PF_EXCHANGES_ABROAD.sql` to create the package
2. Add both security authorities to your configuration:
   - `GET_PERSON_ABROAD_STATUS` for check-status endpoint
   - `RESTORE_PERSON_ABROAD_STATUS` for restore-status endpoint
3. Test both endpoints with real data
4. Monitor both logging tables:
   - `Pf_Exchange_Person_Statuses` for check-status requests
   - `Pf_Exchange_Restore_Statuses` for restore-status requests
5. Verify response formats match specifications
6. Create pull request when ready for review
