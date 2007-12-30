package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * An icon to display a status of a Memory.<P>
 * <P>
 * The value of the memory can't be changed with this icon.
 *<P>
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 1.12 $
 */

public class MemoryIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public MemoryIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/misc/X-red.gif",
                            "resources/icons/misc/X-red.gif"));
                            
        setDisplayLevel(PanelEditor.LABELS);
        // have to do following explicitly, after the ctor
        resetDefaultIcon();
        
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
        if (map==null) map = new java.util.HashMap();
	}
	
	NamedIcon defaultIcon = null;

    // the associated Memory object
    Memory memory = null;
    
    // the map of icons
    java.util.HashMap map = null;

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
    
    public java.util.HashMap getMap() { return map; }

    // display icons

    public void addKeyAndIcon(NamedIcon icon, String keyValue) {
        if (map == null) setMap(); // initialize if needed
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
        return ((javax.swing.JLabel)this).getMaximumSize().height;  // defer to superclass
    }
    
    private int width = -1;
    /**
     * This may be called during the superclass ctor, so before 
     * construction of this object is complete.  Be careful about that!
     */
    protected int maxWidth() {
        return ((javax.swing.JLabel)this).getMaximumSize().width;  // defer to superclass
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


    public void setSelectable(boolean b) {selectable = b;}
    public boolean isSelectable() { return selectable;}
    boolean selectable = false;
    
    /**
     * Pop-up displays the Memory name, allows you to remove the icon.
     *<P>
     * Rotate is not supported for text-holding memories
     *<p>
     * Because this class can change between icon and text forms, 
     * we recreate the popup object each time.
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        popup = new JPopupMenu();
        popup.add(new JMenuItem(getNameString()));
        
        // add x and y coordinates
		if (getViewCoordinates()) {
			popup.add("x= " + this.getX());
			popup.add("y= " + this.getY());
			popup.add(new AbstractAction("Set x & y") {
                public void actionPerformed(ActionEvent e) {
                	String name = getNameString();
                	displayCoordinateEdit(name);
                }
			});
		}
		
        if (icon) {
            popup.add(new AbstractAction("Rotate") {
                public void actionPerformed(ActionEvent e) {
                    // rotate all the icons, a real PITA
                    java.util.Iterator iterator = map.values().iterator();
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
        
        } else if (text) {
            popup = new JPopupMenu();
            popup.add(makeFontSizeMenu());

            popup.add(makeFontStyleMenu());

            popup.add(makeFontColorMenu());

            addFixedItem(popup);
            addShowTooltipItem(popup);
            
            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        } else if (!text && !icon)
            log.warn("showPopUp when neither text nor icon true");

        if (selectable) {
            popup.add(new JSeparator());
    
            java.util.Iterator iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                String value = ((NamedIcon)map.get(key)).getName();
                popup.add(new AbstractAction(key) {
                    public void actionPerformed(ActionEvent e) {
                        String key = e.getActionCommand();
                        memory.setValue(key);
                    }
                });
            }
        }  // end of selectable

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    void displayState() {
        log.debug("displayState");
    	if (memory == null) {  // use default if not connected yet
    		setIcon(defaultIcon);
    		updateSize();
    		return;
    	}
		Object key = memory.getValue();
		if (key != null) {
		    if (map == null) {
		        // no map, attempt to show object directly
                Object val = memory.getValue();
                if (val instanceof String) {
                    setText((String) memory.getValue());
                    setIcon(null);
                    text = true;
                    icon = false;
    		        updateSize();
                    return;
                } else if (val instanceof javax.swing.ImageIcon) {
                    setIcon((javax.swing.ImageIcon) memory.getValue());
                    setText(null);
                    text = false;
                    icon = true;
    		        updateSize();
                    return;
                } else log.warn("can't display current value of "+memory.getSystemName());
		    } else {
		        // map exists, use it
			    NamedIcon newicon = (NamedIcon) map.get(key.toString());
			    if (newicon!=null) {
                    setText(null);
				    super.setIcon(newicon);
                    text = false;
                    icon = true;
    		        updateSize();
				    return;
			    } else {
			        // no match, use default
		            setIcon(defaultIcon);
                    setText(null);
                    text = false;
                    icon = true;
    		        updateSize();
			    }
		    }
		} else {
		    // If fall through to here, no Memory value, set icon to default.
		    setIcon(defaultIcon);
            setText(null);
            text = false;
            icon = true;
    		updateSize();
        }
    }

    public void updateSize() {
    	height = -1;
    	width = -1;
        super.updateSize();
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
