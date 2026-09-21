package jmri.jmrit.display;

import java.awt.event.*;

import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.logixng.GlobalVariable;
import jmri.jmrit.logixng.GlobalVariableManager;
import jmri.util.swing.*;

/**
 * An icon to display and input a GlobalVariable value in a TextField.
 * <p>
 * Handles the case of either a String or an Integer in the GlobalVariable, preserving
 * what it finds.
 *
 * @author Pete Cressman    Copyright (c) 2009
 * @author Daniel Bergqvist Copyright (C) 2022
 * @author Bob Jacobsen  Copyright (c) 2026
 * @since 2.7.2
 */
public class GlobalVariableInputIcon extends PositionableJTextField implements java.beans.PropertyChangeListener {

    // the associated GlobalVariable object
    private NamedBeanHandle<GlobalVariable> namedGlobalVariable;

    private final java.awt.event.MouseListener _mouseListener = JmriMouseListener.adapt(this);
    private final java.awt.event.MouseMotionListener _mouseMotionListener = JmriMouseMotionListener.adapt(this);

    public GlobalVariableInputIcon(int nCols, Editor editor) {
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
                    updateGlobalVariable();
                }
                // redraw the editor window content, including this field
                // we have to redraw the entire contents because of possible overlaps/underlaps
                getEditor().getTargetPanel().repaint();
            }
        });
        _textBox.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                updateGlobalVariable();
                getEditor().getTargetPanel().repaint();
            }
            @Override
            public void focusGained(FocusEvent e) {}
        });
        _textBox.setColumns(_nCols);
        _textBox.setBorder(null);  // drop default border so user can configure entire border appearance
        _textBox.addMouseMotionListener(_mouseMotionListener);
        _textBox.addMouseListener(_mouseListener);
        setPopupUtility(new PositionablePopupUtil(this, _textBox));
    }

    @Override
    public Positionable deepClone() {
        GlobalVariableInputIcon pos = new GlobalVariableInputIcon(_nCols, _editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(GlobalVariableInputIcon pos) {
        pos.setGlobalVariable(namedGlobalVariable.getName());
        return super.finishClone(pos);
    }

    @Override
    public void mouseExited(JmriMouseEvent e) {
        updateGlobalVariable();
        super.mouseExited(e);
    }

    /**
     * Attached a named GlobalVariable to this display item
     *
     * @param pName Used as a system/user name to lookup the GlobalVariable object
     */
    public void setGlobalVariable(String pName) {
        log.debug("setGlobalVariable for memory= {}", pName);
        if (InstanceManager.getNullableDefault(GlobalVariableManager.class) != null) {
            try {
                GlobalVariable globalVariable = InstanceManager.getDefault(GlobalVariableManager.class).getGlobalVariable(pName);
                setGlobalVariable(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, globalVariable));
            } catch (IllegalArgumentException e) {
                log.error("GlobalVariable '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No GlobalVariableManager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attached a named GlobalVariable to this display item
     *
     * @param m The GlobalVariable object
     */
    public void setGlobalVariable(NamedBeanHandle<GlobalVariable> m) {
        if (namedGlobalVariable != null) {
            getGlobalVariable().removePropertyChangeListener(this);
        }
        namedGlobalVariable = m;
        if (namedGlobalVariable != null) {
            getGlobalVariable().addPropertyChangeListener(this, namedGlobalVariable.getName(), "GlobalVariable Input Icon");
            displayState();
            setName(namedGlobalVariable.getName());
        }
    }

    public NamedBeanHandle<GlobalVariable> getNamedGlobalVariable() {
        return namedGlobalVariable;
    }

    public GlobalVariable getGlobalVariable() {
        if (namedGlobalVariable == null) {
            return null;
        }
        return namedGlobalVariable.getBean();
    }

    // update icon as state of GlobalVariable changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_GlobalVariableInputIcon");
    }

    @Override
    public String getNameString() {
        String name;
        if (namedGlobalVariable == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getGlobalVariable().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    @Override
    public void mouseMoved(JmriMouseEvent e) {
        updateGlobalVariable();
    }

    private void updateGlobalVariable() {
        if (namedGlobalVariable == null) {
            return;
        }
        String str = _textBox.getText();
        getGlobalVariable().setValue(str);
    }

    @Override
    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameGlobalVariable"));
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
        _iconEditor = new IconAdder("GlobalVariable") {
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

        makeIconEditorFrame(this, "GlobalVariable", true, _iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.globalVariablePickModelInstance());
        ActionListener addIconAction = a -> editGlobalVariable();
        _iconEditor.makeIconPanel(false);
        _iconEditor.complete(addIconAction, false, false, true);
        _iconEditor.setSelection(getGlobalVariable());
    }

    void editGlobalVariable() {
        setGlobalVariable(_iconEditor.getTableSelection().getDisplayName());
        _nCols = _spinModel.getNumber().intValue();
        _textBox.setColumns(_nCols);
        setSize(getPreferredSize().width + 1, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        validate();
    }

    /**
     * Drive the current state of the display from the state of the GlobalVariable.
     */
    public void displayState() {
        log.debug("displayState");
        if (namedGlobalVariable == null) {  // leave alone if not connected yet
            return;
        }
        Object show = getGlobalVariable().getValue();
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
        if (namedGlobalVariable != null) {
            getGlobalVariable().removePropertyChangeListener(this);
        }
        if (_textBox != null) {
            _textBox.removeMouseMotionListener(_mouseMotionListener);
            _textBox.removeMouseListener(_mouseListener);
        }
        namedGlobalVariable = null;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalVariableInputIcon.class);
}
