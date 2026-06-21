INSERT INTO partner (id, name, document, created_at) VALUES
    ('1f2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d', 'Empresa A', '11222333000181', now()),
    ('2a3b4c5d-6e7f-4b8c-9d0e-1f2a3b4c5d6e', 'Empresa B', '44555666000162', now());

INSERT INTO partner_balance (partner_id, total_balance, available_balance, updated_at) VALUES
    ('1f2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d', 10000.00, 10000.00, now()),
    ('2a3b4c5d-6e7f-4b8c-9d0e-1f2a3b4c5d6e', 50000.00, 50000.00, now());
