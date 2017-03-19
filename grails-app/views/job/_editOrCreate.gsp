<!doctype html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="kickstart_osm"/>
        <title><g:message code="default.${mode}.label" args="[entityDisplayName]"/></title>

        <asset:stylesheet src="job/edit.css"/>

    </head>

    <body>
        <section id="${mode}-job" class="first">
            <h1><g:message code="default.${mode}.label" args="[entityDisplayName]"/></h1>
            <g:render template="messages"/>
            %{--<p>--}%
                %{--<g:message code="default.form.asterisk"/>--}%
            %{--</p>--}%
            <g:form resource="${entity}" method="post" role="form" class="form-horizontal">

                <g:hiddenField name="id" value="${entity?.id}"/>
                <g:hiddenField name="version" value="${entity?.version}"/>

                <div class="card">
                    <fieldset class="form">
                        <g:render template="form"/>
                    </fieldset>
                    <hr>
                    <div class="row">
                        <div class="col-md-6">
                            <g:if test="${mode == 'edit'}">
                                <g:actionSubmit class="btn btn-primary" action="update"
                                                value="${message(code: 'default.button.save.label', default: 'Speichern')}"/>
                                <g:actionSubmit class="btn btn-primary" action="save"
                                                value="${message(code: 'de.iteratec.actions.duplicate', default: 'Kopie speichern')}"
                                                onclick="return promptForDuplicateName();"/>
                            </g:if>
                            <g:elseif test="${mode == 'create'}">
                                <g:actionSubmit class="btn btn-primary" action="save"
                                                value="${message(code: 'default.button.create.label', default: 'Create')}"/>
                            </g:elseif>

                            <a href="<g:createLink action="list"/>" class="btn btn-warning"
                               onclick="return confirm('${message(code: 'default.button.unsavedChanges.confirm.message', default: 'Sind Sie sicher?')}');">
                                <g:message code="default.button.cancel.label" default="Abbrechen"/>
                            </a>

                            <g:if test="${mode == 'edit'}">
                                <g:render template="/_common/modals/deleteSymbolLink" model="[controllerLink: controllerLink]"/>
                            </g:if>
                        </div>
                        <div class="col-md-6 text-right">
                            <g:message code="default.form.asterisk"/>
                        <div>
                    </div>
                </div>
            </g:form>
        </section>
        <content tag="include.bottom">
            <asset:javascript src="prettycron/prettycronManifest.js"/>
            <asset:javascript src="job/edit.js"/>
            <asset:javascript src="timeago/futureOnlyTimeago.js"/>
            <g:if test="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).language.equals('de')}">
                <asset:javascript src="timeago/timeagoDe.js"/>
            </g:if>
            <asset:script type="text/javascript">

                function getExecutionScheduleSetButInactiveLabel() {
                    return '${message(code:'job.executionScheduleSetButInactive.label')}';
                }
                function promptForDuplicateName() {
                    var newName = prompt(
                            OpenSpeedMonitor.i18n.duplicatePrompt,
                            $('input#label').val() + OpenSpeedMonitor.i18n.duplicateSuffix
                    );
                    if (newName != null && newName != '') {
                        $('input#label').val(newName);
                        return true;
                    } else {
                        return false;
                    }
                }

                $(document).ready(function() {

                    doOnDomReady(
                            ${job.label == null},
                            "${g.createLink(action: 'nextExecution')}",
                            '${customConnNameForNative}',
                            ${job.connectivityProfile?job.connectivityProfile.id:'null'},
                            ${job.noTrafficShapingAtAll},
                            "${g.createLink(action: 'tags')}"
                    );

                });
                $( window).load(function(){

                    //for advanced tab: should be included to doOnDomReady() function
                    $('#provideAuthenticateInformation').click(function () {
                        $('.authInfo').toggle($(this).prop('checked'));
                        document.getElementById('authPassword').setAttribute('type', 'password');
                    });
                    $('.authInfo').toggle($('#provideAuthenticateInformation').prop('checked'));
                    if ($('#provideAuthenticateInformation').prop('checked')) {
                        document.getElementById('authPassword').setAttribute('type', 'password');
                    }

                    window.addEventListener("CodeMirrorManifestArrived",function(){
                        var editor = OpenSpeedMonitor.script.codemirrorEditor.init({
                            idCodemirrorElement: "navigationScript",
                            i18nMessage_NO_STEPS_FOUND: '${message(code: 'script.NO_STEPS_FOUND.warning')}',
                            i18nMessage_STEP_NOT_RECORDED: '${message(code: 'script.STEP_NOT_RECORDED.warning')}',
                            i18nMessage_DANGLING_SETEVENTNAME_STATEMENT: '${message(code: 'script.DANGLING_SETEVENTNAME_STATEMENT.warning')}',
                            i18nMessage_MISSING_SETEVENTNAME_STATEMENT: '${message(code: 'script.MISSING_SETEVENTNAME_STATEMENT.warning')}',
                            i18nMessage_WRONG_PAGE: '${message(code: 'script.WRONG_PAGE.error')}',
                            i18nMessage_TOO_MANY_SEPARATORS: '${message(code: 'script.TOO_MANY_SEPARATORS.error')}',
                            i18nMessage_MEASUREDEVENT_NOT_UNIQUE: '${message(code: 'script.MEASUREDEVENT_NOT_UNIQUE.error')}',
                            i18nMessage_WRONG_URL_FORMAT: '${message(code: 'script.WRONG_URL_FORMAT.error')}',
                            measuredEvents: [],
                            linkParseScriptAction: '${createLink(controller: 'script', action: 'parseScript')}',
                            linkMergeDefinedAndUsedPlaceholders: '${createLink(action: 'mergeDefinedAndUsedPlaceholders')}',
                            linkGetScriptSource: '${createLink(action: 'getScriptSource')}',
                            readonly: true,
                            parsedScriptUrl: '${createLink(controller: 'script', action: 'getParsedScript')}'
                        });
                        $('#scriptTabLink').bind("click", function() {
                            editor.update();
                        });
                        $('#script').bind("change", function() {
                            editor.update();
                        });
                        $('#script').change();
                    });
                    OpenSpeedMonitor.postLoader.loadJavascript(
                        '<g:assetPath src="codemirror/codemirrorManifest.js" />',
                        true
                    )
                   OpenSpeedMonitor.postLoader.loadStylesheet(
                        '<g:assetPath src="codemirror/codemirrorManifest.css" />',
                        true
                    )

                });
            </asset:script>
        </content>
    </body>
</html>
