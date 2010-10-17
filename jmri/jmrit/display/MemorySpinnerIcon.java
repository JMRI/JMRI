package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.util.NamedBeanHandle;

/**
 * An icon to display a status of a Memory in a JSpinner.
 *<P>
 * Handles the case of either a String or an Integer in the 
 * Memory, preserving what it finds.
 *<P>
 * @author Bob Jacobsen  Copyright (c) 2009
 * @version $Revision: 1.22 $
 * @since 2.7.2
 */

public class MemorySpinnerIcon extends PositionableJPanel implements ChangeListener, PropertyChangeListener {

    int _min = 0;
    int _max = 100;
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0,_min,_max,1));
    // the associated Memory object
    Memory memory = null;    
    private NamedBeanHandle<Memory> namedMemory;
    
    public MemorySpinnerIcon(Editor editor) {
        super(editor);
        setDisplayLevel(Editor.LABELS);
        
        setLayout(new java.awt.GridBagLayout());
        add(spinner, new java.awt.GridBagConstraints());
        spinner.addChangeListener(this);
        javax.swing.JTextField textBox = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
        textBox.addMouseMotionListener(this);
        textBox.addMouseListener(this);
        setPopupUtility(new PositionablePopupUtil(this, textBox));
    }

    public Positionable clone() {
        MemorySpinnerIcon pos = new MemorySpinnerIcon(_editor);
        pos.setMemory(getNameString());
        finishClone(pos);
        return pos;
    }

    public Dimension getSize() {
        if (debug) {
            Dimension d= spinner.getSize();
            if (debug) log.debug("spinner width= "+d.width+", height= "+d.height);
            java.awt.Rectangle rect= getBounds(null);
            if (debug) log.debug("Bounds rect= ("+rect.x+","+rect.y+
                                  ") width= "+rect.width+", height= "+rect.height);
            d= super.getSize();
            if (debug) log.debug("Panel width= "+d.width+", height= "+d.height);
        }
        return super.getSize();
    }
    
    /**
     * Attached a named Memory to this display item
      * @param pName Used as a system/user name to lookup the Memory object
     */
     public void setMemory(String pName) {
         if (debug) log.debug("setMemory for memory= "+pName);
         if (InstanceManager.memoryManagerInstance()!=null) {
            Memory memory = InstanceManager.memoryManagerInstance().provideMemory(pName);
             if (memory != null) {
                 setMemory(new NamedBeanHandle<Memory>(pName, memory));
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
    public void setMemory(NamedBeanHandle<Memory> m) {
        if (memory != null) {
            memory.removePropertyChangeListener(this);
        }
        memory = InstanceManager.memoryManagerInstance().provideMemory(m.getName());
        if (memory != null) {
            memory.addPropertyChangeListener(this);
            displayState();
            namedMemory = m;
        }
    }

    public NamedBeanHandle<Memory> getMemory() { return namedMemory; }
    
    // update icon as state of Memory changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
                displayState();
        }
    }

    public void stateChanged(ChangeEvent e) {
        spinnerUpdated();
    }

    public String getNameString() {
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
    
    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("Memory"));
        popup.add(new AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
    }

    protected void edit() {
        makeIconEditorFrame(this, "Memory", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.memoryPickModelInstance());
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
        };
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(memory);
    }
    void editMemory() {
        setMemory(_iconEditor.getTableSelection().getDisplayName());
        setSize(getPreferredSize().width, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    public void displayState() {
        if (debug) log.debug("displayState");
    	if (namedMemory == null) {  // leave alone if not connected yet
    		return;
    	}
        if (memory.getValue() == null) return;
        Integer num = null;
        if (memory.getValue().getClass() == String.class) {
            try {
                num =Integer.valueOf((String)memory.getValue());
            } catch (NumberFormatException e) {
                return;
            }
        } else if (memory.getValue().getClass() == Integer.class) {
            num = ((Number)memory.getValue()).intValue();
        } else if (memory.getValue().getClass() == Float.class) {
            num = Integer.valueOf(Math.round((Float)memory.getValue()));
            if (debug) log.debug("num= "+num.toString());
        } else {
            //spinner.setValue(getMemory().getValue());
            return;
        }
        int n = num.intValue();
        if (n>_max) {
            num =Integer.valueOf(_max);
        }
        else if (n<_min) {
            num =Integer.valueOf(_min);
        }
        spinner.setValue(num);
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
        spinnerUpdated();
        super.mouseExited(e);
    }

    protected void spinnerUpdated() {
        if (namedMemory == null) return;
        if (memory.getValue() == null) {
            memory.setValue(spinner.getValue());
            return;
        }
        // Spinner is always an Integer, but memory can contain Integer or String
        if (memory.getValue().getClass() == String.class) {
            String newValue = ""+spinner.getValue();
            if (!memory.getValue().equals(newValue))
                memory.setValue(newValue);
        } else {
            memory.setValue(spinner.getValue());
        }
    }

    public String getValue() {
        return ""+spinner.getValue();
    }
    
    void cleanup() {
        if (memory!=null) {
            memory.removePropertyChangeListener(this);
        }
        spinner.removeChangeListener(this);
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().removeMouseMotionListener(this);
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().removeMouseListener(this);
        spinner = null;
        memory = null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemorySpinnerIcon.class.getName());
}
