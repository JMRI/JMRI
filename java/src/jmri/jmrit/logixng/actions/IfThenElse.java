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

    private Type _type = Type.ExecuteOnChange;
    private final List<ExpressionEntry> _expressionEntries = new ArrayList<>();
    private final List<ActionEntry> _actionEntries = new ArrayList<>();
    private boolean disableCheckForUnconnectedSocket = false;

    public IfThenElse(String sys, String user) {
        super(sys, user);
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, "If")));
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, "Then")));
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, "Else")));
    }

    public IfThenElse(String sys, String user,
            List<Map.Entry<String, String>> expressionSystemNames,
            List<Map.Entry<String, String>> actionSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setExpressionSystemNames(expressionSystemNames);
        setActionSystemNames(actionSystemNames);
    }

    public static String getNewSocketName(String start, String[] names) {
        int x = 1;
        while (x < 10000) {     // Protect from infinite loop
            boolean validName = true;
            for (int i=0; i < names.length; i++) {
                String name = start + Integer.toString(x);
                if (name.equals(names[i])) {
                    validName = false;
                    break;
                }
            }
            if (validName) {
                return "E" + Integer.toString(x);
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
        return getNewSocketName("ElseIf", names);
    }

    public String getNewActionSocketName() {
        String[] names = new String[getChildCount()];
        for (int i=0; i < getChildCount(); i++) {
            names[i] = getChild(i).getName();
        }
        return getNewSocketName("Then", names);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        IfThenElse copy = new IfThenElse(sysName, userName);
        copy.setComment(getComment());
        copy.setType(_type);

        // Ensure the copy has as many childs as myself
        while (copy.getChildCount() < this.getChildCount()) {
            copy.doSocketOperation(copy.getChildCount()-1, FemaleSocketOperation.InsertAfter);
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
        return Category.COMMON;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        for (int i=0; i < _expressionEntries.size(); i++) {
            ExpressionEntry entry = _expressionEntries.get(i);
            boolean result = entry._socket.evaluate();
            TriState _expressionResult = TriState.getValue(result);

            // _lastExpressionResult may be Unknown
            if ((_type == Type.AlwaysExecute) || (_expressionResult != entry._lastExpressionResult)) {
                // Last expression result must be stored as a tri state value, since
                // we must know if the old value is known or not.
                entry._lastExpressionResult = _expressionResult;

                if (result) {
                    _actionEntries.get(i)._socket.execute();
                    return;
                }
            }
        }

        // If here, all expressions where false
        _actionEntries.get(_actionEntries.size()-1)._socket.execute();

    }

    /**
     * Get the type.
     * @return the type
     */
    public Type getType() {
        return _type;
    }

    /**
     * Set the type.
     * @param type the type
     */
    public void setType(Type type) {
        _type = type;
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
        return Bundle.getMessage(locale, "IfThenElse_Long", _type.toString());
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
    public enum Type {
        /**
         * The "then" or "else" action is executed when the expression changes
         * its result. If the expression has returned "false", but now returns
         * "true", the "then" action is executed. If the expression has
         * returned "true", but now returns "false", the "else" action is executed.
         */
        ExecuteOnChange(Bundle.getMessage("IfThenElse_ExecuteOnChange")),

        /**
         * The "then" or "else" action is always executed when this action is
         * executed. If the expression returns "true", the "then" action is
         * executed. If the expression returns "false", the "else" action is
         * executed.
         */
        AlwaysExecute(Bundle.getMessage("IfThenElse_AlwaysExecute"));

        private final String _text;

        private Type(String text) {
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
