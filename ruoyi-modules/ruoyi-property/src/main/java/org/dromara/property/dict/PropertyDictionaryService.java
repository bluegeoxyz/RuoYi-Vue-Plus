package org.dromara.property.dict;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/** Property 字典输入解析；API code 与 Excel 中文 label 均保持兼容。 */
@Service
@RequiredArgsConstructor
public class PropertyDictionaryService {
    private final DictService dictService;

    /** 将字典 code 或中文 label 解析为稳定 code；空输入或无匹配值返回 null。 */
    public String resolveInput(String dictType, String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        String normalized = input.trim();
        Map<String, String> values = dictService.getAllDictByDictType(dictType);
        if (values.containsKey(normalized)) {
            return normalized;
        }
        return values.entrySet().stream()
            .filter(entry -> normalized.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }
}
