INSERT INTO members (name, role) VALUES
    ('Ana Silva', 'MANAGER'),
    ('Bruno Costa', 'EMPLOYEE'),
    ('Carla Mendes', 'EMPLOYEE'),
    ('Diego Alves', 'EMPLOYEE'),
    ('Elisa Rocha', 'INTERN');

INSERT INTO projects (name, start_date, expected_end_date, actual_end_date, total_budget, description, manager_id, status, risk_level)
VALUES
    ('Portal Interno', '2025-01-10', '2025-04-10', NULL, 85000.00,
     'Modernização do portal interno para funcionários.', 1, 'IN_PROGRESS', 'LOW'),
    ('Migração Cloud', '2025-03-01', '2025-09-01', NULL, 320000.00,
     'Migração de cargas de trabalho legadas para a nuvem.', 1, 'PLANNED', 'MEDIUM');

INSERT INTO project_members (project_id, member_id) VALUES
    (1, 2),
    (1, 3),
    (2, 2),
    (2, 4);
