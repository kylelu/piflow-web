package com.nature.mapper;

import com.nature.ApplicationTests;
import com.nature.base.util.LoggerUtil;
import com.nature.base.util.SqlUtils;
import com.nature.component.flow.model.Property;
import com.nature.component.flow.model.Stops;
import com.nature.component.stopsComponent.model.PropertyTemplate;
import com.nature.component.stopsComponent.model.StopsTemplate;
import com.nature.mapper.flow.PropertyMapper;
import com.nature.mapper.flow.StopsMapper;
import com.nature.mapper.stopsComponent.StopsTemplateMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class PropertyMapperTest extends ApplicationTests {

	@Autowired
	private PropertyMapper propertyMapper;
	@Autowired
	private StopsMapper stopsMapper;
	@Autowired
	private StopsTemplateMapper stopsTemplateMapper;

	Logger logger = LoggerUtil.getLogger();

	@Test
	public void testGetPropertyListByStopsId() {
		List<Property> propertyList = propertyMapper.getPropertyListByStopsId("85f90a18423245b09cde371cbb3330sd");
		if (null == propertyList) {
			logger.info("The query result is empty");
		} else {
			logger.info(propertyList.size() + "");
		}
	}

	@Test
	public void testGetStopsPropertyById() {
		Stops stops = propertyMapper.getStopGroupList("fbb42f0d8ca14a83bfab13e0ba2d7292", "2");
		if (null == stops) {
			logger.info("The query result is empty");
		}
		logger.info(stops.toString() + "         ---------------------     name：" + stops.getName());
	}

	@Test
	public void updateStopsPropertyById() {
		int update = propertyMapper.updatePropertyCustomValue("hahah", "8731612e48cc4cc89a24191e737817f2");
		if (0 == update) {
			logger.info("The modification failed, affecting the number of rows:"+update);
		}else {
			logger.info("The modification failed, affecting the number of rows:"+update);
		}
	}

	@Test
	public void deleteStopsPropertyById() {
		int delete = propertyMapper.deleteStopsPropertyById("8731612e42cc4cc89a24191e737817f2");
		if (0 == delete) {
			logger.info("Delete Failed"+",Number of rows affected："+delete);
		}else {
			logger.info("Successfully Deleted"+",Number of rows affected："+delete);
		}
	}
	
	@Test
	public void updateTempleteProperyInfo() {
          Map<String, Property> PropertyMap = new HashMap<String, Property>();
          List<Property> addPropertyList = new ArrayList<Property>();
          //Get stop information
          Stops stopsList = stopsMapper.getStopsById("32a3e372084d4f4eac97853c66b7b2d8");
          //Gets the StopsTemplate for the current stops
          List<StopsTemplate> stopsTemplateList = stopsTemplateMapper.getStopsTemplateByName(stopsList.getName());
          StopsTemplate stopsTemplate = stopsTemplateList.get(0);
          logger.info("StopsTemplateList records the number of bars:"+stopsTemplateList.size());
            //Get the template property of the StopsTemplate
              List<PropertyTemplate> propertiesTemplateList = stopsTemplate.getProperties();
              // The property that currently exists for stop
              List<Property> property = stopsList.getProperties();
              if (null != property && property.size()>0)  
            	  for (Property one : property) {
            		  PropertyMap.put(one.getName(),one);
      			}
              //If the template's data is greater than the current number of stops attributes, do the same and add more stops attributes
              if (propertiesTemplateList.size()>0 && property.size()>0) {
            	  for (PropertyTemplate pt : propertiesTemplateList) { 
        				if (null != pt) {
        					Property ptname = PropertyMap.get(pt.getName());
        					if (ptname!=null) {
        						PropertyMap.remove(pt.getName());
                                Property update = new Property();
                                String name = pt.getName();
                                Date crtDttm = pt.getCrtDttm();
                                String crtUser = pt.getCrtUser();
                            	try {
									BeanUtils.copyProperties(pt, update);
								} catch (Exception e) {
									e.printStackTrace();
								} 
                            	update.setName(name);
                            	update.setCrtDttm(crtDttm);
                            	update.setCrtUser(crtUser);
                            	update.setId(ptname.getId());
                            	propertyMapper.updateStopsProperty(update);
        					}else {
        						logger.info("===============The stop attribute is inconsistent with the template and needs to be added=================");
        						Property newProperty = new Property();
        						try {
									BeanUtils.copyProperties(pt, newProperty);
								} catch (Exception e) {
									e.printStackTrace();
								}  
        						newProperty.setId(SqlUtils.getUUID32());
        						newProperty.setCrtDttm(new Date());
        						newProperty.setCrtUser("Nature");
        						newProperty.setEnableFlag(true);
        						newProperty.setStops(stopsList);
        						addPropertyList.add(newProperty);
							} 
        				}
        			}
            	  if (addPropertyList.size()>0) {
					propertyMapper.addPropertyList(addPropertyList);
				}
			} 
          	// All the changes in the objectPathsMap that need to be modified, leaving the logic to be deleted.
              if (null != PropertyMap && PropertyMap.size()>0)  
				for (String pageid : PropertyMap.keySet()) {
					Property deleteProperty = PropertyMap.get(pageid);
					if (null != deleteProperty) {
						logger.info("===============The stop attribute is inconsistent with the template and needs to be removed=================");
						propertyMapper.deleteStopsPropertyById(deleteProperty.getId());
					}
				}
         
      }

}
