package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;

public class SensorTableTabAction extends AbstractTableTabAction {

    /**
     *
     */
    private static final long serialVersionUID = -8373953953343271566L;

    public SensorTableTabAction(String s) {
        super(s);
    }

    public SensorTableTabAction() {
        this("Multiple Tabbed");
    }

    protected Manager getManager() {
        return InstanceManager.sensorManagerInstance();
    }

    protected String getClassName() {
        return SensorTableAction.class.getName();
    }

    protected AbstractTableAction getNewTableAction(String choice) {
        return new SensorTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }
}
