package jmri.jmrit.logixng.actions;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Executes an action when the expression is True.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class IfThenElse extends AbstractDigitalAction
        implements FemaleSocketListener {

    private ExecuteType _executeType = ExecuteType.ExecuteOnChange;
    private EvaluateType _evaluateType = EvaluateType.EvaluateAll;
    private final List<ExpressionEntry> _expressionEntries = new ArrayList<>();
    private final List<ActionEntry> _actionEntries = new ArrayList<>();
    private boolean disableCheckForUnconnectedSocket = false;

    public IfThenElse(String sys, String user) {
        super(sys, user);
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, Bundle.getMessage("IfThenElse_Socket_If"))));
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, Bundle.getMessage("IfThenElse_Socket_Then"))));
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, Bundle.getMessage("IfThenElse_Socket_Else"))));
    }

    public IfThenElse(String sys, String user,
            List<Map.Entry<String, String>> expressionSystemNames,
            List<Map.Entry<String, String>> actionSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setExpressionSystemNames(expressionSystemNames);
        setActionSystemNames(actionSystemNames);
    }

    public static String getNewSocketName(String propertyName, String[] names) {
        int x = 1;
        while (x < 10000) {     // Protect from infinite loop
            boolean validName = true;
            String name = Bundle.getMessage(propertyName, x);
            for (int i=0; i < names.length; i++) {
                if (name.equals(names[i])) {
                    validName = false;
                    break;
                }
            }
            if (validName) {
                return name;
            }
            x++;
        }
        throw new RuntimeException("Unable to find a new socket name");
    }

    public String getNewExpressionSocketName() {
        String[] names = new String[getChildCount()];
        for (int i=0; i < getChildCount(); i++) {
            names[i] = getChild(i).getName();
        }
        return getNewSocketName("IfThenElse_Socket_ElseIf", names);
    }

    public String getNewActionSocketName() {
        String[] names = new String[getChildCount()];
        for (int i=0; i < getChildCount(); i++) {
            names[i] = getChild(i).getName();
        }
        return getNewSocketName("IfThenElse_Socket_Then2", names);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        IfThenElse copy = new IfThenElse(sysName, userName);
        copy.setComment(getComment());
        copy.setExecuteType(_executeType);
        copy.setEvaluateType(_evaluateType);

        // Ensure the copy has as many childs as myself
        while (copy.getChildCount() < this.getChildCount()) {
            copy.doSocketOperation(copy.getChildCount()-2, FemaleSocketOperation.InsertAfter);
        }

        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    private void setExpressionSystemNames(List<Map.Entry<String, String>> systemNames) {
        if (!_expressionEntries.isEmpty()) {
            throw new RuntimeException("expression system names cannot be set more than once");
        }

        for (Map.Entry<String, String> entry : systemNames) {
            FemaleDigitalExpressionSocket socket =
                    InstanceManager.getDefault(DigitalExpressionManager.class)
                            .createFemaleSocket(this, this, entry.getKey());

            _expressionEntries.add(new ExpressionEntry(socket, entry.getValue()));
        }
    }

    private void setActionSystemNames(List<Map.Entry<String, String>> systemNames) {
        if (!_actionEntries.isEmpty()) {
            throw new RuntimeException("action system names cannot be set more than once");
        }

        for (Map.Entry<String, String> entry : systemNames) {
            FemaleDigitalActionSocket socket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .createFemaleSocket(this, this, entry.getKey());

            _actionEntries.add(new ActionEntry(socket, entry.getValue()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.FLOW_CONTROL;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        boolean changed = false;

        FemaleDigitalActionSocket socketToExecute = null;

        for (int i=0; i < _expressionEntries.size(); i++) {
            ExpressionEntry entry = _expressionEntries.get(i);
            boolean result = entry._socket.evaluate();
            TriState _expressionResult = TriState.getValue(result);

            // _lastExpressionResult may be Unknown
            if ((_executeType == ExecuteType.AlwaysExecute) || (_expressionResult != entry._lastExpressionResult)) {
                changed = true;

                // Last expression result must be stored as a tri state value, since
                // we must know if the old value is known or not.
                entry._lastExpressionResult = _expressionResult;

                if (result) {
                    if (socketToExecute == null) {
                        socketToExecute = _actionEntries.get(i)._socket;
                    }
                    if (_evaluateType == EvaluateType.EvaluateNeeded) {
                        break;
                    }
                }
            }
        }

        // If here, all expressions where false
        if (changed && socketToExecute == null) {
            socketToExecute = _actionEntries.get(_actionEntries.size()-1)._socket;
        }

        if (socketToExecute != null) {
            socketToExecute.execute();
        } else {
            log.trace("socketToExecute is null");
        }
    }

    /**
     * Get the execute type.
     * @return the type
     */
    public ExecuteType getExecuteType() {
        return _executeType;
    }

    /**
     * Set the execute type.
     * @param type the type
     */
    public void setExecuteType(ExecuteType type) {
        _executeType = type;
    }

    /**
     * Get the execute type.
     * @return the type
     */
    public EvaluateType getEvaluateType() {
        return _evaluateType;
    }

    /**
     * Set the execute type.
     * @param type the type
     */
    public void setEvaluateType(EvaluateType type) {
        _evaluateType = type;
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (index+1 > getChildCount()) {
            throw new IllegalArgumentException(String.format("index has invalid value: %d", index));
        }
        if (index+1 == getChildCount()) {
            return _actionEntries.get(_actionEntries.size()-1)._socket;
        }
        if ((index % 2) == 0) {
            return _expressionEntries.get(index >> 1)._socket;
        } else {
            return _actionEntries.get(index >> 1)._socket;
        }
    }

    @Override
    public int getChildCount() {
        return _expressionEntries.size() + _actionEntries.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSocketOperationAllowed(int index, FemaleSocketOperation oper) {
        int numChilds = getChildCount();

        switch (oper) {
            case Remove:
                // Possible if not the last socket,
                // if there is more than four sockets,
                // the socket is not connected and the next socket is not connected
                return (index >= 0)
                        && (index+2 < numChilds)
                        && (numChilds > 4)
                        && !getChild(index).isConnected()
                        && !getChild(index+1).isConnected();
            case InsertBefore:
                return index >= 0;
            case InsertAfter:
                // Possible if not the last socket
                return index < numChilds-1;
            case MoveUp:
                // Possible, except for the first two sockets and the last two sockets
                return (index >= 2) && (index < numChilds-2);
            case MoveDown:
                // Possible if not the last four sockets
                return (index >= 0) && (index < numChilds-4);
            default:
                throw new UnsupportedOperationException("Oper is unknown" + oper.name());
        }
    }

    private void insertNewSocket(int index) {
        int expressionIndex = index >> 1;
        int actionIndex = index >> 1;

        // Does index points to an action socket instead of an expression socket?
        if ((index % 2) != 0) {
            actionIndex = index >> 1;
            expressionIndex = (index >> 1) + 1;
        }

        FemaleDigitalExpressionSocket exprSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, getNewExpressionSocketName());

        FemaleDigitalActionSocket actionSocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, getNewActionSocketName());

        _expressionEntries.add(expressionIndex, new ExpressionEntry(exprSocket));
        _actionEntries.add(actionIndex, new ActionEntry(actionSocket));

        List<FemaleSocket> addList = new ArrayList<>();
        addList.add(actionSocket);
        addList.add(exprSocket);
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, addList);
    }

    private void removeSocket(int index) {
        int actionIndex = index >> 1;
        int expressionIndex = index >> 1;

        // Does index points to an action socket instead of an expression socket?
        if ((index % 2) != 0) {
            expressionIndex = (index >> 1) + 1;
        }

        List<FemaleSocket> removeList = new ArrayList<>();
        removeList.add(_actionEntries.remove(actionIndex)._socket);
        removeList.add(_expressionEntries.remove(expressionIndex)._socket);

        firePropertyChange(Base.PROPERTY_CHILD_COUNT, removeList, null);
    }

    private void moveSocketDown(int index) {
        int actionIndex = index >> 1;
        int expressionIndex = index >> 1;

        // Does index points to an action socket instead of an expression socket?
        if ((index % 2) != 0) {
            expressionIndex = (index >> 1) + 1;
        }

        ActionEntry actionTemp = _actionEntries.get(actionIndex);
        _actionEntries.set(actionIndex, _actionEntries.get(actionIndex+1));
        _actionEntries.set(actionIndex+1, actionTemp);

        ExpressionEntry exprTemp = _expressionEntries.get(expressionIndex);
        _expressionEntries.set(expressionIndex, _expressionEntries.get(expressionIndex+1));
        _expressionEntries.set(expressionIndex+1, exprTemp);

        List<FemaleSocket> list = new ArrayList<>();
        list.add(_actionEntries.get(actionIndex)._socket);
        list.add(_actionEntries.get(actionIndex+1)._socket);
        list.add(_expressionEntries.get(expressionIndex)._socket);
        list.add(_expressionEntries.get(expressionIndex+1)._socket);
        firePropertyChange(Base.PROPERTY_CHILD_REORDER, null, list);
    }

    /** {@inheritDoc} */
    @Override
    public void doSocketOperation(int index, FemaleSocketOperation oper) {
        switch (oper) {
            case Remove:
                if (index+1 >= getChildCount()) throw new UnsupportedOperationException("Cannot remove only the last socket");
                if (getChild(index).isConnected()) throw new UnsupportedOperationException("Socket is connected");
                if (getChild(index+1).isConnected()) throw new UnsupportedOperationException("Socket below is connected");
                removeSocket(index);
                break;
            case InsertBefore:
                insertNewSocket(index);
                break;
            case InsertAfter:
                insertNewSocket(index+1);
                break;
            case MoveUp:
                if (index < 0) throw new UnsupportedOperationException("cannot move up static sockets");
                if (index <= 1) throw new UnsupportedOperationException("cannot move up first two children");
                moveSocketDown(index-2);
                break;
            case MoveDown:
                if (index+2 >= getChildCount()) throw new UnsupportedOperationException("cannot move down last two children");
                moveSocketDown(index);
                break;
            default:
                throw new UnsupportedOperationException("Oper is unknown" + oper.name());
        }
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (disableCheckForUnconnectedSocket) return;

        for (ExpressionEntry entry : _expressionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
            }
        }
        for (ActionEntry entry : _actionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
            }
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        for (ExpressionEntry entry : _expressionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
            }
        }
        for (ActionEntry entry : _actionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
            }
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "IfThenElse_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "IfThenElse_Long", _executeType.toString());
    }

    public int getNumExpressions() {
        return _expressionEntries.size();
    }

    public FemaleDigitalExpressionSocket getExpressionSocket(int socket) {
        return _expressionEntries.get(socket)._socket;
    }

    public String getExpressionSocketSystemName(int socket) {
        return _expressionEntries.get(socket)._socketSystemName;
    }

    public void setExpressionSocketSystemName(int socket, String systemName) {
        _expressionEntries.get(socket)._socketSystemName = systemName;
    }

    public int getNumActions() {
        return _actionEntries.size();
    }

    public FemaleDigitalActionSocket getActionSocket(int socket) {
        return _actionEntries.get(socket)._socket;
    }

    public String getActionSocketSystemName(int socket) {
        return _actionEntries.get(socket)._socketSystemName;
    }

    public void setActionSocketSystemName(int socket, String systemName) {
        _actionEntries.get(socket)._socketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // We don't want to check for unconnected sockets while setup sockets
        disableCheckForUnconnectedSocket = true;

        try {
            for (ExpressionEntry ee : _expressionEntries) {
                if ( !ee._socket.isConnected()
                        || !ee._socket.getConnectedSocket().getSystemName()
                                .equals(ee._socketSystemName)) {

                    String socketSystemName = ee._socketSystemName;
                    ee._socket.disconnect();
                    if (socketSystemName != null) {
                        MaleSocket maleSocket =
                                InstanceManager.getDefault(DigitalExpressionManager.class)
                                        .getBySystemName(socketSystemName);
                        ee._socket.disconnect();
                        if (maleSocket != null) {
                            ee._socket.connect(maleSocket);
                            maleSocket.setup();
                        } else {
                            log.error("cannot load digital expression {}", socketSystemName);
                        }
                    }
                } else {
                    ee._socket.getConnectedSocket().setup();
                }
            }

            for (ActionEntry ae : _actionEntries) {
                if ( !ae._socket.isConnected()
                        || !ae._socket.getConnectedSocket().getSystemName()
                                .equals(ae._socketSystemName)) {

                    String socketSystemName = ae._socketSystemName;
                    ae._socket.disconnect();
                    if (socketSystemName != null) {
                        MaleSocket maleSocket =
                                InstanceManager.getDefault(DigitalActionManager.class)
                                        .getBySystemName(socketSystemName);
                        ae._socket.disconnect();
                        if (maleSocket != null) {
                            ae._socket.connect(maleSocket);
                            maleSocket.setup();
                        } else {
                            log.error("cannot load digital action {}", socketSystemName);
                        }
                    }
                } else {
                    ae._socket.getConnectedSocket().setup();
                }
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }

        disableCheckForUnconnectedSocket = false;
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    /**
     * The type of Action. If the type is changed, the action is aborted if it
     * is currently running.
     */
    public enum ExecuteType {
        /**
         * The "then" or "else" action is executed when the expression changes
         * its result. If the expression has returned "false", but now returns
         * "true", the "then" action is executed. If the expression has
         * returned "true", but now returns "false", the "else" action is executed.
         */
        ExecuteOnChange(Bundle.getMessage("IfThenElse_ExecuteType_ExecuteOnChange")),

        /**
         * The "then" or "else" action is always executed when this action is
         * executed. If the expression returns "true", the "then" action is
         * executed. If the expression returns "false", the "else" action is
         * executed.
         */
        AlwaysExecute(Bundle.getMessage("IfThenElse_ExecuteType_AlwaysExecute"));

        private final String _text;

        private ExecuteType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    public enum EvaluateType {
        /**
         * All the connected expression sockets are evaluated.
         */
        EvaluateAll(Bundle.getMessage("IfThenElse_EvaluateType_EvaluateAll")),

        /**
         * Evaluation starts with the first expression socket and continues
         * until all sockets are evaluated or the result is known.
         */
        EvaluateNeeded(Bundle.getMessage("IfThenElse_EvaluateType_EvaluateNeeded"));

        private final String _text;

        private EvaluateType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    private static enum TriState {
        Unknown,
        False,
        True;

        public static TriState getValue(boolean value) {
            return value ? True : False;
        }
    }

    private static class ExpressionEntry {
        private String _socketSystemName;
        private final FemaleDigitalExpressionSocket _socket;
        private TriState _lastExpressionResult = TriState.Unknown;

        private ExpressionEntry(FemaleDigitalExpressionSocket socket, String socketSystemName) {
            _socketSystemName = socketSystemName;
            _socket = socket;
        }

        private ExpressionEntry(FemaleDigitalExpressionSocket socket) {
            this._socket = socket;
        }

    }

    private static class ActionEntry {
        private String _socketSystemName;
        private final FemaleDigitalActionSocket _socket;

        private ActionEntry(FemaleDigitalActionSocket socket, String socketSystemName) {
            _socketSystemName = socketSystemName;
            _socket = socket;
        }

        private ActionEntry(FemaleDigitalActionSocket socket) {
            this._socket = socket;
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IfThenElse.class);

}
