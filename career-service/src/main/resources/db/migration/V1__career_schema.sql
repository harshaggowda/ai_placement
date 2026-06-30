-- Career Service - Initial Schema
-- V1__career_schema.sql

-- 1. Companies (Catalog of target companies)
CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    industry VARCHAR(100),
    hiring_status VARCHAR(50),
    interview_difficulty VARCHAR(50),
    tech_stack JSONB,
    placement_category VARCHAR(100),
    eligibility_criteria TEXT,
    hiring_seasons JSONB,
    website_url VARCHAR(255),
    logo_url VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 2. Company Preparations (User specific tracking)
CREATE TABLE company_preparations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    company_id UUID NOT NULL REFERENCES companies(id),
    preparation_status VARCHAR(50) NOT NULL,
    priority VARCHAR(50),
    completion_percentage INT DEFAULT 0,
    study_progress JSONB,
    notes TEXT,
    bookmarks JSONB,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE (user_id, company_id)
);

-- 3. Resumes
CREATE TABLE resumes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    file_url VARCHAR(512) NOT NULL,
    status VARCHAR(50) NOT NULL,
    version_number INT NOT NULL DEFAULT 1,
    tags JSONB,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 4. Resume Analysis Metadata (AI analysis data)
CREATE TABLE resume_analysis_metadata (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL REFERENCES resumes(id),
    analysis_id VARCHAR(100),
    resume_version INT NOT NULL,
    analysis_status VARCHAR(50) NOT NULL,
    ai_provider VARCHAR(100),
    generated_time TIMESTAMP WITH TIME ZONE,
    processing_time_ms BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 5. Applications (Job Applications)
CREATE TABLE applications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    company_id UUID REFERENCES companies(id),
    company_name VARCHAR(255),
    role_title VARCHAR(255) NOT NULL,
    application_status VARCHAR(50) NOT NULL,
    applied_date DATE,
    interview_date DATE,
    offer_date DATE,
    rejection_reason TEXT,
    recruiter_notes TEXT,
    status_history JSONB,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 6. Interviews
CREATE TABLE interviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    application_id UUID NOT NULL REFERENCES applications(id),
    interview_round VARCHAR(100) NOT NULL,
    interview_date TIMESTAMP WITH TIME ZONE,
    duration_minutes INT,
    interviewer_name VARCHAR(255),
    questions JSONB,
    answers JSONB,
    feedback TEXT,
    rating INT,
    notes TEXT,
    result VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 7. Learning Resources
CREATE TABLE learning_resources (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    url VARCHAR(512),
    difficulty VARCHAR(50),
    tags JSONB,
    categories JSONB,
    is_template BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 8. User Bookmarked Resources
CREATE TABLE user_learning_bookmarks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    resource_id UUID NOT NULL REFERENCES learning_resources(id),
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE (user_id, resource_id)
);

-- 9. Projects
CREATE TABLE projects (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    github_url VARCHAR(512),
    demo_url VARCHAR(512),
    tech_stack JSONB,
    tags JSONB,
    project_status VARCHAR(50),
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 10. Certificates
CREATE TABLE certificates (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    issuer VARCHAR(255) NOT NULL,
    issue_date DATE,
    expiry_date DATE,
    credential_url VARCHAR(512),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 11. Daily Tasks (Planner)
CREATE TABLE daily_tasks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    priority VARCHAR(50),
    estimated_time_minutes INT,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    reminder_time TIMESTAMP WITH TIME ZONE,
    categories JSONB,
    due_date DATE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 12. Progress Snapshots
CREATE TABLE progress_snapshots (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    snapshot_type VARCHAR(50) NOT NULL,
    snapshot_date DATE NOT NULL,
    overall_readiness INT DEFAULT 0,
    interview_readiness INT DEFAULT 0,
    resume_readiness INT DEFAULT 0,
    learning_progress INT DEFAULT 0,
    metrics JSONB,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE (user_id, snapshot_type, snapshot_date)
);

-- Indexes for performance
CREATE INDEX idx_companies_name ON companies(name);
CREATE INDEX idx_company_preparations_user_id ON company_preparations(user_id);
CREATE INDEX idx_resumes_user_id ON resumes(user_id);
CREATE INDEX idx_applications_user_id ON applications(user_id);
CREATE INDEX idx_interviews_user_id ON interviews(user_id);
CREATE INDEX idx_projects_user_id ON projects(user_id);
CREATE INDEX idx_certificates_user_id ON certificates(user_id);
CREATE INDEX idx_daily_tasks_user_date ON daily_tasks(user_id, due_date);
