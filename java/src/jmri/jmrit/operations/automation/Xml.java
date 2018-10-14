package jmri.jmrit.operations.automation;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2016
 * 
 *
 */
public class Xml {

    // Common to operation xml files
    static final String ID = "id"; // NOI18N
    static final String NAME = "name"; // NOI18N
    static final String COMMENT = "comment"; // NOI18N
    static final String TRUE = "true"; // NOI18N
    static final String FALSE = "false"; // NOI18N
    
    static final String AUTOMATION = "automation"; // NOI18N

    static final String AUTOMATIONS = "automations"; // NOI18N
    
    // Automation.java
    static final String CURRENT_ITEM = "currentItem"; // NOI18N
    

    // AutomationItem.java
    static final String ITEM = "item"; // NOI18N
    static final String SEQUENCE_ID = "sequenceId"; // NOI18N
    static final String ACTION_CODE = "actionCode"; // NOI18N
    static final String TRAIN_ID = "trainId"; // NOI18N
    static final String ROUTE_LOCATION_ID = "routeLocationId"; // NOI18N
    static final String AUTOMATION_ID = "automationId"; // NOI18N
    static final String GOTO_AUTOMATION_ID = "gotoAutomationId"; // NOI18N
    static final String GOTO_AUTOMATION_BRANCHED = "gotoBranched"; // NOI18N
    static final String TRAIN_SCHEDULE_ID = "trainScheduleId"; // NOI18N
    
    static final String HALT_FAIL = "haltFail"; // NOI18N
    
    static final String ACTION_SUCCESSFUL = "actionSuccessful"; // NOI18N
    static final String ACTION_RAN = "actionRan"; // NOI18N
    
    static final String MESSAGES = "messages"; // NOI18N
    static final String MESSAGE_OK = "messageOk"; // NOI18N
    static final String MESSAGE_FAIL = "messageFail"; // NOI18N
    static final String MESSAGE = "message"; // NOI18N

}
