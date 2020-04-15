package com.nature.controller;

import com.nature.base.util.FileUtils;
import com.nature.base.util.JsonUtils;
import com.nature.base.util.LoggerUtil;
import com.nature.component.template.model.FlowTemplate;
import com.nature.component.template.service.IFlowTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
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
    private IFlowTemplateService flowTemplateServiceImpl;

    /**
     * Introducing logs, note that they are all packaged under "org.slf4j"
     */
    Logger logger = LoggerUtil.getLogger();

    @RequestMapping("/saveTemplate")
    @ResponseBody
    @Transactional
    public String saveData(HttpServletRequest request, Model model) {
        String name = request.getParameter("name");
        String loadId = request.getParameter("load");
        String value = request.getParameter("value");
        return flowTemplateServiceImpl.addFlowTemplate(name, loadId, value);
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
    public int deleteFlowTemplate(String id) {
        if (StringUtils.isBlank(id)) {
            return 0;
        }
        return flowTemplateServiceImpl.deleteFlowTemplate(id);
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
        return flowTemplateServiceImpl.uploadFlowTemplate(file);
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
        return flowTemplateServiceImpl.loadFlowTemplateToFlow(loadId, templateId);
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
        List<FlowTemplate> findTemPlateList = flowTemplateServiceImpl.findFlowTemPlateList();
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
        FlowTemplate flowTemplate = flowTemplateServiceImpl.queryFlowTemplate(templateId);
        if (null == flowTemplate) {
            logger.info("Template is empty,Download template failed");
        }
        String fileName = flowTemplate.getName() + ".xml".toString(); // The default save name of the file
        String filePath = flowTemplate.getPath();// File storage path
        FileUtils.downloadFileResponse(response, fileName, filePath);
    }

    @RequestMapping("/templatePage")
    @ResponseBody
    public String flowTemplatePage(HttpServletRequest request, Integer start, Integer length, Integer draw, String extra_search) {
        return flowTemplateServiceImpl.getFlowTemplateListPage(start / length + 1, length, extra_search);
    }
}