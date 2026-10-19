package org.dromara.property.charge.payment.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.charge.payment.domain.bo.*;
import org.dromara.property.charge.payment.domain.vo.PaymentVo;
import org.dromara.property.charge.payment.domain.vo.PaymentExportVo;
import org.dromara.property.charge.payment.domain.vo.PaymentRoomOptionVo;
import org.dromara.property.charge.payment.domain.vo.PaymentReverseResultVo;
import java.util.List;

public interface IPaymentService {
    PaymentVo create(PaymentCreateBo bo);

    PaymentReverseResultVo reverse(Long id, PaymentReverseBo bo);

    PaymentVo queryById(Long id);

    PageResult<PaymentVo> queryPage(PaymentQueryBo bo, PageQuery pageQuery);

    List<PaymentExportVo> export(PaymentQueryBo bo);

    List<PaymentRoomOptionVo> roomOptions(Long projectId);
}
