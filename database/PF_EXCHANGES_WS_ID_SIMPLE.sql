-- Simplified Oracle Package for Pensioner Status Check
-- This version assumes you have a separate function to check citizen arrival

CREATE OR REPLACE PACKAGE PF_EXCHANGES_WS_ID AS
    FUNCTION CHECK_PENSIONER_STATUS(
        p_Pinfl   IN VARCHAR2,
        p_Ws_Id   IN NUMBER,
        o_Out_Text OUT CLOB
    ) RETURN NUMBER;
END PF_EXCHANGES_WS_ID;
/

CREATE OR REPLACE PACKAGE BODY PF_EXCHANGES_WS_ID AS

    -- Helper function to call citizen arrival check endpoint
    -- Returns 1 if citizen has arrived, 0 if not
    FUNCTION check_citizen_arrived(p_pinfl VARCHAR2) RETURN NUMBER IS
        v_arrived NUMBER := 0;
        -- Add your implementation here
        -- This could call another table, procedure, or REST API
    BEGIN
        -- Example: Check a citizen_arrivals table
        BEGIN
            SELECT CASE WHEN arrival_status = 'ARRIVED' THEN 1 ELSE 0 END
            INTO v_arrived
            FROM citizen_arrivals
            WHERE pinfl = p_pinfl
            AND arrival_date >= SYSDATE - 30  -- Within last 30 days
            AND ROWNUM = 1
            ORDER BY arrival_date DESC;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_arrived := 0;
        END;

        RETURN v_arrived;
    END check_citizen_arrived;

    -- Main function
    FUNCTION CHECK_PENSIONER_STATUS(
        p_Pinfl   IN VARCHAR2,
        p_Ws_Id   IN NUMBER,
        o_Out_Text OUT CLOB
    ) RETURN NUMBER IS
        v_person_count NUMBER := 0;
        v_is_active VARCHAR2(1);
        v_result_code NUMBER;
        v_message VARCHAR2(500);
        v_citizen_arrived NUMBER;
    BEGIN
        -- Step 1: Check if pensioner exists
        SELECT COUNT(*), MAX(is_active)
        INTO v_person_count, v_is_active
        FROM pf_pension_recipients  -- Replace with your actual table
        WHERE pinfl = p_Pinfl;

        -- Step 2: Person not found → return 0
        IF v_person_count = 0 THEN
            o_Out_Text := '{"result": 0, "msg": "Pensiya oluvchilar ro''yhatida mavjud emas", "ws_id": ' || p_Ws_Id || '}';
            RETURN 200;
        END IF;

        -- Step 3: Person found and active → return 1
        IF v_is_active = 'Y' THEN
            o_Out_Text := '{"result": 1, "msg": "", "ws_id": ' || p_Ws_Id || ', "status": 1}';
            RETURN 200;
        END IF;

        -- Step 4: Person found but not active, check citizenship arrival
        v_citizen_arrived := check_citizen_arrived(p_Pinfl);

        IF v_citizen_arrived = 1 THEN
            -- Person has returned → activate and return 2
            UPDATE pf_pension_recipients
            SET is_active = 'Y',
                last_updated = SYSDATE
            WHERE pinfl = p_Pinfl;

            COMMIT;

            o_Out_Text := '{"result": 2, "msg": "O''zgartirildi", "ws_id": ' || p_Ws_Id || '}';
            RETURN 200;
        ELSE
            -- Person has not returned → return 3
            o_Out_Text := '{"result": 3, "msg": "O''zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi", "ws_id": ' || p_Ws_Id || ', "status": 0}';
            RETURN 200;
        END IF;

    EXCEPTION
        WHEN OTHERS THEN
            o_Out_Text := '{"result": -1, "msg": "Xatolik: ' || SQLERRM || '", "ws_id": ' || p_Ws_Id || '}';
            RETURN 500;
    END CHECK_PENSIONER_STATUS;

END PF_EXCHANGES_WS_ID;
/
