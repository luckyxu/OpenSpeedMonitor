/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm
/**
 * ConfigService
 * Delivers application-wide configurations.
 */
class ConfigService {

	def grailsApplication
	
    static transactional = true

	/**
	 * Gets detail-data storage time in weeks from osm-configuration.
	 * @return Time in weeks to store detail-data of the application.
	 * @see OsmConfiguration
	 * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#detailDataStorageTimeInWeeks} isn't set.
	 */
    Integer getDetailDataStorageTimeInWeeks() {
		return (Integer)retrieveConfigValue('detailDataStorageTimeInWeeks')
    }
	
    /**
     * Gets detail-data storage time in weeks from osm-configuration.
     * @return Time in weeks to store detail-data of the application.
     * @see OsmConfiguration
     * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#detailDataStorageTimeInWeeks} isn't set.
     */
    Integer getDefaultMaxDownloadTimeInMinutes() {
    	return (Integer)retrieveConfigValue('defaultMaxDownloadTimeInMinutes')
    }
	
	/** 
	 * Gets minDocCompleteTimeInMillisecs from osm-configuration.
	 * {@link EventResult}s with a docCompleteTimeInMillisecs lower than this won't be factored in csi-{@link MeasuredValue}s.
	 * @return The minimum doc complete time in millisecs {@link EventResult}s get factored with in csi-{@link MeasuredValue}s.
     * @see OsmConfiguration
     * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#minDocCompleteTimeInMillisecs} isn't set.
	 */
	Integer getMinDocCompleteTimeInMillisecs(){
		return (Integer)retrieveConfigValue('minDocCompleteTimeInMillisecs')
	}
	
	/**
	 * Gets maxDocCompleteTimeInMillisecs from osm-configuration.
	 * {@link EventResult}s with a docCompleteTimeInMillisecs lower than this won't be factored in csi-{@link MeasuredValue}s.
	 * @return The maximum doc complete time in millisecs {@link EventResult}s get factored with in csi-{@link MeasuredValue}s.
     * @see OsmConfiguration
     * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#maxDocCompleteTimeInMillisecs} isn't set.
	 */
	Integer getMaxDocCompleteTimeInMillisecs(){
		return (Integer)retrieveConfigValue('maxDocCompleteTimeInMillisecs')
	}
	
	/** 
	 * Gets measurementsGenerallyEnabled from osm-configuration.
	 * If false no measurements get started at all (even for active {@link Job}s). If true the active attribute of each {@link Job} decides whether or not it runs measurements. 
	 * @return Whether the performance measurement is generally enabled or not.
     * @see OsmConfiguration
     * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#measurementsGenerallyEnabled} isn't set.
	 */
	boolean areMeasurementsGenerallyEnabled(){
		return (Boolean)retrieveConfigValue('measurementsGenerallyEnabled')
	}
	
	Integer getInitialChartHeightInPixels(){
		return (Integer)retrieveConfigValue('initialChartHeightInPixels')
	}
	
	private Object retrieveConfigValue(String name) {
		List<OsmConfiguration> osmConfigs = OsmConfiguration.list()
		if (osmConfigs.size() != 1 || osmConfigs[0]."${name}" == null) {
			throw new IllegalStateException("${name} couldn\'t be read from Configuration!")
		}else{
			return osmConfigs[0]."${name}"
		}
	}
	
	/**
	 * Gets the name of the used database driver of running environment 
	 * @return {@link String} of the used database driver name
	 */
	public String getDatabaseDriverClassName() {
		return grailsApplication.config.dataSource.driverClassName;
	}
}