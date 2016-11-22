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
import javax.annotation.Nullable;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.RowSorter; // still in use?
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
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
        if (col == LITCOL) { // i18n use Bundle.length() for string size
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
                log.debug("Aspect for mast " + row + " not set");
                return Bundle.getMessage("BeanStateUnknown"); // replace by place holder string in table
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
                //row = table.convertRowIndexToModel(row); // find the right row in model instead of table
                System.out.println("setValueAt (rowConverted=" +row + "; value=" + value + ")");
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

    @Override
    public JTable makeJTable(@Nonnull String name, @Nonnull TableModel model, @Nullable RowSorter<? extends TableModel> sorter) {
        JTable table = new SignalMastJTable(model);
        table.setName(name);
        table.setRowSorter(sorter);
        table.getTableHeader().setReorderingAllowed(true);
        table.setColumnModel(new XTableColumnModel());
        table.createDefaultColumnsFromModel();

        addMouseListenerToHeader(table);
        return table;
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
     * @deprecated since 4.5.4, since 4.5.6 use SignalMastTableAction.createModel()
     */
    @Deprecated
    //The JTable is extended so that we can reset the available aspect in the drop down when required
    class SignalMastJTable extends JTable {

        public SignalMastJTable(TableModel srtr) {
            super(srtr);
        }

        public void clearAspectVector(int row) {
            // no longer called as of 4.5.6
            // Clear the old aspect combobox and forces it to be rebuilt
            System.out.println("clearAspectVector (remove row=" + row + ")");
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
                    log.debug("Update cell " + sysNameList.indexOf(name) + ","
                            + VALUECOL + " for " + name);
                }
                // since we can add columns, the entire row is marked as updated
                int row = sysNameList.indexOf(name);
                //clearAspectVector(row); // old
                this.fireTableRowsUpdated(row, row); // new
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
     * A table cell editor with a custom ComboBox per row as the editing component. Based on:
     * http://alvinalexander.com/java/jwarehouse/netbeans-src/monitor/src/org/netbeans/modules/web/
     * monitor/client/ComboBoxTableCellEditor.java.shtml
     * Called from
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
         *  The initial value of the editor.
         */
        private Object initialValue;

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
            setItems (values);
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

            JComboBox cb = getComboBox();

            cb.addActionListener (new ActionListener ()
            {
                public final void actionPerformed (ActionEvent evt) {
                    eventComboBoxActionPerformed ();
                }
            });

            this.comboBoxFocusRequester = new Runnable ()
            {
                public final void run ()
                {
                    getComboBox().requestFocus ();
                }
            };
            this.comboBoxOpener = new Runnable ()
            {
                public final void run ()
                {
                    if  (startEditingEvent instanceof MouseEvent)
                    {
                        try {
                            getComboBox().setPopupVisible (true);
                        }
                        catch (java.awt.IllegalComponentStateException ex)
                        {
                            //silently ignore - seems to be a bug in JTable
                        }
                    }
                }
            };

        }

        public AspectComboBoxPanel(Object [] values) {
            this (values, null);
        }

        public AspectComboBoxPanel () {
            this(new Object [0], null);
        } //

        public AspectComboBoxPanel (ListCellRenderer customRenderer) {
            this (new Object [0], customRenderer);
        }

        /**
         * Returns the renderer component of the cell.
         * @param table JTable of SinalMastTable
         * @param isSelected tells if this row is selected in the table
         * @param row in table
         * @param column in table, in this case Value (Aspect)
         * @returns JPanel as CellEditor
         * @interfaceMethod javax.swing.table.TableCellEditor
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
            this.initialValue = value;
            updateData(row, true, table);
            return getEditorComponent(table, value, isSelected, row, col);
        }

        protected Component getEditorComponent(JTable  table,
                                                Object  value,
                                                boolean isSelected,
                                                int     row,
                                                int     col)
        {
            // setSelectedItem(value);
            System.out.println("getEditorComponent (#556 row=" + row + ", value=" + value +")");
            //new or old row?
            isSelected = table.isRowSelected(row);
            if  (isSelected) { // there was a problem when selecting another cell in the selected row: ComboBox goes blank
                //old row
                SwingUtilities.invokeLater(this.comboBoxFocusRequester);
                SwingUtilities.invokeLater(this.comboBoxOpener);
                System.out.println("getEditorComponent>isSelected (#564; value=" + value + ")");
                //return getEditorComponent(table, value, isSelected, row, col); // fix for blank box - circular
                //return getComboBox(); // is blank when you click in another column in the same (selected) row
            }
            //the user selected a new row (or initially no row was selected)
            this.editor.removeAll();  // remove the combobox from the panel
            JComboBox editorbox = getEditorBox(table.convertRowIndexToModel(row));
            System.out.println("getRendererComponent>notSelected (#569 row=" + row + ", value=" + value + ")");
            //editorbox.addItem(value); // display current Aspect

            editorbox.setSelectedItem(value);
            editorbox.addActionListener (new ActionListener ()
            {
                public final void actionPerformed (ActionEvent evt) {
                    Object choice = editorbox.getSelectedItem();
                    System.out.println("actionPerformed (#577 event=" + evt.toString() + " choice=" + choice.toString() + " -is this empty?");
                    eventAspectComboBoxActionPerformed(choice); // try a special method for this source
                }
            });
            editor.add(editorbox);
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
            //this.initialValue = value;
            //updateData(row, isSelected, table); // get appropriate ComboBox for mast
            //setSelectedItem(value);
            return getRendererComponent(table, value, isSelected, hasFocus, row, col);
        }

        protected Component getRendererComponent(JTable  table,
                                               Object  value,
                                               boolean isSelected,
                                               boolean hasFocus,
                                               int     row,
                                               int     col)
        {
            //isSelected = table.isRowSelected(row);
            this.renderer.removeAll();  //remove the combobox from the panel
            JComboBox renderbox = new JComboBox(); // EBR create a fake comboBox with the current Aspect of mast in this row
            System.out.println("getRendererComponent (#623 row=" + row + ", value=" + value + " -empty?)");
            //renderbox.addItem(getValueAt(row, VALUECOL));
            renderbox.addItem(value); // display current Aspect
            renderer.add(renderbox);
            //renderbox.setSelectedItem(0);
            return this.renderer;
        }

        private void updateData(int row, boolean isSelected, JTable table) {
            // get correct Aspects for ComboBox
            System.out.println("#624 updateData (row=" + row + ")");
            JComboBox rb = getRendererBox(table.convertRowIndexToModel(row));
            JComboBox eb = getEditorBox(table.convertRowIndexToModel(row));
            this.editor.add(eb);
            this.renderer.add(rb);
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
         *  @interfaceMethod javax.swing.table.TableCellEditor
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
            System.out.println("getCellEditorValue, prevItem= " + prevItem + ")");
            return prevItem;
        }

        // is this really used?
        protected void setSelectedItem(Object item) {
            System.out.println("setSelectedItem, item= " + item + ")");
            if  (getComboBox().getSelectedItem() != item) {
                this.consumeComboBoxActionEvent = false;
                getComboBox().setSelectedItem(item);
                this.consumeComboBoxActionEvent = true;
            }
        }

        public final void setItems (Object [] items) {
            JComboBox cb = getComboBox(); // pick up the same stuff? not good
            //cb.removeAllItems();
            System.out.println("setItems on cb #684");
            final int n = (items != null  ?  items.length  :  0);
            for  (int i = 0; i < n; i++)
            {
                cb.addItem (items [i]);
            }
        }

        final void eventEditorMousePressed() {
            this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add cb to JPanel
            this.editor.revalidate();
            SwingUtilities.invokeLater(this.comboBoxFocusRequester);
            System.out.println("#681 eventEditorMousePressed in row: " + this.currentRow + ")");

        }

        protected void eventTableSelectionChanged() {
            //stop editing if a new row is selected
            System.out.println("#697 eventTableSelectionChanged"); // doesn't fis to remove if here
            if  ( ! this.table.isRowSelected(this.currentRow))
            {
                stopCellEditing ();
            }
        }

        // copy for our own box
        protected void eventAspectComboBoxActionPerformed(@Nonnull Object choice) {
            Object item = choice;
            System.out.println("#713 eventAspectComboBoxActionPerformed; selected item=" + item + ")");
            prevItem = choice; // passed as cell value
            if (consumeComboBoxActionEvent) stopCellEditing();
        }

        protected void eventComboBoxActionPerformed() {
            Object item = getComboBox().getSelectedItem();
            System.out.println("#697 eventComboBoxActionPerformed; selected item=" + item + ")");
            if  (item != null) prevItem = item;
            System.out.println("eventComboBoxActionPerformed (choice/item=" + item.toString() + ")");
            if (consumeComboBoxActionEvent) stopCellEditing();
        }
        public final JComboBox getComboBox() {
            return (JComboBox) getComponent();
        }

        public final Object getInitialValue() {
            return this.initialValue;
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
        table.setDefaultRenderer(JPanel.class, new AspectComboBoxPanel()); // create a separate class for the renderer?
        table.setDefaultEditor(JPanel.class, new AspectComboBoxPanel());
        //TableColumn aspectcol = table.getColumnModel().getColumn(VALUECOL);
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

    // Methods to display VALUECOL (aspect) ComboBox in Signal mAst TAbel
    // Derived from the SignalMastJTable class (which was deprecated since 4.5.5):
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
        log.debug("Combo row: " + row);
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
     * used by getEditorBox() and getRendererBox
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
