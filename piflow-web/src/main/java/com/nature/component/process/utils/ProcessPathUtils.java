package com.nature.component.process.utils;

import com.nature.component.process.model.ProcessPath;

import java.util.Date;

public class ProcessPathUtils {

    public static ProcessPath processPathNewNoId(String username) {
        ProcessPath processPath = new ProcessPath();
        // basic properties (required when creating)
        processPath.setCrtDttm(new Date());
        processPath.setCrtUser(username);
        // basic properties
        processPath.setEnableFlag(true);
        processPath.setLastUpdateUser(username);
        processPath.setLastUpdateDttm(new Date());
        processPath.setVersion(0L);
        return processPath;
    }

    public static ProcessPath initProcessPathBasicPropertiesNoId(ProcessPath processPath, String username) {
        if (null == processPath) {
            return processPathNewNoId(username);
        }
        // basic properties (required when creating)
        processPath.setCrtDttm(new Date());
        processPath.setCrtUser(username);
        // basic properties
        processPath.setEnableFlag(true);
        processPath.setLastUpdateUser(username);
        processPath.setLastUpdateDttm(new Date());
        processPath.setVersion(0L);
        return processPath;
    }

}
