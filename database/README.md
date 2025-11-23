# Database Setup for WS ID Status Check

This directory contains Oracle PL/SQL functions needed for the WS ID pensioner status check endpoint.

## Overview

The `Check_Person_Status` function checks if a pension recipient exists in the database and verifies their citizenship arrival status using the existing `Pf_Person_Abroad.Citizen_Arrived` and `Restore_Person_Arrived` functions.

## Function Logic

1. **Result Code 0**: Person not found in pension recipients list
   - Returns: `{"result": 0, "msg": "Pensiya oluvchilar ro'yhatida mavjud emas", "ws_id": 77}`

2. **Result Code 1**: Person found and already active
   - Returns: `{"result": 1, "msg": "", "ws_id": 77, "status": 1}`

3. **Result Code 2**: Person found, was inactive, citizen has arrived, status activated
   - Makes call to citizen arrival check endpoint/function
   - Updates `is_active` to 'Y' in database
   - Returns: `{"result": 2, "msg": "O'zgartirildi", "ws_id": 77}`

4. **Result Code 3**: Person found, citizen has NOT arrived
   - Returns: `{"result": 3, "msg": "O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi", "ws_id": 77, "status": 0}`

## Installation

### Step 1: Create the Request Log Table

```sql
sqlplus username/password@database
@CREATE_WS_ID_TABLE.sql
```

This creates the `Pf_Exchanges_Ws_Id_Status` table to track all status check requests.

### Step 2: Install the Integrated Function (RECOMMENDED)

```sql
sqlplus username/password@database
@PF_EXCHANGES_WS_ID_INTEGRATED.sql
```

**This is the recommended version** that integrates with your existing:
- `Pf_Person_Abroad.Citizen_Arrived` function
- `Restore_Person_Arrived` function
- Follows the same pattern as `Get_Charged_Info`

### Alternative Options

**Option 1: Full Implementation (with HTTP call)**
```sql
@PF_EXCHANGES_WS_ID.sql
```
Standalone version with HTTP call logic using `UTL_HTTP`.

**Option 2: Simplified Implementation**
```sql
@PF_EXCHANGES_WS_ID_SIMPLE.sql
```
Version with local table checks.

## Database Schema Requirements

### Tables Required

1. **Pf_Persons Table** (your existing table)
   - Must have columns: `Person_Id`, `Pinpp`, `Person_Type`, `Close_Reason`, `Close_Date`, `Close_Desc`, `Birth_Date`
   - Used to check if pension recipient exists and their status

2. **Pf_Exchanges_Ws_Id_Status Table** (created by CREATE_WS_ID_TABLE.sql)
   - Logs all status check requests
   - Similar to `Pf_Exchanges_Ep_Charged_Info` table

### Functions Required

The integrated version depends on these existing functions in your database:

1. **Pf_Person_Abroad.Citizen_Arrived**
   - Checks if a person has arrived back to Uzbekistan
   - Returns 1 if arrived, 0 if not

2. **Restore_Person_Arrived**
   - Restores a person's pension when they return
   - Activates the person in the system

## How It Works

The integrated function follows this logic:

1. **Parse XML Input**: Extracts `ws_id` and `pinfl` from XML
2. **Look for Person**: Checks `Pf_Persons` table for matching `Pinpp`
3. **Check Status**:
   - If not found → Return code 0
   - If found and active (no close reason/date) → Return code 1
   - If found but closed → Check citizenship arrival
4. **Citizenship Check**: Calls `Pf_Person_Abroad.Citizen_Arrived`
   - If arrived → Call `Restore_Person_Arrived` → Return code 2
   - If not arrived → Return code 3
5. **Log Request**: Saves request/response to `Pf_Exchanges_Ws_Id_Status` table

## Integration with Existing Functions

The function uses your existing logic:

```sql
-- Checks if person is closed (abroad)
IF R_Pf_Persons.Close_Reason IS NOT NULL
   OR R_Pf_Persons.Close_Date IS NOT NULL
   OR R_Pf_Persons.Close_Desc = '11' THEN

    -- Check if citizen has arrived
    IF Pf_Person_Abroad.Citizen_Arrived(...) = 1 THEN
        -- Restore person
        Restore_Person_Arrived(...)
        -- Return code 2
    ELSE
        -- Return code 3
    END IF;
END IF;
```

## Testing

Test the function directly in SQL:

```sql
DECLARE
    v_return_code NUMBER;
    v_output_data CLOB;
    v_input_data VARCHAR2(4000);
BEGIN
    -- Build XML input
    v_input_data := '<Data><ws_id>77</ws_id><pinfl>41006673910061</pinfl></Data>';

    -- Call function
    v_return_code := PF_EXCHANGES_WS_ID.Check_Person_Status(
        O_Data => v_output_data,
        P_Data => v_input_data
    );

    DBMS_OUTPUT.PUT_LINE('Return Code: ' || v_return_code);
    DBMS_OUTPUT.PUT_LINE('JSON Output: ' || v_output_data);
END;
/
```

**Expected Output Examples:**

```sql
-- Person not found
Return Code: 1
JSON Output: {"result": 0, "msg": "Pensiya oluvchilar ro'yhatida mavjud emas", "ws_id": 77}

-- Person found and active
Return Code: 1
JSON Output: {"result": 1, "msg": "", "ws_id": 77, "status": 1}

-- Person restored (arrived)
Return Code: 1
JSON Output: {"result": 2, "msg": "O'zgartirildi", "ws_id": 77}

-- Person not arrived
Return Code: 1
JSON Output: {"result": 3, "msg": "O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi", "ws_id": 77, "status": 0}
```

## Permissions

Grant execute permissions to your application user:

```sql
GRANT EXECUTE ON PF_EXCHANGES_WS_ID TO your_application_user;
```

## Integration with Java Application

The Java application calls this function via:
- Repository: `WsIdStatusRepository.java`
- Service: `WsIdStatusServiceImpl.java`
- Controller: `WsIdStatusController.java`

The function expects a 200 return code for success, 500 for errors.
