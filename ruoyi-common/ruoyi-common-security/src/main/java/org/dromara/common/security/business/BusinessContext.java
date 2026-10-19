package org.dromara.common.security.business;

/**
 * 受控的业务上下文。
 *
 * <p>上下文只表达业务能力，不代表已经获得项目、仓库或具体资源的访问权。</p>
 */
public enum BusinessContext {

    /** 查询入住或客户关系列表所需引用数据。 */
    CUSTOMER_RELATION_LIST,

    /** 创建入住或客户关系。 */
    CUSTOMER_RELATION_CREATE,

    /** 编辑入住或客户关系。 */
    CUSTOMER_RELATION_EDIT,

    /** 查询项目住户及其楼栋、单元、房屋筛选数据。 */
    RESIDENT_QUERY,

    /** 查询应收数据。 */
    RECEIVABLE_QUERY,

    /** 生成应收数据。 */
    RECEIVABLE_GENERATE,

    /** 创建收款并执行核销。 */
    PAYMENT_CREATE,

    /** 查询房屋收费设置及其筛选引用数据。 */
    CHARGE_BINDING_QUERY,

    /** 新增房屋收费设置。 */
    CHARGE_BINDING_CREATE,

    /** 编辑房屋收费设置。 */
    CHARGE_BINDING_EDIT,

    /** 新增或编辑入库单。 */
    INBOUND_MAINTAIN,

    /** 新增或编辑出库单。 */
    OUTBOUND_MAINTAIN,

    /** 新增或编辑调拨单。 */
    TRANSFER_MAINTAIN,

    /** 新增或编辑盘点单。 */
    STOCKTAKE_MAINTAIN,

    /** 查询入库单及其仓库筛选数据。 */
    INBOUND_QUERY,

    /** 查询出库单及其仓库筛选数据。 */
    OUTBOUND_QUERY,

    /** 查询调拨单及其源仓、目标仓筛选数据。 */
    TRANSFER_QUERY,

    /** 查询盘点单及其仓库筛选数据。 */
    STOCKTAKE_QUERY,

    /** 查询库存余额及其筛选引用数据。 */
    INVENTORY_QUERY,

    /** 查询库存流水及其筛选引用数据。 */
    INVENTORY_TRANSACTION_QUERY,

    /** 查询库区及其仓库筛选数据。 */
    WAREHOUSE_ZONE_QUERY,

    /** 查询库位及其仓库、库区筛选数据。 */
    WAREHOUSE_LOCATION_QUERY,

    /** 查询物料分类及其公司筛选数据。 */
    WAREHOUSE_MATERIAL_CATEGORY_QUERY,

    /** 查询物料及其公司、分类、计量单位筛选数据。 */
    WAREHOUSE_MATERIAL_QUERY,

    /** 新增或编辑库区所需引用数据。 */
    WAREHOUSE_ZONE_MAINTAIN,

    /** 新增或编辑库位所需引用数据。 */
    WAREHOUSE_LOCATION_MAINTAIN,

    /** 新增或编辑物料分类所需引用数据。 */
    WAREHOUSE_MATERIAL_CATEGORY_MAINTAIN,

    /** 新增或编辑物料所需引用数据。 */
    WAREHOUSE_MATERIAL_MAINTAIN
}
