package jmri.jmrit.ussctc;

import java.util.List;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Logix;
import jmri.Turnout;
import jmri.implementation.DefaultConditionalAction;

/**
 * Provide bean-like access to the collection of Logix, Routes, Memories, etc
 * that make up a OsIndicator.
 * <p>
 * An OS Indicator drives the lamp on the panel for a particular OS. Honors a
 * separate lock/unlocked indication by showing occupied if the associated
 * turnout has been unlocked.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class OsIndicator implements Constants {

    final static String namePrefix = commonNamePrefix + "OsIndicator" + commonNameSuffix; //NOI18N

    /**
     * Nobody can build anonymous object
     */
    //private OsIndicator() {}
    /**
     * Create one from scratch
     *
     * @param output   User- or System name of output turnout to be driven
     * @param osSensor User- or System name of Sensor determining OS occupancy
     * @param lock     Name of NamedBean used for Locking (type to be decided)
     */
    public OsIndicator(String output, String osSensor, String lock) {
        this.lock = lock;
        this.osSensor = osSensor;
        this.output = output;
    }

    /**
     * Create the underlying objects that implement this
     */
    public void instantiate() {
        // find/create Logix
        String nameP = namePrefix + output;
        Logix l = InstanceManager.getDefault(jmri.LogixManager.class).
                getLogix(nameP);
        if (l == null) {
            l = InstanceManager.getDefault(jmri.LogixManager.class).
                    createNewLogix(nameP, ""); //NOI18N
        }
        l.deActivateLogix();
        // Find/create conditional and add
        Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class)
                .getConditional(l, nameP + "C1"); //NOI18N
        if (c == null) {
            c = InstanceManager.getDefault(jmri.ConditionalManager.class)
                    .createNewConditional(nameP + "C1", ""); //NOI18N
            l.addConditional(nameP + "C1", -1); //NOI18N
        }

        // Load variable into the Conditional
        List<ConditionalVariable> variableList = c.getCopyOfStateVariables();
        variableList.add(new ConditionalVariable(false, Conditional.Operator.NONE,
                Conditional.Type.SENSOR_INACTIVE,
                osSensor, true));
        if (!lock.isEmpty()) {
            variableList.add(new ConditionalVariable(false, Conditional.Operator.AND,
                    Conditional.Type.SENSOR_INACTIVE,
                    lock, true));
        }
        c.setStateVariables(variableList);

        List<ConditionalAction> actionList = c.getCopyOfActions();
        actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                Conditional.Action.SET_TURNOUT, output,
                Turnout.CLOSED, " ")); //NOI18N
        actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                Conditional.Action.SET_TURNOUT, output,
                Turnout.THROWN, " ")); //NOI18N
        c.setAction(actionList);          // string data

        // and put it back in operation
        l.activateLogix();

    }

    /**
     * Create an object to represent an existing OsIndicator.
     *
     * @param outputName name of output Turnout that drives the indicator
     * @throws JmriException if no such OsIndicator exists, or some problem
     *                       found
     */
    public OsIndicator(String outputName) throws jmri.JmriException {
        this.output = outputName;

        // findLogix
        String nameP = namePrefix + output;
        Logix l = InstanceManager.getDefault(jmri.LogixManager.class).
                getLogix(nameP);
        if (l == null) {
            throw new jmri.JmriException("Logix does not exist"); //NOI18N
        }

        // Find/create conditional and add
        Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class)
                .getConditional(l, nameP + "C1"); //NOI18N
        if (c == null) {
            throw new jmri.JmriException("Conditional does not exist"); //NOI18N
        }

        // Load variables from the Conditional
        List<ConditionalVariable> variableList = c.getCopyOfStateVariables();
        ConditionalVariable variable = variableList.get(0);
        osSensor = variable.getName();
        if (variableList.size() > 0) {
            variable = variableList.get(1);
            lock = variable.getName();
        }
    }

    public String getOutputName() {
        return output;
    }

    public String getOsSensorName() {
        return osSensor;
    }

    public String getLockName() {
        return lock;
    }

    String output;
    String osSensor;
    String lock;

}
