CREATE TRIGGER INSERT_CUST_TO_MEMBER
AFTER
INSERT ON YRB_CUSTOMER REFERENCING OLD AS OLD_ROW NEW AS NEW_ROW FOR EACH ROW MODE DB2SQL
  WHEN (NEW_ROW.CID > 0) BEGIN ATOMIC
INSERT INTO YRB_MEMBER (CID, CLUB)
VALUES
  (NEW.CID, 'Basic');
END;
INSERT INTO DB2INST1.YRB_CUSTOMER (CID, CITY, NAME)
VALUES
  (10511, 'Blajinder', 'Haryana');
CREATE
  OR REPLACE TRIGGER yrb_customer_insert_res BEFORE
INSERT ON YRB_PURCHASE REFERENCING NEW AS NEW_ROW FOR EACH ROW
select
  CASE
    WHEN count(1) > 0 then 1
    else RAISE_ERROR(
      '90001',
      'Cant find the club and cid combination registered in member table'
    )
  end
from YRB_MEMBER
where
  club = NEW_ROW.club
  and cid = NEW_ROW.cid
INSERT INTO DB2INST1.YRB_PURCHASE (
    CID,
    CLUB,
    QNTY,
    TITLE,
    WHEN,
    YEAR
  )
VALUES
  (
    101,
    'Basic',
    1,
    'Will Snoopy find Lucy?',
    now(),
    1985
  );