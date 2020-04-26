package com.nature.repository.process;

import com.nature.component.process.model.ProcessAndProcessGroup;
import com.nature.component.process.model.ProcessGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public interface ProcessGroupJpaRepository extends JpaRepository<ProcessGroup, String>, JpaSpecificationExecutor<ProcessGroup>, Serializable {
    /**
     * Paging query
     *
     * @return
     */
    @Query("select c from ProcessGroup c where c.enableFlag=true and (c.name like CONCAT('%',:param,'%') or c.description like CONCAT('%',:param,'%'))")
    Page<ProcessGroup> getProcessGroupListPage(@Param("param") String param, Pageable pageable);

    /**
     * Paging query
     *
     * @return
     */
    @Query("select c from ProcessGroup c where c.enableFlag=true and c.crtUser=:userName and (c.name like CONCAT('%',:param,'%') or c.description like CONCAT('%',:param,'%'))")
    Page<ProcessGroup> getProcessGroupListPageByUser(@Param("userName") String userName, @Param("param") String param, Pageable pageable);

    @Transactional
    @Modifying
    @Query("update ProcessGroup c set c.enableFlag = :enableFlag, c.lastUpdateUser = :lastUpdateUser, c.lastUpdateDttm = :lastUpdateDttm where c.id = :id")
    int updateEnableFlagById(@Param("id") String id, @Param("lastUpdateUser") String lastUpdateUser, @Param("lastUpdateDttm") Date lastUpdateDttm, @Param("enableFlag") boolean enableFlag);

    @Transactional
    @Query(nativeQuery = true, value = "select * from flow_process_group s where s.enable_flag = 1 and s.fk_flow_process_group_id = :fid and s.page_id = :pageId")
    ProcessGroup getProcessGroupByPageId(@Param("fid") String fid, @Param("pageId") String pageId);

    @Query(nativeQuery = true, value = "select s.id from flow_process_group s where s.enable_flag = 1 and s.fk_flow_process_group_id = :fid and s.page_id = :pageId")
    String getProcessGroupIdByPageId(@Param("fid") String fid, @Param("pageId") String pageId);

    @Query(value = "select s from ProcessGroup s where s.enableFlag=true and s.appId=:appId")
    ProcessGroup getProcessGroupByAppId(@Param("appId") String appId);


    /**
     * custom Paging query
     *
     * @return
     */
    @Query(nativeQuery = true,
            value = "SELECT id,last_update_dttm AS lastUpdateDttm,crt_dttm AS crtDttm,app_id AS appId,name,description,start_time AS startTime,end_time AS endTime,progress,state,parent_process_id AS parentProcessId,processType FROM (" +
                    "SELECT id,last_update_dttm,crt_dttm,app_id,name,description,start_time,end_time,progress,state,parent_process_id,'TASK' AS processType FROM flow_process " +
                    "WHERE enable_flag=1 AND app_id IS NOT NULL AND fk_flow_process_group_id IS NULL AND (name LIKE CONCAT('%',:param,'%') OR description LIKE CONCAT('%',:param,'%'))" +
                    "UNION ALL " +
                    "SELECT id,last_update_dttm,crt_dttm,app_id,name,description,start_time,end_time,progress,state,parent_process_id,'GROUP' AS processType FROM flow_process_group " +
                    "WHERE enable_flag=1 AND app_id IS NOT NULL AND fk_flow_process_group_id IS NULL AND (name LIKE CONCAT('%',:param,'%') OR description LIKE CONCAT('%',:param,'%'))" +
                    ") AS re"
            , countQuery = "SELECT COUNT(re.id) FROM " +
                           "(" +
                           "SELECT id FROM flow_process WHERE enable_flag=1 AND app_id IS NOT NULL AND fk_flow_process_group_id IS NULL AND (name LIKE CONCAT('%',:param,'%') OR description LIKE CONCAT('%',:param,'%'))" +
                           "UNION ALL " +
                           "SELECT id FROM flow_process_group WHERE enable_flag=1 AND app_id IS NOT NULL AND fk_flow_process_group_id IS NULL AND (name LIKE CONCAT('%',:param,'%') OR description LIKE CONCAT('%',:param,'%'))" +
                           ") AS re")
    Page<Map<String,Object>> getProcessAndProcessGroupListPage(@Param("param") String param, Pageable pageable);

    /**
     * custom Paging query
     *
     * @return
     */
    @Query(nativeQuery = true,
            value = "SELECT id,last_update_dttm AS lastUpdateDttm,crt_dttm AS crtDttm,app_id AS appId,name,description,start_time AS startTime,end_time AS endTime,progress,state,parent_process_id AS parentProcessId,processType FROM " +
                    "(" +
                    "SELECT id,last_update_dttm,crt_dttm,app_id,name,description,start_time,end_time,progress,state,parent_process_id,'TASK' AS processType FROM flow_process " +
                    "WHERE enable_flag=1 AND crt_user=:userName AND app_id IS NOT NULL AND fk_flow_process_group_id IS NULL AND (name LIKE CONCAT('%',:param,'%') OR description LIKE CONCAT('%',:param,'%'))" +
                    "UNION ALL " +
                    "SELECT id,last_update_dttm,crt_dttm,app_id,name,description,start_time,end_time,progress,state,parent_process_id,'GROUP' AS processType FROM flow_process_group " +
                    "WHERE enable_flag=1 AND crt_user=:userName AND app_id IS NOT NULL AND fk_flow_process_group_id IS NULL AND (name LIKE CONCAT('%',:param,'%') OR description LIKE CONCAT('%',:param,'%'))" +
                    ")"
            , countQuery = "SELECT COUNT(re.id) FROM " +
                           "(" +
                           "SELECT id FROM flow_process WHERE enable_flag=1 AND crt_user=:userName AND app_id IS NOT NULL AND fk_flow_process_group_id IS NULL AND (name LIKE CONCAT('%',:param,'%') OR description LIKE CONCAT('%',:param,'%'))" +
                           "UNION ALL " +
                           "SELECT id FROM flow_process_group WHERE enable_flag=1 AND crt_user=:userName AND app_id IS NOT NULL AND fk_flow_process_group_id IS NULL AND (name LIKE CONCAT('%',:param,'%') OR description LIKE CONCAT('%',:param,'%'))" +
                           ") AS re")
    Page<Map<String,Object>> getProcessAndProcessGroupListPageByUser(@Param("userName") String userName, @Param("param") String param, Pageable pageable);
}
