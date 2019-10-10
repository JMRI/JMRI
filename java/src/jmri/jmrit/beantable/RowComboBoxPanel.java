package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.annotation.Nonnull;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table cell editor abstract class with a custom ComboBox per row as the editing component.
 * <p>
 * Used as TableCellRenderer from JTable, declared in ConfigValueColumn()
 * Based on: http://alvinalexander.com/java/jwarehouse/netbeans-src/monitor/src/org/netbeans/modules/web/monitor/client/ComboBoxTableCellEditor.java.shtml
 * @author Egbert Broerse 2016
 * @since 4.7.1
 */
public abstract class RowComboBoxPanel
        extends    DefaultCellEditor
        implements TableCellRenderer {

    /**
     * The surrounding panel for the combobox.
     */
    protected JPanel editor;

    /**
     * The surrounding panel for the combobox.
     */
    protected JPanel renderer;

    /**
     * Listeners for the table added?
     */
    protected boolean tableListenerAdded = false;

    /**
     * The table.
     */
    protected JTable table;

    /**
     *  To request the focus for the combobox (with SwingUtilities.invokeLater())
     */
    protected Runnable comboBoxFocusRequester;

    /**
     *  The current row.
     */
    protected int currentRow = -1;

    /**
     *  The previously selected value in the editor.
     */
    protected Object prevItem;

    /**
     *  React on action events on the combobox?
     */
    protected boolean consumeComboBoxActionEvent = true;

    /**
     *  The event that causes the editing to start. We need it to know
     *  if we should open the popup automatically.
     */
    protected EventObject startEditingEvent = null;

    /**
     *  Create a new CellEditor and CellRenderer.
     *  @param values array (list) of options to display
     *  @param customRenderer renderer to display things
     */
    public RowComboBoxPanel(Object [] values,
                            ListCellRenderer customRenderer) {
        super (new JComboBox());
        // is being filled from HashMap
        this.editor = new JPanel(new BorderLayout ());
        if (values != null) {
            setItems(values); // in 4.5.7 this is not yet called using values, but might be useful in a more general application
        }
        this.renderer = new JPanel(new BorderLayout ());
        setClickCountToStart(1); // value for a DefaultCellEditor: immediately start editing
        //show the combobox if the mouse clicks at the panel
        this.editor.addMouseListener (new MouseAdapter ()
        {
            @Override
            public final void mousePressed (MouseEvent evt)
            {
                eventEditorMousePressed();
            }
        });
    }

    public RowComboBoxPanel(Object [] values) {
        this(values, null);
    }

    public RowComboBoxPanel() {
        this(new Object [0]);
    } // as it is defined in configValueColumn()

    public RowComboBoxPanel(ListCellRenderer customRenderer) {
        this(new Object [0], customRenderer);
    }

    /**
     * Create the editor component for the cell and add a listener for changes in the table.
     *
     * @param table parent JTable of NamedBean
     * @param value current value for cell to be rendered.
     * @param isSelected tells if this row is selected in the table.
     * @param row the row in table.
     * @param col the column in table, in this case Value (Aspect or Appearance).
     * @return A JPanel containing a JComboBox with valid options as the CellEditor for the Value.
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
                @Override
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

    /**
     * (Re)build combobox with all allowed state values, select current and add action listener.
     *
     * @param table parent JTable of NamedBean
     * @param value current value for cell to be rendered.
     * @param isSelected tells if this row is selected in the table.
     * @param row the row in table.
     * @param col the column in table, in this case Value (Aspect or Appearance).
     * @return a JPanel containing a JComboBox
     * @see #getTableCellEditorComponent(JTable, Object, boolean, int, int)
     * @see #getEditorBox(int)
     */
    protected Component getEditorComponent(JTable  table,
                                           Object  value,
                                           boolean isSelected,
                                           int     row,
                                           int     col)
    {
        //new or old row? > should be cleaned up, leave our isSelected argument?
        //isSelected = table.isRowSelected(row);
        if  (isSelected) {
            //old row
            log.debug("getEditorComponent>isSelected (value={})", value);
        }
        //the user selected another row (or initially no row was selected)
        this.editor.removeAll();  // remove the combobox from the panel
        JComboBox editorbox = getEditorBox(table.convertRowIndexToModel(row));
        log.debug("getEditorComponent>notSelected (row={}, value={}; me = {}))", row, value, this.toString());
        if (value != null) {
            editorbox.setSelectedItem(value); // display current Value
        }
        editorbox.addActionListener(new ActionListener ()
        {
            @Override
            public final void actionPerformed(ActionEvent evt) {
                Object choice = editorbox.getSelectedItem();
                log.debug("actionPerformed (event={}, choice={}", evt.toString(), choice.toString());
                eventRowComboBoxActionPerformed(choice); // signal the changed row
            }
        });
        this.editor.add(editorbox);
        return this.editor;
    }

    /**
     * Create the renderer component for the cell and add a listener for changes in the table.
     *
     * @param table the parent Table.
     * @param value current value for cell to be rendered.
     * @param isSelected tells if this row is selected in the table.
     * @param hasFocus true if the row has focus.
     * @param row the row in table.
     * @param col the column in table, in this case Value (Aspect/Appearance).
     * @return A JPanel containing a JComboBox with only the current Value as the CellRenderer.
     */
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
                @Override
                public final void valueChanged(ListSelectionEvent evt)
                {
                    eventTableSelectionChanged ();
                }
            });
        }

        this.currentRow = row;
        return getRendererComponent(table, value, isSelected, hasFocus, row, col); // OK to call getEditorComponent() instead?
    }

    /**
     * (Re)build combobox with only the active state value.
     *
     * @param table the parent Table.
     * @param value current value for cell to be rendered.
     * @param isSelected tells if this row is selected in the table.
     * @param hasFocus true if the row has focus.
     * @param row the row in table.
     * @param col the column in table, in this case Value (Aspect/Appearance).
     * @return a JPanel containing a JComboBox
     * @see #getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
     */
    protected Component getRendererComponent(JTable  table,
                                             Object  value,
                                             boolean isSelected,
                                             boolean hasFocus,
                                             int     row,
                                             int     col)
    {
        this.renderer.removeAll();  //remove the combobox from the panel
        JComboBox<String> renderbox = new JComboBox<>(); // create a fake comboBox with the current Value (Aspect of mast/Appearance of the Head) in this row
        log.debug("RCBP getRendererComponent (row={}, value={})", row, value);
        if (value != null) {
            renderbox.addItem(value.toString()); // display (only) the current Value
        } else {
            renderbox.addItem(""); // blank item
        }
        renderer.add(renderbox);
        return this.renderer;
    }

    /**
     * Refresh contents of editor.
     *
     * @param row the row in table.
     * @param isSelected tells if this row is selected in the table.
     * @param table the parent Table.
     * @see #getTableCellEditorComponent(JTable, Object, boolean, int, int)
     */
    protected void updateData(int row, boolean isSelected, JTable table) {
        // get valid Value options for ComboBox
        log.debug("RCBP updateData (row:{}; me = {}))", row, this.toString());
        JComboBox editorbox = getEditorBox(table.convertRowIndexToModel(row));
        this.editor.add(editorbox);
        if (isSelected) {
            editor.setBackground(table.getSelectionBackground());
        } else {
            editor.setBackground(table.getBackground());
        }
    }

    /**
     *  Is the cell editable? If the mouse was pressed at a margin
     *  we don't want the cell to be editable.
     *
     *  @param evt The event-object
     *  @return true when user clicked inside cell, not on cell border
     */
    @Override
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

    /**
     * Get current contents (value) in cell.
     *
     * @return value (String in 4.6 applications)
     */
    @Override
    public Object getCellEditorValue() {
        log.debug("getCellEditorValue, prevItem: {}; me = {})", prevItem, this.toString());
        return prevItem;
    }

    /**
     *  Put contents into the combobox.
     *  @param items array (strings) of options to display
     */
    public final void setItems(@Nonnull Object [] items) {
        JComboBox<String> editorbox = new JComboBox<> ();
        final int n = items.length;
        for  (int i = 0; i < n; i++)
        {
            if (items [i] != null) {
                editorbox.addItem (items[i].toString());
            }
        }
        this.editor.add(editorbox);
    }

    /**
     * Open combobox (Editor) when clicked.
     */
    protected void eventEditorMousePressed() {
        this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add editorBox to JPanel
        this.editor.revalidate();
        SwingUtilities.invokeLater(this.comboBoxFocusRequester);
        log.debug("eventEditorMousePressed in row {}; me = {})", this.currentRow, this.toString());
    }

    /**
     * Stop editing if a new row is selected.
     */
    protected void eventTableSelectionChanged() {
        log.debug("eventTableSelectionChanged");
        if  ( ! this.table.isRowSelected(this.currentRow))
        {
            stopCellEditing ();
        }
    }

    /**
     * Method for our own VALUECOL row specific JComboBox.
     * @param choice the selected item (Aspect/Appearance) in the combobox list
     */
    protected void eventRowComboBoxActionPerformed(@Nonnull Object choice) {
        Object item = choice;
        log.debug("eventRowComboBoxActionPerformed; selected item: {}, me = {})", item, this.toString());
        prevItem = choice; // passed as cell value
        if (consumeComboBoxActionEvent) stopCellEditing();
    }

    protected int getCurrentRow() {
        return this.currentRow;
    }

    /*
     * Placeholder method; contents are overridden in application.
     */
    protected JComboBox<String> getEditorBox(int row) {
        String [] list = {"Error", "Not Valid"};
        return new JComboBox<>(list);
    }

    private final static Logger log = LoggerFactory.getLogger(BeanTableDataModel.class);

}