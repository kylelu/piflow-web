package com.nature.mapper.stopsComponent;

import com.nature.component.stopsComponent.model.StopGroup;
import com.nature.provider.flow.StopGroupMapperProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StopGroupMapper {
    /**
     * Query all groups
     *
     * @return
     */
    @SelectProvider(type = StopGroupMapperProvider.class, method = "getStopGroupList")
    @Results({@Result(id = true, column = "id", property = "id"),
            @Result(column = "id", property = "stopsTemplateList", many = @Many(select = "com.nature.mapper.stopsComponent.StopsTemplateMapper.getStopsTemplateListByGroupId"))})
    List<StopGroup> getStopGroupList();

    /**
     * Query the stops template group based on the group id
     *
     * @param stopsId
     * @return
     */

    StopGroup getStopGroupById(String stopsId);

    /**
     * add flow_sotps_groups
     *
     * @param stopGroup
     * @return
     */
    @Insert("insert into flow_sotps_groups(id,crt_dttm,crt_user,enable_flag,last_update_dttm,last_update_user,version,group_name) values (#{id},#{crtDttm},#{crtUser},#{enableFlag},#{lastUpdateDttm},#{lastUpdateUser},#{version},#{groupName})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertStopGroup(StopGroup stopGroup);

    @Insert("insert into association_groups_stops_template(groups_id,stops_template_id) values (#{groups_id},#{stops_template_id})")
    int insertAssociationGroupsStopsTemplate(@Param("groups_id") String stopGroupId, @Param("stops_template_id") String stopsTemplateId);

    /**
     * Query flow_sotps_groups based on groupName
     *
     * @param groupName
     * @return
     */
    @Select("<script>" +
            "select id from flow_sotps_groups where enable_flag = 1 and group_name in " +
            "<foreach item='groupName' index='index' collection='group_name' open='(' separator=', ' close=')'>" +
            "#{groupName}" +
            "</foreach>" +
            "</script>")
    List<StopGroup> getStopGroupByName(@Param("group_name") List<String> groupName);

    @Delete("delete from association_groups_stops_template")
    int deleteGroupCorrelation();

    @Delete("delete from flow_sotps_groups")
    int deleteGroup();

    @Delete("delete from flow_stops_property_template")
    int deleteStopsPropertyInfo();

    @Delete("delete from flow_stops_template")
    int deleteStopsInfo();
}
