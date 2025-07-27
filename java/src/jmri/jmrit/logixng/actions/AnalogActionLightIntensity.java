package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;

/**
 * Sets the light intensity.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogActionLightIntensity extends AbstractAnalogAction
        implements PropertyChangeListener {

    public static final int INTENSITY_SOCKET = 0;

    private final LogixNG_SelectNamedBean<VariableLight> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, VariableLight.class, InstanceManager.getDefault(VariableLightManager.class), this);


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
        _selectNamedBean.copy(copy._selectNamedBean);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<VariableLight> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(double value) throws JmriException {
        VariableLight light = _selectNamedBean.evaluateNamedBean(getConditionalNG());

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
        String namedBean = _selectNamedBean.getDescription(locale);

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
        _selectNamedBean.registerListeners();
        _listenersAreRegistered = true;
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
        _listenersAreRegistered = false;
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogActionLightIntensity.class);

}
