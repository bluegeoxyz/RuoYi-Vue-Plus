package org.dromara.property.api;

import java.io.Serializable;

/**
 * 跨模块使用的项目轻量参考数据。
 *
 * @param id          项目ID
 * @param projectCode 项目编码
 * @param projectName 项目名称
 * @param status      项目状态
 */
public record ProjectReferenceDTO(Long id, String projectCode, String projectName, String status) implements Serializable {
}
