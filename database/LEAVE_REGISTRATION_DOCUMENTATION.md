# LEAVE Registration Flow Documentation

## Overview

When a person returns from abroad, the system now performs two key operations:
1. **Restore the person** - Removes close flags from `Pf_Persons` table
2. **Create LEAVE registration** - Opens a registration record in `Pf_Rb_Registration_Books` with type '07' and application_reason 'LEAVE'

This follows the existing registration workflow: **Add → Send → Approve**

## Registration Flow

### Flow Diagram

```
Person Returns from Abroad
         ↓
Check Citizen_Arrived (API call)
         ↓
Restore_Person_Arrived (Clears Pf_Persons close flags)
         ↓
Create LEAVE Registration (Pf_Rb_Registration_Books)
    Registration_Type_Code = '07'
    Application_Reason = 'LEAVE'
    Status = '00' (Initial)
    Is_Closed = 'N'
         ↓
[Manual Step] Send_Registration
    Status changes to '01' (Sent)
         ↓
[Manual Step] Approve_Registration
    Creates Pf_Applications record
    Application_Reason = 'LEAVE'
    Restoration_Date = Registration_Date
    Recalc_Date = Registration_Date
    Generates new Case_Number
         ↓
Person Fully Restored with Application
```

## Database Changes

### 1. Pf_Persons (Updated by Restore_Person_Arrived)
```sql
UPDATE Pf_Persons
SET Close_Reason = NULL,
    Close_Date   = NULL,
    Close_Desc   = NULL
WHERE Person_Id = :person_id
```

### 2. Pf_Rb_Registration_Books (Created by Restore_Person_Status)
```sql
INSERT INTO Pf_Rb_Registration_Books (
    Registration_Id,          -- Generated from sequence
    Registration_Type_Code,   -- '07' (Reregistration/Recalculation)
    Registration_Date,        -- SYSDATE
    Pinpp,                    -- Person's PINFL
    Person_Id,                -- Person ID
    Sobes_Org_Id,             -- From person's address or default
    Status,                   -- '00' (Initial)
    Application_Reason,       -- 'LEAVE' (Important!)
    Is_Closed,                -- 'N'
    Created_By,               -- User ID or 1
    Creation_Date,            -- SYSDATE
    ...
) VALUES (...)
```

### 3. Pf_Applications (Created later by Approve_Registration)
When the registration is approved (manually or automatically), it creates:
```sql
INSERT INTO Pf_Applications (
    Application_Id,           -- Generated
    Case_Number,              -- NEW case number from sequence
    Application_Reason,       -- 'LEAVE'
    Restoration_Date,         -- Registration_Date
    Recalc_Date,              -- Registration_Date
    Person_Id,                -- Person ID
    ...
) VALUES (...)
```

## Registration Types

### Type '07' - Reregistration/Recalculation
Used for various recalculation scenarios based on `Application_Reason`:

| Application_Reason | Description | Case Number | Restoration Date |
|--------------------|-------------|-------------|------------------|
| **'LEAVE'** | Person returned from abroad | NEW | Yes |
| 'DOC' | Additional documents provided | Copy from prev | No |
| 'CHANG' | Change in pension type | Copy from prev | No |
| 'RESUM' | Resume pension | Copy from prev | Yes |
| 'MIGR' | Migration | Copy from prev | No |
| 'ADD' | Additional info | Copy from prev | No |

**Key difference for LEAVE**: Gets a **new case number** (unlike other reasons that copy from previous application)

## Enhanced Restore_Person_Status Function

### What It Does Now

1. **Validates request** (ws_id, pinfl)
2. **Checks person exists** in Pf_Persons
3. **Checks if already active**
   - If yes: Return result=1 (already active)
4. **Calls Citizen_Arrived** API to verify arrival
   - If not arrived: Return result=3
5. **Calls Restore_Person_Arrived** to clear close flags
   - If failed: Return result=3
6. **NEW: Creates LEAVE registration** (if close_desc=11)
   - Only if person was abroad (close_desc='11')
   - Registration_Type_Code = '07'
   - Application_Reason = 'LEAVE'
   - Status = '00' (needs to be sent/approved later)
7. **Returns result=2** (successfully restored)

### Code Changes

```sql
-- After successful restoration:
IF V_Close_Desc = '11' THEN
    -- Person was abroad, create LEAVE registration
    Create_Leave_Registration(
        P_Person_Id    => V_Person_Id,
        P_Pinfl        => R_Row.Pinpp,
        P_Sobes_Org_Id => V_Sobes_Org_Id
    );
END IF;
```

## Registration Fields Explained

### Critical Fields in Pf_Rb_Registration_Books

```sql
Registration_Type_Code = '07'
```
- Code '07' = Reregistration/Recalculation type
- Allows different application reasons
- Used for LEAVE, DOC, CHANG, RESUM, etc.

```sql
Application_Reason = 'LEAVE'
```
- Identifies this as a "return from abroad" case
- Triggers special handling in Approve_Registration
- Creates new case number (not copied from previous)
- Sets restoration_date and recalc_date

```sql
Status = '00'
```
- Initial status
- Must be sent ('01') and approved ('02') to create application

```sql
Is_Closed = 'N'
```
- Registration is open
- Can be processed further

```sql
Person_Id
```
- Links to Pf_Persons record
- Must exist before registration is created

```sql
Sobes_Org_Id
```
- Organization responsible for this person
- Retrieved from person's address or defaults to PF_ADMIN_ORG

## Next Steps After LEAVE Registration Creation

### Option 1: Manual Process (Current System)
1. **Specialist reviews** the LEAVE registration
2. **Calls Send_Registration** function
   - Updates Status from '00' to '01'
3. **Calls Approve_Registration** function
   - Creates Pf_Applications record
   - Status changes to '02'
   - Application is ready for processing

### Option 2: Automatic Approval (Future Enhancement)
Could add to Restore_Person_Status:
```sql
-- After creating registration:
v_Registration_Id := Send_Registration(v_Out_Text, V_Registration_Id);
v_Application_Id := Approve_Registration(v_Out_Text, v_Registration_Id);
```

This would automatically complete the full flow.

## Checking Registration Status

### Query to See Created LEAVE Registrations
```sql
SELECT r.Registration_Id,
       r.Registration_Date,
       r.Pinpp,
       r.Status,
       r.Application_Reason,
       s.Name_Ru AS Status_Name,
       t.Name_Ru AS Type_Name
FROM Pf_Rb_Registration_Books r
LEFT JOIN Pf_Rb_S_Statuses s ON r.Status = s.Code
LEFT JOIN Pf_Rb_S_Types t ON r.Registration_Type_Code = t.Code
WHERE r.Application_Reason = 'LEAVE'
  AND r.Is_Closed = 'N'
ORDER BY r.Registration_Date DESC;
```

### Query to See Full Person Restoration History
```sql
-- Restore log
SELECT *
FROM Pf_Exchange_Restore_Statuses
WHERE Pinpp = '12345678901234'
ORDER BY Creation_Date DESC;

-- Related registration
SELECT *
FROM Pf_Rb_Registration_Books
WHERE Pinpp = '12345678901234'
  AND Application_Reason = 'LEAVE'
ORDER BY Registration_Date DESC;

-- Related application (after approval)
SELECT *
FROM Pf_Applications
WHERE Person_Id = :person_id
  AND Application_Reason = 'LEAVE'
ORDER BY Creation_Date DESC;
```

## Error Handling

### Registration Creation Failures

If `Create_Leave_Registration` fails:
- **Transaction is rolled back** (entire restore operation fails)
- **Error is logged** to `Pf_Exchange_Restore_Statuses` table
- **Result code 3** is returned
- **Person restore is also rolled back** (all-or-nothing)

### Why All-or-Nothing?

**Reason**: We don't want a person to be restored in `Pf_Persons` but missing their LEAVE registration, as this would:
1. Leave person in limbo state (active but no application)
2. Require manual intervention to create registration
3. Break the audit trail

## Testing Scenarios

### Test Case 1: Person Abroad Returns
```sql
-- Initial state: Person is abroad (close_desc=11)
SELECT Person_Id, Close_Desc FROM Pf_Persons WHERE Pinpp = '12345678901234';
-- Result: Close_Desc = '11'

-- Call Restore_Person_Status
-- Expected:
-- 1. Pf_Persons.Close_Desc = NULL
-- 2. New record in Pf_Rb_Registration_Books with Application_Reason='LEAVE'
-- 3. Result code = 2

SELECT * FROM Pf_Rb_Registration_Books
WHERE Pinpp = '12345678901234'
AND Application_Reason = 'LEAVE';
```

### Test Case 2: Person Inactive for Other Reason
```sql
-- Initial state: Person inactive but NOT abroad (close_desc != '11')
SELECT Person_Id, Close_Desc FROM Pf_Persons WHERE Pinpp = '98765432101234';
-- Result: Close_Desc = '05' (some other reason)

-- Call Restore_Person_Status
-- Expected:
-- 1. Pf_Persons remains unchanged (Citizen_Arrived returns 0)
-- 2. NO registration created
-- 3. Result code = 3 (not arrived)
```

### Test Case 3: Person Already Active
```sql
-- Initial state: Person already active
SELECT Person_Id, Close_Desc FROM Pf_Persons WHERE Pinpp = '11111111111111';
-- Result: Close_Desc = NULL

-- Call Restore_Person_Status
-- Expected:
-- 1. No changes to Pf_Persons
-- 2. NO registration created
-- 3. Result code = 1 (already active)
```

## Integration with Existing Code

### No Changes Required To:
- ✅ `Pf_Person_Abroad.Citizen_Arrived` - Used as-is
- ✅ `Restore_Person_Arrived` - Used as-is
- ✅ `Send_Registration` - Works with created registration
- ✅ `Approve_Registration` - Handles 'LEAVE' reason correctly (line 1855-1858)

### Changes Made To:
- ✅ `PF_EXCHANGES_ABROAD.Restore_Person_Status` - Now creates LEAVE registration
- ✅ Added new tables: `Pf_Exchange_Restore_Statuses`

## Benefits

1. **Complete Audit Trail** - Every restoration is logged with registration
2. **Follows Existing Workflow** - Uses standard Add → Send → Approve pattern
3. **Automatic Application Creation** - Registration leads to application when approved
4. **Case Number Generation** - Each return gets proper case number
5. **Integration with Existing Reports** - LEAVE registrations appear in standard reports
6. **Recalculation Triggered** - Restoration_Date and Recalc_Date are set correctly

## Deployment Steps

1. **Deploy table DDL** (if not exists):
   ```sql
   @CREATE_PERSON_STATUS_TABLES.sql
   ```

2. **Deploy enhanced package**:
   ```sql
   @PF_EXCHANGES_ABROAD_WITH_LEAVE_REG.sql
   ```

3. **Grant permissions**:
   ```sql
   GRANT EXECUTE ON PF_EXCHANGES_ABROAD TO your_app_user;
   ```

4. **Test with sample data**:
   ```sql
   -- Test the enhanced function
   DECLARE
       v_return_code NUMBER;
       v_output_data CLOB;
   BEGIN
       v_return_code := PF_EXCHANGES_ABROAD.Restore_Person_Status(
           O_Data => v_output_data,
           P_Data => '<Data><ws_id>77</ws_id><pinfl>12345678901234</pinfl></Data>'
       );
       DBMS_OUTPUT.PUT_LINE('Result: ' || v_output_data);
   END;
   /
   ```

5. **Verify registration created**:
   ```sql
   SELECT * FROM Pf_Rb_Registration_Books
   WHERE Application_Reason = 'LEAVE'
   ORDER BY Registration_Date DESC;
   ```

## Troubleshooting

### Issue: Registration not created
**Possible causes**:
1. Person was not abroad (close_desc != '11')
2. Citizen_Arrived returned 0 (not arrived)
3. Restore_Person_Arrived failed
4. Transaction rolled back due to error

**Solution**: Check `Pf_Exchange_Restore_Statuses` table for error details

### Issue: Duplicate registrations
**Possible causes**:
1. Function called multiple times for same person
2. Check_Exists_Reg should prevent this

**Solution**: Add unique constraint or check in Create_Leave_Registration

### Issue: Cannot approve registration
**Possible causes**:
1. Missing previous application (for type '07')
2. Status not '01' (must be sent first)

**Solution**: Ensure Send_Registration is called first

## Future Enhancements

1. **Auto-approval option** - Add parameter to auto-complete Send/Approve
2. **Email notification** - Notify specialists when LEAVE registration created
3. **Batch processing** - Process multiple returns at once
4. **Integration with scheduler** - Auto-check for returns daily
5. **Dashboard** - Show pending LEAVE registrations
