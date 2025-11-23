-- ============================================================================
-- Package: PF_EXCHANGES_ABROAD
-- Person Abroad Status Check and Restore Functions
--
-- TWO ENDPOINTS SUPPORTED:
-- 1. /check-status - Read-only status check
--    Response: {"result": 1, "msg": "", "ws_id": 77, "status": 1}
--    - result: 1=success (200), 0=error
--    - status: 1=faol, 2=nofaol(chet elda, close_desc=11), 3=nofaol(boshqa)
--    - Logs to: Pf_Exchange_Person_Statuses
--
-- 2. /restore-status - Check arrival and restore if returned
--    Response: {"result": 2, "msg": "O'zgartirildi", "ws_id": 77}
--    - result: 0=not found, 1=already active, 2=restored, 3=not arrived
--    - Logs to: Pf_Exchange_Restore_Statuses
-- ============================================================================

CREATE OR REPLACE PACKAGE PF_EXCHANGES_ABROAD IS

    -- Check person status (read-only, no restoration)
    FUNCTION Check_Person_Status(
        O_Data OUT CLOB,
        P_Data IN VARCHAR2
    ) RETURN NUMBER;

    -- Check arrival and restore person if needed
    FUNCTION Restore_Person_Status(
        O_Data OUT CLOB,
        P_Data IN VARCHAR2
    ) RETURN NUMBER;

END PF_EXCHANGES_ABROAD;
/

CREATE OR REPLACE PACKAGE BODY PF_EXCHANGES_ABROAD IS

    -- ========================================================================
    -- Helper function: Ensure JSON element (Number)
    -- ========================================================================
    FUNCTION Ensure_Json_Element(P_Val NUMBER, P_Alt VARCHAR2 := '""')
    RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE
                   WHEN P_Val IS NOT NULL AND P_Val != TO_CHAR(0) THEN TO_CHAR(P_Val)
                   ELSE P_Alt
               END;
    END Ensure_Json_Element;

    -- ========================================================================
    -- Helper function: Ensure JSON element (Varchar2)
    -- ========================================================================
    FUNCTION Ensure_Json_Element(P_Val VARCHAR2, P_Alt VARCHAR2 := '""', P_Quote BOOLEAN := TRUE)
    RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE
                   WHEN P_Val IS NOT NULL AND P_Val != TO_CHAR(0) THEN
                       CASE WHEN P_Quote THEN '"' ELSE '' END ||
                       REPLACE(REPLACE(REPLACE(P_Val, '"', '\"'), '''', ''), '\', '') ||
                       CASE WHEN P_Quote THEN '"' ELSE '' END
                   ELSE P_Alt
               END;
    END Ensure_Json_Element;

    -- ========================================================================
    -- Function 1: Check Person Status (Read-Only)
    -- ========================================================================
    FUNCTION Check_Person_Status(
        O_Data OUT CLOB,
        P_Data IN VARCHAR2
    ) RETURN NUMBER IS
        Xml_Data       XMLTYPE;
        R_Row          Pf_Exchange_Person_Statuses%ROWTYPE;
        V_Person_Id    NUMBER;
        V_Step         VARCHAR2(400) := 'initial';
        V_Value        VARCHAR2(4000);
        V_Active       NUMBER := 0;
        V_Close_Reason VARCHAR2(100);
        V_Close_Date   DATE;
        V_Close_Desc   VARCHAR2(100);

        --------------------------------------------------------------------------------------------------------------------
        FUNCTION Finish_Request(P_Result_Code IN NUMBER,
                                P_Status IN NUMBER,
                                P_Msg IN VARCHAR2,
                                P_Data_Sqlerr IN CLOB := NULL)
        RETURN NUMBER IS
        BEGIN
            R_Row.Person_Status_Id := Pf_Exchange_Person_Statuses_Seq.NEXTVAL;
            R_Row.In_Data := P_Data;
            R_Row.Result_Code := P_Result_Code;
            R_Row.Status := P_Status;
            R_Row.Msg := P_Msg;
            R_Row.Data_Sqlerr := SUBSTR(P_Data_Sqlerr, 1, 4000);
            R_Row.Creation_Date := SYSDATE;

            O_Data := '{
    "result": ' || Ensure_Json_Element(P_Result_Code) || ',
    "msg": ' || Ensure_Json_Element(P_Msg) || ',
    "ws_id": ' || Ensure_Json_Element(R_Row.Ws_Id) || ',
    "status": ' || Ensure_Json_Element(P_Status, 'null') || '
}';

            INSERT INTO Pf_Exchange_Person_Statuses VALUES R_Row;
            COMMIT;
            RETURN CASE WHEN P_Result_Code = 0 THEN 0 ELSE 1 END;
        EXCEPTION
            WHEN OTHERS THEN
                V_Value := SUBSTR(P_Msg || Core_Const.C_New_Line || SQLERRM || Core_Const.C_New_Line ||
                                  DBMS_UTILITY.Format_Error_Backtrace(), 1, 3798);
                O_Data := '{
    "result": 0,
    "msg": ' || Ensure_Json_Element(V_Value) || ',
    "ws_id": ' || Ensure_Json_Element(R_Row.Ws_Id) || ',
    "status": null
}';
                RETURN 0;
        END Finish_Request;
        --------------------------------------------------------------------------------------------------------------------
    BEGIN
        V_Step := 'request_body';
        Xml_Data := XMLTYPE(Pf_Exchange_Online.Convert_To_Xml('Request', '<Request>' || P_Data || '</Request>'));

        V_Step := 'retrieve_ws_id';
        V_Value := Pf_Exchange_Online.Get_Xml_Param('Data/ws_id', Xml_Data);
        IF V_Value IS NULL OR NOT REGEXP_LIKE(V_Value, '^\d+$') THEN
            RETURN Finish_Request(0, NULL, 'Invalid ws_id - ' || NVL(V_Value, 'null'));
        ELSE
            R_Row.Ws_Id := V_Value;
        END IF;

        V_Step := 'retrieve_pinfl';
        V_Value := Pf_Exchange_Online.Get_Xml_Param('Data/pinfl', Xml_Data);
        IF V_Value IS NULL OR LENGTH(V_Value) != 14 OR NOT REGEXP_LIKE(V_Value, '^\d+$') THEN
            RETURN Finish_Request(0, NULL, 'Invalid pinfl - ' || NVL(V_Value, 'null'));
        ELSE
            R_Row.Pinpp := V_Value;
        END IF;

        V_Step := 'look_for_person';
        SELECT Person_Id,
               CASE
                   WHEN Close_Reason IS NULL AND Close_Date IS NULL AND Close_Desc IS NULL THEN 1
                   ELSE 0
               END,
               Close_Reason,
               Close_Date,
               Close_Desc
        INTO V_Person_Id, V_Active, V_Close_Reason, V_Close_Date, V_Close_Desc
        FROM DUAL
                 LEFT JOIN Pf_Persons ON Pinpp = R_Row.Pinpp
            AND Person_Type = '01';

        -- Case: Person not found
        IF V_Person_Id IS NULL THEN
            RETURN Finish_Request(0, NULL, 'Pensiya oluvchilar ro''yhatida mavjud emas');
        END IF;

        -- Case: Person found and active
        IF V_Active = 1 THEN
            RETURN Finish_Request(1, 1, '');
        END IF;

        -- Person is inactive - check WHY (close_desc)
        V_Step := 'check_close_reason';

        -- Case: Inactive because abroad (close_desc=11)
        IF V_Close_Desc = '11' THEN
            RETURN Finish_Request(1, 2, '');
        END IF;

        -- Case: Inactive for other reasons
        RETURN Finish_Request(1, 3, '');

    EXCEPTION
        WHEN OTHERS THEN
            RETURN Finish_Request(0,
                                  NULL,
                                  'Ma''lumotni qayta ishlashda xatolik. [' || V_Step || ']',
                                  SQLERRM || Core_Const.C_New_Line || DBMS_UTILITY.Format_Error_Backtrace());
    END Check_Person_Status;

    -- ========================================================================
    -- Function 2: Restore Person Status (Check Arrival & Restore)
    -- ========================================================================
    FUNCTION Restore_Person_Status(
        O_Data OUT CLOB,
        P_Data IN VARCHAR2
    ) RETURN NUMBER IS
        Xml_Data         XMLTYPE;
        R_Row            Pf_Exchange_Restore_Statuses%ROWTYPE;
        V_Person_Id      NUMBER;
        V_Step           VARCHAR2(400) := 'initial';
        V_Value          VARCHAR2(4000);
        V_Active         NUMBER := 0;
        V_Birth_Date     DATE;
        V_Arrived        NUMBER;
        V_Restored       NUMBER;
        V_Arrival_Msg    VARCHAR2(4000);
        V_Restore_Msg    VARCHAR2(4000);
        V_Restore_Reason VARCHAR2(4000);

        --------------------------------------------------------------------------------------------------------------------
        FUNCTION Finish_Request(P_Result_Code IN NUMBER,
                                P_Msg IN VARCHAR2,
                                P_Data_Sqlerr IN CLOB := NULL)
        RETURN NUMBER IS
        BEGIN
            R_Row.Restore_Status_Id := Pf_Exchange_Restore_Statuses_Seq.NEXTVAL;
            R_Row.In_Data := P_Data;
            R_Row.Result_Code := P_Result_Code;
            R_Row.Msg := P_Msg;
            R_Row.Data_Sqlerr := SUBSTR(P_Data_Sqlerr, 1, 4000);
            R_Row.Creation_Date := SYSDATE;

            O_Data := '{
    "result": ' || Ensure_Json_Element(P_Result_Code) || ',
    "msg": ' || Ensure_Json_Element(P_Msg) || ',
    "ws_id": ' || Ensure_Json_Element(R_Row.Ws_Id) || '
}';

            INSERT INTO Pf_Exchange_Restore_Statuses VALUES R_Row;
            COMMIT;
            RETURN CASE WHEN P_Result_Code = 0 THEN 0 ELSE 1 END;
        EXCEPTION
            WHEN OTHERS THEN
                V_Value := SUBSTR(P_Msg || Core_Const.C_New_Line || SQLERRM || Core_Const.C_New_Line ||
                                  DBMS_UTILITY.Format_Error_Backtrace(), 1, 3798);
                O_Data := '{
    "result": 0,
    "msg": ' || Ensure_Json_Element(V_Value) || ',
    "ws_id": ' || Ensure_Json_Element(R_Row.Ws_Id) || '
}';
                RETURN 0;
        END Finish_Request;
        --------------------------------------------------------------------------------------------------------------------
    BEGIN
        V_Step := 'request_body';
        Xml_Data := XMLTYPE(Pf_Exchange_Online.Convert_To_Xml('Request', '<Request>' || P_Data || '</Request>'));

        V_Step := 'retrieve_ws_id';
        V_Value := Pf_Exchange_Online.Get_Xml_Param('Data/ws_id', Xml_Data);
        IF V_Value IS NULL OR NOT REGEXP_LIKE(V_Value, '^\d+$') THEN
            RETURN Finish_Request(0, 'Invalid ws_id - ' || NVL(V_Value, 'null'));
        ELSE
            R_Row.Ws_Id := V_Value;
        END IF;

        V_Step := 'retrieve_pinfl';
        V_Value := Pf_Exchange_Online.Get_Xml_Param('Data/pinfl', Xml_Data);
        IF V_Value IS NULL OR LENGTH(V_Value) != 14 OR NOT REGEXP_LIKE(V_Value, '^\d+$') THEN
            RETURN Finish_Request(0, 'Invalid pinfl - ' || NVL(V_Value, 'null'));
        ELSE
            R_Row.Pinpp := V_Value;
        END IF;

        V_Step := 'look_for_person';
        SELECT Person_Id,
               Birth_Date,
               CASE
                   WHEN Close_Reason IS NULL AND Close_Date IS NULL AND Close_Desc IS NULL THEN 1
                   ELSE 0
               END
        INTO V_Person_Id, V_Birth_Date, V_Active
        FROM DUAL
                 LEFT JOIN Pf_Persons ON Pinpp = R_Row.Pinpp
            AND Person_Type = '01';

        -- Case 0: Person not found
        IF V_Person_Id IS NULL THEN
            RETURN Finish_Request(0, 'Pensiya oluvchilar ro''yhatida mavjud emas');
        END IF;

        -- Case 1: Person already active
        IF V_Active = 1 THEN
            RETURN Finish_Request(1, 'Pensiya oluvchilar ro''yhatida mavjud');
        END IF;

        -- Person is inactive - check if they can be restored
        V_Step := 'check_citizen_arrival';

        -- Call existing Citizen_Arrived function
        V_Arrived := Pf_Person_Abroad.Citizen_Arrived(
            o_Out_text   => V_Arrival_Msg,
            p_person_id  => V_Person_Id,
            p_pinpp      => R_Row.Pinpp,
            p_birth_date => V_Birth_Date
        );

        IF V_Arrived = 1 THEN
            -- Citizen has arrived - restore them
            V_Step := 'restore_arrived_person';
            V_Restore_Reason := 'Adliya vazirligi huzuridagi Personallashtirish markazi ma''lumotiga asosan qaytib kelgan';

            -- Call existing Restore_Person_Arrived function
            V_Restored := Restore_Person_Arrived(
                o_Out_Text       => V_Restore_Msg,
                p_Person_Id      => V_Person_Id,
                p_Restore_Reason => V_Restore_Reason
            );

            IF V_Restored = 1 THEN
                -- Case 2: Successfully restored
                RETURN Finish_Request(2, 'Oluvchi statusi faol xolatga keltirildi');
            END IF;
        END IF;

        -- Case 3: Citizen has NOT arrived or restoration failed
        RETURN Finish_Request(3, 'O''zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi');

    EXCEPTION
        WHEN OTHERS THEN
            RETURN Finish_Request(CASE
                                      WHEN V_Person_Id IS NULL THEN 0
                                      WHEN V_Active = 1 THEN 1
                                      ELSE 3
                                  END,
                                  'Ma''lumotni qayta ishlashda xatolik. [' || V_Step || ']',
                                  SQLERRM || Core_Const.C_New_Line || DBMS_UTILITY.Format_Error_Backtrace());
    END Restore_Person_Status;

END PF_EXCHANGES_ABROAD;
/

-- Grant permissions
-- GRANT EXECUTE ON PF_EXCHANGES_ABROAD TO your_application_user;
