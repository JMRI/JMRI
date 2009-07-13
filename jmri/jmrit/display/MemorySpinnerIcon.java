package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;

import java.util.ResourceBundle;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * An icon to display a status of a Memory in a JSpinner.
 *<P>
 * Handles the case of either a String or an Integer in the 
 * Memory, preserving what it finds.
 *<P>
 * @author Bob Jacobsen  Copyright (c) 2009
 * @version $Revision: 1.1 $
 * @since 2.7.2
 */

public class MemorySpinnerIcon extends PositionableJPanel implements java.beans.PropertyChangeListener {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public MemorySpinnerIcon() {
        super();
        setDisplayLevel(PanelEditor.LABELS);
            
        add(spinner);
        spinner.getModel().addChangeListener(
            new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    spinnerUpdated();
                }
            }
        );
    }

    JSpinner spinner = new JSpinner(new SpinnerNumberModel());
    
    // the associated Memory object
    Memory memory = null;
    
    /**
     * Attached a named Memory to this display item
      * @param pName Used as a system/user name to lookup the Memory object
     */
     public void setMemory(String pName) {
         if (InstanceManager.memoryManagerInstance()!=null) {
             memory = InstanceManager.memoryManagerInstance().
                 provideMemory(pName);
             if (memory != null) {
                 setMemory(memory);
             } else {
                 log.error("Memory '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No MemoryManager for this protocol, icon won't see changes");
         }
     }

    /**
     * Attached a named Memory to this display item
     * @param m The Memory object
     */
    public void setMemory(Memory m) {
        memory = m;
        if (memory != null) {
            displayState();
            memory.addPropertyChangeListener(this);
            setProperToolTip();
        }
    }

    public Memory getMemory() { return memory; }
    
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
        if (memory == null) name = rb.getString("NotConnected");
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
        		
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    void displayState() {
        log.debug("displayState");
    	if (memory == null) {  // leave alone if not connected yet
    		return;
    	}
        if (memory.getValue() == null) return;

        // Spinner is always an Integer, but memory can contain Integer or String
        if (memory.getValue().getClass() == String.class) {
            try {
                spinner.setValue(new Integer((String)memory.getValue()));
            } catch (NumberFormatException e) {
                spinner.setValue(new Integer(-1));
            }
        } else {
            spinner.setValue(memory.getValue());
        }
        updateSize();
    }

    protected void spinnerUpdated() {
        if (memory == null) return;
        if (memory.getValue() == null) return;
        // Spinner is always an Integer, but memory can contain Integer or String
        if (memory.getValue().getClass() == String.class) {
            memory.setValue(""+spinner.getValue());
        } else {
            memory.setValue(spinner.getValue());
        }
        updateSize();
    }
    
    int height = 4;
    int width = 4;
    protected void updateSize() {
        height = Math.min(height, getPreferredSize().height);
        width = Math.min(width, getPreferredSize().width);
        setSize(width, height);
    }
    
    public void dispose() {
        memory.removePropertyChangeListener(this);
        memory = null;
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemorySpinnerIcon.class.getName());
}
