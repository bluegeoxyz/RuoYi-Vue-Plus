-- TASK-V1-005-B review-only inventory.
-- This file intentionally contains no DELETE, DROP, TRUNCATE or UPDATE statement.
-- Run only with a read-only account against the intended database.

WITH RECURSIVE removed_menu AS (
    SELECT menu_id, parent_id, menu_name, path, component, perms
    FROM sys_menu
    WHERE perms REGEXP '^(demo|workflow|monitor:snail(ai|job)):'
       OR component REGEXP '^(demo|workflow|ai/chat|monitor/snail(ai|job))'
       OR path IN ('demo', 'workflow', 'ai', 'aichat', 'snailai', 'snailjob')
    UNION DISTINCT
    SELECT child.menu_id, child.parent_id, child.menu_name, child.path, child.component, child.perms
    FROM sys_menu child
    JOIN removed_menu parent ON child.parent_id = parent.menu_id
)
SELECT menu_id, parent_id, menu_name, path, component, perms
FROM removed_menu
ORDER BY parent_id, menu_id;

WITH RECURSIVE removed_menu AS (
    SELECT menu_id
    FROM sys_menu
    WHERE perms REGEXP '^(demo|workflow|monitor:snail(ai|job)):'
       OR component REGEXP '^(demo|workflow|ai/chat|monitor/snail(ai|job))'
       OR path IN ('demo', 'workflow', 'ai', 'aichat', 'snailai', 'snailjob')
    UNION DISTINCT
    SELECT child.menu_id
    FROM sys_menu child
    JOIN removed_menu parent ON child.parent_id = parent.menu_id
)
SELECT rm.role_id, rm.menu_id
FROM sys_role_menu rm
JOIN removed_menu candidate ON candidate.menu_id = rm.menu_id
ORDER BY rm.role_id, rm.menu_id;

-- Stage C decision candidates only; do not remove them in Stage B:
-- demo tables: test_demo, test_demo_encrypt, test_tree
-- workflow tables: flow_*, test_leave
-- SnailJob tables: sj_*
-- The current development database must be backed up and explicitly authorized
-- before a later stage turns this inventory into mutation SQL.
