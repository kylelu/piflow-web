package com.nature.component.process.model;

import com.nature.common.Eunm.ProcessState;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ProcessAndProcessGroup {

    private String id;
    private Object lastUpdateDttm;
    private Object crtDttm;
    private String appId;
    private String name;
    private String description;
    private Object startTime;
    private Object endTime;
    private String progress;
    private String parentProcessId;
    private String state;
    private String tpye;

}
