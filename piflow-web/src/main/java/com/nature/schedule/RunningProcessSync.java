package com.nature.schedule;

import com.nature.base.util.LoggerUtil;
import com.nature.base.util.SpringContextUtil;
import com.nature.common.executor.ServicesExecutor;
import com.nature.domain.process.ProcessDomain;
import com.nature.third.service.IFlow;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class RunningProcessSync extends QuartzJobBean {

    Logger logger = LoggerUtil.getLogger();

    @Resource
    private ProcessDomain processDomain;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");
        logger.info("processSync start : " + formatter.format(new Date()));
        List<String> runningProcess = processDomain.getRunningProcessAppId();
        if (CollectionUtils.isNotEmpty(runningProcess)) {
            Runnable runnable = new Thread(new Thread() {
                @Override
                public void run() {
                    for (String appId : runningProcess) {
                        IFlow getFlowInfoImpl = (IFlow) SpringContextUtil.getBean("flowImpl");
                        getFlowInfoImpl.getProcessInfoAndSave(appId);
                    }
                }
            });
            ServicesExecutor.getServicesExecutorServiceService().execute(runnable);
        }
        logger.info("processSync end : " + formatter.format(new Date()));
    }
}