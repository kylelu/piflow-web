package com.nature.controller;

import com.nature.component.template.service.ITestService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * template的ctrl
 */
@RestController
@RequestMapping("/flowTemplate")
public class FlowTemplateCtrl {

    @Resource
    private ITestService testServiceImpl;


    @RequestMapping("/saveFlowTemplate")
    @ResponseBody
    public String saveFlowTemplate(HttpServletRequest request, Model model) {

        String name = request.getParameter("name");
        String loadId = request.getParameter("load");
        String value = request.getParameter("value");
        return testServiceImpl.addFlowTemplate(name, loadId, value);
    }

    @RequestMapping("/flowTemplatePage")
    @ResponseBody
    public String templatePage(Integer page, Integer limit, String param) {
        return testServiceImpl.getFlowTemplateListPage(page, limit, param);
    }

    /**
     * Delete the template based on id
     *
     * @param id
     * @return
     */
    @RequestMapping("/deleteFlowTemplate")
    @ResponseBody
    public int deleteFlowTemplate(String id) {
        return testServiceImpl.deleteFlowTemplate(id);
    }

    /**
     * Download template
     *
     * @param response
     * @param flowTemplateId
     * @throws Exception
     */
    @RequestMapping("/templateDownload")
    public void templateDownload(HttpServletResponse response, String flowTemplateId) throws Exception {
        testServiceImpl.templateDownload(response, flowTemplateId);
    }

    /**
     * Upload xml file and save flowTemplate
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/uploadXmlFile", method = RequestMethod.POST)
    @ResponseBody
    public String uploadXmlFile(@RequestParam("flowTemplateFile") MultipartFile file) {
        return testServiceImpl.uploadXmlFile(file);
    }

    /**
     * Query all templates for drop-down displays
     *
     * @return
     */
    @RequestMapping("/flowTemplateList")
    @ResponseBody
    public String flowTemplateList() {
        return testServiceImpl.flowTemplateAllSelect();
    }

    /**
     * load template
     *
     * @param request
     * @return
     */
    @RequestMapping("/loadingXmlPage")
    @ResponseBody
    @Transactional
    public String loadingXml(HttpServletRequest request) {
        String templateId = request.getParameter("templateId");
        String loadId = request.getParameter("load");
        return testServiceImpl.loadFlowTemplate(templateId, loadId);
    }
}