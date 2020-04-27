package com.nature.component.process.utils;

import com.nature.base.util.SvgUtils;
import com.nature.base.vo.UserVo;
import com.nature.common.Eunm.ProcessParentType;
import com.nature.common.Eunm.ProcessState;
import com.nature.common.Eunm.RunModeType;
import com.nature.component.flow.model.*;
import com.nature.component.flow.utils.FlowUtil;
import com.nature.component.mxGraph.model.MxGraphModel;
import com.nature.component.mxGraph.utils.MxGraphModelUtils;
import com.nature.component.mxGraph.vo.MxGraphModelVo;
import com.nature.component.process.model.Process;
import com.nature.component.process.model.*;
import com.nature.component.process.vo.*;
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
        ProcessGroup processGroupNew = processGroupNewNoId(username);
        // copy FlowGroup to ProcessGroup
        BeanUtils.copyProperties(flowGroup, processGroupNew);

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
                if (null != processGroupChildNew) {
                    processGroupChildNew.setProcessGroup(processGroupNew);
                    processGroupList.add(processGroupChildNew);
                }
            }
            processGroupNew.setProcessGroupList(processGroupList);
        }

        return processGroupNew;
    }

    public static List<ProcessGroup> copyProcessGroupList(List<ProcessGroup> processGroupList, ProcessGroup processGroup, UserVo currentUser, RunModeType runModeType) {
        List<ProcessGroup> processGroupListCopy = null;
        if (null != processGroupList && processGroupList.size() > 0) {
            processGroupListCopy = new ArrayList<>();
            for (ProcessGroup processGroup_new : processGroupList) {
                ProcessGroup processGroupCopy = copyProcessGroup(processGroup_new, currentUser, runModeType);
                if (null != processGroupCopy) {
                    processGroupCopy.setProcessGroup(processGroup);
                    processGroupListCopy.add(processGroupCopy);
                }
            }
        }
        return processGroupListCopy;
    }

    public static ProcessGroup copyProcessGroup(ProcessGroup processGroup, UserVo currentUser, RunModeType runModeType) {
        if (null == currentUser) {
            return null;
        }
        String username = currentUser.getUsername();
        if (null == processGroup) {
            return null;
        }
        ProcessGroup processGroupCopy = new ProcessGroup();
        BeanUtils.copyProperties(processGroup, processGroupCopy);
        processGroupCopy = ProcessGroupUtils.initProcessGroupBasicPropertiesNoId(processGroupCopy, username);
        processGroupCopy.setId(null);
        processGroupCopy.setParentProcessId(StringUtils.isNotBlank(processGroup.getParentProcessId()) ? processGroup.getParentProcessId() : processGroup.getProcessId());
        processGroupCopy.setState(ProcessState.STARTED);
        processGroupCopy.setRunModeType(null != runModeType ? runModeType : RunModeType.RUN);
        processGroupCopy.setProcessParentType(ProcessParentType.GROUP);

        // mxGraphModelCopy remove Id
        MxGraphModel mxGraphModelCopy = processGroupCopy.getMxGraphModel();
        if (null != mxGraphModelCopy) {
            mxGraphModelCopy = MxGraphModelUtils.copyMxGraphModelAndNewNoIdAndUnlink(mxGraphModelCopy);
            mxGraphModelCopy = MxGraphModelUtils.initMxGraphModelBasicPropertiesNoId(mxGraphModelCopy, username);
            // add link
            mxGraphModelCopy.setProcessGroup(processGroupCopy);
            processGroupCopy.setMxGraphModel(mxGraphModelCopy);
        }

        // processGroupPathList
        List<ProcessGroupPath> processGroupPathList = processGroup.getProcessGroupPathList();
        processGroupCopy.setProcessGroupPathList(copyProcessGroupPathList(processGroupPathList, processGroupCopy, username));
        // processList
        List<Process> processList = processGroup.getProcessList();
        processGroupCopy.setProcessList(copyProcessList(processList, currentUser, runModeType, processGroupCopy));
        // processGroupList
        List<ProcessGroup> processGroupList = processGroup.getProcessGroupList();
        processGroup.setProcessGroupList(copyProcessGroupList(processGroupList, processGroupCopy, currentUser, runModeType));
        return processGroupCopy;
    }

    public static List<Process> copyProcessList(List<Process> processList, UserVo currentUser, RunModeType runModeType, ProcessGroup processGroup) {
        List<Process> processListCopy = null;
        if (null != processList && processList.size() > 0) {
            processListCopy = new ArrayList<>();
            for (Process process : processList) {
                Process processCopy = copyProcess(process, currentUser, runModeType, processGroup);
                processListCopy.add(processCopy);
            }
        }
        return processListCopy;
    }

    public static Process copyProcess(Process process, UserVo currentUser, RunModeType runModeType, ProcessGroup processGroup) {
        if (null == currentUser) {
            return null;
        }
        String username = currentUser.getUsername();
        if (StringUtils.isBlank(username) || null == process) {
            return null;
        }
        // process
        Process processNew = new Process();
        BeanUtils.copyProperties(process, processNew);
        processNew = ProcessUtils.initProcessBasicPropertiesNoId(processNew, username);
        processNew.setId(null);
        processNew.setState(ProcessState.INIT);
        processNew.setRunModeType(null != runModeType ? runModeType : RunModeType.RUN);
        processNew.setParentProcessId(StringUtils.isNotBlank(process.getParentProcessId()) ? process.getParentProcessId() : process.getProcessId());
        processNew.setProcessParentType(ProcessParentType.GROUP);

        //unlink processGroup
        processNew.setProcessGroup(null);
        //link processGroup
        processNew.setProcessGroup(processGroup);

        // copy processMxGraphModel
        MxGraphModel processMxGraphModel = process.getMxGraphModel();
        MxGraphModel mxGraphModelNew = MxGraphModelUtils.copyMxGraphModelAndNewNoIdAndUnlink(processMxGraphModel);
        mxGraphModelNew = MxGraphModelUtils.initMxGraphModelBasicPropertiesNoId(mxGraphModelNew, username);
        // add link
        mxGraphModelNew.setProcess(processNew);
        processNew.setMxGraphModel(mxGraphModelNew);

        //processPathList
        List<ProcessPath> processPathList = process.getProcessPathList();
        if (null != processPathList && processPathList.size() > 0) {
            List<ProcessPath> processPathListNew = new ArrayList<ProcessPath>();
            for (ProcessPath processPath : processPathList) {
                if (null != processPath) {
                    ProcessPath processPathNew = new ProcessPath();
                    BeanUtils.copyProperties(processPath, processPathNew);
                    processPathNew = ProcessPathUtils.initProcessPathBasicPropertiesNoId(processPathNew, username);
                    processPathNew.setId(null);
                    processPathNew.setProcess(processNew);
                    processPathListNew.add(processPathNew);
                }
            }
            processNew.setProcessPathList(processPathListNew);
        }

        //processStopList
        List<ProcessStop> processStopList = process.getProcessStopList();
        if (null != processStopList && processStopList.size() > 0) {
            List<ProcessStop> processStopListNew = new ArrayList<ProcessStop>();
            for (ProcessStop processStop : processStopList) {
                if (null == processStop) {
                    continue;
                }
                ProcessStop processStopNew = new ProcessStop();
                BeanUtils.copyProperties(processStop, processStopNew);
                processStopNew = ProcessStopUtils.initProcessStopBasicPropertiesNoId(processStopNew, username);
                processStopNew.setId(null);
                processStopNew.setProcess(processNew);
                List<ProcessStopProperty> processStopPropertyList = processStop.getProcessStopPropertyList();
                if (null != processStopPropertyList && processStopPropertyList.size() > 0) {
                    List<ProcessStopProperty> processStopPropertyListNew = new ArrayList<>();
                    for (ProcessStopProperty processStopProperty : processStopPropertyList) {
                        if (null == processStopProperty) {
                            continue;
                        }
                        ProcessStopProperty processStopPropertyNew = new ProcessStopProperty();
                        BeanUtils.copyProperties(processStopProperty, processStopPropertyNew);
                        processStopPropertyNew = ProcessStopPropertyUtils.initProcessStopPropertyBasicPropertiesNoId(processStopPropertyNew, username);
                        processStopPropertyNew.setId(null);
                        processStopPropertyNew.setSensitive(processStopPropertyNew.getSensitive());
                        processStopPropertyNew.setProcessStop(processStopNew);
                        processStopPropertyListNew.add(processStopPropertyNew);
                    }
                    processStopNew.setProcessStopPropertyList(processStopPropertyListNew);
                }
                processStopListNew.add(processStopNew);
            }
            processNew.setProcessStopList(processStopListNew);
        }
        return processNew;
    }

    public static List<ProcessGroupPath> copyProcessGroupPathList(List<ProcessGroupPath> processGroupPathList, ProcessGroup processGroupCopy, String username) {
        List<ProcessGroupPath> processGroupPathListCopy = null;
        if (null != processGroupPathList && processGroupPathList.size() > 0) {
            processGroupPathListCopy = new ArrayList<>();
            for (ProcessGroupPath processGroupPath : processGroupPathList) {
                ProcessGroupPath processGroupPathCopy = copyProcessGroupPath(processGroupPath, processGroupCopy, username);
                if (null != processGroupPathCopy) {
                    processGroupPathListCopy.add(processGroupPathCopy);
                }
            }
            processGroupCopy.setProcessGroupPathList(processGroupPathListCopy);
        }
        return processGroupPathListCopy;
    }

    public static ProcessGroupPath copyProcessGroupPath(ProcessGroupPath processGroupPath, ProcessGroup processGroupCopy, String username) {
        ProcessGroupPath processGroupPathCopy = null;
        if (null != processGroupPath) {
            processGroupPathCopy = new ProcessGroupPath();
            processGroupPathCopy.setCrtDttm(new Date());
            processGroupPathCopy.setCrtUser(username);
            processGroupPathCopy.setLastUpdateDttm(new Date());
            processGroupPathCopy.setLastUpdateUser(username);
            processGroupPathCopy.setEnableFlag(true);
            processGroupPathCopy.setFrom(processGroupPath.getFrom());
            processGroupPathCopy.setTo(processGroupPath.getTo());
            processGroupPathCopy.setInport(processGroupPath.getInport());
            processGroupPathCopy.setOutport(processGroupPath.getOutport());
            processGroupPathCopy.setPageId(processGroupPath.getPageId());
            processGroupPathCopy.setProcessGroup(processGroupCopy);
        }
        return processGroupPathCopy;
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
        ProcessGroupVo parentsProcessGroupVo = processGroupPoToVo(parentsProcessGroup);
        processGroupVo.setProcessGroupVo(parentsProcessGroupVo);

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
            for (ProcessGroup processGroup_i : processGroupList) {
                ProcessGroupVo processGroupVo_I_Copy = processGroupPoToVo(processGroup_i);
                if (null == processGroupVo_I_Copy) {
                    continue;
                }
                processGroupVoList.add(processGroupVo_I_Copy);
            }
            processGroupVo.setProcessGroupVoList(processGroupVoList);
        }
        return processGroupVo;
    }
}
