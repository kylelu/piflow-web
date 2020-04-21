package com.nature.domain.flow;

import com.nature.component.flow.model.Flow;
import com.nature.repository.flow.FlowJpaRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Component
public class FlowDomain {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private FlowJpaRepository flowJpaRepository;

    public Flow getFlowById(String id) {
        Flow flow = flowJpaRepository.getOne(id);
        if (null != flow && !flow.getEnableFlag()) {
            flow = null;
        }
        return flow;
    }

    public Flow saveOrUpdate(Flow flow) {
        return flowJpaRepository.save(flow);
    }

    public List<Flow> saveOrUpdate(List<Flow> flowList) {
        return flowJpaRepository.saveAll(flowList);
    }

    public int updateEnableFlagById(String id, boolean enableFlag) {
        return flowJpaRepository.updateEnableFlagById(id, enableFlag);
    }

    public Flow getFlowByPageId(String fid, String pageId) {
        return flowJpaRepository.getFlowByPageId(fid, pageId);
    }

    public String getFlowIdByPageId(String fid, String pageId) {
        return flowJpaRepository.getFlowIdByPageId(fid, pageId);
    }

    public String getFlowIdByNameAndFlowGroupId(String fid, String flowName) {
        return flowJpaRepository.getFlowIdByNameAndFlowGroupId(fid, flowName);
    }

    public Integer getMaxFlowPageIdByFlowGroupId(String flowGroupId) {
        return flowJpaRepository.getMaxFlowPageIdByFlowGroupId(flowGroupId);
    }

    public String[] getFlowNamesByFlowGroupId(String flowGroupId){
        return flowJpaRepository.getFlowNamesByFlowGroupId(flowGroupId);
    }

}
