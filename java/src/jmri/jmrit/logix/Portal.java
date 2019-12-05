package jmri.jmrit.logix;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.implementation.SignalSpeedMap;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Portal is a boundary between two Blocks.
 * <p>
 * A Portal has Lists of the {@link OPath}s that connect through it.
 * The direction of trains passing through the portal is managed from the
 * BlockOrders of the Warrant the train is running under.
 * The Portal fires a PropertyChangeEvent that a
 * {@link jmri.jmrit.display.controlPanelEditor.PortalIcon} can listen
 * for to set direction arrows for a given route.
 *
 * The Portal also supplies speed information from any signals set at its
 * location that the Warrant passes on the Engineer.
 *
 * @author  Pete Cressman Copyright (C) 2009
 */
public class Portal {

    private final ArrayList<OPath> _fromPaths = new ArrayList<>();
    private OBlock _fromBlock;
    private NamedBean _fromSignal;          // may be either SignalHead or SignalMast
    private float _fromSignalOffset;        // adjustment distance for speed change
    private final ArrayList<OPath> _toPaths = new ArrayList<>();
    private OBlock _toBlock;
    private NamedBean _toSignal;            // may be either SignalHead or SignalMast
    private float _toSignalOffset;          // adjustment distance for speed change
    private String _name;
    private int _state = UNKNOWN;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public static final int UNKNOWN = 0x01;
    public static final int ENTER_TO_BLOCK = 0x02;
    public static final int ENTER_FROM_BLOCK = 0x04;

    public Portal(String uName) {
        _name = uName;
    }

    /**
     * Determine which list the Path belongs to and add it to that list.
     *
     * @param path OPath to add
     * @return false if Path does not have a matching block for this Portal
     */
    public boolean addPath(@Nonnull OPath path) {
        Block block = path.getBlock();
        if (block == null) {
            log.error("Path \"{}\" has no block.", path.getName());
            return false;
        }
        if (!this.equals(path.getFromPortal())
                && !this.equals(path.getToPortal())) {
            return false;
        }
        if ((_fromBlock != null) && _fromBlock.equals(block)) {
            return addPath(_fromPaths, path);
        } else if ((_toBlock != null) && _toBlock.equals(block)) {
            return addPath(_toPaths, path);
        }
        // portal is incomplete or path block not in this portal
        return false;
    }

    /**
     * Utility for both path lists.
     * Checks for duplicate name.
     */
    private boolean addPath(@Nonnull List<OPath> list, @Nonnull OPath path) {
        String pName = path.getName();
        for (OPath p : list) {
            if (p.equals(path)) {
                if (pName.equals(p.getName())) {
                    return true;    // OK, everything equal
                } else {
                    log.warn("Path \"{}\" is duplicate of path \"{}\" in Portal \"{}\" from block {}.", path.getName(), p.getName(), _name, path.getBlock().getDisplayName());
                    return false;
                }
            } else if (pName.equals(p.getName())) {
                log.warn("Path \"{}\" is duplicate name for another path in Portal \"{}\" from block {}.", path.getName(), _name, path.getBlock().getDisplayName());
                return false;
            }
        }
        list.add(path);
        return true;
    }

    /**
     * Remove an OPath from this Portal.
     * Checks both the _fromBlock list as the _toBlock list.
     *
     * @param path the OPath to remove
     */
    public void removePath(@Nonnull OPath path) {
        Block block = path.getBlock();
        if (block == null) {
            log.error("Path \"{}\" has no block.", path.getName());
            return;
        }
        log.debug("removePath: {}", toString());
        if (!this.equals(path.getFromPortal())
                && !this.equals(path.getToPortal())) {
            return;
        }
        if (_fromBlock != null && _fromBlock.equals(block)) {
            _fromPaths.remove(path);
        } else if (_toBlock != null && _toBlock.equals(block)) {
            _toPaths.remove(path);
        }
//        pcs.firePropertyChange("RemovePath", block, path); not needed
    }

    /**
     * Set userName of this Portal. Checks if name is available.
     *
     * @param newName name for path
     * @return return error message, null if name change is OK
     */
    public String setName(String newName) {
        if (newName == null || newName.length() == 0) {
            return null;
        }
        String oldName = _name;
        if (newName.equals(oldName)) {
            return null;
        }
        Portal p = jmri.InstanceManager.getDefault(PortalManager.class).getPortal(newName);
        if (p != null) {
            return Bundle.getMessage("DuplicatePortalName", oldName, p.getDescription());
        }
        _name = newName;
        InstanceManager.getDefault(WarrantManager.class).portalNameChange(oldName, newName);
        
        // for some unknown reason, PortalManager firePropertyChange is not read by PortalTableModel
        // so let OBlock do it
        if (_toBlock != null) {
            _toBlock.pseudoPropertyChange("NameChange", oldName, this);
        } else if (_fromBlock != null) {
            _fromBlock.pseudoPropertyChange("NameChange", oldName, this);
        }
        // CircuitBuilder PortalList needs this property change
        pcs.firePropertyChange("NameChange", oldName, newName);
        return null;
    }

    public String getName() {
        return _name;
    }

    /**
     * Set this portal's toBlock. Remove this portal from old toBlock, if any.
     * Add this portal in the new toBlock's list of portals. 
     *
     * @param block to be the new toBlock
     * @param changePaths if true, set block in paths. If false,
     *                    verify that all toPaths are contained in the block.
     * @return false if paths are not in the block
     */
    public boolean setToBlock(OBlock block, boolean changePaths) {
        if (((block != null) && block.equals(_toBlock)) || ((block == null) && (_toBlock == null))) {
            return true;
        }
        if (changePaths) {
            //Switch paths to new block.  User will need to verify connections
            for (OPath opa : _toPaths) {
                opa.setBlock(block);
            }
        } else if (!verify(_toPaths, block)) {
            return false;
        }
        //log.debug("setToBlock: oldBlock= \"{}\" newBlock \"{}\".", getToBlockName(),
        //      (block != null ? block.getDisplayName() : null));
        OBlock oldBlock = _toBlock;
        if (_toBlock != null) {
            _toBlock.removePortal(this);    // may should not
        }
        _toBlock = block;
        if (_toBlock != null) {
            _toBlock.addPortal(this);
        }
        pcs.firePropertyChange("BlockChanged", oldBlock, _toBlock);
        return true;
    }

    public OBlock getToBlock() {
        return _toBlock;
    }

    public String getToBlockName() {
        return (_toBlock != null ? _toBlock.getDisplayName() : null);
    }

    public List<OPath> getToPaths() {
        return _toPaths;
    }

    /**
     * Set this portal's fromBlock. Remove this portal from old fromBlock, if any.
     * Add this portal in the new toBlock's list of portals. 
     *
     * @param block to be the new fromBlock
     * @param changePaths if true, set block in paths. If false,
     *                    verify that all toPaths are contained in the block.
     * @return false if paths are not in the block
     */
    public boolean setFromBlock(OBlock block, boolean changePaths) {
        if ((block != null && block.equals(_fromBlock)) || (block == null && _fromBlock == null)) {
            return true;
        }
        if (changePaths) {
            //Switch paths to new block.  User will need to verify connections
            for (OPath fromPath : _fromPaths) {
                fromPath.setBlock(block);
            }
        } else if (!verify(_fromPaths, block)) {
            return false;
        }
        // log.debug("setFromBlock: oldBlock= \"{}\" newBlock \"{}\".", getFromBlockName(),
        //     (block!=null ? block.getDisplayName() : null));
        OBlock oldBlock = _fromBlock;
        if (_fromBlock != null) {
            _fromBlock.removePortal(this);
        }
        _fromBlock = block;
        if (_fromBlock != null) {
            _fromBlock.addPortal(this);
        }
        pcs.firePropertyChange("BlockChanged", oldBlock, _fromBlock);
        return true;
    }

    public OBlock getFromBlock() {
        return _fromBlock;
    }

    public String getFromBlockName() {
        return (_fromBlock != null ? _fromBlock.getDisplayName() : null);
    }

    public List<OPath> getFromPaths() {
        return _fromPaths;
    }

    /**
     * Set a signal to protect an OBlock. Warrants look ahead for speed changes
     * and change the train speed accordingly.
     * 
     * @param signal either a SignalMast or a SignalHead
     * @param length offset length in millimeters. This is additional
     * entrance space for the block. This distance added to or subtracted from the
     *               calculation of the ramp distance when a warrant must slow the
     *               train in response to the aspect or appearance of the signal.
     * @param protectedBlock OBlock the signal protects
     * @return true if signal is set.
     */
    public boolean setProtectSignal(NamedBean signal, float length, OBlock protectedBlock) {
        if (protectedBlock == null) {
            return false;
        }
        boolean ret = false;
        if ((_fromBlock != null) && _fromBlock.equals(protectedBlock)) {
            _toSignal = signal;
            _toSignalOffset = length;
            ret = true;
        }
        if ((_toBlock != null) && _toBlock.equals(protectedBlock)) {
            _fromSignal = signal;
            _fromSignalOffset = length;
            ret = true;
        }
        if (ret) {
            protectedBlock.pseudoPropertyChange("signalChange", false, true);
            pcs.firePropertyChange("signalChange", false, true);
            log.debug("setProtectSignal: \"{}\" for Block= {} at portal {}",
                    (signal != null?signal.getDisplayName() : "null"), protectedBlock.getDisplayName(), _name);
        }
        return ret;
    }

    /**
     * Get the signal (either a SignalMast or a SignalHead) protecting an OBlock.
     *
     * @param block is the direction of entry, i.e. the protected block
     * @return signal protecting block, if block is protected, otherwise null.
     */
    public NamedBean getSignalProtectingBlock(@Nonnull OBlock block) {
        if (block.equals(_toBlock)) {
            return _fromSignal;
        } else if (block.equals(_fromBlock)) {
            return _toSignal;
        }
        return null;
    }

    /**
     * Get the block protected by a signal.
     *
     * @param signal is the signal, either a SignalMast or a SignalHead
     * @return Protected OBlock, if it is protected, otherwise null.
     */
    public OBlock getProtectedBlock(NamedBean signal) {
        if (signal == null) {
            return null;
        }
        if (signal.equals(_fromSignal)) {
            return _toBlock;
        } else if (signal.equals(_toSignal)) {
            return _fromBlock;
        }
        return null;
    }

    public NamedBean getFromSignal() {
        return _fromSignal;
    }

    public String getFromSignalName() {
        return (_fromSignal != null ? _fromSignal.getDisplayName() : null);
    }

    public float getFromSignalOffset() {
        return _toSignalOffset;
    }

    public NamedBean getToSignal() {
        return _toSignal;
    }

    public String getToSignalName() {
        return (_toSignal != null ? _toSignal.getDisplayName() : null);
    }

    public float getToSignalOffset() {
        return _toSignalOffset;
    }

    public void deleteSignal(@Nonnull NamedBean signal) {
        if (signal.equals(_toSignal)) {
            _toSignal = null;
            _toSignalOffset = 0;
            if (_fromBlock != null) {
                _fromBlock.pseudoPropertyChange("signalChange", false, false);
                pcs.firePropertyChange("signalChange", false, false);
            }
        } else if (signal.equals(_fromSignal)) {
            _fromSignal = null;
            _toSignalOffset = 0;
            if (_toBlock != null) {
                _toBlock.pseudoPropertyChange("signalChange", false, false);
                pcs.firePropertyChange("signalChange", false, false);
            }
        }
    }

    static public NamedBean getSignal(String name) {
        NamedBean signal = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(name);
        if (signal == null) {
            signal = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        }
        return signal;
    }

    /**
     * Get the paths to the portal within the connected Block i.e. the paths in
     * this (the param) block through the Portal.
     *
     * @param block OBlock
     * @return null if portal does not connect to block
     */
    public List<OPath> getPathsWithinBlock(OBlock block) {
        if (block == null) {
            return null;
        }
        if (block.equals(_fromBlock)) {
            return _fromPaths;
        } else if (block.equals(_toBlock)) {
            return _toPaths;
        }
        return null;
    }

    /**
     * Get the block on the other side of the portal from the block
     * block.
     *
     * @param block OBlock
     * @return the opposite block
     */
    public OBlock getOpposingBlock(@Nonnull OBlock block) {
        if (block.equals(_fromBlock)) {
            return _toBlock;
        } else if (block.equals(_toBlock)) {
            return _fromBlock;
        }
        return null;
    }

    /**
     * Get the paths from the portal in the next connected Block i.e. paths in
     * the block on the other side of the portal from this (the param) block.
     *
     * @param block OBlock
     * @return null if portal does not connect to block
     */
    public List<OPath> getPathsFromOpposingBlock(@Nonnull OBlock block) {
        if (block.equals(_fromBlock)) {
            return _toPaths;
        } else if (block.equals(_toBlock)) {
            return _fromPaths;
        }
        return null;
    }

    /**
     * Call is from BlockOrder when setting the path.
     *
     * @param block OBlock
     */
    protected void setEntryState(OBlock block) {
        if (block == null) {
            _state = UNKNOWN;
        } else if (block.equals(_fromBlock)) {
            setState(ENTER_FROM_BLOCK);
        } else if (block.equals(_toBlock)) {
            setState(ENTER_TO_BLOCK);
        }
    }

    public void setState(int s) {
        int old = _state;
        _state = s;
        pcs.firePropertyChange("Direction", old, _state);
    }

    public int getState() {
        return _state;
    }

    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Check signals, if any, for speed into the block. The signal that protects
     * the "to" block is the signal facing the "from" Block, i.e. the "from"
     * signal. (and vice-versa)
     *
     * @param block is the direction of entry, "from" block
     * @return permissible speed, null if no signal
     * @deprecated since 4.17.5 use getPermissibleSpeed(OBlock, boolean)
     */
    @Deprecated
    public String getPermissibleEntranceSpeed(@Nonnull OBlock block) {
        return getPermissibleSpeed(block, true);
    }

    /**
     * Set the distance (plus or minus) in millimeters from the portal gap
     * where the speed change indicated by the signal should be completed.
     *
     * @param block a protected OBlock
     * @param distance length in millimeters
     */
    public void setEntranceSpaceForBlock(@Nonnull OBlock block, float distance) {
        if (block.equals(_toBlock)) {
            if (_fromSignal != null) {
                _fromSignalOffset = distance;
            }
        } else if (block.equals(_fromBlock)) {
            if (_toSignal != null) {
                _toSignalOffset = distance;
            }
        }
    }

    /**
     * Get the distance (plus or minus) in millimeters from the portal gap
     * where the speed change indicated by the signal should be completed.
     *
     * @param block a protected OBlock
     * @return distance
     */
    public float getEntranceSpaceForBlock(@Nonnull OBlock block) {
        if (block.equals(_toBlock)) {
            if (_fromSignal != null) {
                return _fromSignalOffset;
            }
        } else if (block.equals(_fromBlock)) {
            if (_toSignal != null) {
                return _toSignalOffset;
            }
        }
        return 0;
    }

    /**
     * Check signals, if any, for speed out of the block. The signal that
     * protects the "to" block is the signal facing the "from" Block, i.e. the
     * "from" signal. (and vice-versa)
     *
     * @param block is the direction of entry, "from" block
     * @return permissible speed, null if no signal
     * @deprecated since 4.17.5 use getPermissibleSpeed(OBlock, boolean)
     */
    @Deprecated
    public String getPermissibleExitSpeed(@Nonnull OBlock block) {
        return getPermissibleSpeed(block, false);
    }

    /**
     * Check signals, if any, for speed into/out of a given block. The signal that protects
     * the "to" block is the signal facing the "from" Block, i.e. the "from"
     * signal. (and vice-versa)
     *
     * @param block is the direction of entry, "from" block
     * @param entrance true for EntranceSpeed, false for ExitSpeed
     * @return permissible speed, null if no signal
     */
    public String getPermissibleSpeed(@Nonnull OBlock block, boolean entrance) {
        String speed = null;
        String blockName = block.getDisplayName();
        if (block.equals(_toBlock)) {
            if (_fromSignal != null) {
                if (_fromSignal instanceof SignalHead) {
                    speed = getPermissibleSignalSpeed((SignalHead) _fromSignal, entrance);
                } else {
                    speed = getPermissibleSignalSpeed((SignalMast) _fromSignal, entrance);
                }
            }
        } else if (block.equals(_fromBlock)) {
            if (_toSignal != null) {
                if (_toSignal instanceof SignalHead) {
                    speed = getPermissibleSignalSpeed((SignalHead) _toSignal, entrance);
                } else {
                    speed = getPermissibleSignalSpeed((SignalMast) _toSignal, entrance);
                }
            }
        } else {
            log.error("Block \"{}\" is not in Portal \"{}\".", blockName, _name);
        }
        if (log.isDebugEnabled()) {
            if (speed != null) {
                log.debug("Portal \"{}\" has {} speed= {} into \"{}\" from signal.",
                        _name, (entrance ? "ENTRANCE" : "EXIT"), speed, blockName);
            }
        }
        // no signals, proceed at recorded speed
        return speed;
    }

    /**
     * Get entrance or exit speed set on signal head.
     *
     * @param signal signal head to query
     * @param entrance true for EntranceSpeed, false for ExitSpeed
     * @return permissible speed, Restricted if no speed set on signal
     */
    static private @Nonnull String getPermissibleSignalSpeed(@Nonnull SignalHead signal, boolean entrance) {
        int appearance = signal.getAppearance();
        String speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getAppearanceSpeed(signal.getAppearanceName(appearance));
        // on head, speed is the same for entry and exit
        if (speed == null) {
            log.error("SignalHead \"{}\" has no {} speed specified for appearance \"{}\"! - Restricting Movement!",
                    signal.getDisplayName(), (entrance ? "entrance" : "exit"), signal.getAppearanceName(appearance));
            speed = "Restricted";
        }
        log.debug("SignalHead \"{}\" has {} speed notch= {} from appearance \"{}\"",
                signal.getDisplayName(), (entrance ? "entrance" : "exit"), speed, signal.getAppearanceName(appearance));
        return speed;
    }

    /**
     * Get entrance or exit speed set on signal mast.
     *
     * @param signal signal mast to query
     * @param entrance true for EntranceSpeed, false for ExitSpeed
     * @return permissible speed, Restricted if no speed set on signal
     */
    static private @Nonnull String getPermissibleSignalSpeed(@Nonnull SignalMast signal, boolean entrance) {
        String aspect = signal.getAspect();
        String speed;
        if (entrance) {
            speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getAspectSpeed(aspect, signal.getSignalSystem());
        } else {
            speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getAspectExitSpeed(aspect, signal.getSignalSystem());
        }
        if (speed == null) {
            log.error("SignalMast \"{}\" has no {} speed specified for aspect \"{}\"! - Restricting Movement!",
                    signal.getDisplayName(), (entrance ? "entrance" : "exit"), aspect);
            speed = "Restricted";
        }
        log.debug("SignalMast \"{}\" has {} speed notch= {} from aspect \"{}\"",
                signal.getDisplayName(), (entrance ? "entrance" : "exit"), speed, aspect);
        return speed;
    }

    /*
     * block is a potential _toBlock and paths are the current _toPaths
     * or
     * block is a potential _fromBlock and paths are the current _fromPaths
     * Verify that each path has this potential block as its owning block
     */
    static private boolean verify(List<OPath> paths, OBlock block) {
        if (block == null) {
            return (paths.size() == 0);
        }
        String name = block.getSystemName();
        for (OPath path : paths) {
            Block blk = path.getBlock();
            if (blk == null) {
                log.error("Path \"{}\" belongs to null block. Cannot verify set block to \"{}\"",
                        path.getName(), name);
                return false;
            }
            String pathName = blk.getSystemName();
            if (!pathName.equals(name)) {
                log.warn("Path \"{}\" belongs to block \"{}\". Cannot verify set block to \"{}\"",
                        path.getName(), pathName, name);
                return false;
            }
        }
        return true;
    }

    /**
     * Check if path connects to Portal.
     *
     * @param path OPath to test
     * @return true if valid
     */
    public boolean isValidPath(OPath path) {
        String name = path.getName();
        for (OPath toPath : _toPaths) {
            if (toPath.getName().equals(name)) {
                return true;
            }
        }
        for (OPath fromPath : _fromPaths) {
            if (fromPath.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check portal has both blocks and they are different blocks.
     * 
     * @return true if valid
     */
    public boolean isValid() {
        if (_toBlock == null || _fromBlock==null) {
            return false;
        }
        return (!_toBlock.equals(_fromBlock));
    }

    @OverridingMethodsMustInvokeSuper
    public boolean dispose() {
        if (!InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class).okToRemovePortal(this)) {
            return false;
        }
        if (_toBlock != null) {
            _toBlock.removePortal(this);
        } else if (_fromBlock != null) {
            _fromBlock.removePortal(this);
        }
        pcs.firePropertyChange("portalDelete", true, false);
        PropertyChangeListener[] listeners = pcs.getPropertyChangeListeners();
        for (PropertyChangeListener l : listeners) {
            pcs.removePropertyChangeListener(l);
        }
        return true;
    }

    public String getDescription() {
        return Bundle.getMessage("PortalDescription",
                _name, getFromBlockName(), getToBlockName());
    }

    @Override
    @Nonnull
    public String toString() {
        StringBuilder sb = new StringBuilder("Portal \"");
        sb.append(_name);
        sb.append("\" from block \"");
        sb.append(getFromBlockName());
        sb.append("\" to block \"");
        sb.append(getToBlockName());
        sb.append("\"");
        return sb.toString();
    }

    private final static Logger log = LoggerFactory.getLogger(Portal.class);

}
