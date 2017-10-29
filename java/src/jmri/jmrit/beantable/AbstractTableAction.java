package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;
import jmri.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a NamedBeanTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
abstract public class AbstractTableAction extends AbstractAction {

    public AbstractTableAction(String actionName) {
        super(actionName);
    }

    public AbstractTableAction(String actionName, Object option) {
        super(actionName);
    }

    protected BeanTableDataModel m;

    /**
     * Create the JTable DataModel, along with the changes for the specific
     * NamedBean type.
     */
    protected abstract void createModel();

    /**
     * Include the correct title.
     */
    protected abstract void setTitle();

    protected BeanTableFrame f;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableRowSorter<BeanTableDataModel> sorter = new TableRowSorter<>(m);
        JTable dataTable = m.makeJTable(m.getMasterClassName(), m, sorter);

        // allow reordering of the columns
        dataTable.getTableHeader().setReorderingAllowed(true);

        // create the frame
        f = new BeanTableFrame(m, helpTarget(), dataTable) {

            /**
             * Include an "add" button
             */
            @Override
            void extras() {
                if (includeAddButton) {
                    JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));
                    addToBottomBox(addButton, this.getClass().getName());
                    addButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            addPressed(e);
                        }
                    });
                }
            }
        };
        setMenuBar(f); // comes after the Help menu is added by f = new BeanTableFrame(etc.) in stand alone application
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }

    public BeanTableDataModel getTableDataModel() {
        createModel();
        return m;
    }

    public void setFrame(BeanTableFrame frame) {
        f = frame;
    }

    public BeanTableFrame getFrame() {
        return f;
    }

    /**
     * Allow subclasses to add to the frame without having to actually subclass
     * the BeanTableDataFrame.
     *
     * @param f the Frame to add to
     */
    public void addToFrame(BeanTableFrame f) {
    }

    /**
     * If the subClass is being included in a greater tabbed frame, then this
     * method is used to add the details to the tabbed frame.
     *
     * @param f AbstractTableTabAction for the containing frame containing these
     *          and other tabs
     */
    public void addToPanel(AbstractTableTabAction f) {
    }

    /**
     * If the subClass is being included in a greater tabbed frame, then this is
     * used to specify which manager the subclass should be using.
     *
     * @param man Manager for this table tab
     */
    protected void setManager(Manager man) {
    }

    /**
     * Allow subclasses to alter the frame's Menubar without having to actually
     * subclass the BeanTableDataFrame.
     *
     * @param f the Frame to attach the menubar to
     */
    public void setMenuBar(BeanTableFrame f) {
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
     * Specify the JavaHelp target for this specific panel.
     *
     * @return a fixed default string "index" pointing to to highest level in
     *         JMRI Help
     */
    protected String helpTarget() {
        return "index";  // by default, go to the top
    }

    public String getClassDescription() {
        return "Abstract Table Action";
    }

    public void setMessagePreferencesDetails() {
        HashMap<Integer, String> options = new HashMap<>(3);
        options.put(0x00, Bundle.getMessage("DeleteAsk"));
        options.put(0x01, Bundle.getMessage("DeleteNever"));
        options.put(0x02, Bundle.getMessage("DeleteAlways"));
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).messageItemDetails(getClassName(), "deleteInUse", Bundle.getMessage("DeleteItemInUse"), options, 0x00);
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
    public void print(javax.swing.JTable.PrintMode mode, java.text.MessageFormat headerFormat, java.text.MessageFormat footerFormat) {
        log.error("Caught here");
    }

    protected abstract void addPressed(ActionEvent e);

    private final static Logger log = LoggerFactory.getLogger(AbstractTableAction.class);
}
