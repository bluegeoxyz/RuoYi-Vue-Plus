package org.dromara.property.customer.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.customer.domain.Customer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** Customer 主档 Mapper；查询必须映射到带加密注解的 Entity。 */
public interface CustomerMapper extends BaseMapperPlus<Customer, Customer> {
    /** 使用二进制参数检查有效身份，避免 Wrapper 将 byte[] 错误序列化为对象字符串。 */
    @Select("SELECT COUNT(*) FROM pm_customer WHERE id_type = #{idType} " +
        "AND identity_fingerprint = #{fingerprint} AND del_flag = '0'")
    long countActiveIdentity(@Param("idType") String idType, @Param("fingerprint") byte[] fingerprint);

    /** 锁定一条仍有效的 Customer，供状态及删除事务使用。 */
    @Select("SELECT * FROM pm_customer WHERE id = #{id} AND del_flag = '0' FOR UPDATE")
    Customer selectActiveByIdForUpdate(@Param("id") Long id);

    /** 锁定该 Customer 的全部历史关系及相邻索引范围，不忽略逻辑删除关系。 */
    @Select("SELECT id FROM pm_room_customer_rel FORCE INDEX (idx_pm_room_customer_customer) " +
        "WHERE customer_id = #{customerId} FOR UPDATE")
    List<Long> selectAllRelationIdsForUpdate(@Param("customerId") Long customerId);

    /** 使用乐观锁执行逻辑删除并释放有效身份唯一键。 */
    @Update("UPDATE pm_customer SET del_flag = '1', version = version + 1, update_time = NOW() " +
        "WHERE id = #{id} AND version = #{version} AND del_flag = '0'")
    int logicalDelete(@Param("id") Long id, @Param("version") Integer version);

    /** 是否存在任意历史房屋关系，包括已结束及逻辑删除记录。 */
    @Select("SELECT EXISTS(SELECT 1 FROM pm_room_customer_rel WHERE customer_id = #{customerId})")
    boolean existsAnyRoomRelation(@Param("customerId") Long customerId);

    /** 锁定仍待生效或有效的关系索引范围，供停用/注销协议使用。 */
    @Select("SELECT id FROM pm_room_customer_rel FORCE INDEX (idx_pm_room_customer_customer) WHERE customer_id=#{customerId} " +
        "AND relation_status IN ('PENDING','ACTIVE') AND status='0' AND del_flag='0' FOR UPDATE")
    List<Long> selectEffectiveRelationIdsForUpdate(@Param("customerId") Long customerId);
}
