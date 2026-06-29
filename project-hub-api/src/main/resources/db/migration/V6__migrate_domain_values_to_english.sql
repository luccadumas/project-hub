UPDATE members SET role = 'MANAGER' WHERE role = 'GERENTE';
UPDATE members SET role = 'EMPLOYEE' WHERE role = 'FUNCIONARIO';
UPDATE members SET role = 'INTERN' WHERE role = 'ESTAGIARIO';
UPDATE members SET role = 'CONSULTANT' WHERE role = 'CONSULTOR';

UPDATE projects SET status = 'UNDER_ANALYSIS' WHERE status = 'EM_ANALISE';
UPDATE projects SET status = 'ANALYSIS_COMPLETED' WHERE status = 'ANALISE_REALIZADA';
UPDATE projects SET status = 'ANALYSIS_APPROVED' WHERE status = 'ANALISE_APROVADA';
UPDATE projects SET status = 'STARTED' WHERE status = 'INICIADO';
UPDATE projects SET status = 'PLANNED' WHERE status = 'PLANEJADO';
UPDATE projects SET status = 'IN_PROGRESS' WHERE status = 'EM_ANDAMENTO';
UPDATE projects SET status = 'COMPLETED' WHERE status = 'ENCERRADO';
UPDATE projects SET status = 'CANCELED' WHERE status = 'CANCELADO';

UPDATE projects SET risk_level = 'LOW' WHERE risk_level = 'BAIXO';
UPDATE projects SET risk_level = 'MEDIUM' WHERE risk_level = 'MEDIO';
UPDATE projects SET risk_level = 'HIGH' WHERE risk_level = 'ALTO';
