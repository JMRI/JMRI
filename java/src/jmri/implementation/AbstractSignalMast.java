package jmri.implementation;

import java.util.*;
import java.util.List;
import javax.annotation.*;
import jmri.InstanceManager;
import jmri.SignalAppearanceMap;
import jmri.SignalMast;
import jmri.SignalSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class providing the basic logic of the SignalMast interface.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public abstract class AbstractSignalMast extends AbstractNamedBean
        implements SignalMast, java.beans.VetoableChangeListener {

    private final static Logger log = LoggerFactory.getLogger(AbstractSignalMast.class);

    public AbstractSignalMast(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractSignalMast(String systemName) {
        super(systemName);
    }

    @Override
    public void setAspect(@Nonnull String aspect) {
        String oldAspect = this.aspect;
        this.aspect = aspect;
        this.speed = (String) getSignalSystem().getProperty(aspect, "speed");
        firePropertyChange("Aspect", oldAspect, aspect);
    }

    @Override
    public String getAspect() {
        return aspect;
    }
    protected String aspect = null;

    public String getSpeed() {
        return speed;
    }
    protected String speed = null;

    /**
     * The state is the index of the current aspect in the list of possible
     * aspects.
     */
    @Override
    public int getState() {
        return -1;
    }

    @Override
    public void setState(int i) {
    }

    /**
     * By default, signals are lit.
     */
    private boolean mLit = true;

    /**
     * Default behavior for "lit" property is to track value and return it.
     */
    @Override
    public boolean getLit() {
        return mLit;
    }

    /**
     * By default, signals are not held.
     */
    private boolean mHeld = false;

    /**
     * "Held" property is just tracked and notified.
     */
    @Override
    public boolean getHeld() {
        return mHeld;
    }

    /**
     * Set the lit property.
     * <p>
     * This acts on all the SignalHeads included in this SignalMast
     *
     * @param newLit the new value of lit
     */
    @Override
    public void setLit(boolean newLit) {
        boolean oldLit = mLit;
        mLit = newLit;
        if (oldLit != newLit) {
            //updateOutput();
            // notify listeners, if any
            firePropertyChange("Lit", oldLit, newLit);
        }
    }

    /**
     * Set the held property of the signal mast.
     * <p>
     * Note that this does not directly effect the output on the layout; the
     * held property is a local variable which effects the aspect only via
     * higher-level logic.
     *
     * @param newHeld the new value of the help property
     */
    @Override
    public void setHeld(boolean newHeld) {
        boolean oldHeld = mHeld;
        mHeld = newHeld;
        if (oldHeld != newHeld) {
            // notify listeners, if any
            firePropertyChange("Held", oldHeld, newHeld);
        }
    }

    DefaultSignalAppearanceMap map;
    SignalSystem systemDefn;

    protected void configureSignalSystemDefinition(String name) {
        systemDefn = InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem(name);
        if (systemDefn == null) {
            log.error("Did not find signal definition: {}", name);
            throw new IllegalArgumentException("Signal definition not found: " + name);
        }
    }

    protected void configureAspectTable(String signalSystemName, String aspectMapName) {
        map = DefaultSignalAppearanceMap.getMap(signalSystemName, aspectMapName);
    }

    @Override
    public SignalSystem getSignalSystem() {
        return systemDefn;
    }

    @Override
    public SignalAppearanceMap getAppearanceMap() {
        return map;
    }

    ArrayList<String> disabledAspects = new ArrayList<>(1);

    @Override
    @Nonnull
    public Vector<String> getValidAspects() {
        java.util.Enumeration<String> e = map.getAspects();
        // copy List to Vector
        Vector<String> v = new Vector<>();
        while (e.hasMoreElements()) {
            String a = e.nextElement();
            if (!disabledAspects.contains(a)) {
                v.add(a);
            }
        }
        return v;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getMastType() { return mastType; }
    @Override
    public void setMastType(@Nonnull String type) { 
        Objects.requireNonNull(type, "MastType cannot be null");
        mastType = type;
    }
    String mastType;

    /**
     * Get a list of all the known aspects for this mast, including those that
     * have been disabled.
     *
     * @return list of known aspects; may be empty
     */
    public Vector<String> getAllKnownAspects() {
        java.util.Enumeration<String> e = map.getAspects();
        Vector<String> v = new Vector<>();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }

    public void setAspectDisabled(String aspect) {
        if (aspect == null || aspect.equals("")) {
            return;
        }
        if (!map.checkAspect(aspect)) {
            log.warn("attempting to disable an aspect: {} that is not on the mast {}", aspect, getDisplayName());
            return;
        }
        if (!disabledAspects.contains(aspect)) {
            disabledAspects.add(aspect);
            firePropertyChange("aspectDisabled", null, aspect);
        }
    }

    public void setAspectEnabled(String aspect) {
        if (aspect == null || aspect.equals("")) {
            return;
        }
        if (!map.checkAspect(aspect)) {
            log.warn("attempting to disable an aspect: {} that is not on the mast {}", aspect, getDisplayName());
            return;
        }
        if (disabledAspects.contains(aspect)) {
            disabledAspects.remove(aspect);
            firePropertyChange("aspectEnabled", null, aspect);
        }
    }

    public List<String> getDisabledAspects() {
        return disabledAspects;
    }

    @Override
    public boolean isAspectDisabled(String aspect) {
        return disabledAspects.contains(aspect);
    }

    boolean allowUnLit = true;

    @Override
    public void setAllowUnLit(boolean boo) {
        allowUnLit = boo;
    }

    @Override
    public boolean allowUnLit() {
        return allowUnLit;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSignalMast");
    }

}
