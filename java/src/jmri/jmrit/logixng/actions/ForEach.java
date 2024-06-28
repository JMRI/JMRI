package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.ThreadingUtil;

/**
 * Executes an action when the expression is True.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ForEach extends AbstractDigitalAction
        implements FemaleSocketListener, PropertyChangeListener {

    private final LogixNG_SelectString _selectVariable =
            new LogixNG_SelectString(this, this);

    private final LogixNG_SelectNamedBean<Memory> _selectMemoryNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);

    private boolean _useCommonSource = true;
    private CommonManager _commonManager = CommonManager.Sensors;
    private UserSpecifiedSource _userSpecifiedSource = UserSpecifiedSource.Variable;
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private String _variableName = "";
    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;

    public ForEach(String sys, String user) {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ForEach copy = new ForEach(sysName, userName);
        copy.setComment(getComment());
        copy.setUseCommonSource(_useCommonSource);
        copy.setCommonManager(_commonManager);
        copy.setUserSpecifiedSource(_userSpecifiedSource);
        _selectVariable.copy(copy._selectVariable);
        _selectMemoryNamedBean.copy(copy._selectMemoryNamedBean);
        copy.setFormula(_formula);
        copy.setLocalVariableName(_variableName);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectString getSelectVariable() {
        return _selectVariable;
    }

    public LogixNG_SelectNamedBean<Memory> getSelectMemoryNamedBean() {
        return _selectMemoryNamedBean;
    }

    public void setUseCommonSource(boolean commonSource) {
        this._useCommonSource = commonSource;
    }

    public boolean isUseCommonSource() {
        return _useCommonSource;
    }

    public void setCommonManager(CommonManager commonManager) throws ParserException {
        _commonManager = commonManager;
        parseFormula();
    }

    public CommonManager getCommonManager() {
        return _commonManager;
    }

    public void setUserSpecifiedSource(UserSpecifiedSource userSpecifiedSource) throws ParserException {
        _userSpecifiedSource = userSpecifiedSource;
        parseFormula();
    }

    public UserSpecifiedSource getUserSpecifiedSource() {
        return _userSpecifiedSource;
    }

    public void setFormula(String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    private void parseFormula() throws ParserException {
        if (_userSpecifiedSource == UserSpecifiedSource.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
    }

    /**
     * Get name of local variable
     * @return name of local variable
     */
    public String getLocalVariableName() {
        return _variableName;
    }

    /**
     * Set name of local variable
     * @param localVariableName name of local variable
     */
    public void setLocalVariableName(String localVariableName) {
        _variableName = localVariableName;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.FLOW_CONTROL;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws JmriException {
        SymbolTable symbolTable = getConditionalNG().getSymbolTable();

        AtomicReference<Collection<? extends Object>> collectionRef = new AtomicReference<>();
        AtomicReference<JmriException> ref = new AtomicReference<>();

        final ConditionalNG conditionalNG = getConditionalNG();

        if (_useCommonSource) {
            collectionRef.set(_commonManager.getManager().getNamedBeanSet());
        } else {
            ThreadingUtil.runOnLayoutWithJmriException(() -> {

                Object value = null;

                switch (_userSpecifiedSource) {
                    case Variable:
                        String otherLocalVariable = _selectVariable.evaluateValue(getConditionalNG());
                        Object variableValue = conditionalNG
                                        .getSymbolTable().getValue(otherLocalVariable);

                        value = variableValue;
                        break;

                    case Memory:
                        Memory memory = _selectMemoryNamedBean.evaluateNamedBean(getConditionalNG());
                        if (memory != null) {
                            value = memory.getValue();
                        } else {
                            log.warn("ForEach memory is null");
                        }
                        break;

                    case Formula:
                        if (!_formula.isEmpty() && _expressionNode != null) {
                            value = _expressionNode.calculate(conditionalNG.getSymbolTable());
                        }
                        break;

                    default:
                        // Throw exception
                        throw new IllegalArgumentException("_userSpecifiedSource has invalid value: {}" + _userSpecifiedSource.name());
                }

                if (value instanceof Manager) {
                    collectionRef.set(((Manager<? extends NamedBean>) value).getNamedBeanSet());
                } else if (value != null && value.getClass().isArray()) {
                    // Note: (Object[]) is needed to tell that the parameter is an array and not a vararg argument
                    // See: https://stackoverflow.com/questions/2607289/converting-array-to-list-in-java/2607327#2607327
                    collectionRef.set(Arrays.asList((Object[])value));
                } else if (value instanceof Collection) {
                    collectionRef.set((Collection<? extends Object>) value);
                } else if (value instanceof Map) {
                    collectionRef.set(((Map<?,?>) value).entrySet());
                } else {
                    throw new JmriException(Bundle.getMessage("ForEach_InvalidValue",
                                    value != null ? value.getClass().getName() : null));
                }
            });
        }

        if (ref.get() != null) throw ref.get();

        for (Object o : collectionRef.get()) {
            symbolTable.setValue(_variableName, o);
            try {
                _socket.execute();
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
                // Do nothing, just catch it.
            }
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _socket;

            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ForEach_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        if (_useCommonSource) {
            return Bundle.getMessage(locale, "ForEach_Long_Common",
                    _commonManager.toString(), _variableName, _socket.getName());
        } else {
            switch (_userSpecifiedSource) {
                case Variable:
                    return Bundle.getMessage(locale, "ForEach_Long_LocalVariable",
                            _selectVariable.getDescription(locale), _variableName, _socket.getName());

                case Memory:
                    return Bundle.getMessage(locale, "ForEach_Long_Memory",
                            _selectMemoryNamedBean.getDescription(locale), _variableName, _socket.getName());

                case Formula:
                    return Bundle.getMessage(locale, "ForEach_Long_Formula",
                            _formula, _variableName, _socket.getName());

                default:
                    throw new IllegalArgumentException("_variableOperation has invalid value: " + _userSpecifiedSource.name());
            }
        }
    }

    public FemaleDigitalActionSocket getSocket() {
        return _socket;
    }

    public String getSocketSystemName() {
        return _socketSystemName;
    }

    public void setSocketSystemName(String systemName) {
        _socketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_socket.isConnected()
                    || !_socket.getConnectedSocket().getSystemName()
                            .equals(_socketSystemName)) {

                String socketSystemName = _socketSystemName;
                _socket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _socket.disconnect();
                    if (maleSocket != null) {
                        _socket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action {}", socketSystemName);
                    }
                }
            } else {
                _socket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            if (_userSpecifiedSource == UserSpecifiedSource.Memory) {
                _selectMemoryNamedBean.registerListeners();
            }
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_userSpecifiedSource == UserSpecifiedSource.Memory) {
                _selectMemoryNamedBean.unregisterListeners();
            }
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }


    public enum UserSpecifiedSource {
        Variable(Bundle.getMessage("ForEach_UserSpecifiedSource_Variable")),
        Memory(Bundle.getMessage("ForEach_UserSpecifiedSource_Memory")),
        Formula(Bundle.getMessage("ForEach_UserSpecifiedSource_Formula"));

        private final String _text;

        private UserSpecifiedSource(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }




    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ForEach.class);

}
