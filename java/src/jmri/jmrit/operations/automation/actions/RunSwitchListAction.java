package jmri.jmrit.operations.automation.actions;

public class RunSwitchListAction extends RunSwitchListChangesAction {

    private static final int _code = ActionCodes.RUN_SWITCHLIST;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("RunSwitchList");
    }

    @Override
    public void doAction() {
        doAction(!IS_CHANGED);
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

//    private final static Logger log = LoggerFactory.getLogger(RunSwitchListAction.class);

}
