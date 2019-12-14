package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.swing.Timer;
import jmri.BeanSetting;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends jmri.Path. An OPath is a route that traverses a Block from one
 * boundary to another. The dest parameter of Path is used to reference the
 * Block to which this OPath belongs. (Not a destination Block as might be
 * inferred from the naming in Path.java)
 * <p>
 * An OPath inherits the List of BeanSettings for all the turnouts needed to
 * traverse the Block. It also has references to the Portals (block boundary
 * objects) through which it enters or exits the block. One of these may be
 * null, if the OPath dead ends within the block.
 *
 * @author Pete Cressman Copyright (C) 2009
 */
public class OPath extends jmri.Path {

    private Portal _fromPortal;
    private Portal _toPortal;
    private String _name;
    private Timer _timer;
    private boolean _timerActive = false;
    private TimeTurnout _listener;

    /**
     * Create an OPath object with default directions of NONE, and no setting
     * element.
     *
     * @param owner Block owning the path
     * @param name  name of the path
     */
    public OPath(Block owner, String name) {
        super(owner, 0, 0);
        _name = name;
    }

    /**
     * Create an OPath object with default directions of NONE, and no setting
     * element.
     *
     * @param owner    Block owning the path
     * @param name     name of the path
     * @param entry    Portal where path enters
     * @param exit     Portal where path exits
     * @param settings array of turnout settings of the path
     */
    public OPath(String name, OBlock owner, Portal entry, Portal exit, ArrayList<BeanSetting> settings) {
        super(owner, 0, 0);
        _name = name;
        _fromPortal = entry;
        _toPortal = exit;
        if (settings != null) {
            for (BeanSetting setting : settings) {
                addSetting(setting);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("OPath Ctor: name= {}, block= {}, fromPortal= {}, toPortal= {}",
                    name, owner.getDisplayName(), (_fromPortal == null ? "null" : _fromPortal.getName()),
                            (_toPortal == null ? "null" : _toPortal.getName()));
        }
    }

    protected String getOppositePortalName(String name) {
        if (_fromPortal != null && _fromPortal.getName().equals(name)) {
            if (_toPortal != null) {
                return _toPortal.getName();
            }
        }
        if (_toPortal != null && _toPortal.getName().equals(name)) {
            if (_fromPortal != null) {
                return _fromPortal.getName();
            }
        }
        return null;
    }

    @SuppressFBWarnings(value="BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification="OBlock extends Block")
    public void setName(String name) {
        log.debug("OPath \"{}\" setName to \"{}\"", _name, name);
        if (name == null || name.length() == 0) {
            return;
        }
        String oldName = _name;
        _name = name;
        OBlock block = (OBlock) getBlock();
        block.pseudoPropertyChange("pathName", oldName, _name); // for IndicatorTrack icons
        InstanceManager.getDefault(WarrantManager.class).pathNameChange(block, oldName, _name);
        if (_fromPortal != null) {
            if (_fromPortal.addPath(this)) {
                return;
            }
        }
        if (_toPortal != null) {
            _toPortal.addPath(this);
        }
    }

    public String getName() {
        return _name;
    }

    public void setFromPortal(Portal p) {
        if (p != null) {
            log.debug("OPath \"{}\" setFromPortal= \"{}\"", _name, p.getName());
        }
        _fromPortal = p;
    }

    public Portal getFromPortal() {
        return _fromPortal;
    }

    public void setToPortal(Portal p) {
        if (p != null) {
            log.debug("OPath \"{}\" setToPortal= \"{}\"", _name, p.getName());
        }
        _toPortal = p;
    }

    public Portal getToPortal() {
        return _toPortal;
    }

    /**
     * Set path turnout commanded state and lock state
     *
     * @param delay     following actions in seconds
     * @param set       when true, command turnout to settings, false don't set
     *                  command - just do lock setting
     * @param lockState set when lock==true, lockState unset when lock==false
     * @param lock      If lockState==0 setLocked() is not called. (lockState
     *                  should be 1,2,3)
     */
    public void setTurnouts(int delay, boolean set, int lockState, boolean lock) {
        if (delay > 0) {
            if (!_timerActive) {
                // Create a timer if one does not exist
                if (_timer == null) {
                    _listener = new TimeTurnout();
                    _timer = new Timer(2000, _listener);
                    _timer.setRepeats(false);
                }
                _listener.setList(getSettings());
                _listener.setParams(set, lockState, lock);
                _timer.setInitialDelay(delay * 1000);
                _timer.start();
                _timerActive = true;
            } else {
                log.warn("timer already active for delayed turnout action on path {}", toString());
            }
        } else {
            fireTurnouts(getSettings(), set, lockState, lock);
        }
    }

    private void fireTurnouts(List<BeanSetting> list, boolean set, int lockState, boolean lock) {
        for (BeanSetting bs : list) {
            Turnout t = (Turnout) bs.getBean();
            if (set) {
                t.setCommandedState(bs.getSetting());
            }
            if (lockState > 0) {
                t.setLocked(lockState, lock);
            }
        }
    }

    public void dispose() {
        if (_fromPortal != null) {
            _fromPortal.removePath(this);
        }
        if (_toPortal != null) {
            _toPortal.removePath(this);
        }
    }

    /**
     * Class for defining ActionListener for ACTION_DELAYED_TURNOUT
     */
    class TimeTurnout implements java.awt.event.ActionListener {

        private List<BeanSetting> list;
        private int lockState;
        boolean set;
        boolean lock;

        public TimeTurnout() {
        }

        void setList(List<BeanSetting> l) {
            list = l;
        }

        void setParams(boolean s, int ls, boolean l) {
            set = s;
            lockState = ls;
            lock = l;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
            fireTurnouts(list, set, lockState, lock);
            // Turn Timer OFF
            if (_timer != null) {
                _timer.stop();
                _timerActive = false;
            }
        }
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder("\"");
        sb.append(_name);
        sb.append("\" from portal \"");
        sb.append(_fromPortal==null?"null":_fromPortal.getName());
        sb.append("\" to portal \"");
        sb.append(_toPortal==null?"null":_toPortal.getName());
        sb.append("\"");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OPath \"");
        sb.append(_name);
        sb.append("\" on block \"");
        sb.append(getBlock()==null?"null":getBlock().getDisplayName());
        sb.append("\" from portal \"");
        sb.append(_fromPortal==null?"null":_fromPortal.getName());
        sb.append("\" to portal \"");
        sb.append(_toPortal==null?"null":_toPortal.getName());
        sb.append("\" sets ");
        sb.append(getSettings().size());
        sb.append("\" turnouts.");
        return sb.toString();
    }

    /**
     * {@inheritDoc} Does not allow duplicate settings.
     */
    @Override
    public void addSetting(BeanSetting t) {
        for (BeanSetting bs : getSettings()) {
            if (bs.getBeanName().equals(t.getBeanName())) {
                log.error("TO setting for \"{}\" already set to {}", t.getBeanName(), bs.getSetting());
                return;
            }
        }
        super.addSetting(t);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.getBlock());
        hash = 67 * hash + Objects.hashCode(this._fromPortal);
        hash = 67 * hash + Objects.hashCode(this._toPortal);
        hash = 67 * hash + Objects.hashCode(this.getSettings());
        return hash;
    }

    /**
     * {@inheritDoc}
     *
     * Override to indicate logical equality for use as paths in OBlocks.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OPath path = (OPath) obj;
        if (getBlock() != path.getBlock()) {
            return false;
        }
        Portal fromPort = path.getFromPortal();
        Portal toPort = path.getToPortal();
        int numPortals = 0;
        if (fromPort != null) {
            numPortals++;
        }
        if (toPort != null) {
            numPortals++;
        }
        if (_fromPortal != null) {
            numPortals--;
        }
        if (_toPortal != null) {
            numPortals--;
        }
        if (numPortals != 0) {
            return false;
        }
        if (_fromPortal != null && !_fromPortal.equals(fromPort) && !_fromPortal.equals(toPort)) {
            return false;
        }
        if (_toPortal != null && !_toPortal.equals(toPort) && !_toPortal.equals(fromPort)) {
            return false;
        }
        List<BeanSetting> settings = path.getSettings();
        if (settings.size() != getSettings().size()) {
            return false;
        }
        Iterator<BeanSetting> iter = settings.iterator();
        Iterator<BeanSetting> it = getSettings().iterator();
        while (iter.hasNext()) {
            BeanSetting beanSetting = iter.next();
            while (it.hasNext()) {
                BeanSetting bs = it.next();
                if (!bs.getBeanName().equals(beanSetting.getBeanName())) {
                    return false;
                }
                if (bs.getSetting() != beanSetting.getSetting()) {
                    return false;
                }
            }
        }
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(OPath.class);

}
