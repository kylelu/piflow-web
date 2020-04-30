package com.nature.controller;

import com.nature.base.util.FlowXmlUtils;
import com.nature.base.util.LoggerUtil;
import com.nature.base.util.MxGraphUtils;
import com.nature.base.util.SessionUserUtil;
import com.nature.base.vo.UserVo;
import com.nature.common.Eunm.DrawingBoardType;
import com.nature.common.Eunm.ProcessState;
import com.nature.component.flow.model.Flow;
import com.nature.component.flow.model.FlowGroup;
import com.nature.component.flow.service.IFlowGroupService;
import com.nature.component.flow.service.IFlowService;
import com.nature.component.flow.vo.StopGroupVo;
import com.nature.component.group.service.IStopGroupService;
import com.nature.component.mxGraph.model.MxGraphModel;
import com.nature.component.mxGraph.service.IMxGraphModelService;
import com.nature.component.mxGraph.service.IMxNodeImageService;
import com.nature.component.mxGraph.vo.MxGraphModelVo;
import com.nature.component.process.service.IProcessGroupService;
import com.nature.component.process.service.IProcessService;
import com.nature.component.process.vo.ProcessGroupVo;
import com.nature.component.process.vo.ProcessStopVo;
import com.nature.component.process.vo.ProcessVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * grapheditorctrl
 */
@Controller
@RequestMapping("/mxGraph")
public class MxGraphCtrl {
    /**
     * Introducing logs, note that they are all packaged under "org.slf4j"
     */
    Logger logger = LoggerUtil.getLogger();

    @Resource
    private IFlowService flowServiceImpl;

    @Resource
    private IFlowGroupService flowGroupServiceImpl;

    @Resource
    private IStopGroupService stopGroupServiceImpl;

    @Resource
    private IMxGraphModelService mxGraphModelServiceImpl;

    @Resource
    private IMxNodeImageService mxNodeImageServiceImpl;

    @Resource
    private IProcessService processServiceImpl;

    @Resource
    private IProcessGroupService processGroupServiceImpl;


    /**
     * Enter the front page of the drawing board
     *
     * @param request
     * @param model
     * @param drawingBoardType
     * @return
     */
    @RequestMapping("/drawingBoard")
    public String drawingBoard(HttpServletRequest request, Model model, DrawingBoardType drawingBoardType, String processType) {
        String load = request.getParameter("load");
        //set parentAccessPath
        String parentAccessPath = request.getParameter("parentAccessPath");
        model.addAttribute("parentAccessPath", parentAccessPath);
        // set current user
        UserVo user = SessionUserUtil.getCurrentUser();
        model.addAttribute("currentUser", user);
        //set drawingBoardType
        model.addAttribute("drawingBoardType", drawingBoardType);

        String pagePath = "errorPage";
        // Determine whether there is an'id'('load') of'Flow', and if there is, load it, otherwise generate'UUID' to return to the return page.
        if (StringUtils.isBlank(load)) {
            return "errorPage";
        }
        switch (drawingBoardType) {
            case GROUP: {
                Model groupHandleModel = groupHandle(model, load);
                if (null != groupHandleModel) {
                    model = groupHandleModel;
                    pagePath = "mxGraph/index";
                }
                break;
            }
            case TASK: {
                Model taskHandleModel = taskHandle(model, load);
                if (null != taskHandleModel) {
                    model = taskHandleModel;
                    pagePath = "mxGraph/index";
                }
                break;
            }
            case PROCESS: {
                Model processHandleModel = processHandle(model, load, processType);
                if (null != processHandleModel) {
                    model = processHandleModel;
                    pagePath = "mxGraph/index";
                }
                break;
            }
        }
        return pagePath;
    }

    private Model processHandle(Model model, String load, String processType) {
        if (StringUtils.isBlank(load)) {
            return null;
        }
        if (null == model) {
            return null;
        }
        ProcessGroupVo parentsProcessGroupVo = null;
        MxGraphModelVo mxGraphModelVo = null;
        List<Map<String, String>> nodePageIdAndStates = new ArrayList<>();
        ProcessState processState = ProcessState.INIT;
        if ("PROCESS".equals(processType)) {
            ProcessVo processVo = processServiceImpl.getProcessById(load);
            if (null == processVo) {
                return null;
            }
            // processStopVoList
            List<ProcessStopVo> processStopVoList = processVo.getProcessStopVoList();
            if (null != processStopVoList && processStopVoList.size() > 0) {
                Map<String, String> stopNode;
                for (ProcessStopVo processStopVo_i : processStopVoList) {
                    if (null == processStopVo_i) {
                        continue;
                    }
                    stopNode = new HashMap<>();
                    stopNode.put("pageId", processStopVo_i.getPageId());
                    stopNode.put("state", processStopVo_i.getState());
                    nodePageIdAndStates.add(stopNode);
                }
                model.addAttribute("processStopVoListInit", processStopVoList);
                model.addAttribute("percentage", (null != processVo.getProgress() ? processVo.getProgress() : 0.00));
                model.addAttribute("appId", processVo.getAppId());
            }
            parentsProcessGroupVo = processVo.getProcessGroupVo();
            if (null != parentsProcessGroupVo) {
                model.addAttribute("processGroupId", parentsProcessGroupVo.getId());
            }
            mxGraphModelVo = processVo.getMxGraphModelVo();
            processState = processVo.getState();
            model.addAttribute("processType", "TASK");
            model.addAttribute("processId", load);
            model.addAttribute("parentProcessId", processVo.getParentProcessId());
            model.addAttribute("pID", processVo.getProcessId());
            model.addAttribute("processVo", processVo);
        } else {
            ProcessGroupVo processGroupVo = processGroupServiceImpl.getProcessGroupVoAllById(load);
            if (null == processGroupVo) {
                return null;
            }
            parentsProcessGroupVo = processGroupVo.getProcessGroupVo();
            mxGraphModelVo = processGroupVo.getMxGraphModelVo();
            model.addAttribute("processType", "GROUP");

            // processGroupVoList
            List<ProcessGroupVo> processGroupVoList = processGroupVo.getProcessGroupVoList();
            if (null != processGroupVoList && processGroupVoList.size() > 0) {
                Map<String, String> processGroupNode;
                for (ProcessGroupVo processGroupVo_i : processGroupVoList) {
                    if (null == processGroupVo_i) {
                        continue;
                    }
                    processGroupNode = new HashMap<>();
                    processGroupNode.put("pageId", processGroupVo_i.getPageId());
                    processGroupNode.put("state", processGroupVo_i.getState().getText());
                    nodePageIdAndStates.add(processGroupNode);
                }
                model.addAttribute("processGroupVoListInit", processGroupVoList);
            }
            // processVoList
            List<ProcessVo> processVoList = processGroupVo.getProcessVoList();
            Map<String, String> processNode;
            if (null != processVoList && processVoList.size() > 0) {
                for (ProcessVo process_i : processVoList) {
                    if (null == process_i) {
                        continue;
                    }
                    String process_i_stateStr = (null != process_i.getState() ? process_i.getState().getText() : "INIT");
                    processNode = new HashMap<>();
                    processNode.put("pageId", process_i.getPageId());
                    processNode.put("state", process_i_stateStr);
                    nodePageIdAndStates.add(processNode);
                }
                model.addAttribute("processVoListInit", processVoList);
            }
            processState = processGroupVo.getState();
            model.addAttribute("parentProcessId", processGroupVo.getParentProcessId());
            model.addAttribute("percentage", (null != processGroupVo.getProgress() ? processGroupVo.getProgress() : 0.00));
            model.addAttribute("appId", processGroupVo.getAppId());
            model.addAttribute("pID", processGroupVo.getProcessId());
            model.addAttribute("processGroupVo", processGroupVo);
            model.addAttribute("processGroupId", load);
        }
        if (null != processState) {
            model.addAttribute("processState", processState.name());
        }
        if (null != parentsProcessGroupVo) {
            model.addAttribute("parentsId", parentsProcessGroupVo.getId());
        }
        String loadXml = FlowXmlUtils.mxGraphModelToXml(mxGraphModelVo);
        model.addAttribute("xmlDate", loadXml);
        model.addAttribute("load", load);
        model.addAttribute("nodeArr", nodePageIdAndStates);
        return model;
    }

    private Model groupHandle(Model model, String load) {
        if (StringUtils.isBlank(load)) {
            return null;
        }
        if (null == model) {
            return null;
        }
        // Query by loading'id'
        FlowGroup flowGroupById = flowGroupServiceImpl.getFlowGroupById(load);
        if (null == flowGroupById) {
            return null;
        }
        FlowGroup flowGroup = flowGroupById.getFlowGroup();
        if (null != flowGroup) {
            model.addAttribute("parentsId", flowGroup.getId());
        }
        String maxStopPageId = flowServiceImpl.getMaxFlowPageIdByFlowGroupId(load);
        //'maxStopPageId'defaults to 2 if it's empty, otherwise'maxStopPageId'+1
        maxStopPageId = StringUtils.isBlank(maxStopPageId) ? "2" : (Integer.parseInt(maxStopPageId) + 1) + "";
        model.addAttribute("maxStopPageId", maxStopPageId);
        MxGraphModelVo mxGraphModelVo = null;
        MxGraphModel mxGraphModel = flowGroupById.getMxGraphModel();
        mxGraphModelVo = FlowXmlUtils.mxGraphModelPoToVo(mxGraphModel);
        // Change the query'mxGraphModelVo'to'XML'
        String loadXml = MxGraphUtils.mxGraphModelToMxGraphXml(mxGraphModelVo);
        model.addAttribute("xmlDate", loadXml);
        model.addAttribute("load", load);
        model.addAttribute("isExample", (null == flowGroupById.getIsExample() ? false : flowGroupById.getIsExample()));
        return model;
    }

    private Model taskHandle(Model model, String load) {
        if (null == model) {
            return null;
        }
        // Query by loading'id'
        Flow flowById = flowServiceImpl.getFlowById(load);
        if (null == flowById) {
            return null;
        }
        if (null != flowById.getFlowGroup()) {
            String parentsId = flowById.getFlowGroup().getId();
            model.addAttribute("parentsId", parentsId);
        }
        // Group on the left and'stops'
        List<StopGroupVo> groupsVoList = stopGroupServiceImpl.getStopGroupAll();
        model.addAttribute("groupsVoList", groupsVoList);
        String maxStopPageId = flowServiceImpl.getMaxStopPageId(load);
        //'maxStopPageId'defaults to 2 if it's empty, otherwise'maxStopPageId'+1
        maxStopPageId = StringUtils.isBlank(maxStopPageId) ? "2" : (Integer.parseInt(maxStopPageId) + 1) + "";
        model.addAttribute("maxStopPageId", maxStopPageId);
        MxGraphModelVo mxGraphModelVo = null;
        MxGraphModel mxGraphModel = flowById.getMxGraphModel();
        mxGraphModelVo = FlowXmlUtils.mxGraphModelPoToVo(mxGraphModel);
        // Change the query'mxGraphModelVo'to'XML'
        String loadXml = MxGraphUtils.mxGraphModelToMxGraphXml(mxGraphModelVo);
        model.addAttribute("xmlDate", loadXml);
        model.addAttribute("load", load);
        model.addAttribute("isExample", (null == flowById.getIsExample() ? false : flowById.getIsExample()));
        return model;
    }

    /**
     * save data
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/saveDataForTask")
    @ResponseBody
    public String saveDataForTask(HttpServletRequest request, Model model) {
        String imageXML = request.getParameter("imageXML");
        String loadId = request.getParameter("load");
        String operType = request.getParameter("operType");
        return mxGraphModelServiceImpl.saveDataForTask(imageXML, loadId, operType);
    }

    /**
     * save data
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/saveDataForGroup")
    @ResponseBody
    public String saveDataForGroup(HttpServletRequest request, Model model) {
        String imageXML = request.getParameter("imageXML");
        String loadId = request.getParameter("load");
        String operType = request.getParameter("operType");
        return mxGraphModelServiceImpl.saveDataForGroup(imageXML, loadId, operType, true);
    }

    @RequestMapping("/uploadNodeImage")
    @ResponseBody
    public String uploadNodeImage(@RequestParam("file") MultipartFile file, String imageType) {
        return mxNodeImageServiceImpl.uploadNodeImage(file, imageType);
    }

    @RequestMapping("/nodeImageList")
    @ResponseBody
    public String nodeImageList(String imageType) {
        return mxNodeImageServiceImpl.getMxNodeImageList(imageType);
    }

    @RequestMapping("/groupRightRun")
    @ResponseBody
    public String groupRightRun(String pId, String nodeId, String nodeType) {
        return flowGroupServiceImpl.rightRun(pId, nodeId, nodeType);
    }

}
