
package jmri.jmrit.display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.Memory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.NamedBeanHandle;

/**
 * An icon to display and input a Memory value in a TextField.
 *<P>
 * Handles the case of either a String or an Integer in the 
 * Memory, preserving what it finds.
 *<P>
 * @author Pete Cressman  Copyright (c) 2009
 * @version $Revision$
 * @since 2.7.2
 */

public class MemoryInputIcon extends PositionableJPanel implements java.beans.PropertyChangeListener {

    JTextField  _textBox = new JTextField();
    int _nCols; 
    
    // the associated Memory object
    private NamedBeanHandle<Memory> namedMemory;
    
    
    public MemoryInputIcon(int nCols, Editor editor) {
        super(editor);
        _nCols = nCols;
        setDisplayLevel(Editor.LABELS);
        
        setLayout(new java.awt.GridBagLayout());
        add(_textBox, new java.awt.GridBagConstraints());
        _textBox.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e){
                    int key = e.getKeyCode();
                    if (key==KeyEvent.VK_ENTER || key==KeyEvent.VK_TAB) {
                        updateMemory();
                    }
                }
            });
        _textBox.setColumns(_nCols);
        _textBox.addMouseMotionListener(this);
        _textBox.addMouseListener(this);
        setPopupUtility(new PositionablePopupUtil(this, _textBox));
    }

    public Positionable deepClone() {
        MemoryInputIcon pos = new MemoryInputIcon(_nCols, _editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        MemoryInputIcon pos = (MemoryInputIcon)p;
        pos.setMemory(namedMemory.getName());
        return super.finishClone(pos);
    }
    public JComponent getTextComponent() {
    	return _textBox;
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
        updateMemory();
        super.mouseExited(e);
    }

    /**
     * Attached a named Memory to this display item
      * @param pName Used as a system/user name to lookup the Memory object
     */
     public void setMemory(String pName) {
         if (debug) log.debug("setMemory for memory= "+pName);
         if (InstanceManager.memoryManagerInstance()!=null) {
            Memory memory = InstanceManager.memoryManagerInstance().
                 provideMemory(pName);
             if (memory != null) {
                 setMemory(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, memory));
             } else {
                 log.error("Memory '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No MemoryManager for this protocol, icon won't see changes");
         }
         updateSize();
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
        if (namedMemory != null) {
            getMemory().addPropertyChangeListener(this, namedMemory.getName(), "Memory Input Icon");
            displayState();
            setName(namedMemory.getName());
        }
    }

    public void setNumColumns(int nCols) {
        _textBox.setColumns(nCols);
        _nCols = nCols;
    }

    public NamedBeanHandle<Memory> getNamedMemory() { return namedMemory; }
    
    public Memory getMemory() {
        if (namedMemory==null) {
            return null;
        }
        return namedMemory.getBean();
    }
    
    public int getNumColumns() { return _nCols; }
    
    // update icon as state of Memory changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
                displayState();
        }
    }

    public String getNameString() {
        String name;
        if (namedMemory == null) name = Bundle.getMessage("NotConnected");
        else if (getMemory().getUserName()!=null)
            name = getMemory().getUserName()+" ("+getMemory().getSystemName()+")";
        else
            name = getMemory().getSystemName();
        return name;
    }
    
    private void updateMemory() {
        if (namedMemory == null) return;
        String str = _textBox.getText();
        getMemory().setValue(str);
    }

    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Memory"));
        popup.add(new javax.swing.AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
    }

    /**
    * Poppup menu iconEditor's ActionListener
    */
    SpinnerNumberModel _spinModel = new SpinnerNumberModel(3,1,100,1);
    protected void edit() {
        _iconEditor = new IconAdder("Memory") {
                JSpinner spinner = new JSpinner(_spinModel);
                protected void addAdditionalButtons(JPanel p) {
                    ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setColumns(2);
                    spinner.setMaximumSize(spinner.getPreferredSize());
                    spinner.setValue(Integer.valueOf(_textBox.getColumns()));
                    JPanel p2 = new JPanel();
                    //p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
                    //p2.setLayout(new FlowLayout(FlowLayout.TRAILING));
                    p2.add(new JLabel(Bundle.getMessage("NumColsLabel")));
                    p2.add(spinner);
                    p.add(p2);
                    p.setVisible(true);
                }
        };

        makeIconEditorFrame(this, "Memory", true, _iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.memoryPickModelInstance());
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
        };
        _iconEditor.makeIconPanel(false);
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(getMemory());
    }
    void editMemory() {
        setMemory(_iconEditor.getTableSelection().getDisplayName());
        _nCols = _spinModel.getNumber().intValue();
        _textBox.setColumns(_nCols);
        setSize(getPreferredSize().width+1, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        validate();
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
        Object show = getMemory().getValue();
        if (show!=null)
            _textBox.setText(show.toString());
        else
            _textBox.setText("");            
    }

    void cleanup() {
        if (namedMemory!=null) {
            getMemory().removePropertyChangeListener(this);
        }
        if (_textBox!=null) {
            _textBox.removeMouseMotionListener(this);
            _textBox.removeMouseListener(this);
            _textBox = null;
        }
        namedMemory = null;
    }

    static Logger log = LoggerFactory.getLogger(MemoryInputIcon.class.getName());
}
