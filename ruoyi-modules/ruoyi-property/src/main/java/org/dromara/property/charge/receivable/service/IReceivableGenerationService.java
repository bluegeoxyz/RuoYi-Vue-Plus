package org.dromara.property.charge.receivable.service;
import org.dromara.property.charge.receivable.domain.bo.ReceivableGenerateBo; import org.dromara.property.charge.receivable.domain.vo.ReceivableGenerationResultVo;
public interface IReceivableGenerationService { ReceivableGenerationResultVo generate(ReceivableGenerateBo bo); }
