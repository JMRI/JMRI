package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;

public class SensorTableTabAction extends AbstractTableTabAction {

    public SensorTableTabAction(String s) {
        super(s);
    }

    public SensorTableTabAction() {
        this("Multiple Tabbed");
    }

    @Override
    protected Manager getManager() {
        return InstanceManager.sensorManagerInstance();
    }

    @Override
    protected String getClassName() {
        return SensorTableAction.class.getName();
    }

    @Override
    protected AbstractTableAction getNewTableAction(String choice) {
        return new SensorTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }
}
