package jmri.jmrit.display;

import java.awt.Container;
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
import jmri.Light;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IconAdder;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableJPanel;
import jmri.jmrit.display.PositionablePopupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display and input a Bean name in a TextField.
 * <P>
 * Handles the case of either a String or an Integer in the Bean, preserving
 * what it finds. Adapted from {@link jmri.jmrit.MemoryComboIcon}
 * <P>
 * @author Egbert Broerse copyright (C) 2017
 * @since 4.7.3
 */
public class SwitchboardIcon extends PositionableJPanel
        implements java.beans.PropertyChangeListener, ActionListener {

    protected String _type;
    protected JLabel typeLabel;
    protected boolean _icon = false;
    protected boolean _text = false;
    protected boolean _control = false;
    //protected NamedIcon _namedIcon;

    // the associated Bean object
    private NamedBeanHandle namedBean;

    public SwitchboardIcon(Editor editor, String type) {
        super(editor);
        if (type != null) {
            _type = type;
        } else {
            _type = "Turnout";
        }
        setDisplayLevel(Editor.TURNOUTS);

        setLayout(new java.awt.GridBagLayout());
        add(typeLabel);
        addMouseListener(this);

        _control = true;
        setPopupUtility(null);
    }

    @Override
    public JLabel getTextComponent() {
        return new JLabel(_type);
    }

    @Override
    public Positionable deepClone() {
        SwitchboardIcon pos = new SwitchboardIcon(_editor, _type);
        return finishClone(pos);
    }

    protected Positionable finishClone(SwitchboardIcon pos) {
        pos.setBean(namedBean.getName());
        return super.finishClone(pos);
    }

    /**
     * Attach a named bean to this display item.
     *
     * @param pName Used as a system/user name to lookup the bean object
     */
    public void setBean(String pName) {
        log.debug("setBean for = {}", pName);
        if (_type == "Turnout" && InstanceManager.getNullableDefault(jmri.TurnoutManager.class) != null) {
            try {
                Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout(pName);
                setBean(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, t));
            } catch (IllegalArgumentException e) {
                log.error("No TurnoutManager for this protocol, icon won't see changes");
            }
        }
        updateSize();
    }

    /**
     * Attach a named bean to this display item.
     *
     * @param m The bean object
     */
    public void setBean(NamedBeanHandle nbh) {
        if (namedBean != null) {
            getBean().removePropertyChangeListener(this);
        }
        namedBean = nbh;
        if (namedBean != null) {
            getBean().addPropertyChangeListener(this, namedBean.getName(), "Switchboard Icon");
            displayState();
            setName(namedBean.getName());
        }
    }

    @Override
    public NamedBean getNamedBean() {
        return namedBean.getBean();
    }

    public NamedBean getBean() {
        if (namedBean == null) {
            return null;
        }
        return namedBean.getBean();
    }

    public String getType() {
        return _type;
    }

    /**
     * Display
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //updateBean();
    }

    // update icon as state of Bean changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    @Override
    public String getNameString() {
        String name;
        if (namedBean == null) {
            name = Bundle.getMessage("NotConnected");
        } else if (getBean().getUserName() != null) {
            name = getBean().getUserName() + " (" + getBean().getSystemName() + ")";
        } else {
            name = getBean().getSystemName();
        }
        return name;
    }

    private void updateBean() {
        if (namedBean == null) {
            return;
        }
        //getBean().setName(typeLabel.getText()); // set which property?
    }

    @Override
    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanName" + _type));
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
    DefaultListModel<String> _listModel;

    protected void edit() {
        _iconEditor = new IconAdder(_type) {
            JList<String> list;
            JButton bDel = new JButton(Bundle.getMessage("deleteSelection"));
            JButton bAdd = new JButton(Bundle.getMessage("addItem"));
            JTextField textfield = new JTextField(30);
            // does nothing yet
        };

        makeIconEditorFrame(this, _type, true, _iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.turnoutPickModelInstance());
        ActionListener addIconAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                editBean();
            }
        };

        _iconEditor.makeIconPanel(false);
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(getBean());
    }

    /**
     * Utility
     */
    @Override
    protected void makeIconEditorFrame(Container pos, String name, boolean table, IconAdder editor) {
        if (editor != null) {
            _iconEditor = editor;
        } else {
            _iconEditor = new IconAdder(name);
        }
        _iconEditorFrame = _editor.makeAddIconFrame(name, false, table, _iconEditor);
        _iconEditorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                _iconEditorFrame.dispose();
                _iconEditorFrame = null;
            }
        });
        _iconEditorFrame.setLocationRelativeTo(pos);
        _iconEditorFrame.toFront();
        _iconEditorFrame.setVisible(true);
    }

    void editBean() {
        jmri.NamedBean bean = _iconEditor.getTableSelection();
        setBean(bean.getDisplayName());
        setSize(getPreferredSize().width + 1, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        validate();
    }

    /**
     * Drive the current state of the display from the state of the Bean.
     */
    public void displayState() {
        log.debug("displayState");
        if (namedBean == null) {  // leave alone if not connected yet
            return;
        }
        //_model.setSelectedItem(getBean().getDisplayName());
    }

    @Override
    public void mouseExited(MouseEvent e) {
        typeLabel.setFocusable(false);
        typeLabel.transferFocus();
        super.mouseExited(e);
    }

    void cleanup() {
        if (namedBean != null) {
            getBean().removePropertyChangeListener(this);
        }
        if (typeLabel != null) {
//            _comboBox.removeMouseMotionListener(this);
            typeLabel.removeMouseListener(this);
            typeLabel = null;
        }
        namedBean = null;
    }

    private final static Logger log = LoggerFactory.getLogger(SwitchboardIcon.class.getName());
}
