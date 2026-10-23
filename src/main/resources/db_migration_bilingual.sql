-- ==============================================================================
-- BILINGUAL DATA MIGRATION SCRIPT (POSTGRESQL)
-- Chuyển đổi dữ liệu đơn ngữ cũ sang song ngữ (tiếng Việt & tiếng Anh)
-- ==============================================================================

-- 1. Ngành nghề (Industries)
UPDATE industries 
SET name_vi = COALESCE(name_vi, name), 
    name_en = COALESCE(name_en, name)
WHERE (name_vi IS NULL OR name_en IS NULL) AND name IS NOT NULL;

-- 2. Chuyên ngành (Specializations)
UPDATE specializations 
SET name_vi = COALESCE(name_vi, name), 
    name_en = COALESCE(name_en, name)
WHERE (name_vi IS NULL OR name_en IS NULL) AND name IS NOT NULL;

-- 3. Kỹ năng (Skills)
UPDATE skills 
SET name_vi = COALESCE(name_vi, name), 
    name_en = COALESCE(name_en, name)
WHERE (name_vi IS NULL OR name_en IS NULL) AND name IS NOT NULL;

-- 4. Tin tuyển dụng (Jobs)
UPDATE jobs 
SET name_vi = COALESCE(name_vi, name), 
    name_en = COALESCE(name_en, name),
    description_vi = COALESCE(description_vi, description),
    description_en = COALESCE(description_en, description),
    working_hours_vi = COALESCE(working_hours_vi, working_hour),
    working_hours_en = COALESCE(working_hours_en, working_hour)
WHERE name_vi IS NULL OR name_en IS NULL OR description_vi IS NULL OR description_en IS NULL;

-- Migrate job requirements sang job_requirements_vi và job_requirements_en (nếu bảng tồn tại)
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'job_requirements') THEN
        INSERT INTO job_requirements_vi (job_id, requirement)
        SELECT job_id, requirement FROM job_requirements ON CONFLICT DO NOTHING;
        
        INSERT INTO job_requirements_en (job_id, requirement)
        SELECT job_id, requirement FROM job_requirements ON CONFLICT DO NOTHING;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'job_benefits') THEN
        INSERT INTO job_benefits_vi (job_id, benefit)
        SELECT job_id, benefit FROM job_benefits ON CONFLICT DO NOTHING;
        
        INSERT INTO job_benefits_en (job_id, benefit)
        SELECT job_id, benefit FROM job_benefits ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- 5. Công ty (Companies)
UPDATE companies 
SET description_vi = COALESCE(description_vi, description),
    description_en = COALESCE(description_en, description)
WHERE (description_vi IS NULL OR description_en IS NULL) AND description IS NOT NULL;

-- 6. Thông báo (Notifications)
UPDATE notifications 
SET title_vi = COALESCE(title_vi, title),
    title_en = COALESCE(title_en, title),
    message_vi = COALESCE(message_vi, message),
    message_en = COALESCE(message_en, message)
WHERE (title_vi IS NULL OR title_en IS NULL) AND title IS NOT NULL;

-- 7. Câu hỏi phỏng vấn AI (Interview Questions)
UPDATE interview_questions 
SET question_text_vi = COALESCE(question_text_vi, question_text),
    question_text_en = COALESCE(question_text_en, question_text)
WHERE (question_text_vi IS NULL OR question_text_en IS NULL) AND question_text IS NOT NULL;
