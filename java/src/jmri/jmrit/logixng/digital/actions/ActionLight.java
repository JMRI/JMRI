package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.util.ThreadingUtil;

/**
 * This action sets the state of a light.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionLight extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanHandle<Light> _lightHandle;
    private LightState _lightState = LightState.ON;
    
    public ActionLight(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setLightName(String lightName) {
        Light light = InstanceManager.getDefault(LightManager.class).getLight(lightName);
        if (light != null) {
            _lightHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(lightName, light);
            InstanceManager.lightManagerInstance().addVetoableChangeListener(this);
        } else {
            _lightHandle = null;
            InstanceManager.lightManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public void setLight(NamedBeanHandle<Light> handle) {
        _lightHandle = handle;
        if (_lightHandle != null) {
            InstanceManager.lightManagerInstance().addVetoableChangeListener(this);
        } else {
            InstanceManager.lightManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public void setLight(@CheckForNull Light light) {
        if (light != null) {
            _lightHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(light.getDisplayName(), light);
            InstanceManager.lightManagerInstance().addVetoableChangeListener(this);
        } else {
            _lightHandle = null;
            InstanceManager.lightManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public NamedBeanHandle<Light> getLight() {
        return _lightHandle;
    }
    
    public void setLightState(LightState state) {
        _lightState = state;
    }
    
    public LightState getLightState() {
        return _lightState;
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Light) {
                if (evt.getOldValue().equals(getLight().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Light) {
                if (evt.getOldValue().equals(getLight().getBean())) {
                    setLight((Light)null);
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
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        final Light t = _lightHandle.getBean();
        ThreadingUtil.runOnLayout(() -> {
            if (_lightState == LightState.TOGGLE) {
                if (t.getCommandedState() == Light.OFF) {
                    t.setCommandedState(Light.ON);
                } else {
                    t.setCommandedState(Light.OFF);
                }
            } else {
                t.setCommandedState(_lightState.getID());
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
        String lightName;
        if (_lightHandle != null) {
            lightName = _lightHandle.getBean().getDisplayName();
        } else {
            lightName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "Light_Long", lightName, _lightState._text);
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
    
    
    public enum LightState {
        OFF(Light.OFF, Bundle.getMessage("StateOff")),
        ON(Light.ON, Bundle.getMessage("StateOn")),
        TOGGLE(TOGGLE_ID, Bundle.getMessage("LightToggleStatus"));
        
        private final int _id;
        private final String _text;
        
        private LightState(int id, String text) {
            this._id = id;
            this._text = text;
        }
        
        static public LightState get(int id) {
            switch (id) {
                case Light.OFF:
                    return OFF;
                    
                case Light.ON:
                    return ON;
                    
                case TOGGLE_ID:
                    return TOGGLE;
                    
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
    
}
