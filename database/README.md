# Database Setup for WS ID Status Check

This directory contains Oracle PL/SQL functions needed for the WS ID pensioner status check endpoint.

## Overview

The `CHECK_PENSIONER_STATUS` function checks if a pension recipient exists in the database and verifies their citizenship arrival status.

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

### Option 1: Full Implementation (with HTTP call)

```sql
sqlplus username/password@database
@PF_EXCHANGES_WS_ID.sql
```

This version includes HTTP call logic using `UTL_HTTP` to call an external citizen arrival check API.

**Requirements:**
- `UTL_HTTP` package must be available
- ACL (Access Control List) must be configured for network access:

```sql
BEGIN
  DBMS_NETWORK_ACL_ADMIN.APPEND_HOST_ACE(
    host => 'your-api-endpoint.com',
    ace  => xs$ace_type(
      privilege_list => xs$name_list('http'),
      principal_name => 'YOUR_SCHEMA',
      principal_type => xs_acl.ptype_db
    )
  );
END;
/
```

### Option 2: Simplified Implementation

```sql
sqlplus username/password@database
@PF_EXCHANGES_WS_ID_SIMPLE.sql
```

This version assumes you have a local table or function to check citizen arrival status.

## Database Schema Requirements

### Tables Needed

1. **Pension Recipients Table** (example name: `pf_pension_recipients`)
   ```sql
   CREATE TABLE pf_pension_recipients (
       id NUMBER PRIMARY KEY,
       pinfl VARCHAR2(14) NOT NULL,
       is_active VARCHAR2(1) DEFAULT 'N',
       last_updated DATE,
       updated_by VARCHAR2(50),
       -- other columns...
       CONSTRAINT chk_active CHECK (is_active IN ('Y', 'N'))
   );

   CREATE INDEX idx_pinfl ON pf_pension_recipients(pinfl);
   ```

2. **Citizen Arrivals Table** (optional, for simplified version)
   ```sql
   CREATE TABLE citizen_arrivals (
       id NUMBER PRIMARY KEY,
       pinfl VARCHAR2(14) NOT NULL,
       arrival_date DATE,
       arrival_status VARCHAR2(20),
       -- other columns...
   );

   CREATE INDEX idx_arrival_pinfl ON citizen_arrivals(pinfl);
   ```

## Customization

You need to customize the following in the SQL files:

1. **Table Name**: Replace `pf_pension_recipients` with your actual pension recipients table name
2. **Column Names**: Adjust `is_active`, `pinfl`, etc. to match your schema
3. **Citizen Arrival Check**: Implement the logic to check if citizen has arrived:
   - Option A: Call external REST API (see `PF_EXCHANGES_WS_ID.sql`)
   - Option B: Query local table (see `PF_EXCHANGES_WS_ID_SIMPLE.sql`)
   - Option C: Call another PL/SQL function

## API Endpoint Configuration

If using the full implementation with HTTP calls, update this line in `PF_EXCHANGES_WS_ID.sql`:

```sql
v_url := 'http://your-api-endpoint/check-arrival?pinfl=' || p_Pinfl;
```

Replace with your actual citizen arrival check endpoint.

## Testing

Test the function directly in SQL:

```sql
DECLARE
    v_return_code NUMBER;
    v_output_text CLOB;
BEGIN
    v_return_code := PF_EXCHANGES_WS_ID.CHECK_PENSIONER_STATUS(
        p_Pinfl => '41006673910061',
        p_Ws_Id => 77,
        o_Out_Text => v_output_text
    );

    DBMS_OUTPUT.PUT_LINE('Return Code: ' || v_return_code);
    DBMS_OUTPUT.PUT_LINE('JSON Output: ' || v_output_text);
END;
/
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
