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

package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.chart.AggregatorType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import spock.lang.Specification

import static org.junit.Assert.*

/**
 * <p>
 * Test-suite of {@link EventResultDashboardController} and 
 * {@link EventResultDashboardShowAllCommand}.
 * </p> 
 *
 * @author rhe
 * @since IT-98
 */
@TestFor(EventResultDashboardController)
@Mock([ConnectivityProfile])
class EventResultDashboardControllerTests extends Specification {

    public static final String CUSTOM_CONNECTIVITY_NAME = 'Custom (6.000/512 Kbps, 50ms)'
    EventResultDashboardController controllerUnderTest
    static EventResultDashboardShowAllCommand command

    void setup() {
        controllerUnderTest = controller;

        // Mock relevant services:
        command = new EventResultDashboardShowAllCommand()
        controllerUnderTest.jobGroupDaoService = Stub(JobGroupDaoService);
        controllerUnderTest.eventResultDashboardService = Stub(EventResultDashboardService);
    }

    void "command without bound parameters is invalid"() {
        expect:
        !command.validate()
        command.selectedFolder == []
        command.selectedPages == []
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
    }

    void "command with empty bound parameters is invalid"() {
        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
        command.selectedFolder == []
        command.selectedPages == []
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
    }

    void "command bound with default parameters is valid"() {
        given:
        setDefaultParams()
        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.from == new DateTime(2013, 8, 18, 16, 0, 0, DateTimeZone.UTC)
        command.to == new DateTime(2013, 8, 18, 18, 0, 0, DateTimeZone.UTC)
        command.createTimeFrameInterval().start == command.from
        command.createTimeFrameInterval().end == command.to
        command.selectedAggrGroupValuesUnCached == [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES]
        command.selectedFolder == [1L]
        command.selectedPages == [1L, 5L]
        command.selectedAllMeasuredEvents
        command.selectedMeasuredEventIds == [7L, 8L, 9L]
        command.selectedAllBrowsers
        command.selectedBrowsers == [2L]
        command.selectedAllLocations
        command.selectedLocations == [17L]
    }

    void "command is invalid if 'to' is before 'from'"() {
        given:
        setDefaultParams()
        params.from = "2013-08-18T12:00:00.000Z"
        params.to = "2013-08-18T11:00:00.000Z"

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
    }

    void "command is invalid if 'to' is equal to 'from'"() {
        given:
        setDefaultParams()
        params.from = "2013-08-18T11:00:00.000Z"
        params.to = "2013-08-18T11:00:00.000Z"

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
    }

    void "command supports legacy date format in 'from' and 'to'"() {
        given:
        setDefaultParams()
        params.from = "18.08.2013"
        params.to = "18.08.2013"

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.from == new DateTime(2013, 8, 18, 0, 0, 0, 0)
        command.to == new DateTime(2013, 8, 18, 23, 59, 59, 999)
        command.createTimeFrameInterval().start == command.from
        command.createTimeFrameInterval().end == command.to
    }

    void "command supports automatic time frame"() {
        given:
        setDefaultParams()
        params.from = null
        params.to = null
        params.selectedTimeFrameInterval = 3000
        long nowInMillis = DateTime.now().getMillis()
        long allowedDelta = 1000

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.from == null
        command.to == null
        Interval timeFrame = command.createTimeFrameInterval()
        Math.abs(timeFrame.endMillis - nowInMillis) < allowedDelta
        Math.abs(timeFrame.startMillis - (nowInMillis - 3000 * 1000)) < allowedDelta
    }


    void "command is valid with non-default values"() {
        given:
        setDefaultParams()
        params.selectedAllBrowsers = false
        params.selectedAllLocations = false
        params.selectedAllMeasuredEvents = false
        params.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_LOAD_TIME.toString()]
        params.includeNativeConnectivity = false
        params.includeCustomConnectivity = true

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        !command.selectedAllBrowsers
        !command.selectedAllLocations
        !command.selectedMeasuredEventIds
        !command.selectedAggrGroupValuesCached == [AggregatorType.RESULT_CACHED_LOAD_TIME]
        !command.includeNativeConnectivity
        command.includeCustomConnectivity
        command.
    }

    void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid() {
        given:
        setDefaultParams()
        // Fill-in request args:
        params.selectedPages = ['NOT-A-NUMBER']
        params.selectedLocations = 'UGLY'


        when:
        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        then:
        // Verification:
        assertFalse(command.validate())
        assertNotNull("Collections are never null", command.selectedFolder)
        assertNotNull("Collections are never null", command.selectedPages)
        assertNotNull("Collections are never null", command.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", command.selectedBrowsers)
        assertNotNull("Collections are never null", command.selectedLocations)

        assertTrue("Invalid data -> no elements in Collection", command.selectedPages.isEmpty())
        assertTrue("Invalid data -> no elements in Collection", command.selectedLocations.isEmpty())
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedPage_isEmpty_for_RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES() {
        given:
        setDefaultParams()
        params.selectedPages = []

        when:
        controllerUnderTest.bindData(command, params)

        then:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedPage_isEmpty_for_WEEKLY_PAGE() {
        given:
        setDefaultParams()
        params.aggrGroup = AggregatorType.PAGE.toString()
        params.selectedPages = []

        when:
        controllerUnderTest.bindData(command, params)

        then:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedMeasuredEvents_isEmpty_for_RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES() {
        given:
        setDefaultParams()
        params.selectedMeasuredEventIds = []

        when:
        controllerUnderTest.bindData(command, params)

        then:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedBrowsers_isEmpty_for_RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES() {
        given:
        setDefaultParams()
        params.selectedBrowsers = []

        when:
        controllerUnderTest.bindData(command, params)
        then:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedLocations_isEmpty_for_RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES() {
        given:
        setDefaultParams()
        params.selectedLocations = []

        when:
        controllerUnderTest.bindData(command, params)
        then:
        assertFalse(command.validate())
    }

    void testConstructViewDataMap() {
        given:
        Page page1 = new Page(name: 'Page1', weight: 0) {
            public Long getId() { return 1L; }
        };
        Page page2 = new Page(name: 'Page2', weight: 0.25d) {
            public Long getId() { return 2L; }
        };
        Page page3 = new Page(name: 'Page3', weight: 0.5d) {
            public Long getId() { return 3L; }
        };

        MeasuredEvent measuredEvent1 = new MeasuredEvent(name: 'MeasuredEvent1', testedPage: page3) {
            public Long getId() { return 1001L; }
        };
        MeasuredEvent measuredEvent2 = new MeasuredEvent(name: 'MeasuredEvent2', testedPage: page2) {
            public Long getId() { return 1002L; }
        };
        MeasuredEvent measuredEvent3 = new MeasuredEvent(name: 'MeasuredEvent3', testedPage: page1) {
            public Long getId() { return 1003L; }
        };
        MeasuredEvent measuredEvent4 = new MeasuredEvent(name: 'MeasuredEvent4', testedPage: page2) {
            public Long getId() { return 1004L; }
        };

        Browser browser1 = new Browser(name: 'Browser1') {
            public Long getId() { return 11L; }
        };

        Location location1 = new Location(label: 'Location1', browser: browser1) {
            public Long getId() { return 101L; }
        };
        Location location2 = new Location(label: 'Location2', browser: browser1) {
            public Long getId() { return 102L; }
        };
        Location location3 = new Location(label: 'Location3', browser: browser1) {
            public Long getId() { return 103L; }
        };

        controllerUnderTest.eventResultDashboardService.getAllJobGroups() >> {
            return [new JobGroup(name: 'Group2'),
                    new JobGroup(name: 'Group1')]
        };
        controllerUnderTest.eventResultDashboardService.getAllPages() >> {
            return [page1, page2, page3]
        };
        controllerUnderTest.eventResultDashboardService.getAllMeasuredEvents() >> {
            return [measuredEvent3, measuredEvent1, measuredEvent2, measuredEvent4]
        };
        controllerUnderTest.eventResultDashboardService.getAllBrowser() >> {
            return [browser1]
        };
        controllerUnderTest.eventResultDashboardService.getAllLocations() >> {
            return [location1, location2, location3]
        };

        when:
        // Run the test:
        Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll();

        then:
        // Verify result (lists should be sorted by UI visible name or label):
        assertNotNull(result);
        assertEquals(17, result.size());

        // AggregatorType
        assertTrue(result.containsKey('aggrGroupLabels'))
        List<String> aggrGroupLabels = result.get('aggrGroupLabels');
        assertEquals(EventResultDashboardController.AGGREGATOR_GROUP_LABELS, aggrGroupLabels)
        //		assertEquals(2, aggrGroupLabels.size())
        //		assertEquals('AT-1', aggrGroupLabels.get(0))
        //		assertEquals('AT-2', aggrGroupLabels.get(1))

        assertTrue(result.containsKey('aggrGroupValuesCached'))
        assertTrue(result.containsKey('aggrGroupValuesUnCached'))

        // CSI-groups
        assertTrue(result.containsKey('folders'))
        List<JobGroup> csiGroups = result.get('folders');
        assertEquals(2, csiGroups.size())
        assertEquals('Group2', csiGroups.get(0).getName())
        assertEquals('Group1', csiGroups.get(1).getName())

        // Pages
        assertTrue(result.containsKey('pages'))
        List<Page> pages = result.get('pages');
        assertEquals(3, pages.size())
        assertEquals('Page1', pages.get(0).getName())
        assertEquals('Page2', pages.get(1).getName())
        assertEquals('Page3', pages.get(2).getName())

        // MeasuredEvents
        assertTrue(result.containsKey('measuredEvents'))
        List<MeasuredEvent> measuredEvents = result.get('measuredEvents');
        assertEquals(4, measuredEvents.size())
        assertEquals('MeasuredEvent3', measuredEvents.get(0).getName())
        assertEquals('MeasuredEvent1', measuredEvents.get(1).getName())
        assertEquals('MeasuredEvent2', measuredEvents.get(2).getName())
        assertEquals('MeasuredEvent4', measuredEvents.get(3).getName())

        // Browsers
        assertTrue(result.containsKey('browsers'))
        List<Browser> browsers = result.get('browsers');
        assertEquals(1, browsers.size())
        assertEquals('Browser1', browsers.get(0).getName())

        // Locations
        assertTrue(result.containsKey('locations'))
        List<Location> locations = result.get('locations');
        assertEquals(3, locations.size())
        assertEquals('Location1', locations.get(0).getLabel())
        assertEquals('Location2', locations.get(1).getLabel())
        assertEquals('Location3', locations.get(2).getLabel())

        // Data for java-script utilities:
        assertTrue(result.containsKey('dateFormat'))
        assertEquals(EventResultDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART, result.get('dateFormat'))
        assertTrue(result.containsKey('weekStart'))
        assertEquals(EventResultDashboardController.MONDAY_WEEKSTART, result.get('weekStart'))

        // --- Map<PageID, Set<MeasuredEventID>>
        Map<Long, Set<Long>> eventsOfPages = result.get('eventsOfPages')
        assertNotNull(eventsOfPages)

        Set<Long> eventsOfPage1 = eventsOfPages.get(1L)
        assertNotNull(eventsOfPage1)
        assertEquals(1, eventsOfPage1.size());
        assertTrue(eventsOfPage1.contains(1003L));

        Set<Long> eventsOfPage2 = eventsOfPages.get(2L)
        assertNotNull(eventsOfPage2)
        assertEquals(2, eventsOfPage2.size());
        assertTrue(eventsOfPage2.contains(1002L));
        assertTrue(eventsOfPage2.contains(1004L));

        Set<Long> eventsOfPage3 = eventsOfPages.get(3L)
        assertNotNull(eventsOfPage3)
        assertEquals(1, eventsOfPage3.size());
        assertTrue(eventsOfPage3.contains(1001L));

        // --- Map<BrowserID, Set<LocationID>>
        Map<Long, Set<Long>> locationsOfBrowsers = result.get('locationsOfBrowsers')
        assertNotNull(locationsOfBrowsers)

        Set<Long> locationsOfBrowser1 = locationsOfBrowsers.get(11L)
        assertNotNull(locationsOfBrowser1)
        assertEquals(3, locationsOfBrowser1.size());
        assertTrue(locationsOfBrowser1.contains(101L));
        assertTrue(locationsOfBrowser1.contains(102L));
        assertTrue(locationsOfBrowser1.contains(103L));
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_testCopyRequestDataToViewModelMap() {
        given:
        // Create and fill a command:
        // form = '18.08.2013'
        Date expectedFromDate = new Date(1376776800000L)
        command.from = expectedFromDate
        command.fromHour = "12:00"
        Date expectedToDate = new Date(1376863200000L)
        command.to = expectedToDate
        command.toHour = "13:00"
        command.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedBrowsers = [2L]
        command.selectedLocations = [17L]
        command.selectedAllBrowsers = false
        command.selectedAllLocations = false
        command.selectedAllMeasuredEvents = false
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_LOAD_TIME]
        command.debug = false
        command.setFromHour = false
        command.setToHour = false
        command.selectedConnectivities = [CUSTOM_CONNECTIVITY_NAME]
        command.selectedAllConnectivityProfiles = true
        command.showDataMarkers = false

        command.showDataLabels = false
        command.selectedInterval = 0
        command.selectChartType = 0
        command.trimBelowLoadTimes = 0
        command.trimAboveLoadTimes = 0
        command.trimBelowRequestCounts = 0
        command.trimAboveRequestCounts = 0
        command.trimBelowRequestSizes = 0
        command.trimAboveRequestSizes = 0

        command.setFromHour = false
        command.setToHour = false
        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0

        // Do we fill all fields?
        assertTrue(command.validate())

        // Run the test:
        when:
        Map<String, Object> dataUnderTest = new HashMap<String, Object>();
        command.copyRequestDataToViewModelMap(dataUnderTest);

        then:
        // Verification:
        assertEquals(40, dataUnderTest.size());

        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPages', [1L, 5L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', [7L, 8L, 9L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

        assertContainedAndNotNullAndEquals(dataUnderTest, 'from', '18.08.2013');
        assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', '12:00');

        assertContainedAndNotNullAndEquals(dataUnderTest, 'to', '19.08.2013');
        assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', '13:00');
        assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedConnectivities', [CUSTOM_CONNECTIVITY_NAME]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllConnectivityProfiles', true);
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_testCopyRequestDataToViewModelMap_defaultsForMissingValues() {
        given:
        // Create and fill a command:
        // form = '18.08.2013'
        Date expectedFromDate = new Date(1376776800000L)
        command.from = expectedFromDate
        command.fromHour = null // Missing!
        Date expectedToDate = new Date(1376863200000L)
        command.to = expectedToDate
        command.toHour = null // Missing!
        command.aggrGroup = null // Missing!
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedBrowsers = [2L]
        command.selectedLocations = [17L]
        command.selectedAllBrowsers = false
        command.selectedAllLocations = false
        command.selectedAllMeasuredEvents = false
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_LOAD_TIME]
        command.debug = false
        command.setFromHour = false
        command.setToHour = false
        command.selectedConnectivities = [CUSTOM_CONNECTIVITY_NAME]
        command.showDataMarkers = false

        command.showDataLabels = false
        command.selectedInterval = 0
        command.selectChartType = 0
        command.trimBelowLoadTimes = 0
        command.trimAboveLoadTimes = 0
        command.trimBelowRequestCounts = 0
        command.trimAboveRequestCounts = 0
        command.trimBelowRequestSizes = 0
        command.trimAboveRequestSizes = 0
        command.setFromHour = false
        command.setToHour = false
        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0

        // Run the test:
        when:
        Map<String, Object> dataUnderTest = new HashMap<String, Object>();
        command.copyRequestDataToViewModelMap(dataUnderTest);

        then:
        // Verification:
        assertEquals(38, dataUnderTest.size());

        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPages', [1L, 5L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', [7L, 8L, 9L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

        assertContainedAndNotNullAndEquals(dataUnderTest, 'from', '18.08.2013');

        assertContainedAndNotNullAndEquals(dataUnderTest, 'to', '19.08.2013');
        assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', false);
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_testCopyRequestDataToViewModelMap_selectAllSelection() {
        given:
        // Create and fill a command:
        // form = '18.08.2013'
        Date expectedFromDate = new Date(1376776800000L)
        command.from = expectedFromDate
        command.fromHour = "12:00"
        // to = '19.08.2013'
        Date expectedToDate = new Date(1376863200000L)
        command.to = expectedToDate
        command.toHour = "13:00"
        command.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = 'on'
        command.selectedMeasuredEventIds = []
        command.selectedBrowsers = [2L]
        command.selectedLocations = [17L]
        command.selectedAllBrowsers = false
        command.selectedAllLocations = false
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_LOAD_TIME]
        command.debug = true
        command.setFromHour = false
        command.setToHour = false
        command.selectedConnectivities = [CUSTOM_CONNECTIVITY_NAME]
        command.selectedAllConnectivityProfiles = true
        command.showDataMarkers = false

        command.showDataLabels = false
        command.selectedInterval = 0
        command.selectChartType = 0
        command.trimBelowLoadTimes = 0
        command.trimAboveLoadTimes = 0
        command.trimBelowRequestCounts = 0
        command.trimAboveRequestCounts = 0
        command.trimBelowRequestSizes = 0
        command.trimAboveRequestSizes = 0
        command.setFromHour = false
        command.setToHour = false
        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0

        // Do we fill all fields?
        assertTrue(command.validate())

        when:
        // Run the test:
        Map<String, Object> dataUnderTest = new HashMap<String, Object>();
        command.copyRequestDataToViewModelMap(dataUnderTest);

        then:
        // Verification:
        assertEquals(40, dataUnderTest.size());

        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPages', [1L, 5L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', true);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', []);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

        assertContainedAndNotNullAndEquals(dataUnderTest, 'from', '18.08.2013');
        assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', "12:00");

        assertContainedAndNotNullAndEquals(dataUnderTest, 'to', '19.08.2013');
        assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', "13:00");
        assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', true);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedConnectivities', [CUSTOM_CONNECTIVITY_NAME]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllConnectivityProfiles', true);
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_testCreateMvQueryParams() {
        given:
        // form = '18.08.2013'
        command.from = new Date(1376776800000L)
        command.fromHour = "12:00"
        // to = '19.08.2013'
        command.to = new Date(1376863200000L)
        command.toHour = "13:00"
        command.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedBrowsers = [2L]
        command.selectedLocations = [17L]
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES]
        command.selectedAllMeasuredEvents = false
        command.selectedAllBrowsers = false
        command.selectedAllLocations = false
        command.selectedTimeFrameInterval = 0
        command.showDataMarkers = false
        command.showDataLabels = false
        command.selectedInterval = 0
        command.selectChartType = 0
        command.trimBelowLoadTimes = 0
        command.trimAboveLoadTimes = 0
        command.trimBelowRequestCounts = 0
        command.trimAboveRequestCounts = 0
        command.trimBelowRequestSizes = 0
        command.trimAboveRequestSizes = 0
        command.setFromHour = false
        command.setToHour = false
        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0
        command.debug = false

        when:
        // Do we fill all fields?
        assertTrue(command.validate())

        then:
        // Run the test:
        MvQueryParams mvQueryParams = command.createMvQueryParams();

        // Verification:
        assertNotNull(mvQueryParams);
        assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
        assertEquals([1L, 5L] as SortedSet, mvQueryParams.pageIds);
        assertEquals([7L, 8L, 9L] as SortedSet, mvQueryParams.measuredEventIds);
        assertEquals([2L] as SortedSet, mvQueryParams.browserIds);
        assertEquals([17L] as SortedSet, mvQueryParams.locationIds);
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_testCreateMvQueryParams_SelectAllIgnoresRealSelection_MeasuredEvents() {
        given:
        // form = '18.08.2013'
        command.from = new Date(1376776800000L)
        command.fromHour = "12:00"
        // to = '19.08.2013'
        command.to = new Date(1376863200000L)
        command.toHour = "13:00"
        command.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = 'on';
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedBrowsers = [2L]
        command.selectedLocations = [17L]
        command.selectedAllBrowsers = false
        command.selectedAllLocations = false
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_LOAD_TIME]
        command.showDataMarkers = false

        command.showDataLabels = false
        command.selectedInterval = 0
        command.selectChartType = 0
        command.trimBelowLoadTimes = 0
        command.trimAboveLoadTimes = 0
        command.trimBelowRequestCounts = 0
        command.trimAboveRequestCounts = 0
        command.trimBelowRequestSizes = 0
        command.trimAboveRequestSizes = 0
        command.setFromHour = false
        command.setToHour = false
        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0
        command.debug = false

        // Do we fill all fields?
        assertTrue(command.validate())
        when:
        // Run the test:
        MvQueryParams mvQueryParams = command.createMvQueryParams();
        then:
        // Verification:
        assertNotNull(mvQueryParams);
        assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
        assertEquals([1L, 5L] as SortedSet, mvQueryParams.pageIds);
        assertEquals("This set is empty which means to fit all",
                [] as SortedSet, mvQueryParams.measuredEventIds);
        assertEquals([2L] as SortedSet, mvQueryParams.browserIds);
        assertEquals([17L] as SortedSet, mvQueryParams.locationIds);
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_testCreateMvQueryParams_SelectAllIgnoresRealSelection_Browsers() {
        given:
        // form = '18.08.2013'
        command.from = new Date(1376776800000L)
        command.fromHour = "12:00"
        // to = '19.08.2013'
        command.to = new Date(1376863200000L)
        command.toHour = "13:00"
        command.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedAllBrowsers = true;
        command.selectedBrowsers = [2L]
        command.selectedLocations = [17L]
        command.selectedAllLocations = false
        command.selectedAllMeasuredEvents = false
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_LOAD_TIME]
        command.showDataMarkers = false

        command.showDataLabels = false
        command.selectedInterval = 0
        command.selectChartType = 0
        command.trimBelowLoadTimes = 0
        command.trimAboveLoadTimes = 0
        command.trimBelowRequestCounts = 0
        command.trimAboveRequestCounts = 0
        command.trimBelowRequestSizes = 0
        command.trimAboveRequestSizes = 0
        command.setFromHour = false
        command.setToHour = false
        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0
        command.debug = false

        // Do we fill all fields?
        assertTrue(command.validate())

        when:
        // Run the test:
        MvQueryParams mvQueryParams = command.createMvQueryParams();
        then:
        // Verification:
        assertNotNull(mvQueryParams);
        assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
        assertEquals([1L, 5L] as SortedSet, mvQueryParams.pageIds);
        assertEquals([7L, 8L, 9L] as SortedSet, mvQueryParams.measuredEventIds);
        assertEquals("This set is empty which means to fit all",
                [] as SortedSet, mvQueryParams.browserIds);
        assertEquals([17L] as SortedSet, mvQueryParams.locationIds);
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_testCreateMvQueryParams_SelectAllIgnoresRealSelection_Locations() {
        given:
        // form = '18.08.2013'
        command.from = new Date(1376776800000L)
        command.fromHour = "12:00"
        // to = '19.08.2013'
        command.to = new Date(1376863200000L)
        command.toHour = "13:00"
        command.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = true;
        command.selectedLocations = [17L]
        command.selectedAllBrowsers = false
        command.selectedAllMeasuredEvents = false
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_LOAD_TIME]
        command.showDataMarkers = false

        command.showDataLabels = false
        command.selectedInterval = 0
        command.selectChartType = 0
        command.trimBelowLoadTimes = 0
        command.trimAboveLoadTimes = 0
        command.trimBelowRequestCounts = 0
        command.trimAboveRequestCounts = 0
        command.trimBelowRequestSizes = 0
        command.trimAboveRequestSizes = 0
        command.setFromHour = false
        command.setToHour = false
        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0
        command.debug = false

        // Do we fill all fields?
        assertTrue(command.validate())
        when:
        // Run the test:
        MvQueryParams mvQueryParams = command.createMvQueryParams();
        then:
        // Verification:
        assertNotNull(mvQueryParams);
        assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
        assertEquals([1L, 5L] as SortedSet, mvQueryParams.pageIds);
        assertEquals([7L, 8L, 9L] as SortedSet, mvQueryParams.measuredEventIds);
        assertEquals([2L] as SortedSet, mvQueryParams.browserIds);
        assertEquals("This set is empty which means to fit all",
                [] as SortedSet, mvQueryParams.locationIds);
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_testCreateMvQueryParams_() {
        given:
        // form = '18.08.2013'
        command.from = new Date(1376776800000L)
        command.fromHour = "12:00"
        // to = '19.08.2013'
        command.to = new Date(1376863200000L)
        command.toHour = "13:00"
        command.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedBrowsers = [2L]
        command.selectedLocations = [17L]
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES]
        command.selectedAllMeasuredEvents = false
        command.selectedAllBrowsers = false
        command.selectedAllLocations = false
        command.selectedTimeFrameInterval = 0
        command.selectedAllConnectivityProfiles = true
        command.showDataMarkers = false

        command.showDataLabels = false
        command.selectedInterval = 0
        command.selectChartType = 0
        command.trimBelowLoadTimes = 0
        command.trimAboveLoadTimes = 0
        command.trimBelowRequestCounts = 0
        command.trimAboveRequestCounts = 0
        command.trimBelowRequestSizes = 0
        command.trimAboveRequestSizes = 0
        command.setFromHour = false
        command.setToHour = false
        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0
        command.debug = false

        // Do we fill all fields?
        assertTrue(command.validate())
        when:
        // Run the test:
        MvQueryParams mvQueryParams = command.createMvQueryParams();
        then:
        // Verification:
        assertNotNull(mvQueryParams);
        assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
    }

    /**
     * Test for inner class {@link EventResultDashboardShowAllCommand}.
     */
    void testShowAllCommand_testCreateMvQueryParams_invalidCommand() {
        given: "an invalid command"
        assertFalse(command.validate())

        when:
        command.createMvQueryParams();

        then: "an exception is thrown"
        thrown IllegalStateException
    }

    /**
     * <p>
     * Asserts that a value is contained in a {@link Map}, that the value is
     * not <code>null</code> and is equals to specified expected value.
     * </p>
     *
     * @param dataUnderTest
     *         The map which contents is to be checked, not <code>null</code>.
     * @param key
     *         The key to which the value to check is bound,
     *         not <code>null</code>.
     * @param expectedValue
     *         The expected value to be equals to according to
     * {@link Object#equals(Object)}; not <code>null</code>.
     * @throws AssertionError
     *         if at least one of the conditions are not satisfied.
     */
    private
    static void assertContainedAndNotNullAndEquals(Map<String, Object> dataUnderTest, String key, Object expectedValue) throws AssertionError {
        assertNotNull('dataUnderTest', dataUnderTest)
        assertNotNull('key', key)
        assertNotNull('expectedValue', expectedValue)

        assertTrue('Map must contain key \"' + key + '\"', dataUnderTest.containsKey(key))
        assertNotNull('Map must contain a not-null value for key \"' + key + '\"', dataUnderTest.get(key))
        assertEquals(expectedValue, dataUnderTest.get(key))
    }

    private setDefaultParams() {
        params.from = '2013-08-18T16:00:00.000Z'
        params.to = '2013-08-18T18:00:00.000Z'
        params.selectedAggrGroupValuesUnCached = AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'
        params.showDataMarkers = false
        params.showDataLabels = false
        params.selectedInterval = 0
        params.selectedTimeFrameInterval = 0
        params.selectChartType = 0
        params.trimBelowLoadTimes = 0
        params.trimAboveLoadTimes = 0
        params.trimBelowRequestCounts = 0
        params.trimAboveRequestCounts = 0
        params.trimBelowRequestSizes = 0
        params.trimAboveRequestSizes = 0
        params.chartWidth = 0
        params.chartHeight = 0
        params.loadTimeMinimum = 0
    }
}
