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
COMMENT ON TABLE Pf_Exchanges_Ws_Id_Status IS 'WS ID pensioner status check request log';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Ws_Id_Status_Id IS 'Primary key';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Ws_Id IS 'Web service ID from request';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Pinpp IS 'PINFL from request';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.In_Data IS 'Original request data (XML)';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Result_Code IS 'Result code: 0=not found, 1=active, 2=activated, 3=not arrived';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Msg IS 'Result message';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Status IS 'Person status (1=active, 0=inactive)';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Data_Sqlerr IS 'Error details if any';
COMMENT ON COLUMN Pf_Exchanges_Ws_Id_Status.Creation_Date IS 'Request timestamp';
