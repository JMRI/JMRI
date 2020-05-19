package jmri.jmrit.beantable.routetable;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;
import jmri.swing.NamedBeanComboBox;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class RouteExportToLogix {

    private final String LOGIX_SYS_NAME;
    private final String CONDITIONAL_SYS_PREFIX;
    private final String systemName;
    private final RouteManager routeManager;
    private final LogixManager logixManager;

    RouteExportToLogix(String systemName){
        this(systemName,InstanceManager.getDefault(RouteManager.class),
                InstanceManager.getDefault(LogixManager.class));
    }

    RouteExportToLogix(String systemName, RouteManager routeManager,LogixManager logixManager){
        this.systemName = systemName;
        this.routeManager = routeManager;
        this.logixManager = logixManager;

        String logixPrefix = logixManager.getSystemNamePrefix();
        LOGIX_SYS_NAME = logixPrefix + ":RTX:";
        CONDITIONAL_SYS_PREFIX = LOGIX_SYS_NAME + "C";
    }

    public void export() {
        String logixSystemName = LOGIX_SYS_NAME + systemName;
        Route route = routeManager.getBySystemName(systemName);
        if(route == null ){
            log.error("Route {} does not exist",systemName);
            return;
        }
        String uName = route.getUserName();
        Logix logix = logixManager.getBySystemName(logixSystemName);
        if (logix == null) {
            logix = logixManager.createNewLogix(logixSystemName, uName);
            if (logix == null) {
                log.error("Failed to create Logix {}, {}", logixSystemName, uName);
                return;
            }
        }
        logix.deActivateLogix();

        /////////////////// Construct output actions for change to true //////////////////////
        ArrayList<ConditionalAction> actionList = new ArrayList<>();

        for (RouteSensor rSensor : addFrame.get_includedSensorList()) {
            String name = rSensor.getUserName();
            if (name == null || name.length() == 0) {
                name = rSensor.getSysName();
            }
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SENSOR, name, rSensor.getState(), ""));
        }
        for (RouteTurnout rTurnout : addFrame.get_includedSensorList()) {
            String name = rTurnout.getUserName();
            if (name == null || name.length() == 0) {
                name = rTurnout.getSysName();
            }
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, name, rTurnout.getState(), ""));
        }
        String file = route.getOutputSoundName();
        if (file.length() > 0) {
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.PLAY_SOUND, "", -1, FileUtil.getPortableFilename(file)));
        }
        file = route.getOutputScriptName();
        if (file.length() > 0) {
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.RUN_SCRIPT, "", -1, FileUtil.getPortableFilename(file)));
        }

        ///// Construct 'AND' clause from 'VETO' controls ////////
        ArrayList<ConditionalVariable> vetoList = new ArrayList<>();

        // String andClause = null;
        ConditionalVariable cVar = makeCtrlSensorVar(addFrame.getSensor1(), addFrame.getSensor1Mode(), true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlSensorVar(addFrame.getSensor2(), addFrame.getSensor2Mode(), true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlSensorVar(addFrame.getSensor3(), addFrame.getSensor3Mode(), true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlTurnoutVar(cTurnout, cTurnoutStateBox, true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }

        // remove old Conditionals for actions (ver 2.5.2 only -remove a bad idea)
        char[] ch = sName.toCharArray();
        int hash = 0;
        for (char value : ch) {
            hash += value;
        }
        String cSystemName = CONDITIONAL_SYS_PREFIX + "T" + hash;
        removeConditionals(cSystemName, logix);
        cSystemName = CONDITIONAL_SYS_PREFIX + "F" + hash;
        removeConditionals(cSystemName, logix);
        cSystemName = CONDITIONAL_SYS_PREFIX + "A" + hash;
        removeConditionals(cSystemName, logix);
        cSystemName = CONDITIONAL_SYS_PREFIX + "L" + hash;
        removeConditionals(cSystemName, logix);

        int n = 0;
        do {
            n++;
            cSystemName = logixSystemName + n + "A";
        } while (removeConditionals(cSystemName, logix));
        n = 0;
        do {
            n++;
            cSystemName = logixSystemName + n + "T";
        } while (removeConditionals(cSystemName, logix));
        cSystemName = logixSystemName + "L";
        removeConditionals(cSystemName, logix);

        String cUserName;

        ///////////////// Make Trigger Conditionals //////////////////////
        int numConds = 1; // passed through all these, with new value returned each time
        numConds = makeSensorConditional(addFrame.getSensor1(), addFrame.getSensor1Mode(), numConds, false, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(addFrame.getSensor2(), addFrame.getSensor2Mode(), numConds, false, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(addFrame.getSensor3(), addFrame.getSensor3Mode(), numConds, false, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeTurnoutConditional(cTurnout, cTurnoutStateBox, numConds, false, actionList, vetoList, logix, logixSystemName, uName);

        ////// Construct actions for false from the 'any change' controls ////////////
        numConds = makeSensorConditional(addFrame.getSensor1(),addFrame.getSensor1Mode(), numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(addFrame.getSensor2(),addFrame.getSensor2Mode(), numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(addFrame.getSensor3(),addFrame.getSensor3Mode(), numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeTurnoutConditional(cTurnout, cTurnoutStateBox, numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        log.debug("Final number of conditionals: {}", numConds);

        ///////////////// Set up Alignment Sensor, if there is one //////////////////////////
        if (turnoutsAlignedSensor.getSelectedItem() != null) {
            // verify name (logix doesn't use "provideXXX")
            String sensorSystemName = turnoutsAlignedSensor.getSelectedItemDisplayName();
            cSystemName = logixSystemName + "1A"; // NOI18N
            cUserName = turnoutsAlignedSensor.getSelectedItemDisplayName() + "A " + uName; // NOI18N

            ArrayList<ConditionalVariable> variableList = new ArrayList<>();
            for (RouteTurnout rTurnout : addFrame.get_includedTurnoutList()) {
                String name = rTurnout.getUserName();
                if (name == null || name.length() == 0) {
                    name = rTurnout.getSysName();
                }
                // exclude toggled outputs
                switch (rTurnout.getState()) {
                    case Turnout.CLOSED:
                        variableList.add(new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_CLOSED, name, true));
                        break;
                    case Turnout.THROWN:
                        variableList.add(new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_THROWN, name, true));
                        break;
                    default:
                        log.warn("Turnout {} was {}, neither CLOSED nor THROWN; not handled", name, rTurnout.getState()); // NOI18N
                }
            }
            actionList = new ArrayList<>();
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SENSOR, sensorSystemName, Sensor.ACTIVE, ""));
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE, Conditional.Action.SET_SENSOR, sensorSystemName, Sensor.INACTIVE, ""));

            Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(cSystemName, cUserName);
            c.setStateVariables(variableList);
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            c.setAction(actionList);
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
        }

        ///////////////// Set lock turnout information if there is any //////////////////////////
        if (cLockTurnout.getSelectedItem() != null) {
            String turnoutLockSystemName = cLockTurnout.getSelectedItemDisplayName();
            // verify name (logix doesn't use "provideXXX")
            cSystemName = logixSystemName + "1L"; // NOI18N
            cUserName = turnoutLockSystemName + "L " + uName; // NOI18N
            ArrayList<ConditionalVariable> variableList = new ArrayList<>();
            int mode = turnoutModeFromBox(cTurnoutStateBox);
            Conditional.Type conditionalType = Conditional.Type.TURNOUT_CLOSED;
            if (mode == Route.ONTHROWN) {
                conditionalType = Conditional.Type.TURNOUT_THROWN;
            }
            variableList.add(new ConditionalVariable(false, Conditional.Operator.NONE, conditionalType, turnoutLockSystemName, true));

            actionList = new ArrayList<>();
            int option = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            int type = Turnout.LOCKED;
            if (mode == Route.ONCHANGE) {
                option = Conditional.ACTION_OPTION_ON_CHANGE;
                type = Route.TOGGLE;
            }
            for (RouteTurnout rTurnout : addFrame.get_includedTurnoutList()) {
                String name = rTurnout.getUserName();
                if (name == null || name.length() == 0) {
                    name = rTurnout.getSysName();
                }
                actionList.add(new DefaultConditionalAction(option, Conditional.Action.LOCK_TURNOUT, name, type, ""));
            }
            if (mode != Route.ONCHANGE) {
                // add non-toggle actions on
                option = Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE;
                type = Turnout.UNLOCKED;
                for (RouteTurnout rTurnout : addFrame.get_includedTurnoutList()) {
                    String name = rTurnout.getUserName();
                    if (name == null || name.length() == 0) {
                        name = rTurnout.getSysName();
                    }
                    actionList.add(new DefaultConditionalAction(option, Conditional.Action.LOCK_TURNOUT, name, type, ""));
                }
            }

            // add new Conditionals for action on 'locks'
            Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(cSystemName, cUserName);
            c.setStateVariables(variableList);
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            c.setAction(actionList);
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
        }
        logix.activateLogix();
        if (curRoute != null) {
            InstanceManager.getDefault(jmri.RouteManager.class).deleteRoute(curRoute);
        }
        status1.setText(Bundle.getMessage("BeanNameRoute") + "\"" + uName + "\" " + Bundle.getMessage("RouteAddStatusExported") + " (" + addFrame.get_includedTurnoutList().size() + Bundle.getMessage("Turnouts") + ", " + addFrame.get_includedSensorList().size() + " " + Bundle.getMessage("Sensors") + ")");
        finishUpdate();
    }

    private boolean removeConditionals(String cSystemName, Logix logix) {
        Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(cSystemName);
        if (c != null) {
            logix.deleteConditional(cSystemName);
            InstanceManager.getDefault(jmri.ConditionalManager.class).deleteConditional(c);
            return true;
        }
        return false;
    }

    /**
     * Create a new sensor conditional.
     *
     * @param selectedSensor will be used to determine which sensor to make a conditional for
     * @param sensorMode will be used to determine the mode for the conditional
     * @param numConds   number of existing route conditionals
     * @param onChange   ???
     * @param actionList actions to take in conditional
     * @param vetoList   conditionals that can veto an action
     * @param logix      Logix to add the conditional to
     * @param prefix     system prefix for conditional
     * @param uName      user name for conditional
     * @return number of conditionals after the creation
     * @throws IllegalArgumentException if "user input no good"
     */
    // why are the controls being passed, and not their selections?
    private int makeSensorConditional(Sensor selectedSensor, String sensorMode, int numConds, boolean onChange, ArrayList<ConditionalAction> actionList, ArrayList<ConditionalVariable> vetoList, Logix logix, String prefix, String uName) {
        ConditionalVariable cVar = makeCtrlSensorVar(selectedSensor, sensorMode, false, onChange);
        if (cVar != null) {
            ArrayList<ConditionalVariable> varList = new ArrayList<>();
            varList.add(cVar);
            for (ConditionalVariable conditionalVariable : vetoList) {
                varList.add(cloneVariable(conditionalVariable));
            }
            String cSystemName = prefix + numConds + "T";
            String cUserName = selectedSensor.getDisplayName() + numConds + "C " + uName;
            Conditional c;
            try {
                c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(cSystemName, cUserName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(cSystemName);
                // throw without creating any
                throw new IllegalArgumentException("user input no good");
            }
            c.setStateVariables(varList);
            int option = onChange ? Conditional.ACTION_OPTION_ON_CHANGE : Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            c.setAction(cloneActionList(actionList, option));
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            numConds++;
        }
        return numConds;
    }

    /**
     * Create a new turnout conditional.
     *
     * @param jmriBox    control, the selection from which, will be used to determine which sensor to make a conditional
     *                   for
     * @param box        control, the selection from which, will be used to determine the mode for the conditional
     * @param numConds   number of existing route conditionals
     * @param onChange   ???
     * @param actionList actions to take in conditional
     * @param vetoList   conditionals that can veto an action
     * @param logix      Logix to add the conditional to
     * @param prefix     system prefix for conditional
     * @param uName      user name for conditional
     * @return number of conditionals after the creation
     * @throws IllegalArgumentException if "user input no good"
     */
    // why are the controls being passed, and not their selections?
    private int makeTurnoutConditional(NamedBeanComboBox<Turnout> jmriBox, JComboBox<String> box, int numConds, boolean onChange, ArrayList<ConditionalAction> actionList, ArrayList<ConditionalVariable> vetoList, Logix logix, String prefix, String uName) {
        ConditionalVariable cVar = makeCtrlTurnoutVar(jmriBox, box, false, onChange);
        if (cVar != null) {
            ArrayList<ConditionalVariable> varList = new ArrayList<>();
            varList.add(cVar);
            for (ConditionalVariable conditionalVariable : vetoList) {
                varList.add(cloneVariable(conditionalVariable));
            }
            String cSystemName = prefix + numConds + "T";
            String cUserName = jmriBox.getSelectedItemDisplayName() + numConds + "C " + uName;
            Conditional c;
            try {
                c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(cSystemName, cUserName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(cSystemName);
                // throw without creating any
                throw new IllegalArgumentException("user input no good");
            }
            c.setStateVariables(varList);
            int option = onChange ? Conditional.ACTION_OPTION_ON_CHANGE : Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            c.setAction(cloneActionList(actionList, option));
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            numConds++;
        }
        return numConds;
    }

    private ConditionalVariable makeCtrlTurnoutVar(NamedBeanComboBox<Turnout> jmriBox, JComboBox<String> box, boolean makeVeto, boolean onChange) {

        if (jmriBox.getSelectedItem() == null) {
            return null;
        }
        String devName = jmriBox.getSelectedItemDisplayName();
        int mode = turnoutModeFromBox(box);
        Conditional.Operator oper = Conditional.Operator.AND;
        Conditional.Type type;
        boolean negated = false;
        boolean trigger = true;
        switch (mode) {
            case Route.ONCLOSED:    // route fires if turnout goes closed
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_CLOSED;
                break;
            case Route.ONTHROWN:  // route fires if turnout goes thrown
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_THROWN;
                break;
            case Route.ONCHANGE:    // route fires if turnout goes active or inactive
                if (makeVeto || !onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_CLOSED;
                break;
            case Route.VETOCLOSED:  // turnout must be closed for route to fire
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_CLOSED;
                trigger = false;
                negated = true;
                break;
            case Route.VETOTHROWN:  // turnout must be thrown for route to fire
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_THROWN;
                trigger = false;
                negated = true;
                break;
            default:
                log.error("Control Turnout {} has bad mode= {}", devName, mode);
                return null;
        }
        return new ConditionalVariable(negated, oper, type, devName, trigger);
    }

    private void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addFrame, Bundle.getMessage("ErrorRouteAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"), Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
    }

    private ConditionalVariable cloneVariable(ConditionalVariable v) {
        return new ConditionalVariable(v.isNegated(), v.getOpern(), v.getType(), v.getName(), v.doTriggerActions());
    }

    private ArrayList<ConditionalAction> cloneActionList(ArrayList<ConditionalAction> actionList, int option) {
        ArrayList<ConditionalAction> list = new ArrayList<>();
        for (ConditionalAction action : actionList) {
            ConditionalAction clone = new DefaultConditionalAction();
            clone.setType(action.getType());
            clone.setOption(option);
            clone.setDeviceName(action.getDeviceName());
            clone.setActionData(action.getActionData());
            clone.setActionString(action.getActionString());
            list.add(clone);
        }
        return list;
    }

    private ConditionalVariable makeCtrlSensorVar(Sensor selectedSensor, String sensorMode, boolean makeVeto, boolean onChange) {
        if (selectedSensor == null) {
            return null;
        }
        String devName = selectedSensor.getDisplayName();
        Conditional.Operator oper = Conditional.Operator.AND;
        int mode = addFrame.sensorModeFromString(sensorMode);
        boolean trigger = true;
        boolean negated = false;
        Conditional.Type type;
        switch (mode) {
            case Route.ONACTIVE:    // route fires if sensor goes active
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_ACTIVE;
                break;
            case Route.ONINACTIVE:  // route fires if sensor goes inactive
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_INACTIVE;
                break;
            case Route.ONCHANGE:  // route fires if sensor goes active or inactive
                if (makeVeto || !onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_ACTIVE;
                break;
            case Route.VETOACTIVE:  // sensor must be active for route to fire
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_ACTIVE;
                negated = true;
                trigger = false;
                break;
            case Route.VETOINACTIVE:
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_INACTIVE;
                negated = true;
                trigger = false;
                break;
            default:
                log.error("Control Sensor {} has bad mode= {}", devName, mode);
                return null;
        }
        return new ConditionalVariable(negated, oper, type, devName, trigger);
    }

    private static final Logger log = LoggerFactory.getLogger(RouteExportToLogix.class);
}
