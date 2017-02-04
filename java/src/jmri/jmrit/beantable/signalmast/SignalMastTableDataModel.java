package jmri.jmrit.beantable.signalmast;

import java.awt.Component;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalMast;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.beantable.RowComboBoxPanel; // access to RowComboBoxPanel()
import jmri.jmrit.beantable.SignalMastTableAction.MyComboBoxEditor; // deprecated
import jmri.jmrit.beantable.SignalMastTableAction.MyComboBoxRenderer; // deprecated
import jmri.jmrit.signalling.SignallingSourceAction;
import jmri.util.swing.XTableColumnModel; // deprecated
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data model for a SignalMastTable
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2009
 * @author	Egbert Broerse Copyright (C) 2016
 */
public class SignalMastTableDataModel extends BeanTableDataModel {

    static public final int EDITMASTCOL = NUMCOLUMN;
    static public final int EDITLOGICCOL = EDITMASTCOL + 1;
    static public final int LITCOL = EDITLOGICCOL + 1;
    static public final int HELDCOL = LITCOL + 1;

    public String getValue(String name) {
        SignalMast sm = InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName(name);
        if (sm != null) {
            return sm.getAspect();
        } else {
            return null;
        }
    }

    public int getColumnCount() {
        return NUMCOLUMN + 4;
    }

    @Override
    public String getColumnName(int col) {
        if (col == VALUECOL) {
            return Bundle.getMessage("LabelAspectType");
        } else if (col == EDITMASTCOL) {
            return ""; // override default, no title for Edit column
        } else if (col == EDITLOGICCOL) {
            return ""; // override default, no title for Edit Logic column
        } else if (col == LITCOL) {
            return Bundle.getMessage("ColumnHeadLit");
        } else if (col == HELDCOL) {
            return Bundle.getMessage("ColumnHeadHeld");
        } else {
            return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == VALUECOL) {
            return RowComboBoxPanel.class; // Use a JPanel containing a custom Aspect ComboBox
        } else if (col == EDITMASTCOL) {
            return JButton.class;
        } else if (col == EDITLOGICCOL) {
            return JButton.class;
        } else if (col == LITCOL) {
            return Boolean.class;
        } else if (col == HELDCOL) {
            return Boolean.class;
        } else {
            return super.getColumnClass(col);
        }
    }

    public int getPreferredWidth(int col) {
        if (col == LITCOL) { // I18N use Bundle.getMessage() + length() for PreferredWidth size
            return new JTextField(Bundle.getMessage("ColumnHeadLit").length()).getPreferredSize().width;
        } else if (col == HELDCOL) {
            return new JTextField(Bundle.getMessage("ColumnHeadHeld").length()).getPreferredSize().width;
        } else if (col == EDITLOGICCOL) {
            return new JTextField(Bundle.getMessage("EditSignalLogicButton").length()).getPreferredSize().width;
        } else if (col == EDITMASTCOL) {
            return new JTextField(Bundle.getMessage("ButtonEdit").length()).getPreferredSize().width;
        } else {
            return super.getPreferredWidth(col);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == LITCOL) {
            return true;
        } else if (col == EDITLOGICCOL) {
            return true;
        } else if (col == EDITMASTCOL) {
            return true;
        } else if (col == HELDCOL) {
            return true;
        } else {
            return super.isCellEditable(row, col);
        }
    }

    protected Manager getManager() {
        return InstanceManager.getDefault(jmri.SignalMastManager.class);
    }

    protected NamedBean getBySystemName(String name) {
        return InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName(name);
    }

    protected NamedBean getByUserName(String name) {
        return InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName(name);
    }

    protected String getMasterClassName() {
        return getClassName();
    }

    protected void clickOn(NamedBean t) {
    }

    @Override
    public Object getValueAt(int row, int col) {
        // some error checking
        if (row >= sysNameList.size()) {
            log.debug("row index is greater than name list");
            return "error";
        }
        String name = sysNameList.get(row);
        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName(name);
        if (s == null) {
            return Boolean.valueOf(false); // if due to race condition, the device is going away
        }
        if (col == LITCOL) {
            boolean val = s.getLit();
            return Boolean.valueOf(val);
        } else if (col == HELDCOL) {
            boolean val = s.getHeld();
            return Boolean.valueOf(val);
        } else if (col == EDITLOGICCOL) {
            return Bundle.getMessage("EditSignalLogicButton");
        } else if (col == EDITMASTCOL) {
            return Bundle.getMessage("ButtonEdit");
        } else if (col == VALUECOL) {
            try {
                return s.getAspect().toString();
            } catch (java.lang.NullPointerException e) {
                //Aspect not set
                log.debug("Aspect for mast {} not set", row);
                return Bundle.getMessage("BeanStateUnknown"); // use place holder string in table
            }
        } else {
            return super.getValueAt(row, col);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        String name = sysNameList.get(row);
        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName(name);
        if (s == null) {
            return;  // device is going away anyway
        }
        if (col == VALUECOL) {
            if ((String) value != null) {
                //row = table.convertRowIndexToModel(row); // find the right row in model instead of table (not needed here)
                log.debug("setValueAt (rowConverted={}; value={})", row, value);
                s.setAspect((String) value);
                fireTableRowsUpdated(row, row);
            }
        } else if (col == LITCOL) {
            boolean b = ((Boolean) value).booleanValue();
            s.setLit(b);
        } else if (col == HELDCOL) {
            boolean b = ((Boolean) value).booleanValue();
            s.setHeld(b);
        } else if (col == EDITLOGICCOL) {
            editLogic(row, col);
        } else if (col == EDITMASTCOL) {
            editMast(row, col);
        } else {
            super.setValueAt(value, row, col);
        }
    }

    void editLogic(int row, int col) {
        class WindowMaker implements Runnable {

            int row;

            WindowMaker(int r) {
                row = r;
            }

            public void run() {
                SignallingSourceAction action = new SignallingSourceAction(Bundle.getMessage("TitleSignalMastLogicTable"), (SignalMast) getBySystemName(sysNameList.get(row)));
                action.actionPerformed(null);
            }
        }
        WindowMaker t = new WindowMaker(row);
        javax.swing.SwingUtilities.invokeLater(t);
    }

    void editMast(int row, int col) {
        class WindowMaker implements Runnable {

            int row;

            WindowMaker(int r) {
                row = r;
            }

            public void run() {
                AddSignalMastJFrame editFrame = new jmri.jmrit.beantable.signalmast.AddSignalMastJFrame((SignalMast) getBySystemName(sysNameList.get(row)));
                editFrame.setVisible(true);
            }
        }
        WindowMaker t = new WindowMaker(row);
        javax.swing.SwingUtilities.invokeLater(t);
    }

    /**
     * Does not appear to be used.
     * 
     * @param srtr a table model
     * @return a new table
     * @deprecated since 4.5.4 without direct replacement
     */
    @Deprecated
    public JTable makeJTable(TableModel srtr) {
        JTable table = new SignalMastJTable(srtr);

        table.getTableHeader().setReorderingAllowed(true);
        table.setColumnModel(new XTableColumnModel());
        table.createDefaultColumnsFromModel();

        addMouseListenerToHeader(table);
        return table;
    }

    /**
     * @deprecated since 4.5.4; replaced by SignalMastTableAction.createModel()
     * or invoke SignalMastTableDataModel instance directly from ListedTableFrame()
     */
    @Deprecated
    //The JTable is extended so that we can reset the available aspect in the drop down when required
    class SignalMastJTable extends JTable {

        /** 
         * @param srtr a table model.
         */
        public SignalMastJTable(TableModel srtr) {
            super(srtr);
        }

        /**
         * @deprecated since 4.5.7
         */
        public void clearAspectVector(int row) {
            // Clear the old aspect combobox and force it to be rebuilt
            log.debug("clearAspectVector (remove row={})", row);
            boxMap.remove(getModel().getValueAt(row, SYSNAMECOL));
            editorMap.remove(getModel().getValueAt(row, SYSNAMECOL));
            rendererMap.remove(getModel().getValueAt(row, SYSNAMECOL));
        }

        public TableCellRenderer getCellRenderer(int row, int column) {
            if (column == VALUECOL) {
                return getRenderer(row);
            } else {
                return super.getCellRenderer(row, column);
            }
        }

        public TableCellEditor getCellEditor(int row, int column) {
            if (column == VALUECOL) {
                return getEditor(row);
            } else {
                return super.getCellEditor(row, column);
            }
        }

        TableCellRenderer getRenderer(int row) {
            TableCellRenderer retval = rendererMap.get(getModel().getValueAt(row, SYSNAMECOL));
            if (retval == null) {
                // create a new one with right aspects
                retval = new MyComboBoxRenderer(getAspectVector(row));
                rendererMap.put(getModel().getValueAt(row, SYSNAMECOL), retval);
            }
            return retval;
        }
        Hashtable<Object, TableCellRenderer> rendererMap = new Hashtable<Object, TableCellRenderer>();

        TableCellEditor getEditor(int row) {
            TableCellEditor retval = editorMap.get(getModel().getValueAt(row, SYSNAMECOL));
            if (retval == null) {
                // create a new one with right aspects
                retval = new MyComboBoxEditor(getAspectVector(row));
                editorMap.put(getModel().getValueAt(row, SYSNAMECOL), retval);
            }
            return retval;
        }
        Hashtable<Object, TableCellEditor> editorMap = new Hashtable<Object, TableCellEditor>();

        Vector<String> getAspectVector(int row) {
            Vector<String> retval = boxMap.get(getModel().getValueAt(row, SYSNAMECOL));
            if (retval == null) {
                // create a new one with right aspects
                Vector<String> v = InstanceManager.getDefault(jmri.SignalMastManager.class)
                        .getSignalMast((String) getModel().getValueAt(row, SYSNAMECOL)).getValidAspects();
                retval = v;
                boxMap.put(getModel().getValueAt(row, SYSNAMECOL), retval);
            }
            return retval;
        }

        Hashtable<Object, Vector<String>> boxMap = new Hashtable<Object, Vector<String>>();
    }
    // end of deprecated class

    protected String getBeanType() {
        return Bundle.getMessage("BeanNameSignalMast");
    }

    /**
    * Respond to change from bean.
    */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().indexOf("aspectEnabled") >= 0 || e.getPropertyName().indexOf("aspectDisabled") >= 0) {
            if (e.getSource() instanceof NamedBean) {
                String name = ((NamedBean) e.getSource()).getSystemName();
                if (log.isDebugEnabled()) {
                    log.debug("Update cell {}, {} for {}", sysNameList.indexOf(name), VALUECOL, name);
                }
                // since we can add columns, the entire row is marked as updated
                int row = sysNameList.indexOf(name);
                this.fireTableRowsUpdated(row, row);
                clearAspectVector(row);
            }
        }
        super.propertyChange(e);
    }

    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().indexOf("Aspect") >= 0 || e.getPropertyName().indexOf("Lit") >= 0
                || e.getPropertyName().indexOf("Held") >= 0 || e.getPropertyName().indexOf("aspectDisabled") >= 0
                || e.getPropertyName().indexOf("aspectEnabled") >= 0) {

            return true;
        }
        return super.matchPropertyName(e);
    }

    /**
     * Customize the SignalMast Value (Aspect) column to show an appropriate ComboBox of available Aspects
     * when the TableDataModel is being called from ListedTableAction.
     * @param table a JTable of Signal Masts
     */
    @Override
    protected void configValueColumn(JTable table) {
        // have the value column hold a JPanel with a JComboBox for Aspects
        setColumnToHoldButton(table, VALUECOL, configureButton());
        // add extras, override BeanTableDataModel
        log.debug("Mast configValueColumn (I am {})", super.toString());
        table.setDefaultEditor(RowComboBoxPanel.class, new AspectComboBoxPanel()); // provide BeanTableDataModel
        table.setDefaultRenderer(RowComboBoxPanel.class, new AspectComboBoxPanel()); // create a separate class for the renderer
        // Set more things?
    }

    /**
     * Set column width.
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
         * Call method getApectEditorBox() in the surrounding method for the SignalMastTable
         * @param row Index of the row clicked in the table
         * @return an appropriate combobox for this signal mast
         */
        @Override
        protected JComboBox getEditorBox(int row) {
            return getApectEditorBox(row);
        }

    }

    // Methods to display VALUECOL (aspect) ComboBox in the Signal Mast Table
    // Derived from the SignalMastJTable class (deprecated since 4.5.5):
    // All row values are in terms of the Model, not the Table as displayed.

    /**
     * Clear the old aspect comboboxes and force them to be rebuilt
     * @param row Index of the signal mast (in TableDataModel) to be rebuilt in the Hashtables
     */
    public void clearAspectVector(int row) {
        boxMap.remove(this.getValueAt(row, SYSNAMECOL));
        editorMap.remove(this.getValueAt(row, SYSNAMECOL));
    }

    // Hashtables for Editors; not used for Renderer)

    /**
     * Provide a JComboBox element to display inside the JPanel CellEditor.
     * When not yet present, create, store and return a new one.
     * @param row Index number (in TableDataModel)
     * @return A combobox containing the valid aspect names for this mast
     */
    JComboBox getApectEditorBox(int row) {
        JComboBox editCombo = editorMap.get(this.getValueAt(row, SYSNAMECOL));
        if (editCombo == null) {
            // create a new one with correct aspects
            editCombo = new JComboBox<String> (getAspectVector(row));
            editorMap.put(this.getValueAt(row, SYSNAMECOL), editCombo);
        }
        return editCombo;
    }
    Hashtable<Object, JComboBox> editorMap = new Hashtable<Object, JComboBox>();

    /**
     * Holds a Hashtable of valid aspects per signal mast
     * used by getEditorBox()
     * @param row Index number (in TableDataModel)
     * @return The Vector of valid aspect names for this mast to show in the JComboBox
     */
    Vector<String> getAspectVector(int row) {
        Vector<String> comboaspects = boxMap.get(this.getValueAt(row, SYSNAMECOL));
        if (comboaspects == null) {
            // create a new one with right aspects
            Vector<String> v = InstanceManager.getDefault(jmri.SignalMastManager.class)
                    .getSignalMast((String) this.getValueAt(row, SYSNAMECOL)).getValidAspects();
            comboaspects = v;
            boxMap.put(this.getValueAt(row, SYSNAMECOL), comboaspects);
        }
        return comboaspects;
    }

    Hashtable<Object, Vector<String>> boxMap = new Hashtable<Object, Vector<String>>();

    // end of methods to display VALUECOL (Aspect) ComboBox

    protected String getClassName() {
        return jmri.jmrit.beantable.SignalMastTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleSignalMastTable");
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastTableDataModel.class.getName());

}
