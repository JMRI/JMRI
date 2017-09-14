package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
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
 * @author Bob Jacobsen Copyright (C) 2003
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
    @Override
    protected void createModel() {
        m = new BeanTableDataModel() {

            @Override
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

            @Override
            public Manager getManager() {
                return InstanceManager.memoryManagerInstance();
            }

            @Override
            public NamedBean getBySystemName(String name) {
                return InstanceManager.memoryManagerInstance().getBySystemName(name);
            }

            @Override
            public NamedBean getByUserName(String name) {
                return InstanceManager.memoryManagerInstance().getByUserName(name);
            }

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            @Override
            public void clickOn(NamedBean t) {
                // don't do anything on click; not used in this class, because
                // we override setValueAt
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == VALUECOL) {
                    Memory t = (Memory) getBySystemName(sysNameList.get(row));
                    t.setValue(value);
                    fireTableRowsUpdated(row, row);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            @Override
            public String getColumnName(int col) {
                if (col == VALUECOL) {
                    return Bundle.getMessage("BlockValue");
                }
                return super.getColumnName(col);
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == VALUECOL) {
                    return String.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            @Override
            public void configValueColumn(JTable table) {
                // value column isn't button, so config is null
            }

            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
                // return (e.getPropertyName().indexOf("alue")>=0);
            }

            @Override
            public JButton configureButton() {
                log.error("configureButton should not have been called");
                return null;
            }

            @Override
            protected String getBeanType() {
                return Bundle.getMessage("BeanNameMemory");
            }
        };
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleMemoryTable"));
    }

    @Override
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
    JLabel statusBar = new JLabel(Bundle.getMessage("AddBeanStatusEnter"), JLabel.LEADING);
    jmri.UserPreferencesManager p;

    @Override
    protected void addPressed(ActionEvent e) {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddMemory"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.MemoryAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = (ActionEvent e1) -> {
                okPressed(e1);
            };
            ActionListener cancelListener = (ActionEvent e1) -> {
                cancelPressed(e1);
            };
            addFrame.add(new AddNewBeanPanel(sysName, userName, numberToAdd, range, autoSystemName, "ButtonCreate", okListener, cancelListener, statusBar));
            sysName.setToolTipText(Bundle.getMessage("SysNameToolTip", "M")); // override tooltip with bean specific letter
        }
        sysName.setBackground(Color.white);
        // reset statusBar text
        statusBar.setText(Bundle.getMessage("AddBeanStatusEnter"));
        statusBar.setForeground(Color.gray);
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
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("Memories"), numberOfMemory),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }

        String user = userName.getText().trim(); // N11N
        if (user.equals("")) {
            user = null;
        }
        String sName = sysName.getText().trim().toUpperCase(); // N11N
        // initial check for empty entry
        if (sName.length() < 1 && !autoSystemName.isSelected()) {
            statusBar.setText(Bundle.getMessage("WarningSysNameEmpty"));
            statusBar.setForeground(Color.red);
            sysName.setBackground(Color.red);
            return;
        } else {
            sysName.setBackground(Color.white);
        }

        // Add some entry pattern checking, before assembling sName and handing it to the memoryManager
        String statusMessage = Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameMemory"));
        String errorMessage = null;
        StringBuilder b;
        for (int x = 0; x < numberOfMemory; x++) {

            if (x != 0) {
                if (user != null) {
                    b = new StringBuilder(userName.getText().trim()); // N11N
                    b.append(":");
                    b.append(Integer.toString(x));
                    user = b.toString(); // add :x to user name starting with 2nd item
                }
                if (!autoSystemName.isSelected()) {
                    b = new StringBuilder(sysName.getText().trim()); // N11N
                    b.append(":");
                    b.append(Integer.toString(x));
                    sName = b.toString();
                }
            }

            if (user != null && !user.equals("") && jmri.InstanceManager.memoryManagerInstance().getByUserName(user) != null && !p.getPreferenceState(getClassName(), "duplicateUserName")) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateUserName", user), getClassName(), "duplicateUserName", false, true);
                user = null; // new Memory objects always receive a valid system name using the next free index, but user names must not be in use so use none in that case
                // show in status bar
                errorMessage = Bundle.getMessage("ErrorDuplicateUserName", user);
                statusBar.setText(errorMessage);
                statusBar.setForeground(Color.red);
            }
            if (sName != null && !sName.equals("") && jmri.InstanceManager.memoryManagerInstance().getBySystemName(sName) != null && !p.getPreferenceState(getClassName(), "duplicateSystemName")) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateSystemName", sName), getClassName(), "duplicateSystemName", false, true);
                // show in status bar
                errorMessage = Bundle.getMessage("ErrorDuplicateSystemName", sName);
                statusBar.setText(errorMessage);
                statusBar.setForeground(Color.red);
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
                errorMessage = "An error has occurred";
                statusBar.setText(errorMessage);
                statusBar.setForeground(Color.red);
                return; // without creating
            }
            // add first and last names to statusMessage user feedback string
            if (x == 0 || x == numberOfMemory - 1) statusMessage = statusMessage + " " + sName + " (" + user + ")";
            if (x == numberOfMemory - 2) statusMessage = statusMessage + " " + Bundle.getMessage("ItemCreateUpTo") + " ";
            // only mention first and last of range added
        } // end of for loop creating range of Memories

        // provide feedback to user
        if (errorMessage == null) {
            statusBar.setText(statusMessage);
            statusBar.setForeground(Color.gray);
        } else {
            statusBar.setText(errorMessage);
            // statusBar.setForeground(Color.red); // handled when errorMassage is set to differentiate urgency
        }

        p.setSimplePreferenceState(systemNameAuto, autoSystemName.isSelected());
    }

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorMemoryAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleMemoryTable");
    }

    @Override
    protected String getClassName() {
        return MemoryTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryTableAction.class);

}
