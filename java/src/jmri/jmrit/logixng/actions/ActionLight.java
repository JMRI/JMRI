package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.jmrit.logixng.util.parser.Variable;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action sets the state of a light.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionLight extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Light> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Light.class, InstanceManager.getDefault(LightManager.class), this);

    private final LogixNG_SelectEnum<LightState> _selectEnum =
            new LogixNG_SelectEnum<>(this, LightState.values(), LightState.On, this);

    private NamedBeanAddressing _dataAddressing = NamedBeanAddressing.Direct;
    private String _dataReference = "";
    private String _dataLocalVariable = "";
    private String _dataFormula = "";
    private ExpressionNode _dataExpressionNode;

    private int _lightValue = 0;


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
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);

        copy.setDataAddressing(_dataAddressing);
        copy.setDataReference(_dataReference);
        copy.setDataLocalVariable(_dataLocalVariable);
        copy.setDataFormula(_dataFormula);

        copy.setLightValue(_lightValue);

        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Light> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<LightState> getSelectEnum() {
        return _selectEnum;
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


    public void setLightValue(int value) {
        _lightValue = value;
    }

    public int getLightValue() {
        return _lightValue;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private int getNewData(SymbolTable symbolTable) throws JmriException {
        String newValue = "";

        switch (_dataAddressing) {
            case Direct:
                return _lightValue;

            case Reference:
                newValue = ReferenceUtil.getReference(symbolTable, _dataReference);
                break;

            case LocalVariable:
                newValue = TypeConversionUtil
                        .convertToString(symbolTable.getValue(_dataLocalVariable), false);
                break;

            case Formula:
                newValue = _dataExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _dataExpressionNode.calculate(symbolTable), false)
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
        Light light = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (light == null) return;

        LightState state = _selectEnum.evaluateEnum(getConditionalNG());

        SymbolTable symbolTable = getConditionalNG().getSymbolTable();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            if (state == LightState.Toggle) {
                if (light.getKnownState() == Turnout.CLOSED) {
                    light.setCommandedState(Turnout.THROWN);
                } else {
                    light.setCommandedState(Turnout.CLOSED);
                }

            } else if (state == LightState.Intensity) {
                if (light instanceof VariableLight) {
                    ((VariableLight)light).setTargetIntensity(getNewData(symbolTable) / 100.0);
                } else {
                    light.setCommandedState(getNewData(symbolTable) > 50 ? Light.ON : Light.OFF);
                }
            } else if (state == LightState.Interval) {
                if (light instanceof VariableLight) {
                    ((VariableLight)light).setTransitionTime(getNewData(symbolTable));
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
        String namedBean = _selectNamedBean.getDescription(locale);
        String state = _selectEnum.getDescription(locale);

        if (_selectEnum.getAddressing() == NamedBeanAddressing.Direct) {
            if (_selectEnum.getEnum() == LightState.Intensity || _selectEnum.getEnum() == LightState.Interval) {
                String bundleKey = "Light_Long_Value";
                switch (_dataAddressing) {
                    case Direct:
                        String type = _selectEnum.getEnum() == LightState.Intensity ?
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
            }
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
        _selectNamedBean.registerListeners();
        _selectEnum.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
        _selectEnum.unregisterListeners();
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
        Interval(INTERVAL_ID, Bundle.getMessage("LightInterval")),
        Unknown(Light.UNKNOWN, Bundle.getMessage("BeanStateUnknown")),
        Inconsistent(Light.INCONSISTENT, Bundle.getMessage("BeanStateInconsistent"));

        private final int _id;
        private final String _text;

        private LightState(int id, String text) {
            this._id = id;
            this._text = text;
        }

        static public LightState get(int id) {
            switch (id) {
                case Light.UNKNOWN:
                    return Unknown;

                case Light.INCONSISTENT:
                    return Inconsistent;

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
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLight.class);

}
