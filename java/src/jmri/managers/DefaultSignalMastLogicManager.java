package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.implementation.DefaultSignalMastLogic;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a SignalMastLogicManager.
 * @see jmri.SignalMastLogicManager
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public class DefaultSignalMastLogicManager implements jmri.SignalMastLogicManager, java.beans.VetoableChangeListener {

    public DefaultSignalMastLogicManager() {
        registerSelf();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).addPropertyChangeListener(propertyBlockManagerListener);
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).addVetoableChangeListener(this);
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        //_speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
    }

    @Override
    public int getXMLOrder() {
        return Manager.SIGNALMASTLOGICS;
    }

    private static SignalSpeedMap _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);

    public final static jmri.implementation.SignalSpeedMap getSpeedMap() {
        return _speedMap;
    }

    @Override
    public SignalMastLogic getSignalMastLogic(SignalMast source) {
        for (int i = 0; i < signalMastLogic.size(); i++) {
            if (signalMastLogic.get(i).getSourceMast() == source) {
                return signalMastLogic.get(i);
            }
        }
        return null;
    }

    @Override
    public SignalMastLogic newSignalMastLogic(SignalMast source) {
        for (int i = 0; i < signalMastLogic.size(); i++) {
            if (signalMastLogic.get(i).getSourceMast() == source) {
                return signalMastLogic.get(i);
            }
        }
        SignalMastLogic logic = new DefaultSignalMastLogic(source);
        signalMastLogic.add(logic);
        firePropertyChange("length", null, Integer.valueOf(signalMastLogic.size()));
        return logic;
    }

    List<SignalMastLogic> signalMastLogic = new ArrayList<SignalMastLogic>();
    //Hashtable<SignalMast, List<SignalMastLogic>> destLocationList = new Hashtable<SignalMast, List<SignalMastLogic>>();

    @Override
    public void replaceSignalMast(SignalMast oldMast, SignalMast newMast) {
        if (oldMast == null || newMast == null) {
            return;
        }
        for (SignalMastLogic source : signalMastLogic) {
            if (source.getSourceMast() == oldMast) {
                source.replaceSourceMast(oldMast, newMast);
            } else {
                source.replaceDestinationMast(oldMast, newMast);
            }
        }
    }

    @Override
    public void swapSignalMasts(SignalMast mastA, SignalMast mastB) {
        if (mastA == null || mastB == null) {
            return;
        }
        List<SignalMastLogic> mastALogicList = getLogicsByDestination(mastA);
        SignalMastLogic mastALogicSource = getSignalMastLogic(mastA);

        List<SignalMastLogic> mastBLogicList = getLogicsByDestination(mastB);
        SignalMastLogic mastBLogicSource = getSignalMastLogic(mastB);

        if (mastALogicSource != null) {
            mastALogicSource.replaceSourceMast(mastA, mastB);
        }
        if (mastBLogicSource != null) {
            mastBLogicSource.replaceSourceMast(mastB, mastA);
        }

        for (SignalMastLogic mastALogic : mastALogicList) {
            mastALogic.replaceDestinationMast(mastA, mastB);
        }
        for (SignalMastLogic mastBLogic : mastBLogicList) {
            mastBLogic.replaceDestinationMast(mastB, mastA);
        }

    }

    @Override
    public List<SignalMastLogic> getLogicsByDestination(SignalMast destination) {
        List<SignalMastLogic> list = new ArrayList<>();
        for (SignalMastLogic source : signalMastLogic) {
            if (source.isDestinationValid(destination)) {
                list.add(source);
            }
        }
        return list;
    }

    @Override
    public List<SignalMastLogic> getSignalMastLogicList() {
        return signalMastLogic;
    }

    @Override
    public boolean isSignalMastUsed(SignalMast mast) {
        if (getSignalMastLogic(mast) != null) {
            /* Although we might have it registered as a source, it may not have
             any valid destination, so therefore it can be returned as not in use. */
            if (getSignalMastLogic(mast).getDestinationList().size() != 0) {
                return true;
            }
        }
        if (getLogicsByDestination(mast).size() != 0) {
            return true;
        }
        return false;
    }

    @Override
    public void removeSignalMastLogic(SignalMastLogic sml, SignalMast dest) {
        if (sml.removeDestination(dest)) {
            removeSignalMastLogic(sml);
        }
    }

    @Override
    public void removeSignalMastLogic(SignalMastLogic sml) {
        if (sml == null) {
            return;
        }
        //Need to provide a method to delete and dispose.
        sml.dispose();

        signalMastLogic.remove(sml);
        firePropertyChange("length", null, Integer.valueOf(signalMastLogic.size()));
    }

    @Override
    public void removeSignalMast(SignalMast mast) {
        if (mast == null) {
            return;
        }
        for (SignalMastLogic source : signalMastLogic) {
            if (source.isDestinationValid(mast)) {
                source.removeDestination(mast);
            }
        }
        removeSignalMastLogic(getSignalMastLogic(mast));
    }

    /**
     * Disable the use of info from the Layout Editor Panels to configure
     * a Signal Mast Logic for a specific Signal Mast.
     *
     * @param mast The Signal Mast for which LE info is to be disabled
     */
    @Override
    public void disableLayoutEditorUse(SignalMast mast) {
        SignalMastLogic source = getSignalMastLogic(mast);
        if (source != null) {
            source.disableLayoutEditorUse();
        }
        for (SignalMastLogic sml : getLogicsByDestination(mast)) {
            try {
                sml.useLayoutEditor(false, mast);
            } catch (jmri.JmriException e) {
                log.error("Error occurred while trying to disable layout editor use " + e);
            }
        }
    }

    /**
     * By default, register this manager to store as configuration information.
     * Override to change that.
     */
    protected void registerSelf() {
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerConfig(this, jmri.Manager.SIGNALMASTLOGICS);
        }
    }

    // Abstract methods to be extended by subclasses:

    @Override
    public void dispose() {
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.deregister(this);
        }
        signalMastLogic.clear();
    }

    /**
     * Initialise all the Signal Mast Logics. Primarily used after
     * loading a configuration.
     */
    @Override
    public void initialise() {
        for (int i = 0; i < signalMastLogic.size(); i++) {
            signalMastLogic.get(i).initialise();
        }
    }

    @Override
    public SignalMastLogic getBeanBySystemName(String systemName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SignalMastLogic getBeanByUserName(String userName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SignalMastLogic getNamedBean(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSystemPrefix() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public char typeLetter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String makeSystemName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     *
     * @return always 'VALID'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return NameValidity.VALID;
    }


    /**
     * Enforces, and as a user convenience converts to, the standard form for a system name
     * for the NamedBeans handled by this manager.
     *
     * @param inputName System name to be normalized
     * @throws NamedBean.BadSystemNameException If the inputName can't be converted to normalized form
     * @return A system name in standard normalized form 
     */
    @Override
    @CheckReturnValue
    public @Nonnull String normalizeSystemName(@Nonnull String inputName) throws NamedBean.BadSystemNameException {
        return inputName;
    }

    @Override
    public String[] getSystemNameArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getSystemNameList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<SignalMastLogic> getNamedBeanList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    java.beans.VetoableChangeSupport vcs = new java.beans.VetoableChangeSupport(this);

    @Override
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vcs.addVetoableChangeListener(l);
    }

    @Override
    public synchronized void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vcs.removeVetoableChangeListener(l);
    }

    @Override
    public void deleteBean(SignalMastLogic bean, String property) throws java.beans.PropertyVetoException {

    }

    @Override
    public void register(SignalMastLogic n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deregister(SignalMastLogic n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    int signalLogicDelay = 500;

    @Override
    public int getSignalLogicDelay() {
        return signalLogicDelay;
    }

    @Override
    public void setSignalLogicDelay(int l) {
        signalLogicDelay = l;
    }

    protected PropertyChangeListener propertyBlockManagerListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("topology")) {
                //boolean newValue = new Boolean.parseBoolean(String.valueOf(e.getNewValue()));
                boolean newValue = (Boolean) e.getNewValue();
                if (newValue) {
                    for (int i = 0; i < signalMastLogic.size(); i++) {
                        signalMastLogic.get(i).setupLayoutEditorDetails();
                    }
                    if (runWhenStablised) {
                        try {
                            automaticallyDiscoverSignallingPairs();
                        } catch (JmriException je) {
                            //Considered normal if routing not enabled
                        }
                    }
                }
            }
        }
    };

    boolean runWhenStablised = false;

    /**
     * Discover valid destination Signal Masts for a given source Signal Mast on a
     * given Layout Editor Panel.
     *
     * @param source Source SignalMast
     * @param layout Layout Editor panel to check.
     */
    @Override
    public void discoverSignallingDest(SignalMast source, LayoutEditor layout) throws JmriException {
        firePropertyChange("autoSignalMastGenerateStart", null, source.getDisplayName());

        Hashtable<NamedBean, List<NamedBean>> validPaths = new Hashtable<NamedBean, List<NamedBean>>();
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        if (!lbm.isAdvancedRoutingEnabled()) {
            //log.debug("advanced routing not enabled");
            throw new JmriException("advanced routing not enabled");
        }
        if (!lbm.routingStablised()) {
            throw new JmriException("routing not stabilised");
        }
        try {
            validPaths.put(source, lbm.getLayoutBlockConnectivityTools().discoverPairDest(source, layout, SignalMast.class, LayoutBlockConnectivityTools.MASTTOMAST));
        } catch (JmriException e) {
            throw e;
        }

        Enumeration<NamedBean> en = validPaths.keys();
        while (en.hasMoreElements()) {
            SignalMast key = (SignalMast) en.nextElement();
            SignalMastLogic sml = getSignalMastLogic(key);
            if (sml == null) {
                sml = newSignalMastLogic(key);
            }
            List<NamedBean> validDestMast = validPaths.get(key);
            for (int i = 0; i < validDestMast.size(); i++) {
                if (!sml.isDestinationValid((SignalMast) validDestMast.get(i))) {
                    try {
                        sml.setDestinationMast((SignalMast) validDestMast.get(i));
                        sml.useLayoutEditorDetails(true, true, (SignalMast) validDestMast.get(i));
                        sml.useLayoutEditor(true, (SignalMast) validDestMast.get(i));
                    } catch (JmriException e) {
                        //log.debug("We shouldn't get an exception here");
                        log.error("Exception found when adding pair " + source.getDisplayName() + " to destination " + validDestMast.get(i).getDisplayName() + "\n" + e.toString());
                        //throw e;
                    }
                }
            }
            if (sml.getDestinationList().size() == 1 && sml.getAutoTurnouts(sml.getDestinationList().get(0)).size() == 0) {
                key.setProperty("intermediateSignal", true);
            } else {
                key.removeProperty("intermediateSignal");
            }
        }
        initialise();
        firePropertyChange("autoSignalMastGenerateComplete", null, source.getDisplayName());
    }

    /**
     * Discover all possible valid source + destination signal mast pairs
     * on all Layout Editor Panels.
     */
    @Override
    public void automaticallyDiscoverSignallingPairs() throws JmriException {
        runWhenStablised = false;
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        if (!lbm.isAdvancedRoutingEnabled()) {
            throw new JmriException("advanced routing not enabled");
        }
        if (!lbm.routingStablised()) {
            runWhenStablised = true;
            return;
        }
        Hashtable<NamedBean, List<NamedBean>> validPaths = lbm.getLayoutBlockConnectivityTools().discoverValidBeanPairs(null, SignalMast.class, LayoutBlockConnectivityTools.MASTTOMAST);
        Enumeration<NamedBean> en = validPaths.keys();
        firePropertyChange("autoGenerateUpdate", null, ("Found " + validPaths.size() + " masts as sources for logic"));
        for (NamedBean nb : InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBeanList()) {
            nb.removeProperty("intermediateSignal");
        }
        while (en.hasMoreElements()) {
            SignalMast key = (SignalMast) en.nextElement();
            SignalMastLogic sml = getSignalMastLogic(key);
            if (sml == null) {
                sml = newSignalMastLogic(key);
            }
            List<NamedBean> validDestMast = validPaths.get(key);
            for (int i = 0; i < validDestMast.size(); i++) {
                if (!sml.isDestinationValid((SignalMast) validDestMast.get(i))) {
                    try {
                        sml.setDestinationMast((SignalMast) validDestMast.get(i));
                        sml.useLayoutEditorDetails(true, true, (SignalMast) validDestMast.get(i));
                        sml.useLayoutEditor(true, (SignalMast) validDestMast.get(i));
                    } catch (jmri.JmriException ex) {
                        //log.debug("we shouldn't get an exception here!");
                        log.debug(ex.getLocalizedMessage(), ex);
                    }
                }
            }
            if (sml.getDestinationList().size() == 1 && sml.getAutoTurnouts(sml.getDestinationList().get(0)).size() == 0) {
                key.setProperty("intermediateSignal", true);
            }
        }
        initialise();
        firePropertyChange("autoGenerateComplete", null, null);
    }

    /**
     * Populate Sections of type SIGNALMASTLOGIC used with Layout Editor with Signal Mast attributes
     * as stored in Signal Mast Logic.
     */
    public void generateSection() {
        SectionManager sm = InstanceManager.getDefault(jmri.SectionManager.class);
        for (Section nb : sm.getNamedBeanList()) {
            if (nb.getSectionType() == Section.SIGNALMASTLOGIC) {
                nb.removeProperty("intermediateSection");
            }
            nb.removeProperty("forwardMast");
        }
        for (SignalMastLogic sml : getSignalMastLogicList()) {
            jmri.jmrit.display.layoutEditor.LayoutBlock faceLBlock = sml.getFacingBlock();
            if (faceLBlock != null) {
                boolean sourceIntermediate = false;
                if (sml.getSourceMast().getProperty("intermediateSignal") != null) {
                    sourceIntermediate = ((Boolean) sml.getSourceMast().getProperty("intermediateSignal")).booleanValue();
                }
                for (SignalMast destMast : sml.getDestinationList()) {
                    if (sml.getAutoBlocksBetweenMasts(destMast).size() != 0) {
                        Section sec = sm.createNewSection(sml.getSourceMast().getDisplayName() + ":" + destMast.getDisplayName());
                        if (sec == null) {
                            //A Section already exists, lets grab it and check that it is one used with the SML, if so carry on using that.
                            sec = sm.getSection(sml.getSourceMast().getDisplayName() + ":" + destMast.getDisplayName());
                            if (sec.getSectionType() != Section.SIGNALMASTLOGIC) {
                                break;
                            }
                        } else {
                            sec.setSectionType(Section.SIGNALMASTLOGIC);
                            try {
                                //Auto running requires forward/reverse sensors, but at this stage SML does not support that, so just create dummy internal ones for now.
                                Sensor sen = InstanceManager.sensorManagerInstance().provideSensor("IS:" + sec.getSystemName() + ":forward");
                                sen.setUserName(sec.getSystemName() + ":forward");

                                sen = InstanceManager.sensorManagerInstance().provideSensor("IS:" + sec.getSystemName() + ":reverse");
                                sen.setUserName(sec.getSystemName() + ":reverse");
                                sec.setForwardBlockingSensorName(sec.getSystemName() + ":forward");
                                sec.setReverseBlockingSensorName(sec.getSystemName() + ":reverse");
                            } catch (IllegalArgumentException ex) {
                                log.warn("Failed to provide Sensor in generateSection");
                            }
                        }
                        sml.setAssociatedSection(sec, destMast);
                        sec.setProperty("forwardMast", destMast.getDisplayName());
                        boolean destIntermediate = false;
                        if (destMast.getProperty("intermediateSignal") != null) {
                            destIntermediate = ((Boolean) destMast.getProperty("intermediateSignal")).booleanValue();
                        }
                        if (sourceIntermediate || destIntermediate) {
                            sec.setProperty("intermediateSection", true);
                        } else {
                            sec.setProperty("intermediateSection", false);
                        }
                        //Not 100% sure about this for now so will comment out
                        //sml.addSensor(sec.getSystemName()+":forward", Sensor.INACTIVE, destMast);
                    }
                }
            } else {
                log.info("No facing block found " + sml.getSourceMast().getDisplayName());
            }
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { //NOI18N
            StringBuilder message = new StringBuilder();
            boolean found = false;
            message.append(Bundle.getMessage("VetoFoundInSignalMastLogic"));
            message.append("<ul>");
            for (int i = 0; i < signalMastLogic.size(); i++) {
                try {
                    signalMastLogic.get(i).vetoableChange(evt);
                } catch (java.beans.PropertyVetoException e) {
                    if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //NOI18N
                        throw e;
                    }
                    found = true;

                    message.append(e.getMessage());
                    message.append("<br>");

                }
            }
            message.append("</ul>");
            if (found) {
                throw new java.beans.PropertyVetoException(message.toString(), evt);
            }
        } else {
            for (SignalMastLogic sml : signalMastLogic) {
                try {
                    sml.vetoableChange(evt);
                } catch (java.beans.PropertyVetoException e) {
                    throw e;
                }
            }
        }
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameSignalMastLogic");
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogicManager.class);
}
