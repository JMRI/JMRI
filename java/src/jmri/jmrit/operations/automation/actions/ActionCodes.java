package jmri.jmrit.operations.automation.actions;

/**
 * Action codes for automation
 *
 * @author Daniel Boudreau Copyright (C) 2016
 * 
 *
 */
public class ActionCodes {

    // lower byte used in the construction of action codes
    public static final int ENABLE_TRAINS = 0x1;
    public static final int ENABLE_ROUTES = 0x2;
    public static final int OK_MESSAGE = 0x4;
    public static final int FAIL_MESSAGE = 0x8;
    
    public static final int ENABLE_AUTOMATION = 0x10;
    public static final int ENABLE_GOTO = 0x20;
    public static final int ENABLE_OTHER = 0x40;

    // codes use upper byte   
    public static final int CODE_MASK = 0xFF00; // upper byte only
    
    public static final int NO_ACTION = 0x0000 + OK_MESSAGE;
    
    public static final int BUILD_TRAIN = 0x0100 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int BUILD_TRAIN_IF_SELECTED = 0x0200 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int PRINT_TRAIN_MANIFEST = 0x0300 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int PRINT_TRAIN_MANIFEST_IF_SELECTED = 0x0400 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int MOVE_TRAIN = 0x0500 + ENABLE_TRAINS + ENABLE_ROUTES + OK_MESSAGE + FAIL_MESSAGE;
    public static final int TERMINATE_TRAIN = 0x0600 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int WAIT_FOR_TRAIN = 0x0700 + ENABLE_TRAINS + ENABLE_ROUTES + OK_MESSAGE + FAIL_MESSAGE;
    public static final int RESET_TRAIN = 0x0800 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int RUN_TRAIN = 0x0900 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int SELECT_TRAIN = 0x0A00 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int DESELECT_TRAIN = 0x0B00 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int WAIT_FOR_TRAIN_TERMINATE = 0x0C00 + ENABLE_TRAINS + OK_MESSAGE + FAIL_MESSAGE;
    public static final int IS_TRAIN_EN_ROUTE = 0x0D00 + ENABLE_TRAINS + ENABLE_ROUTES + OK_MESSAGE + FAIL_MESSAGE;
    
    public static final int UPDATE_SWITCHLIST = 0x1000 + OK_MESSAGE;
    public static final int PRINT_SWITCHLIST = 0x1100 + OK_MESSAGE;
    public static final int WAIT_SWITCHLIST = 0x1200 + OK_MESSAGE;
    public static final int RUN_SWITCHLIST_CHANGES = 0x1300 + OK_MESSAGE + FAIL_MESSAGE;
    public static final int RUN_SWITCHLIST = 0x1400 + OK_MESSAGE + FAIL_MESSAGE;
    public static final int PRINT_SWITCHLIST_CHANGES = 0x1500 + OK_MESSAGE;
    
    public static final int ACTIVATE_TRAIN_SCHEDULE = 0x2000 + OK_MESSAGE + ENABLE_OTHER; 
    public static final int APPLY_TRAIN_SCHEDULE = 0x2100 + OK_MESSAGE + FAIL_MESSAGE;
    
    public static final int STEP_AUTOMATION = 0x3000 + OK_MESSAGE + FAIL_MESSAGE + ENABLE_AUTOMATION;
    public static final int RUN_AUTOMATION = 0x3100 + OK_MESSAGE + FAIL_MESSAGE + ENABLE_AUTOMATION;
    public static final int STOP_AUTOMATION = 0x3200 + OK_MESSAGE + FAIL_MESSAGE + ENABLE_AUTOMATION;
    public static final int RESUME_AUTOMATION = 0x3300 + OK_MESSAGE + FAIL_MESSAGE + ENABLE_AUTOMATION;
    
//    public static final int MESSAGE = 0x4000  + ENABLE_TRAINS + ENABLE_ROUTES + OK_MESSAGE;
//    public static final int MESSAGE_OK = 0x4100 + ENABLE_TRAINS + ENABLE_ROUTES + OK_MESSAGE;
    public static final int MESSAGE_YES_NO = 0x4200 + ENABLE_TRAINS + ENABLE_ROUTES + OK_MESSAGE;
//    public static final int IF_MESSAGE_NO = 0x4300 + OK_MESSAGE + ENABLE_GOTO_LIST;
    
    public static final int GOTO = 0x5000 + OK_MESSAGE + FAIL_MESSAGE + ENABLE_GOTO;
    public static final int GOTO_IF_TRUE = 0x5100 + OK_MESSAGE + FAIL_MESSAGE + ENABLE_GOTO;
    public static final int GOTO_IF_FALSE = 0x5200 + OK_MESSAGE + FAIL_MESSAGE + ENABLE_GOTO;
    
    public static final int HALT_ACTION = 0xFF00 + OK_MESSAGE;

}
