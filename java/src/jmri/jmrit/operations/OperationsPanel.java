package jmri.jmrit.operations;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.Optional;
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
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import jmri.InstanceManager;
import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.swing.JTablePersistenceManager;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel for operations
 *
 * @author Dan Boudreau Copyright (C) 2008, 2012
 */
public class OperationsPanel extends JPanel {

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

//    protected void addItemLeft(JComponent c, int x, int y) {
//        GridBagConstraints gc = new GridBagConstraints();
//        gc.gridx = x;
//        gc.gridy = y;
//        gc.weightx = 100.0;
//        gc.weighty = 100.0;
//        gc.anchor = GridBagConstraints.WEST;
//        this.add(c, gc);
//    }

//    protected void addItemWidth(JComponent c, int width, int x, int y) {
//        GridBagConstraints gc = new GridBagConstraints();
//        gc.gridx = x;
//        gc.gridy = y;
//        gc.gridwidth = width;
//        gc.weightx = 100.0;
//        gc.weighty = 100.0;
//        this.add(c, gc);
//    }

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
        for (int i = 0; i < InstanceManager.getDefault(CarTypes.class).getMaxFullNameLength(); i++) {
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
     * @param scrollPane the pane containing the textArea
     * @param textArea   the textArea to adjust
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
     * Load the table width, position, and sorting status from the user
     * preferences file.
     *
     * @param table The table to be adjusted.
     * @return true if a default instance of the
     *         {@link jmri.swing.JTablePersistenceManager} is available; false
     *         otherwise
     */
    public boolean loadTableDetails(JTable table) {
        if (table.getRowSorter() == null) {
            TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(table.getModel());
            table.setRowSorter(sorter);
            // only sort on columns that are String, Integer or Boolean (check boxes)
            for (int i = 0; i < table.getColumnCount(); i++) {
                if (table.getColumnClass(i) == String.class ||
                        table.getColumnClass(i) == Integer.class ||
                        table.getColumnClass(i) == Boolean.class) {
                    continue; // allow sorting
                }
                sorter.setSortable(i, false);
            }
        }
        // set row height
        table.setRowHeight(new JComboBox<>().getPreferredSize().height);
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // give each cell a bit of space between the vertical lines and text
        table.setIntercellSpacing(new Dimension(3, 1));
        // table must have a name
        table.setName(getWindowFrameRef() + ":table"); // NOI18N
        Optional<JTablePersistenceManager> manager = InstanceManager.getOptionalDefault(JTablePersistenceManager.class);
        if (manager.isPresent()) {
            manager.get().resetState(table);
            manager.get().persist(table);
            return true;
        }
        return false;
    }

    protected void clearTableSort(JTable table) {
        if (table.getRowSorter() != null) {
            table.getRowSorter().setSortKeys(null);
        }
    }

    protected synchronized void createShutDownTask() {
        InstanceManager.getDefault(OperationsManager.class)
                .setShutDownTask(new SwingShutDownTask("Operations Train Window Check", // NOI18N
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

//    protected String lineWrap(String s) {
//        return this.lineWrap(s, this.getPreferredSize());
//    }

//    protected String lineWrap(String s, Dimension size) {
//        int numberChar = 80;
//        if (size != null) {
//            JLabel X = new JLabel("X");
//            numberChar = size.width / X.getPreferredSize().width;
//        }
//
//        String[] sa = s.split(NEW_LINE);
//        StringBuilder so = new StringBuilder();
//
//        for (int i = 0; i < sa.length; i++) {
//            if (i > 0) {
//                so.append(NEW_LINE);
//            }
//            StringBuilder sb = new StringBuilder(sa[i]);
//            int j = 0;
//            while (j + numberChar < sb.length() && (j = sb.lastIndexOf(" ", j + numberChar)) != -1) {
//                sb.replace(j, j + 1, NEW_LINE);
//            }
//            so.append(sb);
//        }
//        return so.toString();
//    }

    
//    protected JPanel pad; // used to pad out lower part of window to fix horizontal scrollbar issue

 // Kludge fix for horizontal scrollbar encroaching buttons at bottom of a scrollable window.
    protected void addHorizontalScrollBarKludgeFix(JScrollPane pane, JPanel panel) {
        JPanel pad = new JPanel(); // kludge fix for horizontal scrollbar
        pad.add(new JLabel(" "));
        panel.add(pad);

        // make sure control panel is the right size
        pane.setMinimumSize(new Dimension(500, 130));
        pane.setMaximumSize(new Dimension(2000, 170));
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

//        pane.addAncestorListener(this); // used to determine if scrollbar is showing
    }

//    @Override
//    public void ancestorAdded(AncestorEvent event) {
////  log.debug("Ancestor Added");
//        // do nothing
//    }
//
//    @Override
//    public void ancestorRemoved(AncestorEvent event) {
////  log.debug("Ancestor Removed");
//        // do nothing
//    }
//
//    @Override
//    public void ancestorMoved(AncestorEvent event) {
//        if (pad != null) {
//            if (pad.isVisible() ^ ((JScrollPane) event.getSource()).getHorizontalScrollBar().isShowing()) {
//                pad.setVisible(((JScrollPane) event.getSource()).getHorizontalScrollBar().isShowing());
//    log.debug("Scrollbar visible: {}", pad.isVisible());
//            }
//        }
//    }

    protected String getWindowFrameRef() {
        Container c = this.getTopLevelAncestor();
        if (c instanceof JmriJFrame) {
            return ((JmriJFrame) c).getWindowFrameRef();
        }
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(OperationsPanel.class);
}
