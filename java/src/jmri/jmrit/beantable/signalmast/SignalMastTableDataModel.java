package jmri.jmrit.beantable.signalmast;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalMast;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.beantable.SignalMastTableAction.MyComboBoxEditor; // @Deprecated
import jmri.jmrit.beantable.SignalMastTableAction.MyComboBoxRenderer; // @Deprecated
import jmri.jmrit.signalling.SignallingSourceAction;
import jmri.util.swing.XTableColumnModel;
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

    public Class<?> getColumnClass(int col) {
        if (col == VALUECOL) {
            return JPanel.class; // Use a JPanel containing a custom Aspect ComboBox
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
        if (col == LITCOL) { // TODO I18N use Bundle.getMessage() + length() for PreferredWidth size
            return new JTextField(Bundle.getMessage("ColumnHeadLit").length()).getPreferredSize().width;
        } else if (col == HELDCOL) {
            return new JTextField(Bundle.getMessage("ColumnHeadHeld").length()).getPreferredSize().width;
        } else if (col == EDITLOGICCOL) {
            return new JTextField(Bundle.getMessage("ButtonEdit").length()).getPreferredSize().width;
        } else if (col == EDITMASTCOL) {
            return new JTextField(Bundle.getMessage("EditSignalLogicButton").length()).getPreferredSize().width;
        } else {
            return super.getPreferredWidth(col);
        }
    }

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
            log.debug("row is greater than name list");
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
                //row = table.convertRowIndexToModel(row); // find the right row in model instead of table, not needed
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
     * @param srtr a table model
     * @return a new SignalMastJTable
     * @deprecated since 4.5.4; since 4.5.7 use SignalMastTableAction.createModel() or invoke directly from ListedTableFrame()
     */
    @Deprecated
    //The JTable is extended so that we can reset the available aspect in the drop down when required
    class SignalMastJTable extends JTable {

        public SignalMastJTable(TableModel srtr) {
            super(srtr);
        }

        public void clearAspectVector(int row) {
            // no longer called as of 4.5.7
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
    * respond to change from bean
    */
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
     * Table cell editor with a custom ComboBox per row as the editing component.
     * used as TableCellRenderer from JTable, declared in @see ConfigValueColumn().
     * Based on: http://alvinalexander.com/java/jwarehouse/netbeans-src/monitor/src/org/netbeans/modules/web/monitor/client/ComboBoxTableCellEditor.java.shtml
     * @author Egbert Broerse 2016
     * @since 4.5.7
     */
    public class      AspectComboBoxPanel
            extends    DefaultCellEditor
            implements TableCellEditor, TableCellRenderer {

        /**
         * The surrounding panel for the combobox.
         */
        private JPanel editor;

        /**
         * The surrounding panel for the combobox.
         */
        private JPanel renderer;

        /**
         * Listeners for the table added?
         */
        private boolean tableListenerAdded = false;

        /**
         * The table.
         */
        private JTable table;

        /**
         *  To request the focus for the combobox (with SwingUtilities.invokeLater())
         */
        private Runnable comboBoxFocusRequester;

        /**
         *  To popup the combobox (with SwingUtilities.invokeLater())
         */
        private Runnable comboBoxOpener;

        /**
         *  The current row.
         */
        private int currentRow = -1;

        /**
         *  The previously selected value in the editor.
         */
        private Object prevItem;

        /**
         *  React on action events on the combobox?
         */
        private boolean consumeComboBoxActionEvent = true;

        /**
         *  The event that causes the editing to start. We need it to know
         *  if we should open the popup automatically.
         */
        private EventObject startEditingEvent = null;


        /**
         *  Create a new CellEditor
         */
        public AspectComboBoxPanel(Object [] values,
                                   ListCellRenderer customRenderer) {
            super (new JComboBox());
            //setItems (values);
            // to be filled from HashMap
            this.editor = new JPanel(new BorderLayout ());
            this.renderer = new JPanel(new BorderLayout ()); // move to its own class?
            setClickCountToStart(1); // value for a DefaultCellEditor

            //show the combobox if the mouse clicks at the panel
            this.editor.addMouseListener (new MouseAdapter ()
            {
                public final void mousePressed (MouseEvent evt)
                {
                    eventEditorMousePressed ();
                }
            });

        }

        public AspectComboBoxPanel(Object [] values) {
            this (values, null);
        }

        public AspectComboBoxPanel () {
            this(new Object [0], null);
        } // as defined in configValueColumn()

        public AspectComboBoxPanel (ListCellRenderer customRenderer) {
            this (new Object [0], customRenderer);
        }

        /**
         * Returns the editor component of the cell.
         * @param table JTable of SignalMastTable
         * @param isSelected tells if this row is selected in the table
         * @param row in table
         * @param column in table, in this case Value (Aspect)
         * @returns JPanel as CellEditor
         */
        @Override
        public final Component getTableCellEditorComponent (JTable  table,
                                                            Object  value,
                                                            boolean isSelected,
                                                            int     row,
                                                            int     col)
        {
            //add a listener to the table
            if  ( ! this.tableListenerAdded) {
                this.tableListenerAdded = true;
                this.table = table;
                this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener ()
                {
                    public final void valueChanged(ListSelectionEvent evt)
                    {
                        eventTableSelectionChanged ();
                    }
                });
            }
            this.currentRow = row;
            updateData(row, true, table);
            return getEditorComponent(table, value, isSelected, row, col);
        }

        protected Component getEditorComponent(JTable  table,
                                                Object  value,
                                                boolean isSelected,
                                                int     row,
                                                int     col)
        {
            //new or old row? > should be cleaned up, leave our isSelected argument?
            isSelected = table.isRowSelected(row);
            if  (isSelected) {
                //old row
                log.debug("getEditorComponent>isSelected (value={})", value);
            }
            //the user selected a new row (or initially no row was selected)
            this.editor.removeAll();  // remove the combobox from the panel
            JComboBox editorbox = getEditorBox(table.convertRowIndexToModel(row));
            log.debug("getRendererComponent>notSelected (row={}, value={})", row, value);
            if (value != null) {
                editorbox.setSelectedItem(value); // display current Aspect
            }
            editorbox.addActionListener(new ActionListener ()
            {
                public final void actionPerformed(ActionEvent evt) {
                    Object choice = editorbox.getSelectedItem();
                    log.debug("actionPerformed (event={}, choice={}", evt.toString(), choice.toString());
                    if (choice != null) {
                        eventAspectComboBoxActionPerformed(choice); // try a special method for this source
                    }
                }
            });
            this.editor.add(editorbox);
            return this.editor;
        }

        @Override
        public final Component getTableCellRendererComponent (JTable  table,
                                                            Object  value,
                                                            boolean isSelected,
                                                            boolean hasFocus,
                                                            int     row,
                                                            int     col)
        {
            //add a listener to the table
            if  ( ! this.tableListenerAdded) {
                this.tableListenerAdded = true;
                this.table = table;
                this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener ()
                {
                    public final void valueChanged(ListSelectionEvent evt)
                    {
                        eventTableSelectionChanged ();
                    }
                });
            }

            this.currentRow = row;
            return getRendererComponent(table, value, isSelected, hasFocus, row, col); // OK to call getEditorComponent() instead?
        }

        protected Component getRendererComponent(JTable  table,
                                               Object  value,
                                               boolean isSelected,
                                               boolean hasFocus,
                                               int     row,
                                               int     col)
        {
            this.renderer.removeAll();  //remove the combobox from the panel
            JComboBox renderbox = new JComboBox(); // create a fake comboBox with the current Aspect of mast in this row
            log.debug("getRendererComponent (row={}, value={})", row, value);
            renderbox.addItem(value); // display (only) the current Aspect
            renderer.add(renderbox);
            return this.renderer;
        }

        private void updateData(int row, boolean isSelected, JTable table) {
            // get correct Aspects for ComboBox
            log.debug("updateData (row={})", row);
            JComboBox eb = getEditorBox(table.convertRowIndexToModel(row));
            this.editor.add(eb);
            if (isSelected) {
                editor.setBackground(table.getSelectionBackground());
            } else {
                editor.setBackground(table.getBackground());
            }
        }
        /**
         *  Is the cell editable? If the mouse was pressed at a margin
         *  we don't want the cell to be editable.
         *  @param  evt  The event-object.
         */
        public boolean isCellEditable(EventObject evt) {
            this.startEditingEvent = evt;
            if  (evt instanceof MouseEvent  &&  evt.getSource () instanceof JTable) {
                MouseEvent me = (MouseEvent) evt;
                JTable table = (JTable) me.getSource ();
                Point pt = new Point (me.getX (), me.getY ());
                int row = table.rowAtPoint (pt);
                int col = table.columnAtPoint (pt);
                Rectangle rec = table.getCellRect (row, col, false);
                if  (me.getY () >= rec.y + rec.height  ||  me.getX () >= rec.x + rec.width)
                {
                    return false;
                }
            }
            return super.isCellEditable(evt);
        }

        public Object getCellEditorValue() {
            log.debug("getCellEditorValue, prevItem= {})", prevItem);
            return prevItem;
        }

        final void eventEditorMousePressed() {
            this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add eb to JPanel
            this.editor.revalidate();
            SwingUtilities.invokeLater(this.comboBoxFocusRequester);
            log.debug("eventEditorMousePressed in row: {})", this.currentRow);
        }

        protected void eventTableSelectionChanged() {
            //stop editing if a new row is selected
            log.debug("eventTableSelectionChanged");
            if  ( ! this.table.isRowSelected(this.currentRow))
            {
                stopCellEditing ();
            }
        }

        /**
         * method for our own comboBox
         */
        protected void eventAspectComboBoxActionPerformed(@Nonnull Object choice) {
            Object item = choice;
            log.debug("eventAspectComboBoxActionPerformed; selected item={})", item);
            prevItem = choice; // passed as cell value
            if (consumeComboBoxActionEvent) stopCellEditing();
        }
        public final int getCurrentRow() {
            return this.currentRow;
        }
    }

    /**
     * Customize the SignalMast Value (Aspect) column to show an appropriate ComboBox of available Aspects
     * @param table a Jtable
     */
    @Override
    protected void configValueColumn(JTable table) {
        // have the value column hold a JPanel with a JComboBox for Aspects
        setColumnToHoldButton(table, VALUECOL, configureButton());
        // add extras, override BeanTableDataModel
        table.setDefaultEditor(JPanel.class, new AspectComboBoxPanel());
        table.setDefaultRenderer(JPanel.class, new AspectComboBoxPanel()); // create a separate class for the renderer
        // Set more things?
    }

    /**
     * set column width
     */
    @Override
    public JButton configureButton() {
        // pick a large size
        JButton b = new JButton("Diverging Approach Medium"); // about the longest Aspect string
        b.putClientProperty("JComponent.sizeVariant", "small");
        b.putClientProperty("JButton.buttonType", "square");
        return b;
    }

    // Methods to display VALUECOL (aspect) ComboBox in Signal Mast Table
    // Derived from the SignalMastJTable class (deprecated since 4.5.5):
    // All row values are in terms of the Model, not the Table as displayed.

    public void clearAspectVector(int row) {
        //Clear the old aspect combobox and force it to be rebuilt
        boxMap.remove(this.getValueAt(row, SYSNAMECOL));
        rendererMap.remove(this.getValueAt(row, SYSNAMECOL));
        editorMap.remove(this.getValueAt(row, SYSNAMECOL));
    }

    // we need two different Hashtables
    JComboBox getEditorBox(int row) {
        JComboBox editCombo = editorMap.get(this.getValueAt(row, SYSNAMECOL));
        if (editCombo == null) {
            // create a new one with correct aspects
            editCombo = new JComboBox(getAspectVector(row));
            editorMap.put(this.getValueAt(row, SYSNAMECOL), editCombo);
        }
        return editCombo;
    }
    Hashtable<Object, JComboBox> editorMap = new Hashtable<Object, JComboBox>();

    JComboBox getRendererBox(int row) {
        JComboBox renderCombo = rendererMap.get(this.getValueAt(row, SYSNAMECOL));
        log.debug("Combo row: {}", row);
        if (renderCombo == null) {
            // create a new one with correct aspects
            renderCombo = new JComboBox(getAspectVector(row));
            rendererMap.put(this.getValueAt(row, SYSNAMECOL), renderCombo);
        }
        return renderCombo;
    }
    Hashtable<Object, JComboBox> rendererMap = new Hashtable<Object, JComboBox>();

    /**
     * Holds a Hashtable of valid aspects per signal mast
     * used by getEditorBox()
     * @param int row number (in TableDataModel)
     * @returns Vector of aspect names for a JComboBox
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

    // end of methods to display VALUECOL ComboBox

    protected String getClassName() {
        return jmri.jmrit.beantable.SignalMastTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleSignalMastTable");
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastTableDataModel.class.getName());

}
