/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.chartsearch.api.db;

import java.util.List;

import org.openmrs.module.chartsearch.synonyms.Synonym;
import org.openmrs.module.chartsearch.synonyms.SynonymGroup;

@SuppressWarnings("rawtypes")
public interface SynonymDAO extends SingleClassDAO {
	
	/**
	 * Retrieve synonyms by synonym group
	 * 
	 * @param synonymGroup - the synonym group.
	 * @return list of synonyms for the given group.
	 * @should not return voided synonyms
	 */
	List<Synonym> getSynonymsByGroup(SynonymGroup synonymGroup);
	
	/**
	 * Retrieve count of synonyms by synonym group
	 * 
	 * @param synonymGroup - the synonym group.
	 * @return count of synonyms for the given group.
	 * @should not return voided synonyms
	 */
	Integer getSynonymsCountByGroup(SynonymGroup synonymGroup);
	
}
