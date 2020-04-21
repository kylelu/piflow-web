package com.nature.repository.flow;

import com.nature.component.flow.model.Stops;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.io.Serializable;

public interface StopsJpaRepository extends JpaRepository<Stops, String>, JpaSpecificationExecutor<Stops>, Serializable {

    @Transactional
    @Modifying
    @Query("update Stops c set c.enableFlag = :enableFlag where c.id = :id")
    int updateEnableFlagById(@Param("id") String id, @Param("enableFlag") boolean enableFlag);

    @Query(nativeQuery = true, value = "select MAX(page_id+0) from flow_stops where enable_flag=1 and fk_flow_id=:flowId ")
    Integer getMaxStopPageIdByFlowId(@Param("flowId") String flowId);

    @Query(nativeQuery = true, value = "SELECT fs.name from flow_stops fs WHERE fs.enable_flag=1 and fs.fk_flow_id =:flowId")
    String[] getStopNamesByFlowId(@Param("flowId") String flowId);

}
