<!--
This modal is used to create a jobGroup
-->
<div id="jobGroupModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="jobGroupModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="jobGroupModalLabel">
                    <g:message code="default.create.label" args="['JobGroup']" default="Create JobGroup"/>
                </h4>
            </div>

            <div class="modal-body">
                <div>
                    <g:render template="/jobGroup/form"></g:render>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-default" data-dismiss="modal" aria-hidden="true"><g:message
                        code="default.button.cancel.label" default="Cancel"/></button>
                <span class="button">
                    <button type="button" data-dismiss="modal" aria-hidden="true" role="button"
                            id="createJobGroupButton" class="btn btn-primary"
                            onclick="createJobGroup('${g.createLink([controller: 'jobGroup', action: 'createAsync'])}')"><g:message
                            code="default.button.create.label" default="create"/></button>
                </span>
            </div>
        </div>
    </div>
</div>
