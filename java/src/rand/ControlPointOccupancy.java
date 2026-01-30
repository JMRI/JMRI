package rand;

import jmri.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ControlPointOccupancy {
    private static final Logger logger = LoggerFactory.getLogger(ControlPointOccupancy.class);
    private static final Timer timer = new Timer("occupancy timer", true);

    public ControlPointOccupancy(String controlPointName, String pointsSensorName, String normalSensorName, String thrownSensorName) {
        Sensor controlPointSensor = getSensor(controlPointName);
        Turnout controlPointTurnout = getTurnout(controlPointName);
        Sensor pointsSensor = getSensor(pointsSensorName);
        Sensor normalSensor = getSensor(normalSensorName);
        Sensor thrownSensor = getSensor(thrownSensorName);

        try {
            if (pointsSensor != null)
                pointsSensor.setState(Sensor.INACTIVE);
            if (normalSensor != null)
                normalSensor.setState(Sensor.INACTIVE);
            if (thrownSensor != null)
                thrownSensor.setState(Sensor.INACTIVE);
        } catch (JmriException e) {
            logger.error("Could not set state.", e);
        }


        controlPointSensor.addPropertyChangeListener("KnownState", evt -> {
            if (Objects.equals(evt.getNewValue(), evt.getOldValue()))
                return;

            if (controlPointSensor.getState() == Sensor.ACTIVE) {
                toggle(pointsSensor);
                if (controlPointTurnout != null) {
                    switch (controlPointTurnout.getState()) {
                        case Turnout.THROWN:
                            toggle(thrownSensor);
                            break;
                        case Turnout.CLOSED:
                            toggle(normalSensor);
                            break;
                    }
                }
                else {
                    toggle(normalSensor);
                }
            }
        });

    }

    private Sensor getSensor(String name) {
        if (name == null)
            return null;
        Sensor result = InstanceManager.getDefault(SensorManager.class).getByUserName(name);
        if (result == null) {
            throw new IllegalArgumentException("Could not find sensor named " + name);
        }
        return result;
    }

    private Turnout getTurnout(String name) {
        if (name == null)
            return null;
        return InstanceManager.getDefault(TurnoutManager.class).getByUserName(name);
    }

    private void toggle(Sensor sensor) {
        if (sensor == null)
            return;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    switch (sensor.getState()) {
                        case Sensor.ACTIVE:
                            sensor.setState(Sensor.INACTIVE);
                            break;
                        case Sensor.INACTIVE:
                            sensor.setState(Sensor.ACTIVE);
                            break;

                    }
                }
                catch (JmriException e) {
                    logger.error("Could not toggle sensor " + sensor.getUserName(), e);
                }
            }
        }, 2000);
    }

}
