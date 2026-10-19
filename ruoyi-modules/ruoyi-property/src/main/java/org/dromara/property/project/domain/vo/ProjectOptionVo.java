package org.dromara.property.project.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.property.project.domain.Project;

import java.io.Serial;
import java.io.Serializable;

/** 物业项目轻量选项。 */
@Data
@AutoMapper(target = Project.class)
public class ProjectOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String projectCode;
    private String projectName;
    private String projectShortName;
}
