
package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * An icon to display and input a Memory value in a TextField.
 *<P>
 * Handles the case of either a String or an Integer in the 
 * Memory, preserving what it finds.
 *<P>
 * @author Pete Cressman  Copyright (c) 2009
 * @version $Revision: 1.10 $
 * @since 2.7.2
 */

public class MemoryInputIcon extends PositionableJPanel implements java.beans.PropertyChangeListener {

    JTextField  _textBox = new JTextField();
    SpinnerNumberModel _spinCols = new SpinnerNumberModel(0,0,100,1);
    int _nCols; 
    
    // the associated Memory object
    Memory memory = null;
    
    public MemoryInputIcon(int nCols) {
        super();
        _nCols = nCols;
        setDisplayLevel(PanelEditor.LABELS);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(_textBox);
        _textBox.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e){
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_ENTER) {
                        updateMemory();
                    }
                }
            });
        connect(_textBox);
        _textBox.setColumns(_nCols);
    }
    
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
        if (memory != null) {
            memory.removePropertyChangeListener(this);
        }
        memory = m;
        if (memory != null) {
            displayState();
            memory.addPropertyChangeListener(this);
            setProperToolTip();
        }
    }

    public Memory getMemory() { return memory; }
    public int getNumColumns() { return _nCols; }
    
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
    
    protected void addToPopup() {
        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
    }

    private void updateMemory() {
        if (memory == null) return;
        String str = _textBox.getText();
        memory.setValue(str);
    }

    public void mouseExited(MouseEvent e) {
        updateMemory();
    }

    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder("MemoryEditor") {
                JSpinner spinner = new JSpinner(_spinCols);
                protected void addAdditionalButtons(JPanel p) {
                    ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setColumns(2);
                    spinner.setMaximumSize(spinner.getPreferredSize());
                    spinner.setValue(new Integer(_textBox.getColumns()));
                    JPanel p2 = new JPanel();
                    //p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
                    p2.setLayout(new FlowLayout(FlowLayout.TRAILING));
                    p2.add(new JLabel(rb.getString("NumColsLabel")));
                    p2.add(spinner);
                    p.add(p2);
                }
        };

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
        };
        makeAddIconFrame("EditInputBox", "addMemValueToPanel", 
                                             "SelectMemory", _editor);
        _editor.setPickList(PickListModel.memoryPickModelInstance());
        _editor.complete(addIconAction, null, true, true);
        _editor.setSelection(memory);
    }
    void editMemory() {
        setMemory((Memory)_editor.getTableSelection());
        _nCols = _spinCols.getNumber().intValue();
        _textBox.setColumns(_nCols);
        setSize(getPreferredSize().width+1, getPreferredSize().height);
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
        validate();
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
        Object show = memory.getValue();
        if (show!=null)
            _textBox.setText(show.toString());
        else
            _textBox.setText("");            
    }

    public void dispose() {
        memory.removePropertyChangeListener(this);
        memory = null;
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryInputIcon.class.getName());
}
