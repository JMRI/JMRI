package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * An icon to display a status of a Memory.<P>
 * <P>
 * The value of the memory can't be changed with this icon; you'll
 * have to subclass to do that.
 *<P>
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 1.4 $
 */

public class MemoryIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public MemoryIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/misc/X-red.gif",
                            "resources/icons/misc/X-red.gif"));
                            
        // have to do following explicitly, after the ctor
        resetDefaultIcon();
        
        setMap();
        icon = true;
        text = false;
    }

    private void resetDefaultIcon() {
        defaultIcon = new NamedIcon("resources/icons/misc/X-red.gif",
                            "resources/icons/misc/X-red.gif");
    }
    
	public void setDefaultIcon(NamedIcon n) {
        defaultIcon = n;
        displayState(); // in case changed
	}
	
	public NamedIcon getDefaultIcon() {
	    return defaultIcon;
	}
	
	private void setMap() {
        if (map==null) map = new com.sun.java.util.collections.HashMap();
	}
	
	NamedIcon defaultIcon = null;

    // the associated Memory object
    Memory memory = null;
    
    // the map of icons
    com.sun.java.util.collections.HashMap map = null;

    /**
     * Attached a named Memory to this display item
     * @param pName Used as a system/user name to lookup the Memory object
     */
    public void setMemory(String pName) {
        if (InstanceManager.memoryManagerInstance()!=null) {
            memory = InstanceManager.memoryManagerInstance().
                provideMemory(pName);
            if (memory != null) {
                displayState();
                memory.addPropertyChangeListener(this);
                setProperToolTip();
            } else {
                log.error("Memory '"+pName+"' not available, icon won't see changes");
            }
        } else {
            log.error("No MemoryManager for this protocol, icon won't see changes");
        }
    }

    public Memory getMemory() { return memory; }
    
    public com.sun.java.util.collections.HashMap getMap() { return map; }

    // display icons

    public void addKeyAndIcon(NamedIcon icon, String keyValue) {
    	map.put(keyValue, icon);
    	// drop size cache
    	height = -1;
    	width = -1;
        displayState(); // in case changed
    }

    private int height = -1;
    /**
     * This may be called during the superclass ctor, so before 
     * construction of this object is complete.  Be careful about that!
     */
    protected int maxHeight() {
    	// cached?
    	if (height > 0) return height;
    	// no, loop to update
    	if (map == null) setMap();
    	if (defaultIcon == null) resetDefaultIcon();
    	height = defaultIcon.getIconHeight();
    	com.sun.java.util.collections.Collection collection = map.values();
    	com.sun.java.util.collections.Iterator iterator = collection.iterator();
    	while (iterator.hasNext()) {
    		NamedIcon next = (NamedIcon) iterator.next();
    		if (next == null) log.warn("Unexpected null icon in map");
    		height = Math.max(height, next.getIconHeight());
    	}
    	return height;
    }
    
    private int width = -1;
    /**
     * This may be called during the superclass ctor, so before 
     * construction of this object is complete.  Be careful about that!
     */
    protected int maxWidth() {
    	// cached?
    	if (width > 0) return width;
    	// no, loop to update
    	if (map == null) setMap();
    	if (defaultIcon == null) resetDefaultIcon();
    	width = defaultIcon.getIconWidth();
    	com.sun.java.util.collections.Collection collection = map.values();
    	com.sun.java.util.collections.Iterator iterator = collection.iterator();
    	while (iterator.hasNext()) {
    		NamedIcon next = (NamedIcon) iterator.next();
    		if (next == null) log.warn("Unexpected null icon in map");
    		width = Math.max(width, next.getIconWidth());
    	}
    	return width;
    }

    // update icon as state of Memory changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "
                                            +e.getPropertyName()
                                            +" is now "+e.getNewValue());
	if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
        String name;
        if (memory == null) name = "<Not connected>";
        else if (memory.getUserName()!=null)
            name = memory.getUserName()+" ("+memory.getSystemName()+")";
        else
            name = memory.getSystemName();
        return name;
    }


    /**
     * Pop-up displays the Memory name, allows you to rotate the icons
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        if (popup==null) {
            popup = new JPopupMenu();
            popup.add(new JMenuItem(getNameString()));
            if (icon) popup.add(new AbstractAction("Rotate") {
                    public void actionPerformed(ActionEvent e) {
                    	// rotate all the icons, a real PITA
    					com.sun.java.util.collections.Iterator iterator = map.values().iterator();
    					while (iterator.hasNext()) {
    						NamedIcon next = (NamedIcon) iterator.next();
    						next.setRotation(next.getRotation()+1, ours);
    					}
                        displayState();
                    }
                });

            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        } // end creation of pop-up menu

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    void displayState() {
        log.debug("displayState");
    	if (memory == null) {
    		setIcon(defaultIcon);
    		return;
    	}
        updateSize();
		Object key = memory.getValue();
		if (key != null) {
			NamedIcon icon = (NamedIcon) map.get(key.toString());
			if (icon!=null) {
				super.setIcon(icon);
				return;
			}
		}
		// if fall through to here, set icon to default.
		super.setIcon(defaultIcon);
    }

    /**
     * Clicks are ignored
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
    }

    public void dispose() {
        memory.removePropertyChangeListener(this);
        memory = null;
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MemoryIcon.class.getName());
}
