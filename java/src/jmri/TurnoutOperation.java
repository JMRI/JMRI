package jmri;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.implementation.AbstractTurnout;

/**
 * Framework for automating reliable turnout operation. This interface allows a
 * particular style (e.g. retries) to be implemented and then to have multiple
 * instances for variations in parameters if required
 * <p>
 * This mechanism is designed to extensible to allow new operation types (e.g.
 * for Tortoise-style point machines) and to allow individual system types to
 * change it, for example to allow operation with alternative feedback
 * arrangements.
 * <p>
 * The TurnoutOperation class is at the heart of things, although there are
 * several other classes, partly to fit in with JMRI's package structure. Each
 * specific retry scheme has its own concrete subclass of TurnoutOperation. One
 * instance of each such class is created at startup. It has the same name as
 * the prefix to the class, and is called the "defining instance". Further
 * instances can exist with different parameter values (e.g. number of retries).
 * <p>
 * The TurnoutOperationManager class (only one instance) keeps track of the
 * instances and can retrieve them by name. It can also supply a suitable
 * TurnoutOperation for a given turnout, based on the feedback type, if the
 * turnout does not identify one for itself.
 * <p>
 * Each AbstractTurnout may have a reference to a TurnoutOperation class, which
 * may be unique to this turnout or may be shared. When the turnout is thrown,
 * if it has its own TurnoutOperation, this is used (unless the turnout has
 * selected no automation). Otherwise, the TurnoutOperationManager is asked to
 * find one.
 * <p>
 * The TurnoutOperation has a factory method (getOperation) which is called when
 * a turnout is operated, to supply the operator. Each subclass of
 * TurnoutOperation has a corresponding subclass of TurnoutOperator, which
 * contains the logic for the retry scheme. Each operator runs in its own
 * thread, which terminates when the operation is complete. If another operation
 * of the same turnout is made before the first one completes, the older thread
 * terminates itself when it realises it is no longer the active operation for
 * the turnout.
 * <p>
 * The parameters of a TurnoutOperation can be edited. Each subclass has its own
 * xxxTurnoutOperationConfig class, which knows how to display the parameters in
 * a JPanel and gather them up again and store them afterwards.
 * <p>
 * Each subclass also has its own xxxTurnoutOperationXml class, which knows how
 * to store the information in an XML element, and restore it.
 * <p>
 * The current code defines two operations, NoFeedback and Sensor. Because these
 * have so much in common (only the xxxTurnoutOperator class has any
 * differences), most of them is implemented in the CommonTurnout... classes.
 * This family is not part of the general structure, although it can be reused
 * if it helps.
 * <p>
 * <b>Extensibility</b>
 * <p>
 * To write a new type of operation:
 * <ol>
 *  <li>Create the xxxTurnoutOperation class</li>
 *  <li>Create the xxxTurnoutOperator class, including the logic for what
 *  you're trying to do</li>
 *  <li>Create the xxxTurnoutOperationConfig class - the
 *  CommonTurnoutOperationConfig class can be used as a reference</li>
 *  <li>Create the xxxTurnoutOperationXml class - again the Common... class can
 *  be used as a reference</li>
 *  <li>Add the prefix to the class name (e.g. "Tortoise") to the list
 *  AbstractTurnoutManager.validOperationTypes, otherwise it will not be
 *  instantiated at startup and hence will not be available</li>
 * </ol>
 * <p>
 * To change the behavior for a particular system type:
 * <p>
 * There are some functions which can be overridden in the system-specific
 * subclasses to change default behaviour if desired. These mechanisms are
 * orthogonal to the operation subclasses.
 * <ol>
 *  <li>Override AbstractTurnoutManager.getValidOperationTypes to change the
 *  operation types allowed for this system</li>
 *  <li>Override AbstractTurnout.getFeedbackModeForOperation to map
 *  system-specific feedback modes into modes that the general classes know
 *  about</li>
 *  <li>Override AbstractTurnout.getTurnoutOperator if you want to do
 *  something <i>really</i> different</li>
 * </ol>
 *
 * @author John Harper Copyright 2005
 */
public abstract class TurnoutOperation implements Comparable<Object> {

    String name;
    int feedbackModes = 0;
    boolean nonce = false;  // created just for one turnout and not reusable 

    TurnoutOperation(@Nonnull String n) {
        name = n;
        InstanceManager.getDefault(TurnoutOperationManager.class).addOperation(this);
    }

    /**
     * Factory to make a copy of an operation identical in all respects except
     * the name.
     *
     * @param n name for new copy
     * @return TurnoutOperation of same concrete class as this
     */
    public abstract TurnoutOperation makeCopy(@Nonnull String n);

    /**
     * Set feedback modes - part of construction but done separately for
     * ordering problems.
     *
     * @param fm valid feedback modes for this class
     */
    protected void setFeedbackModes(int fm) {
        feedbackModes = fm;
    }

    /**
     * Get the descriptive name of the operation.
     *
     * @return name
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Ordering by name so operations can be sorted on name.
     *
     * @param other other TurnoutOperation object
     * @return usual compareTo return values
     */
    @Override
    public int compareTo(Object other) {
        return name.compareTo(((TurnoutOperation) other).name);
    }

    /**
     * The identity of an operation is its name.
     */
    @Override
    public boolean equals(Object ro) {
        if (ro == null) return false;
        if (ro instanceof TurnoutOperation)
            return name.equals(((TurnoutOperation)ro).name);
        else 
            return false;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    /**
     *
     * @param other another TurnoutOperation
     * @return true iff the two operations are equivalent, i.e. same subclass
     *         and same parameters
     */
    public abstract boolean equivalentTo(TurnoutOperation other);

    /**
     * Rename an operation.
     *
     * @param newName new name to use for rename attempt
     * @return true if the name was changed to the new value - otherwise name
     *         is unchanged
     */
    public boolean rename(@Nonnull String newName) {
        boolean result = false;
        TurnoutOperationManager mgr = InstanceManager.getDefault(TurnoutOperationManager.class);
        if (!isDefinitive() && mgr.getOperation(newName) == null) {
            mgr.removeOperation(this);
            name = newName;
            setNonce(false);
            mgr.addOperation(this);
            result = true;
        }
        return result;
    }

    /**
     * Get the definitive operation for this parameter variation.
     *
     * @return definitive operation
     */
    public TurnoutOperation getDefinitive() {
        String[] myClass = this.getClass().getName().split("\\.");
        String finalClass = myClass[myClass.length - 1];
        String mySubclass = finalClass.substring(0, finalClass.indexOf("TurnoutOperation"));
        return InstanceManager.getDefault(TurnoutOperationManager.class).getOperation(mySubclass);
    }

    /**
     *
     * @return true iff this is the "defining instance" of the class, which we
     *         determine by the name of the instance being the same as the
     *         prefix of the class
     */
    public boolean isDefinitive() {
        String[] classNames = this.getClass().getName().split("\\.");
        String className = classNames[classNames.length - 1];
        String opName = getName() + "TurnoutOperation";
        return (className.equalsIgnoreCase(opName));
    }

    /**
     * Get an instance of the operator for this operation type, set up and
     * started to do its thing in a private thread for the specified turnout.
     *
     * @param t the turnout to apply the operation to
     * @return the operator
     */
    public abstract TurnoutOperator getOperator(@Nonnull AbstractTurnout t);

    /**
     * Delete all knowledge of this operation. Reset any turnouts using it to
     * the default.
     */
    public void dispose() {
        if (!isDefinitive()) {
            InstanceManager.getDefault(TurnoutOperationManager.class).removeOperation(this);
            name = "*deleted";
            pcs.firePropertyChange("Deleted", null, null);  // this will remove all dangling references
        }
    }

    public boolean isDeleted() {
        return (name.equals("*deleted"));
    }

    /**
     * See if operation is in use (needed by the UI).
     *
     * @return true iff any turnouts are using it
     */
    public boolean isInUse() {
        TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        for (Turnout t : tm.getNamedBeanSet()) {
            if (t != null && t.getTurnoutOperation() == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * Nonce support. A nonce is a TurnoutOperation created specifically for one
     * turnout, which can't be directly referred to by name. It does have a
     * name, which is the turnout it was created for, prefixed by "*"
     *
     * @return true if this object is a nonce
     */
    public boolean isNonce() {
        return nonce;
    }

    public void setNonce(boolean n) {
        nonce = n;
        InstanceManager.getDefault(TurnoutOperationManager.class).firePropertyChange("Content", null, null);
    }

    public TurnoutOperation makeNonce(Turnout t) {
        TurnoutOperation op = makeCopy("*" + t.getSystemName());
        op.setNonce(true);
        return op;
    }

    /*
     * property change support
     */
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * @param mode feedback mode for a turnout
     * @return true iff this operation's feedback mode is one we know how to
     *         deal with
     */
    public boolean matchFeedbackMode(int mode) {
        return (mode & feedbackModes) != 0;
    }

}
