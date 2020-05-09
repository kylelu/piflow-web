package com.nature.domain.flow;

import com.nature.component.flow.model.Stops;
import com.nature.repository.flow.StopsJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class StopsDomain {

    @Autowired
    private StopsJpaRepository stopsJpaRepository;

    private Specification<Stops> addEnableFlagParam() {
        Specification<Stops> specification = new Specification<Stops>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Stops> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                //root.get("enableFlag") means to get the field name of enableFlag
                return criteriaBuilder.equal(root.get("enableFlag"), 1);
            }
        };
        return specification;
    }

    public Stops getStopsById(String id) {
        Stops stops = stopsJpaRepository.getOne(id);
        if (null != stops && !stops.getEnableFlag()) {
            stops = null;
        }
        return stops;
    }

    public List<Stops> getStopsList() {
        return stopsJpaRepository.findAll(addEnableFlagParam());
    }

    public Stops saveOrUpdate(Stops stops) {
        return stopsJpaRepository.save(stops);
    }

    public List<Stops> saveOrUpdate(List<Stops> stopsList) {
        return stopsJpaRepository.saveAll(stopsList);
    }

    public int updateEnableFlagById(String id, boolean enableFlag) {
        return stopsJpaRepository.updateEnableFlagById(id, enableFlag);
    }

    public Integer getMaxStopPageIdByFlowId(String flowId) {
        return stopsJpaRepository.getMaxStopPageIdByFlowId(flowId);
    }

    public String[] getStopNamesByFlowId(String flowId) {
        return stopsJpaRepository.getStopNamesByFlowId(flowId);
    }

    public Stops getStopsByPageId(String fid, String stopPageId) {
        return stopsJpaRepository.getStopsByPageId(fid, stopPageId);
    }


}
