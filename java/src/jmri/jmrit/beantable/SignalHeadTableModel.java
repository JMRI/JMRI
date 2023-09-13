package jmri.jmrit.beantable;

import java.util.*;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;

/**
 * Model for a SignalHeadTable.
 * 
 * Code originally located within SignalHeadTableAction.java
 * 
 * @author Bob Jacobsen Copyright (C) 2003,2006,2007, 2008, 2009
 * @author Petr Koud'a Copyright (C) 2007
 * @author Egbert Broerse Copyright (C) 2016
 * @author Steve Young Copyright (C) 2023
 */
public class SignalHeadTableModel extends jmri.jmrit.beantable.BeanTableDataModel<SignalHead> {

    static public final int LITCOL = NUMCOLUMN;
    static public final int HELDCOL = LITCOL + 1;
    static public final int EDITCOL = HELDCOL + 1;

    public SignalHeadTableModel(){
        super();
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN + 3;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case VALUECOL:
                return Bundle.getMessage("SignalMastAppearance");  // override default title, correct name SignalHeadAppearance i.e. "Red"
            case LITCOL:
                return Bundle.getMessage("ColumnHeadLit");
            case HELDCOL:
                return Bundle.getMessage("ColumnHeadHeld");
            case EDITCOL:
                return ""; // no heading on "Edit"
            default:
                return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case VALUECOL:
                return RowComboBoxPanel.class; // Use a JPanel containing a custom Appearance ComboBox
            case LITCOL:
            case HELDCOL:
                return Boolean.class;
            case EDITCOL:
                return JButton.class;
            default:
                return super.getColumnClass(col);
        }
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case LITCOL:
            case HELDCOL:
                return new JTextField(4).getPreferredSize().width;
            case EDITCOL:
                return new JTextField(7).getPreferredSize().width;
            default:
                return super.getPreferredWidth(col);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case LITCOL:
            case HELDCOL:
            case EDITCOL:
                return true;
            default:
                return super.isCellEditable(row, col);
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        // some error checking
        if (row >= sysNameList.size()) {
            log.debug("row is greater than name list");
            return "error";
        }
        String name = sysNameList.get(row);
        SignalHead s = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName(name);
        if (s == null) {
            return Boolean.FALSE; // if due to race condition, the device is going away
        }
        switch (col) {
            case LITCOL:
                return s.getLit();
            case HELDCOL:
                return s.getHeld();
            case EDITCOL:
                return Bundle.getMessage("ButtonEdit");
            case VALUECOL:
                String appearance = s.getAppearanceName();
                if ( !appearance.isEmpty()) {
                    return appearance;
                } else {
                    //Appearance (head) not set
                    log.debug("No Appearance returned for head in row {}", row);
                    return Bundle.getMessage("BeanStateUnknown"); // use place holder string in table
                }
            default:
                return super.getValueAt(row, col);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        String name = sysNameList.get(row);
        SignalHead s = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName(name);
        if (s == null) {
            return;  // device is going away anyway
        }
        switch (col) {
            case VALUECOL:
                if (value != null) {
                    //row = table.convertRowIndexToModel(row); // find the right row in model instead of table (not needed here)
                    log.debug("SignalHead setValueAt (rowConverted={}; value={})", row, value);
                    // convert from String (selected item) to int
                    int newState = 99;
                    String[] stateNameList = s.getValidStateNames(); // Array of valid appearance names
                    int[] validStateList = s.getValidStates(); // Array of valid appearance numbers
                    for (int i = 0; i < stateNameList.length; i++) {
                        if (value.equals(stateNameList[i])) {
                            newState = validStateList[i];
                            break;
                        }
                    }
                    if (newState == 99) {
                        if (stateNameList.length == 0) {
                            newState = SignalHead.DARK;
                            log.warn("New signal state not found so setting to Dark {}", s.getDisplayName());
                        } else {
                            newState = validStateList[0];
                            log.warn("New signal state not found so setting to the first available {}", s.getDisplayName());
                        }
                    }
                    log.debug("Signal Head set from: {} to: {} [{}]", s.getAppearanceName(), value, newState);
                    s.setAppearance(newState);
                    fireTableRowsUpdated(row, row);
                }   break;
            case LITCOL:
                    s.setLit((Boolean) value);
                    break;
            case HELDCOL:
                    s.setHeld((Boolean) value);
                    break;
            case EDITCOL:
                // button clicked - edit
                editSignal(s);
                break;
            default:
                super.setValueAt(value, row, col);
                break;
        }
    }

    @Override
    public String getValue(String name) {
        SignalHead s = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName(name);
        if (s == null) {
            return "<lost>"; // if due to race condition, the device is going away
        }
        String val = null;
        try {
            val = s.getAppearanceName();
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            log.error("Could not get Appearance Name for {}", s.getDisplayName(), e);
        }
        if (val != null) {
            return val;
        } else {
            return "Unexpected null value";
        }
    }

    @Override
    public SignalHeadManager getManager() {
        return InstanceManager.getDefault(SignalHeadManager.class);
    }

    @Override
    public SignalHead getBySystemName(@Nonnull String name) {
        return InstanceManager.getDefault(SignalHeadManager.class).getBySystemName(name);
    }

    @Override
    public SignalHead getByUserName(@Nonnull String name) {
        return InstanceManager.getDefault(SignalHeadManager.class).getByUserName(name);
    }

    @Override
    protected String getMasterClassName() {
        return SignalHeadTableAction.class.getName();
    }

    @Override
        public void clickOn(SignalHead t) {
    }

    /**
     * Set column width.
     *
     * @return a button to fit inside the VALUE column
     */
    @Override
    public JButton configureButton() {
        // pick a large size
        JButton b = new JButton(Bundle.getMessage("SignalHeadStateYellow")); // about the longest Appearance string
        b.putClientProperty("JComponent.sizeVariant", "small");
        b.putClientProperty("JButton.buttonType", "square");
        return b;
    }

    @Override
    public boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().contains("Lit") || e.getPropertyName().contains("Held") || e.getPropertyName().contains("ValidStatesChanged")) {
            return true;
        } else {
            return super.matchPropertyName(e);
        }
    }

    @Override
    protected String getBeanType() {
        return Bundle.getMessage("BeanNameSignalHead");
    }

    /**
     * Respond to change from bean. Prevent Appearance change when
     * Signal Head is set to Hold or Unlit.
     *
     * @param e A property change of any bean
     */
    @Override
    // Might be useful to show only a Dark option in the comboBox if head is Held
    // At present, does not work/change when head Lit/Held checkboxes are (de)activated
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (!e.getPropertyName().contains("Lit") || e.getPropertyName().contains("Held") || e.getPropertyName().contains("ValidStatesChanged")) {
            if (e.getSource() instanceof NamedBean) {
                String name = ((NamedBean) e.getSource()).getSystemName();
                if (log.isDebugEnabled()) {
                    log.debug("Update cell {}, {} for {}", sysNameList.indexOf(name), VALUECOL, name);
                }
                // since we can add columns, the entire row is marked as updated
                int row = sysNameList.indexOf(name);
                this.fireTableRowsUpdated(row, row);
                clearAppearanceVector(row); // activate this method below
            }
        }
        super.propertyChange(e);
    }

    /**
     * Customize the SignalHead Value (Appearance) column to show an
     * appropriate ComboBox of available Appearances when the
     * TableDataModel is being called from ListedTableAction.
     *
     * @param table a JTable of Signal Head
     */
    @Override
    protected void configValueColumn(JTable table) {
        // have the value column hold a JPanel with a JComboBox for Appearances
        setColumnToHoldButton(table, VALUECOL, configureButton());
        // add extras, override BeanTableDataModel
        log.debug("Head configValueColumn (I am {})", super.toString());
        table.setDefaultEditor(RowComboBoxPanel.class, new AppearanceComboBoxPanel());
        table.setDefaultRenderer(RowComboBoxPanel.class, new AppearanceComboBoxPanel()); // use same class for the renderer
        // Set more things?
    }

    /**
     * A row specific Appearance combobox cell editor/renderer.
     */
    class AppearanceComboBoxPanel extends RowComboBoxPanel {
        @Override
        protected final void eventEditorMousePressed() {
            this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add editorBox to JPanel
            this.editor.revalidate();
            SwingUtilities.invokeLater(this.comboBoxFocusRequester);
            log.debug("eventEditorMousePressed in row: {})", this.currentRow);
        }

        /**
         * Call the method in the surrounding method for the
         * SignalHeadTable.
         *
         * @param row the user clicked on in the table
         * @return an appropriate combobox for this signal head
         */
        @Override
        protected JComboBox<String> getEditorBox(int row) {
            return getAppearanceEditorBox(row);
        }
    }

    /**
     * Clear the old appearance comboboxes and force them to be rebuilt.
     * Used with the Single Output Signal Head to capture reconfiguration.
     *
     * @param row Index of the signal mast (in TableDataModel) to be
     *            rebuilt in the Hashtables
     */
    public void clearAppearanceVector(int row) {
        boxMap.remove(this.getValueAt(row, SYSNAMECOL));
        editorMap.remove(this.getValueAt(row, SYSNAMECOL));
    }

    // Hashtables for Editors; not used for Renderer)
    /**
     * Provide a JComboBox element to display inside the JPanel
     * CellEditor. When not yet present, create, store and return a new
     * one.
     *
     * @param row Index number (in TableDataModel)
     * @return A combobox containing the valid appearance names for this
     *         mast
     */
    public JComboBox<String> getAppearanceEditorBox(int row) {
        JComboBox<String> editCombo = editorMap.get(this.getValueAt(row, SYSNAMECOL));
        if (editCombo == null) {
            // create a new one with correct appearances
            editCombo = new JComboBox<>(getRowVector(row));
            editorMap.put(this.getValueAt(row, SYSNAMECOL), editCombo);
        }
        return editCombo;
    }

    final Hashtable<Object, JComboBox<String>> editorMap = new Hashtable<>();

    /**
     * Get a list of all the valid appearances that have not been
     * disabled.
     *
     * @param head the name of the signal head
     * @return List of valid signal head appearance names
     */
    public Vector<String> getValidAppearances(SignalHead head) {
        // convert String[] validStateNames to Vector
        String[] app = head.getValidStateNames();
        Vector<String> v = new Vector<>();
        Collections.addAll(v, app);
        return v;
    }

    /**
     * Holds a Hashtable of valid appearances per signal head, used by
     * getEditorBox()
     *
     * @param row Index number (in TableDataModel)
     * @return The Vector of valid appearance names for this mast to
     *         show in the JComboBox
     */
    Vector<String> getRowVector(int row) {
        Vector<String> comboappearances = boxMap.get(this.getValueAt(row, SYSNAMECOL));
        if (comboappearances == null) {
            // create a new one with right appearance
            comboappearances = getValidAppearances((SignalHead) this.getValueAt(row, SYSNAMECOL));
            boxMap.put(this.getValueAt(row, SYSNAMECOL), comboappearances);
        }
        return comboappearances;
    }

    final Hashtable<Object, Vector<String>> boxMap = new Hashtable<>();

    // end of methods to display VALUECOL ComboBox

    private SignalHeadAddEditFrame editFrame = null;

    private void editSignal(@Nonnull final SignalHead head) {
        // Signal Head was found, initialize for edit
        log.debug("editPressed started for {}", head.getSystemName());
        // create the Edit Signal Head Window
        // Use separate Runnable so window is created on top
        Runnable t = () -> makeEditSignalWindow(head);
        javax.swing.SwingUtilities.invokeLater(t);
    }

    private void makeEditSignalWindow(@Nonnull final SignalHead head) {
        if (editFrame == null) {
            editFrame = new SignalHeadAddEditFrame(head){
                @Override
                public void dispose() {
                    editFrame = null;
                    super.dispose();
                }
            };
            editFrame.initComponents();
        } else {
            if (head.equals(editFrame.getSignalHead())) {
                editFrame.setVisible(true);
            } else {
                log.error("Attempt to edit two signal heads at the same time-{}-and-{}-", editFrame.getSignalHead(), head.getSystemName());
                String msg = Bundle.getMessage("WarningEdit", editFrame.getSignalHead(), head.getSystemName());
                jmri.util.swing.JmriJOptionPane.showMessageDialog(editFrame, msg,
                        Bundle.getMessage("WarningTitle"), jmri.util.swing.JmriJOptionPane.ERROR_MESSAGE);
                editFrame.setVisible(true);
            }
        }
    }

    @Override
    public void dispose(){
        if ( editFrame != null ) {
            editFrame.dispose();
            editFrame = null;
        }
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalHeadTableModel.class);
}
