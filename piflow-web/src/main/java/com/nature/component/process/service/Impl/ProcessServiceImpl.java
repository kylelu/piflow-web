package com.nature.component.process.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nature.base.util.*;
import com.nature.base.vo.StatefulRtnBase;
import com.nature.base.vo.UserVo;
import com.nature.common.Eunm.ProcessState;
import com.nature.common.Eunm.RunModeType;
import com.nature.common.Eunm.StopState;
import com.nature.component.flow.model.Flow;
import com.nature.component.process.model.Process;
import com.nature.component.process.model.ProcessGroup;
import com.nature.component.process.model.ProcessStop;
import com.nature.component.process.service.IProcessService;
import com.nature.component.process.utils.ProcessUtils;
import com.nature.component.process.vo.DebugDataRequest;
import com.nature.component.process.vo.DebugDataResponse;
import com.nature.component.process.vo.ProcessGroupVo;
import com.nature.component.process.vo.ProcessVo;
import com.nature.domain.flow.FlowDomain;
import com.nature.domain.process.ProcessDomain;
import com.nature.mapper.process.ProcessMapper;
import com.nature.mapper.process.ProcessStopMapper;
import com.nature.third.service.IFlow;
import com.nature.third.vo.flow.ThirdProgressVo;
import com.nature.third.vo.flowInfo.ThirdFlowInfoStopVo;
import com.nature.third.vo.flowInfo.ThirdFlowInfoStopsVo;
import com.nature.third.vo.flowInfo.ThirdFlowInfoVo;
import com.nature.transaction.process.ProcessTransaction;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ProcessServiceImpl implements IProcessService {

    Logger logger = LoggerUtil.getLogger();

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTransaction processTransaction;

    @Resource
    private ProcessStopMapper processStopMapper;

    @Resource
    private FlowDomain flowDomain;

    @Resource
    private IFlow flowImpl;

    @Resource
    private ProcessDomain processDomain;

    /**
     * Query processVoList (the query contains its child table)
     *
     * @return
     */
    @Override
    public List<ProcessVo> getProcessAllVoList() {
        List<ProcessVo> processVoList = null;
        List<Process> processList = processMapper.getProcessList();
        if (null != processList && processList.size() > 0) {
            processVoList = new ArrayList<>();
            for (Process process : processList) {
                if (null != process) {
                    ProcessVo processVo = ProcessUtils.processPoToVo(process);
                    processVo.setCrtDttm(process.getCrtDttm());
                    processVoList.add(processVo);
                }
            }
        }
        return processVoList;
    }

    /**
     * Query processVoList (only query process table)
     *
     * @return
     */
    @Override
    public List<ProcessVo> getProcessVoList() {
        List<ProcessVo> processVoList = null;
        List<Process> processList = processMapper.getProcessList();
        if (null != processList && processList.size() > 0) {
            processVoList = new ArrayList<>();
            for (Process process : processList) {
                if (null != process) {
                    ProcessVo processVo = new ProcessVo();
                    BeanUtils.copyProperties(process, processVo);
                    processVo.setCrtDttm(process.getCrtDttm());
                    processVoList.add(processVo);
                }
            }
        }
        return processVoList;
    }

    /**
     * Query processVo based on id (query contains its child table)
     *
     * @param id
     * @return
     */
    @Override
    public ProcessVo getProcessAllVoById(String id) {
        ProcessVo processVo = null;
        if (StringUtils.isNotBlank(id)) {
            Process processById = processMapper.getProcessById(id);
            processVo = ProcessUtils.processPoToVo(processById);
            ProcessGroup processGroup = processById.getProcessGroup();
            if (null != processGroup) {
                ProcessGroupVo processGroupVo = new ProcessGroupVo();
                processGroupVo.setId(processGroup.getId());
                processVo.setProcessGroupVo(processGroupVo);
            }
        }
        return processVo;
    }

    /**
     * Query processVo based on id (only query process table)
     *
     * @param id
     * @return
     */
    @Override
    public ProcessVo getProcessVoById(String id) {
        ProcessVo processVo = null;
        if (StringUtils.isNotBlank(id)) {
            Process processById = processMapper.getProcessById(id);
            if (null != processById) {
                processVo = new ProcessVo();
                BeanUtils.copyProperties(processById, processVo);
                processVo.setCrtDttm(processById.getCrtDttm());
            }
        }
        return processVo;
    }

    /**
     * Query process based on id
     *
     * @param id
     * @return
     */
    @Override
    public ProcessVo getProcessById(String id) {
        ProcessVo processVo = null;
        Process processById = processMapper.getProcessById(id);
        if (null != processById) {
            processVo = ProcessUtils.processPoToVo(processById);
        }
        return processVo;
    }

    /**
     * Query process according to Appid
     *
     * @param appId
     * @return
     */
    @Override
    public ProcessVo getProcessVoByAppId(String appId) {
        ProcessVo processVo = null;
        if (StringUtils.isNotBlank(appId)) {
            Process processById = processMapper.getProcessByAppId(appId);
            if (null != processById) {
                processVo = ProcessUtils.processPoToVo(processById);
            }
        }
        return processVo;
    }

    /**
     * Query process according to array AppId
     *
     * @param appIDs
     * @return
     */
    public List<ProcessVo> getProcessVoByAppIds(String appIDs) {
        return null;
    }

    /**
     * Query appInfo on a third-party interface based on appID and save
     *
     * @param appID
     * @return
     */
    @Override
    public ProcessVo getAppInfoByThirdAndSave(String appID) {
        ProcessVo processVo = new ProcessVo();
        Process processById = processMapper.getProcessByAppId(appID);
        if (null != processById) {
            // If the status is STARTED, the interface is removed. Otherwise, it indicates that the startup is complete and returns directly.
            ProcessState state = processById.getState();
            if (ProcessState.STARTED == state || null == processById.getStartTime()) {
                ThirdFlowInfoVo thirdFlowInfoVo = flowImpl.getFlowInfo(appID);
                if (null != thirdFlowInfoVo) {
                    processById.getProcessStopList();
                    //Determine if the progress returned by the interface is empty
                    if (StringUtils.isNotBlank(thirdFlowInfoVo.getProgress())) {
                        double progressNums = Double.parseDouble(thirdFlowInfoVo.getProgress());
                        Double progressNumsDb = null;
                        String percentage = processById.getProgress();
                        if (StringUtils.isNotBlank(percentage)) {
                            progressNumsDb = Double.parseDouble(percentage);
                        }
                        boolean isUpdateProcess = false;
                        // Determine the status, if the status is STARTED, determine whether the return progress is greater than the database progress, if it is greater than the save
                        // Save the database directly if the state is not STARTED
                        if ("STARTED".equals(thirdFlowInfoVo.getState())) {
                            // Save if the database progress is empty
                            if (null == progressNumsDb) {
                                isUpdateProcess = true;
                            } else if (progressNums > progressNumsDb) {
                                //Save if the return progress is greater than the database progress
                                isUpdateProcess = true;
                            }
                        } else {
                            isUpdateProcess = true;
                        }
                        if (isUpdateProcess) {
                            // Modify flow information
                            processById.setLastUpdateUser("update");
                            processById.setLastUpdateDttm(new Date());
                            processById.setProgress(progressNums + "");
                            processById.setState(ProcessState.selectGender(thirdFlowInfoVo.getState()));
                            //processById.setProcessId(thirdFlowInfoVo.getPid());
                            processById.setProcessId(thirdFlowInfoVo.getId());
                            processById.setName(thirdFlowInfoVo.getName());
                            processById.setStartTime(DateUtils.strCstToDate(thirdFlowInfoVo.getStartTime()));
                            processById.setEndTime(DateUtils.strCstToDate(thirdFlowInfoVo.getEndTime()));
                            processTransaction.updateProcess(processById);
                            // Modify the stops information
                            List<ThirdFlowInfoStopsVo> stops = thirdFlowInfoVo.getStops();
                            if (null != stops && stops.size() > 0) {
                                List<ProcessStop> processStopListNew = new ArrayList<>();
                                processVo.setId(processById.getId());
                                for (ThirdFlowInfoStopsVo thirdFlowInfoStopsVo : stops) {
                                    if (null != thirdFlowInfoStopsVo) {
                                        ThirdFlowInfoStopVo thirdFlowInfoStopVo = thirdFlowInfoStopsVo.getStop();
                                        if (null != thirdFlowInfoStopVo) {
                                            ProcessStop processStopByNameAndPid = processStopMapper.getProcessStopByNameAndPid(processById.getId(), thirdFlowInfoStopVo.getName());
                                            processStopByNameAndPid.setName(thirdFlowInfoStopVo.getName());
                                            processStopByNameAndPid.setState(StopState.selectGender(thirdFlowInfoStopVo.getState()));
                                            processStopByNameAndPid.setStartTime(DateUtils.strCstToDate(thirdFlowInfoStopVo.getStartTime()));
                                            processStopByNameAndPid.setEndTime(DateUtils.strCstToDate(thirdFlowInfoStopVo.getEndTime()));
                                            int updateProcessStop = processStopMapper.updateProcessStop(processStopByNameAndPid);
                                            if (updateProcessStop > 0) {
                                                processStopListNew.add(processStopByNameAndPid);
                                            }
                                        }
                                    }
                                }
                                processById.setProcessStopList(processStopListNew);
                            }
                            processById = processMapper.getProcessByAppId(appID);
                        }
                    }
                }
            }
            processVo = ProcessUtils.processPoToVo(processById);
        }

        return processVo;
    }

    /**
     * Query appInfo according to appID
     *
     * @param appID
     * @return
     */
    @Override
    public String getAppInfoByAppId(String appID) {
        Map<String, Object> rtnMap = new HashMap<String, Object>();
        rtnMap.put("code", 500);
        if (StringUtils.isNotBlank(appID)) {
            // 查询appinfo
            //ProcessVo processVoThird = this.getAppInfoByThirdAndSave(appID);
            Process processById = processMapper.getProcessByAppId(appID);
            ProcessVo processVo = ProcessUtils.processPoToVo(processById);
            if (null != processVo) {
                rtnMap.put("code", 200);
                rtnMap.put("progress", (null != processVo.getProgress() ? processVo.getProgress() : "0.00"));
                rtnMap.put("state", (null != processVo.getState() ? processVo.getState().name() : "NO_STATE"));
                rtnMap.put("processVo", processVo);
            }
        } else {
            rtnMap.put("errorMsg", "appID is null");
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Query progress and save on third-party interface according to appID
     *
     * @param appIDs
     * @return
     */
    @Override
    public String getProgressByThirdAndSave(String[] appIDs) {
        Map<String, Object> rtnMap = new HashMap<String, Object>();
        rtnMap.put("code", 500);
        List<ProcessVo> processVoList = null;
        if (null != appIDs && appIDs.length > 0) {
            List<Process> processListByAppIDs = processMapper.getProcessListByAppIDs(appIDs);
            if (null != processListByAppIDs && processListByAppIDs.size() > 0) {
                processVoList = new ArrayList<>();
                for (Process process : processListByAppIDs) {
                    if (null != process) {
                        ProcessVo processVo = null;
                        // If the status is STARTED, the interface is removed. Otherwise, it indicates that the startup is complete and returns directly.
                        ProcessState state = process.getState();
                        if (ProcessState.STARTED == state) {
                            ThirdProgressVo flowProgress = flowImpl.getFlowProgress(process.getAppId());
                            if (null != flowProgress) {
                                double progressNumsDb = 0.00;
                                String percentage = process.getProgress();
                                if (StringUtils.isNotBlank(percentage)) {
                                    progressNumsDb = Float.parseFloat(percentage);
                                }
                                double progressNums = progressNumsDb;
                                if (!"NaN".equals(flowProgress.getProgress())) {
                                    progressNums = Double.parseDouble(flowProgress.getProgress());
                                    BigDecimal formatBD = new BigDecimal(progressNums);
                                    progressNums = formatBD.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                }
                                boolean isUpdateProcess = false;
                                // Determine the status, if the status is STARTED, determine whether the return progress is greater than the database progress, if it is greater than the save
                                // Save the database directly if the state is not STARTED
                                if ("STARTED".equals(flowProgress.getState())) {
                                    //Save if the return progress is greater than the database progress
                                    if (progressNums > progressNumsDb) {
                                        isUpdateProcess = true;
                                    }
                                } else {
                                    isUpdateProcess = true;
                                }
                                if (isUpdateProcess) {
                                    // Modify flow information
                                    process.setLastUpdateUser("update");
                                    process.setLastUpdateDttm(new Date());
                                    process.setProgress(progressNums + "");
                                    process.setState(ProcessState.selectGender(flowProgress.getState()));
                                    process.setName(flowProgress.getName());
                                    processTransaction.updateProcess(process);
                                }
                            }
                            processVo = ProcessUtils.processPoToVo(process);
                        } else if (null == process.getStartTime()) {
                            processVo = this.getAppInfoByThirdAndSave(process.getAppId());
                        }
                        if (null != processVo) {
                            processVoList.add(processVo);
                        }
                    }
                }
            }
        }
        if (null != processVoList && processVoList.size() > 0) {
            rtnMap.put("code", 200);
            for (ProcessVo processVo : processVoList) {
                if (null != processVo) {
                    rtnMap.put(processVo.getAppId(), processVo);
                }
            }
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Query  process according to appID
     *
     * @param appIDs
     * @return
     */
    @Override
    public String getProgressByAppIds(String[] appIDs) {
        Map<String, Object> rtnMap = new HashMap<String, Object>();
        rtnMap.put("code", 500);
        if (null != appIDs && appIDs.length > 0) {
            List<Process> processListByAppIDs = processMapper.getProcessListByAppIDs(appIDs);
            if (CollectionUtils.isNotEmpty(processListByAppIDs)) {
                rtnMap.put("code", 200);
                for (Process process : processListByAppIDs) {
                    if (null != process) {
                        ProcessVo processVo = ProcessUtils.processPoToVo(process);
                        if (null != process) {
                            rtnMap.put(processVo.getAppId(), processVo);
                        }
                    }
                }
            }
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Modify the process (only update the process table, the subtable is not updated)
     *
     * @param processVo
     * @return
     */
    @Override
    public int updateProcess(ProcessVo processVo, UserVo currentUser) {
        if (null != processVo && null != currentUser) {
            Process processById = processMapper.getProcessById(processVo.getId());
            if (null != processById) {
                BeanUtils.copyProperties("processVo", "processById");
                processById.setLastUpdateUser(currentUser.getUsername());
                processById.setLastUpdateDttm(new Date());
                processById.setState(processVo.getState());
                processById.setProgress(processVo.getProgress());
                processById.setStartTime(processVo.getStartTime());
                processById.setEndTime(processVo.getEndTime());
                processById.setProcessId(processVo.getProcessId());
                processById.setName(processVo.getName());
                return processTransaction.updateProcess(processById);
            }
        }
        return 0;
    }

    /**
     * Copy process and create new
     *
     * @param processId
     * @return
     */
    @Override
    public Process processCopyProcessAndAdd(String processId, UserVo currentUser, RunModeType runModeType) {
        if (StringUtils.isNotBlank(processId)) {
            return null;
        }
        Process process = processDomain.getProcessById(processId);
        Process processCopy = ProcessUtils.copyProcessAndNew(process, currentUser, runModeType);
        if (null != processCopy) {
            processCopy = processDomain.saveOrUpdate(processCopy);
        }

        return processCopy;
    }

    /**
     * Generate Process and save according to flowId
     *
     * @param flowId
     * @return
     */
    @Override
    public ProcessVo flowToProcessAndSave(String flowId) {
        String user = SessionUserUtil.getCurrentUsername();
        //Determine if the flowId is empty
        if (StringUtils.isBlank(flowId)) {
            logger.warn("The parameter'flowId'is empty and the conversion fails");
            return null;
        }
        // Query flow according to Id
        Flow flowById = flowDomain.getFlowById(flowId);
        // Determine if the queryed flow is empty
        if (null == flowById) {
            logger.warn("Unable to query flow Id for'" + flowId + "'flow, the conversion failed");
            return null;
        }
        Process process = ProcessUtils.flowToProcess(flowById, user);
        if (null == process) {
            logger.warn("Conversion failed");
            return null;
        }
        process = processDomain.saveOrUpdate(process);
        if (null != process) {
            ProcessVo processVo = ProcessUtils.processPoToVo(process);
            return processVo;
        } else {
            logger.warn("Save failed, transform failed");
            return null;
        }
    }

    /**
     * Logical deletion
     *
     * @param processId
     * @return
     */
    @Override
    public String delProcess(String processId) {
        String username = SessionUserUtil.getCurrentUsername();
        if (StringUtils.isBlank(username)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("illegal user");
        }
        if (StringUtils.isBlank(processId)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("processID is null");
        }
        // Query Process by 'ProcessId'
        Process processById = processDomain.getProcessById(processId);
        if (null == processById) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("No process with ID of'" + processId + "'was queried");
        }
        if (processById.getState() == ProcessState.STARTED) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Status is STARTED, cannot be deleted");
        }
        processById.setEnableFlag(false);
        processById.setLastUpdateDttm(new Date());
        processById.setLastUpdateUser(username);
        processDomain.saveOrUpdate(processById);
        return ReturnMapUtils.setSucceededMsgRtnJsonStr("Successfully Deleted");
    }

    /**
     * Query the running process List (process List) according to flowId
     *
     * @param flowId
     * @return
     */
    @Override
    public List<ProcessVo> getRunningProcessVoList(String flowId) {
        List<Process> processList = processMapper.getRunningProcessList(flowId);
        if (CollectionUtils.isEmpty(processList)) {
            return null;
        }
        List<ProcessVo> processVoList = new ArrayList<ProcessVo>();
        for (Process process : processList) {
            ProcessVo processVo = ProcessUtils.processOnePoToVo(process);
            if (null != processVo) {
                processVoList.add(processVo);
            }
        }
        return processVoList;
    }

    /**
     * Query processVoList (parameter space-time non-paging)
     *
     * @param offset
     * @param limit
     * @param param
     * @return
     */
    @Override
    public String getProcessVoListPage(Integer offset, Integer limit, String param) {
        Map<String, Object> rtnMap = new HashMap<String, Object>();
        if (null != offset && null != limit) {
            Page<Process> page = PageHelper.startPage(offset, limit);
            processMapper.getProcessListByParam(param);
            rtnMap = PageHelperUtils.setDataTableParam(page, rtnMap);
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Query processVoList (parameter space-time non-paging)
     *
     * @param offset
     * @param limit
     * @param param
     * @return
     */
    @Override
    public String getProcessGroupVoListPage(Integer offset, Integer limit, String param) {
        Map<String, Object> rtnMap = new HashMap<String, Object>();
        if (null != offset && null != limit) {
            Page<Process> page = PageHelper.startPage(offset, limit);
            processMapper.getProcessGroupListByParam(param);
            rtnMap = PageHelperUtils.setDataTableParam(page, rtnMap);
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Start processes
     *
     * @param processId
     * @param checkpoint
     * @param currentUser
     * @return
     */
    @Override
    public String startProcess(String processId, String checkpoint, String runMode, UserVo currentUser) {
        RunModeType runModeType = RunModeType.RUN;
        if (StringUtils.isNotBlank(runMode)) {
            runModeType = RunModeType.selectGender(runMode);
        }
        if (StringUtils.isBlank(processId) || null == currentUser) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("processId is null");
        }
        // Query Process by 'ProcessId' and copy new
        Process process = this.processCopyProcessAndAdd(processId, currentUser, runModeType);
        if (null == process) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("No process Id'" + processId + "'");
        }
        Map<String, Object> stringObjectMap = flowImpl.startFlow(process, checkpoint, runModeType);
        process.setLastUpdateUser(currentUser.getUsername());
        process.setLastUpdateDttm(new Date());
        if (200 == (Integer) stringObjectMap.get("code")) {
            process.setAppId((String) stringObjectMap.get("appId"));
            process.setProcessId((String) stringObjectMap.get("appId"));
            process.setState(ProcessState.STARTED);
            processDomain.saveOrUpdate(process);
            return ReturnMapUtils.setSucceededCustomParamRtnJsonStr("processId", process.getId());
        } else {
            process.setEnableFlag(false);
            processDomain.saveOrUpdate(process);
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Calling interface failed, startup failed");
        }
    }

    /**
     * Stop running processes
     *
     * @param processId
     * @return
     */
    @Override
    public String stopProcess(String processId) {
        Map<String, Object> rtnMap = new HashMap<>();
        rtnMap.put("code", 500);
        if (StringUtils.isNotBlank(processId)) {
            // Query Process by 'ProcessId'
            Process process = processMapper.getProcessById(processId);
            // Determine whether it is empty, and determine whether the save is successful.
            if (null != process) {
                String appId = process.getAppId();
                if (null != appId) {
                    if (ProcessState.STARTED == process.getState()) {
                        String stopFlow = flowImpl.stopFlow(appId);
                        if (StringUtils.isNotBlank(stopFlow) && !stopFlow.contains("Exception")) {
                            rtnMap.put("code", 200);
                            rtnMap.put("errorMsg", "Stop successful, return status is " + stopFlow);
                        } else {
                            logger.warn("Interface return value is null." + stopFlow);
                            rtnMap.put("errorMsg", "Interface return value is " + stopFlow);
                        }
                    } else {
                        logger.warn("The status of the process is " + process.getState() + " and cannot be stopped.");
                        rtnMap.put("errorMsg", "The status of the process is " + process.getState() + " and cannot be stopped.");
                    }
                } else {
                    logger.warn("The 'appId' of the 'process' is empty.");
                    rtnMap.put("errorMsg", "The 'appId' of the 'process' is empty.");
                }
            } else {
                logger.warn("No process ID is '" + processId + "' process");
                rtnMap.put("errorMsg", " No process ID is '" + processId + "' process");
            }
        } else {
            logger.warn("processId is null");
            rtnMap.put("errorMsg", "processId is null");
        }

        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * get debug data
     *
     * @param debugDataRequest
     * @return
     */
    @Override
    public DebugDataResponse getDebugData(DebugDataRequest debugDataRequest) {
        DebugDataResponse debugDataResponse = null;
        if (null != debugDataRequest) {
            // (isNoneEmpty returns false whenever there is a value)
            if (StringUtils.isNoneEmpty(debugDataRequest.getAppID(), debugDataRequest.getStopName(), debugDataRequest.getPortName())) {
                String debugData = flowImpl.getDebugData(debugDataRequest.getAppID(), debugDataRequest.getStopName(), debugDataRequest.getPortName());
                if (StringUtils.isNotBlank(debugData)) {
                    JSONObject obj = JSONObject.fromObject(debugData);
                    String schema = (String) obj.get("schema");
                    String debugDataPath = (String) obj.get("debugDataPath");
                    if (StringUtils.isNotBlank(schema) && StringUtils.isNotBlank(debugDataPath)) {
                        String[] schemaSplit = schema.split(",");
                        debugDataResponse = HdfsUtils.readPath(debugDataPath, debugDataRequest.getStartFileName(), debugDataRequest.getStartLine(), 10);
                        if (null != debugDataResponse) {
                            debugDataResponse.setSchema(Arrays.asList(schemaSplit));
                        }
                    }
                } else {
                    logger.warn("Interface call failed");
                }
            } else {
                logger.warn("param is null");
            }
        } else {
            logger.warn("param is null");
        }
        return debugDataResponse;
    }

    /**
     * Query process based on processId and pageId
     *
     * @param processGroupId
     * @param pageId
     * @return
     */
    @Override
    public ProcessVo getProcessVoByPageId(String processGroupId, String pageId) {
        ProcessVo processVo = null;
        if (StringUtils.isNotBlank(processGroupId) && StringUtils.isNotBlank(pageId)) {
            Process processByPageId = processMapper.getProcessByPageId(processGroupId, pageId);
            processVo = ProcessUtils.processPoToVo(processByPageId);
        }
        return processVo;
    }
}
