package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.NamedBeanHandle;
import jmri.NamedBean.DisplayOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display and input a Memory value in a TextField.
 * <p>
 * Handles the case of either a String or an Integer in the Memory, preserving
 * what it finds.
 *
 * @author Pete Cressman Copyright (c) 2009
 * @since 2.7.2
 */
public class MemoryInputIcon extends PositionableJPanel implements java.beans.PropertyChangeListener {

    JTextField _textBox = new JTextField();
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
            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_TAB) {
                    updateMemory();
                }
            }
        });
        _textBox.setColumns(_nCols);
        _textBox.addMouseMotionListener(this);
        _textBox.addMouseListener(this);
        setPopupUtility(new PositionablePopupUtil(this, _textBox));
    }

    @Override
    public Positionable deepClone() {
        MemoryInputIcon pos = new MemoryInputIcon(_nCols, _editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(MemoryInputIcon pos) {
        pos.setMemory(namedMemory.getName());
        return super.finishClone(pos);
    }

    @Override
    public JComponent getTextComponent() {
        return _textBox;
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
        updateMemory();
        super.mouseExited(e);
    }

    /**
     * Attached a named Memory to this display item
     *
     * @param pName Used as a system/user name to lookup the Memory object
     */
    public void setMemory(String pName) {
        log.debug("setMemory for memory= {}", pName);
        if (InstanceManager.getNullableDefault(jmri.MemoryManager.class) != null) {
            try {
                Memory memory = InstanceManager.memoryManagerInstance().
                        provideMemory(pName);
                setMemory(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, memory));
            } catch (IllegalArgumentException e) {
                log.error("Memory '" + pName + "' not available, icon won't see changes");
            }
        } else {
            log.error("No MemoryManager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attached a named Memory to this display item
     *
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

    public NamedBeanHandle<Memory> getNamedMemory() {
        return namedMemory;
    }

    public Memory getMemory() {
        if (namedMemory == null) {
            return null;
        }
        return namedMemory.getBean();
    }

    public int getNumColumns() {
        return _nCols;
    }

    // update icon as state of Memory changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    @Override
    public String getNameString() {
        String name;
        if (namedMemory == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getMemory().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateMemory();
    }

    private void updateMemory() {
        if (namedMemory == null) {
            return;
        }
        String str = _textBox.getText();
        getMemory().setValue(str);
    }

    @Override
    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameMemory"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    /**
     * Popup menu iconEditor's ActionListener
     */
    SpinnerNumberModel _spinModel = new SpinnerNumberModel(3, 1, 100, 1);

    @Override
    protected void edit() {
        _iconEditor = new IconAdder("Memory") {
            JSpinner spinner = new JSpinner(_spinModel);

            @Override
            protected void addAdditionalButtons(JPanel p) {
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(2);
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
            @Override
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
        setSize(getPreferredSize().width + 1, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        validate();
    }

    /**
     * Drive the current state of the display from the state of the Memory.
     */
    public void displayState() {
        log.debug("displayState");
        if (namedMemory == null) {  // leave alone if not connected yet
            return;
        }
        Object show = getMemory().getValue();
        if (show != null) {
            _textBox.setText(show.toString());
        } else {
            _textBox.setText("");
        }
    }

    @Override
    void cleanup() {
        if (namedMemory != null) {
            getMemory().removePropertyChangeListener(this);
        }
        if (_textBox != null) {
            _textBox.removeMouseMotionListener(this);
            _textBox.removeMouseListener(this);
        }
        namedMemory = null;
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryInputIcon.class);
}
