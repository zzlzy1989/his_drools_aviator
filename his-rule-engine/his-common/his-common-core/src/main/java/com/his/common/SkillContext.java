package com.his.common;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class SkillContext<T> {

    private String tenantId;
    private String eventType;
    private T payload;
    private List<SkillResult> results = new ArrayList<>();

    public void addResult(SkillResult result) {
        results.add(result);
    }

    public boolean hasBlock() {
        return results.stream().anyMatch(r -> r.getLevel() == ResultLevel.BLOCK);
    }
}
