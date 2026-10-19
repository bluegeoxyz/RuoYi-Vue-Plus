package org.dromara.property.customer.importer.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.importer.ImportError;
import org.dromara.common.excel.importer.ImportResult;
import org.dromara.property.dict.PropertyDictTypes;
import org.dromara.property.dict.PropertyDictionaryService;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.domain.bo.CustomerCreateBo;
import org.dromara.property.customer.importer.domain.CustomerImportRow;
import org.dromara.property.customer.importer.service.CustomerImportCommitHook;
import org.dromara.property.customer.importer.service.ICustomerImportService;
import org.dromara.property.customer.service.CustomerAccessPolicy;
import org.dromara.property.customer.service.CustomerAction;
import org.dromara.property.customer.service.CustomerCreateExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

/** Customer Excel 两阶段原子导入实现。 */
@Service
@RequiredArgsConstructor
public class CustomerImportServiceImpl implements ICustomerImportService {
    private static final int MAX_ROWS = 2000;
    private final CustomerAccessPolicy accessPolicy;
    private final CustomerCreateExecutor createExecutor;
    private final Validator validator;
    private final PropertyDictionaryService dictionaryService;
    private final TransactionTemplate transactionTemplate;
    private final List<CustomerImportCommitHook> commitHooks;

    @Override public ImportResult importRows(List<CustomerImportRow> sourceRows) {
        List<CustomerImportRow> rows=sourceRows==null?List.of():sourceRows;
        for(int i=0;i<rows.size();i++)rows.get(i).setRowNumber(i+2);
        List<ImportError> errors=validate(rows); if(!errors.isEmpty())return ImportResult.failure(rows.size(),errors);
        try{commitHooks.forEach(CustomerImportCommitHook::beforeCommit);transactionTemplate.executeWithoutResult(s->commit(rows));return ImportResult.success(rows.size());}
        catch(RuntimeException e){return ImportResult.failure(rows.size(),List.of(error(null,"transaction",safeCommitMessage(e))));}
    }

    private List<ImportError> validate(List<CustomerImportRow> rows){
        accessPolicy.assertEnterpriseMasterAccess(CustomerAction.IMPORT);
        if(rows.isEmpty())return List.of(error(null,"file","未读取到客户数据"));
        if(rows.size()>MAX_ROWS)return List.of(error(null,"file","单次最多导入 2000 条客户数据，请拆分文件后重新导入。"));
        List<ImportError> errors=new ArrayList<>();Map<String,Integer> identities=new HashMap<>();
        for(CustomerImportRow row:rows){
            normalizeDictionary(row, errors);
            CustomerCreateBo bo=toBo(row);Set<ConstraintViolation<CustomerCreateBo>> violations=validator.validate(bo);
            for(var v:violations)errors.add(error(row.getRowNumber(),v.getPropertyPath().toString(),v.getMessage()));
            if(StringUtils.isNotBlank(row.getMobile())&&!row.getMobile().trim().matches("^[0-9+() -]{6,32}$"))errors.add(error(row.getRowNumber(),"mobile","手机号格式错误"));
            if(StringUtils.isNotBlank(row.getPhone())&&!row.getPhone().trim().matches("^[0-9+() -]{6,32}$"))errors.add(error(row.getRowNumber(),"phone","电话格式错误"));
            if((StringUtils.isBlank(row.getIdType()))!=(StringUtils.isBlank(row.getIdNo()))){errors.add(error(row.getRowNumber(),"idNo","证件类型与证件号码必须同时填写"));continue;}
            if(!violations.isEmpty())continue;
            try{Customer prepared=createExecutor.prepare(bo,true);if(prepared.getIdentityFingerprint()!=null){String key=prepared.getIdType()+":"+Base64.getEncoder().encodeToString(prepared.getIdentityFingerprint());Integer first=identities.putIfAbsent(key,row.getRowNumber());if(first!=null)errors.add(error(row.getRowNumber(),"idNo","证件身份与第 "+first+" 行重复"));}}
            catch(ServiceException e){errors.add(error(row.getRowNumber(),"idNo",e.getMessage().contains("有效客户档案已存在")?"该证件身份已存在":e.getMessage()));}
            catch(RuntimeException e){errors.add(error(row.getRowNumber(),"security","Customer 安全处理失败"));}
        }return errors;
    }

    private void normalizeDictionary(CustomerImportRow row,List<ImportError> errors){
        row.setCustomerType(resolve(row,"customerType","客户类型",PropertyDictTypes.CUSTOMER_TYPE,row.getCustomerType(),true,errors));
        row.setGender(resolve(row,"gender","性别",PropertyDictTypes.USER_GENDER,row.getGender(),false,errors));
        row.setIdType(resolve(row,"idType","证件类型",PropertyDictTypes.ID_TYPE,row.getIdType(),false,errors));
    }
    private String resolve(CustomerImportRow row,String field,String label,String dictType,String input,boolean required,List<ImportError> errors){
        if(StringUtils.isBlank(input)){if(required)errors.add(error(row.getRowNumber(),field,label+"不能为空"));return null;}
        String code=dictionaryService.resolveInput(dictType,input);
        if(code==null)errors.add(error(row.getRowNumber(),field,label+"“"+input.trim()+"”不是有效值"));
        return code;
    }
    private void commit(List<CustomerImportRow> rows){accessPolicy.assertEnterpriseMasterAccess(CustomerAction.IMPORT);for(int i=0;i<rows.size();i++){Customer customer=createExecutor.prepare(toBo(rows.get(i)),true);for(CustomerImportCommitHook hook:commitHooks)hook.beforeInsert(i,customer);createExecutor.insert(customer);}}
    private CustomerCreateBo toBo(CustomerImportRow r){CustomerCreateBo b=new CustomerCreateBo();b.setCustomerType(r.getCustomerType());b.setCustomerName(r.getCustomerName());b.setGender(r.getGender());b.setBirthDate(r.getBirthDate());b.setMobile(r.getMobile());b.setPhone(r.getPhone());b.setEmail(r.getEmail());b.setIdType(r.getIdType());b.setIdNo(r.getIdNo());b.setRemark(r.getRemark());return b;}
    private String safeCommitMessage(Throwable error){for(Throwable e=error;e!=null;e=e.getCause())if(e instanceof ServiceException&&e.getMessage()!=null)return e.getMessage()+"，本次写入 0 条。";return "提交阶段发生并发或数据库冲突，本次写入 0 条。";}
    private ImportError error(Integer r,String f,String m){return new ImportError(r,f,m);}
}
