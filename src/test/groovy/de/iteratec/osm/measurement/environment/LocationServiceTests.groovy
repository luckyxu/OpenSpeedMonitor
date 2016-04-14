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

package de.iteratec.osm.measurement.environment

import grails.test.mixin.*
import org.junit.*
import spock.lang.Specification

/**
 * Test-suite for {@link LocationService}.
 */
@TestFor(LocationService)
@Mock([Location])
class LocationServiceTests extends Specification{

	LocationService serviceUnderTest

	void "setup"() {
		serviceUnderTest = service

		new Location(label: 'label1').save(validate: false)
		new Location(label: 'label2').save(validate: false)
		new Location(label: 'label3').save(validate: false)
		new Location(label: 'label4').save(validate: false)
	}

	void "testListLocations"() {
		expect:
		serviceUnderTest.listLocations().size() == 4
	}
}
