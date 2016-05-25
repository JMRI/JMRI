//OperationsPanel.java
package jmri.jmrit.operations;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
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
import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JmriJFrame;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel for operations
 *
 * @author Dan Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */
public class OperationsPanel extends JPanel implements AncestorListener {

    public static final String NEW_LINE = "\n"; // NOI18N
    public static final String NONE = ""; // NOI18N

    public OperationsPanel() {
        super();
    }

    public void initMinimumSize() {
        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight250));
    }

    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
    }

    public void dispose() {
        // The default method does nothing.
    }

    protected void addItem(JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        this.add(c, gc);
    }

    protected void addItemLeft(JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        gc.anchor = GridBagConstraints.WEST;
        this.add(c, gc);
    }

    protected void addItemWidth(JComponent c, int width, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = width;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        this.add(c, gc);
    }

    protected void addItem(JPanel p, JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        p.add(c, gc);
    }

    protected void addItemLeft(JPanel p, JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        gc.anchor = GridBagConstraints.WEST;
        p.add(c, gc);
    }

    protected void addItemTop(JPanel p, JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100;
        gc.weighty = 100;
        gc.anchor = GridBagConstraints.NORTH;
        p.add(c, gc);
    }

    protected void addItemWidth(JPanel p, JComponent c, int width, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = width;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        gc.anchor = GridBagConstraints.WEST;
        p.add(c, gc);
    }

    private static final int MIN_CHECKBOXES = 5;
    private static final int MAX_CHECKBOXES = 11;

    /**
     * Gets the number of checkboxes(+1) that can fix in one row see
     * OperationsFrame.minCheckboxes and OperationsFrame.maxCheckboxes
     *
     * @return the number of checkboxes, minimum is 5 (6 checkboxes)
     */
    protected int getNumberOfCheckboxesPerLine() {
        return getNumberOfCheckboxesPerLine(this.getPreferredSize());
    }

    protected int getNumberOfCheckboxesPerLine(Dimension size) {
        if (size == null) {
            return MIN_CHECKBOXES; // default is 6 checkboxes per row
        }
        StringBuilder padding = new StringBuilder("X");
        for (int i = 0; i < CarTypes.instance().getMaxFullNameLength(); i++) {
            padding.append("X");
        }

        JCheckBox box = new JCheckBox(padding.toString());
        int number = size.width / (box.getPreferredSize().width);
        if (number < MIN_CHECKBOXES) {
            number = MIN_CHECKBOXES;
        }
        if (number > MAX_CHECKBOXES) {
            number = MAX_CHECKBOXES;
        }
        return number;
    }

    protected void addButtonAction(JButton b) {
        b.addActionListener((ActionEvent e) -> {
            buttonActionPerformed(e);
        });
    }

    protected void buttonActionPerformed(ActionEvent ae) {
        log.debug("button action not overridden");
    }

    protected void addRadioButtonAction(JRadioButton b) {
        b.addActionListener((ActionEvent e) -> {
            radioButtonActionPerformed(e);
        });
    }

    protected void radioButtonActionPerformed(ActionEvent ae) {
        log.debug("radio button action not overridden");
    }

    protected void addCheckBoxAction(JCheckBox b) {
        b.addActionListener((ActionEvent e) -> {
            checkBoxActionPerformed(e);
        });
    }

    protected void checkBoxActionPerformed(ActionEvent ae) {
        log.debug("check box action not overridden");
    }

    protected void addSpinnerChangeListerner(JSpinner s) {
        s.addChangeListener((ChangeEvent e) -> {
            spinnerChangeEvent(e);
        });
    }

    protected void spinnerChangeEvent(javax.swing.event.ChangeEvent ae) {
        log.debug("spinner action not overridden");
    }

    protected void addComboBoxAction(JComboBox<?> b) {
        b.addActionListener((ActionEvent e) -> {
            comboBoxActionPerformed(e);
        });
    }

    protected void comboBoxActionPerformed(ActionEvent ae) {
        log.debug("combobox action not overridden");
    }

    protected void selectNextItemComboBox(JComboBox<?> b) {
        int newIndex = b.getSelectedIndex() + 1;
        if (newIndex < b.getItemCount()) {
            b.setSelectedIndex(newIndex);
        }
    }

    /**
     * Will modify the character column width of a TextArea box to 90% of a
     * panels width. ScrollPane is set to 95% of panel width.
     *
     * @param scrollPane
     * @param textArea
     */
    protected void adjustTextAreaColumnWidth(JScrollPane scrollPane, JTextArea textArea) {
        this.adjustTextAreaColumnWidth(scrollPane, textArea, this.getPreferredSize());
    }

    protected void adjustTextAreaColumnWidth(JScrollPane scrollPane, JTextArea textArea, Dimension size) {
        FontMetrics metrics = getFontMetrics(textArea.getFont());
        int columnWidth = metrics.charWidth('m');
        int width = size.width;
        int columns = width / columnWidth * 90 / 100; // make text area 90% of the panel width
        if (columns > textArea.getColumns()) {
            log.debug("Increasing text area character width to {} columns", columns);
            textArea.setColumns(columns);
        }
        scrollPane.setMinimumSize(new Dimension(width * 95 / 100, 60));
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
                p.setTableColumnPreferences(tableref, sorter.getColumnName(i), order, width, TableSorter.getSortOrder(sortStatus), false);
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
                        .getPreferredWidth(), TableSorter.getSortOrder(sortStatus), false);
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
            int sort = TableSorter.getSortStatus(p.getTableColumnSort(tableref, columnName));
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
        TableSorter sorter = null;
        try {
            sorter = (TableSorter) table.getModel();
        } catch (Exception e) {
            log.debug("table doesn't use sorter");
        }
        if (sorter == null) {
            return;
        }
        for (int i = 0; i < table.getColumnCount(); i++) {
            sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
        }
    }

    protected synchronized void createShutDownTask() {
        OperationsManager.getInstance().setShutDownTask(new SwingShutDownTask("Operations Train Window Check", // NOI18N
                Bundle.getMessage("PromptQuitWindowNotWritten"), Bundle.getMessage("PromptSaveQuit"), this) {
                    @Override
                    public boolean checkPromptNeeded() {
                        if (Setup.isAutoSaveEnabled()) {
                            storeValues();
                            return true;
                        }
                        return !OperationsXml.areFilesDirty();
                    }

                    @Override
                    public boolean doPrompt() {
                        storeValues();
                        return true;
                    }

                    @Override
                    public boolean doClose() {
                        storeValues();
                        return true;
                    }
                });
    }

    protected void storeValues() {
        OperationsXml.save();
    }

    protected String lineWrap(String s) {
        return this.lineWrap(s, this.getPreferredSize());
    }

    protected String lineWrap(String s, Dimension size) {
        int numberChar = 80;
        if (size != null) {
            JLabel X = new JLabel("X");
            numberChar = size.width / X.getPreferredSize().width;
        }

        String[] sa = s.split(NEW_LINE);
        StringBuilder so = new StringBuilder();

        for (int i = 0; i < sa.length; i++) {
            if (i > 0) {
                so.append(NEW_LINE);
            }
            StringBuilder sb = new StringBuilder(sa[i]);
            int j = 0;
            while (j + numberChar < sb.length() && (j = sb.lastIndexOf(" ", j + numberChar)) != -1) {
                sb.replace(j, j + 1, NEW_LINE);
            }
            so.append(sb);
        }
        return so.toString();
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
//		log.debug("Ancestor Added");
        // do nothing
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
//		log.debug("Ancestor Removed");	
        // do nothing
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
        if (pad != null) {
            if (pad.isVisible() ^ ((JScrollPane) event.getSource()).getHorizontalScrollBar().isShowing()) {
                pad.setVisible(((JScrollPane) event.getSource()).getHorizontalScrollBar().isShowing());
//				log.debug("Scrollbar visible: {}", pad.isVisible());
            }
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "Checks for instance")
    protected String getWindowFrameRef() {
        if (this.getTopLevelAncestor() instanceof JmriJFrame) {
            return ((JmriJFrame) this.getTopLevelAncestor()).getWindowFrameRef();
        }
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(OperationsPanel.class.getName());
}
