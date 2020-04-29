package com.nature.third.utils;

import com.nature.base.util.DateUtils;
import com.nature.common.Eunm.ProcessState;
import com.nature.common.Eunm.StopState;
import com.nature.component.process.model.Process;
import com.nature.component.process.model.ProcessStop;
import com.nature.third.vo.flowInfo.ThirdFlowInfoStopVo;
import com.nature.third.vo.flowInfo.ThirdFlowInfoStopsVo;
import com.nature.third.vo.flowInfo.ThirdFlowInfoVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThirdFlowInfoVoUtils {

    public static Process setProcess(Process process, ThirdFlowInfoVo thirdFlowInfoVo) {

        if (null != thirdFlowInfoVo && null != process) {
            Map<String, ThirdFlowInfoStopVo> stopsMap = new HashMap<>();
            List<ThirdFlowInfoStopsVo> stops = thirdFlowInfoVo.getStops();
            process.setLastUpdateUser("syncTask");
            process.setLastUpdateDttm(new Date());
            process.setProgress(thirdFlowInfoVo.getProgress());
            String thirdFlowInfoVoState = thirdFlowInfoVo.getState();
            if (StringUtils.isNotBlank(thirdFlowInfoVoState)) {
                ProcessState processState = null;
                if ("NEW".equals(thirdFlowInfoVoState) || "NEW_SAVING".equals(thirdFlowInfoVoState)) {
                    processState = ProcessState.INIT;
                } else if ("RUNNING".equals(thirdFlowInfoVoState)) {
                    processState = ProcessState.SUBMITTED;
                } else {
                    processState = ProcessState.selectGender(thirdFlowInfoVoState);
                }

                process.setState(processState);
            }
            //process.setName(thirdFlowInfoVo.getName());
            //process.setProcessId(thirdFlowInfoVo.getPid());
            process.setProcessId(thirdFlowInfoVo.getId());
            process.setStartTime(DateUtils.strCstToDate(thirdFlowInfoVo.getStartTime()));
            process.setEndTime(DateUtils.strCstToDate(thirdFlowInfoVo.getEndTime()));
            List<ProcessStop> processStopList = process.getProcessStopList();
            if (CollectionUtils.isNotEmpty(stops) && CollectionUtils.isNotEmpty(processStopList)) {
                for (ThirdFlowInfoStopsVo thirdFlowInfoStopsVo : stops) {
                    if (null != thirdFlowInfoStopsVo) {
                        ThirdFlowInfoStopVo stopVo = thirdFlowInfoStopsVo.getStop();
                        if (null != stopVo) {
                            stopsMap.put(stopVo.getName(), stopVo);
                        }
                    }
                }
                for (ProcessStop processStop : processStopList) {
                    if (null != processStop) {
                        ThirdFlowInfoStopVo stopVo = stopsMap.get(processStop.getName());
                        if (null != stopVo) {
                            processStop.setState(StopState.selectGender(stopVo.getState()));
                            processStop.setStartTime(DateUtils.strCstToDate(stopVo.getStartTime()));
                            processStop.setEndTime(DateUtils.strCstToDate(stopVo.getEndTime()));
                        }
                    }
                }
            }
        }
        return process;
    }
}
