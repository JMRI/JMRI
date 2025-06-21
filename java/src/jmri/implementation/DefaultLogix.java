package jmri.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.*;

import jmri.*;
import jmri.jmrit.beantable.LRouteTableAction;
import jmri.jmrit.entryexit.EntryExitPairs;

/**
 * Class providing the basic logic of the Logix interface.
 *
 * @author Dave Duchamp Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009
 */
public class DefaultLogix extends AbstractNamedBean
        implements Logix {

    private final ConditionalManager conditionalManager;

    public DefaultLogix(String systemName, String userName) {
        this(systemName,userName,InstanceManager.getDefault(ConditionalManager.class));
    }

    public DefaultLogix(String systemName,String userName,ConditionalManager conditionalManager) {
        super(systemName, userName);
        this.conditionalManager = conditionalManager;
    }

    public DefaultLogix(String systemName) {
        this(systemName,InstanceManager.getDefault(ConditionalManager.class));
    }

    public DefaultLogix(String systemName,ConditionalManager conditionalManager) {
        super(systemName);
        this.conditionalManager = conditionalManager;
    }

    @Override
    @Nonnull
    public String getBeanType() {
        return Bundle.getMessage("BeanNameLogix");  // NOI18N
    }

    /**
     * Persistant instance variables (saved between runs). Order is significant.
     */
    private final ArrayList<String> _conditionalSystemNames = new ArrayList<>();
    private ArrayList<JmriSimplePropertyListener> _listeners = new ArrayList<>();

    /**
     * Maintain a list of conditional objects.  The key is the conditional system name
     * @since 4.7.4
     */
    private final HashMap<String, Conditional> _conditionalMap = new HashMap<>();

    /**
     * Operational instance variables (not saved between runs)
     */
    private boolean mEnabled = true;

    private boolean _isActivated = false;

    private boolean _isGuiSet = false;

    /**
     * Get number of Conditionals for this Logix
     */
    @Override
    public int getNumConditionals() {
        return _conditionalSystemNames.size();
    }

    /**
     * Move 'row' to 'nextInOrder' and shift all between 'row' and 'nextInOrder'
     * up one position {@literal ( row > nextInOrder )}
     */
    @Override
    public void swapConditional(int nextInOrder, int row) {
        if (row <= nextInOrder) {
            return;
        }
        String temp = _conditionalSystemNames.get(row);
        for (int i = row; i > nextInOrder; i--) {
            _conditionalSystemNames.set(i, _conditionalSystemNames.get(i - 1));
        }
        _conditionalSystemNames.set(nextInOrder, temp);
    }

    /**
     * Returns the system name of the conditional that will calculate in the
     * specified order. This is also the order the Conditional is listed in the
     * Add/Edit Logix dialog. If 'order' is greater than the number of
     * Conditionals for this Logix, null is returned.
     *
     * @param order  order in which the Conditional calculates.
     */
    @Override
    @CheckForNull
    public String getConditionalByNumberOrder(int order) {
        try {
            return _conditionalSystemNames.get(order);
        } catch (java.lang.IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    /**
     * Add a Conditional to this Logix R
     *
     * @param systemName The Conditional system name
     * @param order       the order this conditional should calculate in if
     *                   order is negative, the conditional is added at the end
     *                   of current group of conditionals
     */
    @Override
    public void addConditional(String systemName, int order) {
        _conditionalSystemNames.add(systemName);
    }

    /**
     * Add a child Conditional to the parent Logix.
     *
     * @since 4.7.4
     * @param systemName The system name for the Conditional object.
     * @param conditional The Conditional object.
     * @return true if the Conditional was added, false otherwise.
     */
    @Override
    public boolean addConditional(String systemName, Conditional conditional) {
        Conditional chkDuplicate = _conditionalMap.putIfAbsent(systemName, conditional);
        if (chkDuplicate == null) {
            return true;
        }
        log.error("Conditional '{}' has already been added to Logix '{}'", systemName, getSystemName());  // NOI18N
        return false;
    }

    /**
     * Get a Conditional belonging to this Logix.
     *
     * @since 4.7.4
     * @param systemName The name of the Conditional object.
     * @return the Conditional object or null if not found.
     */
    @Override
    @CheckForNull
    public Conditional getConditional(String systemName) {
        return _conditionalMap.get(systemName);
    }

    /**
     * Set enabled status. Enabled is a bound property All conditionals are set
     * to UNKNOWN state and recalculated when the Logix is enabled, provided the
     * Logix has been previously activated.
     */
    @Override
    public void setEnabled(boolean state) {

        boolean old = mEnabled;
        mEnabled = state;
        if (old != state) {
            boolean active = _isActivated;
            deActivateLogix();
            activateLogix();
            _isActivated = active;
            for (int i = _listeners.size() - 1; i >= 0; i--) {
                _listeners.get(i).setEnabled(state);
            }
            firePropertyChange(PROPERTY_ENABLED, old, state);
        }
    }

    /**
     * Get enabled status
     */
    @Override
    public boolean getEnabled() {
        return mEnabled;
    }

    /**
     * Delete a Conditional and remove it from this Logix
     * <p>
     * Note: Since each Logix must have at least one Conditional to do anything,
     * the user is warned in Logix Table Action when the last Conditional is
     * deleted.
     *
     * @param systemName The Conditional system name
     * @return null if Conditional was successfully deleted or not present, otherwise
     * returns a string array list of current usage that prevent deletion, used to present
     * a warning dialog to the user
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "null returned is documented in each method to mean completed without problems")
    @Override
    public String[] deleteConditional(String systemName) {
        if (_conditionalSystemNames.isEmpty()) {
            return null;
        }

        // check other Logix(es) for use of this conditional (systemName) for use as a
        // variable in one of their conditionals
        ArrayList<String> checkReferences = conditionalManager.getWhereUsed(systemName);
        if (checkReferences != null) {
            Conditional c = getConditional(systemName);
            String refName = checkReferences.get(0);
            Logix x = conditionalManager.getParentLogix(refName);
            if ( x == null ) {
                log.error("Unable to get Parent Logix {} while deleting Conditional {}",
                    refName, systemName);
                return null;
            }
            Conditional cRef = x.getConditional(refName);
            return new String[]{c.getUserName(), c.getSystemName(), cRef.getUserName(),
                cRef.getSystemName(), x.getUserName(), x.getSystemName()};
        }

        // Confirm the presence of the Conditional object
        Conditional c = conditionalManager.getBySystemName(systemName);
        if (c == null) {
            log.error("attempt to delete non-existing Conditional - {}", systemName);  // NOI18N
            return null;
        }

        // Remove Conditional from this logix
        if (!_conditionalSystemNames.remove(systemName)) {
            log.error("attempt to delete Conditional not in Logix: {}", systemName);  // NOI18N
            return null;
        }

        _conditionalMap.remove(systemName);
        return null;
    }

    /**
     * Calculate all Conditionals, triggering action if the user specified
     * conditions are met, and the Logix is enabled.
     */
    @Override
    public void calculateConditionals() {
        for (String conditionalSystemName : _conditionalSystemNames) {
            Conditional c = getConditional(conditionalSystemName);
            if (c == null) {
                log.error("Invalid conditional system name when calculating Logix - {}",
                    conditionalSystemName);
            } else {
                // calculate without taking any action unless Logix is enabled
                c.calculate(mEnabled, null);
            }
        }
    }

    /**
     * Activate the Logix, starts Logix processing by connecting all inputs that
     * are included the Conditionals in this Logix.
     * <p>
     * A Logix must be activated before it will calculate any of its
     * Conditionals.
     */
    @Override
    public void activateLogix() {
        // if the Logix is already busy, simply return
        if (_isActivated) {
            return;
        }
        // set the state of all Conditionals to UNKNOWN
        resetConditionals();
        // assemble a list of needed listeners
        assembleListenerList();
        // create and attach the needed property change listeners
        // start a minute Listener if needed
        for (JmriSimplePropertyListener listener : _listeners) {
            startListener(listener);
        }
        // mark this Logix as busy
        _isActivated = true;
        // calculate this Logix to set initial state of Conditionals
        calculateConditionals();
    }

    private void resetConditionals() {
        for (String conditionalSystemName : _conditionalSystemNames) {
            Conditional conditional = getConditional(conditionalSystemName);
            if (conditional != null) {
                try {
                    conditional.setState(NamedBean.UNKNOWN);
                } catch (JmriException ignore) {
                }
            }
        }
    }

    // Pattern to check for new style NX system name
    static final Pattern NXUUID = Pattern.compile(
        "^IN:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",   // NOI18N
        Pattern.CASE_INSENSITIVE);

    /**
     * ConditionalVariables only have a single name field.  For user interface purposes
     * a gui name is used for the referenced conditional user name.  This is not used
     * for other object types.
     * <p>
     * In addition to setting the GUI name, any state variable references are changed to
     * conditional system names.  This converts the XML system/user name field to the system name
     * for conditional references.  It does not affect other objects such as sensors, turnouts, etc.
     * <p>
     * For Entry/Exit references, replace NX user names and old style NX UUID references
     * with the new style "IN:" + UUID reference.  If the referenced NX does not exist,
     * it will be removed from the Variable or Action list. (4.11.4)
     * <p>
     * Called by {@link jmri.managers.DefaultLogixManager#activateAllLogixs}
     * @since 4.7.4
     */
    @Override
    public void setGuiNames() {
        if (_isGuiSet) {
            return;
        }
        if ("SYS".equals(getSystemName())) {
            _isGuiSet = true;
            return;
        }
        for (String cName : _conditionalSystemNames) {
            Conditional conditional = getConditional(cName);
            if (conditional == null) {
                // A Logix index entry exists without a corresponding conditional.  This
                // should never happen.
                log.error("setGuiNames: Missing conditional for Logix index entry, "
                    + " Logix name = '{}', Conditional index name = '{}'",  // NOI18N
                        getSystemName(), cName);
                continue;
            }
            List<ConditionalVariable> varList = conditional.getCopyOfStateVariables();
            boolean isDirty = false;
            ArrayList<ConditionalVariable> badVariable = new ArrayList<>();
            for (ConditionalVariable condVar : varList) {
                // Find any Conditional State Variables
                if (condVar.getType() == Conditional.Type.CONDITIONAL_TRUE
                        || condVar.getType() == Conditional.Type.CONDITIONAL_FALSE) {
                    // Get the referenced (target) conditonal -- The name can be either a system name or a user name
                    Conditional cRef = conditionalManager.getConditional(condVar.getName());
                    if (cRef != null) {
                        // re-arrange names as needed
                        condVar.setName(cRef.getSystemName()); // The state variable reference is now a conditional system name
                        String uName = cRef.getUserName();
                        if (uName == null || uName.isEmpty()) {
                            condVar.setGuiName(cRef.getSystemName());
                        } else {
                            condVar.setGuiName(uName);
                        }
                        // Add the conditional reference to the where used map
                        conditionalManager.addWhereUsed(condVar.getName(), cName);
                        isDirty = true;
                    } else {
                        log.error("setGuiNames: For conditional '{}' in logix '{}', "
                            + "the referenced conditional, '{}',  does not exist",
                                cName, getSystemName(), condVar.getName());
                    }
                }

                // Find any Entry/Exit State Variables
                if (condVar.getType() == Conditional.Type.ENTRYEXIT_ACTIVE
                        || condVar.getType() == Conditional.Type.ENTRYEXIT_INACTIVE) {
                    if (!NXUUID.matcher(condVar.getName()).find()) {
                        // Either a user name or an old style system name (plain UUID)
                        jmri.jmrit.entryexit.DestinationPoints dp =
                            InstanceManager.getDefault(EntryExitPairs.class).
                                getNamedBean(condVar.getName());
                        if (dp != null) {
                            // Replace name with current system name
                            condVar.setName(dp.getSystemName());
                            isDirty = true;
                        } else {
                            log.error("setGuiNames: For conditional '{}' in logix '{}', "
                                + "the referenced Entry Exit Pair, '{}',  does not exist",
                                    cName, getSystemName(), condVar.getName());
                            badVariable.add(condVar);
                        }
                    }
                }
            }
            if (!badVariable.isEmpty()) {
                isDirty = true;
                badVariable.forEach(varList::remove);
            }
            if (isDirty) {
                conditional.setStateVariables(varList);
            }

            List<ConditionalAction> actionList = conditional.getCopyOfActions();
            isDirty = false;
            ArrayList<ConditionalAction> badAction = new ArrayList<>();
            for (ConditionalAction action : actionList) {
                // Find any Entry/Exit Actions
                if (action.getType() == Conditional.Action.SET_NXPAIR_ENABLED
                    || action.getType() == Conditional.Action.SET_NXPAIR_DISABLED
                        || action.getType() == Conditional.Action.SET_NXPAIR_SEGMENT) {
                    if (!NXUUID.matcher(action.getDeviceName()).find()) {
                        // Either a user name or an old style system name (plain UUID)
                        jmri.jmrit.entryexit.DestinationPoints dp =
                            InstanceManager.getDefault(EntryExitPairs.class).
                                getNamedBean(action.getDeviceName());
                        if (dp != null) {
                            // Replace name with current system name
                            action.setDeviceName(dp.getSystemName());
                            isDirty = true;
                        } else {
                            log.error("setGuiNames: For conditional '{}' in logix '{}', "
                                + "the referenced Entry Exit Pair, '{}',  does not exist",  // NOI18N
                                    cName, getSystemName(), action.getDeviceName());
                            badAction.add(action);
                        }
                    }
                }
            }
            if (!badAction.isEmpty()) {
                isDirty = true;
                badAction.forEach(actionList::remove);
            }
            if (isDirty) {
                conditional.setAction(actionList);
            }
        }
        _isGuiSet = true;
    }

    /**
     * Assemble a list of Listeners needed to activate this Logix.
     */
    private void assembleListenerList() {
        // initialize by cleaning up
        // start from end down to safely delete preventing concurrent modification ex
        for (int i = _listeners.size() - 1; i >= 0; i--) {
            removeListener(_listeners.get(i));
        }
        _listeners = new ArrayList<>();
        // cycle thru Conditionals to find objects to listen to
        for (int i = 0; i < _conditionalSystemNames.size(); i++) {
            Conditional conditional = getConditional(_conditionalSystemNames.get(i));
            if (conditional != null) {
                List<ConditionalVariable> variableList = conditional.getCopyOfStateVariables();
                for (ConditionalVariable variable : variableList) {
                    // check if listening for a change has been suppressed
                    int varListenerType = 0;
                    String varName = variable.getName();
                    NamedBeanHandle<?> namedBean = variable.getNamedBean();
                    Conditional.Type varType = variable.getType();
                    int signalAspect = -1;
                    // Get Listener type from variable type
                    switch (varType) {
                        case SENSOR_ACTIVE:
                        case SENSOR_INACTIVE:
                            varListenerType = LISTENER_TYPE_SENSOR;
                            break;
                        case TURNOUT_THROWN:
                        case TURNOUT_CLOSED:
                            varListenerType = LISTENER_TYPE_TURNOUT;
                            break;
                        case CONDITIONAL_TRUE:
                        case CONDITIONAL_FALSE:
                            varListenerType = LISTENER_TYPE_CONDITIONAL;
                            break;
                        case LIGHT_ON:
                        case LIGHT_OFF:
                            varListenerType = LISTENER_TYPE_LIGHT;
                            break;
                        case MEMORY_EQUALS:
                        case MEMORY_COMPARE:
                        case MEMORY_EQUALS_INSENSITIVE:
                        case MEMORY_COMPARE_INSENSITIVE:
                            varListenerType = LISTENER_TYPE_MEMORY;
                            break;
                        case ROUTE_FREE:
                        case ROUTE_OCCUPIED:
                        case ROUTE_ALLOCATED:
                        case ROUTE_SET:
                        case TRAIN_RUNNING:
                            varListenerType = LISTENER_TYPE_WARRANT;
                            break;
                        case FAST_CLOCK_RANGE:
                            varListenerType = LISTENER_TYPE_FASTCLOCK;
                            varName = "clock";  // NOI18N
                            break;
                        case SIGNAL_HEAD_RED:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            signalAspect = SignalHead.RED;
                            break;
                        case SIGNAL_HEAD_YELLOW:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            signalAspect = SignalHead.YELLOW;
                            break;
                        case SIGNAL_HEAD_GREEN:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            signalAspect = SignalHead.GREEN;
                            break;
                        case SIGNAL_HEAD_DARK:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            signalAspect = SignalHead.DARK;
                            break;
                        case SIGNAL_HEAD_LUNAR:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            signalAspect = SignalHead.LUNAR;
                            break;
                        case SIGNAL_HEAD_FLASHRED:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            signalAspect = SignalHead.FLASHRED;
                            break;
                        case SIGNAL_HEAD_FLASHYELLOW:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            signalAspect = SignalHead.FLASHYELLOW;
                            break;
                        case SIGNAL_HEAD_FLASHGREEN:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            signalAspect = SignalHead.FLASHGREEN;
                            break;
                        case SIGNAL_HEAD_FLASHLUNAR:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            signalAspect = SignalHead.FLASHLUNAR;
                            break;
                        case SIGNAL_HEAD_LIT:
                        case SIGNAL_HEAD_HELD:
                            varListenerType = LISTENER_TYPE_SIGNALHEAD;
                            break;
                        case SIGNAL_MAST_ASPECT_EQUALS:
                        case SIGNAL_MAST_LIT:
                        case SIGNAL_MAST_HELD:
                            varListenerType = LISTENER_TYPE_SIGNALMAST;
                            break;
                        case BLOCK_STATUS_EQUALS:
                            varListenerType = LISTENER_TYPE_OBLOCK;
                            break;
                        case ENTRYEXIT_ACTIVE:
                        case ENTRYEXIT_INACTIVE:
                            varListenerType = LISTENER_TYPE_ENTRYEXIT;
                            break;
                        default:
                            if (!LRouteTableAction.getLogixInitializer().equals(varName)) {
                                log.warn("Unhandled conditional variable type: {}", varType);  // NOI18N
                            }
                            break;
                    }
                    int positionOfListener = getPositionOfListener(varListenerType, varType, varName);
                    // add to list if new
                    JmriSimplePropertyListener listener;
                    if (positionOfListener == -1) {
                        switch (varListenerType) {
                            case LISTENER_TYPE_SENSOR:
                                listener = new JmriTwoStatePropertyListener(Sensor.PROPERTY_KNOWN_STATE,
                                    LISTENER_TYPE_SENSOR, namedBean, varType, conditional);
                                break;
                            case LISTENER_TYPE_TURNOUT:
                                listener = new JmriTwoStatePropertyListener(Turnout.PROPERTY_KNOWN_STATE,
                                    LISTENER_TYPE_TURNOUT, namedBean, varType, conditional);
                                break;
                            case LISTENER_TYPE_CONDITIONAL:
                                listener = new JmriTwoStatePropertyListener(Conditional.PROPERTY_KNOWN_STATE,
                                    LISTENER_TYPE_CONDITIONAL, namedBean, varType, conditional);
                                break;
                            case LISTENER_TYPE_LIGHT:
                                listener = new JmriTwoStatePropertyListener(Light.PROPERTY_KNOWN_STATE,
                                    LISTENER_TYPE_LIGHT, namedBean, varType, conditional);
                                break;
                            case LISTENER_TYPE_MEMORY:
                                listener = new JmriTwoStatePropertyListener(Memory.PROPERTY_VALUE,
                                    LISTENER_TYPE_MEMORY, namedBean, varType, conditional);
                                break;
                            case LISTENER_TYPE_WARRANT:
                                listener = new JmriSimplePropertyListener(null,
                                    LISTENER_TYPE_WARRANT, namedBean, varType, conditional);
                                break;
                            case LISTENER_TYPE_FASTCLOCK:
                                listener = new JmriClockPropertyListener(Timebase.PROPERTY_CHANGE_MINUTES,
                                    LISTENER_TYPE_FASTCLOCK, varName, varType, conditional,
                                        variable.getNum1(), variable.getNum2());
                                break;
                            case LISTENER_TYPE_SIGNALHEAD:
                                if (signalAspect < 0) {
                                    if (varType == Conditional.Type.SIGNAL_HEAD_LIT) {
                                        listener = new JmriTwoStatePropertyListener(SignalHead.PROPERTY_LIT,
                                            LISTENER_TYPE_SIGNALHEAD, namedBean, varType, conditional);
                                    } else { // varType == Conditional.TYPE_SIGNAL_HEAD_HELD
                                        listener = new JmriTwoStatePropertyListener(SignalHead.PROPERTY_HELD,
                                            LISTENER_TYPE_SIGNALHEAD, namedBean, varType, conditional);
                                    }
                                } else {
                                    listener = new JmriMultiStatePropertyListener(SignalHead.PROPERTY_APPEARANCE,
                                        LISTENER_TYPE_SIGNALHEAD, namedBean, varType, conditional, signalAspect);
                                }
                                break;
                            case LISTENER_TYPE_SIGNALMAST:
                                switch (varType) {
                                    case SIGNAL_MAST_LIT:
                                        listener = new JmriTwoStatePropertyListener(SignalMast.PROPERTY_LIT,
                                            LISTENER_TYPE_SIGNALMAST,  namedBean, varType, conditional);
                                        break;
                                    case SIGNAL_MAST_HELD:
                                        listener = new JmriTwoStatePropertyListener(SignalMast.PROPERTY_HELD,
                                            LISTENER_TYPE_SIGNALMAST, namedBean, varType, conditional);
                                        break;
                                    default:
                                        listener = new JmriTwoStatePropertyListener(SignalMast.PROPERTY_ASPECT,
                                            LISTENER_TYPE_SIGNALMAST, namedBean, varType, conditional);
                                        break;
                                }
                                break;
                            case LISTENER_TYPE_OBLOCK:
                                listener = new JmriTwoStatePropertyListener(jmri.jmrit.logix.OBlock.PROPERTY_STATE,
                                    LISTENER_TYPE_OBLOCK, namedBean, varType, conditional);
                                break;
                            case LISTENER_TYPE_ENTRYEXIT:
                                listener = new JmriTwoStatePropertyListener(EntryExitPairs.PROPERTY_ACTIVE,
                                    LISTENER_TYPE_ENTRYEXIT, namedBean, varType, conditional);
                                break;
                            default:
                                if (!LRouteTableAction.getLogixInitializer().equals(varName)) {
                                    log.error("Unknown (new) Variable Listener type= {}, for varName= {}, varType= {} in Conditional, {}",
                                        varListenerType, varName, varType, _conditionalSystemNames.get(i));
                                }
                                continue;
                        }
                        _listeners.add(listener);
                    } else {
                        switch (varListenerType) {
                            case LISTENER_TYPE_SENSOR:
                            case LISTENER_TYPE_TURNOUT:
                            case LISTENER_TYPE_CONDITIONAL:
                            case LISTENER_TYPE_LIGHT:
                            case LISTENER_TYPE_MEMORY:
                            case LISTENER_TYPE_WARRANT:
                            case LISTENER_TYPE_SIGNALMAST:
                            case LISTENER_TYPE_OBLOCK:
                            case LISTENER_TYPE_ENTRYEXIT:
                                listener = _listeners.get(positionOfListener);
                                listener.addConditional(conditional);
                                break;
                            case LISTENER_TYPE_FASTCLOCK:
                                JmriClockPropertyListener cpl = (JmriClockPropertyListener) _listeners.get(positionOfListener);
                                cpl.setRange(variable.getNum1(), variable.getNum2());
                                cpl.addConditional(conditional);
                                break;
                            case LISTENER_TYPE_SIGNALHEAD:
                                if (signalAspect < 0) {
                                    listener = _listeners.get(positionOfListener);
                                    listener.addConditional(conditional);
                                } else {
                                    JmriMultiStatePropertyListener mpl = (JmriMultiStatePropertyListener) _listeners.get(positionOfListener);
                                    mpl.addConditional(conditional);
                                    mpl.setState(signalAspect);
                                }
                                break;
                            default:
                                log.error("Unknown (old) Variable Listener type= {}, for varName= {}, varType= {} in Conditional, {}",
                                        varListenerType, varName, varType, _conditionalSystemNames.get(i));
                        }
                    }
                    // addition listeners needed for memory compare
                    if (varType == Conditional.Type.MEMORY_COMPARE || varType == Conditional.Type.MEMORY_COMPARE_INSENSITIVE) {
                        positionOfListener = getPositionOfListener(varListenerType, varType, variable.getDataString());
                        if (positionOfListener == -1) {
                            String name = variable.getDataString();
                            try {
                                Memory my = InstanceManager.memoryManagerInstance().provideMemory(name);
                                NamedBeanHandle<?> nb = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, my);

                                listener = new JmriTwoStatePropertyListener(Memory.PROPERTY_VALUE, LISTENER_TYPE_MEMORY,
                                        nb, varType, conditional);
                                _listeners.add(listener);
                            } catch (IllegalArgumentException ex) {
                                log.error("invalid memory name= \"{}\" in state variable", name);  // NOI18N
                                break;
                            }
                        } else {
                            listener = _listeners.get(positionOfListener);
                            listener.addConditional(conditional);
                        }
                    }
                }
            } else {
                log.error("invalid conditional system name in Logix \"{}\" "
                        + "assembleListenerList DELETING {} from Conditional list.",
                    getSystemName(), _conditionalSystemNames.get(i));
                _conditionalSystemNames.remove(i);
            }
        }
    }

    private int getPositionOfListener(int varListenerType, Conditional.Type varType, String varName) {
        // check if already in list
        for (int j = 0; (j < _listeners.size()); j++) {
            if (varListenerType == _listeners.get(j).getType()) {
                if (varName.equals(_listeners.get(j).getDevName())) {
                    if (varListenerType == LISTENER_TYPE_SIGNALHEAD) {
                        if (varType == Conditional.Type.SIGNAL_HEAD_LIT
                                || varType == Conditional.Type.SIGNAL_HEAD_HELD) {
                            if (varType == _listeners.get(j).getVarType()) {
                                return j;
                            }
                        } else if (SignalHead.PROPERTY_APPEARANCE.equals(_listeners.get(j).getPropertyName())) {
                            // the Appearance Listener can handle all aspects
                            return j;
                        }
                    } else {
                        return j;
                    }
                }
            }

        }
        return -1;
    }

    /* /**
     * Assembles and returns a list of state variables that are used by
     * conditionals of this Logix including the number of occurances of each
     * variable that trigger a calculation, and the number of occurances where
     * the triggering has been suppressed. The main use of this method is to
     * return information that can be used to test for inconsistency in
     * suppressing triggering of a calculation among multiple occurances of the
     * same state variable. Caller provides an ArrayList of the variables to
     * check and an empty Array list to return the counts for triggering or
     * suppressing calculation. The first index is a count that the
     * correspondeing variable triggers calculation and second is a count that
     * the correspondeing variable suppresses Calculation. Note this method must
     * not modify the supplied variable list in any way.
     *
     * public void getStateVariableList(ArrayList <ConditionalVariable> varList,
     * ArrayList <int[]> triggerPair) { // initialize Conditional c = null;
     * String testSystemName = ""; String testUserName = ""; String testVarName
     * = ""; // cycle thru Conditionals to find state variables
     * ConditionalManager cm = InstanceManager.getDefault(jmri.ConditionalManager.class); for
     * (int i=0; i<_conditionalSystemNames.size(); i++) { c =
     * cm.getBySystemName(_conditionalSystemNames.get(i)); if (c!=null) {
     * ArrayList variableList = c.getCopyOfStateVariables(); for (int k = 0;
     * k<variableList.size(); k++) { ConditionalVariable variable =
     * (ConditionalVariable)variableList.get(k); testVarName =
     * variable.getName(); testSystemName = ""; testUserName = ""; // initialize
     * this state variable switch (variable.getType()) { case
     * Conditional.TYPE_SENSOR_ACTIVE: case Conditional.TYPE_SENSOR_INACTIVE:
     * Sensor s = InstanceManager.sensorManagerInstance().
     * getSensor(testVarName); if (s!=null) { testSystemName =
     * s.getSystemName(); testUserName = s.getUserName(); } break; case
     * Conditional.TYPE_TURNOUT_THROWN: case Conditional.TYPE_TURNOUT_CLOSED:
     * Turnout t = InstanceManager.turnoutManagerInstance().
     * getTurnout(testVarName); if (t!=null) { testSystemName =
     * t.getSystemName(); testUserName = t.getUserName(); } break; case
     * Conditional.TYPE_CONDITIONAL_TRUE: case
     * Conditional.TYPE_CONDITIONAL_FALSE: Conditional cx =
     * InstanceManager.getDefault(jmri.ConditionalManager.class).
     * getConditional(this,testVarName); if (cx==null) { cx =
     * InstanceManager.getDefault(jmri.ConditionalManager.class).
     * getBySystemName(testVarName); } if (cx!=null) { testSystemName =
     * cx.getSystemName(); testUserName = cx.getUserName(); } break; case
     * Conditional.TYPE_LIGHT_ON: case Conditional.TYPE_LIGHT_OFF: Light lgt =
     * InstanceManager.lightManagerInstance(). getLight(testVarName); if
     * (lgt!=null) { testSystemName = lgt.getSystemName(); testUserName =
     * lgt.getUserName(); } break; case Conditional.TYPE_MEMORY_EQUALS: Memory m
     * = InstanceManager.memoryManagerInstance(). getMemory(testVarName); if
     * (m!=null) { testSystemName = m.getSystemName(); testUserName =
     * m.getUserName(); } break; case Conditional.TYPE_SIGNAL_HEAD_RED: case
     * Conditional.TYPE_SIGNAL_HEAD_YELLOW: case
     * Conditional.TYPE_SIGNAL_HEAD_GREEN: case
     * Conditional.TYPE_SIGNAL_HEAD_DARK: case
     * Conditional.TYPE_SIGNAL_HEAD_FLASHRED: case
     * Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW: case
     * Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN: SignalHead h =
     * InstanceManager.getDefault(jmri.SignalHeadManager.class). getSignalHead(testVarName);
     * if (h!=null) { testSystemName = h.getSystemName(); testUserName =
     * h.getUserName(); } break; case Conditional.TYPE_SIGNAL_HEAD_LIT:
     * SignalHead hx = InstanceManager.getDefault(jmri.SignalHeadManager.class).
     * getSignalHead(testVarName); if (hx!=null) { testSystemName =
     * hx.getSystemName(); testUserName = hx.getUserName(); } break; case
     * Conditional.TYPE_SIGNAL_HEAD_HELD: SignalHead hy =
     * InstanceManager.getDefault(jmri.SignalHeadManager.class). getSignalHead(testVarName);
     * if (hy!=null) { testSystemName = hy.getSystemName(); testUserName =
     * hy.getUserName(); } break; default: testSystemName = ""; } // check if
     * this state variable is already in the list to be returned boolean inList
     * = false; int indexOfRepeat = -1; if (testSystemName!="") { // getXXXXXX
     * succeeded, process this state variable for (int j=0; j<varList.size();
     * j++) { ConditionalVariable v = varList.get(j); if (
     * v.getName().equals(testSystemName) || v.getName().equals(testUserName) )
     * { inList = true; indexOfRepeat = j; break; } } // add to list if new and
     * if there is room if ( inList ) { int[] trigs =
     * triggerPair.get(indexOfRepeat); if ( variable.doCalculation() ) {
     * trigs[0]++; } else { trigs[1]++;
     *
     * }
     * }
     * }
     * }
     * }
     * else { log.error("invalid conditional system name in Logix
     * getStateVariableList - "+ _conditionalSystemNames.get(i));
     *
     * }
     * }
     * } // getStateVariableList
     */

    /**
     * Deactivate the Logix. This method disconnects the Logix from all input
     * objects and stops it from being triggered to calculate.
     * <p>
     * A Logix must be deactivated before its Conditionals are changed.
     */
    @Override
    public void deActivateLogix() {
        if (_isActivated) {
            // Logix is active, deactivate it and all listeners
            _isActivated = false;
            // remove listeners if there are any
            for (int i = _listeners.size() - 1; i >= 0; i--) {
                removeListener(_listeners.get(i));
            }
        }
    }

    /**
     * Creates a listener of the required type and starts it
     */
    private void startListener(JmriSimplePropertyListener listener) {

        if (listener.getType() == LISTENER_TYPE_FASTCLOCK) {
            Timebase tb = InstanceManager.getDefault(Timebase.class);
            tb.addMinuteChangeListener(listener);
        } else {
            NamedBeanHandle<?> namedBeanHandle = listener.getNamedBean();
            if (namedBeanHandle == null) {
                log.error("Bad name for {} '{}' when setting up Logix listener [ {} ]",
                    getListenerTypeName(listener.getType()), listener.getDevName(), this.getSystemName());
            } else {
                NamedBean nb = namedBeanHandle.getBean();
                nb.addPropertyChangeListener(listener, namedBeanHandle.getName(),
                    "Logix " + getDisplayName());
            }
        }
    }

    /**
     * Remove a listener of the required type
     */
    private void removeListener(JmriSimplePropertyListener listener) {
        String typeName = null;
        NamedBean nb;
        NamedBeanHandle<?> namedBeanHandle;
        try {
            switch (listener.getType()) {
                case LISTENER_TYPE_FASTCLOCK:
                    Timebase tb = InstanceManager.getDefault(Timebase.class);
                    tb.removeMinuteChangeListener(listener);
                    return;
                case LISTENER_TYPE_ENTRYEXIT:
                    NamedBean ex = InstanceManager.getDefault(EntryExitPairs.class)
                            .getNamedBean(listener.getDevName());
                    if (ex == null) {
                        typeName = "entryexit";  // NOI18N
                        break;
                    }
                    ex.removePropertyChangeListener(listener);
                    return;
                default:
                    namedBeanHandle = listener.getNamedBean();
                    if (namedBeanHandle == null) {
                        typeName = getListenerTypeName(listener.getType());
                        break;
                    }
                    nb = namedBeanHandle.getBean();
                    nb.removePropertyChangeListener(listener);
                    return;
            }
        } catch (Exception ex) {
            log.error("Bad name for listener on \"{}\": ", listener.getDevName(), ex);  // NOI18N
        }
        log.error("Bad name for {} listener on \"{}\" when removing", typeName, listener.getDevName());  // NOI18N
    }

    /**
     * Get an I18N String of the Bean Listener type.
     * @param listenerType the LISTENER_TYPE constant.
     * @return I18N String of the Bean type, non-plural.
     */
    private static String getListenerTypeName(int listenerType) {
        String msg;
        switch (listenerType) {
            case LISTENER_TYPE_SENSOR:
                msg = InstanceManager.getDefault(SensorManager.class).getBeanTypeHandled();
                break;
            case LISTENER_TYPE_TURNOUT:
                msg = InstanceManager.getDefault(TurnoutManager.class).getBeanTypeHandled();
                break;
            case LISTENER_TYPE_LIGHT:
                msg = InstanceManager.getDefault(LightManager.class).getBeanTypeHandled();
                break;
            case LISTENER_TYPE_CONDITIONAL:
                msg = InstanceManager.getDefault(ConditionalManager.class).getBeanTypeHandled();
                break;
            case LISTENER_TYPE_SIGNALHEAD:
                msg = InstanceManager.getDefault(SignalHeadManager.class).getBeanTypeHandled();
                break;
            case LISTENER_TYPE_SIGNALMAST:
                msg = InstanceManager.getDefault(SignalMastManager.class).getBeanTypeHandled();
                break;
            case LISTENER_TYPE_MEMORY:
                msg = InstanceManager.getDefault(MemoryManager.class).getBeanTypeHandled();
                break;
            case LISTENER_TYPE_WARRANT:
                msg = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class).getBeanTypeHandled();
                break;
            case LISTENER_TYPE_OBLOCK:
                msg = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBeanTypeHandled();
                break;
            case LISTENER_TYPE_ENTRYEXIT:
                msg = InstanceManager.getDefault(EntryExitPairs.class).getBeanTypeHandled();
                break;
            default:
                msg = "unknown Listener type number: " + listenerType;  // NOI18N
        }
        return msg;
    }

    /* /**
     * Assembles a list of state variables that both trigger the Logix, and are
     * changed by it. Returns true if any such variables were found. Returns
     * false otherwise. Can be called when Logix is enabled.
     *
     * public boolean checkLoopCondition() { loopGremlins = new
     * ArrayList<String[]>(); if (!_isActivated) { // Prepare a list of all
     * variables used in conditionals java.util.HashSet <ConditionalVariable>
     * variableList = new java.util.HashSet<ConditionalVariable>();
     * ConditionalManager cm = InstanceManager.getDefault(jmri.ConditionalManager.class); for
     * (int i=0; i<_conditionalSystemNames.size(); i++) { Conditional c = null;
     * c = cm.getBySystemName(_conditionalSystemNames.get(i)); if (c!=null) { //
     * Not necesary to modify methods, equals and hashcode. Redundacy checked in
     * addGremlin variableList.addAll(c.getCopyOfStateVariables()); } }
     * java.util.HashSet <ConditionalVariable> variableList = new
     * java.util.HashSet<ConditionalVariable>(); ConditionalVariable v = null;
     * // check conditional action items Conditional c = null; for (int i=0;
     * i<_conditionalSystemNames.size(); i++) { // get next conditional c =
     * cm.getBySystemName(_conditionalSystemNames.get(i)); if (c!=null) {
     * ArrayList <ConditionalAction> actionList = c.getCopyOfActions(); for (int
     * j = 0; j < actionList.size(); j++) { ConditionalAction action =
     * actionList.get(j); String sName = ""; String uName = ""; switch
     * (action.getType()) { case Conditional.ACTION_NONE: break; case
     * Conditional.ACTION_SET_TURNOUT: case Conditional.ACTION_DELAYED_TURNOUT:
     * case Conditional.ACTION_RESET_DELAYED_TURNOUT: case
     * Conditional.ACTION_CANCEL_TURNOUT_TIMERS: Turnout t =
     * InstanceManager.turnoutManagerInstance().
     * provideTurnout(action.getDeviceName()); if (t!=null) { sName =
     * t.getSystemName(); uName = t.getUserName(); // check for action on the
     * same turnout Iterator <ConditionalVariable>it= variableList.iterator();
     * while(it.hasNext()) { v = it.next(); if (v.getType() ==
     * Conditional.TYPE_TURNOUT_CLOSED || v.getType() ==
     * Conditional.TYPE_TURNOUT_THROWN) { if ( (v.getName().equals(sName)) ||
     * (v.getName().equals(uName)) ) { // possible conflict found
     * addGremlin("Turnout", sName, uName); } } } } break; case
     * Conditional.ACTION_SET_SIGNAL_APPEARANCE: case
     * Conditional.ACTION_SET_SIGNAL_HELD: case
     * Conditional.ACTION_CLEAR_SIGNAL_HELD: case
     * Conditional.ACTION_SET_SIGNAL_DARK: case
     * Conditional.ACTION_SET_SIGNAL_LIT: SignalHead h =
     * InstanceManager.getDefault(jmri.SignalHeadManager.class).
     * getSignalHead(action.getDeviceName()); if (h!=null) { sName =
     * h.getSystemName(); uName = h.getUserName(); // check for action on the
     * same signal head Iterator <ConditionalVariable>it=
     * variableList.iterator(); while(it.hasNext()) { v = it.next(); if
     * (v.getType() >= Conditional.TYPE_SIGNAL_HEAD_RED || v.getType() <=
     * Conditional.TYPE_SIGNAL_HEAD_HELD) { if ( (v.getName().equals(sName)) ||
     * (v.getName().equals(uName)) ) { // possible conflict found
     * addGremlin("SignalHead", sName, uName); } } } } break; case
     * Conditional.ACTION_SET_SENSOR: case Conditional.ACTION_DELAYED_SENSOR:
     * case Conditional.ACTION_RESET_DELAYED_SENSOR: case
     * Conditional.ACTION_CANCEL_SENSOR_TIMERS: Sensor s =
     * InstanceManager.sensorManagerInstance().
     * provideSensor(action.getDeviceName()); if (s!=null) { sName =
     * s.getSystemName(); uName = s.getUserName(); // check for action on the
     * same sensor Iterator <ConditionalVariable>it= variableList.iterator();
     * while(it.hasNext()) { v = it.next(); if (v.getType() ==
     * Conditional.TYPE_SENSOR_ACTIVE || v.getType() ==
     * Conditional.TYPE_SENSOR_INACTIVE) {
     *
     * if ( (v.getName().equals(sName)) || (v.getName().equals(uName)) ) { //
     * possible conflict found addGremlin("Sensor",sName, uName); } } } } break;
     * case Conditional.ACTION_SET_LIGHT: case
     * Conditional.ACTION_SET_LIGHT_TRANSITION_TIME: case
     * Conditional.ACTION_SET_LIGHT_INTENSITY: Light lgt =
     * InstanceManager.lightManagerInstance(). getLight(action.getDeviceName());
     * if (lgt!=null) { sName = lgt.getSystemName(); uName = lgt.getUserName();
     * // check for listener on the same light Iterator <ConditionalVariable>it=
     * variableList.iterator(); while(it.hasNext()) { v = it.next(); if
     * (v.getType() == Conditional.TYPE_LIGHT_ON || v.getType() ==
     * Conditional.TYPE_LIGHT_OFF) { if ( (v.getName().equals(sName)) ||
     * (v.getName().equals(uName)) ) { // possible conflict found
     * addGremlin("Light", sName, uName); } } } } break; case
     * Conditional.ACTION_SET_MEMORY: case Conditional.ACTION_COPY_MEMORY:
     * Memory m = InstanceManager.memoryManagerInstance().
     * provideMemory(action.getDeviceName()); if (m!=null) { sName =
     * m.getSystemName(); uName = m.getUserName(); // check for variable on the
     * same memory Iterator <ConditionalVariable>it= variableList.iterator();
     * while(it.hasNext()) { v = it.next(); if (v.getType() ==
     * Conditional.TYPE_MEMORY_EQUALS) { if ( (v.getName().equals(sName)) ||
     * (v.getName().equals(uName)) ) { // possible conflict found
     * addGremlin("Memory", sName, uName); } } } } break; case
     * Conditional.ACTION_SET_FAST_CLOCK_TIME: case
     * Conditional.ACTION_START_FAST_CLOCK: case
     * Conditional.ACTION_STOP_FAST_CLOCK: Iterator <ConditionalVariable>it=
     * variableList.iterator(); while(it.hasNext()) { v = it.next(); if
     * (v.getType() == Conditional.TYPE_FAST_CLOCK_RANGE) {
     * addGremlin("FastClock", null, v.getName()); } } break; default: } } } } }
     * return (loopGremlins.size()>0); }
     *
     * private void addGremlin(String type, String sName, String uName) { //
     * check for redundancy String names = uName+ (sName == null ? "" : "
     * ("+sName+")"); for (int i=0; i<loopGremlins.size(); i++) { String[] str =
     * loopGremlins.get(i); if (str[0].equals(type) && str[1].equals(names)) {
     * return; } } String[] item = new String[2]; item[0] = type; item[1] =
     * names; loopGremlins.add(item); }
     *
     * ArrayList <String[]> loopGremlins = null;
     *
     * /**
     * Returns a string listing state variables that might result in a loop.
     * Returns an empty string if there are none, probably because
     * "checkLoopCondition" was not invoked before the call, or returned false.
     *
     * public ArrayList
     * <String[]> getLoopGremlins() {return(loopGremlins);}
     */

    /**
     * Not needed for Logixs - included to complete implementation of the
     * NamedBean interface.
     */
    @Override
    public int getState() {
        log.warn("Unexpected call to getState in DefaultLogix.");  // NOI18N
        return UNKNOWN;
    }

    /**
     * Not needed for Logixs - included to complete implementation of the
     * NamedBean interface.
     * @param state unused.
     */
    @Override
    public void setState(int state) {
        log.warn("Unexpected call to setState in DefaultLogix.");  // NOI18N
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if (Manager.PROPERTY_CAN_DELETE.equals(evt.getPropertyName())) {
            NamedBean nb = (NamedBean) evt.getOldValue();
            for (JmriSimplePropertyListener listener : _listeners) {
                if (nb.equals(listener.getBean())) {
                    var e = new java.beans.PropertyChangeEvent(this, Manager.PROPERTY_DO_NOT_DELETE, null, null);
                    throw new java.beans.PropertyVetoException(
                        Bundle.getMessage("InUseLogixListener", nb.getBeanType(), getDisplayName()), e);   // NOI18N
                }
            }

            String cName;
            Conditional c;
            for (String conditionalSystemName : _conditionalSystemNames) {
                cName = conditionalSystemName;
                c = conditionalManager.getBySystemName(cName);
                if (c != null) {
                    for (ConditionalAction ca : c.getCopyOfActions()) {
                        if (nb.equals(ca.getBean())) {
                            var e = new java.beans.PropertyChangeEvent(
                                this, Manager.PROPERTY_DO_NOT_DELETE, null, null);
                            throw new java.beans.PropertyVetoException(
                                Bundle.getMessage("InUseLogixAction", nb.getBeanType(), getDisplayName()), e); // NOI18N
                        }
                    }
                    for (ConditionalVariable v : c.getCopyOfStateVariables()) {
                        if (nb.equals(v.getBean()) || nb.equals(v.getNamedBeanData())) {
                            var e = new java.beans.PropertyChangeEvent(this,
                                Manager.PROPERTY_DO_NOT_DELETE, null, null);
                            throw new java.beans.PropertyVetoException( Bundle.getMessage("InUseLogixVariable",
                                nb.getBeanType(), getDisplayName()), e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            for (int i = 0; i < getNumConditionals(); i++) {
                DefaultConditional cdl = (DefaultConditional) getConditional(getConditionalByNumberOrder(i));
                if ( cdl == null ) {
                    continue;
                }
                cdl.getStateVariableList().forEach( variable -> {
                    if (bean.equals(variable.getBean())) {
                        report.add(new NamedBeanUsageReport("ConditionalVariable", cdl, variable.toString()));
                    }
                    if (bean.equals(variable.getNamedBeanData())) {
                        report.add(new NamedBeanUsageReport("ConditionalVariableData", cdl, variable.toString()));
                    }
                });
                cdl.getActionList().forEach( action -> {
                    if (bean.equals(action.getBean())) {
                        boolean triggerType = cdl.getTriggerOnChange();
                        report.add(new NamedBeanUsageReport("ConditionalAction", cdl, action.description(triggerType)));
                    }
                });
            }
        }
        return report;
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void dispose() {
        super.dispose();
        for (int i = 0; i < getNumConditionals(); i++) {
            Conditional c = getConditional(getConditionalByNumberOrder(i));
            if ( c != null ) {
                c.dispose();
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultLogix.class);

}
