package com.nature.component.template.service;

import com.nature.component.template.model.FlowTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface IFlowTemplateService {

    /**
     * save FlowTemplate
     *
     * @param name
     * @param loadId
     * @param value
     * @return
     */
    public String addFlowTemplate(String name, String loadId, String value);

    /**
     * Query the list of all FlowTemplates
     *
     * @return
     */
    public List<FlowTemplate> findFlowTemPlateList();

    /**
     * Delete the FlowTemplate based on id
     *
     * @param id
     * @return
     */
    public int deleteFlowTemplate(String id);

    /**
     * Query the FlowTemplate by id
     *
     * @param id
     * @return
     */
    public FlowTemplate queryFlowTemplate(String id);

    /**
     * Query all FlowTemplate list pagination
     *
     * @param offset Number of pages
     * @param limit  Number of pages per page
     * @param param  search for the keyword
     * @return
     */
    public String getFlowTemplateListPage(Integer offset, Integer limit, String param);

    public String loadFlowTemplateToFlow(String flowId,String flowTemplateId);

    public String uploadFlowTemplate(MultipartFile file);
}
