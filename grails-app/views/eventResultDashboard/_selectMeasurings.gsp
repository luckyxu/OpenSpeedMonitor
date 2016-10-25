<%@ page defaultCodec="none" %></page>
<%-- 
GSP-Template Mappings:

* folders List<JobGroup>: all Folders for selection 
* selectedFolder List<JobGroup>: all selected folders from past call

* pages List<Page>: all Pages for selection
* selectedPages List<Page>: all selected pages from past call

* measuredEvents List<MeasuredEvent>: see other
* selectedMeasuredEventIds List<MeasuredEvent>: see other
* selectedAllMeasuredEvents :

* browsers
* selectedBrowsers
* selectedAllBrowsers

* locations
* selectedLocations
* selectedAllLocations
 --%>
<div class="col-md-3" id="filter-navtab-jobGroup">
    <g:render template="selectJobGroup" model="['folders': folders, 'selectedFolder': selectedFolder,
                                                'tagToJobGroupNameMap': tagToJobGroupNameMap]" />
</div>

%{--the rest----------------------------------------------------------------------------------------------}%
<div id="filter-complete-tabbable" class="col-md-5 tabbable">
    <g:render template="/eventResultDashboard/selectPageLocation"
              model="['locationsOfBrowsers'             : locationsOfBrowsers,
                      'eventsOfPages'                   : eventsOfPages,
                      'pages'                           : pages,
                      'selectedPages'                   : selectedPages,
                      'measuredEvents'                  : measuredEvents,
                      'selectedAllMeasuredEvents'       : selectedAllMeasuredEvents,
                      'selectedMeasuredEvents'          : selectedMeasuredEvents,
                      'browsers'                        : browsers,
                      'selectedBrowsers'                : selectedBrowsers,
                      'selectedAllBrowsers'             : selectedAllBrowsers,
                      'locations'                       : locations,
                      'selectedLocations'               : selectedLocations,
                      'selectedAllLocations'            : selectedAllLocations,
                      'connectivityProfiles'            : connectivityProfiles,
                      'selectedConnectivityProfiles'    : selectedConnectivityProfiles,
                      'selectedAllConnectivityProfiles' : selectedAllConnectivityProfiles,
                      'showExtendedConnectivitySettings': false]"/>
</div>
