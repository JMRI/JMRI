package rand;

import jmri.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OnewayDoubleTrack {
    private static final Logger logger = LoggerFactory.getLogger(OnewayDoubleTrack.class);
    private static final Timer timer = new Timer("double track timer", true);

    private final List<ControlPoint> controlPoints = new ArrayList<>();

    public OnewayDoubleTrack addEntrance(String blockName, int enterState, String enterBlockName, String exitBlockName) {
        controlPoints.add(new Entrance(blockName, enterState, enterBlockName, exitBlockName));
        return this;
    }

    public OnewayDoubleTrack addWayPoint(String blockName, String previousBlockName, String nextBlockName) {
        controlPoints.add(new WayPoint(blockName, previousBlockName, nextBlockName));
        return this;
    }

    public void build() throws JmriException {
        for (ControlPoint controlPoint : controlPoints) {
            controlPoint.build();
        }
    }

    private static Sensor getSensor(String name) {
        Sensor result = InstanceManager.getDefault(SensorManager.class).getByUserName(name);
        if (result == null) {
            throw new IllegalArgumentException("Could not find sensor named " + name);
        }
        return result;
    }

    private static Turnout getTurnout(String name) {
        Turnout result = InstanceManager.getDefault(TurnoutManager.class).getByUserName(name);
        if (result == null) {
            throw new IllegalArgumentException("Could not find sensor named " + name);
        }
        return result;
    }

    private static void setState(Sensor sensor, int value) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sensor.setState(value);
                }
                catch (JmriException e) {
                    logger.error("Could not toggle sensor " + sensor.getUserName(), e);
                }
            }
        }, 2000);
    }

    private static abstract class ControlPoint {
        protected String blockName;

        private ControlPoint(String blockName) {
            this.blockName = blockName;
        }

        abstract void build() throws JmriException;

    }

    private static class Entrance extends ControlPoint {
        String enterBlockName;
        String exitBlockName;
        int enterState;

        private Entrance(String blockName, int enterState, String enterBlockName, String exitBlockName) {
            super(blockName);
            this.enterBlockName = enterBlockName;
            this.exitBlockName = exitBlockName;
            this.enterState = enterState;
        }

        @Override
        void build() throws JmriException {
            Sensor controlPointSensor = getSensor(blockName);
            Turnout controlPointTurnout = getTurnout(blockName);
            Sensor exitBlockSensor = getSensor(exitBlockName);
            Sensor enterBlockSensor = getSensor(enterBlockName);

            exitBlockSensor.setState(Sensor.INACTIVE);
            enterBlockSensor.setState(Sensor.INACTIVE);

            controlPointSensor.addPropertyChangeListener("KnownState", evt -> {
                if (Objects.equals(evt.getNewValue(), evt.getOldValue()))
                    return;

                if (controlPointSensor.getState() == Sensor.ACTIVE) {
                    if (controlPointTurnout.getState() == enterState) {
                        setState(enterBlockSensor, Sensor.ACTIVE);
                    }
                    else {
                        setState(exitBlockSensor, Sensor.INACTIVE);
                    }
                }
            });
        }
    }

    private static class WayPoint extends ControlPoint {
        private final String previousBlockName;
        private final String nextBlockName;

        private WayPoint(String blockName, String previousBlockName, String nextBlockName) {
            super(blockName);
            this.previousBlockName = previousBlockName;
            this.nextBlockName = nextBlockName;
        }
        @Override
        void build() throws JmriException {
            Sensor controlPointSensor = getSensor(blockName);
            Sensor nextBlockSensor = getSensor(nextBlockName);
            Sensor previousBlockSensor = getSensor(previousBlockName);

            nextBlockSensor.setState(Sensor.INACTIVE);
            previousBlockSensor.setState(Sensor.INACTIVE);

            controlPointSensor.addPropertyChangeListener("KnownState", evt -> {
                if (Objects.equals(evt.getNewValue(), evt.getOldValue()))
                    return;

                if (controlPointSensor.getState() == Sensor.ACTIVE) {
                    setState(previousBlockSensor, Sensor.INACTIVE);
                    setState(nextBlockSensor, Sensor.ACTIVE);
                }
            });
        }
    }
}
