
// PortalIcon.java

package jmri.jmrit.display.controlPanelEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.display.TurnoutIcon;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.Portal;

/**
 * @author PeteCressman Copyright (C) 2011
 * @version $Revision$
 */

public class PortalIcon extends jmri.jmrit.display.PositionableIcon implements java.beans.PropertyChangeListener  {

    public static final String HIDDEN = "hidden";
    public static final String VISIBLE = "block";
    public static final String PATH = "path";
    public static final String TO_ARROW = "toArrow";
    public static final String FROM_ARROW = "fromArrow";

    protected static NamedIcon _toArrowIcon = NamedIcon.getIconByName("resources/icons/track/toArrow.gif");
    protected static NamedIcon _fromArrowIcon = NamedIcon.getIconByName("resources/icons/track/fromArrow.gif");
    protected static NamedIcon _hiddenIcon = NamedIcon.getIconByName("resources/icons/Invisible.gif");

    private Portal _portal;

    public PortalIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        initMap();
    }

    public PortalIcon(Editor editor, Portal portal) {
        this(editor);
        setPortal(portal);
    }

    private void initMap() {
        _iconMap = new java.util.HashMap<String, NamedIcon>();

        setIcon("resources/icons/throttles/RoundRedCircle20.png",VISIBLE);
        setIcon("resources/icons/greenSquare.gif",PATH);
        setIcon("resources/icons/Invisible.gif",HIDDEN);
        setIcon("resources/icons/track/toArrow.gif",TO_ARROW);
        setIcon("resources/icons/track/fromArrow.gif",FROM_ARROW);
        setFamily("Standard");

        setPopupUtility(null);        // no text 
    }
    
    public Positionable deepClone() {
    	PortalIcon pos = new PortalIcon(_editor, _portal);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
    	PortalIcon pos = (PortalIcon)p;
        pos._iconMap = cloneMap(_iconMap, pos);
        return super.finishClone(p);
    }
    
    private void setIcon(String fileName, String key) {
        NamedIcon icon = NamedIcon.getIconByName(fileName);
        if (icon==null) {
            icon = _editor.loadFailed("Portal icon for status \""+key+"\" ", fileName);
            if (icon==null) {
                log.info("Portal icon for status \""+key+"\" removed for url= "+fileName);
                return;
            }        	
        }
        _iconMap.put(key, icon);
        if (log.isDebugEnabled()) log.debug("\""+getName()+"\" put icon key= \""+key+"\" icon= "+fileName);
    }
    
    /**
    * Place icon by its bean state name
    */
    public void setIcon(String name, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("\""+getName()+"\" put icon key= \""+name+"\" icon= "+icon);
        _iconMap.put(name, icon);
    }    

    public Portal getPortal() {
        return _portal;
    }
    public void setPortal(Portal portal) {
    	if (_portal!=null) {
    		if (_portal.equals(portal)) {
    			return;
    		} else {
    			_portal.removePropertyChangeListener(this);
    		}   			
    	} else if (portal==null) {
    		return;
    	}
        _portal = portal;
        _portal.addPropertyChangeListener(this); 		
        setName(_portal.getName());
        setTooltip(new ToolTip(_portal.getDescription(), 0, 0));
   }

    public void setStatus(String status) {
        if (log.isDebugEnabled()) log.debug("\""+getName()+"\" setStatus("+status+") icon= "+_iconMap.get(status));
        setIcon(_iconMap.get(status));
        updateSize();
        repaint();
    }
    
    /* currently Portals do not have an instance manager - !!!todo */
    public jmri.NamedBean getNamedBean(){
        return _portal;
    }
    
    public void displayState(int state) {
    	switch (state) {
    		case 0x02:
    	    	setStatus(TO_ARROW);
    			break;
    		case 0x04:
    	    	setStatus(TO_ARROW);
    			break;
    		case 0x10:
    	    	setStatus(VISIBLE);
    			break;
    		case 0x20:
    	    	setStatus(PATH);
    			break;
    		default:
    	    	setStatus(HIDDEN);
    			break;
    	}    	
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        Object source = e.getSource();
        if (source instanceof Portal && "Direction".equals(e.getPropertyName())) {
        	switch (((Integer)e.getNewValue()).intValue()) {
        	case Portal.UNKNOWN:
        		setStatus(HIDDEN);
        		break;
    		case Portal.ENTER_TO_BLOCK:
    			setStatus(TO_ARROW);
    			break;
    		case Portal.ENTER_FROM_BLOCK:
    			setStatus(FROM_ARROW);
    			break;
    		}
       	
        }
    }
    
    public String getNameString() {
        return _portal.getDescription();
    }

    static Logger log = LoggerFactory.getLogger(PortalIcon.class.getName());
}
