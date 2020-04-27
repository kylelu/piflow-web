package com.nature.component.process.service.Impl;

import com.github.pagehelper.PageHelper;
import com.nature.base.util.*;
import com.nature.base.vo.UserVo;
import com.nature.common.Eunm.PortType;
import com.nature.common.Eunm.ProcessParentType;
import com.nature.common.Eunm.ProcessState;
import com.nature.common.Eunm.RunModeType;
import com.nature.component.process.model.ProcessGroup;
import com.nature.component.process.model.ProcessGroupPath;
import com.nature.component.process.model.ProcessStop;
import com.nature.component.process.service.IProcessGroupService;
import com.nature.component.process.utils.ProcessGroupUtils;
import com.nature.component.process.utils.ProcessUtils;
import com.nature.component.process.vo.*;
import com.nature.domain.process.ProcessDomain;
import com.nature.domain.process.ProcessGroupDomain;
import com.nature.domain.process.ProcessGroupPathDomain;
import com.nature.mapper.process.ProcessGroupMapper;
import com.nature.third.service.IFlow;
import com.nature.third.service.IGroup;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ProcessGroupServiceImpl implements IProcessGroupService {

    Logger logger = LoggerUtil.getLogger();

    @Resource
    private ProcessGroupDomain processGroupDomain;

    @Resource
    private ProcessGroupPathDomain processGroupPathDomain;

    @Resource
    private ProcessDomain processDomain;

    @Resource
    private ProcessGroupMapper processGroupMapper;

    @Resource
    private IGroup groupImpl;

    @Resource
    private IFlow flowImpl;

    /**
     * Query processVo based on id (query contains its child table)
     *
     * @param id ProcessGroup Id
     * @return ProcessGroupVo (query contains its child table)
     */
    @Override
    public ProcessGroupVo getProcessGroupVoAllById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        ProcessGroup processGroupById = processGroupDomain.getProcessGroupById(id);
        return ProcessGroupUtils.processGroupPoToVo(processGroupById);
    }

    /**
     * Query processGroupVo based on id (only query process table)
     *
     * @param id ProcessGroup Id
     * @return ProcessGroupVo (Only themselves do not include subtables)
     */
    @Override
    public ProcessGroupVo getProcessGroupVoById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        ProcessGroup processGroupById = processGroupDomain.getProcessGroupById(id);
        if (null == processGroupById) {
            return null;
        }
        ProcessGroupVo processGroupVo = new ProcessGroupVo();
        BeanUtils.copyProperties(processGroupById, processGroupVo);
        processGroupVo.setCrtDttm(processGroupById.getCrtDttm());
        return processGroupVo;
    }

    /**
     * Query appInfo according to appID
     *
     * @param appID appId
     * @return ProcessGroupVo
     */
    @Override
    public String getAppInfoByAppId(String appID) {

        if (StringUtils.isBlank(appID)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("appID is null");
        }
        // find appInfo
        ProcessGroup processGroupByAppId = processGroupDomain.getProcessGroupByAppId(appID);
        ProcessGroupVo processGroupVo = ProcessGroupUtils.processGroupPoToVo(processGroupByAppId);
        if (null == processGroupVo) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("No data was queried");

        }
        Map<String, Object> rtnMap = ReturnMapUtils.setSucceededMsg(ReturnMapUtils.SUCCEEDED_MSG);
        rtnMap.put("progress", (null != processGroupVo.getProgress() ? processGroupVo.getProgress() : "0.00"));
        rtnMap.put("state", (null != processGroupVo.getState() ? processGroupVo.getState().name() : "NO_STATE"));
        rtnMap.put("processGroupVo", processGroupVo);
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Query  appInfo according to appID
     *
     * @param appIDs AppId array
     * @return string
     */
    @Override
    public String getAppInfoByAppIds(String[] appIDs) {
        if (null == appIDs || appIDs.length <= 0) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Incoming parameter is null");
        }
        List<ProcessGroup> processGroupListByAppIDs = processGroupMapper.getProcessGroupListByAppIDs(appIDs);
        if (CollectionUtils.isEmpty(processGroupListByAppIDs)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("No data was queried");
        }
        Map<String, Object> rtnMap = ReturnMapUtils.setSucceededMsg(ReturnMapUtils.SUCCEEDED_MSG);
        for (ProcessGroup processGroup : processGroupListByAppIDs) {
            if (null == processGroup) {
                continue;
            }
            ProcessGroupVo processGroupVo = ProcessGroupUtils.processGroupPoToVo(processGroup);
            if (null != processGroupVo) {
                rtnMap.put(processGroupVo.getAppId(), processGroupVo);
            }
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Start processesGroup
     *
     * @param processGroupId Run ProcessGroup Id
     * @param checkpoint     checkpoint
     * @param currentUser    currentUser
     * @return json
     */
    @Override
    @Transactional
    public String startProcessGroup(String processGroupId, String checkpoint, String runMode, UserVo currentUser) {
        Map<String, Object> rtnMap = new HashMap<>();
        rtnMap.put("code", 500);
        RunModeType runModeType = RunModeType.RUN;
        if (StringUtils.isNotBlank(runMode)) {
            runModeType = RunModeType.selectGender(runMode);
        }
        if (StringUtils.isNotBlank(processGroupId) && null != currentUser) {
            // Query Process by 'processGroupId'
            ProcessGroup processGroupById = processGroupDomain.getProcessGroupById(processGroupId);
            // copy and Create
            ProcessGroup processGroupCopy = ProcessGroupUtils.copyProcessGroup(processGroupById, currentUser, runModeType);
            // ProcessGroup processGroupCopy = this.copyProcessGroupAndNewCreate(processGroupById, currentUser, runModeType);
            processGroupCopy = processGroupDomain.saveOrUpdate(processGroupCopy);

            if (null != processGroupCopy) {
                Map<String, Object> stringObjectMap = groupImpl.startFlowGroup(processGroupCopy, runModeType);
                if (200 == (Integer) stringObjectMap.get("code")) {
                    processGroupCopy.setAppId((String) stringObjectMap.get("appId"));
                    processGroupCopy.setProcessId((String) stringObjectMap.get("appId"));
                    processGroupCopy.setState(ProcessState.STARTED);
                    processGroupCopy.setLastUpdateUser(currentUser.getUsername());
                    processGroupCopy.setLastUpdateDttm(new Date());
                    processGroupCopy.setProcessParentType(ProcessParentType.GROUP);
                    processGroupDomain.saveOrUpdate(processGroupCopy);
                    rtnMap.put("code", 200);
                    rtnMap.put("processGroupId", processGroupCopy.getId());
                    rtnMap.put("errorMsg", "Successful startup");
                    logger.info("save process success,update success");
                } else {
                    processGroupDomain.updateEnableFlagById(processGroupCopy.getId(), false);
                    rtnMap.put("errorMsg", "Calling interface failed, startup failed");
                    logger.warn("Calling interface failed, startup failed");
                }
            } else {
                rtnMap.put("errorMsg", "No process group Id'" + processGroupId + "'");
                logger.warn("No process group Id'" + processGroupId + "'");
            }
        } else {
            rtnMap.put("errorMsg", "processGroupId is null");
            logger.warn("processGroupId is null");
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Query processGroupVoList (parameter space-time non-paging)
     *
     * @param offset Number of pages
     * @param limit  Number each page
     * @param param  Search content
     * @return json
     */
    @Override
    public String getProcessGroupVoListPage(Integer offset, Integer limit, String param) {
        if (null == offset || null == limit) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr(ReturnMapUtils.ERROR_MSG);
        }
        Page<ProcessGroup> processGroupListPage = processGroupDomain.getProcessGroupListPage(offset - 1, limit, param);
        List<ProcessGroup> content = processGroupListPage.getContent();
        List<ProcessGroupVo> processGroupVoList = null;
        if (null != content && content.size() > 0) {
            processGroupVoList = new ArrayList<>();
            ProcessGroupVo processGroupVo;
            for (ProcessGroup processGroup : content) {
                if (null == processGroup) {
                    continue;
                }
                processGroupVo = new ProcessGroupVo();
                BeanUtils.copyProperties(processGroup, processGroupVo);
                processGroupVoList.add(processGroupVo);
            }
        }
        Map<String, Object> rtnMap = ReturnMapUtils.setSucceededMsg(ReturnMapUtils.SUCCEEDED_MSG);
        rtnMap.put("msg", "");
        rtnMap.put("count", processGroupListPage.getTotalElements());
        rtnMap.put("data", processGroupVoList);//Data collection
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Stop running processGroup
     *
     * @param processGroupId ProcessGroup Id
     * @return json
     */
    @Override
    public String stopProcessGroup(String processGroupId) {
        Map<String, Object> rtnMap = new HashMap<>();
        rtnMap.put("code", 500);
        if (StringUtils.isNotBlank(processGroupId)) {
            // Query Process by 'processGroupId'
            ProcessGroup processGroup = processGroupMapper.getProcessGroupById(processGroupId);
            // Determine whether it is empty, and determine whether the save is successful.
            if (null != processGroup) {
                String appId = processGroup.getAppId();
                if (null != appId) {
                    if (ProcessState.STARTED == processGroup.getState()) {
                        String stopFlow = groupImpl.stopFlowGroup(appId);
                        if (StringUtils.isNotBlank(stopFlow) && !stopFlow.contains("Exception")) {
                            rtnMap.put("code", 200);
                            rtnMap.put("errorMsg", "Stop successful, return status is " + stopFlow);
                        } else {
                            logger.warn("Interface return value is null." + stopFlow);
                            rtnMap.put("errorMsg", "Interface return value is " + stopFlow);
                        }
                    } else {
                        logger.warn("The status of the process is " + processGroup.getState() + " and cannot be stopped.");
                        rtnMap.put("errorMsg", "The status of the process is " + processGroup.getState() + " and cannot be stopped.");
                    }
                } else {
                    logger.warn("The 'appId' of the 'process' is empty.");
                    rtnMap.put("errorMsg", "The 'appId' of the 'process' is empty.");
                }
            } else {
                logger.warn("No process ID is '" + processGroupId + "' process");
                rtnMap.put("errorMsg", " No process ID is '" + processGroupId + "' process");
            }
        } else {
            logger.warn("processGroupId is null");
            rtnMap.put("errorMsg", "processGroupId is null");
        }

        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * get debug data
     *
     * @param debugDataRequest DebugDataRequest
     * @return DebugDataResponse
     */
    @Override
    public DebugDataResponse getDebugData(DebugDataRequest debugDataRequest) {
        DebugDataResponse debugDataResponse = null;
        if (null == debugDataRequest) {
            logger.warn("param is null");
            return null;
        }
        // Returns true when all is null
        if (StringUtils.isAllEmpty(debugDataRequest.getAppID(), debugDataRequest.getStopName(), debugDataRequest.getPortName())) {
            logger.warn("param is null");
            return null;
        }
        String debugData = flowImpl.getDebugData(debugDataRequest.getAppID(), debugDataRequest.getStopName(), debugDataRequest.getPortName());
        if (StringUtils.isBlank(debugData)) {
            logger.warn("Interface call failed");
            return null;
        }
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
        return debugDataResponse;
    }

    /**
     * delProcessGroup
     *
     * @param processGroupID ProcessGroup Id
     * @return json
     */
    @Override
    public String delProcessGroup(String processGroupID) {
        Map<String, Object> rtnMap = new HashMap<>();
        rtnMap.put("code", 500);
        if (StringUtils.isNotBlank(processGroupID)) {
            UserVo currentUser = SessionUserUtil.getCurrentUser();
            // Query Process by 'processGroupID'
            ProcessGroup processGroupById = processGroupMapper.getProcessGroupById(processGroupID);
            if (null != processGroupById) {
                if (processGroupById.getState() != ProcessState.STARTED) {
                    int updateEnableFlagById = processGroupMapper.updateEnableFlagById(processGroupID, currentUser.getUsername());
                    // Determine whether the deletion is successful
                    if (updateEnableFlagById > 0) {
                        rtnMap.put("code", 200);
                        rtnMap.put("errorMsg", "Successfully Deleted");
                    } else {
                        logger.warn("Failed to delete");
                        rtnMap.put("errorMsg", "Failed to delete");
                    }
                } else {
                    logger.warn("Status is STARTED, cannot be deleted");
                    rtnMap.put("errorMsg", "Status is STARTED, cannot be deleted");
                }
            } else {
                logger.warn("No process ID is '" + processGroupID + "' process");
                rtnMap.put("errorMsg", "No process ID is '" + processGroupID + "' process");
            }
        } else {
            logger.warn("processGroupID is null");
            rtnMap.put("errorMsg", "processGroupID is null");
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * getGroupLogData
     *
     * @param processGroupAppID ProcessGroup AppId
     * @return json
     */
    @Override
    public String getGroupLogData(String processGroupAppID) {
        Map<String, Object> rtnMap = new HashMap<>();
        rtnMap.put("code", 500);
        if (StringUtils.isNotBlank(processGroupAppID)) {
            // Query groupLogData by 'processGroupAppID'
            String groupLogData = groupImpl.getFlowGroupInfoStr(processGroupAppID);
            if (StringUtils.isNotBlank(groupLogData)) {
                rtnMap.put("code", 200);
                rtnMap.put("data", groupLogData);
            } else {
                logger.warn("No process ID is '" + processGroupAppID + "' process");
                rtnMap.put("errorMsg", "Interface return data is empty");
            }
        } else {
            logger.warn("processGroupID is null");
            rtnMap.put("errorMsg", "processGroupID is null");
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * getStartGroupJson
     *
     * @param processGroupId ProcessGroup Id
     * @return json
     */
    @Override
    @Transactional
    public String getStartGroupJson(String processGroupId) {
        if (StringUtils.isBlank(processGroupId)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("processGroupID is null");
        }
        // Query groupLogData by 'processGroupId'
        ProcessGroup processGroup = processGroupDomain.getProcessGroupById(processGroupId);
        if (null == processGroup) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("No process ID is '" + processGroupId + "' process");
        }
        String formatJson = ProcessUtils.processGroupToJson(processGroup, processGroup.getRunModeType());
        return ReturnMapUtils.setSucceededCustomParamRtnJsonStr("data", formatJson);
    }

    /**
     * getProcessIdByPageId
     *
     * @param fId    Parents Id
     * @param pageId MxGraph PageId
     * @return json
     */
    @Override
    public String getProcessIdByPageId(String fId, String pageId) {
        return processDomain.getProcessIdByPageId(fId, pageId);
    }

    /**
     * getProcessGroupIdByPageId
     *
     * @param fId    Parents Id
     * @param pageId MxGraph PageId
     * @return json
     */
    @Override
    public String getProcessGroupIdByPageId(String fId, String pageId) {
        return processGroupDomain.getProcessIdGroupByPageId(fId, pageId);
    }

    /**
     * getProcessGroupVoByPageId
     *
     * @param processGroupId ProcessGroup Id
     * @param pageId         MxGraph PageId
     * @return json
     */
    @Override
    @Transactional
    public ProcessGroupVo getProcessGroupVoByPageId(String processGroupId, String pageId) {
        ProcessGroupVo processGroupVo = null;
        ProcessGroup processGroup = processGroupDomain.getProcessGroupByPageId(processGroupId, pageId);
        if (null != processGroup) {
            processGroupVo = new ProcessGroupVo();
            BeanUtils.copyProperties(processGroup, processGroupVo);
            // List<ProcessGroup> processGroupList = processGroup.getProcessGroupList();
            // List<Process> processList = processGroup.getProcessList();
            // if (null != processGroupList) {
            //     processGroupVo.setFlowGroupQuantity(processGroupList.size());
            // }
            // if (null != processList) {
            //     processGroupVo.setFlowQuantity(processList.size());
            // }
        }
        return processGroupVo;
    }

    /**
     * getProcessGroupPathVoByPageId
     *
     * @param processGroupId ProcessGroup Id
     * @param pageId         MxGraph PageId
     * @return json
     */
    public ProcessGroupPathVo getProcessGroupPathVoByPageId(String processGroupId, String pageId) {
        ProcessGroupPath processGroupPathByPageId = processGroupPathDomain.getProcessGroupPathByPageId(processGroupId, pageId);
        if (null == processGroupPathByPageId) {
            return null;
        }
        List<String> pageIds = new ArrayList<>();
        String pathTo = processGroupPathByPageId.getTo();
        String pathFrom = processGroupPathByPageId.getFrom();
        if (StringUtils.isNotBlank(pathFrom)) {
            pageIds.add(pathFrom);
        }
        if (StringUtils.isNotBlank(pathTo)) {
            pageIds.add(pathTo);
            ;
        }
        if (StringUtils.isBlank(processGroupId) || null == pageIds || pageIds.size() <= 0) {
            return null;
        }
        List<Map<String, Object>> processGroupNamesAndPageIdsByPageIds = processGroupDomain.getProcessGroupNamesAndPageIdsByPageIds(processGroupId, pageIds);
        if (null == processGroupNamesAndPageIdsByPageIds || processGroupNamesAndPageIdsByPageIds.size() <= 0) {
            return null;
        }
        ProcessGroupPathVo processGroupPathVo = new ProcessGroupPathVo();
        pathTo = (null == pathTo ? "" : pathTo);
        pathFrom = (null == pathTo ? "" : pathFrom);
        for (Map<String, Object> processGroupNameAndPageId : processGroupNamesAndPageIdsByPageIds) {
            if (null != processGroupNameAndPageId) {
                String currentpageId = (String) processGroupNameAndPageId.get("pageId");
                if (pathTo.equals(currentpageId)) {
                    processGroupPathVo.setTo((String) processGroupNameAndPageId.get("name"));
                } else if (pathFrom.equals(currentpageId)) {
                    processGroupPathVo.setFrom((String) processGroupNameAndPageId.get("name"));
                }
            }
        }
        processGroupPathVo.setInport(StringUtils.isNotBlank(processGroupPathByPageId.getInport()) ? processGroupPathByPageId.getInport() : PortType.DEFAULT.getText());
        processGroupPathVo.setOutport(StringUtils.isNotBlank(processGroupPathByPageId.getOutport()) ? processGroupPathByPageId.getOutport() : PortType.DEFAULT.getText());
        return processGroupPathVo;
    }

}