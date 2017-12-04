package jmri.jmrit.beantable;

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBeanHandleManager;
import jmri.UserPreferencesManager;
import jmri.swing.JTablePersistenceManager;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of NamedBean manager contents.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2006
 */
abstract public class BeanTableDataModel extends AbstractTableModel implements PropertyChangeListener {

    static public final int SYSNAMECOL = 0;
    static public final int USERNAMECOL = 1;
    static public final int VALUECOL = 2;
    static public final int COMMENTCOL = 3;
    static public final int DELETECOL = 4;
    static public final int NUMCOLUMN = 5;
    protected List<String> sysNameList = null;
    boolean noWarnDelete = false;
    NamedBeanHandleManager nbMan = InstanceManager.getDefault(NamedBeanHandleManager.class);

    public BeanTableDataModel() {
        super();
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    protected synchronized void updateNameList() {
        // first, remove listeners from the individual objects
        if (sysNameList != null) {
            for (int i = 0; i < sysNameList.size(); i++) {
                // if object has been deleted, it's not here; ignore it
                NamedBean b = getBySystemName(sysNameList.get(i));
                if (b != null) {
                    b.removePropertyChangeListener(this);
                }
            }
        }
        sysNameList = getManager().getSystemNameList();
        // and add them back in
        for (int i = 0; i < sysNameList.size(); i++) {
            getBySystemName(sysNameList.get(i)).addPropertyChangeListener(this, null, "Table View");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new NamedBean is available in the manager
            updateNameList();
            log.debug("Table changed length to {}", sysNameList.size());
            fireTableDataChanged();
        } else if (matchPropertyName(e)) {
            // a value changed.  Find it, to avoid complete redraw
            if (e.getSource() instanceof NamedBean) {
                String name = ((NamedBean) e.getSource()).getSystemName();
                if (log.isDebugEnabled()) {
                    log.debug("Update cell " + sysNameList.indexOf(name) + ","
                            + VALUECOL + " for " + name);
                }
                // since we can add columns, the entire row is marked as updated
                int row = sysNameList.indexOf(name);
                try {
                    fireTableRowsUpdated(row, row);
                } catch (Exception ex) {
                    log.error("Exception updating table", ex);
                }
            }
        }
    }

    /**
     * Is this property event announcing a change this table should display?
     * <P>
     * Note that events will come both from the NamedBeans and also from the
     * manager
     *
     * @param e the event to match
     * @return true if the property name is of interest, false otherwise
     */
    protected boolean matchPropertyName(PropertyChangeEvent e) {
        return (e.getPropertyName().contains("State")
                || e.getPropertyName().contains("Appearance")
                || e.getPropertyName().contains("Comment"))
                || e.getPropertyName().contains("UserName");
    }

    @Override
    public int getRowCount() {
        return sysNameList.size();
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case SYSNAMECOL:
                return Bundle.getMessage("ColumnSystemName"); //"System Name";
            case USERNAMECOL:
                return Bundle.getMessage("ColumnUserName"); //"User Name";
            case VALUECOL:
                return Bundle.getMessage("ColumnState"); //"State";
            case COMMENTCOL:
                return Bundle.getMessage("ColumnComment"); //"Comment";
            case DELETECOL:
                return "";
            default:
                return "unknown";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case SYSNAMECOL:
            case USERNAMECOL:
            case COMMENTCOL:
                return String.class;
            case VALUECOL:
            case DELETECOL:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        String uname;
        switch (col) {
            case VALUECOL:
            case COMMENTCOL:
            case DELETECOL:
                return true;
            case USERNAMECOL:
                NamedBean b = getBySystemName(sysNameList.get(row));
                uname = b.getUserName();
                if ((uname == null) || uname.equals("")) {
                    return true;
                }
            //$FALL-THROUGH$
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        NamedBean b;
        switch (col) {
            case SYSNAMECOL:  // slot number
                return sysNameList.get(row);
            case USERNAMECOL:  // return user name
                // sometimes, the TableSorter invokes this on rows that no longer exist, so we check
                b = getBySystemName(sysNameList.get(row));
                return (b != null) ? b.getUserName() : null;
            case VALUECOL:  //
                return getValue(sysNameList.get(row));
            case COMMENTCOL:
                b = getBySystemName(sysNameList.get(row));
                return (b != null) ? b.getComment() : null;
            case DELETECOL:  //
                return Bundle.getMessage("ButtonDelete");
            default:
                log.error("internal state inconsistent with table requst for {} {}", row, col);
                return null;
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case SYSNAMECOL:
                return new JTextField(5).getPreferredSize().width;
            case COMMENTCOL:
            case USERNAMECOL:
                return new JTextField(15).getPreferredSize().width; // TODO I18N using Bundle.getMessage()
            case VALUECOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
            case DELETECOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                return new JTextField(22).getPreferredSize().width;
            default:
                log.warn("Unexpected column in getPreferredWidth: {}", col);
                return new JTextField(8).getPreferredSize().width;
        }
    }

    abstract public String getValue(String systemName);

    abstract protected Manager getManager();

    protected void setManager(Manager man) {
    }

    abstract protected NamedBean getBySystemName(String name);

    abstract protected NamedBean getByUserName(String name);

    abstract protected void clickOn(NamedBean t);

    public int getDisplayDeleteMsg() {
        return InstanceManager.getDefault(UserPreferencesManager.class).getMultipleChoiceOption(getMasterClassName(), "deleteInUse");
    }

    public void setDisplayDeleteMsg(int boo) {
        InstanceManager.getDefault(UserPreferencesManager.class).setMultipleChoiceOption(getMasterClassName(), "deleteInUse", boo);
    }

    abstract protected String getMasterClassName();

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case USERNAMECOL:
                //Directly changing the username should only be possible if the username was previously null or ""
                // check to see if user name already exists
                if (((String) value).equals("")) {
                    value = null;
                } else {
                    NamedBean nB = getByUserName((String) value);
                    if (nB != null) {
                        log.error("User name is not unique {}", value);
                        String msg = Bundle.getMessage("WarningUserName", new Object[]{("" + value)});
                        JOptionPane.showMessageDialog(null, msg,
                                Bundle.getMessage("WarningTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                NamedBean nBean = getBySystemName(sysNameList.get(row));
                nBean.setUserName((String) value);
                if (nbMan.inUse(sysNameList.get(row), nBean)) {
                    String msg = Bundle.getMessage("UpdateToUserName", new Object[]{getBeanType(), value, sysNameList.get(row)});
                    int optionPane = JOptionPane.showConfirmDialog(null,
                            msg, Bundle.getMessage("UpdateToUserNameTitle"),
                            JOptionPane.YES_NO_OPTION);
                    if (optionPane == JOptionPane.YES_OPTION) {
                        //This will update the bean reference from the systemName to the userName
                        try {
                            nbMan.updateBeanFromSystemToUser(nBean);
                        } catch (JmriException ex) {
                            //We should never get an exception here as we already check that the username is not valid
                        }
                    }
                }
                fireTableRowsUpdated(row, row);
                break;
            case COMMENTCOL:
                getBySystemName(sysNameList.get(row)).setComment(
                        (String) value);
                fireTableRowsUpdated(row, row);
                break;
            case VALUECOL:
                // button fired, swap state
                NamedBean t = getBySystemName(sysNameList.get(row));
                clickOn(t);
                break;
            case DELETECOL:
                // button fired, delete Bean
                deleteBean(row, col);
                break;
            default:
                break;
        }
    }

    protected void deleteBean(int row, int col) {
        final NamedBean t = getBySystemName(sysNameList.get(row));
        //int count = t.getNumPropertyChangeListeners()-1; // one is this table
        DeleteBeanWorker worker = new DeleteBeanWorker(t);
        worker.execute();
    }

    /**
     * Delete the bean after all the checking has been done.
     * <p>
     * Separate so that it can be easily subclassed if other functionality is
     * needed.
     *
     * @param bean NamedBean to delete
     */
    void doDelete(NamedBean bean) {
        try {
            getManager().deleteBean(bean, "DoDelete");
        } catch (PropertyVetoException e) {
            //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
            log.error(e.getMessage());
        }
    }

    /**
     * Configure a table to have our standard rows and columns. This is
     * optional, in that other table formats can use this table model. But we
     * put it here to help keep it consistent. This also persists the table user
     * interface state.
     *
     * @param table {@link JTable} to configure
     */
    public void configureTable(JTable table) {
        // allow reordering of the columns
        table.getTableHeader().setReorderingAllowed(true);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        table.sizeColumnsToFit(-1);

        configValueColumn(table);
        configDeleteColumn(table);

        MouseListener popupListener = new PopupListener();
        table.addMouseListener(popupListener);

        this.persistTable(table);

    }

    protected void configValueColumn(JTable table) {
        // have the value column hold a button
        setColumnToHoldButton(table, VALUECOL, configureButton());
    }

    public JButton configureButton() {
        // pick a large size
        JButton b = new JButton(Bundle.getMessage("BeanStateInconsistent"));
        b.putClientProperty("JComponent.sizeVariant", "small");
        b.putClientProperty("JButton.buttonType", "square");
        return b;
    }

    protected void configDeleteColumn(JTable table) {
        // have the delete column hold a button
        setColumnToHoldButton(table, DELETECOL,
                new JButton(Bundle.getMessage("ButtonDelete")));
    }

    /**
     * Service method to setup a column so that it will hold a button for it's
     * values
     *
     * @param table  {@link JTable} to use
     * @param column Column to setup
     * @param sample Typical button, used for size
     */
    protected void setColumnToHoldButton(JTable table, int column, JButton sample) {
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        table.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        table.setDefaultEditor(JButton.class, buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
                .setPreferredWidth((sample.getPreferredSize().width) + 4);
    }

    synchronized public void dispose() {
        getManager().removePropertyChangeListener(this);
        if (sysNameList != null) {
            for (int i = 0; i < sysNameList.size(); i++) {
                NamedBean b = getBySystemName(sysNameList.get(i));
                if (b != null) {
                    b.removePropertyChangeListener(this);
                }
            }
        }
    }

    /**
     * Method to self print or print preview the table. Printed in equally sized
     * columns across the page with headings and vertical lines between each
     * column. Data is word wrapped within a column. Can handle data as strings,
     * comboboxes or booleans
     *
     * @param w the printer writer
     */
    public void printTable(HardcopyWriter w) {
        // determine the column size - evenly sized, with space between for lines
        int columnSize = (w.getCharactersPerLine() - this.getColumnCount() - 1) / this.getColumnCount();

        // Draw horizontal dividing line
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                (columnSize + 1) * this.getColumnCount());

        // print the column header labels
        String[] columnStrings = new String[this.getColumnCount()];
        // Put each column header in the array
        for (int i = 0; i < this.getColumnCount(); i++) {
            columnStrings[i] = this.getColumnName(i);
        }
        w.setFontStyle(Font.BOLD);
        printColumns(w, columnStrings, columnSize);
        w.setFontStyle(0);
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                (columnSize + 1) * this.getColumnCount());

        // now print each row of data
        // create a base string the width of the column
        StringBuilder spaces = new StringBuilder(""); // NOI18N
        for (int i = 0; i < columnSize; i++) {
            spaces.append(" "); // NOI18N
        }
        for (int i = 0; i < this.getRowCount(); i++) {
            for (int j = 0; j < this.getColumnCount(); j++) {
                //check for special, non string contents
                Object value = this.getValueAt(i, j);
                if (value == null) {
                    columnStrings[j] = spaces.toString();
                } else if (value instanceof JComboBox<?>) {
                    columnStrings[j] = ((JComboBox<?>) value).getSelectedItem().toString();
                } else {
                    // Boolean or String
                    columnStrings[j] = value.toString();
                }
            }
            printColumns(w, columnStrings, columnSize);
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                    (columnSize + 1) * this.getColumnCount());
        }
        w.close();
    }

    protected void printColumns(HardcopyWriter w, String columnStrings[], int columnSize) {
        // create a base string the width of the column
        StringBuilder spaces = new StringBuilder(""); // NOI18N
        for (int i = 0; i < columnSize; i++) {
            spaces.append(" "); // NOI18N
        }
        // loop through each column
        boolean complete = false;
        while (!complete) {
            StringBuilder lineString = new StringBuilder(""); // NOI18N
            complete = true;
            for (int i = 0; i < columnStrings.length; i++) {
                String columnString = ""; // NOI18N
                // if the column string is too wide cut it at word boundary (valid delimiters are space, - and _)
                // use the intial part of the text,pad it with spaces and place the remainder back in the array
                // for further processing on next line
                // if column string isn't too wide, pad it to column width with spaces if needed
                if (columnStrings[i].length() > columnSize) {
                    boolean noWord = true;
                    for (int k = columnSize; k >= 1; k--) {
                        if (columnStrings[i].substring(k - 1, k).equals(" ")
                                || columnStrings[i].substring(k - 1, k).equals("-")
                                || columnStrings[i].substring(k - 1, k).equals("_")) {
                            columnString = columnStrings[i].substring(0, k)
                                    + spaces.substring(columnStrings[i].substring(0, k).length());
                            columnStrings[i] = columnStrings[i].substring(k);
                            noWord = false;
                            complete = false;
                            break;
                        }
                    }
                    if (noWord) {
                        columnString = columnStrings[i].substring(0, columnSize);
                        columnStrings[i] = columnStrings[i].substring(columnSize);
                        complete = false;
                    }

                } else {
                    columnString = columnStrings[i] + spaces.substring(columnStrings[i].length());
                    columnStrings[i] = "";
                }
                lineString.append(columnString).append(" "); // NOI18N
            }
            try {
                w.write(lineString.toString());
                //write vertical dividing lines
                for (int i = 0; i < w.getCharactersPerLine(); i = i + columnSize + 1) {
                    w.write(w.getCurrentLineNumber(), i, w.getCurrentLineNumber() + 1, i);
                }
                w.write("\n"); // NOI18N
            } catch (IOException e) {
                log.warn("error during printing: {}", e.getMessage());
            }
        }
    }

    /**
     * Create and configure a new table using the given model and row sorter.
     *
     * @param name   the name of the table
     * @param model  the data model for the table
     * @param sorter the row sorter for the table; if null, the table will not
     *               be sortable
     * @return the table
     * @throws NullPointerException if name or model is null
     */
    public JTable makeJTable(@Nonnull String name, @Nonnull TableModel model, @Nullable RowSorter<? extends TableModel> sorter) {
        Objects.requireNonNull(name, "the table name must be nonnull");
        Objects.requireNonNull(model, "the table model must be nonnull");
        return this.configureJTable(name, new JTable(model), sorter);
    }

    /**
     * Configure a new table using the given model and row sorter.
     *
     * @param table  the table to configure
     * @param name   the table name
     * @param sorter the row sorter for the table; if null, the table will not
     *               be sortable
     * @return the table
     * @throws NullPointerException if table or the table name is null
     */
    protected JTable configureJTable(@Nonnull String name, @Nonnull JTable table, @Nullable RowSorter<? extends TableModel> sorter) {
        Objects.requireNonNull(table, "the table must be nonnull");
        Objects.requireNonNull(name, "the table name must be nonnull");
        table.setRowSorter(sorter);
        table.setName(name);
        table.getTableHeader().setReorderingAllowed(true);
        table.setColumnModel(new XTableColumnModel());
        table.createDefaultColumnsFromModel();
        addMouseListenerToHeader(table);
        return table;
    }

    abstract protected String getBeanType();/*{
     return "Bean";
     }*/

    protected void showPopup(MouseEvent e) {
        JTable source = (JTable) e.getSource();
        int row = source.rowAtPoint(e.getPoint());
        int column = source.columnAtPoint(e.getPoint());
        if (!source.isRowSelected(row)) {
            source.changeSelection(row, column, false, false);
        }
        final int rowindex = source.convertRowIndexToModel(row);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("CopyName"));
        menuItem.addActionListener((ActionEvent e1) -> {
            copyName(rowindex, 0);
        });
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("Rename"));
        menuItem.addActionListener((ActionEvent e1) -> {
            renameBean(rowindex, 0);
        });
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("Clear"));
        menuItem.addActionListener((ActionEvent e1) -> {
            removeName(rowindex, 0);
        });
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("Move"));
        menuItem.addActionListener((ActionEvent e1) -> {
            moveBean(rowindex, 0);
        });
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("ButtonDelete"));
        menuItem.addActionListener((ActionEvent e1) -> {
            deleteBean(rowindex, 0);
        });
        popupMenu.add(menuItem);

        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    public void copyName(int row, int column) {
        NamedBean nBean = getBySystemName(sysNameList.get(row));
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection name = new StringSelection(nBean.getUserName());
        clipboard.setContents(name, null);
    }

    public void renameBean(int row, int column) {
        NamedBean nBean = getBySystemName(sysNameList.get(row));
        String oldName = nBean.getUserName();
        JTextField _newName = new JTextField(20);
        _newName.setText(oldName);
        Object[] renameBeanOption = {Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), _newName};
        int retval = JOptionPane.showOptionDialog(null,
                Bundle.getMessage("RenameFrom", oldName), Bundle.getMessage("RenameTitle", getBeanType()),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                renameBeanOption, renameBeanOption[2]);

        if (retval != 1) {
            return;
        }
        String value = _newName.getText().trim(); // N11N

        if (value.equals(oldName)) {
            //name not changed.
            return;
        } else {
            NamedBean nB = getByUserName(value);
            if (nB != null) {
                log.error("User name is not unique {}", value);
                String msg = Bundle.getMessage("WarningUserName", new Object[]{("" + value)});
                JOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        nBean.setUserName(value);
        fireTableRowsUpdated(row, row);
        if (!value.equals("")) {
            if (oldName == null || oldName.equals("")) {
                if (!nbMan.inUse(sysNameList.get(row), nBean)) {
                    return;
                }
                String msg = Bundle.getMessage("UpdateToUserName", new Object[]{getBeanType(), value, sysNameList.get(row)});
                int optionPane = JOptionPane.showConfirmDialog(null,
                        msg, Bundle.getMessage("UpdateToUserNameTitle"),
                        JOptionPane.YES_NO_OPTION);
                if (optionPane == JOptionPane.YES_OPTION) {
                    //This will update the bean reference from the systemName to the userName
                    try {
                        nbMan.updateBeanFromSystemToUser(nBean);
                    } catch (JmriException ex) {
                        //We should never get an exception here as we already check that the username is not valid
                    }
                }

            } else {
                nbMan.renameBean(oldName, value, nBean);
            }

        } else {
            //This will update the bean reference from the old userName to the SystemName
            nbMan.updateBeanFromUserToSystem(nBean);
        }
    }

    public void removeName(int row, int column) {
        NamedBean nBean = getBySystemName(sysNameList.get(row));
        String msg = Bundle.getMessage("UpdateToSystemName", new Object[]{getBeanType()});
        int optionPane = JOptionPane.showConfirmDialog(null,
                msg, Bundle.getMessage("UpdateToSystemNameTitle"),
                JOptionPane.YES_NO_OPTION);
        if (optionPane == JOptionPane.YES_OPTION) {
            nbMan.updateBeanFromUserToSystem(nBean);
        }
        nBean.setUserName(null);
        fireTableRowsUpdated(row, row);
    }

    public void moveBean(int row, int column) {
        final NamedBean t = getBySystemName(sysNameList.get(row));
        String currentName = t.getUserName();
        NamedBean oldNameBean = getBySystemName(sysNameList.get(row));

        if ((currentName == null) || currentName.equals("")) {
            JOptionPane.showMessageDialog(null, "Can not move an empty UserName");
            return;
        }

        JComboBox<String> box = new JComboBox<>();
        List<String> nameList = getManager().getSystemNameList();
        for (int i = 0; i < nameList.size(); i++) {
            NamedBean nb = getBySystemName(nameList.get(i));
            //Only add items that do not have a username assigned.
            if (nb.getDisplayName().equals(nameList.get(i))) {
                box.addItem(nameList.get(i));
            }
        }

        int retval = JOptionPane.showOptionDialog(null,
                "Move " + getBeanType() + " " + currentName + " from " + oldNameBean.getSystemName(), "Move UserName",
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), box}, null); // TODO I18N
        log.debug("Dialog value {} selected {}:{}", retval, box.getSelectedIndex(), box.getSelectedItem());
        if (retval != 1) {
            return;
        }
        String entry = (String) box.getSelectedItem();
        NamedBean newNameBean = getBySystemName(entry);
        if (oldNameBean != newNameBean) {
            oldNameBean.setUserName("");
            newNameBean.setUserName(currentName);
            InstanceManager.getDefault(NamedBeanHandleManager.class).moveBean(oldNameBean, newNameBean, currentName);
            if (nbMan.inUse(newNameBean.getSystemName(), newNameBean)) {
                String msg = Bundle.getMessage("UpdateToUserName", new Object[]{getBeanType(), currentName, sysNameList.get(row)});
                int optionPane = JOptionPane.showConfirmDialog(null, msg, Bundle.getMessage("UpdateToUserNameTitle"), JOptionPane.YES_NO_OPTION);
                if (optionPane == JOptionPane.YES_OPTION) {
                    try {
                        nbMan.updateBeanFromSystemToUser(newNameBean);
                    } catch (JmriException ex) {
                        //We should never get an exception here as we already check that the username is not valid
                    }
                }
            }
            fireTableRowsUpdated(row, row);
            InstanceManager.getDefault(UserPreferencesManager.class).
                    showInfoMessage("Reminder", getBeanType() + " " + Bundle.getMessage("UpdateComplete"), getMasterClassName(), "remindSaveReLoad");
            //JOptionPane.showMessageDialog(null, getBeanType() + " " + Bundle.getMessage("UpdateComplete"));
        }
    }

    protected void showTableHeaderPopup(MouseEvent e, JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        XTableColumnModel tcm = (XTableColumnModel) table.getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(false); i++) {
            TableColumn tc = tcm.getColumnByModelIndex(i);
            String columnName = table.getModel().getColumnName(i);
            if (columnName != null && !columnName.equals("")) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(table.getModel().getColumnName(i), tcm.isColumnVisible(tc));
                menuItem.addActionListener(new HeaderActionListener(tc, tcm));
                popupMenu.add(menuItem);
            }

        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    protected void addMouseListenerToHeader(JTable table) {
        MouseListener mouseHeaderListener = new TableHeaderListener(table);
        table.getTableHeader().addMouseListener(mouseHeaderListener);
    }

    /**
     * Persist the state of the table after first setting the table to the last
     * persisted state.
     *
     * @param table the table to persist
     * @throws NullPointerException if the name of the table is null
     */
    public void persistTable(@Nonnull JTable table) throws NullPointerException {
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent((manager) -> {
            manager.resetState(table); // throws NPE if table name is null
            manager.persist(table);
        });
    }

    /**
     * Stop persisting the state of the table.
     *
     * @param table the table to stop persisting
     * @throws NullPointerException if the name of the table is null
     */
    public void stopPersistingTable(@Nonnull JTable table) throws NullPointerException {
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent((manager) -> {
            manager.stopPersisting(table); // throws NPE if table name is null
        });
    }

    static class HeaderActionListener implements ActionListener {

        TableColumn tc;
        XTableColumnModel tcm;

        HeaderActionListener(TableColumn tc, XTableColumnModel tcm) {
            this.tc = tc;
            this.tcm = tcm;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            //Do not allow the last column to be hidden
            if (!check.isSelected() && tcm.getColumnCount(true) == 1) {
                return;
            }
            tcm.setColumnVisible(tc, check.isSelected());
        }
    }

    class DeleteBeanWorker extends SwingWorker<Void, Void> {

        NamedBean t;

        public DeleteBeanWorker(NamedBean bean) {
            t = bean;
        }

        @Override
        public Void doInBackground() {
            StringBuilder message = new StringBuilder();
            try {
                getManager().deleteBean(t, "CanDelete");  // NOI18N
            } catch (PropertyVetoException e) {
                if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //NOI18N
                    log.warn(e.getMessage());
                    message.append(Bundle.getMessage("VetoDeleteBean", t.getBeanType(), t.getFullyFormattedDisplayName(), e.getMessage()));
                    JOptionPane.showMessageDialog(null, message.toString(),
                            Bundle.getMessage("WarningTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                message.append(e.getMessage());
            }
            int count = t.getNumPropertyChangeListeners();
            if (log.isDebugEnabled()) {
                log.debug("Delete with " + count);
            }
            if (getDisplayDeleteMsg() == 0x02 && message.toString().equals("")) {
                doDelete(t);
            } else {
                final JDialog dialog = new JDialog();
                dialog.setTitle(Bundle.getMessage("WarningTitle"));
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JPanel container = new JPanel();
                container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                if (count > 0) { // warn of listeners attached before delete

                    JLabel question = new JLabel(Bundle.getMessage("DeletePrompt", t.getFullyFormattedDisplayName()));
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);

                    ArrayList<String> listenerRefs = t.getListenerRefs();
                    if (listenerRefs.size() > 0) {
                        ArrayList<String> listeners = new ArrayList<>();
                        for (int i = 0; i < listenerRefs.size(); i++) {
                            if (!listeners.contains(listenerRefs.get(i))) {
                                listeners.add(listenerRefs.get(i));
                            }
                        }

                        message.append("<br>");
                        message.append(Bundle.getMessage("ReminderInUse", count));
                        message.append("<ul>");
                        for (int i = 0; i < listeners.size(); i++) {
                            message.append("<li>");
                            message.append(listeners.get(i));
                            message.append("</li>");
                        }
                        message.append("</ul>");

                        JEditorPane pane = new JEditorPane();
                        pane.setContentType("text/html");
                        pane.setText("<html>" + message.toString() + "</html>");
                        pane.setEditable(false);
                        JScrollPane jScrollPane = new JScrollPane(pane);
                        container.add(jScrollPane);
                    }
                } else {
                    String msg = MessageFormat.format(
                            Bundle.getMessage("DeletePrompt"),
                            new Object[]{t.getSystemName()});
                    JLabel question = new JLabel(msg);
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                }

                final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
                remember.setFont(remember.getFont().deriveFont(10f));
                remember.setAlignmentX(Component.CENTER_ALIGNMENT);

                JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));
                JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));
                JPanel button = new JPanel();
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.add(yesButton);
                button.add(noButton);
                container.add(button);

                noButton.addActionListener((ActionEvent e) -> {
                    //there is no point in remembering this the user will never be
                    //able to delete a bean!
                    dialog.dispose();
                });

                yesButton.addActionListener((ActionEvent e) -> {
                    if (remember.isSelected()) {
                        setDisplayDeleteMsg(0x02);
                    }
                    doDelete(t);
                    dialog.dispose();
                });
                container.add(remember);
                container.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.setAlignmentY(Component.CENTER_ALIGNMENT);
                dialog.getContentPane().add(container);
                dialog.pack();
                dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - dialog.getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - dialog.getHeight() / 2);
                dialog.setModal(true);
                dialog.setVisible(true);
            }
            return null;
        }

    }

    class PopupListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }
    }

    class PopupMenuRemoveName implements ActionListener {

        int row;

        PopupMenuRemoveName(int row) {
            this.row = row;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteBean(row, 0);
        }
    }

    class TableHeaderListener extends MouseAdapter {

        JTable table;

        TableHeaderListener(JTable tbl) {
            super();
            table = tbl;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(BeanTableDataModel.class);
}
