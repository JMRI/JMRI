package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.SignalMastLogicManager;
import jmri.SignalMastManager;
import jmri.implementation.DefaultSignalMastLogic;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a SignalMastLogicManager.
 * @see jmri.SignalMastLogicManager
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public class DefaultSignalMastLogicManager
        extends AbstractManager<SignalMastLogic>
        implements SignalMastLogicManager {

    public DefaultSignalMastLogicManager(InternalSystemConnectionMemo memo) {
        super(memo);
        registerSelf();
        InstanceManager.getDefault(LayoutBlockManager.class).addPropertyChangeListener(propertyBlockManagerListener);
        InstanceManager.getDefault(SignalMastManager.class).addVetoableChangeListener(this);
        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        //_speedMap = InstanceManager.getDefault(SignalSpeedMap.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.SIGNALMASTLOGICS;
    }

    private static SignalSpeedMap _speedMap = InstanceManager.getDefault(SignalSpeedMap.class);

    public final static SignalSpeedMap getSpeedMap() {
        return _speedMap;
    }

    /** {@inheritDoc} */
    @Override
    public SignalMastLogic getSignalMastLogic(SignalMast source) {
        for (SignalMastLogic signalMastLogic : _beans) {
            if (signalMastLogic.getSourceMast() == source) {
                return signalMastLogic;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SignalMastLogic newSignalMastLogic(SignalMast source) {
        for (SignalMastLogic signalMastLogic : _beans) {
            if (signalMastLogic.getSourceMast() == source) {
                return signalMastLogic;
            }
        }
        SignalMastLogic logic = new DefaultSignalMastLogic(source);
        _beans.add(logic);
        firePropertyChange("length", null, _beans.size());
        return logic;
    }

    /** {@inheritDoc} */
    @Override
    public void replaceSignalMast(SignalMast oldMast, SignalMast newMast) {
        if (oldMast == null || newMast == null) {
            return;
        }
        for (SignalMastLogic source : _beans) {
            if (source.getSourceMast() == oldMast) {
                source.replaceSourceMast(oldMast, newMast);
            } else {
                source.replaceDestinationMast(oldMast, newMast);
            }
        }
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public List<SignalMastLogic> getLogicsByDestination(SignalMast destination) {
        List<SignalMastLogic> list = new ArrayList<>();
        for (SignalMastLogic source : _beans) {
            if (source.isDestinationValid(destination)) {
                list.add(source);
            }
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public List<SignalMastLogic> getSignalMastLogicList() {
        return new ArrayList<>(_beans);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSignalMastUsed(SignalMast mast) {
        if (getSignalMastLogic(mast) != null) {
            /* Although we might have it registered as a source, it may not have
             any valid destination, so therefore it can be returned as not in use. */
            if (!getSignalMastLogic(mast).getDestinationList().isEmpty()) {
                return true;
            }
        }
        if (!getLogicsByDestination(mast).isEmpty()) {
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void removeSignalMastLogic(SignalMastLogic sml, SignalMast dest) {
        if (sml.removeDestination(dest)) {
            removeSignalMastLogic(sml);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeSignalMastLogic(SignalMastLogic sml) {
        if (sml == null) {
            return;
        }
        //Need to provide a method to delete and dispose.
        sml.dispose();

        _beans.remove(sml);
        firePropertyChange("length", null, _beans.size());
    }

    /** {@inheritDoc} */
    @Override
    public void removeSignalMast(SignalMast mast) {
        if (mast == null) {
            return;
        }
        for (SignalMastLogic source : _beans) {
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
            } catch (JmriException e) {
                log.error("Error occurred while trying to disable layout editor use " + e);
            }
        }
    }

    // Abstract methods to be extended by subclasses:

    /**
     * Initialise all the Signal Mast Logics. Primarily used after
     * loading a configuration.
     */
    @Override
    public void initialise() {
        for (SignalMastLogic signalMastLogic : _beans) {
            signalMastLogic.initialise();
        }
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    int signalLogicDelay = 500;

    /** {@inheritDoc} */
    @Override
    public int getSignalLogicDelay() {
        return signalLogicDelay;
    }

    /** {@inheritDoc} */
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
                    for (SignalMastLogic signalMastLogic : _beans) {
                        signalMastLogic.setupLayoutEditorDetails();
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

        Hashtable<SignalMast, List<NamedBean>> validPaths = new Hashtable<>();
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
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

        for (Map.Entry<SignalMast, List<NamedBean>> entry : validPaths.entrySet()) {
            SignalMast key = entry.getKey();
            SignalMastLogic sml = getSignalMastLogic(key);
            if (sml == null) {
                sml = newSignalMastLogic(key);
            }

            List<NamedBean> validDestMast = entry.getValue();
            for (NamedBean sm : validDestMast) {
                if (!sml.isDestinationValid((SignalMast) sm)) {
                    try {
                        sml.setDestinationMast((SignalMast) sm);
                        sml.useLayoutEditorDetails(true, true, (SignalMast) sm);
                        sml.useLayoutEditor(true, (SignalMast) sm);
                    } catch (JmriException e) {
                        //log.debug("We shouldn't get an exception here");
                        log.error("Exception found when adding pair {}  to destination {}/\n{}", source.getDisplayName(), sm.getDisplayName(), e.toString());
                        //throw e;
                    }
                }
            }
            if (sml.getDestinationList().size() == 1 && sml.getAutoTurnouts(sml.getDestinationList().get(0)).isEmpty()) {
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
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
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
        for (NamedBean nb : InstanceManager.getDefault(SignalMastManager.class).getNamedBeanSet()) {
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
                    } catch (JmriException ex) {
                        //log.debug("we shouldn't get an exception here!");
                        log.debug(ex.getLocalizedMessage(), ex);
                    }
                }
            }
            if (sml.getDestinationList().size() == 1 && sml.getAutoTurnouts(sml.getDestinationList().get(0)).isEmpty()) {
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
        SectionManager sm = InstanceManager.getDefault(SectionManager.class);
        for (Section nb : sm.getNamedBeanSet()) {
            if (nb.getSectionType() == Section.SIGNALMASTLOGIC) {
                nb.removeProperty("intermediateSection");
            }
            nb.removeProperty("forwardMast");
        }
        for (SignalMastLogic sml : getSignalMastLogicList()) {
            LayoutBlock faceLBlock = sml.getFacingBlock();
            if (faceLBlock != null) {
                boolean sourceIntermediate = false;
                if (sml.getSourceMast().getProperty("intermediateSignal") != null) {
                    sourceIntermediate = ((Boolean) sml.getSourceMast().getProperty("intermediateSignal"));
                }
                for (SignalMast destMast : sml.getDestinationList()) {
                    if (!sml.getAutoBlocksBetweenMasts(destMast).isEmpty()) {
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
                            destIntermediate = ((Boolean) destMast.getProperty("intermediateSignal"));
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

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameSignalMastLogics" : "BeanNameSignalMastLogic");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<SignalMastLogic> getNamedBeanClass() {
        return SignalMastLogic.class;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogicManager.class);
}
