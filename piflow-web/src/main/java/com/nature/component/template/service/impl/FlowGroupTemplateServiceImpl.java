package com.nature.component.template.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nature.base.util.*;
import com.nature.base.vo.UserVo;
import com.nature.common.constant.SysParamsCache;
import com.nature.component.flow.model.*;
import com.nature.component.mxGraph.model.MxCell;
import com.nature.component.mxGraph.model.MxGeometry;
import com.nature.component.mxGraph.model.MxGraphModel;
import com.nature.component.mxGraph.utils.MxCellUtils;
import com.nature.component.template.model.FlowGroupTemplate;
import com.nature.component.template.service.IFlowGroupTemplateService;
import com.nature.component.template.vo.FlowGroupTemplateVo;
import com.nature.domain.flow.*;
import com.nature.domain.mxGraph.MxCellDomain;
import com.nature.domain.mxGraph.MxGeometryDomain;
import com.nature.domain.mxGraph.MxGraphModelDomain;
import com.nature.mapper.flow.FlowGroupMapper;
import com.nature.mapper.flow.FlowMapper;
import com.nature.mapper.template.FlowGroupTemplateMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FlowGroupTemplateServiceImpl implements IFlowGroupTemplateService {

    Logger logger = LoggerUtil.getLogger();

    @Resource
    private FlowGroupTemplateMapper flowGroupTemplateMapper;

    @Resource
    private FlowGroupMapper flowGroupMapper;

    @Resource
    private FlowMapper flowMapper;

    @Resource
    private FlowGroupTemplateDomain flowGroupTemplateDomain;

    @Resource
    private FlowGroupDomain flowGroupDomain;

    @Resource
    private MxGraphModelDomain mxGraphModelDomain;

    @Resource
    private MxCellDomain mxCellDomain;

    @Resource
    private MxGeometryDomain mxGeometryDomain;

    @Resource
    private FlowGroupPathsDomain flowGroupPathsDomain;

    @Resource
    private FlowDomain flowDomain;

    @Resource
    private StopsDomain stopsDomain;

    @Resource
    private PropertyDomain propertyDomain;

    @Resource
    private PathsDomain pathsDomain;


    /**
     * add FlowGroupTemplate
     *
     * @param name
     * @param loadId
     * @param value
     * @return
     */
    @Override
    @Transactional
    public String addFlowGroupTemplate(String name, String loadId, String value) {
        UserVo user = SessionUserUtil.getCurrentUser();
        String username = (null != user) ? user.getUsername() : "-1";
        Map<String, Object> rtnMap = new HashMap<>();
        rtnMap.put("code", 500);
        if (StringUtils.isAnyEmpty(name, loadId)) {
            rtnMap.put("errorMsg", "Some parameters passed in are empty");
            logger.info("Some parameters passed in are empty");
            return JsonUtils.toJsonNoException(rtnMap);
        }
        FlowGroup flowGroupById = flowGroupDomain.getFlowGroupById(loadId);
        if (null != flowGroupById) {
            //Splicing XML according to flowById
            String flowGroupXmlStr = FlowXmlUtils.flowGroupToXmlStr(flowGroupById);
            logger.info(flowGroupXmlStr);

            FlowGroupTemplate flowGroupTemplate = new FlowGroupTemplate();
            flowGroupTemplate.setId(SqlUtils.getUUID32());
            flowGroupTemplate.setCrtDttm(new Date());
            flowGroupTemplate.setCrtUser(username);
            flowGroupTemplate.setEnableFlag(true);
            flowGroupTemplate.setLastUpdateUser(username);
            flowGroupTemplate.setLastUpdateDttm(new Date());
            flowGroupTemplate.setName(name);
            //XML to file and save to specified directory
            String path = FileUtils.createXml(flowGroupXmlStr, name, ".xml", SysParamsCache.XML_PATH);
            flowGroupTemplate.setPath(path);
            flowGroupTemplateDomain.saveOrUpdate(flowGroupTemplate);
            rtnMap.put("code", 200);
            rtnMap.put("errorMsg", "save template success");
            return JsonUtils.toJsonNoException(rtnMap);
        } else {
            rtnMap.put("errorMsg", "Flow information is empty");
            logger.info("Flow information is empty,loadId：" + loadId);
            return JsonUtils.toJsonNoException(rtnMap);
        }
    }

    @Override
    public String getFlowGroupTemplateListPage(Integer offset, Integer limit, String param) {
        Map<String, Object> rtnMap = new HashMap<String, Object>();
        if (null != offset && null != limit) {
            Page<FlowGroupTemplateVo> page = PageHelper.startPage(offset, limit);
            flowGroupTemplateMapper.getFlowGroupTemplateVoListPage(param);
            rtnMap = PageHelperUtils.setDataTableParam(page, rtnMap);
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Delete the template based on id
     *
     * @param id
     * @return
     */
    @Override
    public int deleteFlowGroupTemplate(String id) {
        int deleteTemplate = 0;
        if (StringUtils.isNoneBlank(id)) {
            deleteTemplate = flowGroupTemplateDomain.updateEnableFlagById(id, false);
        }
        return deleteTemplate;
    }

    /**
     * Download template
     *
     * @param flowGroupTemplateId
     */
    @Override
    public void templateDownload(HttpServletResponse response, String flowGroupTemplateId) {
        FlowGroupTemplateVo flowGroupTemplateVo = flowGroupTemplateMapper.getFlowGroupTemplateVoById(flowGroupTemplateId);
        if (null == flowGroupTemplateVo) {
            logger.info("Template is empty,Download template failed");
        } else {
            String fileName = flowGroupTemplateVo.getName() + ".xml".toString(); // The default save name of the file
            String filePath = flowGroupTemplateVo.getPath();// File storage path
            FileUtils.downloadFileResponse(response, fileName, filePath);
        }

    }

    /**
     * Upload xml file and save flowGroupTemplate
     *
     * @param file
     * @return
     */
    @Override
    public String uploadXmlFile(MultipartFile file) {
        String username = SessionUserUtil.getCurrentUsername();
        if (file.isEmpty()) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Upload failed, please try again later");
        }
        Map<String, Object> uploadMap = FileUtils.uploadRtnMap(file, SysParamsCache.IMAGES_PATH);
        if (null == uploadMap ||uploadMap.isEmpty()) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Upload failed, please try again later");
        }
        Integer code = (Integer) uploadMap.get("code");
        if (500 == code) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("failed to upload file");
        }
        String name = (String) uploadMap.get("fileName");
        String path = (String) uploadMap.get("path");
        FlowGroupTemplate flowGroupTemplate = new FlowGroupTemplate();
        flowGroupTemplate.setId(SqlUtils.getUUID32());
        flowGroupTemplate.setCrtDttm(new Date());
        flowGroupTemplate.setCrtUser(username);
        flowGroupTemplate.setEnableFlag(true);
        flowGroupTemplate.setLastUpdateUser(username);
        flowGroupTemplate.setLastUpdateDttm(new Date());

        //File name prefix
        String prefix = name.substring(0, name.length() - 4);
        //Suffix .xml
        String Suffix = name.substring(name.length() - 4);
        //SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmSSSS");
        //Date nowDate = new Date();
        //String fileName = sdf.format(nowDate);
        //Add timestamp
        //String uploadFileName = prefix + "-" + fileName;
        String uploadFileName = prefix;
        flowGroupTemplate.setName(uploadFileName + Suffix);
        flowGroupTemplate.setPath(path);
        //Read the XML file according to the saved file path and return the XML string
        String xmlFileToStr = FileUtils.XmlFileToStrByAbsolutePath(flowGroupTemplate.getPath());
        if (StringUtils.isBlank(xmlFileToStr)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("XML file read failed, upload template failed");
        }
        flowGroupTemplateDomain.saveOrUpdate(flowGroupTemplate);
        return ReturnMapUtils.setSucceededMsgRtnJsonStr("successful template upload");
    }

    @Override
    public String flowGroupTemplateAllSelect() {
        Map<String, Object> rtnMap = new HashMap<String, Object>();
        rtnMap.put("code", 500);
        List<FlowGroupTemplateVo> findTemPlateList = flowGroupTemplateMapper.getFlowGroupTemplateVoListPage(null);
        if (null != findTemPlateList && findTemPlateList.size() > 0) {
            rtnMap.put("code", 200);
            rtnMap.put("temPlateList", findTemPlateList);
        } else {
            rtnMap.put("errorMsg", "Query result is empty");
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    @Override
    public String loadFlowGroupTemplate(String templateId, String loadId) {
        Map<String, Object> rtnMap = new HashMap<>();
        rtnMap.put("code", 500);
        UserVo currentUser = SessionUserUtil.getCurrentUser();
        if (null == currentUser) {
            logger.info("Illegal user, Load failed");
            rtnMap.put("errorMsg", "Illegal user, Load failed");
            return JsonUtils.toJsonNoException(rtnMap);
        }
        FlowGroup flowGroupById = flowGroupDomain.getFlowGroupById(loadId);
        if (null == flowGroupById) {
            logger.info("Template is empty and failed to load the template");
            rtnMap.put("errorMsg", "Load failed, please try again");
            return JsonUtils.toJsonNoException(rtnMap);
        }
        FlowGroupTemplateVo flowGroupTemplateVo = flowGroupTemplateMapper.getFlowGroupTemplateVoById(templateId);
        if (null == flowGroupTemplateVo) {
            logger.info("Template is empty and failed to load the template");
            rtnMap.put("errorMsg", "Load failed, please try again");
            return JsonUtils.toJsonNoException(rtnMap);
        }
        //The XML file is read and returned according to the saved file path
        String xmlFileToStr = FileUtils.XmlFileToStrByAbsolutePath(flowGroupTemplateVo.getPath());
        if (StringUtils.isBlank(xmlFileToStr)) {
            logger.info("XML file read failed, loading template failed");
            rtnMap.put("errorMsg", "XML file read failed, loading template failed");
            return JsonUtils.toJsonNoException(rtnMap);
        }
        // Get the maximum value of pageid in stop
        String maxStopPageIdByFlowGroupId = flowMapper.getMaxFlowPageIdByFlowGroupId(loadId);
        maxStopPageIdByFlowGroupId = StringUtils.isNotBlank(maxStopPageIdByFlowGroupId) ? maxStopPageIdByFlowGroupId : "0";
        int maxPageId = Integer.parseInt(maxStopPageIdByFlowGroupId);
        String username = currentUser.getUsername();
        // Get the current flowGroup containing all flow names
        String[] flowNamesByFlowGroupId = flowMapper.getFlowNamesByFlowGroupId(loadId);
        Map<String, Object> XmlStrToFlowGroupRtnMap = FlowXmlUtils.XmlStrToFlowGroup(xmlFileToStr, maxPageId, username, flowNamesByFlowGroupId, false);
        if (200 != (Integer) XmlStrToFlowGroupRtnMap.get("code")) {
            return JsonUtils.toJsonNoException(XmlStrToFlowGroupRtnMap);
        }
        FlowGroup flowGroupXml = (FlowGroup) XmlStrToFlowGroupRtnMap.get("flowGroup");
        if (null == flowGroupXml) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Conversion failure");
        }
        // Added processing artboard data
        // Fetch the artboard data to be added
        MxGraphModel mxGraphModelXml = flowGroupXml.getMxGraphModel();
        if (null != mxGraphModelXml) {
            MxGraphModel mxGraphModel = flowGroupById.getMxGraphModel();
            if (null == mxGraphModel) {
                mxGraphModel = new MxGraphModel();
                mxGraphModel.setFlowGroup(flowGroupById);
                mxGraphModel.setId(SqlUtils.getUUID32());
                mxGraphModel.setCrtDttm(new Date());
                mxGraphModel.setCrtUser(username);
                mxGraphModel.setLastUpdateDttm(new Date());
                mxGraphModel.setLastUpdateUser(username);
                mxGraphModel.setEnableFlag(true);
                mxGraphModel = mxGraphModelDomain.saveOrUpdate(mxGraphModel);
            }
            List<MxCell> rootXml = mxGraphModelXml.getRoot();
            if (null == rootXml || rootXml.size() <= 1) {
                List<MxCell> mxCellList = MxCellUtils.initMxCell(username, mxGraphModel);
                if (null != mxCellList) {
                    mxCellDomain.saveOrUpdate(mxCellList);
                }
            }
            if (null != rootXml && rootXml.size() > 0) {
                for (MxCell mxCell : rootXml) {
                    if (null != mxCell) {
                        // get mxGeometry
                        MxGeometry mxGeometry = mxCell.getMxGeometry();
                        //Associated sketchpad
                        mxCell.setMxGraphModel(mxGraphModel);
                        mxCell.setMxGeometry(null);
                        //new
                        mxCell = mxCellDomain.saveOrUpdate(mxCell);
                        if (null != mxGeometry) {
                            mxGeometry.setMxCell(mxCell);
                            // new
                            mxGeometryDomain.saveOrUpdate(mxGeometry);
                        }

                    }
                }
            }
        }
        // Added processing flow data
        List<Flow> flowListXml = flowGroupXml.getFlowList();
        if (null != flowListXml && flowListXml.size() > 0) {
            for (Flow flowXml : flowListXml) {
                if (null != flowXml) {
                    MxGraphModel flowMxGraphModelXml = flowXml.getMxGraphModel();

                    flowXml.setFlowGroup(flowGroupById);
                    flowXml.setMxGraphModel(null);
                    flowXml = flowDomain.saveOrUpdate(flowXml);
                    if (null != flowMxGraphModelXml) {
                        List<MxCell> root = flowMxGraphModelXml.getRoot();
                        flowMxGraphModelXml.setRoot(null);
                        flowMxGraphModelXml.setFlow(flowXml);
                        flowMxGraphModelXml = mxGraphModelDomain.saveOrUpdate(flowMxGraphModelXml);
                        for (MxCell mxCell : root) {
                            MxGeometry flowMxGeometryXml = mxCell.getMxGeometry();
                            mxCell.setMxGeometry(null);
                            mxCell.setMxGraphModel(flowMxGraphModelXml);
                            mxCell = mxCellDomain.saveOrUpdate(mxCell);
                            if (null != flowMxGeometryXml) {
                                flowMxGeometryXml.setMxCell(mxCell);
                                mxGeometryDomain.saveOrUpdate(flowMxGeometryXml);
                            }
                        }
                    }
                }
            }
        }
        // Added processing of flowGroupPath data
        List<FlowGroupPaths> flowGroupPathsListXml = flowGroupXml.getFlowGroupPathsList();
        if (null != flowGroupPathsListXml && flowGroupPathsListXml.size() > 0) {
            for (FlowGroupPaths flowGroupPathsXml : flowGroupPathsListXml) {
                flowGroupXml.setFlowGroupPathsList(null);
                flowGroupPathsXml.setFlowGroup(flowGroupById);
            }
            flowGroupPathsDomain.saveOrUpdate(flowGroupPathsListXml);
        }

        // Added processing of flowGroupPath data
        List<FlowGroup> flowGroupListXml = flowGroupXml.getFlowGroupList();
        if (null != flowGroupListXml && flowGroupListXml.size() > 0) {
            for (FlowGroup flowGroupListXml_i : flowGroupListXml) {
                flowGroupXml.setFlowGroupList(null);
                flowGroupListXml_i.setFlowGroup(flowGroupById);
            }
            flowGroupDomain.saveOrUpdate(flowGroupListXml);
        }
        rtnMap.put("code", 200);
        rtnMap.put("errorMsg", "success");
        return JsonUtils.toJsonNoException(rtnMap);
    }

}
