package com.nature.component.process.utils;

import com.nature.component.process.model.ProcessStop;

import java.util.*;

public class ProcessStopUtils {

    public static ProcessStop processStopNewNoId(String username) {
        ProcessStop processStop = new ProcessStop();
        // basic properties (required when creating)
        processStop.setCrtDttm(new Date());
        processStop.setCrtUser(username);
        // basic properties
        processStop.setEnableFlag(true);
        processStop.setLastUpdateUser(username);
        processStop.setLastUpdateDttm(new Date());
        processStop.setVersion(0L);
        return processStop;
    }

    public static ProcessStop initProcessStopBasicPropertiesNoId(ProcessStop processStop, String username) {
        if (null == processStop) {
            return processStopNewNoId(username);
        }
        // basic properties (required when creating)
        processStop.setCrtDttm(new Date());
        processStop.setCrtUser(username);
        // basic properties
        processStop.setEnableFlag(true);
        processStop.setLastUpdateUser(username);
        processStop.setLastUpdateDttm(new Date());
        processStop.setVersion(0L);
        return processStop;
    }

}
