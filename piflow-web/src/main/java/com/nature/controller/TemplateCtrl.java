package com.nature.controller;

import com.alibaba.fastjson.JSON;
import com.nature.base.util.*;
import com.nature.base.vo.UserVo;
import com.nature.common.constant.SysParamsCache;
import com.nature.component.flow.model.Flow;
import com.nature.component.flow.model.Stops;
import com.nature.component.flow.service.IFlowService;
import com.nature.component.mxGraph.model.MxGraphModel;
import com.nature.component.mxGraph.vo.MxGraphModelVo;
import com.nature.component.template.model.StopTemplateModel;
import com.nature.component.template.model.Template;
import com.nature.component.template.service.IFlowAndStopsTemplateVoService;
import com.nature.component.template.service.ITemplateService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * template的ctrl
 */
@RestController
@RequestMapping("/template")
public class TemplateCtrl {

    @Resource
    private IFlowService iFlowServiceImpl;

    @Resource
    private ITemplateService iTemplateService;

    @Resource
    private IFlowAndStopsTemplateVoService flowAndStopsTemplateVoServiceImpl;

    /**
     * Introducing logs, note that they are all packaged under "org.slf4j"
     */
    Logger logger = LoggerUtil.getLogger();

    @RequestMapping("/saveTemplate")
    @ResponseBody
    @Transactional
    public String saveData(HttpServletRequest request, Model model) {
        UserVo user = SessionUserUtil.getCurrentUser();
        String username = (null != user) ? user.getUsername() : "-1";
        String name = request.getParameter("name");
        String loadId = request.getParameter("load");
        String value = request.getParameter("value");
        MxGraphModelVo mxGraphModelVo = null;
        if (StringUtils.isAnyEmpty(name, loadId)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Some incoming parameters are empty");
        }
        Flow flowById = iFlowServiceImpl.getFlowById(loadId);
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

        Template template = new Template();
        template.setId(SqlUtils.getUUID32());
        template.setCrtDttm(new Date());
        template.setCrtUser(username);
        template.setEnableFlag(true);
        template.setLastUpdateUser(username);
        template.setLastUpdateDttm(new Date());
        template.setName(name);
        //Keep one copy in the database
        template.setValue(value);
        template.setFlow(flowById);
        //XML to file and save to the specified directory
        String path = FileUtils.createXml(flowAndStopInfoToXml, name, ".xml", SysParamsCache.XML_PATH);
        template.setPath(path);
        int addTemplate = iTemplateService.addTemplate(template);
        if (addTemplate > 0) {
            //Save the stop, property information
            List<Stops> stopsList = flowById.getStopsList();
            if (null != stopsList && stopsList.size() > 0) {
                flowAndStopsTemplateVoServiceImpl.addStopsList(stopsList, template);
            }
            return ReturnMapUtils.setSucceededMsgRtnJsonStr("save template success");
        } else {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("failed to save template");
        }
    }

    /**
     * Delete the template based on id
     *
     * @param id
     * @return
     */
    @RequestMapping("/deleteTemplate")
    @ResponseBody
    @Transactional
    public int deleteTemplate(String id) {
        int deleteTemplate = 0;
        if (StringUtils.isNoneBlank(id)) {
            Template template = iTemplateService.queryTemplate(id);
            if (null != template) {
                List<StopTemplateModel> stopsList = template.getStopsList();
                if (null != stopsList && stopsList.size() > 0) {
                    for (StopTemplateModel stopTemplateVo : stopsList) {
                        //First remove the stop attribute based on stopid
                        flowAndStopsTemplateVoServiceImpl.deleteStopPropertyTemByStopId(stopTemplateVo.getId());
                    }
                    //First delete stop based on templateId
                    flowAndStopsTemplateVoServiceImpl.deleteStopTemByTemplateId(template.getId());
                }
                //Delete the template
                deleteTemplate = iTemplateService.deleteTemplate(template.getId());
            }
        }
        return deleteTemplate;
    }

    /**
     * Upload xml file and save template
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload(@RequestParam("templateFile") MultipartFile file) {
        UserVo user = SessionUserUtil.getCurrentUser();
        String username = (null != user) ? user.getUsername() : "-1";
        if (file.isEmpty()) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Upload failed, please try again later");
        }
        String upload = FileUtils.upload(file, SysParamsCache.XML_PATH);
        Map<String, Object> map = JSON.parseObject(upload);
        if (null == map || map.isEmpty()) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Upload failed, please try again later");
        }
        Integer code = (Integer) map.get("code");
        if (500 == code) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("failed to upload file");
        }
        String name = (String) map.get("fileName");
        String path = (String) map.get("url");
        Template template = new Template();
        template.setId(SqlUtils.getUUID32());
        template.setCrtDttm(new Date());
        template.setCrtUser(username);
        template.setEnableFlag(true);
        template.setLastUpdateUser(username);
        template.setLastUpdateDttm(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmSSSS");
        Date nowDate = new Date();
        String fileName = sdf.format(nowDate);
        //File name prefix
        String prefix = name.substring(0, name.length() - 4);
        //Suffix .xml
        String Suffix = name.substring(name.length() - 4);
        //Add timestamp
        String uploadfileName = prefix + "-" + fileName;
        template.setName(uploadfileName + Suffix);
        template.setPath(path);
        //Read the xml file according to the saved file path and return the xml string
        String xmlFileToStr = FileUtils.XmlFileToStrByAbsolutePath(template.getPath());
        if (StringUtils.isBlank(xmlFileToStr)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("The xml file failed to read. Please try again.");
        }
        List<StopTemplateModel> stopsList = null;
        //Xml conversion Template object, including stops and attributes
        Template xmlToFlowStopInfo = FlowXmlUtils.xmlToFlowStopInfo(xmlFileToStr);
        if (null != xmlToFlowStopInfo) {
            stopsList = xmlToFlowStopInfo.getStopsList();
        }
        //Get the mxGraphModel part from the xml string and save it to value
        MxGraphModelVo xmlToMxGraphModelVo = FlowXmlUtils.allXmlToMxGraphModelVo(xmlFileToStr, 0);
        if (null != xmlToMxGraphModelVo) {
            // Convert the mxGraphModelVo from the query to XML
            String loadXml = FlowXmlUtils.mxGraphModelToXml(xmlToMxGraphModelVo);
            template.setValue(loadXml);
        }

        int addTemplate = iTemplateService.addTemplate(template);
        if (addTemplate > 0) {
            //Save stop, attribute information
            if (null != stopsList && stopsList.size() > 0) {
                List<Stops> stop = FlowXmlUtils.stopTemplateVoToStop(stopsList);
                if (null != stop && stop.size() > 0) {
                    flowAndStopsTemplateVoServiceImpl.addStopsList(stop, template);
                }
            }
            return ReturnMapUtils.setSucceededMsgRtnJsonStr("Template upload succeeded");
        } else {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("template upload failed");
        }
    }


    /**
     * load template
     *
     * @param request
     * @return
     */
    @RequestMapping("/loadingXmlPage")
    @ResponseBody
    public String loadingXml(HttpServletRequest request) {
        String templateId = request.getParameter("templateId");
        String loadId = request.getParameter("load");
        return iTemplateService.loadTemplateToFlow(loadId, templateId);
    }

    /**
     * Query all templates for drop-down displays
     *
     * @return
     */
    @RequestMapping("/templateAllSelect")
    @ResponseBody
    public String template() {
        Map<String, Object> rtnMap = new HashMap<String, Object>();
        rtnMap.put("code", 500);
        List<Template> findTemPlateList = iTemplateService.findTemPlateList();
        if (null != findTemPlateList && findTemPlateList.size() > 0) {
            rtnMap.put("code", 200);
            rtnMap.put("temPlateList", findTemPlateList);
        } else {
            rtnMap.put("errorMsg", "The query result is empty");
        }
        return JsonUtils.toJsonNoException(rtnMap);
    }

    /**
     * Download template
     *
     * @param response
     * @param templateId
     * @throws Exception
     */
    @RequestMapping("/templateDownload")
    public void downloadLocal(HttpServletResponse response, String templateId) {
        Template template = iTemplateService.queryTemplate(templateId);
        if (null == template) {
            logger.info("Template is empty,Download template failed");
        }
        String fileName = template.getName() + ".xml".toString(); // The default save name of the file
        String filePath = template.getPath();// File storage path
        FileUtils.downloadFileResponse(response, fileName, filePath);
    }

    @RequestMapping("/templatePage")
    @ResponseBody
    public String templatePage(HttpServletRequest request, Integer start, Integer length, Integer draw, String extra_search) {
        return iTemplateService.getTemplateListPage(start / length + 1, length, extra_search);
    }
}