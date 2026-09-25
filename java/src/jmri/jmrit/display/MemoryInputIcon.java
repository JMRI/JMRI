package jmri.jmrit.display;

import java.awt.event.*;

import javax.annotation.Nonnull;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.NamedBeanHandle;
import jmri.NamedBean.DisplayOptions;
import jmri.util.swing.*;

/**
 * An icon to display and input a Memory value in a TextField.
 * <p>
 * Handles the case of either a String or an Integer in the Memory, preserving
 * what it finds.
 *
 * @author Pete Cressman Copyright (c) 2009
 * @author Bob Jacobsen  Copyright (c) 2026
 * @since 2.7.2
 */
public class MemoryInputIcon extends PositionableJTextField implements java.beans.PropertyChangeListener {
        
    // the associated Memory object
    private NamedBeanHandle<Memory> namedMemory;

    private final java.awt.event.MouseListener _mouseListener = JmriMouseListener.adapt(this);
    private final java.awt.event.MouseMotionListener _mouseMotionListener = JmriMouseMotionListener.adapt(this);

    public MemoryInputIcon(int nCols, Editor editor) {
        super(editor);
        _nCols = nCols;
        setDisplayLevel(Editor.LABELS);

        setLayout(new java.awt.GridBagLayout());
        add(_textBox, new java.awt.GridBagConstraints());
        _textBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                log.trace("Key Listener fired");
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_TAB) {
                    updateMemory();
                }
                // redraw the editor window content, including this field
                // we have to redraw the entire contents because of possible overlaps/underlaps
                getEditor().getTargetPanel().repaint();
            }
        });
        _textBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateMemory();
                getEditor().getTargetPanel().repaint();
            }
        });
        _textBox.setColumns(_nCols);
        _textBox.setBorder(null);  // drop default border so user can configure entire border appearance
        _textBox.addMouseMotionListener(_mouseMotionListener);
        _textBox.addMouseListener(_mouseListener);
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
    public void mouseExited(JmriMouseEvent e) {
        updateMemory();
        super.mouseExited(e);
    }

    @Override
    public void mouseMoved(JmriMouseEvent e) {
        // TODO: understand e.consume();
        updateMemory();
        super.mouseMoved(e);
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
                log.error("Memory '{}' not available, icon won't see changes", pName);
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

    public NamedBeanHandle<Memory> getNamedMemory() {
        return namedMemory;
    }

    public Memory getMemory() {
        if (namedMemory == null) {
            return null;
        }
        return namedMemory.getBean();
    }

    // update icon as state of Memory changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_MemoryInputIcon");
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
            final JSpinner spinner = new JSpinner(_spinModel);

            @Override
            protected void addAdditionalButtons(JPanel p) {
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(2);
                spinner.setMaximumSize(spinner.getPreferredSize());
                spinner.setValue(_textBox.getColumns());
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
        ActionListener addIconAction = a -> editMemory();
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
        // and redraw
        getEditor().getTargetPanel().repaint();
    }

    @Override
    void cleanup() {
        if (namedMemory != null) {
            getMemory().removePropertyChangeListener(this);
        }
        if (_textBox != null) {
            _textBox.removeMouseMotionListener(_mouseMotionListener);
            _textBox.removeMouseListener(_mouseListener);
        }
        namedMemory = null;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemoryInputIcon.class);
}
