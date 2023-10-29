package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import jmri.InstanceManager;
import jmri.jmrit.logixng.GlobalVariable;
import jmri.jmrit.logixng.GlobalVariableManager;
import jmri.NamedBeanHandle;
import jmri.NamedBean.DisplayOptions;
import jmri.util.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display and input a GlobalVariable value in a TextField.
 * <p>
 * Handles the case of either a String or an Integer in the GlobalVariable, preserving
 * what it finds.
 *
 * @author Pete Cressman    Copyright (c) 2012
 * @author Daniel Bergqvist Copyright (C) 2022
 * @since 2.7.2
 */
public class GlobalVariableComboIcon extends MemoryOrGVComboIcon
        implements java.beans.PropertyChangeListener, ActionListener {

    private final JComboBox<String> _comboBox;
    private final ComboModel _model;

    // the associated GlobalVariable object
    private NamedBeanHandle<GlobalVariable> namedGlobalVariable;

    private final java.awt.event.MouseListener _mouseListener = JmriMouseListener.adapt(this);
    private final java.awt.event.MouseMotionListener _mouseMotionListener = JmriMouseMotionListener.adapt(this);

    public GlobalVariableComboIcon(Editor editor, String[] list) {
        super(editor);
        if (list != null) {
            _model = new ComboModel(list);
        } else {
            _model = new ComboModel();
        }
        _comboBox = new JComboBox<>(_model);
        _comboBox.addActionListener(this);
        setDisplayLevel(Editor.LABELS);

        setLayout(new java.awt.GridBagLayout());
        add(_comboBox);
        _comboBox.addMouseListener(JmriMouseListener.adapt(this));

        for (int i = 0; i < _comboBox.getComponentCount(); i++) {
            java.awt.Component component = _comboBox.getComponent(i);
            if (component instanceof AbstractButton) {
                component.addMouseListener(_mouseListener);
                component.addMouseMotionListener(_mouseMotionListener);
            }
        }
        setPopupUtility(new PositionablePopupUtil(this, _comboBox));
    }

    @Override
    public JComboBox<String> getTextComponent() {
        return _comboBox;
    }

    @Override
    public Positionable deepClone() {
        String[] list = new String[_model.getSize()];
        for (int i = 0; i < _model.getSize(); i++) {
            list[i] = _model.getElementAt(i);
        }
        GlobalVariableComboIcon pos = new GlobalVariableComboIcon(_editor, list);
        return finishClone(pos);
    }

    protected Positionable finishClone(GlobalVariableComboIcon pos) {
        pos.setGlobalVariable(namedGlobalVariable.getName());
        return super.finishClone(pos);
    }

    /**
     * Attach a named GlobalVariable to this display item.
     *
     * @param pName used as a system/user name to look up the GlobalVariable object
     */
    public void setGlobalVariable(String pName) {
        log.debug("setGlobalVariable for memory= {}", pName);
        if (InstanceManager.getNullableDefault(GlobalVariableManager.class) != null) {
            try {
                GlobalVariable globalVariable = InstanceManager.getDefault(GlobalVariableManager.class).getGlobalVariable(pName);
                setGlobalVariable(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, globalVariable));
            } catch (IllegalArgumentException e) {
                log.error("No GlobalVariableManager for this protocol, icon won't see changes");
            }
        }
        updateSize();
    }

    /**
     * Attach a named GlobalVariable to this display item.
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

    @Override
    public ComboModel getComboModel() {
        return _model;
    }

    /**
     * Display
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
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
        return Bundle.getMessage("PositionableType_GlobalVariableComboIcon");
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
    protected void update() {
        if (namedGlobalVariable == null) {
            return;
        }
        getGlobalVariable().setValue(_comboBox.getSelectedItem());
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
    private DefaultListModel<String> _listModel;

    @Override
    protected void edit() {
        _iconEditor = new IconAdder("GlobalVariable") {
            JList<String> list;
            final JButton bDel = new JButton(Bundle.getMessage("deleteSelection"));
            final JButton bAdd = new JButton(Bundle.getMessage("addItem"));
            final JTextField textfield = new JTextField(30);
            int idx;

            @Override
            protected void addAdditionalButtons(JPanel p) {
                _listModel = new DefaultListModel<>();
                bDel.addActionListener(a -> {
                    if ( list == null ){ return; }
                    idx = list.getSelectedIndex();
                    if (idx >= 0) {
                        _listModel.removeElementAt(idx);
                    }
                });
                bAdd.addActionListener(a -> {
                    String text = textfield.getText();
                    if (text == null || list == null || text.length() == 0 || _listModel.indexOf(text) >= 0) {
                        return;
                    }
                    idx = list.getSelectedIndex();
                    if (idx < 0) {
                        idx = _listModel.getSize();
                    }
                    _listModel.add(idx, text);
                });
                for (int i = 0; i < _model.getSize(); i++) {
                    _listModel.add(i, _model.getElementAt(i));
                }
                list = new JList<>(_listModel);
                JScrollPane scrollPane = new JScrollPane(list);
                JPanel p1 = new JPanel();
                p1.add(new JLabel(Bundle.getMessage("comboList")));
                p.add(p1);
                p.add(scrollPane);
                p1 = new JPanel();
                p1.add(new JLabel(Bundle.getMessage("newItem"), SwingConstants.RIGHT));
                textfield.setMaximumSize(textfield.getPreferredSize());
                p1.add(textfield);
                p.add(p1);
                JPanel p2 = new JPanel();
                //p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
                //p2.setLayout(new FlowLayout(FlowLayout.TRAILING));
                p2.add(bDel);
                p2.add(bAdd);
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
        jmri.NamedBean bean = _iconEditor.getTableSelection();
        setGlobalVariable(bean.getDisplayName());
        _model.removeAllElements();
        for (int i = 0; i < _listModel.size(); i++) {
            _model.addElement(_listModel.getElementAt(i));
        }
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
        _model.setSelectedItem(getGlobalVariable().getValue());
    }

    @Override
    public void mouseExited(JmriMouseEvent e) {
        _comboBox.setFocusable(false);
        _comboBox.transferFocus();
        super.mouseExited(e);
    }

    @Override
    void cleanup() {
        if (namedGlobalVariable != null) {
            getGlobalVariable().removePropertyChangeListener(this);
        }
        if (_comboBox != null) {
            for (int i = 0; i < _comboBox.getComponentCount(); i++) {
                java.awt.Component component = _comboBox.getComponent(i);
                if (component instanceof AbstractButton) {
                    component.removeMouseListener(_mouseListener);
                    component.removeMouseMotionListener(_mouseMotionListener);
                }
            }
            _comboBox.removeMouseListener(_mouseListener);
        }
        namedGlobalVariable = null;
    }

    private final static Logger log = LoggerFactory.getLogger(GlobalVariableComboIcon.class);

}
