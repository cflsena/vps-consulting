INSERT INTO partner (id, name, document, created_at) VALUES
    ('c0892412-4fb5-4550-9b91-b3a3e5fe0e68', 'Empresa A', '64781774000180', now()),
    ('53924ce9-7a07-419f-b6c0-3807d4276350', 'Empresa B',  '06228417000192', now()),
    ('ac666aa0-0d46-4a68-b3c8-9a48cb62ad7a', 'Empresa C',   '90468921000176', now());

INSERT INTO partner_credit (partner_id, credit_limit, available_balance, updated_at) VALUES
    ('c0892412-4fb5-4550-9b91-b3a3e5fe0e68', 10000.00,  10000.00,  now()),
    ('53924ce9-7a07-419f-b6c0-3807d4276350', 50000.00,  50000.00,  now()),
    ('ac666aa0-0d46-4a68-b3c8-9a48cb62ad7a', 200000.00, 200000.00, now());
