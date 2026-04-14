-- =============================================================================
-- 错误：Field 'manage_id' / 'manager_id' doesn't have a default value
-- 原因：表 lab 上同时存在 manage_id 与 manager_id 且均为 NOT NULL 时，只填一列会报错。
-- 当前 Java 实体 Lab 会在保存时把两列写成同一 user_id（见 manageIdShadow + manager 关联）。
-- 长期仍建议只保留 manager_id 一列并删除 manage_id，然后删掉实体中 manageIdShadow 字段。
-- =============================================================================
-- 先执行：SHOW COLUMNS FROM lab LIKE '%manage%';
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 【推荐·最常见】同时存在 manage_id 与 manager_id，且新建实验室时 manager_id 已正确：
-- 直接删除多余的 manage_id（若该列上有外键，先 SHOW CREATE TABLE lab; 再 DROP FOREIGN KEY）
-- ---------------------------------------------------------------------------
-- ALTER TABLE lab DROP FOREIGN KEY <FK_引用_manage_id_的约束名>;
-- ALTER TABLE lab DROP COLUMN manage_id;

-- 若担心历史数据只在 manage_id 里，先合并再删：
-- UPDATE lab SET manager_id = manage_id
-- WHERE (manager_id IS NULL OR manager_id = 0) AND manage_id IS NOT NULL AND manage_id <> 0;
-- ALTER TABLE lab DROP FOREIGN KEY <如有>;
-- ALTER TABLE lab DROP COLUMN manage_id;

-- ---------------------------------------------------------------------------
-- 仅有 manage_id、没有 manager_id：改名为 manager_id（与 lab_category、JPA 一致）
-- ---------------------------------------------------------------------------
-- ALTER TABLE lab CHANGE COLUMN manage_id manager_id INT NOT NULL;
-- ALTER TABLE lab ADD CONSTRAINT fk_lab_manager FOREIGN KEY (manager_id) REFERENCES tb_user (user_id);

-- ---------------------------------------------------------------------------
-- 说明：当前实体 Lab 已临时映射列 manage_id 以兼容旧库；执行「CHANGE manage_id → manager_id」或仅保留 manager_id 后，
--       请把 Lab.java 中 @JoinColumn(name = "manage_id") 改回 name = "manager_id" 并重新编译。
-- =============================================================================
