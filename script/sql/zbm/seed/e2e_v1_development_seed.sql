/*
 * TASK-V1-005-E development-only E2E fixture.
 * Target: zbm_dev_v1 only. Synthetic namespace: E2E-V1-* / ids 950050000000000xxx.
 * Password for fixture users: admin123 (BCrypt test hash; development use only).
 * Re-runnable: every row uses a fixed primary key and ON DUPLICATE KEY UPDATE.
 */
DELIMITER $$
DROP PROCEDURE IF EXISTS seed_zbm_v1_e2e$$
CREATE PROCEDURE seed_zbm_v1_e2e()
BEGIN
    IF DATABASE() <> 'zbm_dev_v1' OR COALESCE(@zbm_allow_e2e_seed, '') <> 'ZBM_V1_E2E_LOCAL' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Refusing E2E seed outside zbm_dev_v1 or without explicit guard';
    END IF;

    INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,user_type,password,status,del_flag,create_time,remark) VALUES
      (950050000000000101,1761000000000000103,'e2e_customer','E2E客服','sys_user','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0',NOW(),'TASK-V1-005-E synthetic fixture'),
      (950050000000000102,1761000000000000103,'e2e_cashier','E2E收费员','sys_user','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0',NOW(),'TASK-V1-005-E synthetic fixture'),
      (950050000000000103,1761000000000000103,'e2e_multi','E2E多项目经理','sys_user','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0',NOW(),'TASK-V1-005-E synthetic fixture'),
      (950050000000000104,1761000000000000103,'e2e_warehouse','E2E仓库主管','sys_user','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0',NOW(),'TASK-V1-005-E synthetic fixture')
    ON DUPLICATE KEY UPDATE nick_name=VALUES(nick_name),password=VALUES(password),status='0',del_flag='0',remark=VALUES(remark);

    INSERT IGNORE INTO sys_user_role(user_id,role_id) VALUES
      (950050000000000101,102113167622012936),(950050000000000102,102113167622012937),
      (950050000000000103,102113167622012941),(950050000000000104,102113167622012940);

    INSERT INTO hr_employee(id,employee_no,employee_name,dept_id,user_id,employment_status,sort,status,del_flag,create_time,remark) VALUES
      (950050000000000201,'E2E-V1-E01','E2E客服',1761000000000000103,950050000000000101,'ACTIVE',1,'0','0',NOW(),'TASK-V1-005-E synthetic fixture'),
      (950050000000000202,'E2E-V1-E02','E2E收费员',1761000000000000103,950050000000000102,'ACTIVE',2,'0','0',NOW(),'TASK-V1-005-E synthetic fixture'),
      (950050000000000203,'E2E-V1-E03','E2E多项目经理',1761000000000000103,950050000000000103,'ACTIVE',3,'0','0',NOW(),'TASK-V1-005-E synthetic fixture'),
      (950050000000000204,'E2E-V1-E04','E2E仓库主管',1761000000000000103,950050000000000104,'ACTIVE',4,'0','0',NOW(),'TASK-V1-005-E synthetic fixture')
    ON DUPLICATE KEY UPDATE employee_name=VALUES(employee_name),user_id=VALUES(user_id),employment_status='ACTIVE',status='0',del_flag='0';

    INSERT INTO pm_project(id,project_code,project_name,project_short_name,project_type,address,sort,status,del_flag,create_time,remark) VALUES
      (950050000000000301,'E2E-V1-A','E2E物业项目A','E2E-A','RESIDENTIAL','合成测试地址A',1,'0','0',NOW(),'TASK-V1-005-E synthetic fixture'),
      (950050000000000302,'E2E-V1-B','E2E物业项目B','E2E-B','RESIDENTIAL','合成测试地址B',2,'0','0',NOW(),'TASK-V1-005-E synthetic fixture')
    ON DUPLICATE KEY UPDATE project_name=VALUES(project_name),status='0',del_flag='0',remark=VALUES(remark);

    INSERT INTO pm_project_member(id,project_id,employee_id,member_role,is_manager,start_date,member_status,sort,status,del_flag,create_time,remark) VALUES
      (950050000000000311,950050000000000301,950050000000000201,'CUSTOMER_SERVICE','0','2026-01-01','ACTIVE',1,'0','0',NOW(),'TASK-V1-005-E'),
      (950050000000000312,950050000000000301,950050000000000202,'CASHIER','0','2026-01-01','ACTIVE',2,'0','0',NOW(),'TASK-V1-005-E'),
      (950050000000000313,950050000000000301,950050000000000203,'MANAGER','1','2026-01-01','ACTIVE',3,'0','0',NOW(),'TASK-V1-005-E'),
      (950050000000000314,950050000000000302,950050000000000203,'MANAGER','1','2026-01-01','ACTIVE',4,'0','0',NOW(),'TASK-V1-005-E'),
      (950050000000000315,950050000000000301,950050000000000204,'WAREHOUSE','0','2026-01-01','ACTIVE',5,'0','0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE member_status='ACTIVE',status='0',del_flag='0';

    INSERT INTO pm_building(id,project_id,building_code,building_name,building_type,sort,status,del_flag,create_time,remark) VALUES
      (950050000000000401,950050000000000301,'E2E-B01','E2E一号楼','RESIDENTIAL',1,'0','0',NOW(),'TASK-V1-005-E'),
      (950050000000000402,950050000000000302,'E2E-B01','E2E一号楼','RESIDENTIAL',1,'0','0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE building_name=VALUES(building_name),status='0',del_flag='0';
    INSERT INTO pm_unit(id,project_id,building_id,unit_code,unit_name,floor_count,sort,status,del_flag,create_time,remark) VALUES
      (950050000000000411,950050000000000301,950050000000000401,'E2E-U01','E2E一单元',20,1,'0','0',NOW(),'TASK-V1-005-E'),
      (950050000000000412,950050000000000302,950050000000000402,'E2E-U01','E2E一单元',20,1,'0','0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE unit_name=VALUES(unit_name),status='0',del_flag='0';
    INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,floor_no,room_type,property_type,building_area,usable_area,delivery_status,occupancy_status,sort,status,del_flag,create_time,remark) VALUES
      (950050000000000421,950050000000000301,950050000000000401,950050000000000411,'E2E-R0101','0101',1,'HOUSE','RESIDENTIAL',88.00,72.00,'DELIVERED','OCCUPIED',1,'0','0',NOW(),'TASK-V1-005-E'),
      (950050000000000422,950050000000000302,950050000000000402,950050000000000412,'E2E-R0101','0101',1,'HOUSE','RESIDENTIAL',90.00,75.00,'DELIVERED','VACANT',1,'0','0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE occupancy_status=VALUES(occupancy_status),status='0',del_flag='0';

    INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,status,lifecycle_status,version,del_flag,create_time,remark) VALUES
      (950050000000000501,'E2E-V1-C01','PERSON','E2E业主甲','0','ACTIVE',0,'0',NOW(),'TASK-V1-005-E synthetic, no real PII'),
      (950050000000000502,'E2E-V1-C02','PERSON','E2E住户乙','0','ACTIVE',0,'0',NOW(),'TASK-V1-005-E synthetic, no real PII')
    ON DUPLICATE KEY UPDATE customer_name=VALUES(customer_name),status='0',lifecycle_status='ACTIVE',del_flag='0';
    INSERT INTO pm_room_customer_rel(id,project_id,room_id,customer_id,relation_type,is_primary,start_date,move_in_date,relation_status,status,del_flag,create_time,remark) VALUES
      (950050000000000511,950050000000000301,950050000000000421,950050000000000501,'OWNER','1','2026-01-01','2026-01-01','ACTIVE','0','0',NOW(),'TASK-V1-005-E'),
      (950050000000000512,950050000000000301,950050000000000421,950050000000000502,'RESIDENT','0','2026-01-01','2026-01-01','ACTIVE','0','0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE relation_status='ACTIVE',status='0',del_flag='0',move_out_date=NULL,end_date=NULL;

    INSERT INTO pm_charge_item(id,project_id,item_code,item_name,charge_type,charge_category,charge_object_type,status,sort,del_flag,create_time,remark) VALUES
      (950050000000000601,950050000000000301,'E2E-V1-FEE','E2E物业服务费','PERIODIC','PROPERTY_FEE','ROOM','0',1,'0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE item_name=VALUES(item_name),status='0',del_flag='0';

    INSERT INTO wm_warehouse(id,company_id,project_id,warehouse_scope,warehouse_code,warehouse_name,status,sort,del_flag,create_time,remark) VALUES
      (950050000000000701,1761000000000000103,950050000000000301,'PROJECT','E2E-V1-WA','E2E项目A仓库','0',1,'0',NOW(),'TASK-V1-005-E'),
      (950050000000000702,1761000000000000103,950050000000000302,'PROJECT','E2E-V1-WB','E2E项目B仓库','0',2,'0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE warehouse_name=VALUES(warehouse_name),status='0',del_flag='0';
    INSERT INTO wm_unit(id,unit_code,unit_name,symbol,precision_scale,status,sort,del_flag,create_time,remark) VALUES
      (950050000000000711,'E2E-V1-PCS','E2E件','件',0,'0',1,'0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE unit_name=VALUES(unit_name),status='0',del_flag='0';
    INSERT INTO wm_material_category(id,company_id,parent_id,ancestors,category_code,category_name,sort,status,del_flag,create_time,remark) VALUES
      (950050000000000721,1761000000000000103,0,'0','E2E-V1-CAT','E2E耗材',1,'0','0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE category_name=VALUES(category_name),status='0',del_flag='0';
    INSERT INTO wm_material(id,company_id,material_code,material_name,category_id,base_unit_id,specification,safety_stock,status,sort,del_flag,create_time,remark) VALUES
      (950050000000000731,1761000000000000103,'E2E-V1-MAT','E2E清洁耗材',950050000000000721,950050000000000711,'合成测试规格',10,'0',1,'0',NOW(),'TASK-V1-005-E')
    ON DUPLICATE KEY UPDATE material_name=VALUES(material_name),safety_stock=VALUES(safety_stock),status='0',del_flag='0';
END$$
DELIMITER ;
CALL seed_zbm_v1_e2e();
DROP PROCEDURE seed_zbm_v1_e2e;
