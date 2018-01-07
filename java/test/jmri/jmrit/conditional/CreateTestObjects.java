package jmri.jmrit.conditional;

import java.util.ArrayList;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.Sensor;
import jmri.Turnout;
import jmri.implementation.DefaultConditional;
import jmri.implementation.DefaultConditionalAction;

public class CreateTestObjects {
    public static void createTestObjects() {
        for (int i = 1; i < 10; i++) {
            InstanceManager.getDefault(jmri.SensorManager.class).newSensor("IS" + i, "Sensor " + i);
            InstanceManager.getDefault(jmri.TurnoutManager.class).newTurnout("IT" + i, "Turnout " + i);
        }
        InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix("IX101", "Logix 101");
        Logix x2 = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix("IX102", "Logix 102");  // NOI18N

        // Create conditional
        Conditional cdl = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional("IX102C1", "IX102 Conditional 1");  // NOI18N
        x2.addConditional("IX102C1", 0);  // NOI18N

        // Create Variables
        ArrayList<ConditionalVariable> variableList = new ArrayList<>();
        
//       <conditionalStateVariable operator="2" negated="yes" type="1" systemName="Reset" dataString="" num1="0" num2="0" triggersCalc="yes" />
        ConditionalVariable var1 = new ConditionalVariable();
        var1.setOpern(2);
        var1.setNegation(true);
        var1.setType(1);
        var1.setName("Sensor 1");  // NOI18N
        var1.setDataString("");
        var1.setNum1(0);
        var1.setNum2(0);
        var1.setTriggerActions(true);
        variableList.add(var1);

//       <conditionalStateVariable operator="3" negated="yes" type="2" systemName="Reset" dataString="" num1="0" num2="0" triggersCalc="yes" />
        ConditionalVariable var2 = new ConditionalVariable();
        var2.setOpern(3);
        var2.setNegation(true);
        var2.setType(2);
        var2.setName("Sensor 1");  // NOI18N
        var2.setDataString("");
        var2.setNum1(0);
        var2.setNum2(0);
        var2.setTriggerActions(true);
        variableList.add(var2);

        cdl.setStateVariables(variableList);

        // Create actions
        ArrayList<ConditionalAction> actionList = ((DefaultConditional) cdl).getActionList();

//       <conditionalAction option="1" type="9" systemName="Reset" data="4" delay="0" string="" />
        ConditionalAction act1 = new DefaultConditionalAction();
        act1.setOption(1);
        act1.setType(9);
        act1.setDeviceName("Sensor 1");  // NOI18N
        act1.setActionData(4);
        act1.setActionString("");
        actionList.add(act1);

//       <conditionalAction option="1" type="2" systemName="T-LT264" data="2" delay="0" string="" />
        ConditionalAction act2 = new DefaultConditionalAction();
        act2.setOption(1);
        act2.setType(2);
        act2.setDeviceName("Turnout 1");  // NOI18N
        act2.setActionData(2);
        act2.setActionString("");
        actionList.add(act2);

        cdl.setAction(actionList);

    }
}
