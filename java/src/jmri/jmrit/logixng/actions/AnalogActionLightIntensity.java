package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TypeConversionUtil;

/**
 * Sets the light intensity.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogActionLightIntensity extends AbstractAnalogAction
        implements VetoableChangeListener {

    public static final int INTENSITY_SOCKET = 0;
    
    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<VariableLight> _lightHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    
    
    public AnalogActionLightIntensity(String sys, String user) {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        AnalogActionManager manager = InstanceManager.getDefault(AnalogActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        AnalogActionLightIntensity copy = new AnalogActionLightIntensity(sysName, userName);
        copy.setComment(getComment());
        if (_lightHandle != null) copy.setLight(_lightHandle);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    public void setLight(@Nonnull String lightName) {
        assertListenersAreNotRegistered(log, "setLight");
        VariableLight light = InstanceManager.getDefault(VariableLightManager.class).getNamedBean(lightName);
        if (light != null) {
            setLight(light);
        } else {
            removeLight();
            log.warn("light \"{}\" is not found", lightName);
        }
    }

    public void setLight(@Nonnull NamedBeanHandle<VariableLight> handle) {
        assertListenersAreNotRegistered(log, "setLight");
        _lightHandle = handle;
        InstanceManager.lightManagerInstance().addVetoableChangeListener(this);
    }

    public void setLight(@Nonnull VariableLight light) {
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

    public NamedBeanHandle<VariableLight> getLight() {
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
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof VariableLight) {
                if (evt.getOldValue().equals(getLight().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("AnalogActionLightIntensity_LightInUseLightActionVeto", getDisplayName()), e); // NOI18N
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(double value) throws JmriException {
        VariableLight light;
        
        switch (_addressing) {
            case Direct:
                light = _lightHandle != null ? _lightHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                light = InstanceManager.getDefault(VariableLightManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                light = InstanceManager.getDefault(VariableLightManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                light = _expressionNode != null ?
                        InstanceManager.getDefault(VariableLightManager.class)
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
        
        double intensity = value;
        
        if (intensity < 0.0) intensity = 0.0;
        if (intensity > 100.0) intensity = 100.0;
        
        light.setTargetIntensity(intensity/100.0);
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
        return Bundle.getMessage(locale, "AnalogActionLightIntensity_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;

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

        return Bundle.getMessage(locale, "AnalogActionLightIntensity_Long", namedBean);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _listenersAreRegistered = true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogActionLightIntensity.class);

}
