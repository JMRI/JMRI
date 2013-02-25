
// PortalIcon.java

package jmri.jmrit.display.controlPanelEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;

/**
 * @author PeteCressman Copyright (C) 2011
 * @version $Revision$
 */

public class PortalIcon extends jmri.jmrit.display.PositionableIcon {

    public static final String HIDDEN = "HIDDEN";
    public static final String BLOCK = "BLOCK";
    public static final String PATH = "PATH";

    private Portal _portal;

    public PortalIcon(Editor editor, Portal portal) {
        // super ctor call to make sure this is an icon label
        super(editor);
        _portal = portal;
        initMap();
    }

    // Config file ctor
    public PortalIcon(String blockName, String portalName, Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        OBlock block = jmri.InstanceManager.oBlockManagerInstance().getOBlock(blockName);
        _portal = block.getPortalByName(portalName);
        initMap();
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

        setName(_portal.getName());
        setTooltip(new ToolTip(_portal.getDescription(), 0, 0));
        setPopupUtility(null);        // no text 
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
        if (log.isDebugEnabled()) log.debug("\""+getName()+"\" setIcon \""+name+"\" icon= "+icon);
        _iconMap.put(name, icon);
    }

    public void setStatus(String status) {
        if (log.isDebugEnabled()) log.debug("\""+getName()+"\" setStatus("+status+")");
        setIcon(_iconMap.get(status));
        updateSize();
    }
    
    public String getNameString() {
        return _portal.getDescription();
    }
    
    static Logger log = LoggerFactory.getLogger(PortalIcon.class.getName());
}
