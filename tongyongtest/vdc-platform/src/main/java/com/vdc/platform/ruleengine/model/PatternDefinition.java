package com.vdc.platform.ruleengine.model;

import lombok.Data;

import java.util.List;

@Data
public class PatternDefinition {

    private List<Integer> enterPattern;
    private List<Object> exitPattern;
    private Boolean requireVehicle;
}
