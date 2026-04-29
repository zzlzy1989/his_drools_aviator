package com.his.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillResult {

    private ResultLevel level;
    private String source;
    private String message;
}
