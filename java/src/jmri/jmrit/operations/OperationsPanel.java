package jmri.jmrit.operations;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.swing.JTablePersistenceManager;
import jmri.util.JmriJFrame;
import jmri.util.swing.SplitButtonColorChooserPanel;

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
     * @param size the preferred size
     */
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
     */
    public void loadTableDetails(JTable table) {
        loadTableDetails(table, getWindowFrameRef());
        persist(table);
    }
    
    public static void loadTableDetails(JTable table, String name) {
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
        table.setName(name + ":table"); // NOI18N
        Optional<JTablePersistenceManager> manager = InstanceManager.getOptionalDefault(JTablePersistenceManager.class);
        if (manager.isPresent()) {
            manager.get().resetState(table);
        }
    }
    
    public static void persist(JTable table) {
        Optional<JTablePersistenceManager> manager = InstanceManager.getOptionalDefault(JTablePersistenceManager.class);
        if (manager.isPresent()) {
            manager.get().persist(table);
        }
    }
    
    public static void cacheState(JTable table) {
        Optional<JTablePersistenceManager> manager = InstanceManager.getOptionalDefault(JTablePersistenceManager.class);
        if (manager.isPresent()) {
            manager.get().cacheState(table);
        }
    }
    
    public static void saveTableState() {
        Optional<JTablePersistenceManager> manager = InstanceManager.getOptionalDefault(JTablePersistenceManager.class);
        if (manager.isPresent()) {
            manager.get().setPaused(false); // cheater way to save preferences.
        }
    }

    protected void clearTableSort(JTable table) {
        if (table.getRowSorter() != null) {
            table.getRowSorter().setSortKeys(null);
        }
    }

    protected void storeValues() {
        OperationsXml.save();
    }

/*
 * Kludge fix for horizontal scrollbar encroaching buttons at bottom of a scrollable window.
 */
    protected void addHorizontalScrollBarKludgeFix(JScrollPane pane, JPanel panel) {
        JPanel pad = new JPanel(); // kludge fix for horizontal scrollbar
        pad.add(new JLabel(" "));
        panel.add(pad);

        // make sure control panel is the right size
        pane.setMinimumSize(new Dimension(500, 130));
        pane.setMaximumSize(new Dimension(2000, 170));
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    }

    protected String getWindowFrameRef() {
        Container c = this.getTopLevelAncestor();
        if (c instanceof JmriJFrame) {
            return ((JmriJFrame) c).getWindowFrameRef();
        }
        return null;
    }
    
    public static JPanel getColorChooserPanel(String text, JColorChooser chooser) {
        JPanel pTextColorPanel = new JPanel();
        pTextColorPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TextColor")));
        chooser.setColor(TrainCommon.getTextColor(text));
        AbstractColorChooserPanel commentColorPanels[] = {new SplitButtonColorChooserPanel()};
        chooser.setChooserPanels(commentColorPanels);
        chooser.setPreviewPanel(new JPanel());
        pTextColorPanel.add(chooser);
        return pTextColorPanel;
    }


    private final static Logger log = LoggerFactory.getLogger(OperationsPanel.class);
}
