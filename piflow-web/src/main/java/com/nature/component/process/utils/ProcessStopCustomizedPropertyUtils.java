package com.nature.component.process.utils;

import com.nature.component.process.model.ProcessStopCustomizedProperty;

import java.util.Date;

public class ProcessStopCustomizedPropertyUtils {

    public static ProcessStopCustomizedProperty processStopCustomizedPropertyNewNoId(String username) {
        ProcessStopCustomizedProperty processStopCustomizedProperty = new ProcessStopCustomizedProperty();
        // basic properties (required when creating)
        processStopCustomizedProperty.setCrtDttm(new Date());
        processStopCustomizedProperty.setCrtUser(username);
        // basic properties
        processStopCustomizedProperty.setEnableFlag(true);
        processStopCustomizedProperty.setLastUpdateUser(username);
        processStopCustomizedProperty.setLastUpdateDttm(new Date());
        processStopCustomizedProperty.setVersion(0L);
        return processStopCustomizedProperty;
    }

    public static ProcessStopCustomizedProperty initProcessStopCustomizedPropertyBasicPropertiesNoId(ProcessStopCustomizedProperty processStopCustomizedProperty, String username) {
        if (null == processStopCustomizedProperty) {
            return processStopCustomizedPropertyNewNoId(username);
        }
        // basic properties (required when creating)
        processStopCustomizedProperty.setCrtDttm(new Date());
        processStopCustomizedProperty.setCrtUser(username);
        // basic properties
        processStopCustomizedProperty.setEnableFlag(true);
        processStopCustomizedProperty.setLastUpdateUser(username);
        processStopCustomizedProperty.setLastUpdateDttm(new Date());
        processStopCustomizedProperty.setVersion(0L);
        return processStopCustomizedProperty;
    }

}
