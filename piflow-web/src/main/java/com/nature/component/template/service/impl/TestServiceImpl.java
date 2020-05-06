package com.nature.component.template.service.impl;

import com.nature.base.util.*;
import com.nature.common.Eunm.TemplateType;
import com.nature.common.constant.SysParamsCache;
import com.nature.component.flow.model.Flow;
import com.nature.component.flow.model.Paths;
import com.nature.component.flow.model.Stops;
import com.nature.component.mxGraph.model.MxCell;
import com.nature.component.mxGraph.model.MxGraphModel;
import com.nature.component.mxGraph.utils.MxCellUtils;
import com.nature.component.mxGraph.utils.MxGraphModelUtils;
import com.nature.component.mxGraph.vo.MxGraphModelVo;
import com.nature.component.template.model.FlowTemplate;
import com.nature.component.template.service.ITestService;
import com.nature.component.template.utils.FlowTemplateUtils;
import com.nature.component.template.vo.FlowTemplateVo;
import com.nature.domain.flow.FlowDomain;
import com.nature.domain.template.FlowTemplateDomain;
import com.nature.mapper.flow.FlowMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TestServiceImpl implements ITestService {


    Logger logger = LoggerUtil.getLogger();

    @Resource
    private FlowMapper flowMapper;

    @Resource
    private FlowTemplateDomain flowTemplateDomain;

    @Resource
    private FlowDomain flowDomain;

    @Transactional
    @Override
    public String addFlowTemplate(String name, String loadId, String value) {
        String username = SessionUserUtil.getCurrentUsername();
        MxGraphModelVo mxGraphModelVo = null;
        if (StringUtils.isAnyEmpty(name, loadId)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Some incoming parameters are empty");
        }
        Flow flowById = flowDomain.getFlowById(loadId);
        if (null == flowById) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Flow information is empty");
        }

        if (StringUtils.isBlank(value)) {
            MxGraphModel mxGraphModel = flowById.getMxGraphModel();
            if (null != mxGraphModel) {
                mxGraphModelVo = FlowXmlUtils.mxGraphModelPoToVo(mxGraphModel);
                // Convert the query mxGraphModelVo to XML
                value = FlowXmlUtils.mxGraphModelToXml(mxGraphModelVo);
            }
        }
        //Concatenate the XML according to the flowById
        String flowAndStopInfoToXml = FlowXmlUtils.flowAndStopInfoToXml(flowById, value);
        logger.info(flowAndStopInfoToXml);
        //XML to file and save to the specified directory
        String path = FileUtils.createXml(flowAndStopInfoToXml, name, SysParamsCache.XML_PATH);
        FlowTemplate flowTemplate = FlowTemplateUtils.newFlowTemplateNoId(username);
        flowTemplate.setId(SqlUtils.getUUID32());
        flowTemplate.setDescription(flowById.getDescription());
        flowTemplate.setTemplateType(TemplateType.TASK);
        flowTemplate.setName(name);
        flowTemplate.setSourceFlowName(flowById.getName());

        flowTemplate.setPath(path);
        flowTemplateDomain.saveOrUpdate(flowTemplate);
        return ReturnMapUtils.setSucceededMsgRtnJsonStr("save FlowTemplate success");
    }

    @Override
    public List<FlowTemplate> findFlowTemPlateList() {
        return flowTemplateDomain.getFlowTemplateList();
    }

    @Override
    public int deleteFlowTemplate(String id) {
        return flowTemplateDomain.updateEnableFlagById(id, false);
    }

    @Override
    public FlowTemplate queryFlowTemplate(String id) {
        return flowTemplateDomain.getFlowTemplateById(id);
    }

    @Override
    public String getFlowTemplateListPage(Integer offset, Integer limit, String param) {
        Map<String, Object> rtnMap = new HashMap<>();
        if (null != offset && null != limit) {
            Page<FlowTemplate> flowTemplateListPage = flowTemplateDomain.getFlowTemplateListPage(offset - 1, limit, param);
            List<FlowTemplate> content = flowTemplateListPage.getContent();
            List<FlowTemplateVo> flowTemplateVoList = new ArrayList<>();
            if (null != content) {
                FlowTemplateVo flowTemplateVo;
                for (FlowTemplate flowTemplate : content) {
                    flowTemplateVo = new FlowTemplateVo();
                    BeanUtils.copyProperties(flowTemplate, flowTemplateVo);
                    flowTemplateVoList.add(flowTemplateVo);
                }
            }
            rtnMap.put("iTotalDisplayRecords", flowTemplateListPage.getTotalElements());
            rtnMap.put("iTotalRecords", flowTemplateListPage.getTotalElements());
            rtnMap.put("pageData", flowTemplateVoList);//Data collection
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    @Override
    public String loadFlowTemplateToFlow(String flowId, String flowTemplateId) {
        String currentUsername = SessionUserUtil.getCurrentUsername();
        if (StringUtils.isBlank(currentUsername)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Illegal user, Load failed");
        }
        if (null == flowId) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("FlowId is empty, loading FlowTemplate failed");
        }
        if (null == flowTemplateId) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("FlowTemplateId is empty, loading FlowTemplate failed");
        }
        Flow flowById = flowDomain.getFlowById(flowId);
        if (null == flowById) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Flow is empty, loading FlowTemplate failed");
        }
        FlowTemplate flowTemplate = flowTemplateDomain.getFlowTemplateById(flowTemplateId);
        if (null == flowTemplate) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("FlowTemplate is empty, loading FlowTemplate failed");
        }
        //Read the xml file according to the saved file path and return
        String xmlFileToStr = FileUtils.XmlFileToStrByAbsolutePath(flowTemplate.getPath());
        if (StringUtils.isBlank(xmlFileToStr)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("The xml file failed to read and the FlowTemplate failed to be loaded.");
        }
        // Get the maximum pageId in stop
        String maxStopPageId = flowMapper.getMaxStopPageId(flowId);
        // Get the current flow containing all stop names
        String[] stopNamesByFlowId = flowMapper.getStopNamesByFlowId(flowId);
        Map<String, Object> flowTemplateXmlToFlowRtnMap = FlowXmlUtils.flowTemplateXmlToFlow(xmlFileToStr, currentUsername, maxStopPageId, null, stopNamesByFlowId);
        if (200 != (Integer) flowTemplateXmlToFlowRtnMap.get("code")) {
            return JsonUtils.toJsonNoException(flowTemplateXmlToFlowRtnMap);
        }
        Flow flowTemplateXmlToFlow = (Flow) flowTemplateXmlToFlowRtnMap.get("flow");
        if (null == flowTemplateXmlToFlow) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Conversion failure");
        }
        // Added processing drawing board data
        // Fetch the drawing board data to be added
        MxGraphModel mxGraphModelXml = flowTemplateXmlToFlow.getMxGraphModel();
        if (null != mxGraphModelXml) {
            MxGraphModel mxGraphModel = flowById.getMxGraphModel();
            if (null == mxGraphModel) {
                mxGraphModel = MxGraphModelUtils.setMxGraphModelBasicInformation(null, false, currentUsername);
            } else {
                // Update basic information
                mxGraphModel = MxGraphModelUtils.updateMxGraphModelBasicInformation(mxGraphModel, currentUsername);
            }
            mxGraphModel.setFlow(flowById);
            // link flow
            mxGraphModel.setFlow(flowById);

            List<MxCell> mxCellList = null;
            if (null == mxGraphModel.getRoot() || mxGraphModel.getRoot().size() <= 1) {
                mxCellList = MxCellUtils.initMxCell(currentUsername, mxGraphModel);
            }
            if (null == mxCellList) {
                mxCellList = new ArrayList<>();
            }
            List<MxCell> rootXml = mxGraphModelXml.getRoot();
            if (null != rootXml && rootXml.size() > 0) {
                for (MxCell mxCell : rootXml) {
                    mxCell.setMxGraphModel(mxGraphModel);
                    mxCellList.add(mxCell);
                }
            }
            mxGraphModel.setRoot(mxCellList);
            flowById.setMxGraphModel(mxGraphModel);
        }
        // Added processing flow data
        List<Stops> stopsListXml = flowTemplateXmlToFlow.getStopsList();
        if (null != stopsListXml && stopsListXml.size() > 0) {
            List<Stops> stopsList = flowById.getStopsList();
            if (null == stopsList) {
                stopsList = new ArrayList<>();
            }
            for (Stops stops : stopsListXml) {
                stops.setFlow(flowById);
                stopsList.add(stops);
            }
            flowById.setStopsList(stopsList);
        }
        List<Paths> pathsListXml = flowTemplateXmlToFlow.getPathsList();
        if (null != pathsListXml && pathsListXml.size() > 0) {
            List<Paths> pathsList = flowById.getPathsList();
            if (null == pathsList) {
                pathsList = new ArrayList<>();
            }
            for (Paths paths : pathsListXml) {
                paths.setFlow(flowById);
                pathsList.add(paths);
            }
            flowById.setPathsList(pathsList);
        }
        // save
        flowDomain.saveOrUpdate(flowById);
        return ReturnMapUtils.setSucceededMsgRtnJsonStr("Successfully loaded FlowTemplate");
    }

    public String uploadFlowTemplate(MultipartFile file) {
        String username = SessionUserUtil.getCurrentUsername();
        if (file.isEmpty()) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Upload failed, please try again later");
        }
        Map<String, Object> uploadMap = FileUtils.uploadRtnMap(file, SysParamsCache.XML_PATH);
        if (null == uploadMap || uploadMap.isEmpty()) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Upload failed, please try again later");
        }
        Integer code = (Integer) uploadMap.get("code");
        if (500 == code) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("failed to upload file");
        }
        String fileName = (String) uploadMap.get("fileName");
        String path = (String) uploadMap.get("path");
        FlowTemplate flowTemplate = FlowTemplateUtils.newFlowTemplateNoId(username);
        flowTemplate.setId(SqlUtils.getUUID32());
        flowTemplate.setName(fileName);
        flowTemplate.setPath(path);
        flowTemplateDomain.saveOrUpdate(flowTemplate);
        return ReturnMapUtils.setSucceededMsgRtnJsonStr("FlowTemplate upload succeeded");
    }
}
