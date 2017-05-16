/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.script.Script
import grails.plugins.taggable.Taggable
import grails.util.Environment

import org.grails.databinding.BindUsing
import org.quartz.CronExpression

/**
 * <p>
 * A web page test job definition.
 * </p>
 *
 * @see Script
 */
class Job implements Taggable {

    def jobProcessingService

    Long id;

    String label;
    Script script;
    Location location;
    Date lastRun;

    @BindUsing({ obj, source -> source['description'] })
    String description = "";
    int runs = 1;

    boolean active;
    boolean firstViewOnly = true
    boolean captureVideo = true
    boolean persistNonMedianResults = true;

    /**
     * If a job can't be deleted due to existing csiAggregations, it's marked as deleted
     */
    boolean deleted = false

    /**
     * Stop Test at Document Complete.
     */
    boolean web10
    /**
     * Disable Javascript
     */
    boolean noscript
    /**
     * Clear SSL Certificate Caches
     */
    boolean clearcerts
    /**
     * Ignore SSL Certificate Errors
     * e.g. Name mismatch, Self-signed certificates, etc.
     */
    boolean ignoreSSL
    /**
     * Disable Compatibility View (IE Only)
     * Forces all pages to load in standards mode
     */
    boolean standards
    /**
     *  Capture network packet trace (tcpdump)
     */
    boolean tcpdump
    /**
     * Save response bodies (For text resources)
     */
    boolean bodies
    /**
     * Continuous Video Capture
     * Unstable/experimental, may cause tests to fail
     */
    boolean continuousVideo
    /**
     * Preserve original User Agent string
     * Do not add PTST to the browser UA string
     */
    boolean keepua

    /**
     * True if measurement should happen without any traffic shaping.
     * Corresponds with selection 'Native (No Traffic Shaping)' in job creation.
     */
    boolean noTrafficShapingAtAll

    /**
     * Pre-configured {@link ConnectivityProfile} associated with this Job.
     */
    ConnectivityProfile connectivityProfile

    /**
     * Whether or not custom values for bandwidthDown, bandwidthUp, latency and packetLoss are set for this job.
     * If true, {@link #bandwidthDown}, {@link #bandwidthUp}, {@link #latency} and {@link #packetLoss} should be set.
     * If false an  {@link #connectivityProfile} should exist for this job.
     */
    boolean customConnectivityProfile

    /**
     * Name of the custom connectivity. Should only be non null value if {@link #customConnectivityProfile} is true.
     */
    String customConnectivityName

    /**
     * Bandwidth to set for downlink.
     * @see https://sites.google.com/a/webpagetest.org/docs/advanced-features/webpagetest-restful-apis
     */
    Integer bandwidthDown
    /**
     * Bandwidth to set for uplink.
     * @see https://sites.google.com/a/webpagetest.org/docs/advanced-features/webpagetest-restful-apis
     */
    Integer bandwidthUp
    /**
     * TCP-Latency to set for the measurement.
     * @see https://sites.google.com/a/webpagetest.org/docs/advanced-features/webpagetest-restful-apis
     */
    Integer latency
    /**
     * Packet loss rate to set for the measurement.
     * @see https://sites.google.com/a/webpagetest.org/docs/advanced-features/webpagetest-restful-apis
     */
    Integer packetLoss

    boolean option_isPrivate
    String  option_block
    boolean option_noopt
    boolean option_noimages
    boolean option_noheaders
    boolean option_pngss
    Integer option_iq
    boolean option_mobile
    String  option_uastring
    boolean option_customDimensions
    Integer option_width
    Integer option_height
    boolean option_customBrowserDimensions
    Integer option_browser_width
    Integer option_browser_height
    Integer option_dpr
    boolean option_mv
    String  option_medianMetric
    String  option_cmdline
    boolean option_htmlbody
    String  option_custom
    String  option_tester
    String  option_affinity
    boolean option_timeline
    Integer option_timelineStack
    String  option_mobileDevice
    String  option_appendua
    boolean option_lighthouse
    String  option_type
    String  option_customHeaders
    boolean option_trace

    /**
     * @deprecated Use executionSchedule instead
     */
    @Deprecated
    Integer frequencyInMin;
    Integer maxDownloadTimeInMinutes

    /**
     * Set to label of this job's script if script
     * contains no setEventName statements.
     */
    String eventNameIfUnknown;

    Map<String, String> variables

    /**
     * A cron string
     */
    String executionSchedule;

    boolean provideAuthenticateInformation
    String authUsername
    String authPassword

    /**
     * The {@link JobGroup} this job is assigned to <em>or</em>
     */
    JobGroup jobGroup;
    static belongsTo = [jobGroup: JobGroup];

    JobStatistic jobStatistic

    static transients = ['nextExecutionTime']

    static constraints = {
        label(maxSize: 255, blank: false, unique: true)
        script(nullable: true, blank:false)
        location(nullable: false)
        lastRun(nullable: true)
        jobGroup(nullable: false)

        description(widget: 'textarea', maxSize: 255)
        runs(range: 1..25)

        firstViewOnly(nullable: true)
        captureVideo(nullable: true)
        persistNonMedianResults(nullable: false)

        web10(nullable: true)
        noscript(nullable: true)
        clearcerts(nullable: true)
        ignoreSSL(nullable: true)
        standards(nullable: true)
        tcpdump(nullable: true)
        bodies(nullable: true)
        continuousVideo(nullable: true)
        keepua(nullable: true)

        connectivityProfile(nullable: true, validator: { profile, instance ->

            boolean notNull = profile != null
            boolean nullAndCustom =
                    profile == null &&
                            instance.customConnectivityProfile == true &&
                            instance.noTrafficShapingAtAll == false &&
                            instance.bandwidthDown != null && instance.bandwidthUp != null && instance.latency != null && instance.packetLoss != null
            boolean nullAndNative =
                    profile == null &&
                            instance.customConnectivityProfile == false &&
                            instance.noTrafficShapingAtAll == true

            return notNull || nullAndCustom || nullAndNative;

        })
        customConnectivityName(nullable: true, validator: { connName, instance ->

            boolean notNull = connName != null
            boolean nullAndNotCustom = connName == null && instance.customConnectivityProfile == false

            return notNull || nullAndNotCustom

        })
        bandwidthDown(nullable: true, min: -2147483648, max: 2147483647)
        bandwidthUp(nullable: true, min: -2147483648, max: 2147483647)
        latency(nullable: true, min: -2147483648, max: 2147483647)
        packetLoss(nullable: true, min: -2147483648, max: 2147483647)
        frequencyInMin(nullable: true, min: -2147483648, max: 2147483647)
        maxDownloadTimeInMinutes(range: 10..240)
        eventNameIfUnknown(nullable: true, maxSize: 255)
        variables(nullable: true)

        // if an executionSchedule is set, make sure that it is a valid Cron expression
        executionSchedule(nullable: true, validator: {
            if ((it != null) && (!(CronExpression.isValidExpression(it)))) {
                return ['executionScheduleInvalid']
            }
        })
        // Job may only be active if it has an executionSchedule
        active(nullable: false, validator: { active, obj ->
            if (active && !obj.executionSchedule) {
                return ['executionScheduleMissing']
            }
        })

        provideAuthenticateInformation(nullable: false)
        authUsername(nullable: true, maxSize: 255)
        authPassword(nullable: true, maxSize: 255, password: true)
        jobStatistic(nullable: true)

        //option_isPrivate(nullable: true)
        option_block(nullable: true)
        //option_authType(nullable: true)
        //option_noopt(nullable: true)
        //option_noimages(nullable: true)
        //option_noheaders(nullable: true)
        //option_pngss(nullable: true)
        option_iq(nullable: true)
        //option_mobile(nullable: true)
        option_uastring(nullable: true)
        option_width(nullable: true)
        option_height(nullable: true)
        option_browser_width(nullable: true)
        option_browser_height(nullable: true)
        option_dpr(nullable: true)
        //option_mv(nullable: true)
        option_medianMetric(nullable: true)
        option_cmdline(nullable: true)
        //option_htmlbody(nullable: true)
        option_custom(nullable: true)
        option_tester(nullable: true)
        option_affinity(nullable: true)
        //option_timeline(nullable: true)
        option_timelineStack(nullable: true)
        option_mobileDevice(nullable: true)
        option_appendua(nullable: true)
        //option_lighthouse(nullable: true)
        option_type(nullable: true)
        option_customHeaders(nullable: true)
    }

    static mapping = {
        sort 'label': 'asc'
        noTrafficShapingAtAll defaultValue: false
        customConnectivityProfile defaultValue: false
        persistNonMedianResults defaultValue: '1'
        label(index: 'label_idx')
    }

    def beforeValidate() {
        description = description?.trim()
    }

    String toString() {
        label
    }

    Date getNextExecutionTime() {
        return executionSchedule ? CronExpressionFormatter.getNextValidTimeAfter(new CronExpression(executionSchedule), new Date()) : null
    }

    private void performQuartzScheduling(boolean start) {
        if (Environment.getCurrent() != Environment.TEST) {
            if (start) {
                jobProcessingService.scheduleJob(this)
            } else {
                jobProcessingService.unscheduleJob(this)
            }
        }
    }

    def afterInsert() {
        performQuartzScheduling(active)
    }

    def afterUpdate() {
        performQuartzScheduling(active)
    }

    def beforeDelete() {
        performQuartzScheduling(false)
    }
}
