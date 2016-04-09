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
import jmri.jmrit.operations.setup.Control;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for operations
 *
 * @author Dan Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "cast is consistent OperationsPanel")
public class OperationsFrame extends JmriJFrame implements AncestorListener {

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
        ((OperationsPanel) this.getContentPane()).saveTableDetails(table);
    }

    /**
     * Loads the table's width, position, and sorting status from the user
     * preferences file.
     *
     * @param table The table to be adjusted.
     * @return true if table has been adjusted by saved xml file.
     */
    public boolean loadTableDetails(JTable table) {
        return ((OperationsPanel) this.getContentPane()).loadTableDetails(table);
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
