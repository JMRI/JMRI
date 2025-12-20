package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.DefaultSignalMastLogic;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.display.layoutEditor.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Default implementation of a SignalMastLogicManager.
 *
 * @see jmri.SignalMastLogicManager
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class DefaultSignalMastLogicManager
        extends AbstractManager<SignalMastLogic>
        implements SignalMastLogicManager {

    public DefaultSignalMastLogicManager(InternalSystemConnectionMemo memo) {
        super(memo);
        registerSelf();
        addListeners();
    }

    final void addListeners(){
        InstanceManager.getDefault(LayoutBlockManager.class).addPropertyChangeListener(propertyBlockManagerListener);
        InstanceManager.getDefault(SignalMastManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(TurnoutManager.class).addVetoableChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getXMLOrder() {
        return Manager.SIGNALMASTLOGICS;
    }

    private static final SignalSpeedMap _speedMap = InstanceManager.getDefault(SignalSpeedMap.class);

    private static final String PROPERTY_INTERMEDIATE_SIGNAL = "intermediateSignal";

    private static final String PROPERTY_INTERMEDIATE_SECTION = "intermediateSection";

    public static final SignalSpeedMap getSpeedMap() {
        return _speedMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public SignalMastLogic getSignalMastLogic(SignalMast source) {
        for (SignalMastLogic signalMastLogic : _beans) {
            if (signalMastLogic.getSourceMast() == source) {
                return signalMastLogic;
            }
        }
        return null;
    }

    /**
     * Provide / create New SML.
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public SignalMastLogic newSignalMastLogic(@Nonnull SignalMast source) throws IllegalArgumentException {
        for (SignalMastLogic signalMastLogic : _beans) {
            if (signalMastLogic.getSourceMast() == source) {
                return signalMastLogic;
            }
        }
        SignalMastLogic logic = new DefaultSignalMastLogic(source);
        _beans.add(logic);
        firePropertyChange(PROPERTY_LENGTH, null, _beans.size());
        return logic;
    }

    /** {@inheritDoc} */
    @Override
    public void replaceSignalMast(@Nonnull SignalMast oldMast, @Nonnull SignalMast newMast) {
        Objects.requireNonNull(oldMast);
        Objects.requireNonNull(newMast);

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
    public void swapSignalMasts(@Nonnull SignalMast mastA, @Nonnull SignalMast mastB) {
        Objects.requireNonNull(mastA);
        Objects.requireNonNull(mastB);

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
    @Nonnull
    @Override
    public List<SignalMastLogic> getLogicsByDestination(@Nonnull SignalMast destination) {
        List<SignalMastLogic> list = new ArrayList<>();
        for (SignalMastLogic source : _beans) {
            if (source.isDestinationValid(destination)) {
                list.add(source);
            }
        }
        return list;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public List<SignalMastLogic> getSignalMastLogicList() {
        return new ArrayList<>(_beans);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSignalMastUsed(@Nonnull SignalMast mast) {
        SignalMastLogic sml = getSignalMastLogic(mast);
        if ( sml != null
            /* Although we might have it registered as a source, it may not have
             any valid destination, so therefore it can be returned as not in use. */
            && !sml.getDestinationList().isEmpty()) {
                return true;
        }
        return !getLogicsByDestination(mast).isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public void removeSignalMastLogic(@Nonnull SignalMastLogic sml, @Nonnull SignalMast dest) {
        if (sml.removeDestination(dest)) {
            removeSignalMastLogic(sml);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeSignalMastLogic(@Nonnull SignalMastLogic sml) {
        Objects.requireNonNull(sml);
        //Need to provide a method to delete and dispose.
        sml.dispose();

        _beans.remove(sml);
        firePropertyChange(PROPERTY_LENGTH, null, _beans.size());
    }

    /** {@inheritDoc} */
    @Override
    public void removeSignalMast(@Nonnull SignalMast mast) {
        Objects.requireNonNull(mast);
        for (SignalMastLogic source : _beans) {
            if (source.isDestinationValid(mast)) {
                source.removeDestination(mast);
            }
        }
        SignalMastLogic sml = getSignalMastLogic(mast);
        if ( sml != null ) {
            removeSignalMastLogic(sml);
        }
    }

    /**
     * Disable the use of info from the Layout Editor Panels to configure
     * a Signal Mast Logic for a specific Signal Mast.
     *
     * @param mast The Signal Mast for which LE info is to be disabled
     */
    @Override
    public void disableLayoutEditorUse(@Nonnull SignalMast mast) {
        SignalMastLogic source = getSignalMastLogic(mast);
        if (source != null) {
            source.disableLayoutEditorUse();
        }
        for (SignalMastLogic sml : getLogicsByDestination(mast)) {
            try {
                sml.useLayoutEditor(false, mast);
            } catch (JmriException e) {
                log.error("Error occurred while trying to disable layout editor use", e);
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

    private final PropertyChangeListener propertyBlockManagerListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if ( LayoutBlockManager.PROPERTY_TOPOLOGY.equals(e.getPropertyName())) {
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

    private boolean runWhenStablised = false;

    /**
     * Discover valid destination Signal Masts for a given source Signal Mast on a
     * given Layout Editor Panel.
     *
     * @param source Source SignalMast
     * @param layout Layout Editor panel to check.
     */
    @Override
    public void discoverSignallingDest(@Nonnull SignalMast source, @Nonnull LayoutEditor layout) throws JmriException {
        firePropertyChange(PROPERTY_AUTO_SIGNALMAST_GENERATE_START, null, source.getDisplayName());

        HashMap<SignalMast, List<NamedBean>> validPaths = new HashMap<>();
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        if (!lbm.isAdvancedRoutingEnabled()) {
            //log.debug("advanced routing not enabled");
            throw new JmriException("advanced routing not enabled");
        }
        if (!lbm.routingStablised()) {
            throw new JmriException("routing not stabilised");
        }

        validPaths.put(source, lbm.getLayoutBlockConnectivityTools()
            .discoverPairDest(source, layout, SignalMast.class, LayoutBlockConnectivityTools.Routing.MASTTOMAST));

        validPaths.entrySet().forEach( entry -> {
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
                        log.error("Exception found when adding pair {}  to destination {}/\n{}",
                            source.getDisplayName(), sm.getDisplayName(), e.toString());
                        //throw e;
                    }
                }
            }
            if (sml.getDestinationList().size() == 1
                    && sml.getAutoTurnouts(sml.getDestinationList().get(0)).isEmpty()) {
                key.setProperty(PROPERTY_INTERMEDIATE_SIGNAL, true);
            } else {
                key.removeProperty(PROPERTY_INTERMEDIATE_SIGNAL);
            }
        });
        initialise();
        firePropertyChange(PROPERTY_AUTO_SIGNALMAST_GENERATE_COMPLETE, null, source.getDisplayName());
    }

    /**
     * Discover all possible valid source + destination signal mast pairs on all
     * Layout Editor Panels.
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
        HashMap<NamedBean, List<NamedBean>> validPaths = lbm.getLayoutBlockConnectivityTools()
            .discoverValidBeanPairs(null, SignalMast.class, LayoutBlockConnectivityTools.Routing.MASTTOMAST);

        firePropertyChange(PROPERTY_AUTO_GENERATE_UPDATE, null,
            ("Found " + validPaths.size() + " masts as sources for logic"));
        InstanceManager.getDefault(SignalMastManager.class).getNamedBeanSet().forEach(nb ->
            nb.removeProperty(PROPERTY_INTERMEDIATE_SIGNAL));
        for (Entry<NamedBean, List<NamedBean>> e : validPaths.entrySet()) {
            if (log.isDebugEnabled()) {
                e.getValue().forEach(dest -> log.debug("Found valid mast pair: {} -> {}", e.getKey().getDisplayName(), dest.getDisplayName()));
            }
            SignalMast key = (SignalMast) e.getKey();
            SignalMastLogic sml = getSignalMastLogic(key);
            if (sml == null) {
                sml = newSignalMastLogic(key);
            }
            List<NamedBean> validDestMast = validPaths.get(key);
            for (NamedBean nb : validDestMast) {
                if (!sml.isDestinationValid((SignalMast) nb)) {
                    try {
                        sml.setDestinationMast((SignalMast) nb);
                        sml.useLayoutEditorDetails(true, true, (SignalMast) nb);
                        log.debug("SML Manager: Setting up logic for source '{}' to destination '{}'", key.getDisplayName(), nb.getDisplayName());
                        sml.useLayoutEditor(true, (SignalMast) nb);
                    }
                    catch (JmriException ex) {
                        //log.debug("we shouldn't get an exception here!");
                        log.warn("Unexpected exception setting mast", ex);
                    }
                }
            }
            if (sml.getDestinationList().size() == 1
                    && sml.getAutoTurnouts(sml.getDestinationList().get(0)).isEmpty()) {
                key.setProperty(PROPERTY_INTERMEDIATE_SIGNAL, true);
            }
        }
        initialise();
        firePropertyChange(PROPERTY_AUTO_GENERATE_COMPLETE, null, null);
    }

    /**
     * Populate Sections of type SIGNALMASTLOGIC used with Layout Editor with
     * Signal Mast attributes as stored in Signal Mast Logic.
     */
    public void generateSection() {
        SectionManager sm = InstanceManager.getDefault(SectionManager.class);
        sm.getNamedBeanSet().stream().map( nb -> {
            if (nb.getSectionType() == Section.SIGNALMASTLOGIC) {
                nb.removeProperty(PROPERTY_INTERMEDIATE_SECTION);
            }
            return nb;
        }).forEachOrdered( nb -> nb.removeProperty("forwardMast"));
        for (SignalMastLogic sml : getSignalMastLogicList()) {
            LayoutBlock faceLBlock = sml.getFacingBlock();
            if (faceLBlock != null) {
                boolean sourceIntermediate = false;
                Object intermSigProp = sml.getSourceMast().getProperty(PROPERTY_INTERMEDIATE_SIGNAL);
                if (intermSigProp != null) {
                    sourceIntermediate = ((Boolean) intermSigProp);
                }
                for (SignalMast destMast : sml.getDestinationList()) {
                    java.util.List<Block> autoBlocks = sml.getAutoBlocksBetweenMasts(destMast);
                    log.debug("generateSection: Considering SML path for section creation: {} -> {}",
                                sml.getSourceMast().getDisplayName(), destMast.getDisplayName());
                    log.debug("generateSection: Found {} auto-blocks for this path: {}", autoBlocks.size(), autoBlocks);
                    if (!autoBlocks.isEmpty()) {
                        String secUserName = sml.getSourceMast().getDisplayName() + ":" + destMast.getDisplayName();
                        Section sec = sm.getSection(secUserName);
                        if (sec != null) {
                            //A Section already exists, lets check that it is one used with the SML, if so carry on using that.
                            if (sec.getSectionType() != Section.SIGNALMASTLOGIC) {
                                break;
                            }
                        } else {
                            try {
                                sec = sm.createNewSection(secUserName);
                            } catch(IllegalArgumentException ex){
                                log.warn("Unable to create section for {} {}",secUserName,ex.getMessage());
                                continue;
                            }
                            // new mast
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
                        log.debug("generateSection: Using/Creating section '{}' for path {} -> {}",
                                sec.getDisplayName(), sml.getSourceMast().getDisplayName(), destMast.getDisplayName());
                        sml.setAssociatedSection(sec, destMast);
                        sec.setProperty("forwardMast", destMast.getDisplayName());
                        boolean destIntermediate = false;
                        Object destMastImSigProp = destMast.getProperty(PROPERTY_INTERMEDIATE_SIGNAL);
                        if ( destMastImSigProp != null) {
                            destIntermediate = ((Boolean) destMastImSigProp);
                        }

                        sec.setProperty(PROPERTY_INTERMEDIATE_SECTION, sourceIntermediate || destIntermediate);

                        //Not 100% sure about this for now so will comment out
                        //sml.addSensor(sec.getSystemName()+":forward", Sensor.INACTIVE, destMast);
                    }
                }
            } else {
                log.debug("No facing block found {}", sml.getSourceMast().getDisplayName());
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int setupSignalMastsDirectionSensors() {
        int errorCount = 0;
        for (SignalMastLogic sml : getSignalMastLogicList()) {
            errorCount += sml.setupDirectionSensors();
        }
        return errorCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSignalMastsDirectionSensors() {
        for (SignalMastLogic sml : getSignalMastLogicList()) {
            sml.removeDirectionSensors();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose(){
        InstanceManager.getDefault(LayoutBlockManager.class).removePropertyChangeListener(propertyBlockManagerListener);
        InstanceManager.getDefault(SignalMastManager.class).removeVetoableChangeListener(this);
        InstanceManager.getDefault(TurnoutManager.class).removeVetoableChangeListener(this);
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSignalMastLogicManager.class);
}
