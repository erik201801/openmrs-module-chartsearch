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
package org.apache.solr.handler.dataimport.custom;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.solr.handler.dataimport.DataImportHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

/**
 *
 */
public class PatientInfoProviderFileImpl implements PatientInfoProvider {
	
	private static final Logger log = LoggerFactory.getLogger(DataImportHandler.class);
	
	private final String FILENAME = "PatientsInfo.data";
	
	/**
	 * @see org.apache.solr.handler.dataimport.custom.PatientInfoProvider#getData()
	 */
	@Override
	public Map<Integer, PatientInfo> getData() {
		ICsvMapReader mapReader = null;
		try {
			try {
				mapReader = new CsvMapReader(new FileReader(FILENAME), CsvPreference.STANDARD_PREFERENCE);
				// the header elements are used to map the values to the bean (names must match)
				final String[] header = mapReader.getHeader(true);
				if (header == null) return null;
				final CellProcessor[] processors = getProcessors();
				
				Map<Integer, PatientInfo> data = new HashMap<Integer, PatientInfo>();
				PatientInfo patientInfo;
				Map<String, Object> map;
				while ((map = mapReader.read(header, processors)) != null) {
					Integer patientId = Integer.parseInt((String) map.get("patientId"));
					Long time = Long.parseLong((String) map.get("time"));
					patientInfo = new PatientInfo(patientId, time);
					log.info(String.format("lineNo=%s, rowNo=%s, customer=%s", mapReader.getLineNumber(),
					    mapReader.getRowNumber(), patientInfo));
					data.put(patientInfo.getPatientId(), patientInfo);
				}
				
				log.info("Returned {} entries", data.size());
				return data;
				
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block				
				log.error("Error generated", e);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("Error generated", e);
			}
			
		}
		finally {
			if (mapReader != null) {
				try {
					mapReader.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Error generated", e);
				}
			}
		}
		
		log.error("Returned 0 entries, exception!");
		return null;
		
	}
	
	/**
	 * @see org.apache.solr.handler.dataimport.custom.PatientInfoProvider#updateData(java.util.Map)
	 */
	@Override
	public void updateData(Collection<PatientInfo> data) {
		ICsvMapWriter beanWriter = null;
		try {
			beanWriter = new CsvMapWriter(new FileWriter(FILENAME), CsvPreference.STANDARD_PREFERENCE);
			
			// the header elements are used to map the bean values to each column (names must match)
			final String[] header = new String[] { "patientId", "time" };
			final CellProcessor[] processors = getProcessors();
			
			// write the header
			beanWriter.writeHeader(header);
			
			// write the beans
			for (final PatientInfo patientInfo : data) {
				final Map<String, Object> info = new HashMap<String, Object>();
				info.put(header[0], patientInfo.getPatientId());
				info.put(header[1], patientInfo.getTime().getTime());
				beanWriter.write(info, header, processors);
			}
			log.info("Writing patient info data to file finished succesfully");
			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Error generated", e);
		}
		finally {
			if (beanWriter != null) {
				try {
					beanWriter.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Error generated", e);
				}
			}
		}
		
	}
	
	/**
	 * Sets up the processors used for the examples. There are 10 CSV columns, so 10 processors are
	 * defined. Empty columns are read as null (hence the NotNull() for mandatory columns).
	 * 
	 * @return the cell processors
	 */
	private static CellProcessor[] getProcessors() {
		
		final CellProcessor[] processors = new CellProcessor[] { new Unique(), // patientId (must be unique)
		        new NotNull(), // last index time
		};
		
		return processors;
	}
	
}
