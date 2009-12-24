package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
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
 * @version $Revision: 1.14 $
 * @since 2.7.2
 */

public class MemorySpinnerIcon extends PositionableJPanel implements java.beans.PropertyChangeListener {

    public MemorySpinnerIcon() {
        super();
        setDisplayLevel(PanelEditor.LABELS);
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(spinner);
        connect(spinner.getEditor()); // add mouse listeners, etc
        connect(((JSpinner.DefaultEditor)spinner.getEditor()).getTextField()); // cast going to be a problem?
        spinner.getModel().addChangeListener(
            new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    spinnerUpdated();
                }
            }
        );
    }
    int _min = 0;
    int _max = 100;
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0,_min,_max,1));
    
    // the associated Memory object
    //Memory memory = null;
    private NamedBeanHandle<Memory> namedMemory;
    
    /**
     * Attached a named Memory to this display item
      * @param pName Used as a system/user name to lookup the Memory object
     */
     public void setMemory(String pName) {
         if (InstanceManager.memoryManagerInstance()!=null) {
            Memory memory = InstanceManager.memoryManagerInstance().
                 provideMemory(pName);
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
        if (namedMemory != null) {
            getMemory().removePropertyChangeListener(this);
        }
        namedMemory = m;
        //memory = InstanceManager.memoryManagerInstance().provideMemory(m.getName());
        if (namedMemory != null) {
            displayState();
            getMemory().addPropertyChangeListener(this);
            setProperToolTip();
        }
    }

    public Memory getMemory() { return namedMemory.getBean(); }
    
    public NamedBeanHandle <Memory> getNamedMemory() {
        return namedMemory;
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

    public String getNameString() {
        String name = rb.getString("NotConnected");
        if (namedMemory != null) name = namedMemory.getName();
        return name;
    }

    public void setSelectable(boolean b) {selectable = b;}
    public boolean isSelectable() { return selectable;}
    boolean selectable = false;
    
    protected void addToPopup() {
        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
    }

    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder("MemoryEditor");
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
        };
        makeAddIconFrame("EditSpinner", "addMemValueToPanel", 
                                             "SelectMemory", _editor);
        _editor.setPickList(PickListModel.memoryPickModelInstance());
        _editor.complete(addIconAction, null, true, true);
        _editor.setSelection(getMemory());
    }
    void editMemory() {
        setMemory(_editor.getTableSelection().getDisplayName());
        setSize(getPreferredSize().width, getPreferredSize().height);
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
        invalidate();
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    void displayState() {
        log.debug("displayState");
    	if (namedMemory == null) {  // leave alone if not connected yet
    		return;
    	}
        if (getMemory().getValue() == null) return;
        Integer num = null;
        if (getMemory().getValue().getClass() == String.class) {
            try {
                num =new Integer((String)getMemory().getValue());
            } catch (NumberFormatException e) {
                return;
            }
        } else if (getMemory().getValue().getClass() == Integer.class) {
            num = ((Number)getMemory().getValue()).intValue();
        } else if (getMemory().getValue().getClass() == Float.class) {
            num = new Integer(Math.round((Float)getMemory().getValue()));
            log.debug("num= "+num.toString());
        } else {
            //spinner.setValue(getMemory().getValue());
            return;
        }
        int n = num.intValue();
        if (n>_max) {
            num =new Integer(_max);
        }
        else if (n<_min) {
            num =new Integer(_min);
        }
        spinner.setValue(num);
    }

    protected void spinnerUpdated() {
        if (namedMemory == null) return;
        if (getMemory().getValue() == null) {
            getMemory().setValue(spinner.getValue());
            return;
        }
        // Spinner is always an Integer, but memory can contain Integer or String
        if (getMemory().getValue().getClass() == String.class) {
            String newValue = ""+spinner.getValue();
            if (! getMemory().getValue().equals(newValue))
                getMemory().setValue(newValue);
        } else {
            getMemory().setValue(spinner.getValue());
        }
    }

    public String getValue() {
        return ""+spinner.getValue();
    }
    
    public void mouseExited(MouseEvent e) {
        spinnerUpdated();
    }
    
    public void dispose() {
        getMemory().removePropertyChangeListener(this);
        namedMemory = null;
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemorySpinnerIcon.class.getName());
}
