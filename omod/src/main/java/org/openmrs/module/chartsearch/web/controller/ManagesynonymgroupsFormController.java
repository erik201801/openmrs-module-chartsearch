/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.chartsearch.web.controller;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.chartsearch.api.ChartSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("chartsearch.ManagesynonymGroupsFormController")
@RequestMapping("/module/chartsearch/managesynonymgroups")
public class ManagesynonymgroupsFormController {
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(method = RequestMethod.GET)
	public void showForm(ModelMap map) {
		ChartSearchService chartSearchService = Context.getService(ChartSearchService.class);
		List synGroups = chartSearchService.getAllSynonymGroups();
		
		map.put("synonymGroups", synGroups);
		
	}
	
}
