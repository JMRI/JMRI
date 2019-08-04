package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
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
 * @author Pete Cressman Copyright (c) 2012
 * @since 2.7.2
 */
public class MemoryComboIcon extends PositionableJPanel
        implements java.beans.PropertyChangeListener, ActionListener {

    private JComboBox<String> _comboBox;
    private ComboModel _model;

    // the associated Memory object
    private NamedBeanHandle<Memory> namedMemory;

    public MemoryComboIcon(Editor editor, String[] list) {
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
        _comboBox.addMouseListener(this);

        for (int i = 0; i < _comboBox.getComponentCount(); i++) {
            java.awt.Component component = _comboBox.getComponent(i);
            if (component instanceof AbstractButton) {
                component.addMouseListener(this);
                component.addMouseMotionListener(this);
            }
        }
        setPopupUtility(new PositionablePopupUtil(this, _comboBox));
    }

    @Override
    public JComboBox<String> getTextComponent() {
        return _comboBox;
    }

    class ComboModel extends DefaultComboBoxModel<String> {

        ComboModel() {
            super();
        }

        ComboModel(String[] l) {
            super(l);
        }

        @Override
        public void addElement(String obj) {
            if (getIndexOf(obj) >= 0) {
                return;
            }
            super.addElement(obj);
            updateMemory();
        }

        @Override
        public void insertElementAt(String obj, int idx) {
            if (getIndexOf(obj) >= 0) {
                return;
            }
            super.insertElementAt(obj, idx);
            updateMemory();
        }
    }

    @Override
    public Positionable deepClone() {
        String[] list = new String[_model.getSize()];
        for (int i = 0; i < _model.getSize(); i++) {
            list[i] = _model.getElementAt(i);
        }
        MemoryComboIcon pos = new MemoryComboIcon(_editor, list);
        return finishClone(pos);
    }

    protected Positionable finishClone(MemoryComboIcon pos) {
        pos.setMemory(namedMemory.getName());
        return super.finishClone(pos);
    }

    /**
     * Attach a named Memory to this display item.
     *
     * @param pName used as a system/user name to look up the Memory object
     */
    public void setMemory(String pName) {
        log.debug("setMemory for memory= {}", pName);
        if (InstanceManager.getNullableDefault(jmri.MemoryManager.class) != null) {
            try {
                Memory memory = InstanceManager.memoryManagerInstance().provideMemory(pName);
                setMemory(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, memory));
            } catch (IllegalArgumentException e) {
                log.error("No MemoryManager for this protocol, icon won't see changes");
            }
        }
        updateSize();
    }

    /**
     * Attach a named Memory to this display item.
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

    public ComboModel getComboModel() {
        return _model;
    }

    /**
     * Display
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        updateMemory();
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

    private void updateMemory() {
        if (namedMemory == null) {
            return;
        }
        getMemory().setValue(_comboBox.getSelectedItem());
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
    private DefaultListModel<String> _listModel;

    @Override
    protected void edit() {
        _iconEditor = new IconAdder("Memory") {
            JList<String> list;
            JButton bDel = new JButton(Bundle.getMessage("deleteSelection"));
            JButton bAdd = new JButton(Bundle.getMessage("addItem"));
            JTextField textfield = new JTextField(30);
            int idx;

            @Override
            protected void addAdditionalButtons(JPanel p) {
                _listModel = new DefaultListModel<>();
                bDel.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent a) {
                        idx = list.getSelectedIndex();
                        if (idx >= 0) {
                            _listModel.removeElementAt(idx);
                        }
                    }
                });
                bAdd.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent a) {
                        String text = textfield.getText();
                        if (text == null || text.length() == 0 || _listModel.indexOf(text) >= 0) {
                            return;
                        }
                        idx = list.getSelectedIndex();
                        if (idx < 0) {
                            idx = _listModel.getSize();
                        }
                        _listModel.add(idx, text);
                    }
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
        jmri.NamedBean bean = _iconEditor.getTableSelection();
        setMemory(bean.getDisplayName());
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
     * Drive the current state of the display from the state of the Memory.
     */
    public void displayState() {
        log.debug("displayState");
        if (namedMemory == null) {  // leave alone if not connected yet
            return;
        }
        _model.setSelectedItem(getMemory().getValue());
    }

    @Override
    public void mouseExited(MouseEvent e) {
        _comboBox.setFocusable(false);
        _comboBox.transferFocus();
        super.mouseExited(e);
    }

    @Override
    void cleanup() {
        if (namedMemory != null) {
            getMemory().removePropertyChangeListener(this);
        }
        if (_comboBox != null) {
            for (int i = 0; i < _comboBox.getComponentCount(); i++) {
                java.awt.Component component = _comboBox.getComponent(i);
                if (component instanceof AbstractButton) {
                    component.removeMouseListener(this);
                    component.removeMouseMotionListener(this);
                }
            }
            _comboBox.removeMouseListener(this);
        }
        namedMemory = null;
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryComboIcon.class);

}
