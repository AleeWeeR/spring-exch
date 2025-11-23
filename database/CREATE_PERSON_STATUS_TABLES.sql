-- ============================================================================
-- Table 1: Person Status Checks (for /check-status endpoint)
-- ============================================================================
CREATE TABLE Pf_Exchange_Person_Statuses (
    Person_Status_Id NUMBER PRIMARY KEY,
    Ws_Id            NUMBER,
    Pinpp            VARCHAR2(14),
    In_Data          CLOB,
    Result_Code      NUMBER,      -- 1=success, 0=error
    Msg              VARCHAR2(4000),
    Status           NUMBER,      -- 1=faol, 2=nofaol(abroad), 3=nofaol(other)
    Data_Sqlerr      VARCHAR2(4000),
    Creation_Date    DATE DEFAULT SYSDATE
);

-- Create sequence for Person_Status_Id
CREATE SEQUENCE Pf_Exchange_Person_Statuses_Seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Create indexes for better performance
CREATE INDEX Idx_Person_Status_Pinpp ON Pf_Exchange_Person_Statuses(Pinpp);
CREATE INDEX Idx_Person_Status_Ws_Id ON Pf_Exchange_Person_Statuses(Ws_Id);
CREATE INDEX Idx_Person_Status_Date ON Pf_Exchange_Person_Statuses(Creation_Date);

-- Add comments
COMMENT ON TABLE Pf_Exchange_Person_Statuses IS 'Check-status endpoint request log - read-only status checks';
COMMENT ON COLUMN Pf_Exchange_Person_Statuses.Person_Status_Id IS 'Primary key';
COMMENT ON COLUMN Pf_Exchange_Person_Statuses.Ws_Id IS 'Web service ID from request';
COMMENT ON COLUMN Pf_Exchange_Person_Statuses.Pinpp IS 'PINFL from request';
COMMENT ON COLUMN Pf_Exchange_Person_Statuses.In_Data IS 'Original request data (XML/JSON)';
COMMENT ON COLUMN Pf_Exchange_Person_Statuses.Result_Code IS '1=success (200), 0=error';
COMMENT ON COLUMN Pf_Exchange_Person_Statuses.Msg IS 'Error message if result=0, empty otherwise';
COMMENT ON COLUMN Pf_Exchange_Person_Statuses.Status IS '1=faol, 2=nofaol(chet elda, close_desc=11), 3=nofaol(boshqa)';
COMMENT ON COLUMN Pf_Exchange_Person_Statuses.Data_Sqlerr IS 'Error details if any';
COMMENT ON COLUMN Pf_Exchange_Person_Statuses.Creation_Date IS 'Request timestamp';

-- ============================================================================
-- Table 2: Restore Status Requests (for /restore-status endpoint)
-- ============================================================================
CREATE TABLE Pf_Exchange_Restore_Statuses (
    Restore_Status_Id NUMBER PRIMARY KEY,
    Ws_Id             NUMBER,
    Pinpp             VARCHAR2(14),
    In_Data           CLOB,
    Result_Code       NUMBER,      -- 0=not found, 1=already active, 2=restored, 3=not arrived
    Msg               VARCHAR2(4000),
    Data_Sqlerr       VARCHAR2(4000),
    Creation_Date     DATE DEFAULT SYSDATE
);

-- Create sequence for Restore_Status_Id
CREATE SEQUENCE Pf_Exchange_Restore_Statuses_Seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Create indexes for better performance
CREATE INDEX Idx_Restore_Status_Pinpp ON Pf_Exchange_Restore_Statuses(Pinpp);
CREATE INDEX Idx_Restore_Status_Ws_Id ON Pf_Exchange_Restore_Statuses(Ws_Id);
CREATE INDEX Idx_Restore_Status_Date ON Pf_Exchange_Restore_Statuses(Creation_Date);

-- Add comments
COMMENT ON TABLE Pf_Exchange_Restore_Statuses IS 'Restore-status endpoint request log - check arrival and restore operations';
COMMENT ON COLUMN Pf_Exchange_Restore_Statuses.Restore_Status_Id IS 'Primary key';
COMMENT ON COLUMN Pf_Exchange_Restore_Statuses.Ws_Id IS 'Web service ID from request';
COMMENT ON COLUMN Pf_Exchange_Restore_Statuses.Pinpp IS 'PINFL from request';
COMMENT ON COLUMN Pf_Exchange_Restore_Statuses.In_Data IS 'Original request data (XML/JSON)';
COMMENT ON COLUMN Pf_Exchange_Restore_Statuses.Result_Code IS '0=not found, 1=already active, 2=restored, 3=not arrived';
COMMENT ON COLUMN Pf_Exchange_Restore_Statuses.Msg IS 'Result message in Uzbek';
COMMENT ON COLUMN Pf_Exchange_Restore_Statuses.Data_Sqlerr IS 'Error details if any';
COMMENT ON COLUMN Pf_Exchange_Restore_Statuses.Creation_Date IS 'Request timestamp';
