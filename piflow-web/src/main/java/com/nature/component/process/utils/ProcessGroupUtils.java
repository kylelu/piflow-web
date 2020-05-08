package com.nature.component.process.utils;

import com.nature.base.util.SvgUtils;
import com.nature.base.vo.UserVo;
import com.nature.common.Eunm.ProcessParentType;
import com.nature.common.Eunm.ProcessState;
import com.nature.common.Eunm.RunModeType;
import com.nature.component.flow.model.Flow;
import com.nature.component.flow.model.FlowGroup;
import com.nature.component.flow.model.FlowGroupPaths;
import com.nature.component.mxGraph.model.MxGraphModel;
import com.nature.component.mxGraph.utils.MxGraphModelUtils;
import com.nature.component.mxGraph.vo.MxGraphModelVo;
import com.nature.component.process.model.Process;
import com.nature.component.process.model.*;
import com.nature.component.process.vo.ProcessGroupPathVo;
import com.nature.component.process.vo.ProcessGroupVo;
import com.nature.component.process.vo.ProcessVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProcessGroupUtils {

    public static ProcessGroup processGroupNewNoId(String username) {

        ProcessGroup processGroup = new ProcessGroup();
        // basic properties (required when creating)
        processGroup.setCrtDttm(new Date());
        processGroup.setCrtUser(username);
        // basic properties
        processGroup.setEnableFlag(true);
        processGroup.setLastUpdateUser(username);
        processGroup.setLastUpdateDttm(new Date());
        processGroup.setVersion(0L);
        return processGroup;
    }

    public static ProcessGroup initProcessGroupBasicPropertiesNoId(ProcessGroup processGroup, String username) {
        if (null == processGroup) {
            return processGroupNewNoId(username);
        }
        // basic properties (required when creating)
        processGroup.setCrtDttm(new Date());
        processGroup.setCrtUser(username);
        // basic properties
        processGroup.setEnableFlag(true);
        processGroup.setLastUpdateUser(username);
        processGroup.setLastUpdateDttm(new Date());
        processGroup.setVersion(0L);
        return processGroup;
    }

    public static ProcessGroup flowGroupToProcessGroup(FlowGroup flowGroup, String username, RunModeType runModeType) {
        ProcessGroup processGroupNew = new ProcessGroup();
        // copy FlowGroup to ProcessGroup
        BeanUtils.copyProperties(flowGroup, processGroupNew);
        processGroupNew = initProcessGroupBasicPropertiesNoId(processGroupNew, username);

        // Take out the sketchpad information of 'flowGroup'
        MxGraphModel flowGroupMxGraphModel = flowGroup.getMxGraphModel();
//----------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------
        // The 'flowGroup' palette information changes to 'viewXml'
        String viewXml = SvgUtils.mxGraphModelToViewXml(flowGroupMxGraphModel, true, false);
        // set viewXml
        processGroupNew.setViewXml(viewXml);
//----------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------
        MxGraphModel mxGraphModelProcessGroup = MxGraphModelUtils.copyMxGraphModelAndNewNoIdAndUnlink(flowGroupMxGraphModel);
        mxGraphModelProcessGroup = MxGraphModelUtils.initMxGraphModelBasicPropertiesNoId(mxGraphModelProcessGroup, username);
        // add link
        mxGraphModelProcessGroup.setProcessGroup(processGroupNew);
        processGroupNew.setMxGraphModel(mxGraphModelProcessGroup);

        // set flowGroupId
        processGroupNew.setFlowId(flowGroup.getId());

        processGroupNew.setRunModeType(runModeType);
        processGroupNew.setProcessParentType(ProcessParentType.GROUP);

        // Get the paths information of flow
        List<FlowGroupPaths> flowGroupPathsList = flowGroup.getFlowGroupPathsList();
        // isEmpty
        if (null != flowGroupPathsList && flowGroupPathsList.size() > 0) {
            List<ProcessGroupPath> processGroupPathList = new ArrayList<>();
            // Loop paths information
            for (FlowGroupPaths flowGroupPaths : flowGroupPathsList) {
                // isEmpty
                if (null != flowGroupPaths) {
                    ProcessGroupPath processGroupPath = new ProcessGroupPath();
                    // Copy flowGroupPaths information into processGroupPath
                    BeanUtils.copyProperties(flowGroupPaths, processGroupPath);
                    // Set basic information
                    processGroupPath.setCrtDttm(new Date());
                    processGroupPath.setCrtUser(username);
                    processGroupPath.setLastUpdateDttm(new Date());
                    processGroupPath.setLastUpdateUser(username);
                    processGroupPath.setEnableFlag(true);
                    // Associated foreign key
                    processGroupPath.setProcessGroup(processGroupNew);
                    processGroupPathList.add(processGroupPath);
                }
            }
            processGroupNew.setProcessGroupPathList(processGroupPathList);
        }

        // flow to remove flowGroup
        List<Flow> flowList = flowGroup.getFlowList();
        // flowList isEmpty
        if (null != flowList && flowList.size() > 0) {
            // List of stop of process
            List<Process> processList = new ArrayList<>();
            // Loop flowList
            for (Flow flow : flowList) {
                // isEmpty
                if (null == flow) {
                    continue;
                }
                Process processNew = ProcessUtils.flowToProcess(flow, username);
                if (null == processNew) {
                    continue;
                }
                processNew.setProcessGroup(processGroupNew);
                processList.add(processNew);
            }
            processGroupNew.setProcessList(processList);
        }

        List<FlowGroup> flowGroupList = flowGroup.getFlowGroupList();
        if (null != flowGroupList && flowGroupList.size() > 0) {
            // List of stop of process
            List<ProcessGroup> processGroupList = new ArrayList<>();
            // Loop flowGroupList
            for (FlowGroup flowGroupList_i : flowGroupList) {
                ProcessGroup processGroupChildNew = flowGroupToProcessGroup(flowGroupList_i, username, runModeType);
                processGroupChildNew.setProcessGroup(processGroupNew);
                processGroupList.add(processGroupChildNew);
            }
            processGroupNew.setProcessGroupList(processGroupList);
        }

        return processGroupNew;
    }

    public static List<ProcessGroup> copyProcessGroupList(List<ProcessGroup> processGroupList, ProcessGroup processGroup, UserVo currentUser, RunModeType runModeType) {
        List<ProcessGroup> copyProcessGroupList = null;
        if (null != processGroupList && processGroupList.size() > 0) {
            copyProcessGroupList = new ArrayList<>();
            for (ProcessGroup processGroup_new : processGroupList) {
                ProcessGroup copyProcessGroup = copyProcessGroup(processGroup_new, currentUser, runModeType);
                if (null != copyProcessGroup) {
                    copyProcessGroup.setProcessGroup(processGroup);
                    copyProcessGroupList.add(copyProcessGroup);
                }
            }
        }
        return copyProcessGroupList;
    }

    public static ProcessGroup copyProcessGroup(ProcessGroup processGroup, UserVo currentUser, RunModeType runModeType) {
        if (null == currentUser) {
            return null;
        }
        String username = currentUser.getUsername();
        if (null == processGroup) {
            return null;
        }
        ProcessGroup copyProcessGroup = new ProcessGroup();
        BeanUtils.copyProperties(processGroup, copyProcessGroup);
        copyProcessGroup = ProcessGroupUtils.initProcessGroupBasicPropertiesNoId(copyProcessGroup, username);
        copyProcessGroup.setId(null);
        copyProcessGroup.setParentProcessId(StringUtils.isNotBlank(processGroup.getParentProcessId()) ? processGroup.getParentProcessId() : processGroup.getProcessId());
        copyProcessGroup.setState(ProcessState.INIT);
        copyProcessGroup.setRunModeType(null != runModeType ? runModeType : RunModeType.RUN);
        copyProcessGroup.setProcessParentType(ProcessParentType.GROUP);
        copyProcessGroup.setStartTime(null);
        copyProcessGroup.setEndTime(null);
        copyProcessGroup.setProgress("0.00");

        // copyMxGraphModel remove Id
        MxGraphModel copyMxGraphModel = copyProcessGroup.getMxGraphModel();
        if (null != copyMxGraphModel) {
            copyMxGraphModel = MxGraphModelUtils.copyMxGraphModelAndNewNoIdAndUnlink(copyMxGraphModel);
            copyMxGraphModel = MxGraphModelUtils.initMxGraphModelBasicPropertiesNoId(copyMxGraphModel, username);
            // add link
            copyMxGraphModel.setProcessGroup(copyProcessGroup);
            copyProcessGroup.setMxGraphModel(copyMxGraphModel);
        }

        // processGroupPathList
        List<ProcessGroupPath> processGroupPathList = processGroup.getProcessGroupPathList();
        copyProcessGroup.setProcessGroupPathList(copyProcessGroupPathList(processGroupPathList, copyProcessGroup, username));
        // processList
        List<Process> processList = processGroup.getProcessList();
        List<Process> copyProcessList = ProcessUtils.copyProcessList(processList, currentUser, runModeType, copyProcessGroup);
        copyProcessGroup.setProcessList(copyProcessList);
        // processGroupList
        List<ProcessGroup> processGroupList = processGroup.getProcessGroupList();
        List<ProcessGroup> copyProcessGroupList = copyProcessGroupList(processGroupList, copyProcessGroup, currentUser, runModeType);
        copyProcessGroup.setProcessGroupList(copyProcessGroupList);
        return copyProcessGroup;
    }

    public static List<ProcessGroupPath> copyProcessGroupPathList(List<ProcessGroupPath> processGroupPathList, ProcessGroup copyProcessGroup, String username) {
        List<ProcessGroupPath> copyProcessGroupPathList = null;
        if (null != processGroupPathList && processGroupPathList.size() > 0) {
            copyProcessGroupPathList = new ArrayList<>();
            for (ProcessGroupPath processGroupPath : processGroupPathList) {
                ProcessGroupPath copyProcessGroupPath = copyProcessGroupPath(processGroupPath, username);
                if (null != copyProcessGroupPath) {
                    copyProcessGroupPath.setProcessGroup(copyProcessGroup);
                    copyProcessGroupPathList.add(copyProcessGroupPath);
                }
            }
            //copyProcessGroup.setProcessGroupPathList(copyProcessGroupPathList);
        }
        return copyProcessGroupPathList;
    }

    public static ProcessGroupPath copyProcessGroupPath(ProcessGroupPath processGroupPath, String username) {
        if (null == processGroupPath) {
            return null;
        }
        ProcessGroupPath copyProcessGroupPath = new ProcessGroupPath();
        copyProcessGroupPath.setCrtDttm(new Date());
        copyProcessGroupPath.setCrtUser(username);
        copyProcessGroupPath.setLastUpdateDttm(new Date());
        copyProcessGroupPath.setLastUpdateUser(username);
        copyProcessGroupPath.setEnableFlag(true);
        copyProcessGroupPath.setFrom(processGroupPath.getFrom());
        copyProcessGroupPath.setTo(processGroupPath.getTo());
        copyProcessGroupPath.setInport(processGroupPath.getInport());
        copyProcessGroupPath.setOutport(processGroupPath.getOutport());
        copyProcessGroupPath.setPageId(processGroupPath.getPageId());
        return copyProcessGroupPath;
    }

    public static ProcessGroupVo processGroupPoToVo(ProcessGroup processGroup) {
        if (null == processGroup) {
            return null;
        }
        ProcessGroupVo processGroupVo = new ProcessGroupVo();

        BeanUtils.copyProperties(processGroup, processGroupVo);
        processGroupVo.setProgress(StringUtils.isNotBlank(processGroup.getProgress()) ? processGroup.getProgress() : "0.00");

        // Parents ProcessGroup Copy
        ProcessGroup parentsProcessGroup = processGroup.getProcessGroup();
        if (null != parentsProcessGroup) {
            ProcessGroupVo parentsProcessGroupVo = new ProcessGroupVo();
            BeanUtils.copyProperties(parentsProcessGroup, parentsProcessGroupVo);
            processGroupVo.setProcessGroupVo(parentsProcessGroupVo);
        }

        // MxGraphModel Copy
        MxGraphModel mxGraphModel = processGroup.getMxGraphModel();
        MxGraphModelVo mxGraphModelVo = MxGraphModelUtils.mxGraphModelPoToVo(mxGraphModel);
        processGroupVo.setMxGraphModelVo(mxGraphModelVo);

        //Process List Copy
        List<Process> processList = processGroup.getProcessList();
        if (null != processList && processList.size() > 0) {
            List<ProcessVo> processVoList = new ArrayList<>();
            for (Process process : processList) {
                ProcessVo processVo = ProcessUtils.processPoToVo(process);
                if (null == processVo) {
                    continue;
                }
                processVo.setState(process.getState());
                processVoList.add(processVo);
            }
            processGroupVo.setProcessVoList(processVoList);
        }

        // ProcessGroupPath List Copy
        List<ProcessGroupPath> processGroupPathList = processGroup.getProcessGroupPathList();
        if (null != processGroupPathList && processGroupPathList.size() > 0) {
            List<ProcessGroupPathVo> processGroupPathVoList = new ArrayList<>();
            ProcessGroupPathVo processGroupPathVo;
            for (ProcessGroupPath processGroupPath : processGroupPathList) {
                if (null == processGroupPath) {
                    continue;
                }
                processGroupPathVo = new ProcessGroupPathVo();
                BeanUtils.copyProperties(processGroupPath, processGroupPathVo);
                processGroupPathVoList.add(processGroupPathVo);
            }
            processGroupVo.setProcessGroupPathVoList(processGroupPathVoList);
        }

        // ProcessGroup List Copy
        List<ProcessGroup> processGroupList = processGroup.getProcessGroupList();
        if (null != processGroupList && processGroupList.size() > 0) {
            List<ProcessGroupVo> processGroupVoList = new ArrayList<>();
            ProcessGroupVo copy_ProcessGroupVo_I;
            for (ProcessGroup processGroup_i : processGroupList) {
                copy_ProcessGroupVo_I = processGroupPoToVo(processGroup_i);
                if (null == copy_ProcessGroupVo_I) {
                    continue;
                }
                processGroupVoList.add(copy_ProcessGroupVo_I);
            }
            processGroupVo.setProcessGroupVoList(processGroupVoList);
        }
        return processGroupVo;
    }
}
