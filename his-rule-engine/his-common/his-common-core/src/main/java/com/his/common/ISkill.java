package com.his.common;

public interface ISkill<T> {

    String supportEvent();

    int getOrder();

    void execute(SkillContext<T> context);
}
