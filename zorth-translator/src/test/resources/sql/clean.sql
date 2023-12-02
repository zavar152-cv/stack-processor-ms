DELETE
FROM requests;
ALTER SEQUENCE requests_id_seq RESTART WITH 1;
ALTER SEQUENCE compiler_out_id_seq RESTART WITH 1;
ALTER SEQUENCE debug_messages_id_seq RESTART WITH 1;