package de.iteratec.osm.measurement.environment
import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.WptStatus
import de.iteratec.osm.system.LocationHealthCheck
import de.iteratec.osm.system.LocationHealthCheckDaoService
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable

class QueueDashboardController {

    LocationHealthCheckDaoService locationHealthCheckDaoService
    QueueAndJobStatusService queueAndJobStatusService

    def index() { }

    def getActiveWptServer()
    {
        List<WebPageTestServer> list = WebPageTestServer.findAllByActive(true)
        return ControllerUtils.sendObjectAsJSON(response, list )
    }

    @RestAction
    def getWptServerInformation(Long id)
    {
        if(id == null) {
            return emptyResponse()
        }
        WebPageTestServer wptserver = WebPageTestServer.findById(id)

        if(wptserver != null){
            List<Location> listLocation = Location.findAllByActiveAndWptServer(true, wptserver)
            List<LocationHealthCheck> healthChecks = locationHealthCheckDaoService.getLatestHealthChecksFor(listLocation)
            List<Map> listLocationInfo = new ArrayList<Map>()

            listLocation.forEach( {Location location ->

                List<JobResult> executingJobResults
                executingJobResults = queueAndJobStatusService.getExecutingJobResults(location)

                Map<Job, List<JobResult>> executingJobs
                executingJobs = queueAndJobStatusService.aggregateJobs(executingJobResults)

                LocationHealthCheck healthCheck = healthChecks.findAll{ it.location == location }[0]

                DefaultQueueDashboardCommand command = new DefaultQueueDashboardCommand()
                command.location = location
                command.healthCheck = healthCheck
                command.executingJobResults = executingJobResults
                command.executingJobs = executingJobs
                Map map = buildMap(command)
                listLocationInfo.add(map)
            } )
            return ControllerUtils.sendObjectAsJSON(response, listLocationInfo)
        }
        return emptyResponse()
    }

    Map buildMap(DefaultQueueDashboardCommand command){
        return [
                id                  : command?.location?.uniqueIdentifierForServer,
                lastHealthCheckDate : command?.healthCheck?.date?.toString(),
                label               : command?.location?.location,
                agents              : command?.healthCheck?.numberOfAgents,
                jobs                : command?.healthCheck?.numberOfPendingJobsInWpt,
                eventResultsLastHour: command?.healthCheck?.numberOfEventResultsLastHour,
                jobResultsLastHour  : command?.healthCheck?.numberOfJobResultsLastHour,
                errorsLastHour      : command?.healthCheck?.numberOfErrorsLastHour,
                jobsNextHour        : command?.healthCheck?.numberOfJobResultsNextHour,
                eventsNextHour      : command?.healthCheck?.numberOfEventResultsNextHour,
                executingJobs       : command?.executingJobs?.values(),
                pendingJobs         : command?.executingJobResults?.findAll {
                it.httpStatusCode == WptStatus.PENDING.getWptStatusCode() }?.size(),
                runningJobs         : command?.executingJobResults?.findAll {
                it.httpStatusCode == WptStatus.RUNNING.getWptStatusCode() }?.size()
        ]
    }

    def emptyResponse()
    {
        response.status = 404
    }
}

class DefaultQueueDashboardCommand implements Validateable {
    Location location
    LocationHealthCheck healthCheck
    List<JobResult> executingJobResults
    Map<Job, List<JobResult>> executingJobs

    static constrains = {
        location(nullable: true)
        healthCheck(nullable: true)
        executingJobResults(nullable: true)
        executingJobs(nullable: true)
    }
}