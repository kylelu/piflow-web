package com.nature.component.group;

import com.nature.ApplicationTests;
import com.nature.base.util.LoggerUtil;
import com.nature.component.stopsComponent.model.StopsTemplate;
import com.nature.component.stopsComponent.service.IStopsTemplateService;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class StopsTemplateServiceTest extends ApplicationTests {

	@Autowired
	private IStopsTemplateService stopsTemplateService;

	Logger logger = LoggerUtil.getLogger();

	@Test
	public void testGetStopsTemplateById() {
		StopsTemplate stopsTemplate = stopsTemplateService.getStopsTemplateById("fbb42f0d8ca14a83bfab13e0ba2d7293");
		if (null == stopsTemplate) {
			logger.info("The query result is empty");
			stopsTemplate = new StopsTemplate();
		}
		logger.info(stopsTemplate.toString());
	}

	@Test
	public void testGetStopsPropertyById() {
		StopsTemplate stopsTemplate = stopsTemplateService.getStopsPropertyById("fbb42f0d8ca14a83bfab13e0ba2d7293");
		if (null == stopsTemplate) {
			logger.info("The query result is empty");
			stopsTemplate = new StopsTemplate();
		}
		logger.info(stopsTemplate.toString());
	}

}
