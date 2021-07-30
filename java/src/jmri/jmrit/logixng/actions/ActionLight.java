package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action sets the state of a light.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionLight extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Light> _lightHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private LightState _lightState = LightState.On;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    private NamedBeanAddressing _dataAddressing = NamedBeanAddressing.Direct;
    private String _dataReference = "";
    private String _dataLocalVariable = "";
    private String _dataFormula = "";
    private ExpressionNode _dataExpressionNode;

    private int _lightValue= 0;

    public ActionLight(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionLight copy = new ActionLight(sysName, userName);
        copy.setComment(getComment());
        if (_lightHandle != null) copy.setLight(_lightHandle);
        copy.setBeanState(_lightState);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);

        copy.setStateAddressing(_stateAddressing);
        copy.setStateFormula(_stateFormula);
        copy.setStateLocalVariable(_stateLocalVariable);
        copy.setStateReference(_stateReference);

        copy.setDataAddressing(_dataAddressing);
        copy.setDataReference(_dataReference);
        copy.setDataLocalVariable(_dataLocalVariable);
        copy.setDataFormula(_dataFormula);

        copy.setLightValue(_lightValue);

        return manager.registerAction(copy);
    }

    public void setLight(@Nonnull String lightName) {
        assertListenersAreNotRegistered(log, "setLight");
        Light light = InstanceManager.getDefault(LightManager.class).getLight(lightName);
        if (light != null) {
            setLight(light);
        } else {
            removeLight();
            log.warn("light \"{}\" is not found", lightName);
        }
    }

    public void setLight(@Nonnull NamedBeanHandle<Light> handle) {
        assertListenersAreNotRegistered(log, "setLight");
        _lightHandle = handle;
        InstanceManager.lightManagerInstance().addVetoableChangeListener(this);
    }

    public void setLight(@Nonnull Light light) {
        assertListenersAreNotRegistered(log, "setLight");
        setLight(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(light.getDisplayName(), light));
    }

    public void removeLight() {
        assertListenersAreNotRegistered(log, "setLight");
        if (_lightHandle != null) {
            InstanceManager.lightManagerInstance().removeVetoableChangeListener(this);
            _lightHandle = null;
        }
    }

    public NamedBeanHandle<Light> getLight() {
        return _lightHandle;
    }

    public void setAddressing(NamedBeanAddressing addressing) throws ParserException {
        _addressing = addressing;
        parseFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _reference = reference;
    }

    public String getReference() {
        return _reference;
    }

    public void setLocalVariable(@Nonnull String localVariable) {
        _localVariable = localVariable;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setFormula(@Nonnull String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    private void parseFormula() throws ParserException {
        if (_addressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
    }


    public void setStateAddressing(NamedBeanAddressing addressing) throws ParserException {
        _stateAddressing = addressing;
        parseStateFormula();
    }

    public NamedBeanAddressing getStateAddressing() {
        return _stateAddressing;
    }

    public void setBeanState(LightState state) {
        _lightState = state;
    }

    public LightState getBeanState() {
        return _lightState;
    }

    public void setStateReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _stateReference = reference;
    }

    public String getStateReference() {
        return _stateReference;
    }

    public void setStateLocalVariable(@Nonnull String localVariable) {
        _stateLocalVariable = localVariable;
    }

    public String getStateLocalVariable() {
        return _stateLocalVariable;
    }

    public void setStateFormula(@Nonnull String formula) throws ParserException {
        _stateFormula = formula;
        parseStateFormula();
    }

    public String getStateFormula() {
        return _stateFormula;
    }

    private void parseStateFormula() throws ParserException {
        if (_stateAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _stateExpressionNode = parser.parseExpression(_stateFormula);
        } else {
            _stateExpressionNode = null;
        }
    }


    public void setDataAddressing(NamedBeanAddressing addressing) throws ParserException {
        _dataAddressing = addressing;
        parseDataFormula();
    }

    public NamedBeanAddressing getDataAddressing() {
        return _dataAddressing;
    }

    public void setDataReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _dataReference = reference;
    }

    public String getDataReference() {
        return _dataReference;
    }

    public void setDataLocalVariable(@Nonnull String localVariable) {
        _dataLocalVariable = localVariable;
    }

    public String getDataLocalVariable() {
        return _dataLocalVariable;
    }

    public void setDataFormula(@Nonnull String formula) throws ParserException {
        _dataFormula = formula;
        parseDataFormula();
    }

    public String getDataFormula() {
        return _dataFormula;
    }

    private void parseDataFormula() throws ParserException {
        if (_dataAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _dataExpressionNode = parser.parseExpression(_dataFormula);
        } else {
            _dataExpressionNode = null;
        }
    }


    public void setLightValue(@Nonnull int value) {
        _lightValue = value;
    }

    public int getLightValue() {
        return _lightValue;
    }


    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Light) {
                if (evt.getOldValue().equals(getLight().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Light_LightInUseLightExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Light) {
                if (evt.getOldValue().equals(getLight().getBean())) {
                    removeLight();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }

    private String getNewState() throws JmriException {

        switch (_stateAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _stateReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_stateLocalVariable), false);

            case Formula:
                return _stateExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _stateExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _stateAddressing.name());
        }
    }

    private int getNewData() throws JmriException {
        String newValue = "";

        switch (_dataAddressing) {
            case Direct:
                return _lightValue;

            case Reference:
                newValue = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _dataReference);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                newValue = TypeConversionUtil
                        .convertToString(symbolTable.getValue(_dataLocalVariable), false);
                break;

            case Formula:
                newValue = _dataExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _dataExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : "";
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _dataAddressing.name());
        }
        try {
            int newInt = Integer.parseInt(newValue);
            if (newInt < 0) newInt = 0;
            if (newInt > 100) newInt = 100;
            return newInt;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Light light;

        switch (_addressing) {
            case Direct:
                light = _lightHandle != null ? _lightHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                light = InstanceManager.getDefault(LightManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                light = InstanceManager.getDefault(LightManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                light = _expressionNode != null ?
                        InstanceManager.getDefault(LightManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        if (light == null) {
//            log.warn("light is null");
            return;
        }

        String name = (_stateAddressing != NamedBeanAddressing.Direct)
                ? getNewState() : null;

        LightState state;
        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            state = _lightState;
        } else {
            state = LightState.valueOf(name);
        }

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            if (state == LightState.Toggle) {
                if (light.getCommandedState() == Turnout.CLOSED) {
                    light.setCommandedState(Turnout.THROWN);
                } else {
                    light.setCommandedState(Turnout.CLOSED);
                }

            } else if (state == LightState.Intensity) {
                if (light instanceof VariableLight) {
                    ((VariableLight)light).setTargetIntensity(getNewData() / 100.0);
                } else {
                    light.setCommandedState(getNewData() > 50 ? Light.ON : Light.OFF);
                }
            } else if (state == LightState.Interval) {
                if (light instanceof VariableLight) {
                    ((VariableLight)light).setTransitionTime(getNewData());
                }
            } else {
                light.setCommandedState(state.getID());
            }
        });
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Light_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String state;

        switch (_addressing) {
            case Direct:
                String lightName;
                if (_lightHandle != null) {
                    lightName = _lightHandle.getBean().getDisplayName();
                } else {
                    lightName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", lightName);
                break;

            case Reference:
                namedBean = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        switch (_stateAddressing) {
            case Direct:
                if (_lightState == LightState.Intensity || _lightState == LightState.Interval) {
                    String bundleKey = "Light_Long_Value";
                    switch (_dataAddressing) {
                        case Direct:
                            String type = _lightState == LightState.Intensity ?
                                     Bundle.getMessage("Light_Intensity_Value") :
                                     Bundle.getMessage("Light_Interval_Value");
                            return Bundle.getMessage(locale, bundleKey, namedBean, type, _lightValue);
                        case Reference:
                            return Bundle.getMessage(locale, bundleKey, namedBean, "", Bundle.getMessage("AddressByReference", _dataReference));
                        case LocalVariable:
                            return Bundle.getMessage(locale, bundleKey, namedBean, "", Bundle.getMessage("AddressByLocalVariable", _dataLocalVariable));
                        case Formula:
                            return Bundle.getMessage(locale, bundleKey, namedBean, "", Bundle.getMessage("AddressByFormula", _dataFormula));
                        default:
                            throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
                    }
                } else {
                    state = Bundle.getMessage(locale, "AddressByDirect", _lightState._text);
                }
                break;

            case Reference:
                state = Bundle.getMessage(locale, "AddressByReference", _stateReference);
                break;

            case LocalVariable:
                state = Bundle.getMessage(locale, "AddressByLocalVariable", _stateLocalVariable);
                break;

            case Formula:
                state = Bundle.getMessage(locale, "AddressByFormula", _stateFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _stateAddressing.name());
        }

        return Bundle.getMessage(locale, "Light_Long", namedBean, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
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


    // This constant is only used internally in LightState but must be outside
    // the enum.
    private static final int TOGGLE_ID = -1;
    private static final int INTENSITY_ID = -2;
    private static final int INTERVAL_ID = -3;


    public enum LightState {
        Off(Light.OFF, Bundle.getMessage("StateOff")),
        On(Light.ON, Bundle.getMessage("StateOn")),
        Toggle(TOGGLE_ID, Bundle.getMessage("LightToggleStatus")),
        Intensity(INTENSITY_ID, Bundle.getMessage("LightIntensity")),
        Interval(INTERVAL_ID, Bundle.getMessage("LightInterval"));

        private final int _id;
        private final String _text;

        private LightState(int id, String text) {
            this._id = id;
            this._text = text;
        }

        static public LightState get(int id) {
            switch (id) {
                case Light.OFF:
                    return Off;

                case Light.ON:
                    return On;

                case TOGGLE_ID:
                    return Toggle;

                default:
                    throw new IllegalArgumentException("invalid light state");
            }
        }

        public int getID() {
            return _id;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ActionLight: bean = {}, report = {}", cdl, report);
        if (getLight() != null && bean.equals(getLight().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLight.class);

}
