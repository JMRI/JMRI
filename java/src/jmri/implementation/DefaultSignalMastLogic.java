package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTrackExpectedState;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link jmri.SignalMastLogic}.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class DefaultSignalMastLogic extends AbstractNamedBean implements SignalMastLogic, VetoableChangeListener {

    SignalMast source;
    SignalMast destination;
    String stopAspect;

    Hashtable<SignalMast, DestinationMast> destList = new Hashtable<>();
    LayoutEditor editor;

    LayoutBlock facingBlock = null;
    LayoutBlock remoteProtectingBlock = null;

    boolean disposing = false;

    /**
     * Initialise a Signal Mast Logic for a given source Signal Mast.
     *
     * @param source  The Signal Mast we are configuring an SML for
     */
    public DefaultSignalMastLogic(@Nonnull SignalMast source) {
        super(source.toString()); // default system name
        this.source = source;
        try {
            this.stopAspect = source.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
            this.source.addPropertyChangeListener(propertySourceMastListener);
            if (source.getAspect() == null) {
                source.setAspect(stopAspect);
            }
        } catch (Exception ex) {
            log.error("Error while creating Signal Logic", ex);
        }
    }

    // Most of the following methods will inherit Javadoc from SignalMastLogic.java
    /**
     * {@inheritDoc }
     */
    @Override
    public void setFacingBlock(LayoutBlock facing) {
        facingBlock = facing;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayoutBlock getFacingBlock() {
        return facingBlock;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayoutBlock getProtectingBlock(@Nonnull SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return null;
        }
        return destList.get(dest).getProtectingBlock();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public SignalMast getSourceMast() {
        return source;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void replaceSourceMast(SignalMast oldMast, SignalMast newMast) {
        if (oldMast != source) {
            // Old mast does not match new mast so will exit replace
            return;
        }
        source.removePropertyChangeListener(propertySourceMastListener);
        source = newMast;
        stopAspect = source.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
        source.addPropertyChangeListener(propertySourceMastListener);
        if (source.getAspect() == null) {
            source.setAspect(stopAspect);
        }
        getDestinationList().forEach(sm -> {
            DestinationMast destMast = destList.get(sm);
            if (destMast.getAssociatedSection() != null) {
                String oldUserName = destMast.getAssociatedSection().getUserName();
                String newUserName = source.getDisplayName() + ":" + sm.getDisplayName();
                if (oldUserName != null) {
                    InstanceManager.getDefault(NamedBeanHandleManager.class).renameBean(oldUserName, newUserName, ((NamedBean) destMast.getAssociatedSection()));
                } else {
                    log.warn("AssociatedSection oldUserName null for destination mast {}, skipped", destMast.getDisplayName());
                }
            }
        });
        firePropertyChange("updatedSource", oldMast, newMast);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void replaceDestinationMast(SignalMast oldMast, SignalMast newMast) {
        if (!destList.containsKey(oldMast)) {
            return;
        }
        DestinationMast destMast = destList.get(oldMast);
        destMast.updateDestinationMast(newMast);
        if (destination == oldMast) {
            oldMast.removePropertyChangeListener(propertyDestinationMastListener);
            newMast.addPropertyChangeListener(propertyDestinationMastListener);
            destination = newMast;
            setSignalAppearance();
        }
        destList.remove(oldMast);
        if (destMast.getAssociatedSection() != null) {
            String oldUserName = destMast.getAssociatedSection().getUserName();
            String newUserName = source.getDisplayName() + ":" + newMast.getDisplayName();
            if (oldUserName != null) {
                InstanceManager.getDefault(NamedBeanHandleManager.class).renameBean(oldUserName, newUserName, destMast.getAssociatedSection());
            } else {
                log.warn("AssociatedSection oldUserName null for destination mast {}, skipped", destMast.getDisplayName());
            }
        }
        destList.put(newMast, destMast);
        firePropertyChange("updatedDestination", oldMast, newMast);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setDestinationMast(SignalMast dest) {
        if (destList.containsKey(dest)) {
            // if already present, not a change
            log.debug("Destination mast '{}' was already defined in SML with this source mast", dest.getDisplayName());
            return;
        }
        int oldSize = destList.size();
        destList.put(dest, new DestinationMast(dest));
        //InstanceManager.getDefault(SignalMastLogicManager.class).addDestinationMastToLogic(this, dest);
        firePropertyChange("length", oldSize, destList.size());
        // make new dest mast appear in (update of) SignallingSourcePanel Table by having that table listen to PropertyChange Events from SML TODO
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isDestinationValid(SignalMast dest) {
        if (dest == null) {
            return false;
        }
        return destList.containsKey(dest);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<SignalMast> getDestinationList() {
        List<SignalMast> out = new ArrayList<>();
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getComment(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return "";
        }
        return destList.get(dest).getComment();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setComment(String comment, SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return;
        }
        destList.get(dest).setComment(comment);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setStore(int store, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setStore(store);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getStoreState(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return STORENONE;
        }
        return destList.get(destination).getStoreState();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setEnabled(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return;
        }
        destList.get(dest).setEnabled();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setDisabled(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return;
        }
        destList.get(dest).setDisabled();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isEnabled(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return false;
        }
        return destList.get(dest).isEnabled();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isActive(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return false;
        }
        return destList.get(dest).isActive();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public SignalMast getActiveDestination() {
        for (SignalMast sm : getDestinationList()) {
            if (destList.get(sm).isActive()) {
                return sm;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean removeDestination(SignalMast dest) {
        int oldSize = destList.size();
        if (destList.containsKey(dest)) {
            //InstanceManager.getDefault(SignalMastLogicManager.class).removeDestinationMastToLogic(this, dest);
            destList.get(dest).dispose();
            destList.remove(dest);
            firePropertyChange("length", oldSize, destList.size());
        }
        return destList.isEmpty();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void disableLayoutEditorUse() {
        destList.values().forEach(dest -> {
            try {
                dest.useLayoutEditor(false);
            } catch (JmriException e) {
                log.error("Could not disable LayoutEditor ",  e);
            }
        });
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void useLayoutEditor(boolean boo, SignalMast destination) throws JmriException {
        if (!destList.containsKey(destination)) {
            return;
        }
        if (boo) {
            log.debug("Set use layout editor");
            Set<LayoutEditor> layout = InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class);
            /*We don't care which layout editor panel the signalmast is on, just so long as
             the routing is done via layout blocks*/
            // TODO: what is this?
            log.debug("userLayoutEditor finds layout list size is {}", layout.size());
            for (LayoutEditor findeditor : layout) {
                log.debug("layouteditor {}", findeditor.getLayoutName());
                if (facingBlock == null) {
                    facingBlock = InstanceManager.getDefault(LayoutBlockManager.class).getFacingBlockByMast(getSourceMast(), findeditor);
                }
            }
        }
        try {
            destList.get(destination).useLayoutEditor(boo);
        } catch (JmriException e) {
            throw e;
        }
    }

    /**
     * Add direction sensors to SML
     *
     * @return number of errors
     */
    @Override
    public int setupDirectionSensors() {
        // iterrate over the signal masts
        int errorCount = 0;
        for (SignalMast sm : getDestinationList()) {
            String displayName = sm.getDisplayName();
            Section sec = getAssociatedSection(sm);
            if (sec != null) {
                Block thisFacingBlock;
                Sensor fwd = sec.getForwardBlockingSensor();
                Sensor rev = sec.getReverseBlockingSensor();
                LayoutBlock lBlock = getFacingBlock();
                if (lBlock == null) {
                    try {
                        useLayoutEditor(true, sm); // force a refind
                    } catch (JmriException ex) {
                        continue;
                    }
                }
                if (lBlock != null) {
                    thisFacingBlock = lBlock.getBlock();
                    EntryPoint fwdEntryPoint = sec.getEntryPointFromBlock(thisFacingBlock, Section.FORWARD);
                    EntryPoint revEntryPoint = sec.getEntryPointFromBlock(thisFacingBlock, Section.REVERSE);
                    log.debug("Mast[{}] Sec[{}] Fwd[{}] Rev [{}]",
                            displayName, sec, fwd, rev);
                    if (fwd != null && fwdEntryPoint != null) {
                        addSensor(fwd.getUserName(), Sensor.INACTIVE, sm);
                        log.debug("Mast[{}] Sec[{}] Fwd[{}] fwdEP[{}]",
                                displayName, sec, fwd,
                                fwdEntryPoint.getBlock().getUserName());

                    } else if (rev != null && revEntryPoint != null) {
                        addSensor(rev.getUserName(), Sensor.INACTIVE, sm);
                        log.debug("Mast[{}] Sec[{}] Rev [{}] revEP[{}]",
                                displayName, sec, rev,
                                revEntryPoint.getBlock().getUserName());

                    } else {
                        log.error("Mast[{}] Cannot Establish entry point to protected section", displayName);
                        errorCount += 1;
                    }
                } else {
                    log.error("Mast[{}] No Facing Block", displayName);
                    errorCount += 1;
                }
            } else {
                log.error("Mast[{}] No Associated Section", displayName);
                errorCount += 1;
            }
        }
        return errorCount;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeDirectionSensors() {
        //TODO find aaway of easilty identifying the ones we added.
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean useLayoutEditor(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).useLayoutEditor();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void useLayoutEditorDetails(boolean turnouts, boolean blocks, SignalMast destination) throws JmriException {
        if (!destList.containsKey(destination)) {
            return;
        }
        try {
            destList.get(destination).useLayoutEditorDetails(turnouts, blocks);
        } catch (JmriException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean useLayoutEditorBlocks(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).useLayoutEditorBlocks();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean useLayoutEditorTurnouts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).useLayoutEditorTurnouts();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Section getAssociatedSection(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return null;
        }
        return destList.get(destination).getAssociatedSection();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setAssociatedSection(Section sec, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setAssociatedSection(sec);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean allowAutoMaticSignalMastGeneration(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).allowAutoSignalMastGen();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void allowAutoMaticSignalMastGeneration(boolean allow, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).allowAutoSignalMastGen(allow);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void allowTurnoutLock(boolean lock, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).allowTurnoutLock(lock);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isTurnoutLockAllowed(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isTurnoutLockAllowed();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setTurnouts(Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setTurnouts(turnouts);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setAutoTurnouts(Hashtable<Turnout, Integer> turnouts, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setAutoTurnouts(turnouts);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setBlocks(Hashtable<Block, Integer> blocks, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setBlocks(blocks);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setAutoBlocks(LinkedHashMap<Block, Integer> blocks, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setAutoBlocks(blocks);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setMasts(Hashtable<SignalMast, String> masts, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setMasts(masts);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setAutoMasts(Hashtable<SignalMast, String> masts, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setAutoMasts(masts, true);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setSensors(Hashtable<NamedBeanHandle<Sensor>, Integer> sensors, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setSensors(sensors);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void addSensor(String sensorName, int state, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        Sensor sen = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if (sen != null) {
            NamedBeanHandle<Sensor> namedSensor = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sen);
            destList.get(destination).addSensor(namedSensor, state);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeSensor(String sensorName, SignalMast destination) {
        Sensor sen = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        removeSensor(sen, destination);
    }

    public void removeSensor(Sensor sen, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        if (sen != null) {
            destList.get(destination).removeSensor(sen);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Block> getBlocks(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getBlocks();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Block> getAutoBlocks(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getAutoBlocks();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Block> getAutoBlocksBetweenMasts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getAutoBlocksBetweenMasts();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Turnout> getTurnouts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getTurnouts();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<NamedBeanHandle<Turnout>> getNamedTurnouts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getNamedTurnouts();
    }

    public void removeTurnout(Turnout turn, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }

        if (turn != null) {
            destList.get(destination).removeTurnout(turn);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Turnout> getAutoTurnouts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getAutoTurnouts();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Sensor> getSensors(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getSensors();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<NamedBeanHandle<Sensor>> getNamedSensors(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getNamedSensors();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<SignalMast> getSignalMasts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getSignalMasts();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<SignalMast> getAutoMasts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<>();
        }
        return destList.get(destination).getAutoSignalMasts();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void initialise() {
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            destList.get(en.nextElement()).initialise();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void initialise(SignalMast destination) {
        if (disposing) {
            return;
        }

        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).initialise();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LinkedHashMap<Block, Integer> setupLayoutEditorTurnoutDetails(List<LayoutBlock> blks, SignalMast destination) {
        if (disposing) {
            return new LinkedHashMap<>();
        }

        if (!destList.containsKey(destination)) {
            return new LinkedHashMap<>();
        }
        return destList.get(destination).setupLayoutEditorTurnoutDetails(blks);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setupLayoutEditorDetails() {
        if (disposing) {
            return;
        }
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            try {
                destList.get(en.nextElement()).setupLayoutEditorDetails();
            } catch (JmriException e) {
                //Considered normal if no route is valid on a Layout Editor panel
            }
        }
    }

    /**
     * Check if routes to the destination Signal Mast are clear.
     *
     * @return true if the path to the next signal is clear
     */
    boolean checkStates() {
        SignalMast oldActiveMast = destination;
        if (destination != null) {
            firePropertyChange("state", oldActiveMast, null);
            log.debug("Remove listener from destination");
            destination.removePropertyChangeListener(propertyDestinationMastListener);
            if (destList.containsKey(destination)) {
                destList.get(destination).clearTurnoutLock();
            }
        }

        Enumeration<SignalMast> en = destList.keys();
        log.debug("checkStates enumerates over {} mast(s)", destList.size());
        while (en.hasMoreElements()) {
            SignalMast key = en.nextElement();
            log.debug("  Destination mast {}", key.getDisplayName());
            log.debug("    isEnabled: {}", (destList.get(key)).isEnabled());
            log.debug("    isActive: {}", destList.get(key).isActive());

            if ((destList.get(key)).isEnabled() && (destList.get(key).isActive())) {
                destination = key;
                log.debug("      Add listener to destination");
                destination.addPropertyChangeListener(propertyDestinationMastListener);
                log.debug("      firePropertyChange: \"state\"");
                firePropertyChange("state", oldActiveMast, destination);
                destList.get(key).lockTurnouts();
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean areBlocksIncluded(List<Block> blks) {
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            boolean included;
            for (int i = 0; i < blks.size(); i++) {
                included = destList.get(dm).isBlockIncluded(blks.get(i));
                if (included) {
                    return true;
                }
                included = destList.get(dm).isAutoBlockIncluded(blks.get(i));
                if (included) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getBlockState(Block block, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getBlockState(block);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isBlockIncluded(Block block, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isBlockIncluded(block);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isTurnoutIncluded(Turnout turnout, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isTurnoutIncluded(turnout);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isSensorIncluded(Sensor sensor, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isSensorIncluded(sensor);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isSignalMastIncluded(SignalMast signal, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isSignalMastIncluded(signal);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getAutoBlockState(Block block, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getAutoBlockState(block);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getSensorState(Sensor sensor, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getSensorState(sensor);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getTurnoutState(Turnout turnout, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getTurnoutState(turnout);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getAutoTurnoutState(Turnout turnout, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getAutoTurnoutState(turnout);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getSignalMastState(SignalMast mast, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return null;
        }
        return destList.get(destination).getSignalMastState(mast);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getAutoSignalMastState(SignalMast mast, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return null;
        }
        return destList.get(destination).getAutoSignalMastState(mast);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public float getMaximumSpeed(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getMinimumSpeed();
    }

    volatile boolean inWait = false;

    /**
     * Before going active or checking that we can go active, wait 500ms
     * for things to settle down to help prevent a race condition.
     */
    synchronized void setSignalAppearance() {
        log.debug("setMastAppearance (Aspect) called for {}", source.getDisplayName());
        if (inWait) {
            log.debug("setMastAppearance (Aspect) called with inWait set, returning");
            return;
        }
        inWait = true;

        // The next line forces a single initialization of InstanceManager.getDefault(SignalSpeedMap.class)
        // before launching parallel threads
        InstanceManager.getDefault(SignalSpeedMap.class);

        // The next line forces a single initialization of InstanceManager.getDefault(SignalMastLogicManager.class)
        // before launching delay
        int tempDelay = InstanceManager.getDefault(SignalMastLogicManager.class).getSignalLogicDelay() / 2;
        log.debug("SignalMastLogicManager started (delay)");
        ThreadingUtil.runOnLayoutDelayed(
                () -> {
                    setMastAppearance();
                    inWait = false;
                },
                tempDelay
        );

    }

    /**
     * Evaluate the destination signal mast Aspect and set ours accordingly.
     */
    void setMastAppearance() {
        log.debug("Set source Signal Mast Aspect");
        if (getSourceMast().getHeld()) {
            log.debug("Signal is at a Held state so will set to the aspect defined for Held or Danger");

            String heldAspect = getSourceMast().getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.HELD);
            if (heldAspect != null) {
                log.debug("  Setting to HELD value of {}", heldAspect);
                ThreadingUtil.runOnLayout(() -> {
                    getSourceMast().setAspect(heldAspect);
                });
            } else {
                String dangerAspect = getSourceMast().getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
                log.debug("  Setting to DANGER value of {}", dangerAspect);
                ThreadingUtil.runOnLayout(() -> {
                    getSourceMast().setAspect(dangerAspect);
                });
            }
            return;
        }
        if (!checkStates()) {
            log.debug("Advanced routes not clear, set Stop aspect");
            getSourceMast().setAspect(stopAspect);
            return;
        }
        String[] advancedAspect;
        if (destination.getHeld()) {
            if (destination.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.HELD) != null) {
                advancedAspect = getSourceMast().getAppearanceMap().getValidAspectsForAdvancedAspect(destination.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.HELD));
            } else {
                advancedAspect = getSourceMast().getAppearanceMap().getValidAspectsForAdvancedAspect(destination.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER));
            }
        } else {
            advancedAspect = getSourceMast().getAppearanceMap().getValidAspectsForAdvancedAspect(destination.getAspect());
        }

        log.debug("distant aspect is {}", destination.getAspect());
        log.debug("advanced aspect is {}", advancedAspect != null ? advancedAspect : "<null>");

        if (advancedAspect != null) {
            String aspect = stopAspect;
            if (destList.get(destination).permissiveBlock) {
                if (!getSourceMast().isPermissiveSmlDisabled()) {
                    //if a block is in a permissive state then we set the permissive appearance
                    aspect = getSourceMast().getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.PERMISSIVE);
                }
            } else {
                for (String advancedAspect1 : advancedAspect) {
                    if (!getSourceMast().isAspectDisabled(advancedAspect1)) {
                        aspect = advancedAspect1;
                        break;
                    }
                }
                List<Integer> divergAspects = new ArrayList<>();
                List<Integer> nonDivergAspects = new ArrayList<>();
                List<Integer> eitherAspects = new ArrayList<>();
                if (advancedAspect.length > 1) {
                    float maxSigSpeed = -1;
                    float maxPathSpeed = destList.get(destination).getMinimumSpeed();
                    boolean divergRoute = destList.get(destination).turnoutThrown;

                    log.debug("Diverging route? {}", divergRoute);
                    boolean divergFlagsAvailable = false;
                    //We split the aspects into two lists, one with diverging flag set, the other without.
                    for (int i = 0; i < advancedAspect.length; i++) {
                        String div = null;
                        if (!getSourceMast().isAspectDisabled(advancedAspect[i])) {
                            div = (String) getSourceMast().getSignalSystem().getProperty(advancedAspect[i], "route");
                        }
                        if (div != null) {
                            if (div.equals("Diverging")) {
                                log.debug("Aspect {} added as Diverging Route", advancedAspect[i]);
                                divergAspects.add(i);
                                divergFlagsAvailable = true;
                                log.debug("Using Diverging Flag");
                            } else if (div.equals("Either")) {
                                log.debug("Aspect {} added as both Diverging and Normal Route", advancedAspect[i]);
                                nonDivergAspects.add(i);
                                divergAspects.add(i);
                                divergFlagsAvailable = true;
                                eitherAspects.add(i);
                                log.debug("Using Diverging Flag");
                            } else {
                                log.debug("Aspect {} added as Normal Route", advancedAspect[i]);
                                nonDivergAspects.add(i);
                                log.debug("Aspect {} added as Normal Route", advancedAspect[i]);
                            }
                        } else {
                            nonDivergAspects.add(i);
                            log.debug("Aspect {} added as Normal Route", advancedAspect[i]);
                        }
                    }
                    if ((eitherAspects.equals(divergAspects)) && (divergAspects.size() < nonDivergAspects.size())) {
                        //There are no unique diverging aspects
                        log.debug("'Either' aspects equals divergAspects and is less than non-diverging aspects");
                        divergFlagsAvailable = false;
                    }
                    log.debug("path max speed : {}", maxPathSpeed);
                    for (int i = 0; i < advancedAspect.length; i++) {
                        if (!getSourceMast().isAspectDisabled(advancedAspect[i])) {
                            String strSpeed = (String) getSourceMast().getSignalSystem().getProperty(advancedAspect[i], "speed");
                            log.debug("Aspect Speed = {} for aspect {}", strSpeed, advancedAspect[i]);
                            /*  if the diverg flags available is set and the diverg aspect
                             array contains the entry then we will check this aspect.

                             If the diverg flag has not been set then we will check.
                             */
                            log.debug("advanced aspect {}",advancedAspect[i]);
                            if ((divergRoute && (divergFlagsAvailable) && (divergAspects.contains(i))) || ((divergRoute && !divergFlagsAvailable) || (!divergRoute)) && (nonDivergAspects.contains(i))) {
                                log.debug("In list");
                                if ((strSpeed != null) && (!strSpeed.isEmpty())) {
                                    float speed = 0.0f;
                                    try {
                                        speed = Float.parseFloat(strSpeed);
                                    } catch (NumberFormatException nx) {
                                        // not a number, perhaps a name?
                                        try {
                                            speed = InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(strSpeed);
                                        } catch (IllegalArgumentException ex) {
                                            // not a name either
                                            log.warn("Using speed = 0.0 because could not understand \"{}\"", strSpeed);
                                        }
                                    }
                                    //Integer state = Integer.parseInt(strSpeed);
                                    /* This pics out either the highest speed signal if there
                                     * is no block speed specified or the highest speed signal
                                     * that is under the minimum block speed.
                                     */
                                    log.debug("{} signal state speed {} maxSigSpeed {} maxPathSpeed {}", destination.getDisplayName(), speed, maxSigSpeed, maxPathSpeed);
                                    if (maxPathSpeed == 0) {
                                        if (maxSigSpeed == -1) {
                                            log.debug("min speed on this route is equal to 0 so will set this as our max speed");
                                            maxSigSpeed = speed;
                                            aspect = advancedAspect[i];
                                            log.debug("Aspect to set is {}", aspect);
                                        } else if (speed > maxSigSpeed) {
                                            log.debug("new speed is faster than old will use this");
                                            maxSigSpeed = speed;
                                            aspect = advancedAspect[i];
                                            log.debug("Aspect to set is {}", aspect);
                                        }
                                    } else if ((speed > maxSigSpeed) && (maxSigSpeed < maxPathSpeed) && (speed <= maxPathSpeed)) {
                                        //Only set the speed to the lowest if the max speed is greater than the path speed
                                        //and the new speed is less than the last max speed
                                        log.debug("our minimum speed on this route is less than our state speed, we will set this as our max speed");
                                        maxSigSpeed = speed;
                                        aspect = advancedAspect[i];
                                        log.debug("Aspect to set is {}", aspect);
                                    } else if ((maxSigSpeed > maxPathSpeed) && (speed < maxSigSpeed)) {
                                        log.debug("our max signal speed is greater than our path speed on this route, our speed is less that the maxSigSpeed");
                                        maxSigSpeed = speed;
                                        aspect = advancedAspect[i];
                                        log.debug("Aspect to set is {}", aspect);

                                    } else if (maxSigSpeed == -1) {
                                        log.debug("maxSigSpeed returned as -1");
                                        maxSigSpeed = speed;
                                        aspect = advancedAspect[i];
                                        log.debug("Aspect to set is {}", aspect);
                                    }
                                }
                            }
                        } else {
                            log.debug("Aspect has been disabled {}", advancedAspect[i]);
                        }
                    }
                }
            }
            if ((aspect != null) && (!aspect.isEmpty())) {
                log.debug("setMastAppearance setting aspect \"{}\"", aspect);
                String aspectSet = aspect; // for lambda
                try {
                    ThreadingUtil.runOnLayout(() -> {
                        getSourceMast().setAspect(aspectSet);
                    });
                } catch (Exception ex) {
                    log.error("Exception while setting Signal Logic", ex);
                }
                return;
            }
        }
        log.debug("Aspect returned is not valid, setting stop");
        ThreadingUtil.runOnLayout(() -> {
            getSourceMast().setAspect(stopAspect);
        });
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setConflictingLogic(SignalMast sm, LevelXing lx) {
        if (sm == null) {
            return;
        }
        log.debug("setConflicting logic mast {}", sm.getDisplayName());
        if (sm == source) {
            log.debug("source is us so exit");
            return;
        }
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockAC())) {
                destList.get(dm).addAutoSignalMast(sm);
            } else if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockBD())) {
                destList.get(dm).addAutoSignalMast(sm);
            } else if (destList.get(dm).isAutoBlockIncluded(lx.getLayoutBlockAC())) {
                destList.get(dm).addAutoSignalMast(sm);
            } else if (destList.get(dm).isAutoBlockIncluded(lx.getLayoutBlockBD())) {
                destList.get(dm).addAutoSignalMast(sm);
            } else {
                log.debug("Block not found");
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeConflictingLogic(SignalMast sm, LevelXing lx) {
        if (sm == source) {
            return;
        }
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockAC())) {
                destList.get(dm).removeAutoSignalMast(sm);
            } else if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockBD())) {
                destList.get(dm).removeAutoSignalMast(sm);
            }
        }
    }

    /**
     * Class to store SML properties for a destination mast paired with this
     * source mast.
     */
    private class DestinationMast {

        LayoutBlock destinationBlock = null;
        LayoutBlock protectingBlock = null; //this is the block that the source signal is protecting

        List<NamedBeanSetting> userSetTurnouts = new ArrayList<>(0);
        Hashtable<Turnout, Integer> autoTurnouts = new Hashtable<>(0);
        //Hashtable<Turnout, Boolean> turnoutThroats = new Hashtable<Turnout, Boolean>(0);
        //Hashtable<Turnout, Boolean> autoTurnoutThroats = new Hashtable<Turnout, Boolean>(0);

        List<NamedBeanSetting> userSetMasts = new ArrayList<>(0);
        Hashtable<SignalMast, String> autoMasts = new Hashtable<>(0);
        List<NamedBeanSetting> userSetSensors = new ArrayList<>(0);
        List<NamedBeanSetting> userSetBlocks = new ArrayList<>(0);
        boolean turnoutThrown = false;
        boolean permissiveBlock = false;
        boolean disposed = false;

        List<LevelXing> blockInXings = new ArrayList<>();

        //autoBlocks are for those automatically generated by the system.
        LinkedHashMap<Block, Integer> autoBlocks = new LinkedHashMap<>(0);

        List<Block> xingAutoBlocks = new ArrayList<>(0);
        List<Block> dblCrossoverAutoBlocks = new ArrayList<>(0);
        SignalMast destinationSignalMast;
        boolean active = false;
        boolean destMastInit = false;

        float minimumBlockSpeed = 0.0f;

        boolean useLayoutEditor = false;
        boolean useLayoutEditorTurnouts = false;
        boolean useLayoutEditorBlocks = false;
        boolean lockTurnouts = false;

        NamedBeanHandle<Section> associatedSection = null;

        DestinationMast(SignalMast destination) {
            this.destinationSignalMast = destination;
            if (destination.getAspect() == null) {
                try {
                    destination.setAspect(destination.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER));
                } catch (Exception ex) {
                    log.error("Error while creating Signal Logic", ex);
                }
            }
        }

        void updateDestinationMast(SignalMast newMast) {
            destinationSignalMast = newMast;
            if (destinationSignalMast.getAspect() == null) {
                try {
                    destinationSignalMast.setAspect(destinationSignalMast.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER));
                } catch (Exception ex) {
                    log.error("Error while creating Signal Logic", ex);
                }
            }
        }

        LayoutBlock getProtectingBlock() {
            return protectingBlock;
        }

        String getDisplayName() {
            return destinationSignalMast.getDisplayName();
        }

        String comment;

        String getComment() {
            return comment;
        }

        void setComment(String comment) {
            String old = this.comment;
            this.comment = comment;
            firePropertyChange("Comment", old, comment);
        }

        boolean isActive() {
            if (disposed) {
                log.error("checkState called even though this has been disposed of");
                return false;
            }
            return active;
        }

        float getMinimumSpeed() {
            return minimumBlockSpeed;
        }

        boolean enable = true;

        void setEnabled() {
            enable = true;
            firePropertyChange("Enabled", false, this.destinationSignalMast);
        }

        void setDisabled() {
            enable = false;
            firePropertyChange("Enabled", true, this.destinationSignalMast);
        }

        boolean isEnabled() {
            return enable;
        }

        int store = STOREALL;

        void setStore(int store) {
            this.store = store;
        }

        int getStoreState() {
            return store;
        }

        void setAssociatedSection(Section section) {
            if (section != null && (!useLayoutEditor || !useLayoutEditorBlocks)) {
                log.warn("This Logic {} to {} is not using the Layout Editor or its Blocks, the associated Section will not be populated correctly", source.getDisplayName(), destinationSignalMast.getDisplayName());
            }
            if (section == null) {
                associatedSection = null;
                return;
            }
            associatedSection = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(section.getDisplayName(), section);
            if (!autoBlocks.isEmpty()) { // associatedSection is guaranteed to exist
                createSectionDetails();
            }
        }

        Section getAssociatedSection() {
            if (associatedSection != null) {
                return associatedSection.getBean();
            }
            return null;
        }

        void createSectionDetails() {
            getAssociatedSection().removeAllBlocksFromSection();
            getAutoBlocksBetweenMasts().forEach(key -> {
                getAssociatedSection().addBlock(key);
            });
            String dir = Path.decodeDirection(getFacingBlock().getNeighbourDirection(getProtectingBlock()));
            EntryPoint ep = new EntryPoint(getProtectingBlock().getBlock(), getFacingBlock().getBlock(), dir);
            ep.setTypeForward();
            getAssociatedSection().addToForwardList(ep);

            LayoutBlock proDestLBlock = InstanceManager.getDefault(LayoutBlockManager.class).getProtectedBlockByNamedBean(destinationSignalMast, destinationBlock.getMaxConnectedPanel());
            if (proDestLBlock != null) {
                log.debug("Add protecting Block {}", proDestLBlock.getDisplayName());
                dir = Path.decodeDirection(proDestLBlock.getNeighbourDirection(destinationBlock));
                ep = new EntryPoint(destinationBlock.getBlock(), proDestLBlock.getBlock(), dir);
                ep.setTypeReverse();
                getAssociatedSection().addToReverseList(ep);
            } else {
                log.debug(" ### Protecting Block not found ### ");
            }
        }

        boolean isTurnoutLockAllowed() {
            return lockTurnouts;
        }

        void allowTurnoutLock(boolean lock) {
            if (lockTurnouts == lock) {
                return;
            }
            if (!lock) {
                clearTurnoutLock();
            }
            lockTurnouts = lock;
        }

        void setTurnouts(Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts) {
            if (this.userSetTurnouts != null) {
                userSetTurnouts.forEach(nbh -> {
                    nbh.getBean().removePropertyChangeListener(propertyTurnoutListener);
                });
            }
            destMastInit = false;
            if (turnouts == null) {
                userSetTurnouts = new ArrayList<>(0);
            } else {
                userSetTurnouts = new ArrayList<>();
                Enumeration<NamedBeanHandle<Turnout>> e = turnouts.keys();
                while (e.hasMoreElements()) {
                    NamedBeanHandle<Turnout> nbh = e.nextElement();
                    NamedBeanSetting nbs = new NamedBeanSetting(nbh, turnouts.get(nbh));
                    userSetTurnouts.add(nbs);
                }
            }
            firePropertyChange("turnouts", null, this.destinationSignalMast);
        }

        void setAutoTurnouts(Hashtable<Turnout, Integer> turnouts) {
            log.debug("{} called setAutoTurnouts with {}", destinationSignalMast.getDisplayName(), (turnouts != null ? "" + turnouts.size() + " turnouts in hash table" : "null hash table reference"));
            if (this.autoTurnouts != null) {
                Enumeration<Turnout> keys = this.autoTurnouts.keys();
                while (keys.hasMoreElements()) {
                    Turnout key = keys.nextElement();
                    key.removePropertyChangeListener(propertyTurnoutListener);
                }
                //minimumBlockSpeed = 0;
            }
            destMastInit = false;
            if (turnouts == null) {
                this.autoTurnouts = new Hashtable<>(0);
            } else {
                this.autoTurnouts = new Hashtable<>(turnouts);
            }
            firePropertyChange("autoturnouts", null, this.destinationSignalMast);
        }

        void setBlocks(Hashtable<Block, Integer> blocks) {
            log.debug("{} Set blocks called", destinationSignalMast.getDisplayName());
            if (this.userSetBlocks != null) {
                userSetBlocks.forEach(nbh -> {
                    nbh.getBean().removePropertyChangeListener(propertyBlockListener);
                });
            }
            destMastInit = false;

            userSetBlocks = new ArrayList<>(0);
            if (blocks != null) {
                userSetBlocks = new ArrayList<>();
                Enumeration<Block> e = blocks.keys();
                while (e.hasMoreElements()) {
                    Block blk = e.nextElement();
                    NamedBeanHandle<?> nbh = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(blk.getDisplayName(), blk);
                    NamedBeanSetting nbs = new NamedBeanSetting(nbh, blocks.get(blk));
                    userSetBlocks.add(nbs);
                }
            }
            firePropertyChange("blocks", null, this.destinationSignalMast);
        }

        public void setAutoBlocks(LinkedHashMap<Block, Integer> blocks) {
            if (log.isDebugEnabled()) {
                log.debug("{} called setAutoBlocks with {}", destinationSignalMast.getDisplayName(), (blocks != null ? "" + blocks.size() + " blocks in hash table" : "null hash table reference"));
            }
            if (this.autoBlocks != null) {
                autoBlocks.keySet().forEach(key -> {
                    key.removePropertyChangeListener(propertyBlockListener);
                });
            }
            destMastInit = false;
            if (blocks == null) {
                this.autoBlocks = new LinkedHashMap<>(0);

            } else {
                this.autoBlocks = new LinkedHashMap<>(blocks);
                //We shall remove the facing block in the list.
                if (facingBlock != null) {
                    if (autoBlocks.containsKey(facingBlock.getBlock())) {
                        autoBlocks.remove(facingBlock.getBlock());
                    }
                }
                if (getAssociatedSection() != null) {
                    createSectionDetails();
                }
            }
            firePropertyChange("autoblocks", null, this.destinationSignalMast);
        }

        void setMasts(Hashtable<SignalMast, String> masts) {
            if (this.userSetMasts != null) {
                userSetMasts.forEach(nbh -> {
                    nbh.getBean().removePropertyChangeListener(propertySignalMastListener);
                });
            }

            destMastInit = false;

            if (masts == null) {
                userSetMasts = new ArrayList<>(0);
            } else {
                userSetMasts = new ArrayList<>();
                Enumeration<SignalMast> e = masts.keys();
                while (e.hasMoreElements()) {
                    SignalMast mast = e.nextElement();
                    NamedBeanHandle<?> nbh = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(mast.getDisplayName(), mast);
                    NamedBeanSetting nbs = new NamedBeanSetting(nbh, masts.get(mast));
                    userSetMasts.add(nbs);
                }
            }
            firePropertyChange("masts", null, this.destinationSignalMast);
        }

        /**
         *
         * @param newAutoMasts Hashtable of signal masts and set to Aspects
         * @param overwrite    When true, replace an existing autoMasts list in
         *                     the SML
         */
        void setAutoMasts(Hashtable<SignalMast, String> newAutoMasts, boolean overwrite) {
            log.debug("{} setAutoMast Called", destinationSignalMast.getDisplayName());
            if (this.autoMasts != null) {
                Enumeration<SignalMast> keys = this.autoMasts.keys();
                while (keys.hasMoreElements()) {
                    SignalMast key = keys.nextElement();
                    key.removePropertyChangeListener(propertySignalMastListener);
                }
                //minimumBlockSpeed = 0;
            }
            destMastInit = false;
            if (overwrite) {
                if (newAutoMasts == null) {
                    this.autoMasts = new Hashtable<>(0);
                } else {
                    this.autoMasts = new Hashtable<>(newAutoMasts);
                }
            } else {
                if (newAutoMasts == null) {
                    this.autoMasts = new Hashtable<>(0);
                } else {
                    Enumeration<SignalMast> keys = newAutoMasts.keys();
                    while (keys.hasMoreElements()) {
                        SignalMast key = keys.nextElement();
                        this.autoMasts.put(key, newAutoMasts.get(key));
                    }
                }
            }
            //kick off the process to add back in signal masts at crossings.
            for (int i = 0; i < blockInXings.size(); i++) {
                blockInXings.get(i).addSignalMastLogic(source);
            }

            firePropertyChange("automasts", null, this.destinationSignalMast);
        }

        void setSensors(Hashtable<NamedBeanHandle<Sensor>, Integer> sensors) {
            if (this.userSetSensors != null) {
                userSetSensors.forEach(nbh -> {
                    nbh.getBean().removePropertyChangeListener(propertySensorListener);
                });
            }
            destMastInit = false;

            if (sensors == null) {
                userSetSensors = new ArrayList<>(0);
            } else {
                userSetSensors = new ArrayList<>();
                Enumeration<NamedBeanHandle<Sensor>> e = sensors.keys();
                while (e.hasMoreElements()) {
                    NamedBeanHandle<Sensor> nbh = e.nextElement();
                    NamedBeanSetting nbs = new NamedBeanSetting(nbh, sensors.get(nbh));
                    userSetSensors.add(nbs);
                }
            }
            firePropertyChange("sensors", null, this.destinationSignalMast);
        }

        void addSensor(NamedBeanHandle<Sensor> sen, int state) {
            for (NamedBeanSetting nbh : userSetSensors) {
                if (nbh.getBean().equals(sen.getBean())) {
                    return;
                }
            }
            sen.getBean().addPropertyChangeListener(propertySensorListener);
            NamedBeanSetting nbs = new NamedBeanSetting(sen, state);
            userSetSensors.add(nbs);
            firePropertyChange("sensors", null, this.destinationSignalMast);
        }

// not used now, preserved for later use
//         void removeSensor(NamedBeanHandle<Sensor> sen) {
//             for (NamedBeanSetting nbh : userSetSensors) {
//                 if (nbh.getBean().equals(sen.getBean())) {
//                     sen.getBean().removePropertyChangeListener(propertySensorListener);
//                     userSetSensors.remove(nbh);
//                     firePropertyChange("sensors", null, this.destination);
//                     return;
//                 }
//             }
//         }

        void removeSensor(Sensor sen) {
            for (NamedBeanSetting nbh : userSetSensors) {
                if (nbh.getBean().equals(sen)) {
                    sen.removePropertyChangeListener(propertySensorListener);
                    userSetSensors.remove(nbh);
                    firePropertyChange("sensors", null, this.destinationSignalMast);
                    return;
                }
            }
        }

        List<Block> getBlocks() {
            List<Block> out = new ArrayList<>();
            userSetBlocks.forEach(nbh -> {
                out.add((Block) nbh.getBean());
            });
            return out;
        }

        List<Block> getAutoBlocks() {
            List<Block> out = new ArrayList<>();
            Set<Block> blockKeys = autoBlocks.keySet();
            //while ( blockKeys.hasMoreElements() )
            blockKeys.forEach(key -> {
                //Block key = blockKeys.nextElement();
                out.add(key);
            });
            return out;
        }

        List<Block> getAutoBlocksBetweenMasts() {
            if (destList.get(destinationSignalMast).xingAutoBlocks.isEmpty() && destList.get(destinationSignalMast).dblCrossoverAutoBlocks.isEmpty()) {
                return getAutoBlocks();
            }
            List<Block> returnList = getAutoBlocks();
            for (Block blk : getAutoBlocks()) {
                if (xingAutoBlocks.contains(blk)) {
                    returnList.remove(blk);
                }
                if (dblCrossoverAutoBlocks.contains(blk)) {
                    returnList.remove(blk);
                }
            }

            return returnList;
        }

        List<Turnout> getTurnouts() {
            List<Turnout> out = new ArrayList<>();
            userSetTurnouts.forEach(nbh -> {
                out.add((Turnout) nbh.getBean());
            });
            return out;
        }

        void removeTurnout(Turnout turn) {
            Iterator<NamedBeanSetting> nbh = userSetTurnouts.iterator();
            while (nbh.hasNext()) {
                NamedBeanSetting i = nbh.next();
                if (i.getBean().equals(turn)) {
                    turn.removePropertyChangeListener(propertyTurnoutListener);
                    nbh.remove();
                    firePropertyChange("turnouts", null, this.destinationSignalMast);
                }
            }
        }

        @SuppressWarnings("unchecked") // (NamedBeanHandle<Turnout>) nbh.getNamedBean() is unchecked cast
        List<NamedBeanHandle<Turnout>> getNamedTurnouts() {
            List<NamedBeanHandle<Turnout>> out = new ArrayList<>();
            userSetTurnouts.forEach(nbh -> {
                out.add((NamedBeanHandle<Turnout>) nbh.getNamedBean());
            });
            return out;
        }

        List<Turnout> getAutoTurnouts() {
            List<Turnout> out = new ArrayList<>();
            Enumeration<Turnout> en = autoTurnouts.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }

        List<SignalMast> getSignalMasts() {
            List<SignalMast> out = new ArrayList<>();
            userSetMasts.forEach(nbh -> {
                out.add((SignalMast) nbh.getBean());
            });
            return out;
        }

        List<SignalMast> getAutoSignalMasts() {
            List<SignalMast> out = new ArrayList<>();
            Enumeration<SignalMast> en = autoMasts.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }

        List<Sensor> getSensors() {
            List<Sensor> out = new ArrayList<>();
            userSetSensors.forEach(nbh -> {
                out.add((Sensor) nbh.getBean());
            });
            return out;
        }

        @SuppressWarnings("unchecked") // (NamedBeanHandle<Sensor>) nbh.getNamedBean() is unchecked cast
        List<NamedBeanHandle<Sensor>> getNamedSensors() {
            List<NamedBeanHandle<Sensor>> out = new ArrayList<>();
            userSetSensors.forEach(nbh -> {
                out.add((NamedBeanHandle<Sensor>) nbh.getNamedBean());
            });
            return out;
        }

        boolean isBlockIncluded(Block block) {
            return userSetBlocks.stream().anyMatch(nbh -> (nbh.getBean().equals(block)));
        }

        boolean isAutoBlockIncluded(LayoutBlock block) {
            if (block != null) {
                return autoBlocks.containsKey(block.getBlock());
            }
            return false;
        }

        boolean isAutoBlockIncluded(Block block) {
            return autoBlocks.containsKey(block);
        }

        boolean isBlockIncluded(LayoutBlock block) {
            return userSetBlocks.stream().anyMatch(nbh -> (nbh.getBean().equals(block.getBlock())));
        }

        boolean isTurnoutIncluded(Turnout turnout) {
            return userSetTurnouts.stream().anyMatch(nbh -> (nbh.getBean().equals(turnout)));
        }

        boolean isSensorIncluded(Sensor sensor) {
            return userSetSensors.stream().anyMatch(nbh -> (nbh.getBean().equals(sensor)));
        }

        boolean isSignalMastIncluded(SignalMast signal) {
            return userSetMasts.stream().anyMatch(nbh -> (nbh.getBean().equals(signal)));
        }

        int getAutoBlockState(Block block) {
            if (autoBlocks == null) {
                return -1;
            }
            return autoBlocks.get(block);
        }

        int getBlockState(Block block) {
            if (userSetBlocks == null) {
                return -1;
            }
            for (NamedBeanSetting nbh : userSetBlocks) {
                if (nbh.getBean().equals(block)) {
                    return nbh.getSetting();
                }
            }
            return -1;
        }

        int getSensorState(Sensor sensor) {
            if (userSetSensors == null) {
                return -1;
            }
            for (NamedBeanSetting nbh : userSetSensors) {
                if (nbh.getBean().equals(sensor)) {
                    return nbh.getSetting();
                }
            }
            return -1;
        }

        int getTurnoutState(Turnout turnout) {
            if (userSetTurnouts == null) {
                return -1;
            }
            for (NamedBeanSetting nbh : userSetTurnouts) {
                if (nbh.getBean().equals(turnout)) {
                    return nbh.getSetting();
                }
            }
            return -1;
        }

        int getAutoTurnoutState(Turnout turnout) {
            if (autoTurnouts == null) {
                return -1;
            }
            if (autoTurnouts.containsKey(turnout)) {
                return autoTurnouts.get(turnout);
            }
            return -1;
        }

        String getSignalMastState(SignalMast mast) {
            if (userSetMasts == null) {
                return null;
            }
            for (NamedBeanSetting nbh : userSetMasts) {
                if (nbh.getBean().equals(mast)) {
                    return nbh.getStringSetting();
                }
            }
            return null;
        }

        String getAutoSignalMastState(SignalMast mast) {
            if (autoMasts == null) {
                return null;
            }
            return autoMasts.get(mast);
        }

        // the following 2 methods are not supplied in the implementation
        boolean inWait = false;

        /*
         * Before going active or checking that we can go active, wait
         * for things to settle down to help prevent a race condition.
         */
        void checkState() {
            if (disposed) {
                log.error("checkState called even though this has been disposed of {}", getSourceMast().getDisplayName());
                return;
            }

            if (!enable) {
                return;
            }
            if (inWait) {
                return;
            }

            log.debug("check Signal Dest State called");
            inWait = true;

            // The next line forces a single initialization of InstanceManager.getDefault(SignalMastLogicManager.class)
            // before launching parallel threads
            int tempDelay = InstanceManager.getDefault(SignalMastLogicManager.class).getSignalLogicDelay();

            ThreadingUtil.runOnLayoutDelayed(
                    () -> {
                        checkStateDetails();
                        inWait = false;
                    }, tempDelay
            );
        }

        /**
         * Check the details of this source-destination signal mast logic pair.
         * Steps through every sensor, turnout etc. before setting the SML
         * Aspect on the source mast via {
         *
         * @see #setSignalAppearance } and {
         * @see #setMastAppearance }
         */
        private void checkStateDetails() {
            turnoutThrown = false;
            permissiveBlock = false;
            if (disposed) {
                log.error("checkStateDetails called even though this has been disposed of {} {}", getSourceMast().getDisplayName(), destinationSignalMast.getDisplayName());
                return;
            }
            if (!enable) {
                return;
            }
            log.debug("From {} to {} internal check state", getSourceMast().getDisplayName(), destinationSignalMast.getDisplayName());
            active = false;
            if ((useLayoutEditor) && (autoTurnouts.isEmpty()) && (autoBlocks.isEmpty())) {
                return;
            }
            boolean state = true;
            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                if (key.getKnownState() != autoTurnouts.get(key)) {
                    if (key.getState() != autoTurnouts.get(key)) {
                        if (isTurnoutIncluded(key)) {
                            if (key.getState() != getTurnoutState(key)) {
                                state = false;
                            } else if (key.getState() == Turnout.THROWN) {
                                turnoutThrown = true;
                            }
                        } else {
                            state = false;
                        }
                    }
                } else if (key.getState() == Turnout.THROWN) {
                    turnoutThrown = true;
                }
            }

            for (NamedBeanSetting nbh : userSetTurnouts) {
                Turnout key = (Turnout) nbh.getBean();
                if (key.getKnownState() != nbh.getSetting()) {
                    state = false;
                } else if (key.getState() == Turnout.THROWN) {
                    turnoutThrown = true;
                }
            }

            Enumeration<SignalMast> mastKeys = autoMasts.keys();
            while (mastKeys.hasMoreElements()) {
                SignalMast key = mastKeys.nextElement();
                String aspect = key.getAspect();
                log.debug("key {} {} {}", key.getDisplayName(), aspect, autoMasts.get(key));
                if ((aspect != null) && (!aspect.equals(autoMasts.get(key)))) {
                    if (isSignalMastIncluded(key)) {
                        //Basically if we have a blank aspect, we don't care about the state of the signalmast
                        if (!getSignalMastState(key).isEmpty()) {
                            if (!aspect.equals(getSignalMastState(key))) {
                                state = false;
                            }
                        }
                    } else {
                        state = false;
                    }
                }
            }
            for (NamedBeanSetting nbh : userSetMasts) {
                SignalMast key = (SignalMast) nbh.getBean();
                String aspect = key.getAspect();
                if ((aspect == null) || (!aspect.equals(nbh.getStringSetting()))) {
                    state = false;
                }
            }

            for (NamedBeanSetting nbh : userSetSensors) {
                Sensor key = (Sensor) nbh.getBean();
                if (key.getKnownState() != nbh.getSetting()) {
                    state = false;
                }
            }

            for (Map.Entry<Block, Integer> entry : this.autoBlocks.entrySet()) {
                log.debug(" entry {} {} {}", entry.getKey().getDisplayName(), entry.getKey().getState(), entry.getValue());
                if (entry.getKey().getState() != autoBlocks.get(entry.getKey())) {
                    if (isBlockIncluded(entry.getKey())) {
                        if (getBlockState(entry.getKey()) != 0x03) {
                            if (entry.getKey().getState() != getBlockState(entry.getKey())) {
                                if (entry.getKey().getState() == Block.OCCUPIED && entry.getKey().getPermissiveWorking()) {
                                    permissiveBlock = true;
                                } else {
                                    state = false;
                                }
                            }
                        }
                    } else {
                        if (entry.getKey().getState() == Block.OCCUPIED && entry.getKey().getPermissiveWorking()) {
                            permissiveBlock = true;
                        } else if (entry.getKey().getState() == Block.UNDETECTED) {
                            log.debug("Block {} is UNDETECTED so treat as unoccupied", entry.getKey().getDisplayName());
                        } else {
                            state = false;
                        }
                    }
                }
            }

            for (NamedBeanSetting nbh : userSetBlocks) {
                Block key = (Block) nbh.getBean();
                if (nbh.getSetting() != 0x03) {
                    if (key.getState() != nbh.getSetting()) {
                        if (key.getState() == Block.OCCUPIED && key.getPermissiveWorking()) {
                            permissiveBlock = true;
                        } else {
                            state = false;
                        }
                    }
                }
            }
            if (permissiveBlock) {
                /*If a block has been found to be permissive, but the source signalmast
                 does not support a call-on/permissive aspect then the route can not be set*/
                if (getSourceMast().getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.PERMISSIVE) == null) {
                    state = false;
                }
            }

            /*This check is purely for use with the dispatcher, it will check to see if any of the blocks are set to "useExtraColor"
             which is a means to determine if the block is in a section that is occupied and it not ours thus we can set the signal to danger.*/
            if (state && getAssociatedSection() != null
                    && InstanceManager.getNullableDefault(jmri.jmrit.dispatcher.DispatcherFrame.class) != null
                    && InstanceManager.getNullableDefault(LayoutBlockManager.class) != null
                    && getAssociatedSection().getState() != Section.FORWARD) {

                LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
                for (Block key : autoBlocks.keySet()) {
                    LayoutBlock lb = lbm.getLayoutBlock(key);
                    if (lb != null && lb.getUseExtraColor()) {
                        state = false;
                        break;
                    }
                }
                if (!state) {
                    for (NamedBeanSetting nbh : userSetBlocks) {
                        Block key = (Block) nbh.getBean();
                        LayoutBlock lb = lbm.getLayoutBlock(key);
                        if (lb != null && lb.getUseExtraColor()) {
                            state = false;
                            break;
                        }
                    }
                }
            }

            if (!state) {
                turnoutThrown = false;
                permissiveBlock = false;
            }

            active = state;
            ThreadingUtil.runOnLayout(() -> {
                setSignalAppearance();
            });
        }

        /**
         * Set up this source-destination signal mast logic pair. Steps through
         * every list defined on the source mast.
         */
        void initialise() {
            if ((destMastInit) || (disposed)) {
                return;
            }

            active = false;
            turnoutThrown = false;
            permissiveBlock = false;
            boolean routeclear = true;
            if ((useLayoutEditor) && (autoTurnouts.isEmpty()) && (autoBlocks.isEmpty()) && (autoMasts.isEmpty())) {
                return;
            }

            calculateSpeed();

            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                key.addPropertyChangeListener(propertyTurnoutListener);

                if (key.getKnownState() != autoTurnouts.get(key)) {
                    if (key.getState() != autoTurnouts.get(key)) {
                        if (isTurnoutIncluded(key)) {
                            if (key.getState() != getTurnoutState(key)) {
                                routeclear = false;
                            } else if (key.getState() == Turnout.THROWN) {
                                turnoutThrown = true;
                            }
                        } else {
                            routeclear = false;
                        }
                    }
                } else if (key.getState() == Turnout.THROWN) {
                    turnoutThrown = true;
                }
            }

            for (NamedBeanSetting nbh : userSetTurnouts) {
                Turnout key = (Turnout) nbh.getBean();
                key.addPropertyChangeListener(propertyTurnoutListener, nbh.getBeanName(), "Signal Mast Logic:" + source.getDisplayName() + " to " + destinationSignalMast.getDisplayName());
                if (key.getKnownState() != nbh.getSetting()) {
                    routeclear = false;
                } else if (key.getState() == Turnout.THROWN) {
                    turnoutThrown = true;
                }
            }

            Enumeration<SignalMast> mastKeys = autoMasts.keys();
            while (mastKeys.hasMoreElements()) {
                SignalMast key = mastKeys.nextElement();
                log.debug("{} auto mast add list {}", destinationSignalMast.getDisplayName(), key.getDisplayName());
                key.addPropertyChangeListener(propertySignalMastListener);
                String aspect = key.getAspect();
                if ( aspect != null && !aspect.equals(autoMasts.get(key))) {
                    if (isSignalMastIncluded(key)) {
                        if (aspect.equals(getSignalMastState(key))) {
                            routeclear = false;
                        }
                    } else {
                        routeclear = false;
                    }
                }
            }

            for (NamedBeanSetting nbh : userSetMasts) {
                SignalMast key = (SignalMast) nbh.getBean();
                key.addPropertyChangeListener(propertySignalMastListener);
                String aspect = key.getAspect();
                log.debug("mast '{}' key aspect '{}'", destinationSignalMast.getDisplayName(), aspect);
                if ((aspect == null) || (!aspect.equals(nbh.getStringSetting()))) {
                    routeclear = false;
                }
            }
            for (NamedBeanSetting nbh : userSetSensors) {
                Sensor sensor = (Sensor) nbh.getBean();
                sensor.addPropertyChangeListener(propertySensorListener, nbh.getBeanName(), "Signal Mast Logic:" + source.getDisplayName() + " to " + destinationSignalMast.getDisplayName());
                if (sensor.getKnownState() != nbh.getSetting()) {
                    routeclear = false;
                }
            }

            for (Map.Entry<Block, Integer> entry : this.autoBlocks.entrySet()) {
                log.debug("{} auto block add list {}", destinationSignalMast.getDisplayName(), entry.getKey().getDisplayName());
                entry.getKey().addPropertyChangeListener(propertyBlockListener);
                if (entry.getKey().getState() != entry.getValue()) {
                    if (isBlockIncluded(entry.getKey())) {
                        if (entry.getKey().getState() != getBlockState(entry.getKey())) {
                            if (entry.getKey().getState() == Block.OCCUPIED && entry.getKey().getPermissiveWorking()) {
                                permissiveBlock = true;
                            } else {
                                routeclear = false;
                            }
                        }
                    } else {
                        if (entry.getKey().getState() == Block.OCCUPIED && entry.getKey().getPermissiveWorking()) {
                            permissiveBlock = true;
                        } else if (entry.getKey().getState() == Block.UNDETECTED) {
                            log.debug("Block {} is UNDETECTED so treat as unoccupied", entry.getKey().getDisplayName());
                        } else {
                            routeclear = false;
                        }
                    }
                }
            }

            for (NamedBeanSetting nbh : userSetBlocks) {
                Block key = (Block) nbh.getBean();
                key.addPropertyChangeListener(propertyBlockListener);
                if (key.getState() != getBlockState(key)) {
                    if (key.getState() == Block.OCCUPIED && key.getPermissiveWorking()) {
                        permissiveBlock = true;
                    } else {
                        routeclear = false;
                    }
                }
            }
            if (permissiveBlock) {
                /* If a block has been found to be permissive, but the source signalmast
                 does not support a call-on/permissive aspect then the route can not be set */
                if (getSourceMast().getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.PERMISSIVE) == null) {
                    routeclear = false;
                }
            }
            if (routeclear) {
                active = true;
                setSignalAppearance();
            } else {
                permissiveBlock = false;
                turnoutThrown = false;
            }
            destMastInit = true;
        }

        void useLayoutEditor(boolean boo) throws JmriException {
            log.debug("{} called useLayoutEditor({}), is {}", destinationSignalMast.getDisplayName(), boo, useLayoutEditor);
            if (useLayoutEditor == boo) {
                return;
            }
            useLayoutEditor = boo;
            if ((boo) && (InstanceManager.getDefault(LayoutBlockManager.class).routingStablised())) {
                try {
                    setupLayoutEditorDetails();
                } catch (JmriException e) {
                    throw e;
                    // Considered normal if there is no valid path using the layout editor.
                }
            } else {
                destinationBlock = null;
                facingBlock = null;
                protectingBlock = null;
                setAutoBlocks(null);
                setAutoTurnouts(null);
            }
        }

        void useLayoutEditorDetails(boolean turnouts, boolean blocks) throws JmriException {
            log.debug("{} use layout editor details called {}", destinationSignalMast.getDisplayName(), useLayoutEditor);
            useLayoutEditorTurnouts = turnouts;
            useLayoutEditorBlocks = blocks;
            if ((useLayoutEditor) && (InstanceManager.getDefault(LayoutBlockManager.class).routingStablised())) {
                try {
                    setupLayoutEditorDetails();
                } catch (JmriException e) {
                    throw e;
                    // Considered normal if there is no valid path using the Layout Editor.
                }
            }
        }

        void setupLayoutEditorDetails() throws JmriException {
            log.debug("setupLayoutEditorDetails: useLayoutEditor={} disposed={}", useLayoutEditor, disposed);
            if ((!useLayoutEditor) || (disposed)) {
                return;
            }
            LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
            if ( destinationBlock != null) {
                log.debug("{} Set use layout editor", destinationSignalMast.getDisplayName());
            }
            Set<LayoutEditor> layout = InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class);
            List<LayoutBlock> protectingBlocks = new ArrayList<>();
            // We don't care which Layout Editor panel the signal mast is on, just so long as
            // the routing is done via layout blocks.
            remoteProtectingBlock = null;
            for (int i = 0; i < layout.size(); i++) {
                log.debug("{} Layout name {}", destinationSignalMast.getDisplayName(), editor );
                if (facingBlock == null) {
                    facingBlock = lbm.getFacingBlockByNamedBean(getSourceMast(), editor);
                }
                if (protectingBlock == null && protectingBlocks.isEmpty()) {
                    //This is wrong
                    protectingBlocks = lbm.getProtectingBlocksByNamedBean(getSourceMast(), editor);
                }
                if (destinationBlock == null) {
                    destinationBlock = lbm.getFacingBlockByNamedBean(destinationSignalMast, editor);
                }
                if (remoteProtectingBlock == null) {
                    remoteProtectingBlock = lbm.getProtectedBlockByNamedBean(destinationSignalMast, editor);
                }
            }
            // At this point, if we are not using the Layout Editor turnout or block
            // details then there is no point in trying to gather them.
            if ((!useLayoutEditorTurnouts) && (!useLayoutEditorBlocks)) {
                return;
            }
            if (facingBlock == null) {
                log.error("No facing block found for source mast {}", getSourceMast().getDisplayName());
                throw new JmriException("No facing block found for source mast " + getSourceMast().getDisplayName());
            }
            if (destinationBlock == null) {
                log.error("No facing block found for destination mast {}", destinationSignalMast.getDisplayName());
                throw new JmriException("No facing block found for destination mast " + destinationSignalMast.getDisplayName());
            }
            List<LayoutBlock> lblks = new ArrayList<>();
            if (protectingBlock == null) {
                log.debug("protecting block is null");
                String pBlkNames = "";
                StringBuffer lBlksNamesBuf = new StringBuffer();
                for (LayoutBlock pBlk : protectingBlocks) {
                    log.debug("checking layoutBlock {}", pBlk.getDisplayName());
                    pBlkNames = pBlkNames + pBlk.getDisplayName() + " (" + lbm.getLayoutBlockConnectivityTools().checkValidDest(facingBlock, pBlk, destinationBlock, remoteProtectingBlock, LayoutBlockConnectivityTools.Routing.MASTTOMAST) + "), ";
                    if (lbm.getLayoutBlockConnectivityTools().checkValidDest(facingBlock, pBlk, destinationBlock, remoteProtectingBlock, LayoutBlockConnectivityTools.Routing.MASTTOMAST)) {
                        try {
                            lblks = lbm.getLayoutBlockConnectivityTools().getLayoutBlocks(facingBlock, destinationBlock, pBlk, true, LayoutBlockConnectivityTools.Routing.MASTTOMAST);
                            protectingBlock = pBlk;
                            log.debug("building path names...");
                            for (LayoutBlock lBlk : lblks) {
                                lBlksNamesBuf.append(" ");
                                lBlksNamesBuf.append(lBlk.getDisplayName());
                            }
                            break;
                        } catch (JmriException ee) {
                            log.debug("path not found this time");
                        }
                    }
                }
                String lBlksNames = new String(lBlksNamesBuf);

                if (protectingBlock == null) {
                    throw new JmriException("Path not valid, protecting block is null. Protecting block: " + pBlkNames + " not connected to " + facingBlock.getDisplayName() + ". Layout block names: " + lBlksNames);
                }
            }
            try {
                if (!lbm.getLayoutBlockConnectivityTools().checkValidDest(facingBlock, protectingBlock, destinationBlock, remoteProtectingBlock, LayoutBlockConnectivityTools.Routing.MASTTOMAST)) {
                    throw new JmriException("Path not valid, destination check failed.");
                }
            } catch (JmriException e) {
                throw e;
            }
            if (log.isDebugEnabled()) {
                log.debug("{} face {}", destinationSignalMast.getDisplayName(), facingBlock);
                log.debug("{} prot {}", destinationSignalMast.getDisplayName(), protectingBlock);
                log.debug("{} dest {}", destinationSignalMast.getDisplayName(), destinationBlock);
            }

            if (destinationBlock != null && protectingBlock != null && facingBlock != null) {
                setAutoMasts(null, true);
                if (log.isDebugEnabled()) {
                    log.debug("{} face {}", destinationSignalMast.getDisplayName(), facingBlock.getDisplayName());
                    log.debug("{} prot {}", destinationSignalMast.getDisplayName(), protectingBlock.getDisplayName());
                    log.debug("{} dest {}", destinationSignalMast.getDisplayName(), destinationBlock.getDisplayName());
                }

                try {
                    lblks = lbm.getLayoutBlockConnectivityTools().getLayoutBlocks(facingBlock, destinationBlock, protectingBlock, true, LayoutBlockConnectivityTools.Routing.MASTTOMAST);
                } catch (JmriException ee) {
                    log.error("No blocks found by the layout editor for pair {}-{}", source.getDisplayName(), destinationSignalMast.getDisplayName());
                }
                LinkedHashMap<Block, Integer> block = setupLayoutEditorTurnoutDetails(lblks);

                for (int i = 0; i < blockInXings.size(); i++) {
                    blockInXings.get(i).removeSignalMastLogic(source);
                }
                blockInXings = new ArrayList<>(0);
                xingAutoBlocks = new ArrayList<>(0);
                for (LayoutEditor lay : layout) {
                    for (LevelXing levelXing : lay.getLevelXings()) {
                        //Looking for a crossing that both layout blocks defined and they are individual.
                        if ((levelXing.getLayoutBlockAC() != null)
                                && (levelXing.getLayoutBlockBD() != null)
                                && (levelXing.getLayoutBlockAC() != levelXing.getLayoutBlockBD())) {
                            if (lblks.contains(levelXing.getLayoutBlockAC()) &&
                                    levelXing.getLayoutBlockAC() != facingBlock) {  // Don't include the facing xing blocks
                                block.put(levelXing.getLayoutBlockBD().getBlock(), Block.UNOCCUPIED);
                                xingAutoBlocks.add(levelXing.getLayoutBlockBD().getBlock());
                                blockInXings.add(levelXing);
                            } else if (lblks.contains(levelXing.getLayoutBlockBD()) &&
                                    levelXing.getLayoutBlockBD() != facingBlock) {  // Don't include the facing xing blocks
                                block.put(levelXing.getLayoutBlockAC().getBlock(), Block.UNOCCUPIED);
                                xingAutoBlocks.add(levelXing.getLayoutBlockAC().getBlock());
                                blockInXings.add(levelXing);
                            }
                        }
                    }
                }
                if (useLayoutEditorBlocks) {
                    setAutoBlocks(block);
                } else {
                    setAutoBlocks(null);
                }
                if (!useLayoutEditorTurnouts) {
                    setAutoTurnouts(null);
                }

                setupAutoSignalMast(null, false);
            }
            initialise();
        }

        /**
         * From a list of Layout Blocks, search for included Turnouts and their
         * Set To state.
         *
         * @param lblks List of Layout Blocks
         * @return a list of block - turnout state pairs
         */
        LinkedHashMap<Block, Integer> setupLayoutEditorTurnoutDetails(List<LayoutBlock> lblks) {
            ConnectivityUtil connection;
            List<LayoutTrackExpectedState<LayoutTurnout>> turnoutList;
            Hashtable<Turnout, Integer> turnoutSettings = new Hashtable<>();
            LinkedHashMap<Block, Integer> block = new LinkedHashMap<>();
            for (int i = 0; i < lblks.size(); i++) {
                log.debug("layoutblock {}",lblks.get(i).getDisplayName());
                block.put(lblks.get(i).getBlock(), Block.UNOCCUPIED);
                if ((i > 0)) {
                    int nxtBlk = i + 1;
                    int preBlk = i - 1;
                    if (i == lblks.size() - 1) {
                        nxtBlk = i;
                    }
                    //We use the best connectivity for the current block;
                    connection = new ConnectivityUtil(lblks.get(i).getMaxConnectedPanel());
                    if (i == lblks.size() - 1 && remoteProtectingBlock != null) {
                        turnoutList = connection.getTurnoutList(lblks.get(i).getBlock(), lblks.get(preBlk).getBlock(), remoteProtectingBlock.getBlock());
                    }else{
                        turnoutList = connection.getTurnoutList(lblks.get(i).getBlock(), lblks.get(preBlk).getBlock(), lblks.get(nxtBlk).getBlock());
                    }
                    for (int x = 0; x < turnoutList.size(); x++) {
                        LayoutTurnout lt = turnoutList.get(x).getObject();
                        if (lt instanceof LayoutSlip) {
                            LayoutSlip ls = (LayoutSlip) lt;
                            int slipState = turnoutList.get(x).getExpectedState();
                            int taState = ls.getTurnoutState(slipState);
                            Turnout tTemp = ls.getTurnout();
                            if (tTemp == null ) {
                                log.error("Unexpected null Turnout in {}, skipped", ls);
                                continue; // skip this one in loop, what else can you do?
                            }
                            turnoutSettings.put(ls.getTurnout(), taState);
                            int tbState = ls.getTurnoutBState(slipState);
                            turnoutSettings.put(ls.getTurnoutB(), tbState);
                        } else if ( lt != null ) {
                            String t = lt.getTurnoutName();
                            // temporary = why is this looking up the Turnout instead of using getTurnout()?
                            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(t);
                            if (log.isDebugEnabled()) {
                                if (    (lt.getTurnoutType() == LayoutTurnout.TurnoutType.RH_TURNOUT ||
                                         lt.getTurnoutType() == LayoutTurnout.TurnoutType.LH_TURNOUT ||
                                         lt.getTurnoutType() == LayoutTurnout.TurnoutType.WYE_TURNOUT)
                                        && (!lt.getBlockName().isEmpty())) {
                                    log.debug("turnout in list is straight left/right wye");
                                    log.debug("turnout block Name {}", lt.getBlockName());
                                    log.debug("current {} - pre {}", lblks.get(i).getBlock().getDisplayName(), lblks.get(preBlk).getBlock().getDisplayName());
                                    log.debug("A {}", lt.getConnectA());
                                    log.debug("B {}", lt.getConnectB());
                                    log.debug("C {}", lt.getConnectC());
                                    log.debug("D {}", lt.getConnectD());
                                }
                            }
                            if (turnout != null ) {
                                turnoutSettings.put(turnout, turnoutList.get(x).getExpectedState());
                            }
                            Turnout tempT;
                            if ((tempT = lt.getSecondTurnout()) != null) {
                                turnoutSettings.put(tempT, turnoutList.get(x).getExpectedState());
                            }
                            /* TODO: We could do with a more intelligent way to deal with double crossovers, other than
                                just looking at the state of the other conflicting blocks, such as looking at Signalmasts
                                that protect the other blocks and the settings of any other turnouts along the way.
                             */
                            if (lt.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER) {
                                LayoutBlock tempLB;
                                if (turnoutList.get(x).getExpectedState() == Turnout.THROWN) {
                                    if (lt.getLayoutBlock() == lblks.get(i) || lt.getLayoutBlockC() == lblks.get(i)) {
                                        // A or C, add B and D to remove list unless A=B or C=D
                                        if ((tempLB = lt.getLayoutBlockB()) != null) {
                                            if (!tempLB.equals(lt.getLayoutBlock())) {
                                                dblCrossoverAutoBlocks.add(tempLB.getBlock());
                                            }
                                            block.put(tempLB.getBlock(), Block.UNOCCUPIED);
                                        }
                                        if ((tempLB = lt.getLayoutBlockD()) != null) {
                                            if (!tempLB.equals(lt.getLayoutBlockC())) {
                                                dblCrossoverAutoBlocks.add(tempLB.getBlock());
                                            }
                                            block.put(tempLB.getBlock(), Block.UNOCCUPIED);
                                        }
                                    } else if (lt.getLayoutBlockB() == lblks.get(i) || lt.getLayoutBlockD() == lblks.get(i)) {
                                        // B or D, add A and C to remove list unless A=B or C=D
                                        if ((tempLB = lt.getLayoutBlock()) != null) {
                                            if (!tempLB.equals(lt.getLayoutBlockB())) {
                                                dblCrossoverAutoBlocks.add(tempLB.getBlock());
                                            }
                                            block.put(tempLB.getBlock(), Block.UNOCCUPIED);
                                        }
                                        if ((tempLB = lt.getLayoutBlockC()) != null) {
                                            if (!tempLB.equals(lt.getLayoutBlockD())) {
                                                dblCrossoverAutoBlocks.add(tempLB.getBlock());
                                            }
                                            block.put(tempLB.getBlock(), Block.UNOCCUPIED);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (useLayoutEditorTurnouts) {
                setAutoTurnouts(turnoutSettings);
            }
            return block;
        }

        /**
         * Generate auto signalmast for a given SML. Looks through all the other
         * logics to see if there are any blocks that are in common and thus
         * will add the other signal mast protecting that block.
         *
         * @param sml       The Signal Mast Logic for which to set up
         *                  autoSignalMasts
         * @param overwrite When true, replace an existing autoMasts list in the
         *                  SML
         */
        void setupAutoSignalMast(SignalMastLogic sml, boolean overwrite) {
            if (!allowAutoSignalMastGeneration) {
                return;
            }
            List<SignalMastLogic> smlList = InstanceManager.getDefault(SignalMastLogicManager.class).getLogicsByDestination(destinationSignalMast);
            List<Block> allBlock = new ArrayList<>();

            userSetBlocks.forEach(nbh -> {
                allBlock.add((Block) nbh.getBean());
            });

            Set<Block> blockKeys = autoBlocks.keySet();
            blockKeys.stream().filter(key -> (!allBlock.contains(key))).forEachOrdered(key -> {
                allBlock.add(key);
            });
            Hashtable<SignalMast, String> masts;
            if (sml != null) {
                masts = autoMasts;
                if (sml.areBlocksIncluded(allBlock)) {
                    SignalMast mast = sml.getSourceMast();
                    String danger = mast.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
                    masts.put(mast, danger);
                } else {
                    //No change so will leave.
                    return;
                }
            } else {
                masts = new Hashtable<>();
                for (int i = 0; i < smlList.size(); i++) {
                    if (smlList.get(i).areBlocksIncluded(allBlock)) {
                        SignalMast mast = smlList.get(i).getSourceMast();
                        String danger = mast.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
                        masts.put(mast, danger);
                    }
                }
            }
            setAutoMasts(masts, overwrite);
        }

        /**
         * Add a certain Signal Mast to the list of AutoSignalMasts for this
         * SML.
         *
         * @param mast The Signal Mast to be added
         */
        void addAutoSignalMast(SignalMast mast) {
            log.debug("{} add mast to auto list {}", destinationSignalMast.getDisplayName(), mast);
            String danger = mast.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
            if (danger == null) {
                log.error("Can not add SignalMast {} to logic for {} to {} as it does not have a Danger appearance configured", mast.getDisplayName(), source.getDisplayName(), destinationSignalMast.getDisplayName());
                return;
            }
            this.autoMasts.put(mast, danger);
            if (destMastInit) {
                mast.addPropertyChangeListener(propertySignalMastListener);
            }
            firePropertyChange("automasts", null, this.destinationSignalMast);
        }

        /**
         * Remove a certain Signal Mast from the list of AutoSignalMasts for
         * this SML.
         *
         * @param mast The Signal Mast to be removed
         */
        void removeAutoSignalMast(SignalMast mast) {
            this.autoMasts.remove(mast);
            if (destMastInit) {
                mast.removePropertyChangeListener(propertySignalMastListener);
            }
            firePropertyChange("automasts", this.destinationSignalMast, null);
        }

        boolean useLayoutEditor() {
            return useLayoutEditor;
        }

        boolean useLayoutEditorBlocks() {
            return useLayoutEditorBlocks;
        }

        boolean useLayoutEditorTurnouts() {
            return useLayoutEditorTurnouts;
        }

        boolean allowAutoSignalMastGeneration = false;

        boolean allowAutoSignalMastGen() {
            return allowAutoSignalMastGeneration;
        }

        void allowAutoSignalMastGen(boolean gen) {
            if (allowAutoSignalMastGeneration == gen) {
                return;
            }
            allowAutoSignalMastGeneration = gen;
        }

        /**
         * Remove references from this Destination Mast and clear lists, so that
         * it can eventually be garbage-collected.
         * <p>
         * Note: This does not stop any delayed operations that might be queued.
         */
        void dispose() {
            disposed = true;
            clearTurnoutLock();
            destinationSignalMast.removePropertyChangeListener(propertyDestinationMastListener);
            setBlocks(null);
            setAutoBlocks(null);
            setTurnouts(null);
            setAutoTurnouts(null);
            setSensors(null);
            setMasts(null);
            setAutoMasts(null, true);
        }

        void lockTurnouts() {
            // We do not allow the turnouts to be locked if we are disposing the logic,
            // if the logic is not active, or if we do not allow the turnouts to be locked.
            if ((disposed) || (!lockTurnouts) || (!active)) {
                return;
            }

            userSetTurnouts.stream().map(nbh -> (Turnout) nbh.getBean()).forEachOrdered(key -> {
                key.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
            });
            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                key.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
            }
        }

        void clearTurnoutLock() {
            // We do not allow the turnout lock to be cleared if we are not active,
            // and the lock flag has not been set.
            if ((!lockTurnouts) && (!active)) {
                return;
            }

            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                key.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
            }

            userSetTurnouts.stream().map(nbh -> (Turnout) nbh.getBean()).forEachOrdered(key -> {
                key.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
            });
        }

        protected void calculateSpeed() {
            log.debug("{} calculate the speed setting for this logic ie what the signalmast will display", destinationSignalMast.getDisplayName());
            minimumBlockSpeed = 0.0f;
            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                log.debug("{} turnout {}", destinationSignalMast.getDisplayName(), key.getDisplayName());
                if (!isTurnoutIncluded(key)) {
                    if (autoTurnouts.get(key) == Turnout.CLOSED) {
                        if (((key.getStraightLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getStraightLimit() != -1)) {
                            minimumBlockSpeed = key.getStraightLimit();
                            log.debug("{} turnout {} set speed to {}", destinationSignalMast.getDisplayName(), key.getDisplayName(), minimumBlockSpeed);
                        }
                    } else {
                        if (((key.getDivergingLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getDivergingLimit() != -1)) {
                            minimumBlockSpeed = key.getDivergingLimit();
                            log.debug("{} turnout {} set speed to {}", destinationSignalMast.getDisplayName(), key.getDisplayName(), minimumBlockSpeed);
                        }
                    }
                }
            }

            userSetTurnouts.forEach(nbh -> {
                Turnout key = (Turnout) nbh.getBean();
                if (nbh.getSetting() == Turnout.CLOSED) {
                    if (((key.getStraightLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getStraightLimit() != -1)) {
                        minimumBlockSpeed = key.getStraightLimit();
                        log.debug("{} turnout {} set speed to {}", destinationSignalMast.getDisplayName(), key.getDisplayName(), minimumBlockSpeed);
                    }
                } else if (nbh.getSetting() == Turnout.THROWN) {
                    if (((key.getDivergingLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getDivergingLimit() != -1)) {
                        minimumBlockSpeed = key.getDivergingLimit();
                        log.debug("{} turnout {} set speed to {}", destinationSignalMast.getDisplayName(), key.getDisplayName(), minimumBlockSpeed);
                    }
                }
            });

            Set<Block> autoBlockKeys = autoBlocks.keySet();
            for (Block key : autoBlockKeys) {
                log.debug("{} auto block add list {}", destinationSignalMast.getDisplayName(), key.getDisplayName());
                if (!isBlockIncluded(key)) {
                    if (((key.getSpeedLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getSpeedLimit() != -1)) {
                        minimumBlockSpeed = key.getSpeedLimit();
                        log.debug("{} block {} set speed to {}", destinationSignalMast.getDisplayName(), key.getDisplayName(), minimumBlockSpeed);
                    }
                }
            }
            for (NamedBeanSetting nbh : userSetBlocks) {
                Block key = (Block) nbh.getBean();
                if (((key.getSpeedLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getSpeedLimit() != -1)) {
                    log.debug("{} block {} set speed to {}", destinationSignalMast.getDisplayName(), key.getDisplayName(), minimumBlockSpeed);
                    minimumBlockSpeed = key.getSpeedLimit();
                }
            }
            /*if(minimumBlockSpeed==-0.1f)
             minimumBlockSpeed==0.0f;*/
        }

        protected PropertyChangeListener propertySensorListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                Sensor sen = (Sensor) e.getSource();
                log.debug("{} to {} destination sensor {} trigger {}", source.getDisplayName(), destinationSignalMast.getDisplayName(), sen.getDisplayName(), e.getPropertyName());
                if (e.getPropertyName().equals("KnownState")) {
                    int now = ((Integer) e.getNewValue());
                    log.debug("current value {} value we want {}", now, getSensorState(sen));
                    if (isSensorIncluded(sen) && getSensorState(sen) != now) {
                        log.debug("Sensor {} caused the signalmast to be set to danger", sen.getDisplayName());
                        //getSourceMast().setAspect(stopAspect);
                        if (active == true) {
                            active = false;
                            setSignalAppearance();
                        }
                    } else if (getSensorState(sen) == now) {
                        log.debug("{} sensor {} triggers a calculation of change", destinationSignalMast.getDisplayName(), sen.getDisplayName());
                        checkState();
                    }
                }
            }
        };

        protected PropertyChangeListener propertyTurnoutListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                Turnout turn = (Turnout) e.getSource();
                //   log.debug(destination.getDisplayName() + " destination sensor "+ sen.getDisplayName() + "trigger");
                if (e.getPropertyName().equals("KnownState")) {
                    //Need to check this against the manual list vs auto list
                    //The manual list should over-ride the auto list
                    int now = ((Integer) e.getNewValue());
                    if (isTurnoutIncluded(turn)) {
                        if (getTurnoutState(turn) != now) {
                            log.debug("Turnout {} caused the signalmast to be set", turn.getDisplayName());
                            log.debug("From {} to {} Turnout {} caused the signalmast to be set to danger", getSourceMast().getDisplayName(), destinationSignalMast.getDisplayName(), turn.getDisplayName());
                            if (active == true) {
                                active = false;
                                setSignalAppearance();
                            }
                        } else {
                            log.debug("{} turnout {} triggers a calculation of change", destinationSignalMast.getDisplayName(), turn.getDisplayName());
                            checkState();
                        }
                    } else if (autoTurnouts.containsKey(turn)) {
                        if (getAutoTurnoutState(turn) != now) {
                            log.debug("Turnout {} auto caused the signalmast to be set", turn.getDisplayName());
                            log.debug("From {} to {} Auto Turnout {} auto caused the signalmast to be set to danger", getSourceMast().getDisplayName(), destinationSignalMast.getDisplayName(), turn.getDisplayName());
                            if (active == true) {
                                active = false;
                                setSignalAppearance();
                            }
                        } else {
                            log.debug("From {} to {} turnout {} triggers a calculation of change", getSourceMast().getDisplayName(), destinationSignalMast.getDisplayName(), turn.getDisplayName());
                            checkState();
                        }
                    }

                } else if ((e.getPropertyName().equals("TurnoutStraightSpeedChange")) || (e.getPropertyName().equals("TurnoutDivergingSpeedChange"))) {
                    calculateSpeed();
                }
            }
        };

        protected PropertyChangeListener propertyBlockListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                Block block = (Block) e.getSource();
                log.debug("{} destination block {} trigger {} {}", destinationSignalMast.getDisplayName(), block.getDisplayName(), e.getPropertyName(), e.getNewValue());
                if (e.getPropertyName().equals("state") || e.getPropertyName().equals("allocated")) {
                    // TODO: what is this?
                    log.debug("Included in user entered block {}", Boolean.toString(isBlockIncluded(block)));
                    log.debug("Included in AutoGenerated Block {}", Boolean.toString(autoBlocks.containsKey(block)));
                    if (isBlockIncluded(block)) {
                        log.debug("{} in manual block", destinationSignalMast.getDisplayName());
                        log.debug("  state: {}  {}", getBlockState(block), block.getState());
                        checkState();
                    } else if (autoBlocks.containsKey(block)) {
                        log.debug("{} in auto block", destinationSignalMast.getDisplayName());
                        log.debug("  states: {}  {}", getAutoBlockState(block), block.getState());
                        checkState();
                    } else {
                        log.debug("{} Not found", destinationSignalMast.getDisplayName());
                    }
                } else if (e.getPropertyName().equals("BlockSpeedChange")) {
                    calculateSpeed();
                }
            }
        };

        protected PropertyChangeListener propertySignalMastListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {

                SignalMast mast = (SignalMast) e.getSource();
                log.debug("{} signalmast change {} {}", destinationSignalMast.getDisplayName(), mast.getDisplayName(), e.getPropertyName());
                //   log.debug(destination.getDisplayName() + " destination sensor "+ sen.getDisplayName() + "trigger");
                if (e.getPropertyName().equals("Aspect")) {

                    String now = ((String) e.getNewValue());
                    log.debug("{} match property {}", destinationSignalMast.getDisplayName(), now);
                    if (isSignalMastIncluded(mast)) {
                        if (!now.equals(getSignalMastState(mast))) {
                            log.debug("{} in mast list SignalMast {} caused the signalmast to be set", destinationSignalMast.getDisplayName(), mast.getDisplayName());
                            log.debug("SignalMast {} caused the signalmast to be set", mast.getDisplayName());
                            if (active) {
                                active = false;
                                setSignalAppearance();
                            }
                        } else {
                            log.debug("{} in mast list signalmast change", destinationSignalMast.getDisplayName());
                            checkState();
                        }
                    } else if (autoMasts.containsKey(mast)) {
                        if (!now.equals(getAutoSignalMastState(mast))) {
                            log.debug("SignalMast {} caused the signalmast to be set", mast.getDisplayName());
                            log.debug("{} in auto mast list SignalMast {} caused the signalmast to be set", destinationSignalMast.getDisplayName(), mast.getDisplayName());
                            if (active) {
                                active = false;
                                setSignalAppearance();
                            }
                        } else {
                            log.debug("{} in auto mast list signalmast change", destinationSignalMast.getDisplayName());
                            checkState();
                        }
                    }
                }
            }
        };

        private class NamedBeanSetting {

            NamedBeanHandle<?> namedBean;
            int setting = 0;
            String strSetting = null;

            NamedBeanSetting(NamedBeanHandle<?> namedBean, int setting) {
                this.namedBean = namedBean;
                this.setting = setting;
            }

            NamedBeanSetting(NamedBeanHandle<?> namedBean, String setting) {
                this.namedBean = namedBean;
                strSetting = setting;
            }

            NamedBean getBean() {
                return namedBean.getBean();
            }

            NamedBeanHandle<?> getNamedBean() {
                return namedBean;
            }

            int getSetting() {
                return setting;
            }

            String getStringSetting() {
                return strSetting;
            }

            String getBeanName() {
                return namedBean.getName();
            }
        }
    }

    /**
     * The listener on the destination Signal Mast.
     */
    protected PropertyChangeListener propertyDestinationMastListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            SignalMast mast = (SignalMast) e.getSource();
            if (mast == destination) {
                log.debug("destination mast change {}", mast.getDisplayName());
                setSignalAppearance();
            }
        }
    };

    /**
     * The listener on the source Signal Mast.
     */
    protected PropertyChangeListener propertySourceMastListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            SignalMast mast = (SignalMast) e.getSource();
            if ((mast == source) && (e.getPropertyName().equals("Held"))) {
                log.debug("source mast change {} {}", mast.getDisplayName(), e.getPropertyName());
                setSignalAppearance();
            }
        }
    };

    //@todo need to think how we deal with auto generated lists based upon the layout editor.
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            boolean found = false;
            StringBuilder message = new StringBuilder();
            if (nb instanceof SignalMast) {
                if (nb.equals(source)) {
                    message.append("Has SignalMast Logic attached which will be <b>Deleted</b> to <ul>");
                    for (SignalMast sm : getDestinationList()) {
                        message.append("<li>");
                        message.append(sm.getDisplayName());
                        message.append("</li>");
                    }
                    message.append("</ul>");
                    throw new PropertyVetoException(message.toString(), evt);

                } else if (isDestinationValid((SignalMast) nb)) {
                    throw new PropertyVetoException("Is the end point mast for logic attached to signal mast " + source.getDisplayName() + " which will be <b>Deleted</b> ", evt);
                }
                for (SignalMast sm : getDestinationList()) {
                    if (isSignalMastIncluded((SignalMast) nb, sm)) {
                        message.append("<li>");
                        message.append("Used in conflicting logic of ").append(source.getDisplayName())
                            .append(" & ").append(sm.getDisplayName()).append("</li>");
                    }
                }
            }
            if (nb instanceof Turnout) {
                for (SignalMast sm : getDestinationList()) {
                    if (isTurnoutIncluded((Turnout) nb, sm)) {
                        message.append("<li>Is in logic between Signal Masts ").append(source.getDisplayName())
                            .append(" ").append(sm.getDisplayName()).append("</li>");
                        found = true;
                    }
                }
            }
            if (nb instanceof Sensor) {
                for (SignalMast sm : getDestinationList()) {
                    if (isSensorIncluded((Sensor) nb, sm)) {
                        message.append("<li>");
                        message.append("Is in logic between Signal Masts ").append(source.getDisplayName())
                            .append(" ").append(sm.getDisplayName()).append("</li>");
                        found = true;
                    }
                }
            }
            if (found) {
                throw new PropertyVetoException(message.toString(), evt);
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // NOI18N
            if (nb instanceof SignalMast) {
                if (nb.equals(source)) {
                    dispose();
                }
                if (isDestinationValid((SignalMast) nb)) {
                    removeDestination((SignalMast) nb);
                }
                for (SignalMast sm : getDestinationList()) {
                    if (isSignalMastIncluded((SignalMast) nb, sm)) {
                        log.warn("Unhandled condition: signal mast included during DoDelete");
                        // @todo need to deal with this situation
                    }
                }
            }
            if (nb instanceof Turnout) {
                Turnout t = (Turnout) nb;
                getDestinationList().stream().filter(sm -> (isTurnoutIncluded(t, sm))).forEachOrdered(sm -> {
                    removeTurnout(t, sm);
                });
            }
            if (nb instanceof Sensor) {
                Sensor s = (Sensor) nb;
                getDestinationList().stream().filter(sm -> (isSensorIncluded(s, sm))).forEachOrdered(sm -> {
                    removeSensor(s, sm);
                });
            }
        }
    }

    /**
     * Note: This does not stop any delayed operations that might be queued.
     */
    @Override
    public void dispose() {
        disposing = true;
        getSourceMast().removePropertyChangeListener(propertySourceMastListener);
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            destList.get(dm).dispose();
        }
        super.dispose(); // release any prop change listeners
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSignalMastLogic");
    }

    /**
     * No valid integer state, always return a constant.
     *
     * @return Always zero
     */
    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void setState(int i) {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            if (bean.equals(getSourceMast())) {
                report.add(new NamedBeanUsageReport("SMLSourceMast"));  // NOI18N
            }
            getDestinationList().forEach((dest) -> {
                if (bean.equals(dest)) {
                    report.add(new NamedBeanUsageReport("SMLDestinationMast"));  // NOI18N
                }
                getAutoBlocks(dest).forEach((block) -> {
                    if (bean.equals(block)) {
                        report.add(new NamedBeanUsageReport("SMLBlockAuto", dest));  // NOI18N
                    }
                });
                getBlocks(dest).forEach((block) -> {
                    if (bean.equals(block)) {
                        report.add(new NamedBeanUsageReport("SMLBlockUser", dest));  // NOI18N
                    }
                });
                getAutoTurnouts(dest).forEach((turnout) -> {
                    if (bean.equals(turnout)) {
                        report.add(new NamedBeanUsageReport("SMLTurnoutAuto", dest));  // NOI18N
                    }
                });
                getTurnouts(dest).forEach((turnout) -> {
                    if (bean.equals(turnout)) {
                        report.add(new NamedBeanUsageReport("SMLTurnoutUser", dest));  // NOI18N
                    }
                });
                getSensors(dest).forEach((sensor) -> {
                    if (bean.equals(sensor)) {
                        report.add(new NamedBeanUsageReport("SMLSensor", dest));  // NOI18N
                    }
                });
                getAutoMasts(dest).forEach((mast) -> {
                    if (bean.equals(mast)) {
                        report.add(new NamedBeanUsageReport("SMLMastAuto", dest));  // NOI18N
                    }
                });
                getSignalMasts(dest).forEach((mast) -> {
                    if (bean.equals(mast)) {
                        report.add(new NamedBeanUsageReport("SMLMastUser", dest));  // NOI18N
                    }
                });
            });
        }
        return report;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogic.class);

}
