
// PortalIcon.java

package jmri.jmrit.display.controlPanelEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JPopupMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.display.palette.PortalItemPanel;
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

/*    protected static NamedIcon _toArrowIcon = NamedIcon.getIconByName("resources/icons/track/toArrow.gif");
    protected static NamedIcon _fromArrowIcon = NamedIcon.getIconByName("resources/icons/track/fromArrow.gif");
    protected static NamedIcon _hiddenIcon = NamedIcon.getIconByName("resources/icons/Invisible.gif");
*/
    private Portal _portal;
    private PortalItemPanel _portalPanel;
    private String _status;
    //jmri.util.JmriJFrame _paletteFrame;

    public PortalIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        initMap();
        setPopupUtility(null);        // no text 
    }

    public PortalIcon(Editor editor, Portal portal) {
        this(editor);
        setPortal(portal);
    }

    private void initMap() {
        _iconMap = new java.util.HashMap<String, NamedIcon>();
        ControlPanelEditor ed = (ControlPanelEditor)_editor;
        _iconMap.put(VISIBLE, ed.getPortalIcon(VISIBLE));
        _iconMap.put(PATH, ed.getPortalIcon(PATH));
        _iconMap.put(HIDDEN,ed.getPortalIcon(HIDDEN));
        _iconMap.put(TO_ARROW, ed.getPortalIcon(TO_ARROW));
        _iconMap.put(FROM_ARROW, ed.getPortalIcon(FROM_ARROW));
        setFamily((ed.getPortalIconFamily()));
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
    	        _portal = portal;
    	        _portal.addPropertyChangeListener(this); 		
    		}   			
    	} else if (portal==null) {
    		return;
    	} else {
    		_portal = portal;
    	}
        setName(_portal.getName());
        setTooltip(new ToolTip(_portal.getDescription(), 0, 0));
   }

    public void setStatus(String status) {
        if (log.isDebugEnabled()) log.debug("\""+getName()+"\" setStatus("+status+") icon= "+_iconMap.get(status));
        setIcon(_iconMap.get(status));
        _status = status;
        updateSize();
        repaint();
    }
    
    public String getStatus() {
    	return _status;
    }
    
    /* currently Portals do not have an instance manager - !!!todo? */
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

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("portal"));
        popup.add(new javax.swing.AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    editItem();
                }
            });
        return true;
    }

    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("portal")));
        _portalPanel = new PortalItemPanel(_paletteFrame, "Portal", _iconFamily, _editor);

        ActionListener updateAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // duplicate _iconMap map with unscaled and unrotated icons
        HashMap<String, NamedIcon> map = new HashMap<String, NamedIcon>();
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            NamedIcon oldIcon = entry.getValue();
            NamedIcon newIcon = cloneIcon(oldIcon, this);
            newIcon.rotate(0, this);
            newIcon.scale(1.0, this);
            newIcon.setRotation(4, this);
            map.put(entry.getKey(), newIcon);
        }
        _portalPanel.init(updateAction, map);
        _paletteFrame.add(_portalPanel);
        _paletteFrame.setLocationRelativeTo(this);
        _paletteFrame.toFront();
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
        HashMap<String, NamedIcon> iconMap = _portalPanel.getIconMap();
        if (iconMap!=null) {
        	HashMap<String, NamedIcon> oldMap = cloneMap(_iconMap, this);
            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                if (log.isDebugEnabled()) log.debug("key= "+entry.getKey());
                NamedIcon newIcon = entry.getValue();
                NamedIcon oldIcon = oldMap.get(entry.getKey());
                newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                newIcon.setRotation(oldIcon.getRotation(), this);
                setIcon(entry.getKey(), newIcon);
            }
        }   // otherwise retain current map
//        jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex();
        _paletteFrame.dispose();
        _paletteFrame = null;
        _portalPanel.dispose();
        _portalPanel = null;
        setStatus(_status);
    }
    static Logger log = LoggerFactory.getLogger(PortalIcon.class.getName());
}
