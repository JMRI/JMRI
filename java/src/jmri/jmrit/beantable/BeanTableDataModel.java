// BeanTableDataModel.java
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
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import jmri.Manager;
import jmri.NamedBean;
import jmri.util.com.sun.TableSorter;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of NamedBean manager contents
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2006
 * @version	$Revision$
 */
abstract public class BeanTableDataModel extends javax.swing.table.AbstractTableModel
        implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 3276184372154591138L;
    static public final int SYSNAMECOL = 0;
    static public final int USERNAMECOL = 1;
    static public final int VALUECOL = 2;
    static public final int COMMENTCOL = 3;
    static public final int DELETECOL = 4;

    static public final int NUMCOLUMN = 5;

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

    protected List<String> sysNameList = null;

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new NamedBean is available in the manager
            updateNameList();
            log.debug("Table changed length to " + sysNameList.size());
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
                    log.error("Property Change " + ex.toString());
                }
            }
        }
    }

    /**
     * Is this property event announcing a change this table should display?
     * <P>
     * Note that events will come both from the NamedBeans and also from the
     * manager
     */
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        return (e.getPropertyName().indexOf("State") >= 0 || e.getPropertyName().indexOf("Appearance") >= 0
                || e.getPropertyName().indexOf("Comment") >= 0) || e.getPropertyName().indexOf("UserName") >= 0;
    }

    public int getRowCount() {
        return sysNameList.size();
    }

    public int getColumnCount() {
        return NUMCOLUMN;
    }

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

    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case VALUECOL:
            case COMMENTCOL:
            case DELETECOL:
                return true;
            case USERNAMECOL:
                NamedBean b = getBySystemName(sysNameList.get(row));
                if ((b.getUserName() == null) || b.getUserName().equals("")) {
                    return true;
                }
            //$FALL-THROUGH$
            default:
                return false;
        }
    }

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
                log.error("internal state inconsistent with table requst for " + row + " " + col);
                return null;
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case SYSNAMECOL:
                return new JTextField(5).getPreferredSize().width;
            case COMMENTCOL:
            case USERNAMECOL:
                return new JTextField(15).getPreferredSize().width;
            case VALUECOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
            case DELETECOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                return new JTextField(22).getPreferredSize().width;
            default:
                log.warn("Unexpected column in getPreferredWidth: " + col);
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
        return jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).getMultipleChoiceOption(getMasterClassName(), "deleteInUse");
    }

    public void setDisplayDeleteMsg(int boo) {
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMultipleChoiceOption(getMasterClassName(), "deleteInUse", boo);
    }

    abstract protected String getMasterClassName();

    public void setValueAt(Object value, int row, int col) {
        if (col == USERNAMECOL) {
            //Directly changing the username should only be possible if the username was previously null or ""
            // check to see if user name already exists
            if (((String) value).equals("")) {
                value = null;
            } else {
                NamedBean nB = getByUserName((String) value);
                if (nB != null) {
                    log.error("User name is not unique " + value);
                    String msg;
                    msg = Bundle.getMessage("WarningUserName", new Object[]{("" + value)});
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
                    } catch (jmri.JmriException ex) {
                        //We should never get an exception here as we already check that the username is not valid
                    }
                }
            }
            fireTableRowsUpdated(row, row);
        } else if (col == COMMENTCOL) {
            getBySystemName(sysNameList.get(row)).setComment(
                    (String) value);
            fireTableRowsUpdated(row, row);
        } else if (col == VALUECOL) {
            // button fired, swap state
            NamedBean t = getBySystemName(sysNameList.get(row));
            clickOn(t);
        } else if (col == DELETECOL) {
            // button fired, delete Bean
            deleteBean(row, col);
        }
    }

    protected void deleteBean(int row, int col) {
        final NamedBean t = getBySystemName(sysNameList.get(row));
        //int count = t.getNumPropertyChangeListeners()-1; // one is this table
        DeleteBeanWorker worker = new DeleteBeanWorker(t);
        worker.execute();
    }

    class DeleteBeanWorker extends SwingWorker<Void, Void> {

        NamedBean t;

        public DeleteBeanWorker(NamedBean bean) {
            t = bean;
        }

        @Override
        public java.lang.Void doInBackground() throws Exception {
            StringBuilder message = new StringBuilder();
            try {
                getManager().deleteBean(t, "CanDelete");  //IN18N
            } catch (java.beans.PropertyVetoException e) {
                if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //IN18N
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
                String msg;
                dialog.setTitle(Bundle.getMessage("WarningTitle"));
                dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                JPanel container = new JPanel();
                container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                if (count > 0) { // warn of listeners attached before delete
                    msg = java.text.MessageFormat.format(Bundle.getMessage("DeletePrompt"), new Object[]{t.getSystemName()});

                    JLabel question = new JLabel(Bundle.getMessage("DeletePrompt", t.getFullyFormattedDisplayName()));
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);

                    ArrayList<String> listenerRefs = t.getListenerRefs();
                    if (listenerRefs.size() > 0) {
                        ArrayList<String> listeners = new ArrayList<String>();
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
                    msg = java.text.MessageFormat.format(
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

                noButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //there is no point in remembering this the user will never be
                        //able to delete a bean!
                        dialog.dispose();
                    }
                });

                yesButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (remember.isSelected()) {
                            setDisplayDeleteMsg(0x02);
                        }
                        doDelete(t);
                        dialog.dispose();
                    }
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

    boolean noWarnDelete = false;

    /**
     * Delete the bean after all the checking has been done.
     * <P>
     * Separate so that it can be easily subclassed if other functionality is
     * needed.
     */
    void doDelete(NamedBean bean) {
        try {
            getManager().deleteBean(bean, "DoDelete");
        } catch (java.beans.PropertyVetoException e) {
            //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
            log.error(e.getMessage());
            return;
        }
    }

    /**
     * Configure a table to have our standard rows and columns. This is
     * optional, in that other table formats can use this table model. But we
     * put it here to help keep it consistent.
     *
     * @param table
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

        loadTableColumnDetails(table);

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
     * @param table
     * @param column
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
     */
    @SuppressWarnings("unchecked")
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
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
        String spaces = "";
        for (int i = 0; i < columnSize; i++) {
            spaces = spaces + " ";
        }
        for (int i = 0; i < this.getRowCount(); i++) {
            for (int j = 0; j < this.getColumnCount(); j++) {
                //check for special, non string contents
                if (this.getValueAt(i, j) == null) {
                    columnStrings[j] = spaces;
                } else if (this.getValueAt(i, j) instanceof JComboBox) {
                    columnStrings[j] = (String) ((JComboBox<String>) this.getValueAt(i, j)).getSelectedItem();
                } else if (this.getValueAt(i, j) instanceof Boolean) {
                    columnStrings[j] = (this.getValueAt(i, j)).toString();
                } else {
                    columnStrings[j] = (String) this.getValueAt(i, j);
                }
            }
            printColumns(w, columnStrings, columnSize);
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                    (columnSize + 1) * this.getColumnCount());
        }
        w.close();
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    protected void printColumns(HardcopyWriter w, String columnStrings[], int columnSize) {
        String columnString = "";
        String lineString = "";
        // create a base string the width of the column
        String spaces = "";
        for (int i = 0; i < columnSize; i++) {
            spaces = spaces + " ";
        }
        // loop through each column
        boolean complete = false;
        while (!complete) {
            complete = true;
            for (int i = 0; i < columnStrings.length; i++) {
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
                lineString = lineString + columnString + " ";
            }
            try {
                w.write(lineString);
                //write vertical dividing lines
                for (int i = 0; i < w.getCharactersPerLine(); i = i + columnSize + 1) {
                    w.write(w.getCurrentLineNumber(), i, w.getCurrentLineNumber() + 1, i);
                }
                lineString = "\n";
                w.write(lineString);
                lineString = "";
            } catch (IOException e) {
                log.warn("error during printing: " + e);
            }
        }
    }

    public JTable makeJTable(TableSorter sorter) {
        JTable table = new JTable(sorter) {
            /**
             *
             */
            private static final long serialVersionUID = 2511932624004472654L;

            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                boolean res = super.editCellAt(row, column, e);
                java.awt.Component c = this.getEditorComponent();
                if (c instanceof javax.swing.JTextField) {
                    ((JTextField) c).selectAll();
                }
                return res;
            }
        };
        table.getTableHeader().setReorderingAllowed(true);
        table.setColumnModel(new XTableColumnModel());
        table.createDefaultColumnsFromModel();

        addMouseListenerToHeader(table);
        return table;
    }

    abstract protected String getBeanType();/*{
     return "Bean";
     }*/


    class PopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }
    }

    protected void showPopup(MouseEvent e) {
        JTable source = (JTable) e.getSource();
        TableSorter tmodel = ((TableSorter) source.getModel());
        int row = source.rowAtPoint(e.getPoint());
        int column = source.columnAtPoint(e.getPoint());
        if (!source.isRowSelected(row)) {
            source.changeSelection(row, column, false, false);
        }
        final int rowindex = tmodel.modelIndex(row);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("CopyName"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyName(rowindex, 0);
            }
        });
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("Rename"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameBean(rowindex, 0);
            }
        });
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("Clear"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeName(rowindex, 0);
            }
        });
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("Move"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                moveBean(rowindex, 0);
            }
        });
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("Delete"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteBean(rowindex, 0);
            }
        });
        popupMenu.add(menuItem);

        popupMenu.show(e.getComponent(), e.getX(), e.getY());

    }

    class popupmenuRemoveName implements ActionListener {

        int row;

        popupmenuRemoveName(int row) {
            this.row = row;
        }

        public void actionPerformed(ActionEvent e) {
            deleteBean(row, 0);
        }
    }

    jmri.NamedBeanHandleManager nbMan = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

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
        Object[] renameBeanOption = {"Cancel", "OK", _newName};
        int retval = JOptionPane.showOptionDialog(null,
                "Rename UserName From " + oldName, "Rename " + getBeanType(),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                renameBeanOption, renameBeanOption[2]);

        if (retval != 1) {
            return;
        }
        String value = _newName.getText().trim();

        if (value.equals(oldName)) {
            //name not changed.
            return;
        } else {
            NamedBean nB = getByUserName(value);
            if (nB != null) {
                log.error("User name is not unique " + value);
                String msg;
                msg = Bundle.getMessage("WarningUserName", new Object[]{("" + value)});
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
                    } catch (jmri.JmriException ex) {
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

        JComboBox<String> box = new JComboBox<String>();
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
                new Object[]{"Cancel", "OK", box}, null);
        log.debug("Dialog value " + retval + " selected " + box.getSelectedIndex() + ":"
                + box.getSelectedItem());
        if (retval != 1) {
            return;
        }
        String entry = (String) box.getSelectedItem();
        NamedBean newNameBean = getBySystemName(entry);
        if (oldNameBean != newNameBean) {
            oldNameBean.setUserName("");
            newNameBean.setUserName(currentName);
            jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).moveBean(oldNameBean, newNameBean, currentName);
            if (nbMan.inUse(newNameBean.getSystemName(), newNameBean)) {
                String msg = Bundle.getMessage("UpdateToUserName", new Object[]{getBeanType(), currentName, sysNameList.get(row)});
                int optionPane = JOptionPane.showConfirmDialog(null, msg, Bundle.getMessage("UpdateToUserNameTitle"), JOptionPane.YES_NO_OPTION);
                if (optionPane == JOptionPane.YES_OPTION) {
                    try {
                        nbMan.updateBeanFromSystemToUser(newNameBean);
                    } catch (jmri.JmriException ex) {
                        //We should never get an exception here as we already check that the username is not valid
                    }
                }
            }
            fireTableRowsUpdated(row, row);
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
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
                menuItem.addActionListener(new headerActionListener(tc, tcm));
                popupMenu.add(menuItem);
            }

        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    static class headerActionListener implements ActionListener {

        TableColumn tc;
        XTableColumnModel tcm;

        headerActionListener(TableColumn tc, XTableColumnModel tcm) {
            this.tc = tc;
            this.tcm = tcm;
        }

        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            //Do not allow the last column to be hidden
            if (!check.isSelected() && tcm.getColumnCount(true) == 1) {
                return;
            }
            tcm.setColumnVisible(tc, check.isSelected());
        }
    }

    protected void addMouseListenerToHeader(JTable table) {
        MouseListener mouseHeaderListener = new TableHeaderListener(table);
        table.getTableHeader().addMouseListener(mouseHeaderListener);
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

    public void saveTableColumnDetails(JTable table) {
        saveTableColumnDetails(table, getMasterClassName());
    }

    public void saveTableColumnDetails(JTable table, String beantableref) {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        XTableColumnModel tcm = (XTableColumnModel) table.getColumnModel();
        TableSorter tmodel = ((TableSorter) table.getModel());
        Enumeration<TableColumn> en = tcm.getColumns(false);
        while (en.hasMoreElements()) {
            TableColumn tc = en.nextElement();

            try {
                String columnName = (String) tc.getHeaderValue();
                //skip empty or blank columns
                if (columnName != null && !columnName.equals("")) {
                    int index = tcm.getColumnIndex(tc.getIdentifier(), false);
                    p.setTableColumnPreferences(beantableref, columnName, index, tc.getPreferredWidth(), tmodel.getSortingStatus(tc.getModelIndex()), !tcm.isColumnVisible(tc));
                }
            } catch (Exception e) {
                log.warn("unable to store settings for table column " + tc.getHeaderValue());
                e.printStackTrace();
            }
        }
    }

    public void loadTableColumnDetails(JTable table) {
        loadTableColumnDetails(table, getMasterClassName());
    }

    public void loadTableColumnDetails(JTable table, String beantableref) {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        //Set all the sort and width details of the table first.

        //Reorder the columns first
        for (int i = 0; i < table.getColumnCount(); i++) {
            String columnName = p.getTableColumnAtNum(beantableref, i);
            if (columnName != null) {
                int originalLocation = -1;
                for (int j = 0; j < table.getColumnCount(); j++) {
                    if (table.getColumnName(j).equals(columnName)) {
                        originalLocation = j;
                        break;
                    }
                }
                if (originalLocation != -1 && (originalLocation != i)) {
                    table.moveColumn(originalLocation, i);
                }
            }
        }

        //Set column widths, sort order and hidden status
        XTableColumnModel tcm = (XTableColumnModel) table.getColumnModel();
        Enumeration<TableColumn> en = tcm.getColumns(false);
        //jtable.setDefaultEditor(Object.class, new RosterCellEditor());
        TableSorter tmodel = ((TableSorter) table.getModel());
        while (en.hasMoreElements()) {
            TableColumn tc = en.nextElement();
            String columnName = (String) tc.getHeaderValue();
            if (p.getTableColumnWidth(beantableref, columnName) != -1) {
                int width = p.getTableColumnWidth(beantableref, columnName);
                tc.setPreferredWidth(width);

                int sort = p.getTableColumnSort(beantableref, columnName);
                tmodel.setSortingStatus(tc.getModelIndex(), sort);

                if (p.getTableColumnHidden(beantableref, columnName)) {
                    tcm.setColumnVisible(tc, false);
                } else {
                    tcm.setColumnVisible(tc, true);
                }

            }
        }
    }
    private final static Logger log = LoggerFactory.getLogger(BeanTableDataModel.class.getName());

}
