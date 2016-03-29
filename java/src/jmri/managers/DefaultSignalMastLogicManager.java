package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.implementation.SignalSpeedMap;
import jmri.implementation.DefaultSignalMastLogic;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public class DefaultSignalMastLogicManager implements jmri.SignalMastLogicManager, java.beans.VetoableChangeListener {

    public DefaultSignalMastLogicManager() {
        registerSelf();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).addPropertyChangeListener(propertyBlockManagerListener);
        jmri.InstanceManager.signalMastManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        //_speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
    }

    public int getXMLOrder() {
        return Manager.SIGNALMASTLOGICS;
    }

    private static SignalSpeedMap _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);

    public final static jmri.implementation.SignalSpeedMap getSpeedMap() {
        return _speedMap;
    }

    public SignalMastLogic getSignalMastLogic(SignalMast source) {
        for (int i = 0; i < signalMastLogic.size(); i++) {
            if (signalMastLogic.get(i).getSourceMast() == source) {
                return signalMastLogic.get(i);
            }
        }
        return null;
    }

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

    ArrayList<SignalMastLogic> signalMastLogic = new ArrayList<SignalMastLogic>();

    //Hashtable<SignalMast, ArrayList<SignalMastLogic>> destLocationList = new Hashtable<SignalMast, ArrayList<SignalMastLogic>>();
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

    public void swapSignalMasts(SignalMast mastA, SignalMast mastB) {
        if (mastA == null || mastB == null) {
            return;
        }
        ArrayList<SignalMastLogic> mastALogicList = getLogicsByDestination(mastA);
        SignalMastLogic mastALogicSource = getSignalMastLogic(mastA);

        ArrayList<SignalMastLogic> mastBLogicList = getLogicsByDestination(mastB);
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

    /**
     * Gather a list of all the signal mast logics, by destination signal mast
     */
    public ArrayList<SignalMastLogic> getLogicsByDestination(SignalMast destination) {
        ArrayList<SignalMastLogic> list = new ArrayList<SignalMastLogic>();
        for (SignalMastLogic source : signalMastLogic) {
            if (source.isDestinationValid(destination)) {
                list.add(source);
            }
        }
        return list;
    }

    /**
     * Returns an arraylist of signalmastlogic
     *
     * @return An ArrayList of SignalMast logics
     */
    public ArrayList<SignalMastLogic> getSignalMastLogicList() {
        return signalMastLogic;
    }

    public boolean isSignalMastUsed(SignalMast mast) {
        if (getSignalMastLogic(mast) != null) {
            /*Although the we might have it registered as a source, it may not have
             any valid destination, so therefore it can be returned as not in use */
            if (getSignalMastLogic(mast).getDestinationList().size() != 0) {
                return true;
            }
        }
        if (getLogicsByDestination(mast).size() != 0) {
            return true;
        }
        return false;
    }

    /**
     * Remove a destination mast from the signalmast logic
     *
     * @param sml  The signalmast logic of the source signal
     * @param dest The destination mast
     */
    public void removeSignalMastLogic(SignalMastLogic sml, SignalMast dest) {
        if (sml.removeDestination(dest)) {
            removeSignalMastLogic(sml);
        }
    }

    /**
     * Completely remove the signalmast logic.
     */
    public void removeSignalMastLogic(SignalMastLogic sml) {
        if (sml == null) {
            return;
        }
        //Need to provide a method to delete and dispose.
        sml.dispose();

        signalMastLogic.remove(sml);
        firePropertyChange("length", null, Integer.valueOf(signalMastLogic.size()));
    }

    /*
     * Procedure for completely remove a signalmast out of all the logics
     */
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

    public void disableLayoutEditorUse(SignalMast mast) {
        SignalMastLogic source = getSignalMastLogic(mast);
        if (source != null) {
            source.disableLayoutEditorUse();
        }
        for (SignalMastLogic sml : getLogicsByDestination(mast)) {
            try {
                sml.useLayoutEditor(false, mast);
            } catch (jmri.JmriException e) {
                log.error("Error occured while trying to disable layout editor use " + e);
            }
        }
    }

    /**
     * By default, register this manager to store as configuration information.
     * Override to change that.
     *
     */
    protected void registerSelf() {
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(this, jmri.Manager.SIGNALMASTLOGICS);
        }
    }

    // abstract methods to be extended by subclasses
    // to free resources when no longer used
    public void dispose() {
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().deregister(this);
        }
        signalMastLogic.clear();
    }

    /**
     * Used to initialise all the signalmast logics. primarily used after
     * loading.
     */
    public void initialise() {
        for (int i = 0; i < signalMastLogic.size(); i++) {
            signalMastLogic.get(i).initialise();
        }
    }

    public NamedBean getBeanBySystemName(String systemName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedBean getBeanByUserName(String userName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedBean getNamedBean(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public char systemLetter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSystemPrefix() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public char typeLetter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String makeSystemName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getSystemNameArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<String> getSystemNameList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<NamedBean> getNamedBeanList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    java.beans.VetoableChangeSupport vcs = new java.beans.VetoableChangeSupport(this);

    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vcs.addVetoableChangeListener(l);
    }

    public synchronized void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vcs.removeVetoableChangeListener(l);
    }

    public void deleteBean(NamedBean bean, String property) throws java.beans.PropertyVetoException {

    }

    public void register(NamedBean n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deregister(NamedBean n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    long signalLogicDelay = 500L;

    public long getSignalLogicDelay() {
        return signalLogicDelay;
    }

    public void setSignalLogicDelay(long l) {
        signalLogicDelay = l;
    }

    protected PropertyChangeListener propertyBlockManagerListener = new PropertyChangeListener() {
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
     * Discover valid destination signalmasts for a given source signal on a
     * given layout editor panel.
     *
     * @param source Source SignalMast
     * @param layout Layout Editor panel to check.
     */
    public void discoverSignallingDest(SignalMast source, LayoutEditor layout) throws JmriException {
        firePropertyChange("autoSignalMastGenerateStart", null, source.getDisplayName());

        Hashtable<NamedBean, List<NamedBean>> validPaths = new Hashtable<NamedBean, List<NamedBean>>();
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        if (!lbm.isAdvancedRoutingEnabled()) {
            //log.debug("advanced routing not enabled");
            throw new JmriException("advanced routing not enabled");
        }
        if (!lbm.routingStablised()) {
            throw new JmriException("Routing not stabilised");
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
     * Discover all possible valid source and destination signalmasts past pairs
     * on all layout editor panels.
     */
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
        Hashtable<NamedBean, ArrayList<NamedBean>> validPaths = lbm.getLayoutBlockConnectivityTools().discoverValidBeanPairs(null, SignalMast.class, LayoutBlockConnectivityTools.MASTTOMAST);
        Enumeration<NamedBean> en = validPaths.keys();
        firePropertyChange("autoGenerateUpdate", null, ("Found " + validPaths.size() + " masts as sources for logic"));
        for (NamedBean nb : InstanceManager.signalMastManagerInstance().getNamedBeanList()) {
            nb.removeProperty("intermediateSignal");
        }
        while (en.hasMoreElements()) {
            SignalMast key = (SignalMast) en.nextElement();
            SignalMastLogic sml = getSignalMastLogic(key);
            if (sml == null) {
                sml = newSignalMastLogic(key);
            }
            ArrayList<NamedBean> validDestMast = validPaths.get(key);
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

    public void generateSection() {
        SectionManager sm = InstanceManager.sectionManagerInstance();
        for (NamedBean nb : sm.getNamedBeanList()) {
            if (((Section) nb).getSectionType() == Section.SIGNALMASTLOGIC) {
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
                            //Auto running requires forward/reverse sensors, but at this stage SML does not support that, so just create dummy internal ones for now.
                            Sensor sen = InstanceManager.sensorManagerInstance().provideSensor("IS:" + sec.getSystemName() + ":forward");
                            sen.setUserName(sec.getSystemName() + ":forward");

                            sen = InstanceManager.sensorManagerInstance().provideSensor("IS:" + sec.getSystemName() + ":reverse");
                            sen.setUserName(sec.getSystemName() + ":reverse");
                            sec.setForwardBlockingSensorName(sec.getSystemName() + ":forward");
                            sec.setReverseBlockingSensorName(sec.getSystemName() + ":reverse");
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

    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            StringBuilder message = new StringBuilder();
            boolean found = false;
            message.append(Bundle.getMessage("VetoFoundInSignalMastLogic"));
            message.append("<ul>");
            for (int i = 0; i < signalMastLogic.size(); i++) {
                try {
                    signalMastLogic.get(i).vetoableChange(evt);
                } catch (java.beans.PropertyVetoException e) {
                    if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //IN18N
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

    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameSignalMastLogic");
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogicManager.class.getName());
}
