-- =============================================================================
-- 修复 lab.manager_id 脏数据，解决 Hibernate/MySQL 报错：
-- Cannot add or update a child row: foreign key ... manager_id -> tb_user(user_id)
-- =============================================================================
-- 在以下任一情况后执行本脚本再启动应用（或手动 ADD CONSTRAINT）：
--   - 从 admin_id 改名为 manager_id 后数据未对齐
--   - 历史数据里 manager_id 指向已删除用户
-- =============================================================================

-- 0) 常见脏数据：manager_id = 0（tb_user 通常从 1 自增，不存在 user_id=0）
-- UPDATE lab l INNER JOIN lab_category c ON c.id = l.category_id
-- INNER JOIN tb_user uc ON uc.user_id = c.manager_id
-- SET l.manager_id = c.manager_id
-- WHERE l.manager_id = 0 AND c.manager_id IS NOT NULL;

-- 1) 诊断：哪些实验室的 manager_id 在用户表里不存在（或为空）
SELECT l.id AS lab_id, l.lab_code, l.manager_id, l.category_id
FROM lab l
LEFT JOIN tb_user u ON u.user_id = l.manager_id
WHERE l.manager_id IS NULL OR l.manager_id = 0 OR u.user_id IS NULL;

-- 2) 用「所属分类」上已存在的 manager_id 回填（分类管理员必须在用户表存在）
UPDATE lab l
INNER JOIN lab_category c ON c.id = l.category_id
LEFT JOIN tb_user ul ON ul.user_id = l.manager_id
INNER JOIN tb_user uc ON uc.user_id = c.manager_id
SET l.manager_id = c.manager_id
WHERE (l.manager_id IS NULL OR l.manager_id = 0 OR ul.user_id IS NULL)
  AND c.manager_id IS NOT NULL;

-- 3) 若第 1 步仍有残留行，请人工指定一个有效的实验室管理员 user_id 再执行（把 1 改成真实 ID）：
-- UPDATE lab l
-- LEFT JOIN tb_user u ON u.user_id = l.manager_id
-- SET l.manager_id = 1
-- WHERE l.manager_id IS NULL OR l.manager_id = 0 OR u.user_id IS NULL;

-- 4) 再次确认应无脏数据（结果应为空集）
SELECT l.id AS lab_id, l.manager_id
FROM lab l
LEFT JOIN tb_user u ON u.user_id = l.manager_id
WHERE l.manager_id IS NULL OR l.manager_id = 0 OR u.user_id IS NULL;

-- 5) 若 Hibernate 未成功加上外键，可手工添加（约束名可按需修改，与 SHOW CREATE TABLE lab 核对避免重复）：
-- ALTER TABLE lab
--   ADD CONSTRAINT fk_lab_manager FOREIGN KEY (manager_id) REFERENCES tb_user (user_id);
