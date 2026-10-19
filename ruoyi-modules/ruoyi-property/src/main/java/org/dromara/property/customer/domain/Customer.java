package org.dromara.property.customer.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.encrypt.annotation.EncryptField;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDate;

/** 企业级客户主档，仅限服务端受控使用。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_customer")
public class Customer extends BaseEntity {
    @TableId
    private Long id;
    private String customerNo;
    private String customerType;
    private String customerName;
    private String gender;
    private String mobile;
    private String phone;
    private String email;
    private String idType;
    @EncryptField
    private String idNo;
    private LocalDate birthDate;
    private String status;
    private byte[] identityFingerprint;
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private byte[] activeIdentityKey;
    private String piiKeyVersion;
    private String lifecycleStatus;
    private Long mergedIntoCustomerId;
    private String remark;
    @Version
    private Integer version;
    @TableLogic
    private String delFlag;
}
