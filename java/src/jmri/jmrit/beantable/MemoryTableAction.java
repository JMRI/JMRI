package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.Memory;
import jmri.NamedBean;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a MemoryTable GUI.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 */
public class MemoryTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public MemoryTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary Memory manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.MemoryManager.class) == null) {
            setEnabled(false);
        }

    }

    public MemoryTableAction() {
        this(Bundle.getMessage("TitleMemoryTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Memory objects
     */
    protected void createModel() {
        m = new BeanTableDataModel() {

            public String getValue(String name) {
                Memory mem = InstanceManager.memoryManagerInstance().getBySystemName(name);
                if (mem == null) {
                    return "?";
                }
                Object m = mem.getValue();
                if (m != null) {
                    return m.toString();
                } else {
                    return "";
                }
            }

            public Manager getManager() {
                return InstanceManager.memoryManagerInstance();
            }

            public NamedBean getBySystemName(String name) {
                return InstanceManager.memoryManagerInstance().getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return InstanceManager.memoryManagerInstance().getByUserName(name);
            }

            protected String getMasterClassName() {
                return getClassName();
            }

            public void clickOn(NamedBean t) {
                // don't do anything on click; not used in this class, because 
                // we override setValueAt
            }

            public void setValueAt(Object value, int row, int col) {
                if (col == VALUECOL) {
                    Memory t = (Memory) getBySystemName(sysNameList.get(row));
                    t.setValue(value);
                    fireTableRowsUpdated(row, row);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            public String getColumnName(int col) {
                if (col == VALUECOL) {
                    return Bundle.getMessage("BlockValue");
                }
                return super.getColumnName(col);
            }

            public Class<?> getColumnClass(int col) {
                if (col == VALUECOL) {
                    return String.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            public void configValueColumn(JTable table) {
                // value column isn't button, so config is null
            }

            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
                // return (e.getPropertyName().indexOf("alue")>=0);
            }

            public JButton configureButton() {
                log.error("configureButton should not have been called");
                return null;
            }

            protected String getBeanType() {
                return Bundle.getMessage("BeanNameMemory");
            }
        };
    }

    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleMemoryTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.MemoryTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(20);
    JTextField userName = new JTextField(20);
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAdd = new JSpinner(rangeSpinner);
    JCheckBox range = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JCheckBox autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    jmri.UserPreferencesManager p;

    protected void addPressed(ActionEvent e) {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddMemory"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.MemoryAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            };
            ActionListener cancelListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) { cancelPressed(e); }
            };
            addFrame.add(new AddNewBeanPanel(sysName, userName, numberToAdd, range, autoSystemName, "ButtonOK", okListener, cancelListener));
            //sys.setToolTipText(Bundle.getMessage("SysNameTooltip", "M")); // override tooltip with bean specific letter, doesn't work
        }
        if (p.getSimplePreferenceState(systemNameAuto)) {
            autoSystemName.setSelected(true);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    void okPressed(ActionEvent e) {

        int numberOfMemory = 1;

        if (range.isSelected()) {
            numberOfMemory = (Integer) numberToAdd.getValue();
        }

        if (numberOfMemory >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(addFrame,
                    Bundle.getMessage("WarnExcessBeans", numberOfMemory),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }

        String user = userName.getText();
        if (user.equals("")) {
            user = null;
        }
        String sName = sysName.getText();
        StringBuilder b;
        for (int x = 0; x < numberOfMemory; x++) {

            if (x != 0) {
                if (user != null) {
                    b = new StringBuilder(userName.getText());
                    b.append(":");
                    b.append(Integer.toString(x));
                    user = b.toString(); // add :x to user name starting with 2nd item
                }
                if (!autoSystemName.isSelected()) {
                    b = new StringBuilder(sysName.getText());
                    b.append(":");
                    b.append(Integer.toString(x));
                    sName = b.toString();
                }
            }

            if (user != null && !user.equals("") && jmri.InstanceManager.memoryManagerInstance().getByUserName(user) != null && !p.getPreferenceState(getClassName(), "duplicateUserName")) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateUserName", user), getClassName(), "duplicateUserName", false, true);
                user = null; // new Memory objects always receive a valid system name using the next free index, but user names must not be in use so use none in that case
            }

            if (sName != null && !sName.equals("") && jmri.InstanceManager.memoryManagerInstance().getBySystemName(sName) != null && !p.getPreferenceState(getClassName(), "duplicateSystemName")) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateSystemName", sName), getClassName(), "duplicateSystemName", false, true);
                return; // new Memory objects are always valid, but system names must not be in use so skip in that case
            }

            try {
                if (autoSystemName.isSelected()) {
                    InstanceManager.memoryManagerInstance().newMemory(user);
                } else {
                    InstanceManager.memoryManagerInstance().newMemory(sName, user);
                }
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating
            }
        }
        p.setSimplePreferenceState(systemNameAuto, autoSystemName.isSelected());
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorMemoryAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleMemoryTable");
    }

    protected String getClassName() {
        return MemoryTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryTableAction.class.getName());
}
