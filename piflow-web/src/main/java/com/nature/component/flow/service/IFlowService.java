package com.nature.component.flow.service;

import com.nature.base.vo.UserVo;
import com.nature.component.flow.model.Flow;
import com.nature.component.flow.vo.FlowVo;

import javax.transaction.Transactional;
import java.util.List;

public interface IFlowService {

    /**
     * Query flow information based on id
     *
     * @param id
     * @return
     */
    @Transactional
    public Flow getFlowById(String id);

    /**
     * Query flow information based on pageId
     *
     * @param fid
     * @param pageId
     * @return
     */
    @Transactional
    public FlowVo getFlowByPageId(String fid, String pageId);

    /**
     * Query flow information based on id
     *
     * @param id
     * @return
     */
    @Transactional
    public String getFlowVoById(String id);

    /**
     * add flow(Contains drawing board information)
     *
     * @param flowVo
     * @return
     */
    @Transactional
    public String addFlow(FlowVo flowVo, UserVo user);

    @Transactional
    public int updateFlow(Flow flow, UserVo user);

    @Transactional
    public int deleteFLowInfo(String id);

    public String getMaxStopPageId(String flowId);

    public List<FlowVo> getFlowList();

    /**
     * Paging query flow
     *
     * @param offset Number of pages
     * @param limit  Number of pages per page
     * @param param  search for the keyword
     * @return
     */
    public String getFlowListPage(Integer offset, Integer limit, String param);

    public String getFlowExampleList();


    /**
     * Call the start interface and save the return information
     *
     * @param flowId
     * @return
     */
    public String runFlow(String flowId, String runMode);

    public String updateFlowBaseInfo(FlowVo flowVo);

    public String updateFlowNameById(String id, String flowGroupId, String flowName, String pageId);

    public Boolean updateFlowNameById(String id, String flowName);

    public String getMaxFlowPageIdByFlowGroupId(String flowGroupId);
	
}
