package jmri.jmrit.operations.automation;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2016
 * 
 *
 */
public class Xml {

    private Xml(){
        //class of constants
    }

    // Common to operation xml files
    protected static final String ID = "id"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String COMMENT = "comment"; // NOI18N
    protected static final String TRUE = "true"; // NOI18N
    protected static final String FALSE = "false"; // NOI18N
    
    protected static final String AUTOMATION = "automation"; // NOI18N

    protected static final String AUTOMATIONS = "automations"; // NOI18N
    
    protected static final String AUTOMATION_OPTIONS = "automationOptions"; // NOI18N
    protected static final String AUTOMATION_STARTUP_ID = "automationStartupId"; // NOI18N
    
    // Automation.java
    protected static final String CURRENT_ITEM = "currentItem"; // NOI18N
    

    // AutomationItem.java
    protected static final String ITEM = "item"; // NOI18N
    protected static final String SEQUENCE_ID = "sequenceId"; // NOI18N
    protected static final String ACTION_CODE = "actionCode"; // NOI18N
    protected static final String TRAIN_ID = "trainId"; // NOI18N
    protected static final String ROUTE_LOCATION_ID = "routeLocationId"; // NOI18N
    protected static final String AUTOMATION_ID = "automationId"; // NOI18N
    protected static final String GOTO_AUTOMATION_ID = "gotoAutomationId"; // NOI18N
    protected static final String GOTO_AUTOMATION_BRANCHED = "gotoBranched"; // NOI18N
    protected static final String TRAIN_SCHEDULE_ID = "trainScheduleId"; // NOI18N
    
    protected static final String HALT_FAIL = "haltFail"; // NOI18N
    
    protected static final String ACTION_SUCCESSFUL = "actionSuccessful"; // NOI18N
    protected static final String ACTION_RAN = "actionRan"; // NOI18N
    
    protected static final String MESSAGES = "messages"; // NOI18N
    protected static final String MESSAGE_OK = "messageOk"; // NOI18N
    protected static final String MESSAGE_FAIL = "messageFail"; // NOI18N
    protected static final String MESSAGE = "message"; // NOI18N

}
