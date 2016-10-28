package jmri.jmrit.ussctc;

import java.util.ArrayList;
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
 * <P>
 * An OS Indicator drives the lamp on the panel for a particular OS. Honors a
 * separate lock/unlocked indication by showing occupied if the associated
 * turnout has been unlocked.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public class OsIndicator implements Constants {

    final static String namePrefix = commonNamePrefix + "OsIndicator" + commonNameSuffix;

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
                    createNewLogix(nameP, "");
        }
        l.deActivateLogix();
        // Find/create conditional and add
        Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class)
                .getConditional(l, nameP + "C1");
        if (c == null) {
            c = InstanceManager.getDefault(jmri.ConditionalManager.class)
                    .createNewConditional(nameP + "C1", "");
            l.addConditional(nameP + "C1", -1);
        }

        // Load variable into the Conditional
        ArrayList<ConditionalVariable> variableList = c.getCopyOfStateVariables();
        variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_NONE,
                Conditional.TYPE_SENSOR_INACTIVE,
                osSensor, true));
        if (!lock.equals("")) {
            variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_AND,
                    Conditional.TYPE_SENSOR_INACTIVE,
                    lock, true));
        }
        c.setStateVariables(variableList);

        ArrayList<ConditionalAction> actionList = c.getCopyOfActions();
        actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                Conditional.ACTION_SET_TURNOUT, output,
                Turnout.CLOSED, " "));
        actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                Conditional.ACTION_SET_TURNOUT, output,
                Turnout.THROWN, " "));
        c.setAction(actionList);										// string data

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
            throw new jmri.JmriException("Logix does not exist");
        }

        // Find/create conditional and add
        Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class)
                .getConditional(l, nameP + "C1");
        if (c == null) {
            throw new jmri.JmriException("Conditional does not exist");
        }

        // Load variables from the Conditional
        ArrayList<ConditionalVariable> variableList = c.getCopyOfStateVariables();
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
