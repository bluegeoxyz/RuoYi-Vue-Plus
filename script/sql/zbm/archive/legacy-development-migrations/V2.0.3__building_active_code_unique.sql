-- Run after checking: SELECT project_id, building_code, COUNT(*) FROM pm_building WHERE del_flag = '0' GROUP BY project_id, building_code HAVING COUNT(*) > 1;
ALTER TABLE pm_building
    ADD COLUMN active_building_code VARCHAR(32)
        GENERATED ALWAYS AS (CASE WHEN del_flag = '0' THEN building_code ELSE NULL END) STORED,
    ADD UNIQUE KEY uk_pm_building_active_code (project_id, active_building_code);
