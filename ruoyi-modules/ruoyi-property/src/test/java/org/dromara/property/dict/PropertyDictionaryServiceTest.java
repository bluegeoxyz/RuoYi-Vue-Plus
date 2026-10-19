package org.dromara.property.dict;

import org.dromara.common.core.service.DictService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PropertyDictionaryServiceTest {
    private final DictService dictService = mock(DictService.class);
    private final PropertyDictionaryService service = new PropertyDictionaryService(dictService);

    @Test
    void resolvesChineseLabelAndKeepsStableCodeCompatible() {
        when(dictService.getAllDictByDictType(PropertyDictTypes.DELIVERY_STATUS))
            .thenReturn(Map.of("UNDELIVERED", "未交付", "DELIVERED", "已交付"));
        assertEquals("DELIVERED", service.resolveInput(PropertyDictTypes.DELIVERY_STATUS, "已交付"));
        assertEquals("DELIVERED", service.resolveInput(PropertyDictTypes.DELIVERY_STATUS, "DELIVERED"));
        assertNull(service.resolveInput(PropertyDictTypes.DELIVERY_STATUS, "已交房"));
    }
}
