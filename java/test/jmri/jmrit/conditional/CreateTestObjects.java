package jmri.jmrit.conditional;

import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;

public class CreateTestObjects {
    public static void createTestObjects() {
        for (int i = 1; i < 10; i++) {
            Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).newSensor("IS" + i, "Sensor " + i);  // NOI18N
            Turnout t = InstanceManager.getDefault(jmri.TurnoutManager.class).newTurnout("IT" + i, "Turnout " + i);  // NOI18N
        }
        Logix x1 = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix("IX101", "Logix 101");  // NOI18N
        Logix x2 = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix("IX102", "Logix 102");  // NOI18N
    }
}
