package jmri.jmrit.beantable.routetable;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Enable creation of a Logix from a Route.
 *
 * Split from {@link jmri.jmrit.beantable.RouteTableAction}
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 * @author Paul Bender Copyright (C) 2020
 */
public class RouteExportToLogix {

    private final String logixSysName;
    private final String conditionalSysPrefix;
    private final String systemName;
    private final RouteManager routeManager;
    private final LogixManager logixManager;
    private final ConditionalManager conditionalManager;

    RouteExportToLogix(String systemName){
        this(systemName,InstanceManager.getDefault(RouteManager.class),
                InstanceManager.getDefault(LogixManager.class),
                InstanceManager.getDefault(ConditionalManager.class));
    }

    RouteExportToLogix(String systemName, RouteManager routeManager,LogixManager logixManager,ConditionalManager conditionalManager){
        this.systemName = systemName;
        this.routeManager = routeManager;
        this.logixManager = logixManager;
        this.conditionalManager = conditionalManager;

        String logixPrefix = logixManager.getSystemNamePrefix();
        logixSysName = logixPrefix + ":RTX:";
        conditionalSysPrefix = logixSysName + "C";
    }

    public void export() {
        String logixSystemName = logixSysName + systemName;
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
        ArrayList<ConditionalAction> actionList = getConditionalActions(route);

        String file = route.getOutputSoundName();
        if (file!=null && file.length() > 0) {
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.PLAY_SOUND, "", -1, FileUtil.getPortableFilename(file)));
        }
        file = route.getOutputScriptName();
        if (file!=null && file.length() > 0) {
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.RUN_SCRIPT, "", -1, FileUtil.getPortableFilename(file)));
        }

        ///// Construct 'AND' clause from 'VETO' controls ////////
        ArrayList<ConditionalVariable> vetoList = getVetoVariables(route);

        removeOldConditionalNames(route,logix);

        ///////////////// Make Trigger Conditionals //////////////////////
        int numConds = 1; // passed through all these, with new value returned each time
        numConds = makeSensorConditional(route.getRouteSensor(0), route.getRouteSensorMode(0), numConds, false, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(route.getRouteSensor(1), route.getRouteSensorMode(1), numConds, false, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(route.getRouteSensor(2), route.getRouteSensorMode(2), numConds, false, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeTurnoutConditional(route.getCtlTurnout(), route.getControlTurnoutState(), numConds, false, actionList, vetoList, logix, logixSystemName, uName);

        ////// Construct actions for false from the 'any change' controls ////////////
        numConds = makeSensorConditional(route.getRouteSensor(0), route.getRouteSensorMode(0), numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(route.getRouteSensor(1), route.getRouteSensorMode(1), numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(route.getRouteSensor(2), route.getRouteSensorMode(2), numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeTurnoutConditional(route.getCtlTurnout(), route.getControlTurnoutState(), numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        log.debug("Final number of conditionals: {}", numConds);
        addRouteAlignmentSensorToLogix(logixSystemName, route, uName, logix);

        addRouteLockToLogix(logixSystemName, route, uName, logix);

        logix.activateLogix();
        routeManager.deleteRoute(route);
    }

    private void addRouteAlignmentSensorToLogix(String logixSystemName, Route route, String uName, Logix logix) {
        String cUserName;
        ArrayList<ConditionalAction> actionList;
        ///////////////// Set up Alignment Sensor, if there is one //////////////////////////
        if (route.getTurnoutsAlgdSensor() != null) {
            String sensorSystemName = route.getTurnoutsAlgdSensor().getDisplayName();
            String cSystemName = logixSystemName + "1A"; // NOI18N
            cUserName = route.getTurnoutsAlgdSensor().getDisplayName() + "A " + uName; // NOI18N

            ArrayList<ConditionalVariable> variableList = new ArrayList<>();
            for(int i=0;i<route.getNumOutputTurnouts();i++){
                String name = route.getOutputTurnout(i).getDisplayName();

                // exclude toggled outputs
                switch (route.getOutputTurnoutState(i)) {
                    case Turnout.CLOSED:
                        variableList.add(new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_CLOSED, name, true));
                        break;
                    case Turnout.THROWN:
                        variableList.add(new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_THROWN, name, true));
                        break;
                    default:
                        log.warn("Turnout {} was {}, neither CLOSED nor THROWN; not handled", name, route.getOutputTurnoutState(i)); // NOI18N
                }
            }
            actionList = new ArrayList<>();
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SENSOR, sensorSystemName, Sensor.ACTIVE, ""));
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE, Conditional.Action.SET_SENSOR, sensorSystemName, Sensor.INACTIVE, ""));

            Conditional c = conditionalManager.createNewConditional(cSystemName, cUserName);
            c.setStateVariables(variableList);
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            c.setAction(actionList);
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
        }
    }

    private void removeOldConditionalNames(Route route,Logix logix) {
        // remove old Conditionals for actions (ver 2.5.2 only -remove a bad idea)
        char[] ch = route.getSystemName().toCharArray();
        int hash = 0;
        for (char value : ch) {
            hash += value;
        }
        String cSystemName = conditionalSysPrefix + "T" + hash;
        removeConditionals(cSystemName, logix);
        cSystemName = conditionalSysPrefix + "F" + hash;
        removeConditionals(cSystemName, logix);
        cSystemName = conditionalSysPrefix + "A" + hash;
        removeConditionals(cSystemName, logix);
        cSystemName = conditionalSysPrefix + "L" + hash;
        removeConditionals(cSystemName, logix);

        int n = 0;
        do {
            n++;
            cSystemName = logix.getSystemName() + n + "A";
        } while (removeConditionals(cSystemName, logix));
        n = 0;
        do {
            n++;
            cSystemName = logix.getSystemName() + n + "T";
        } while (removeConditionals(cSystemName, logix));
        cSystemName = logix.getSystemName() + "L";
        removeConditionals(cSystemName, logix);
    }

    private void addRouteLockToLogix(String logixSystemName, Route route, String uName, Logix logix) {
        String cSystemName;
        String cUserName;
        ArrayList<ConditionalAction> actionList;///////////////// Set lock turnout information if there is any //////////////////////////
        if (route.getLockCtlTurnout()!=null) {
            Turnout lockControlTurnout = route.getLockCtlTurnout();

            // verify name (logix doesn't use "provideXXX")
            cSystemName = logixSystemName + "1L"; // NOI18N
            cUserName = lockControlTurnout.getSystemName() + "L " + uName; // NOI18N
            ArrayList<ConditionalVariable> variableList = new ArrayList<>();
            int mode = route.getLockControlTurnoutState();
            Conditional.Type conditionalType = Conditional.Type.TURNOUT_CLOSED;
            if (mode == Route.ONTHROWN) {
                conditionalType = Conditional.Type.TURNOUT_THROWN;
            }
            variableList.add(new ConditionalVariable(false, Conditional.Operator.NONE, conditionalType, lockControlTurnout.getSystemName(), true));

            actionList = new ArrayList<>();
            int option = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            int type = Turnout.LOCKED;
            if (mode == Route.ONCHANGE) {
                option = Conditional.ACTION_OPTION_ON_CHANGE;
                type = Route.TOGGLE;
            }
            for(int i=0;i<route.getNumOutputTurnouts();i++){
                actionList.add(new DefaultConditionalAction(option, Conditional.Action.LOCK_TURNOUT, route.getOutputTurnout(i).getDisplayName(), type, ""));
            }
            if (mode != Route.ONCHANGE) {
                // add non-toggle actions on
                option = Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE;
                type = Turnout.UNLOCKED;
                for(int i=0;i<route.getNumOutputTurnouts();i++){
                    actionList.add(new DefaultConditionalAction(option, Conditional.Action.LOCK_TURNOUT, route.getOutputTurnout(i).getDisplayName(), type, ""));
                }
            }

            // add new Conditionals for action on 'locks'
            Conditional c = conditionalManager.createNewConditional(cSystemName, cUserName);
            c.setStateVariables(variableList);
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            c.setAction(actionList);
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
        }
    }

    private ArrayList<ConditionalVariable> getVetoVariables(Route route) {
        ArrayList<ConditionalVariable> vetoList = new ArrayList<>();

        ConditionalVariable cVar = makeCtrlSensorVar(route.getRouteSensor(0),route.getRouteSensorMode(0), true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlSensorVar(route.getRouteSensor(1),route.getRouteSensorMode(1), true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlSensorVar(route.getRouteSensor(2),route.getRouteSensorMode(2), true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlTurnoutVar(route.getCtlTurnout(), route.getControlTurnoutState(), true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        return vetoList;
    }

    private ArrayList<ConditionalAction> getConditionalActions(Route route) {
        ArrayList<ConditionalAction> actionList = new ArrayList<>();

        for(int i=0;i<route.getNumOutputSensors();i++){
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.Action.SET_SENSOR, route.getOutputSensor(i).getDisplayName(),
                    route.getOutputSensorState(i), ""));
        }
        for(int i=0;i<route.getNumOutputTurnouts();i++){
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.Action.SET_TURNOUT, route.getOutputTurnout(i).getDisplayName(),
                    route.getOutputTurnoutState(i), ""));
        }
        log.debug("sensor actions {} turnout actions {} resulting Action List size {}",
                route.getNumOutputSensors(),route.getNumOutputTurnouts(),actionList.size());
        return actionList;
    }

    private boolean removeConditionals(String cSystemName, Logix logix) {
        Conditional c = conditionalManager.getBySystemName(cSystemName);
        if (c != null) {
            logix.deleteConditional(cSystemName);
            conditionalManager.deleteConditional(c);
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
    private int makeSensorConditional(Sensor selectedSensor, int sensorMode, int numConds, boolean onChange, ArrayList<ConditionalAction> actionList, ArrayList<ConditionalVariable> vetoList, Logix logix, String prefix, String uName) {
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
                c = conditionalManager.createNewConditional(cSystemName, cUserName);
            } catch (Exception ex) {
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
     * @param turnout    will be used to determine which turnout to make a conditional for
     * @param state      will be used to determine the mode for the conditional
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
    private int makeTurnoutConditional(Turnout turnout, int state, int numConds, boolean onChange, ArrayList<ConditionalAction> actionList,
                                       ArrayList<ConditionalVariable> vetoList, Logix logix, String prefix, String uName) {
        ConditionalVariable cVar = makeCtrlTurnoutVar(turnout,state, false, onChange);
        if (cVar != null) {
            ArrayList<ConditionalVariable> varList = new ArrayList<>();
            varList.add(cVar);
            for (ConditionalVariable conditionalVariable : vetoList) {
                varList.add(cloneVariable(conditionalVariable));
            }
            String cSystemName = prefix + numConds + "T";
            String cUserName = turnout.getDisplayName() + numConds + "C " + uName;
            Conditional c;
            try {
                c = conditionalManager.createNewConditional(cSystemName, cUserName);
            } catch (Exception ex) {
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

    private ConditionalVariable makeCtrlTurnoutVar(Turnout turnout, int mode, boolean makeVeto, boolean onChange) {

        if (turnout == null) {
            return null;
        }
        String devName = turnout.getDisplayName();
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

    private ConditionalVariable makeCtrlSensorVar(Sensor selectedSensor, int mode, boolean makeVeto, boolean onChange) {
        if (selectedSensor == null) {
            return null;
        }
        String devName = selectedSensor.getDisplayName();
        Conditional.Operator oper = Conditional.Operator.AND;
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
