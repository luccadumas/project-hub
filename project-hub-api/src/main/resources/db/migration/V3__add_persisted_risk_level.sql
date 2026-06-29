ALTER TABLE projects
    ADD COLUMN risk_level VARCHAR(50);

UPDATE projects
SET risk_level = CASE
    WHEN total_budget > 500000
        OR (
            EXTRACT(YEAR FROM age(expected_end_date, start_date)) * 12
            + EXTRACT(MONTH FROM age(expected_end_date, start_date))
        ) > 6 THEN 'HIGH'
    WHEN (
            total_budget > 100000
            AND total_budget <= 500000
        )
        OR (
            (
                EXTRACT(YEAR FROM age(expected_end_date, start_date)) * 12
                + EXTRACT(MONTH FROM age(expected_end_date, start_date))
            ) > 3
            AND (
                EXTRACT(YEAR FROM age(expected_end_date, start_date)) * 12
                + EXTRACT(MONTH FROM age(expected_end_date, start_date))
            ) <= 6
        ) THEN 'MEDIUM'
    ELSE 'LOW'
END;

ALTER TABLE projects
    ALTER COLUMN risk_level SET NOT NULL;

CREATE INDEX idx_projects_risk_level ON projects (risk_level);
