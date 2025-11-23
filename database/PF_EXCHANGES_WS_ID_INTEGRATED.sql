-- Oracle Package for WS ID Status Check
-- Integrated with existing Pf_Person_Abroad.Citizen_Arrived and Restore_Person_Arrived

CREATE OR REPLACE PACKAGE PF_EXCHANGES_WS_ID AS
    FUNCTION Check_Person_Status(
        O_Data OUT CLOB,
        P_Data IN VARCHAR2
    ) RETURN NUMBER;
END PF_EXCHANGES_WS_ID;
/

CREATE OR REPLACE PACKAGE BODY PF_EXCHANGES_WS_ID AS

    --------------------------------------------------------------------------------------------------------------------
    FUNCTION Check_Person_Status(
        O_Data OUT CLOB,
        P_Data IN VARCHAR2
    ) RETURN NUMBER
    IS
        Xml_Data         XMLTYPE;
        R_Row            Pf_Exchanges_Ws_Id_Status%ROWTYPE;  -- You'll need to create this table
        R_Pf_Persons     Pf_Persons%ROWTYPE;
        V_Person_Id      NUMBER;
        V_Step           VARCHAR2(400) := 'initial';
        V_Value          VARCHAR2(4000);
        V_Result_Code    NUMBER := 0;
        V_Msg            VARCHAR2(4000);
        V_Status         NUMBER;
        V_Restore_Reason VARCHAR2(4000);

        --------------------------------------------------------------------------------------------------------------------
        FUNCTION Finish_Request(
            P_Result_Code  IN NUMBER,
            P_Msg          IN VARCHAR2,
            P_Status       IN NUMBER := NULL,
            P_Data_Sqlerr  IN CLOB := NULL
        ) RETURN NUMBER IS
        BEGIN
            R_Row.Ws_Id_Status_Id := Pf_Exchanges_Ws_Id_Status_Seq.NEXTVAL;
            R_Row.In_Data := P_Data;
            R_Row.Result_Code := P_Result_Code;
            R_Row.Msg := P_Msg;
            R_Row.Status := P_Status;
            R_Row.Data_Sqlerr := SUBSTR(P_Data_Sqlerr, 1, 4000);
            R_Row.Creation_Date := SYSDATE;

            -- Build JSON response based on result code
            IF P_Result_Code = 0 THEN
                -- Not found
                O_Data := '{
                    "result": ' || P_Result_Code || ',
                    "msg": ' || Ensure_Json_Element(P_Msg) || ',
                    "ws_id": ' || Ensure_Json_Element(R_Row.Ws_Id) || '
                }';
            ELSIF P_Result_Code = 1 THEN
                -- Found and active
                O_Data := '{
                    "result": ' || P_Result_Code || ',
                    "msg": ' || Ensure_Json_Element(P_Msg) || ',
                    "ws_id": ' || Ensure_Json_Element(R_Row.Ws_Id) || ',
                    "status": ' || Ensure_Json_Element(P_Status) || '
                }';
            ELSIF P_Result_Code = 2 THEN
                -- Activated
                O_Data := '{
                    "result": ' || P_Result_Code || ',
                    "msg": ' || Ensure_Json_Element(P_Msg) || ',
                    "ws_id": ' || Ensure_Json_Element(R_Row.Ws_Id) || '
                }';
            ELSIF P_Result_Code = 3 THEN
                -- Not arrived
                O_Data := '{
                    "result": ' || P_Result_Code || ',
                    "msg": ' || Ensure_Json_Element(P_Msg) || ',
                    "ws_id": ' || Ensure_Json_Element(R_Row.Ws_Id) || ',
                    "status": ' || Ensure_Json_Element(P_Status) || '
                }';
            END IF;

            INSERT INTO Pf_Exchanges_Ws_Id_Status VALUES R_Row;
            COMMIT;
            RETURN 1;

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
        BEGIN
            SELECT *
            INTO R_Pf_Persons
            FROM Pf_Persons
            WHERE Pinpp = R_Row.Pinpp
              AND Person_Type = '01'
              AND ROWNUM = 1;

            V_Person_Id := R_Pf_Persons.Person_Id;

        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                V_Person_Id := NULL;
        END;

        -- Step 1: Check if person exists
        IF V_Person_Id IS NULL THEN
            RETURN Finish_Request(0, 'Pensiya oluvchilar ro''yhatida mavjud emas');
        END IF;

        -- Step 2: Check if person is active
        IF R_Pf_Persons.Close_Reason IS NULL
           AND R_Pf_Persons.Close_Date IS NULL
           AND R_Pf_Persons.Close_Desc IS NULL THEN
            -- Person is active
            RETURN Finish_Request(1, '', 1);
        END IF;

        -- Step 3: Person is not active, check if they need to be restored
        V_Step := 'check_citizen_arrived';

        -- Check if person has closed status that requires arrival check
        IF R_Pf_Persons.Close_Reason IS NOT NULL
           OR R_Pf_Persons.Close_Date IS NOT NULL
           OR R_Pf_Persons.Close_Desc = '11' THEN

            -- Call the existing Citizen_Arrived function
            IF Pf_Person_Abroad.Citizen_Arrived(
                o_Out_text   => V_Msg,
                p_person_id  => R_Pf_Persons.Person_Id,
                p_pinpp      => R_Pf_Persons.Pinpp,
                p_birth_date => R_Pf_Persons.Birth_Date
            ) = 1 THEN

                -- Person has arrived, restore them
                V_Step := 'restore_person';
                V_Restore_Reason := 'Adliya vazirligi huzuridagi Personallashtirish markazi ma''lumotiga asosan qaytib kelgan';

                IF Restore_Person_Arrived(
                    o_Out_Text       => V_Msg,
                    p_Person_Id      => R_Pf_Persons.Person_Id,
                    p_Restore_Reason => V_Restore_Reason
                ) = 1 THEN
                    -- Successfully restored
                    RETURN Finish_Request(2, 'O''zgartirildi');
                ELSE
                    -- Restore failed, but person arrived
                    RETURN Finish_Request(2, V_Msg);
                END IF;

            ELSE
                -- Person has NOT arrived
                V_Msg := 'O''zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi';
                RETURN Finish_Request(3, V_Msg, 0);
            END IF;

        ELSE
            -- Person is closed but not for abroad reasons
            RETURN Finish_Request(3, 'O''zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi', 0);
        END IF;

    EXCEPTION
        WHEN OTHERS THEN
            RETURN Finish_Request(
                0,
                'Ma''lumotni qayta ishlashda xatolik. [' || V_Step || ']',
                NULL,
                SQLERRM || Core_Const.C_New_Line || DBMS_UTILITY.Format_Error_Backtrace()
            );
    END Check_Person_Status;

END PF_EXCHANGES_WS_ID;
/

-- Grant permissions
-- GRANT EXECUTE ON PF_EXCHANGES_WS_ID TO your_application_user;
