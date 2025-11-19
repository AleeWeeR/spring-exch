-- Oracle Repository Layer for Person Abroad Status Check
-- Separates concerns: Repository functions for data access, Service layer in Java
--
-- TWO ENDPOINTS SUPPORTED:
-- 1. /check-status - Read-only status check
--    Response: {"result": 1, "msg": "", "ws_id": 77, "status": 1}
--    - result: 1=success (200), 0=error
--    - status: 1=faol, 2=nofaol(chet elda, close_desc=11), 3=nofaol(boshqa)
--
-- 2. /restore-status - Check arrival and restore if returned
--    Response: {"result": 2, "msg": "O'zgartirildi", "ws_id": 77}
--    - result: 0=not found, 1=already active, 2=restored, 3=not arrived

-- ============================================================================
-- PACKAGE 1: Person Repository - Data Access Layer
-- ============================================================================
CREATE OR REPLACE PACKAGE Pf_Person_Repository AS
    -- Get person by PINFL
    FUNCTION Get_Person_By_Pinfl(
        p_Pinfl IN VARCHAR2
    ) RETURN Pf_Persons%ROWTYPE;

    -- Check if person exists and is active
    FUNCTION Is_Person_Active(
        p_Pinfl IN VARCHAR2
    ) RETURN NUMBER;  -- Returns: -1=not found, 0=inactive, 1=active

    -- Get person closure status
    FUNCTION Get_Person_Close_Status(
        p_Pinfl IN VARCHAR2,
        o_Close_Reason OUT VARCHAR2,
        o_Close_Date OUT DATE,
        o_Close_Desc OUT VARCHAR2
    ) RETURN NUMBER;  -- Returns: 1=found, 0=not found
END Pf_Person_Repository;
/

CREATE OR REPLACE PACKAGE BODY Pf_Person_Repository AS

    FUNCTION Get_Person_By_Pinfl(
        p_Pinfl IN VARCHAR2
    ) RETURN Pf_Persons%ROWTYPE
    IS
        v_person Pf_Persons%ROWTYPE;
    BEGIN
        SELECT *
        INTO v_person
        FROM Pf_Persons
        WHERE Pinpp = p_Pinfl
          AND Person_Type = '01'
          AND ROWNUM = 1;

        RETURN v_person;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN NULL;
    END Get_Person_By_Pinfl;

    FUNCTION Is_Person_Active(
        p_Pinfl IN VARCHAR2
    ) RETURN NUMBER
    IS
        v_count NUMBER;
        v_close_reason VARCHAR2(100);
        v_close_date DATE;
        v_close_desc VARCHAR2(100);
    BEGIN
        -- Check if person exists
        SELECT COUNT(*),
               MAX(Close_Reason),
               MAX(Close_Date),
               MAX(Close_Desc)
        INTO v_count,
             v_close_reason,
             v_close_date,
             v_close_desc
        FROM Pf_Persons
        WHERE Pinpp = p_Pinfl
          AND Person_Type = '01';

        -- Person not found
        IF v_count = 0 THEN
            RETURN -1;
        END IF;

        -- Person is active (no closure)
        IF v_close_reason IS NULL
           AND v_close_date IS NULL
           AND v_close_desc IS NULL THEN
            RETURN 1;
        END IF;

        -- Person is inactive (closed)
        RETURN 0;
    END Is_Person_Active;

    FUNCTION Get_Person_Close_Status(
        p_Pinfl IN VARCHAR2,
        o_Close_Reason OUT VARCHAR2,
        o_Close_Date OUT DATE,
        o_Close_Desc OUT VARCHAR2
    ) RETURN NUMBER
    IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*),
               MAX(Close_Reason),
               MAX(Close_Date),
               MAX(Close_Desc)
        INTO v_count,
             o_Close_Reason,
             o_Close_Date,
             o_Close_Desc
        FROM Pf_Persons
        WHERE Pinpp = p_Pinfl
          AND Person_Type = '01';

        RETURN CASE WHEN v_count > 0 THEN 1 ELSE 0 END;
    END Get_Person_Close_Status;

END Pf_Person_Repository;
/

-- ============================================================================
-- PACKAGE 2: WS ID Status Repository - Individual Operations
-- ============================================================================
CREATE OR REPLACE PACKAGE Pf_Person_Abroad_Repository AS

    -- Check if citizen has arrived
    FUNCTION Check_Citizen_Arrival(
        p_Person_Id  IN NUMBER,
        p_Pinfl      IN VARCHAR2,
        p_Birth_Date IN DATE,
        o_Message    OUT VARCHAR2
    ) RETURN NUMBER;  -- Returns: 1=arrived, 0=not arrived

    -- Restore person who has arrived
    FUNCTION Restore_Arrived_Person(
        p_Person_Id IN NUMBER,
        o_Message   OUT VARCHAR2
    ) RETURN NUMBER;  -- Returns: 1=success, 0=failed

    -- Log status check/restore request
    -- For check-status endpoint: p_Status should be 1/2/3
    -- For restore-status endpoint: p_Status should be NULL
    PROCEDURE Log_Status_Request(
        p_Ws_Id       IN NUMBER,
        p_Pinfl       IN VARCHAR2,
        p_In_Data     IN CLOB,
        p_Result_Code IN NUMBER,
        p_Msg         IN VARCHAR2,
        p_Status      IN NUMBER := NULL
    );

END Pf_Person_Abroad_Repository;
/

CREATE OR REPLACE PACKAGE BODY Pf_Person_Abroad_Repository AS

    FUNCTION Check_Citizen_Arrival(
        p_Person_Id  IN NUMBER,
        p_Pinfl      IN VARCHAR2,
        p_Birth_Date IN DATE,
        o_Message    OUT VARCHAR2
    ) RETURN NUMBER
    IS
        v_result NUMBER;
    BEGIN
        -- Call existing Citizen_Arrived function
        v_result := Pf_Person_Abroad.Citizen_Arrived(
            o_Out_text   => o_Message,
            p_person_id  => p_Person_Id,
            p_pinpp      => p_Pinfl,
            p_birth_date => p_Birth_Date
        );

        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            o_Message := 'Error checking citizen arrival: ' || SQLERRM;
            RETURN 0;
    END Check_Citizen_Arrival;

    FUNCTION Restore_Arrived_Person(
        p_Person_Id IN NUMBER,
        o_Message   OUT VARCHAR2
    ) RETURN NUMBER
    IS
        v_result NUMBER;
        v_restore_reason VARCHAR2(4000);
    BEGIN
        v_restore_reason := 'Adliya vazirligi huzuridagi Personallashtirish markazi ma''lumotiga asosan qaytib kelgan';

        -- Call existing Restore_Person_Arrived function
        v_result := Restore_Person_Arrived(
            o_Out_Text       => o_Message,
            p_Person_Id      => p_Person_Id,
            p_Restore_Reason => v_restore_reason
        );

        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            o_Message := 'Error restoring person: ' || SQLERRM;
            RETURN 0;
    END Restore_Arrived_Person;

    PROCEDURE Log_Status_Request(
        p_Ws_Id       IN NUMBER,
        p_Pinfl       IN VARCHAR2,
        p_In_Data     IN CLOB,
        p_Result_Code IN NUMBER,
        p_Msg         IN VARCHAR2,
        p_Status      IN NUMBER := NULL
    )
    IS
        PRAGMA AUTONOMOUS_TRANSACTION;
    BEGIN
        INSERT INTO Pf_Exchanges_Ws_Id_Status (
            Ws_Id_Status_Id,
            Ws_Id,
            Pinpp,
            In_Data,
            Result_Code,
            Msg,
            Status,
            Creation_Date
        ) VALUES (
            Pf_Exchanges_Ws_Id_Status_Seq.NEXTVAL,
            p_Ws_Id,
            p_Pinfl,
            p_In_Data,
            p_Result_Code,
            p_Msg,
            p_Status,
            SYSDATE
        );

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            -- Log error but don't fail the main operation
            DBMS_OUTPUT.PUT_LINE('Error logging request: ' || SQLERRM);
    END Log_Status_Request;

END Pf_Person_Abroad_Repository;
/

-- Grant permissions
-- GRANT EXECUTE ON Pf_Person_Repository TO your_application_user;
-- GRANT EXECUTE ON Pf_Person_Abroad_Repository TO your_application_user;
