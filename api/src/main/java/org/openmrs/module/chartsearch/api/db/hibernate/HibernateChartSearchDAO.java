/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.chartsearch.api.db.hibernate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.chartsearch.api.db.ChartSearchDAO;
import org.openmrs.module.chartsearch.solr.ChartSearchCustomIndexer;

/**
 * It is a default implementation of {@link ChartSearchDAO}.
 */
public class HibernateChartSearchDAO implements ChartSearchDAO {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * SQL processing to get patient data to be indexed
	 */
	@SuppressWarnings("deprecation")
	@Override
	public String indexAllPatientData(Integer numberOfResults, SolrServer solrServer, String indexingInfo) {
		PreparedStatement preparedStatement = null;
		SolrInputDocument doc = new SolrInputDocument();
		
		indexingInfo += "We are now going to fetch patient data from the database, this can take some time depending on the number of documents you have set!!!<br />";
		String sql = " SELECT  o.uuid as id,  obs_id,	 person_id,  obs_datetime, obs_group_id, cn1.name as concept_name, cn2.name as coded, value_boolean,  value_datetime, value_numeric, value_text, cc.concept_class_name FROM openmrs.obs o "
		        + "inner join (SELECT * FROM openmrs.concept_name c WHERE c.locale = 'en' AND concept_name_type = 'FULLY_SPECIFIED') as cn1 on cn1.concept_id = o.concept_id "
		        + "LEFT join (SELECT * FROM openmrs.concept_name c WHERE c.locale = 'en' AND concept_name_type = 'FULLY_SPECIFIED') as cn2 on cn2.concept_id = o.value_coded  "
		        + "LEFT join (SELECT DISTINCT o.concept_id, class.name AS concept_class_name FROM concept_class class JOIN concept c ON c.class_id = class.concept_class_id JOIN obs o ON o.concept_id = c.concept_id) AS cc ON cc.concept_id = o.concept_id "
		        + "WHERE o.voided=0 AND cn1.voided=0 LIMIT " + numberOfResults;
		
		try {
			log.debug("SQL Query for indexing all data is: " + sql);
			
			preparedStatement = sessionFactory.getCurrentSession().connection().prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			indexingInfo += "We have now finished to fetch all the patient data from th database and beginning the indexing.<br />";
			
			while (rs.next()) {
				setResultsFieldValues(rs);
				addResultsFieldValuesToADocument(doc, indexingInfo);
				
				UpdateResponse resp = solrServer.add(doc);
				
				resp = solrServer.commit(true, true);
				resp = solrServer.optimize(true, true);
				
				indexingInfo += "We have now finished indexing all the data :)<br />";
				doc.clear();
			}
		}
		catch (Exception e) {
			throw new DAOException("Error getting mrn log", e);
		}
		finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				}
				catch (SQLException e) {
					log.error("Error generated while closing statement", e);
				}
			}
		}
		return indexingInfo;
	}
	
	private static void setResultsFieldValues(ResultSet rs) throws SQLException {
		ChartSearchCustomIndexer.setId(rs.getString("id"));
		ChartSearchCustomIndexer.setObsId(rs.getInt("obs_id"));
		ChartSearchCustomIndexer.setPersonId(rs.getInt("person_id"));
		ChartSearchCustomIndexer.setObsDatetime(rs.getDate("obs_datetime"));
		ChartSearchCustomIndexer.setObsGroupId(rs.getInt("obs_group_id"));
		ChartSearchCustomIndexer.setConceptName(rs.getString("concept_name"));
		ChartSearchCustomIndexer.setCoded(rs.getString("coded"));
		ChartSearchCustomIndexer.setValueBoolean(rs.getBoolean("value_boolean"));
		ChartSearchCustomIndexer.setValueDatetime(rs.getDate("value_datetime"));
		ChartSearchCustomIndexer.setValueNumeric(rs.getFloat("value_numeric"));
		ChartSearchCustomIndexer.setValueText(rs.getString("value_text"));
		ChartSearchCustomIndexer.setConceptClassName(rs.getString("concept_class_name"));
	}
	
	private static void addResultsFieldValuesToADocument(SolrInputDocument doc, String indexingInfo) {
		doc.addField("id", ChartSearchCustomIndexer.getId());
		doc.addField("obs_id", ChartSearchCustomIndexer.getObsId());
		doc.addField("person_id", ChartSearchCustomIndexer.getPersonId());
		doc.addField("obs_datetime", ChartSearchCustomIndexer.getObsDatetime());
		doc.addField("obs_group_id", ChartSearchCustomIndexer.getObsGroupId());
		doc.addField("concept_name", ChartSearchCustomIndexer.getConceptName());
		doc.addField("coded", ChartSearchCustomIndexer.getCoded());
		doc.addField("value_boolean", ChartSearchCustomIndexer.isValueBoolean());
		doc.addField("value_datetime", ChartSearchCustomIndexer.getValueDatetime());
		doc.addField("value_numeric", ChartSearchCustomIndexer.getValueNumeric());
		doc.addField("value_text", ChartSearchCustomIndexer.getValueText());
		doc.addField("concept_class_name", ChartSearchCustomIndexer.getConceptClassName());
		indexingInfo += "Finished adding id, obs_id, person_id, obs_datetime, obs_group_id, concept_name," +
				" coded, value_boolean, value_datetime, value_numeric, value_text, and concept_class_name to the document to be indexed.<br />";
	}
}
