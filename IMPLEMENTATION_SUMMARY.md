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

## Architecture: Repository Layer Pattern

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
│  - Orchestrates repository calls                │
│  - Builds appropriate responses                 │
│  - Handles logging                              │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│ PersonAbroadRepository                          │
│  - isPersonActive(pinfl)                        │
│  - getPersonCloseStatus(pinfl)                  │
│  - checkCitizenArrival(id, pinfl, birthDate)    │
│  - restoreArrivedPerson(id)                     │
│  - logStatusRequest(...)                        │
│  - getPersonIdByPinfl(pinfl)                    │
│  - getPersonBirthDate(id)                       │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│ Oracle Repository Layer                         │
│  - Pf_Person_Repository (data access)           │
│  - Pf_Person_Abroad_Repository (operations)     │
└─────────────────────────────────────────────────┘
```

## Database Layer: Oracle Packages

### Package 1: Pf_Person_Repository (Data Access)

```sql
FUNCTION Is_Person_Active(p_Pinfl IN VARCHAR2) RETURN NUMBER;
-- Returns: -1=not found, 0=inactive, 1=active

FUNCTION Get_Person_Close_Status(
    p_Pinfl IN VARCHAR2,
    o_Close_Reason OUT VARCHAR2,
    o_Close_Date OUT DATE,
    o_Close_Desc OUT VARCHAR2
) RETURN NUMBER;
-- Returns closure details
```

### Package 2: Pf_Person_Abroad_Repository (Operations)

```sql
FUNCTION Check_Citizen_Arrival(
    p_Person_Id  IN NUMBER,
    p_Pinfl      IN VARCHAR2,
    p_Birth_Date IN DATE,
    o_Message    OUT VARCHAR2
) RETURN NUMBER;
-- Wraps existing Pf_Person_Abroad.Citizen_Arrived
-- Returns: 1=arrived, 0=not arrived

FUNCTION Restore_Arrived_Person(
    p_Person_Id IN NUMBER,
    o_Message   OUT VARCHAR2
) RETURN NUMBER;
-- Wraps existing Restore_Person_Arrived
-- Returns: 1=success, 0=failed

PROCEDURE Log_Status_Request(
    p_Ws_Id       IN NUMBER,
    p_Pinfl       IN VARCHAR2,
    p_In_Data     IN CLOB,
    p_Result_Code IN NUMBER,
    p_Msg         IN VARCHAR2,
    p_Status      IN NUMBER := NULL  -- NULL for restore-status, 1/2/3 for check-status
);
-- Logs request to Pf_Exchanges_Ws_Id_Status table
```

## Database Table: Pf_Exchanges_Ws_Id_Status

```sql
CREATE TABLE Pf_Exchanges_Ws_Id_Status (
    Ws_Id_Status_Id NUMBER PRIMARY KEY,
    Ws_Id           NUMBER,
    Pinpp           VARCHAR2(14),
    In_Data         CLOB,              -- Request JSON
    Result_Code     NUMBER,            -- Response result code
    Msg             VARCHAR2(4000),    -- Response message
    Status          NUMBER,            -- For check-status only (1/2/3), NULL for restore
    Data_Sqlerr     VARCHAR2(4000),
    Creation_Date   DATE DEFAULT SYSDATE
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

1. `database/PF_EXCHANGES_WS_ID_REPOSITORY_LAYER.sql` - **MAIN PACKAGE** (use this)
   - Pf_Person_Repository package
   - Pf_Person_Abroad_Repository package

2. `database/CREATE_WS_ID_TABLE.sql` - Request logging table

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

### 3. Repository Layer Pattern
- **Reason**: Separation of concerns
- Oracle functions handle only data access
- Java service layer orchestrates business logic
- Better testability and maintainability

### 4. Shared Request Format
- **Reason**: Both endpoints accept same input
- Single request DTO can be reused

### 5. Database Logging Flexibility
- **Reason**: Support both endpoint types
- `Status` column is optional (NULL for restore-status)
- `Result_Code` has different meanings per endpoint

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

-- Step 1: Create table for request logging
@database/CREATE_WS_ID_TABLE.sql

-- Step 2: Create repository layer packages
@database/PF_EXCHANGES_WS_ID_REPOSITORY_LAYER.sql

-- Step 3: Grant permissions
GRANT EXECUTE ON Pf_Person_Repository TO your_app_user;
GRANT EXECUTE ON Pf_Person_Abroad_Repository TO your_app_user;
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

1. **Separation of Concerns**: Repository pattern with clean layers
2. **Two Distinct Operations**: Read-only check vs. check-and-restore
3. **Different Permissions**: Separate security authorities per endpoint
4. **Request Logging**: All requests logged to database table
5. **Integration**: Uses existing `Citizen_Arrived` and `Restore_Person_Arrived` functions
6. **Type Safety**: Separate response DTOs prevent field confusion
7. **Error Handling**: Comprehensive error handling and logging
8. **Validation**: Input validation for ws_id and pinfl

## Git Branch

All changes committed to:
```
claude/add-ws-id-post-controller-01X9kxBzCptWCx4NnUDVk4Kg
```

## Naming Evolution

The implementation went through several iterations:
1. **Initial**: WsIdStatus* (single endpoint)
2. **Refactored**: PersonAbroad* (two separate endpoints)
3. **Final**: Two separate response DTOs per endpoint type

## Next Steps

1. Deploy Oracle packages to your database
2. Add both security authorities to your configuration
3. Test both endpoints with real data
4. Monitor `Pf_Exchanges_Ws_Id_Status` table for request logs
5. Verify different response formats work correctly
6. Create pull request when ready for review
