package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.swing.Timer;

import jmri.BeanSetting;
import jmri.Block;
import jmri.Turnout;

/**
 * Extends jmri.Path.
 * An OPath is a route that traverses a Block from one boundary to another.
 * The mBlock parameter of Path is used to reference the Block to which this OPath belongs.
 * (Not a destination Block as might be inferred from the naming in Path.java)
 * <P>
 * An OPath inherits the List of BeanSettings for all the turnouts needed to traverse the Block.
 * It also has references to the Portals (block boundary objects) through wich it enters or
 * exits the block.  One of these may be null, if the OPath  dead ends within the block.
 *
 * @author	Pete Cressman  Copyright (C) 2009
 */
public class OPath extends jmri.Path  {

    private Portal _fromPortal;
    private Portal _toPortal;
    private String _name;
    private Timer _timer;
    private boolean _timerActive = false; 
    private TimeTurnout _listener;

    /**
     * Create an object with default directions of NONE, and
     * no setting element.
     */
    public OPath(Block owner, String name) {
        super(owner, 0, 0);
        _name = name;
    }
    public OPath(Block owner, int toBlockDirection, int fromBlockDirection) {
        super(owner, toBlockDirection, fromBlockDirection);
    }    
    public OPath(Block owner, int toBlockDirection, int fromBlockDirection, BeanSetting setting) {
        super(owner, toBlockDirection, fromBlockDirection, setting);
    }
    public OPath(String name, OBlock owner, Portal entry, Portal exit, ArrayList<BeanSetting> settings) {
        super(owner, 0, 0);
        _name = name;
        _fromPortal = entry;
        _toPortal = exit;
        if (settings!=null) {
            for (int i=0; i<settings.size(); i++) {
                addSetting(settings.get(i));
            }        	
        }
       if (log.isDebugEnabled()) log.debug("Ctor: name= "+name+", block= "+owner.getDisplayName()+
                                ", fromPortal= "+(_fromPortal==null?"null":_fromPortal.getName())+
                                ", toPortal= "+(_toPortal==null?"null":_toPortal.getName()));
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR")
    // OPath ctor invokes Path ctor via super(), which calls this, before the internal
    // _block variable has been set so that Path.getPath() can work.  In this implementation,
    // getPath() only controls whether log.debug(...) is fired, but this might change if/when
    // super.setBlock(...) is changed, in which case this logic will fail.
    public void setBlock(Block block) {
        if (getBlock()==block) { return; }
        if (log.isDebugEnabled()) log.debug("OPath \""+_name+"\" changing blocks from "+
                             (getBlock()!=null ? getBlock().getDisplayName() : null)+
                             " to "+(block!=null ? block.getDisplayName() : null)+".");
        super.setBlock(block);
    }

    protected String getOppositePortalName(String name) {
        if (_fromPortal!=null && _fromPortal.getName().equals(name)) {
            if (_toPortal!=null) {
                return _toPortal.getName();
            }
        }
        if (_toPortal!=null && _toPortal.getName().equals(name)) {
            if (_fromPortal!=null) {
                return _fromPortal.getName();
            }
        }
        return null;
    }

    protected boolean validatePortals() {
        if (!_fromPortal.isValid()) {
            return false;
        }
        return _toPortal.isValid();
    }

    public void setName(String name) { 
        if (log.isDebugEnabled()) log.debug("OPath \""+_name+"\" setName to "+name);
        if (name == null || name.length()==0) { return; }
        String oldName  = _name;
        _name = name;
        OBlock block = (OBlock)getBlock();
        block.pseudoPropertyChange("pathName", oldName, _name);
    	WarrantTableAction.pathNameChange(block, oldName, _name);
        if (_fromPortal!=null) {
        	if (_fromPortal.addPath(this)) {
        		return;
        	}
        }
        if (_toPortal!=null) {
        	_toPortal.addPath(this);
        }
    }
    
    public String getName() { return _name; }
    
    public void setFromPortal(Portal p) {
        if (log.isDebugEnabled() && p!=null) log.debug("OPath \""+_name+"\" setFromPortal= "+p.getName());
        _fromPortal = p;
    }
    public Portal getFromPortal() { return _fromPortal; }
    
    public void setToPortal(Portal p) {
        if (log.isDebugEnabled() && p!=null) log.debug("OPath \""+_name+"\" setToPortal= "+p.getName());
        _toPortal = p;
    }
    public Portal getToPortal() { return _toPortal; }

    /**
    * Set path turnout commanded state and lock state
    * @param delay following actions in seconds
    * @param set when true, command turnout to settings, false don't set command - just do lock setting
    * @param lockState set when lock==true, lockState unset when lock==false
    * @param lock
    * If lockState==0 setLocked() is not called.  (lockState should be 1,2,3)  
    */
    public void setTurnouts(int delay, boolean set, int lockState, boolean lock) {
        if(delay>0) {
            if (!_timerActive) {
                // Create a timer if one does not exist
                if (_timer==null) {
                    _listener = new TimeTurnout();
                    _timer = new Timer(2000, _listener);
                    _timer.setRepeats(false);
                }
                _listener.setList(getSettings());
                _listener.setParams(set, lockState, lock);
                _timer.setInitialDelay(delay*1000);
                _timer.start();
                _timerActive = true;
            }
            else {
                log.warn("timer already active for delayed turnout action on path "+toString());
            }
        } else { fireTurnouts(getSettings(), set, lockState, lock); }
    }

    void fireTurnouts(List<BeanSetting> list, boolean set, int lockState, boolean lock) {
        for (int i=0; i<list.size(); i++)  {
            BeanSetting bs = list.get(i);
            Turnout t = (Turnout)bs.getBean();
            if (t==null) {
                log.error("Invalid turnout on path "+toString());
            } else {
                if (set) {
                    t.setCommandedState(bs.getSetting());
                }
                if (lockState>0) {
                    t.setLocked(lockState, lock);
                }
            }
        }
    }

    public void dispose() {
        if (_fromPortal!=null) { _fromPortal.removePath(this); }
        if (_toPortal!=null) { _toPortal.removePath(this); }
    }


	/**
	 *	Class for defining ActionListener for ACTION_DELAYED_TURNOUT
	 */
	class TimeTurnout implements java.awt.event.ActionListener 
	{
        private List<BeanSetting> list;
        private int lockState;
        boolean set;
        boolean lock;
		public TimeTurnout( ) {
		}

        void setList(List<BeanSetting> l) {
			list =  l;
        }
        void setParams(boolean s, int ls, boolean l) {
            set = s;
            lockState = ls;
            lock = l;
        }
		
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
            fireTurnouts(list, set, lockState, lock);
			// Turn Timer OFF
            if (_timer != null)
            {
                _timer.stop();
                _timerActive = false;
            }
		}
	}

    public String getDescription() {
        return "\""+_name+"\""+(_fromPortal==null?"":" from portal "+_fromPortal.getName())+
                    (_toPortal==null?"":" to portal "+ _toPortal.getName());
    }
    
    public String toString() {
        return "OPath \""+_name+"\"on block "+(getBlock()!=null ? getBlock().getDisplayName(): "null")+
            (_fromPortal==null?"":" from portal "+_fromPortal.getName())+
            (_toPortal==null?"":" to portal "+ _toPortal.getName());
    }

    /**
     * override to disallow duplicate setting
     */
    public void addSetting(BeanSetting t) {
        Iterator<BeanSetting> iter = getSettings().iterator();
        while (iter.hasNext()) {
        	BeanSetting bs = iter.next();
    		if (bs.getBeanName().equals(t.getBeanName())) {
    			log.error("TO setting for \""+ t.getBeanName()+"\" already set to "+bs.getSetting());
    			return;
    		}
        }
		super.addSetting(t);
    }
    
    /**
     * Override to indicate logical equality for use as paths in OBlocks.
     * 
     */
    public boolean equals(OPath path) {
    	if (_fromPortal!=null && !_fromPortal.equals(path.getFromPortal())) {
    		return false;
    	}
    	if (_toPortal!=null && !_toPortal.equals(path.getToPortal())) {
    		return false;
    	}
        Iterator<BeanSetting> iter = path.getSettings().iterator();
        while (iter.hasNext()) {
        	BeanSetting beanSetting = iter.next();
        	Iterator<BeanSetting> it = getSettings().iterator();
        	while (it.hasNext()) {
            	BeanSetting bs = it.next();
        		if (!bs.getBeanName().equals(beanSetting.getBeanName())) {
        			return false;
        		}
        		if (bs.getSetting()!= beanSetting.getSetting()) {
        			return false;
        		}
        	}
        }
    	return true;
    }
       
    static Logger log = LoggerFactory.getLogger(OPath.class.getName());
}
