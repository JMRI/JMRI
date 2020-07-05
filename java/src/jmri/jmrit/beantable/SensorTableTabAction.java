package jmri.jmrit.beantable;

import jmri.*;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

@API(status = STABLE)
public class SensorTableTabAction extends AbstractTableTabAction<Sensor> {

    public SensorTableTabAction(String s) {
        super(s);
    }

    public SensorTableTabAction() {
        this("Multiple Tabbed");
    }

    @Override
    protected Manager<Sensor> getManager() {
        return InstanceManager.sensorManagerInstance();
    }

    @Override
    protected String getClassName() {
        return SensorTableAction.class.getName();
    }

    @Override
    protected SensorTableAction getNewTableAction(String choice) {
        return new SensorTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }
}
