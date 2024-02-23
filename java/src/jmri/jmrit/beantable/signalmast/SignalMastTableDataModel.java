package jmri.jmrit.beantable.signalmast;

import java.util.HashMap;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.beantable.RowComboBoxPanel;
import jmri.jmrit.signalling.SignallingSourceAction;

/**
 * Data model for a SignalMastTable
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2009
 * @author Egbert Broerse Copyright (C) 2016
 */
public class SignalMastTableDataModel extends BeanTableDataModel<SignalMast> {

    public static final int EDITMASTCOL = NUMCOLUMN;
    public static final int EDITLOGICCOL = EDITMASTCOL + 1;
    public static final int LITCOL = EDITLOGICCOL + 1;
    public static final int HELDCOL = LITCOL + 1;

    @Override
    public String getValue(String name) {
        SignalMast sm = InstanceManager.getDefault(SignalMastManager.class).getBySystemName(name);
        if (sm != null) {
            return sm.getAspect();
        } else {
            return null;
        }
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN + 4;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case VALUECOL:
                return Bundle.getMessage("LabelAspectType");
            case EDITMASTCOL:
                return ""; // override default, no title for Edit column
            case EDITLOGICCOL:
                return ""; // override default, no title for Edit Logic column
            case LITCOL:
                return Bundle.getMessage("ColumnHeadLit");
            case HELDCOL:
                return Bundle.getMessage("ColumnHeadHeld");
            default:
                return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case VALUECOL:
                return RowComboBoxPanel.class; // Use a JPanel containing a custom Aspect ComboBox
            case EDITMASTCOL:
            case EDITLOGICCOL:
                return JButton.class;
            case LITCOL:
            case HELDCOL:
                return Boolean.class;
            default:
                return super.getColumnClass(col);
        }
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case LITCOL:
                // I18N use Bundle.getMessage() + length() for PreferredWidth size
                return new JTextField(Bundle.getMessage("ColumnHeadLit").length()).getPreferredSize().width;
            case HELDCOL:
                return new JTextField(Bundle.getMessage("ColumnHeadHeld").length()).getPreferredSize().width;
            case EDITLOGICCOL:
                return new JTextField(Bundle.getMessage("EditSignalLogicButton").length()).getPreferredSize().width;
            case EDITMASTCOL:
                return new JTextField(Bundle.getMessage("ButtonEdit").length()).getPreferredSize().width;
            default:
                return super.getPreferredWidth(col);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case LITCOL:
            case EDITLOGICCOL:
            case EDITMASTCOL:
            case HELDCOL:
                return true;
            default:
                return super.isCellEditable(row, col);
        }
    }

    @Override
    protected Manager<SignalMast> getManager() {
        return InstanceManager.getDefault(SignalMastManager.class);
    }

    @Override
    protected SignalMast getBySystemName(@Nonnull String name) {
        return InstanceManager.getDefault(SignalMastManager.class).getBySystemName(name);
    }

    @Override
    protected SignalMast getByUserName(@Nonnull String name) {
        return InstanceManager.getDefault(SignalMastManager.class).getByUserName(name);
    }

    @Override
    protected String getMasterClassName() {
        return getClassName();
    }

    @Override
    protected void clickOn(SignalMast t) {
        log.debug("No action for click on {}",t.getDisplayName());
    }

    @Override
    public Object getValueAt(int row, int col) {
        // some error checking
        if (row >= sysNameList.size()) {
            log.debug("row index is greater than name list");
            return "error";
        }
        String name = sysNameList.get(row);
        SignalMast s = InstanceManager.getDefault(SignalMastManager.class).getBySystemName(name);
        if (s == null) {
            return false; // if due to race condition, the device is going away
        }
        switch (col) {
            case LITCOL:
                return s.getLit();
            case HELDCOL:
                return s.getHeld();
            case EDITLOGICCOL:
                return Bundle.getMessage("EditSignalLogicButton");
            case EDITMASTCOL:
                return Bundle.getMessage("ButtonEdit");
            case VALUECOL:
                String aspect = s.getAspect();
                if ( aspect != null) {
                    return aspect;
                } else {
                    //Aspect not set,  - too verbose, even at trace
                    //log.trace("Aspect not set, NULL aspect returned for mast in row {}", row);
                    return Bundle.getMessage("BeanStateUnknown"); // use place holder string in table
                }
            default:
                return super.getValueAt(row, col);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        String name = sysNameList.get(row);
        SignalMast s = InstanceManager.getDefault(SignalMastManager.class).getBySystemName(name);
        if (s == null) {
            return;  // device is going away anyway
        }
        switch (col) {
            case VALUECOL:
                if ((String) value != null) {
                    log.debug("setValueAt (rowConverted={}; value={})", row, value);
                    s.setAspect((String) value);
                    fireTableRowsUpdated(row, row);
                }
                break;
            case LITCOL: {
                boolean b = ((Boolean) value);
                s.setLit(b);
                break;
            }
            case HELDCOL: {
                boolean b = ((Boolean) value);
                s.setHeld(b);
                break;
            }
            case EDITLOGICCOL:
                editLogic(row, col);
                break;
            case EDITMASTCOL:
                editMast(row, col);
                break;
            default:
                super.setValueAt(value, row, col);
                break;
        }
    }

    void editLogic(int row, int col) {
        SwingUtilities.invokeLater(() -> {
            SignallingSourceAction action = new SignallingSourceAction(Bundle.getMessage(
                "TitleSignalMastLogicTable"), getBySystemName(sysNameList.get(row)));
            action.actionPerformed(null);
        });
    }

    void editMast(int row, int col) {
        SwingUtilities.invokeLater(() -> {
            AddSignalMastJFrame editFrame = new AddSignalMastJFrame(getBySystemName(sysNameList.get(row)));
            editFrame.setVisible(true);
        });
    }

    /**
     * Respond to change from bean.
     *
     * @param e the change event to respond to
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ( (e.getPropertyName().contains("aspectEnabled") || e.getPropertyName().contains("aspectDisabled"))
            && (e.getSource() instanceof NamedBean ) ) {

            String name = ((NamedBean) e.getSource()).getSystemName();
            if (log.isDebugEnabled()) {
                log.debug("Update cell {}, {} for {}", sysNameList.indexOf(name), VALUECOL, name);
            }
            // since we can add columns, the entire row is marked as updated
            int row = sysNameList.indexOf(name);
            this.fireTableRowsUpdated(row, row);
            clearAspectVector(row);
        }
        super.propertyChange(e);
    }

    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().contains("Aspect") || e.getPropertyName().contains("Lit")
                || e.getPropertyName().contains("Held") || e.getPropertyName().contains("aspectDisabled")
                || e.getPropertyName().contains("aspectEnabled")) {

            return true;
        }
        return super.matchPropertyName(e);
    }

    /**
     * Customize the SignalMast Value (Aspect) column to show an appropriate
     * ComboBox of available Aspects when the TableDataModel is being called
     * from ListedTableAction.
     *
     * @param table a JTable of Signal Masts
     */
    @Override
    protected void configValueColumn(JTable table) {
        // have the value column hold a JPanel with a JComboBox for Aspects
        setColumnToHoldButton(table, VALUECOL, configureButton());
        // add extras, override BeanTableDataModel
        log.debug("Mast configValueColumn (I am {})", super.toString());
        table.setDefaultEditor(RowComboBoxPanel.class, new AspectComboBoxPanel());
        // create a separate class for the renderer
        table.setDefaultRenderer(RowComboBoxPanel.class, new AspectComboBoxPanel());
        // Set more things?
    }

    /**
     * Set column width.
     *
     * @return a button to fit inside the VALUE column
     */
    @Override
    public JButton configureButton() {
        // pick a large size
        JButton b = new JButton("Diverging Approach Medium"); // about the longest Aspect string
        b.putClientProperty("JComponent.sizeVariant", "small");
        b.putClientProperty("JButton.buttonType", "square");
        return b;
    }

    /**
     * A row specific Aspect combobox cell editor/renderer
     */
    public class AspectComboBoxPanel extends RowComboBoxPanel {

        @Override
        protected final void eventEditorMousePressed() {
            this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add eb to JPanel
            this.editor.revalidate();
            SwingUtilities.invokeLater(this.comboBoxFocusRequester);
            log.debug("eventEditorMousePressed in row: {}; me = {})", this.currentRow, this.toString());
        }

        /**
         * Call method getAspectEditorBox() in the surrounding method for the SignalMastTable.
         * @param row Index of the row clicked in the table
         * @return an appropriate combobox for this signal mast
         */
        @Override
        protected JComboBox<String> getEditorBox(int row) {
            return getAspectEditorBox(row);
        }

    }

    /**
     * Clear the old aspect comboboxes and force them to be rebuilt
     *
     * @param row Index of the signal mast (in TableDataModel) to be rebuilt in
     *            the HashMaps
     */
    public void clearAspectVector(int row) {
        boxMap.remove(this.getValueAt(row, SYSNAMECOL));
        editorMap.remove(this.getValueAt(row, SYSNAMECOL));
    }

    // HashMaps for Editors; not used for Renderer)
    /**
     * Provide a JComboBox element to display inside the JPanel CellEditor. When
     * not yet present, create, store and return a new one.
     *
     * @param row Index number (in TableDataModel)
     * @return A combobox containing the valid aspect names for this mast
     */
    JComboBox<String> getAspectEditorBox(int row) {
        JComboBox<String> editCombo = editorMap.get(this.getValueAt(row, SYSNAMECOL));
        if (editCombo == null) {
            // create a new one with correct aspects
            editCombo = new JComboBox<>(getAspectVector(row));
            editorMap.put(this.getValueAt(row, SYSNAMECOL), editCombo);
        }
        return editCombo;
    }
    HashMap<Object, JComboBox<String>> editorMap = new HashMap<>();

    /**
     * Holds a HashMap of valid aspects per signal mast used by getEditorBox()
     *
     * @param row Index number (in TableDataModel)
     * @return The Vector of valid aspect names for this mast to show in the
     *         JComboBox
     */
    Vector<String> getAspectVector(int row) {
        Vector<String> comboaspects = boxMap.get(this.getValueAt(row, SYSNAMECOL));
        if (comboaspects == null) {
            // create a new one with right aspects
            Vector<String> v = ((SignalMast)this.getValueAt(row, SYSNAMECOL)).getValidAspects();
            comboaspects = v;
            boxMap.put(this.getValueAt(row, SYSNAMECOL), comboaspects);
        }
        return comboaspects;
    }

    HashMap<Object, Vector<String>> boxMap = new HashMap<>();

    // end of methods to display VALUECOL (Aspect) ComboBox
    protected String getClassName() {
        return jmri.jmrit.beantable.SignalMastTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleSignalMastTable");
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalMastTableDataModel.class);

}
