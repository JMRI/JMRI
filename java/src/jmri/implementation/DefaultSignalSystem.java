package jmri.implementation;

import java.util.*;

import jmri.SignalSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a basic signal system definition.
 * <p>
 * The default contents are taken from the NamedBeanBundle properties file. This
 * makes creation a little more heavy-weight, but speeds operation.
 *
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class DefaultSignalSystem extends AbstractNamedBean implements SignalSystem {

    public DefaultSignalSystem(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalSystem(String systemName) {
        super(systemName);
    }

    @Override
    public void setProperty(String aspect, String key, Object value) {
        getTable(aspect).put(key, value);
        if (!keys.contains(key)) {
            keys.add(key);
        }
    }

    @Override
    public Object getProperty(String aspect, String key) {
        if (aspect == null) {
            return null;
        }
        return getTable(aspect).get(key);
    }

    @Override
    public void setImageType(String type) {
        if (!imageTypes.contains(type)) {
            imageTypes.add(type);
        }
    }

    @Override
    public Enumeration<String> getImageTypeList() {
        return imageTypes.elements();
    }

    @Override
    public String getAspect(Object obj, String key) {
        if (obj == null) {
            return null;
        }
        Set<String> aspectKeys = aspects.keySet();
        for (String aspect : aspectKeys) {
            if (getTable(aspect).containsKey(key)) {
                if (getTable(aspect).get(key).equals(obj)) {
                    return aspect;
                }
            }
        }
        return null;
    }

    protected Hashtable<String, Object> getTable(String aspect) {
        Hashtable<String, Object> t = aspects.get(aspect);
        if (t == null) {
            t = new Hashtable<>();
            aspects.put(aspect, t);
        }
        return t;
    }

    @Override
    public Enumeration<String> getAspects() {
        return new Vector<String>(aspects.keySet()).elements();  // this will be greatly simplified when we can just return keySet
    }

    @Override
    public Enumeration<String> getKeys() {
        return keys.elements();
    }

    @Override
    public boolean checkAspect(String aspect) {
        return aspects.get(aspect) != null;
    }

    public void loadDefaults() {

        log.debug("start loadDefaults");

        String aspect;
        String key = Bundle.getMessage("SignalAspectKey");
        String value;

        aspect = Bundle.getMessage("SignalAspectDefaultRed");
        value = Bundle.getMessage("SignalAspect_" + key + "_" + aspect);
        setProperty(aspect, key, value);

        aspect = Bundle.getMessage("SignalAspectDefaultYellow");
        value = Bundle.getMessage("SignalAspect_" + key + "_" + aspect);
        setProperty(aspect, key, value);

        aspect = Bundle.getMessage("SignalAspectDefaultGreen");
        value = Bundle.getMessage("SignalAspect_" + key + "_" + aspect);
        setProperty(aspect, key, value);

    }

    /**
     * {@inheritDoc}
     *
     * This method returns a constant result on the DefaultSignalSystem.
     *
     * @return {@link jmri.NamedBean#INCONSISTENT}
     */
    @Override
    public int getState() {
        return INCONSISTENT;
    }

    /**
     * {@inheritDoc}
     *
     * This method has no effect on the DefaultSignalSystem.
     */
    @Override
    public void setState(int s) {
        // do nothing
    }

    float maximumLineSpeed = 0.0f;

    @Override
    public float getMaximumLineSpeed() {
        if (maximumLineSpeed == 0.0f) {
            for (String as : aspects.keySet()) {
                String speed = (String) getProperty(as, "speed");
                if (speed != null) {
                    float aspectSpeed = 0.0f;
                    try {
                        aspectSpeed = Float.valueOf(speed);
                    } catch (NumberFormatException nx) {
                        try {
                            aspectSpeed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(speed);
                        } catch (IllegalArgumentException ex) {
                            //Considered Normal if the speed does not appear in the map
                            log.debug("Speed {} not found in map", speed);
                        }
                    }
                    if (aspectSpeed > maximumLineSpeed) {
                        maximumLineSpeed = aspectSpeed;
                    }
                }

            }
        }
        if (maximumLineSpeed == 0.0f) {
            //no speeds configured so will use the default.
            maximumLineSpeed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed("Maximum");
        }
        return maximumLineSpeed;
    }

    protected java.util.HashMap<String, Hashtable<String, Object>> aspects
            = new java.util.LinkedHashMap<>();

    protected java.util.Vector<String> keys = new java.util.Vector<>();

    protected java.util.Vector<String> imageTypes = new java.util.Vector<>();

    // note that this doesn't properly implement the 
    // contract in {@link NamedBean.toString()}, 
    // which means things like tables and persistance 
    // might not behave properly.
    @Override
    public String toString() {
        StringBuilder retval = new StringBuilder();
        retval.append("SignalSystem ").append(getSystemName()).append("\n");
        Enumeration<String> e1 = getAspects();
        while (e1.hasMoreElements()) {
            String s1 = e1.nextElement();
            retval.append("  ").append(s1).append("\n");
            Enumeration<String> e2 = getKeys();
            while (e2.hasMoreElements()) {
                String s2 = e2.nextElement();
                retval.append("    ").append(s2).append(": ").append(getProperty(s1, s2)).append("\n");
            }
        }
        return retval.toString();
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSignalSystem");
    }
    
    @Override
    /**
     * {@inheritDoc}
     */
    public String summary() {
        StringBuilder retval = new StringBuilder();
        retval.append(toString());
        retval.append("\n  BeanType: "+getBeanType());
        
        retval.append("\n  keys:");
        for (String key : keys) retval.append("\n    "+key);
        
        retval.append("\n  aspects:");
        Set<String> values = aspects.keySet();
        for (String value : values) 
            retval.append("\n    "+value);
        
        retval.append("\n  maximumLineSpeed = "+getMaximumLineSpeed());
        
        return new String(retval);
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalSystem.class);
}

