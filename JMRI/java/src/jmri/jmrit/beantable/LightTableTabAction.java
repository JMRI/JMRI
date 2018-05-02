package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;

public class LightTableTabAction extends AbstractTableTabAction {

    public LightTableTabAction(String s) {
        super(s);
    }

    public LightTableTabAction() {
        this("Multiple Tabbed");
    }

    @Override
    protected Manager getManager() {
        return InstanceManager.lightManagerInstance();
    }

    @Override
    protected String getClassName() {
        return LightTableAction.class.getName();
    }

    @Override
    protected AbstractTableAction getNewTableAction(String choice) {
        return new LightTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LightTable";
    }
}
