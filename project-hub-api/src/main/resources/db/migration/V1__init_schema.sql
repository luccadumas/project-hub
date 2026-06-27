CREATE TABLE members (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    role        VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE projects (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200)    NOT NULL,
    start_date          DATE            NOT NULL,
    expected_end_date   DATE            NOT NULL,
    actual_end_date     DATE,
    total_budget        NUMERIC(15, 2)  NOT NULL,
    description         TEXT,
    manager_id          BIGINT          NOT NULL REFERENCES members (id),
    status              VARCHAR(50)     NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE project_members (
    project_id  BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    member_id   BIGINT NOT NULL REFERENCES members (id),
    PRIMARY KEY (project_id, member_id)
);

CREATE INDEX idx_projects_status ON projects (status);
CREATE INDEX idx_projects_manager_id ON projects (manager_id);
CREATE INDEX idx_projects_name ON projects (name);
CREATE INDEX idx_project_members_member_id ON project_members (member_id);
