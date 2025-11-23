-- Oracle Package Specification
CREATE OR REPLACE PACKAGE PF_EXCHANGES_WS_ID AS
    -- Function to check pensioner status and verify citizenship arrival
    FUNCTION CHECK_PENSIONER_STATUS(
        p_Pinfl   IN VARCHAR2,
        p_Ws_Id   IN NUMBER,
        o_Out_Text OUT CLOB
    ) RETURN NUMBER;
END PF_EXCHANGES_WS_ID;
/

-- Oracle Package Body
CREATE OR REPLACE PACKAGE BODY PF_EXCHANGES_WS_ID AS

    -- Function implementation
    FUNCTION CHECK_PENSIONER_STATUS(
        p_Pinfl   IN VARCHAR2,
        p_Ws_Id   IN NUMBER,
        o_Out_Text OUT CLOB
    ) RETURN NUMBER IS
        v_result_code NUMBER := 0;
        v_message VARCHAR2(500);
        v_status NUMBER;
        v_person_exists NUMBER := 0;
        v_current_status NUMBER;
        v_citizen_arrived NUMBER := 0;
        v_json CLOB;

    BEGIN
        -- Step 1: Check if pensioner exists in the database
        -- This checks your pension recipients table (adjust table name as needed)
        BEGIN
            SELECT 1,
                   CASE WHEN is_active = 'Y' THEN 1 ELSE 0 END
            INTO v_person_exists, v_current_status
            FROM pf_pension_recipients  -- Replace with your actual table name
            WHERE pinfl = p_Pinfl
            AND ROWNUM = 1;

        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_person_exists := 0;
                v_current_status := 0;
        END;

        -- Step 2: If person not found, return 0
        IF v_person_exists = 0 THEN
            v_result_code := 0;
            v_message := 'Pensiya oluvchilar ro''yhatida mavjud emas';
            v_status := NULL;

            -- Build JSON response
            o_Out_Text := '{"result": ' || v_result_code ||
                         ', "msg": "' || v_message ||
                         '", "ws_id": ' || p_Ws_Id || '}';

            RETURN 200; -- Success return code
        END IF;

        -- Step 3: Person exists, check if already active
        IF v_current_status = 1 THEN
            -- Person is active, just return status 1
            v_result_code := 1;
            v_message := '';
            v_status := 1;

            o_Out_Text := '{"result": ' || v_result_code ||
                         ', "msg": "' || v_message ||
                         '", "ws_id": ' || p_Ws_Id ||
                         ', "status": ' || v_status || '}';

            RETURN 200;
        END IF;

        -- Step 4: Person exists but not active, check citizenship arrival status
        -- Call the citizen arrival check function/procedure
        -- This could be a REST API call or another PL/SQL function
        BEGIN
            -- Option 1: If you have a PL/SQL function that checks arrival
            -- v_citizen_arrived := CHECK_CITIZEN_ARRIVAL(p_Pinfl);

            -- Option 2: If you need to call an external REST API
            -- You can use UTL_HTTP or APEX_WEB_SERVICE
            DECLARE
                v_http_request  UTL_HTTP.REQ;
                v_http_response UTL_HTTP.RESP;
                v_response_text VARCHAR2(4000);
                v_url VARCHAR2(500);
            BEGIN
                -- Replace with your actual citizen arrival check endpoint
                v_url := 'http://your-api-endpoint/check-arrival?pinfl=' || p_Pinfl;

                -- Make HTTP request
                v_http_request := UTL_HTTP.BEGIN_REQUEST(v_url, 'GET');
                UTL_HTTP.SET_HEADER(v_http_request, 'Content-Type', 'application/json');

                v_http_response := UTL_HTTP.GET_RESPONSE(v_http_request);

                -- Read response
                UTL_HTTP.READ_TEXT(v_http_response, v_response_text);
                UTL_HTTP.END_RESPONSE(v_http_response);

                -- Parse response to determine if citizen has arrived
                -- Assuming the API returns {"arrived": true/false}
                IF v_response_text LIKE '%"arrived"%true%' THEN
                    v_citizen_arrived := 1;
                ELSE
                    v_citizen_arrived := 0;
                END IF;

            EXCEPTION
                WHEN OTHERS THEN
                    -- If API call fails, assume not arrived
                    v_citizen_arrived := 0;
                    DBMS_OUTPUT.PUT_LINE('Error calling citizen arrival API: ' || SQLERRM);
            END;

        EXCEPTION
            WHEN OTHERS THEN
                v_citizen_arrived := 0;
        END;

        -- Step 5: Process result based on citizenship arrival status
        IF v_citizen_arrived = 1 THEN
            -- Person has returned to Uzbekistan, activate their status
            UPDATE pf_pension_recipients  -- Replace with your actual table name
            SET is_active = 'Y',
                last_updated = SYSDATE,
                updated_by = 'SYSTEM'
            WHERE pinfl = p_Pinfl;

            COMMIT;

            v_result_code := 2;
            v_message := 'O''zgartirildi';
            v_status := NULL;

            o_Out_Text := '{"result": ' || v_result_code ||
                         ', "msg": "' || v_message ||
                         '", "ws_id": ' || p_Ws_Id || '}';

        ELSE
            -- Person has NOT returned to Uzbekistan
            v_result_code := 3;
            v_message := 'O''zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi';
            v_status := 0;

            o_Out_Text := '{"result": ' || v_result_code ||
                         ', "msg": "' || v_message ||
                         '", "ws_id": ' || p_Ws_Id ||
                         ', "status": ' || v_status || '}';
        END IF;

        RETURN 200; -- Success

    EXCEPTION
        WHEN OTHERS THEN
            -- Handle any errors
            o_Out_Text := '{"result": -1, "msg": "Xatolik: ' || SQLERRM || '", "ws_id": ' || p_Ws_Id || '}';
            RETURN 500; -- Error code
    END CHECK_PENSIONER_STATUS;

END PF_EXCHANGES_WS_ID;
/

-- Grant execute permissions (adjust as needed)
-- GRANT EXECUTE ON PF_EXCHANGES_WS_ID TO your_application_user;
