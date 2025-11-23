-- Table to track WS ID status check requests
-- Similar to PF_EXCHANGES_EP_CHARGED_INFO

CREATE TABLE Pf_Exchanges_Ws_Id_Status (
    Ws_Id_Status_Id NUMBER PRIMARY KEY,
    Ws_Id           NUMBER,
    Pinpp           VARCHAR2(14),
    In_Data         CLOB,
    Result_Code     NUMBER,
    Msg             VARCHAR2(4000),
    Status          NUMBER,
    Data_Sqlerr     VARCHAR2(4000),
    Creation_Date   DATE DEFAULT SYSDATE
);

-- Create sequence for primary key
CREATE SEQUENCE Pf_Exchanges_Ws_Id_Status_Seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Create indexes for better performance
CREATE INDEX Idx_Ws_Id_Status_Pinpp ON Pf_Exchanges_Ws_Id_Status(Pinpp);
CREATE INDEX Idx_Ws_Id_Status_Ws_Id ON Pf_Exchanges_Ws_Id_Status(Ws_Id);
CREATE INDEX Idx_Ws_Id_Status_Date ON Pf_Exchanges_Ws_Id_Status(Creation_Date);

-- Add comments
COMMENT ON TABLE Pf_Exchanges_Ws_Id_Status IS 'Person abroad status check and restore request log - supports two endpoints';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Ws_Id_Status_Id IS 'Primary key';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Ws_Id IS 'Web service ID from request';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Pinpp IS 'PINFL from request';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.In_Data IS 'Original request data (JSON)';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Result_Code IS 'Check-status: 1=success,0=error | Restore-status: 0=not found,1=already active,2=restored,3=not arrived';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Msg IS 'Result message in Uzbek';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Status IS 'Only for check-status: 1=faol,2=nofaol(chet elda),3=nofaol(boshqa) | NULL for restore-status';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Data_Sqlerr IS 'Error details if any';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Creation_Date IS 'Request timestamp';
