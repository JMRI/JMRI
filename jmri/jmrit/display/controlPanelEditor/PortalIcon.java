
// PortalIcon.java

package jmri.jmrit.display.controlPanelEditor;

import java.awt.Point;

import jmri.jmrit.display.ToolTip;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.Portal;
/**
 * @author PeteCressman Copyright (C) 2011
 * @version $Revision: 1.3 $
 */

public class PortalIcon extends jmri.jmrit.display.PositionableIcon {

    public static final String HIDDEN = "HIDDEN";
    public static final String BLOCK = "BLOCK";
    public static final String PATH = "PATH";

    private Portal _portal;

    public PortalIcon(jmri.jmrit.display.Editor editor, Portal portal) {
        // super ctor call to make sure this is an icon label
        super(editor);
        _portal = portal;
        initMap();
        setTooltip(new ToolTip(_portal.toString(), 0, 0));
        setPopupUtility(null);        // no text 
    }

    private void initMap() {
        _iconMap = new java.util.Hashtable<String, NamedIcon>();

        String fileName = "resources/icons/throttles/RoundRedCircle20.png";
        NamedIcon icon = new NamedIcon(fileName, fileName);
        _iconMap.put(BLOCK, icon);
        fileName = "resources/icons/greenSquare.gif";
        icon = new NamedIcon(fileName, fileName);
        _iconMap.put(PATH, icon);
        fileName = "resources/icons/Invisible.gif";
        icon = new NamedIcon(fileName, fileName);
        _iconMap.put(HIDDEN, icon);

    }

    public Portal getPortal() {
        return _portal;
    }
    public void setPortal(Portal portal) {
        _portal = portal;
    }

    /**
    * Place icon by its bean state name
    */
    public void setIcon(String name, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("set \""+name+"\" icon= "+icon);
        _iconMap.put(name, icon);
    }

    public void setStatus(String status) {
        if (log.isDebugEnabled()) log.debug("PortalIcon.setStatus("+status+")");
        setIcon(_iconMap.get(status));
        updateSize();
    }
    
    public String getNameString() {
        return "Portal: "+_portal.getName();
    }
    /********** Positionable overrides ******************/

    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        _portal.setIconPosition(new Point(x,y));
    }

    public void setLocation(Point p) {
        super.setLocation(p);
        _portal.setIconPosition(p);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PortalIcon.class.getName());
}
