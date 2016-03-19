//OperationsFrame.java
package jmri.jmrit.operations;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JmriJFrame;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for operations
 *
 * @author Dan Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */
public class OperationsFrame extends JmriJFrame implements AncestorListener {

    /**
     *
     */
    private static final long serialVersionUID = -8263240370517832287L;
    public static final String NEW_LINE = "\n"; // NOI18N
    public static final String NONE = ""; // NOI18N

    public OperationsFrame(String s) {
        this(s, new OperationsPanel());
    }

    public OperationsFrame() {
        this(new OperationsPanel());
    }

    public OperationsFrame(OperationsPanel p) {
        super();
        this.setContentPane(p);
        this.setEscapeKeyClosesWindow(true);
    }

    public OperationsFrame(String s, OperationsPanel p) {
        super(s);
        this.setContentPane(p);
        this.setEscapeKeyClosesWindow(true);
    }

    @Override
    public void initComponents() {
        // default method does nothing, but fail to call super.initComponents,
        // so that Exception does not need to be caught
    }

    public void initMinimumSize() {
        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight250));
    }

    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
    }

    protected void addItem(JComponent c, int x, int y) {
        ((OperationsPanel) this.getContentPane()).addItem(c, x, y);
    }

    protected void addItemLeft(JComponent c, int x, int y) {
        ((OperationsPanel) this.getContentPane()).addItemLeft(c, x, y);
    }

    protected void addItemWidth(JComponent c, int width, int x, int y) {
        ((OperationsPanel) this.getContentPane()).addItemWidth(c, width, x, y);
    }

    protected void addItem(JPanel p, JComponent c, int x, int y) {
        ((OperationsPanel) this.getContentPane()).addItem(p, c, x, y);
    }

    protected void addItemLeft(JPanel p, JComponent c, int x, int y) {
        ((OperationsPanel) this.getContentPane()).addItemLeft(p, c, x, y);
    }

    protected void addItemTop(JPanel p, JComponent c, int x, int y) {
        ((OperationsPanel) this.getContentPane()).addItemTop(p, c, x, y);
    }

    protected void addItemWidth(JPanel p, JComponent c, int width, int x, int y) {
        ((OperationsPanel) this.getContentPane()).addItemWidth(p, c, width, x, y);
    }

    /**
     * Gets the number of checkboxes(+1) that can fix in one row see
     * OperationsFrame.minCheckboxes and OperationsFrame.maxCheckboxes
     *
     * @return the number of checkboxes, minimum is 5 (6 checkboxes)
     */
    protected int getNumberOfCheckboxesPerLine() {
        return ((OperationsPanel) this.getContentPane()).getNumberOfCheckboxesPerLine(this.getPreferredSize());
    }

    protected void addButtonAction(JButton b) {
        b.addActionListener(this::buttonActionPerformed);
    }

    protected void buttonActionPerformed(ActionEvent ae) {
        ((OperationsPanel) this.getContentPane()).buttonActionPerformed(ae);
    }

    protected void addRadioButtonAction(JRadioButton b) {
        b.addActionListener(this::radioButtonActionPerformed);
    }

    protected void radioButtonActionPerformed(ActionEvent ae) {
        ((OperationsPanel) this.getContentPane()).radioButtonActionPerformed(ae);
    }

    protected void addCheckBoxAction(JCheckBox b) {
        b.addActionListener(this::checkBoxActionPerformed);
    }

    protected void checkBoxActionPerformed(ActionEvent ae) {
        ((OperationsPanel) this.getContentPane()).checkBoxActionPerformed(ae);
    }

    protected void addSpinnerChangeListerner(JSpinner s) {
        s.addChangeListener(this::spinnerChangeEvent);
    }

    protected void spinnerChangeEvent(ChangeEvent ae) {
        ((OperationsPanel) this.getContentPane()).spinnerChangeEvent(ae);
    }

    protected void addComboBoxAction(JComboBox<?> b) {
        b.addActionListener(this::comboBoxActionPerformed);
    }

    protected void comboBoxActionPerformed(ActionEvent ae) {
        ((OperationsPanel) this.getContentPane()).comboBoxActionPerformed(ae);
    }

    protected void selectNextItemComboBox(JComboBox<?> b) {
        ((OperationsPanel) this.getContentPane()).selectNextItemComboBox(b);
    }

    /**
     * Will modify the character column width of a TextArea box to 90% of a
     * panels width. ScrollPane is set to 95% of panel width.
     *
     * @param scrollPane
     * @param textArea
     */
    protected void adjustTextAreaColumnWidth(JScrollPane scrollPane, JTextArea textArea) {
        ((OperationsPanel) this.getContentPane()).adjustTextAreaColumnWidth(scrollPane, textArea, this.getPreferredSize());
    }

    /**
     * Saves the table's width, position, and sorting status in the user
     * preferences file
     *
     * @param table Table to be saved.
     */
    protected void saveTableDetails(JTable table) {
        UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
        if (p == null) {
            return;
        }
        TableSorter sorter = null;
        String tableref = getWindowFrameRef() + ":table"; // NOI18N
        try {
            sorter = (TableSorter) table.getModel();
        } catch (Exception e) {
            log.debug("table " + tableref + " doesn't use sorter");
        }

        // is the table using XTableColumnModel?
        if (sorter != null && sorter.getColumnCount() != table.getColumnCount()) {
            log.debug("Sort column count: {} table column count: {} XTableColumnModel in use", sorter.getColumnCount(),
                    table.getColumnCount());
            XTableColumnModel tcm = (XTableColumnModel) table.getColumnModel();
            // need to have all columns visible so we can get the proper column order
            boolean[] columnVisible = new boolean[sorter.getColumnCount()];
            for (int i = 0; i < sorter.getColumnCount(); i++) {
                columnVisible[i] = tcm.isColumnVisible(tcm.getColumnByModelIndex(i));
                tcm.setColumnVisible(tcm.getColumnByModelIndex(i), true);
            }
            // now save with the correct column order
            for (int i = 0; i < sorter.getColumnCount(); i++) {
                int sortStatus = sorter.getSortingStatus(i);
                int width = tcm.getColumnByModelIndex(i).getPreferredWidth();
                int order = table.convertColumnIndexToView(i);
                // must save with column not hidden
                p.setTableColumnPreferences(tableref, sorter.getColumnName(i), order, width, sortStatus, false);
            }
            // now restore
            for (int i = 0; i < sorter.getColumnCount(); i++) {
                tcm.setColumnVisible(tcm.getColumnByModelIndex(i), columnVisible[i]);
            }

        } // standard table
        else {
            for (int i = 0; i < table.getColumnCount(); i++) {
                int sortStatus = 0;
                if (sorter != null) {
                    sortStatus = sorter.getSortingStatus(i);
                }
                p.setTableColumnPreferences(tableref, table.getColumnName(i), i, table.getColumnModel().getColumn(i)
                        .getPreferredWidth(), sortStatus, false);
            }
        }
    }

    /**
     * Loads the table's width, position, and sorting status from the user
     * preferences file.
     *
     * @param table The table to be adjusted.
     * @return true if table has been adjusted by saved xml file.
     */
    public boolean loadTableDetails(JTable table) {
        UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
        TableSorter sorter = null;
        String tableref = getWindowFrameRef() + ":table"; // NOI18N
        if (p == null || p.getTablesColumnList(tableref).isEmpty()) {
            return false;
        }
        try {
            sorter = (TableSorter) table.getModel();
        } catch (Exception e) {
            log.debug("table " + tableref + " doesn't use sorter");
        }
        // bubble sort
        int count = 0;
        while (!sortTable(table, p, tableref) && count < 10) {
            count++;
            log.debug("bubble sort pass {}:", count);
        }
        // Some tables have more than one name, so use the current one for size
        for (int i = 0; i < table.getColumnCount(); i++) {
            String columnName = table.getColumnName(i);
            int sort = p.getTableColumnSort(tableref, columnName);
            if (sorter != null) {
                sorter.setSortingStatus(i, sort);
            }
            int width = p.getTableColumnWidth(tableref, columnName);
            if (width != -1) {
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            } else {
                // name not found so use one that exists
                String name = p.getTableColumnAtNum(tableref, i);
                if (name != null) {
                    width = p.getTableColumnWidth(tableref, name);
                    table.getColumnModel().getColumn(i).setPreferredWidth(width);
                }
            }
        }
        return true;
    }

    private boolean sortTable(JTable table, UserPreferencesManager p, String tableref) {
        boolean sortDone = true;
        for (int i = 0; i < table.getColumnCount(); i++) {
            String columnName = table.getColumnName(i);
            int order = p.getTableColumnOrder(tableref, columnName);
            if (order == -1) {
                log.debug("Column name {} not found in user preference file", columnName);
                break; // table structure has changed quit sort
            }
            if (i != order && order < table.getColumnCount()) {
                table.moveColumn(i, order);
                log.debug("Move column number: {} name: {} to: {}", i, columnName, order);
                sortDone = false;
            }
        }
        return sortDone;
    }

    protected void clearTableSort(JTable table) {
        ((OperationsPanel) this.getContentPane()).clearTableSort(table);
    }

    protected synchronized void createShutDownTask() {
        ((OperationsPanel) this.getContentPane()).createShutDownTask();
    }

    @Override
    public void dispose() {
        ((OperationsPanel) this.getContentPane()).dispose();
        super.dispose();
    }

    @Override
    protected void storeValues() {
        ((OperationsPanel) this.getContentPane()).storeValues();
    }

    protected String lineWrap(String s) {
        return ((OperationsPanel) this.getContentPane()).lineWrap(s, this.getPreferredSize());
    }

    // Kludge fix for horizontal scrollbar encroaching buttons at bottom of a scrollable window.
    protected JPanel pad; // used to pad out lower part of window to fix horizontal scrollbar issue

    protected void addHorizontalScrollBarKludgeFix(JScrollPane pane, JPanel panel) {
        pad = new JPanel();	// kludge fix for horizontal scrollbar
        pad.add(new JLabel(" "));
        panel.add(pad);

        // make sure control panel is the right size
        pane.setMinimumSize(new Dimension(500, 130));
        pane.setMaximumSize(new Dimension(2000, 170));
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        pane.addAncestorListener(this); // used to determine if scrollbar is showing
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        ((OperationsPanel) this.getContentPane()).ancestorAdded(event);
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        ((OperationsPanel) this.getContentPane()).ancestorRemoved(event);
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
        ((OperationsPanel) this.getContentPane()).ancestorMoved(event);
    }

    private final static Logger log = LoggerFactory.getLogger(OperationsFrame.class.getName());
}
