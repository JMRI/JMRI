package jmri.jmrit.logix;

import java.util.List;
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

    private String _fromPortalName;
    private String _toPortalName;
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
    public OPath(String name, OBlock owner, String entry, int fromBlockDirection,
                         String exit, int toBlockDirection) {
        super(owner, toBlockDirection, fromBlockDirection);
        _name = name;
        _fromPortalName = entry;
        _toPortalName = exit;
        if (log.isDebugEnabled()) log.debug("Ctor: name= "+name+", block= "+owner.getDisplayName()+
                  ", fromPortal= "+_fromPortalName+", toPortal= "+_toPortalName);
    }

    public void setBlock(Block block) {
        if (getBlock()==block) { return; }
        if (log.isDebugEnabled()) log.debug("OPath \""+_name+"\" changing blocks from "+
                             (getBlock()!=null ? getBlock().getDisplayName() : null)+
                             " to "+(block!=null ? block.getDisplayName() : null)+".");
        super.setBlock(block);
    }

    public boolean isPortal(String name) {
        if (_fromPortalName != null && _fromPortalName.equals(name)) { return true; }
        if (_toPortalName != null && _toPortalName.equals(name)) { return true; }
        return false;
    }

    public String getOppositePortalName(String name) {
        if (_fromPortalName!=null && _fromPortalName.equals(name)) {
            return _toPortalName;
        }
        if (_toPortalName!=null && _toPortalName.equals(name)) {
            return _fromPortalName;
        }
        return null;
    }

    public boolean validatePortals() {
        if (!portalOK(_fromPortalName)) {
            return false;
        }
        return portalOK(_toPortalName);
    }
    private boolean portalOK(String name) {
        Portal portal = ((OBlock)getBlock()).getPortalByName(name);
        return portal.isValid();
    }

    public void setName(String n) { _name = n; }
    
    public String getName() { return _name; }
    
    public void setFromPortalName(String p) {
        if (p.equals("") ) { p = null; }
        _fromPortalName = p;
    }
    public String getFromPortalName() { return _fromPortalName; }
    
    public void setToPortalName(String p) {
        if (p.equals("") ) { p = null; }
        _toPortalName = p;
    }
    public String getToPortalName() { return _toPortalName; }

    public void setTurnouts(int delay) {
        if(delay>0) {
            if (!_timerActive) {
                // Create a timer if one does not exist
                if (_timer==null) {
                    _listener = new TimeTurnout();
                    _timer = new Timer(2000, _listener);
                    _timer.setRepeats(false);
                }
                _listener.setList(getSettings());
                _timer.setInitialDelay(delay*1000);
                _timer.start();
                _timerActive = true;
            }
            else {
                log.warn("timer already active for delayed turnout action on path "+toString());
            }
        } else { fireTurnouts(getSettings()); }
    }

    void fireTurnouts(List<BeanSetting> list) {
        for (int i=0; i<list.size(); i++)  {
            BeanSetting bs = list.get(i);
            Turnout t = (Turnout)bs.getBean();
            if (t==null) {
                log.error("Invalid turnout on path "+toString());
            } else {
                t.setCommandedState(bs.getSetting());
            }
        }
    }


	/**
	 *	Class for defining ActionListener for ACTION_DELAYED_TURNOUT
	 */
	class TimeTurnout implements java.awt.event.ActionListener 
	{
        private List<BeanSetting> list;
		public TimeTurnout( ) {
		}

        void setList(List<BeanSetting> l) {
			list =  l;
        }
		
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
            fireTurnouts(list);
			// Turn Timer OFF
            if (_timer != null)
            {
                _timer.stop();
                _timerActive = false;
            }
		}
	}

    public String toString() {
        return "Path \""+_name+"\"on block \""+(getBlock()!=null ? getBlock().getDisplayName(): "null")+"\" from portal "+getFromPortalName()+" to portal "+ getToPortalName();
    }
       
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OPath.class.getName());
}
