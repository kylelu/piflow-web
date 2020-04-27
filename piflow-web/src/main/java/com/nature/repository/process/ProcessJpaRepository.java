package com.nature.repository.process;

import com.nature.component.process.model.Process;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.io.Serializable;

public interface ProcessJpaRepository extends JpaRepository<Process, String>, JpaSpecificationExecutor<Process>, Serializable {
    /**
     * Paging query
     *
     * @return
     */
    @Query("select c from Process c where c.enableFlag=true and (c.name like CONCAT('%',:param,'%') or c.description like CONCAT('%',:param,'%'))")
    Page<Process> getProcessListPage(@Param("param") String param, Pageable pageable);

    /**
     * Paging query
     *
     * @return
     */
    @Query("select c from Process c where c.enableFlag=true and c.crtUser=:userName and (c.name like CONCAT('%',:param,'%') or c.description like CONCAT('%',:param,'%'))")
    Page<Process> getProcessListPage(@Param("userName") String userName, @Param("param") String param, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from flow_process s where s.enable_flag = 1 and s.fk_flow_process_group_id = :fid and s.page_id = :pageId")
    Process getProcessByPageId(@Param("fid") String fid, @Param("pageId") String pageId);

    @Query(nativeQuery = true, value = "select s.id from flow_process s where s.enable_flag = 1 and s.fk_flow_process_group_id = :fid and s.page_id = :pageId")
    String getProcessIdByPageId(@Param("fid") String fid, @Param("pageId") String pageId);

    @Query(nativeQuery = true, value = "select s.id from flow_process s where s.enable_flag = 1 and s.fk_flow_process_group_id = :fid and s.name = :processName")
    String getProcessIdByNameAndProcessGroupId(@Param("fid") String fid, @Param("processName") String processName);

    @Query(nativeQuery = true, value = "select MAX(s.page_id+0) from flow_process s where s.enable_flag = 1 and s.fk_flow_process_group_id = :processGroupId")
    String getMaxStopPageIdByProcessGroupId(@Param("processGroupId") String processGroupId);

}
