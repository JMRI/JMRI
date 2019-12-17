package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.ProxyManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.SystemConnectionMemoManager;
import jmri.swing.ManagerComboBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a NamedBeanTable GUI.
 *
 * @param <E> type of NamedBean supported in this table
 * @author Bob Jacobsen Copyright (C) 2003
 */
public abstract class AbstractTableAction<E extends NamedBean> extends AbstractAction {

    public AbstractTableAction(String actionName) {
        super(actionName);
    }

    public AbstractTableAction(String actionName, Object option) {
        super(actionName);
    }

    protected BeanTableDataModel<E> m;

    /**
     * Create the JTable DataModel, along with the changes for the specific
     * NamedBean type.
     */
    protected abstract void createModel();

    /**
     * Include the correct title.
     */
    protected abstract void setTitle();

    protected BeanTableFrame<E> f;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableRowSorter<BeanTableDataModel<E>> sorter = new TableRowSorter<>(m);
        JTable dataTable = m.makeJTable(m.getMasterClassName(), m, sorter);

        // allow reordering of the columns
        dataTable.getTableHeader().setReorderingAllowed(true);

        // create the frame
        f = new BeanTableFrame<E>(m, helpTarget(), dataTable) {

            /**
             * Include an "Add..." button
             */
            @Override
            void extras() {
                if (includeAddButton) {
                    JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));
                    addToBottomBox(addButton, this.getClass().getName());
                    addButton.addActionListener((ActionEvent e1) -> {
                        addPressed(e1);
                    });
                }
            }
        };
        setMenuBar(f); // comes after the Help menu is added by f = new
                       // BeanTableFrame(etc.) in stand alone application
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }

    public BeanTableDataModel<E> getTableDataModel() {
        createModel();
        return m;
    }

    public void setFrame(@Nonnull BeanTableFrame<E> frame) {
        f = frame;
    }

    public BeanTableFrame<E> getFrame() {
        return f;
    }

    /**
     * Allow subclasses to add to the frame without having to actually subclass
     * the BeanTableDataFrame.
     *
     * @param f the Frame to add to
     */
    public void addToFrame(@Nonnull BeanTableFrame<E> f) {
    }

    /**
     * If the subClass is being included in a greater tabbed frame, then this
     * method is used to add the details to the tabbed frame.
     *
     * @param f AbstractTableTabAction for the containing frame containing these
     *          and other tabs
     */
    public void addToPanel(AbstractTableTabAction<E> f) {
    }

    /**
     * If the subClass is being included in a greater tabbed frame, then this is
     * used to specify which manager the subclass should be using.
     *
     * @param man Manager for this table tab
     */
    protected void setManager(@Nonnull Manager<E> man) {
    }

    /**
     * Allow subclasses to alter the frame's Menubar without having to actually
     * subclass the BeanTableDataFrame.
     *
     * @param f the Frame to attach the menubar to
     */
    public void setMenuBar(BeanTableFrame<E> f) {
    }

    public JPanel getPanel() {
        return null;
    }

    public void dispose() {
        if (m != null) {
            m.dispose();
        }
        // should this also dispose of the frame f?
    }

    /**
     * Increments trailing digits of a system/user name (string) I.E. "Geo7"
     * returns "Geo8" Note: preserves leading zeros: "Geo007" returns "Geo008"
     * Also, if no trailing digits, appends "1": "Geo" returns "Geo1"
     *
     * @param name the system or user name string
     * @return the same name with trailing digits incremented by one
     */
    protected @Nonnull String nextName(@Nonnull String name) {
        final String[] parts = name.split("(?=\\d+$)", 2);
        String numString = "0";
        if (parts.length == 2) {
            numString = parts[1];
        }
        final int numStringLength = numString.length();
        final int num = Integer.parseInt(numString) + 1;
        return parts[0] + String.format("%0" + numStringLength + "d", num);
    }

    /**
     * Specify the JavaHelp target for this specific panel.
     *
     * @return a fixed default string "index" pointing to to highest level in
     *         JMRI Help
     */
    protected String helpTarget() {
        return "index"; // by default, go to the top
    }

    public String getClassDescription() {
        return "Abstract Table Action";
    }

    public void setMessagePreferencesDetails() {
        HashMap<Integer, String> options = new HashMap<>(3);
        options.put(0x00, Bundle.getMessage("DeleteAsk"));
        options.put(0x01, Bundle.getMessage("DeleteNever"));
        options.put(0x02, Bundle.getMessage("DeleteAlways"));
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMessageItemDetails(getClassName(),
                "deleteInUse", Bundle.getMessage("DeleteItemInUse"), options, 0x00);
    }

    protected abstract String getClassName();

    public boolean includeAddButton() {
        return includeAddButton;
    }

    protected boolean includeAddButton = true;

    /**
     * Used with the Tabbed instances of table action, so that the print option
     * is handled via that on the appropriate tab.
     *
     * @param mode         table print mode
     * @param headerFormat messageFormat for header
     * @param footerFormat messageFormat for footer
     */
    public void print(JTable.PrintMode mode, MessageFormat headerFormat, MessageFormat footerFormat) {
        log.error("Printing not handled for {} tables.", m.getBeanType());
    }

    protected abstract void addPressed(ActionEvent e);

    /**
     * Configure the combo box listing managers.
     *
     * @param comboBox     the combo box to configure
     * @param manager      the current manager
     * @param managerClass the implemented manager class for the current
     *                     mananger; this is the class used by
     *                     {@link InstanceManager#getDefault(Class)} to get the
     *                     default manager, which may or may not be the current
     *                     manager
     */
    protected void configureManagerComboBox(ManagerComboBox<E> comboBox, Manager<E> manager,
            Class<? extends Manager<E>> managerClass) {
        Manager<E> defaultManager = InstanceManager.getDefault(managerClass);
        // populate comboBox
        if (defaultManager instanceof ProxyManager) {
            comboBox.setManagers(defaultManager);
        } else {
            comboBox.setManagers(manager);
        }
        // set current selection
        if (manager instanceof ProxyManager) {
            UserPreferencesManager upm = InstanceManager.getDefault(UserPreferencesManager.class);
            String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
            if (upm.getComboBoxLastSelection(systemSelectionCombo) != null) {
                SystemConnectionMemo memo = SystemConnectionMemoManager.getDefault()
                        .getSystemConnectionMemoForUserName(upm.getComboBoxLastSelection(systemSelectionCombo));
                comboBox.setSelectedItem(memo.get(managerClass));
            } else {
                ProxyManager<E> proxy = (ProxyManager<E>) manager;
                comboBox.setSelectedItem(proxy.getDefaultManager());
            }
        } else {
            comboBox.setSelectedItem(manager);
        }
    }

    /**
     * Remove the Add panel prefixBox listener before disposal.
     * The listener is created when the Add panel is defined.  It persists after the
     * the Add panel has been disposed.  When the next Add is created, AbstractTableAction
     * sets the default connection as the current selection.  This triggers validation before
     * the new Add panel is created.
     * <p>
     * The listener is removed by the controlling table action before disposing of the Add
     * panel after Close or Create.
     * @param prefixBox The prefix combobox that might contain the listener.
     */
    protected void removePrefixBoxListener(ManagerComboBox<E> prefixBox) {
        Arrays.asList(prefixBox.getActionListeners()).forEach((l) -> {
            prefixBox.removeActionListener(l);
        });
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractTableAction.class);
}
