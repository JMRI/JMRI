package jmri.jmrit.logixng.tools.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import jmri.InstanceManager;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;

/**
 * Editor for LogixNG Tables
 *
 * @author Dave Duchamp Copyright (C) 2007  (ConditionalListEdit)
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011  (ConditionalListEdit)
 * @author Matthew Harris copyright (c) 2009  (ConditionalListEdit)
 * @author Dave Sand copyright (c) 2017  (ConditionalListEdit)
 * @author Daniel Bergqvist (c) 2019
 */
    public final class TableEditor implements AbstractLogixNGEditor<NamedTable> {

    private NamedTableManager _tableManager = null;
    private NamedTable _curTable = null;

    private boolean _inEditMode = false;

    private boolean _showReminder = false;

    private final SymbolTable symbolTable = new DefaultSymbolTable();

//    private final JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));   // NOI18N
//    private final JLabel _sysNameLabel = new JLabel(Bundle.getMessage("SystemName") + ":");  // NOI18N
//    private final JLabel _userNameLabel = new JLabel(Bundle.getMessage("UserName") + ":");   // NOI18N
//    private final String systemNameAuto = this.getClass().getName() + ".AutoSystemName";         // NOI18N
//    private final JTextField _systemName = new JTextField(20);
//    private final JTextField _addUserName = new JTextField(20);

//    private NamedBeanComboBox<NamedTable> _nameComboBox = null;


    /**
     * Create a new ConditionalNG List View editor.
     *
     * @param m the bean table model
     * @param sName name of the NamedTable being edited
     */
    public TableEditor(BeanTableDataModel<NamedTable> m, String sName) {
        _tableManager = InstanceManager.getDefault(jmri.jmrit.logixng.NamedTableManager.class);
        _curTable = _tableManager.getBySystemName(sName);
        makeEditTableWindow();
    }

    // ------------ NamedTable Variables ------------
    private JmriJFrame _editLogixNGFrame = null;
    private final JTextField editUserName = new JTextField(20);
    private final JTextField editCsvTableName = new JTextField(40);
//    private JLabel status = new JLabel(" ");

    // ------------ ConditionalNG Variables ------------
    private TableTableModel tableTableModel = null;
//    private ConditionalNG _curConditionalNG = null;
//    private int _conditionalRowNumber = 0;
//    private boolean _inReorderMode = false;
//    private boolean _inActReorder = false;
//    private boolean _inVarReorder = false;
//    private int _nextInOrder = 0;

    // ------------ Select NamedTable/ConditionalNG Variables ------------
//    private JPanel _selectLogixNGPanel = null;
//    private JPanel _selectConditionalNGPanel = null;
//    private JComboBox<String> _selectLogixNGComboBox = new JComboBox<>();
//    private JComboBox<String> _selectConditionalNGComboBox = new JComboBox<>();
//    private TreeMap<String, String> _selectLogixNGMap = new TreeMap<>();
//    private ArrayList<String> _selectConditionalNGList = new ArrayList<>();

    // ------------ Edit ConditionalNG Variables ------------
//    private boolean _inEditConditionalNGMode = false;
//    private JmriJFrame _editConditionalNGFrame = null;
//    private JTextField _conditionalUserName = new JTextField(22);
//    private JRadioButton _triggerOnChangeButton;

    // ------------ Methods for Edit NamedTable Pane ------------

    private JButton createFileChooser() {
        JButton selectFileButton = new JButton("..."); // "File" replaced by ...
        selectFileButton.setMaximumSize(selectFileButton.getPreferredSize());
        selectFileButton.setToolTipText(Bundle.getMessage("TableEdit_FileButtonHint"));  // NOI18N
        selectFileButton.addActionListener((ActionEvent e) -> {
            JFileChooser csvFileChooser = new JFileChooser(FileUtil.getUserFilesPath());
            csvFileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv", "txt")); // NOI18N
            csvFileChooser.rescanCurrentDirectory();
            int retVal = csvFileChooser.showOpenDialog(null);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                // set selected file location
                try {
                    editCsvTableName.setText(FileUtil.getPortableFilename(csvFileChooser.getSelectedFile().getCanonicalPath()));
                } catch (java.io.IOException ex) {
                    log.error("exception setting file location", ex);  // NOI18N
                    editCsvTableName.setText("");
                }
            }
        });
        return selectFileButton;
    }


    /**
     * Create and/or initialize the Edit NamedTable pane.
     */
    private void makeEditTableWindow() {
        editUserName.setText(_curTable.getUserName());
        // clear conditional table if needed
        if (tableTableModel != null) {
            tableTableModel.fireTableStructureChanged();
        }
        _inEditMode = true;
        if (_editLogixNGFrame == null) {
            if (_curTable.getUserName() != null) {
                _editLogixNGFrame = new JmriJFrame(
                        Bundle.getMessage("TitleEditLogixNG2",
                                _curTable.getSystemName(),   // NOI18N
                                _curTable.getUserName()),    // NOI18N
                        false,
                        false);
            } else {
                _editLogixNGFrame = new JmriJFrame(
                        Bundle.getMessage("TitleEditLogixNG", _curTable.getSystemName()),  // NOI18N
                        false,
                        false);
            }
            _editLogixNGFrame.addHelpMenu(
                    "package.jmri.jmrit.logixng.LogixNGTableTableEditor", true);  // NOI18N
            _editLogixNGFrame.setLocation(100, 30);
            Container contentPane = _editLogixNGFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel systemNameLabel = new JLabel(Bundle.getMessage("ColumnSystemName") + ":");  // NOI18N
            panel1.add(systemNameLabel);
            JLabel fixedSystemName = new JLabel(_curTable.getSystemName());
            panel1.add(fixedSystemName);
            contentPane.add(panel1);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel userNameLabel = new JLabel(Bundle.getMessage("ColumnUserName") + ":");  // NOI18N
            panel2.add(userNameLabel);
            panel2.add(editUserName);
            editUserName.setToolTipText(Bundle.getMessage("LogixNGUserNameHint2"));  // NOI18N
            contentPane.add(panel2);

            boolean isCsvTable = _curTable instanceof DefaultCsvNamedTable;

            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel tableTypeLabel = new JLabel(Bundle.getMessage("TableEditor_TableType") + ": ");  // NOI18N
            panel3.add(tableTypeLabel);
            panel3.add(new JLabel(
                    isCsvTable
                            ? Bundle.getMessage("TableEditor_CsvFile")
                            : Bundle.getMessage("TableEditor_UnknownTableType")));
            contentPane.add(panel3);

            if (isCsvTable) {
                JPanel panel4 = new JPanel();
                panel4.setLayout(new FlowLayout());
                JLabel tableFileNameLabel = new JLabel(Bundle.getMessage("TableEditor_FileName") + ": ");  // NOI18N
                panel4.add(tableFileNameLabel);
//                panel4.add(new JLabel(((DefaultCsvNamedTable)_curTable).getFileName()));
                editCsvTableName.setText(((DefaultCsvNamedTable)_curTable).getFileName());
                panel4.add(editCsvTableName);
//                editCsvTableName.setToolTipText(Bundle.getMessage("LogixNGUserNameHint2"));  // NOI18N
                panel4.add(createFileChooser());
                contentPane.add(panel4);
            }


            // add table of Tables
            JPanel pctSpace = new JPanel();
            pctSpace.setLayout(new FlowLayout());
            pctSpace.add(new JLabel("   "));
            contentPane.add(pctSpace);
            JPanel pTitle = new JPanel();
            pTitle.setLayout(new FlowLayout());
//            pTitle.add(new JLabel(Bundle.getMessage("ConditionalNGTableTitle")));  // NOI18N
            contentPane.add(pTitle);
            // initialize table of conditionals
            tableTableModel = new TableTableModel();
            JTable tableTable = new JTable(tableTableModel);
            tableTable.setCellSelectionEnabled(true);
            tableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tableTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
            tableTable.getTableHeader().setReorderingAllowed(false);

            JButton cellRefByIndexButton = new JButton(Bundle.getMessage("TableEditor_CopyToClipboard"));  // NOI18N
            JLabel cellRefByIndexLabel = new JLabel();  // NOI18N
            JTextField cellRefByIndex = new JTextField();
            cellRefByIndex.setEditable(false);
            cellRefByIndexButton.setEnabled(false);

            JButton cellRefByHeaderButton = new JButton(Bundle.getMessage("TableEditor_CopyToClipboard"));  // NOI18N
            JLabel cellRefByHeaderLabel = new JLabel();  // NOI18N
            JTextField cellRefByHeader = new JTextField();
            cellRefByHeader.setEditable(false);
            cellRefByHeaderButton.setEnabled(false);

            java.awt.datatransfer.Clipboard clipboard =
                    Toolkit.getDefaultToolkit().getSystemClipboard();

            cellRefByIndexButton.addActionListener(
                    (evt) -> { clipboard.setContents(new StringSelection(cellRefByIndexLabel.getText()), null);});

            cellRefByHeaderButton.addActionListener(
                    (evt) -> { clipboard.setContents(new StringSelection(cellRefByHeaderLabel.getText()), null);});

            ListSelectionListener selectCellListener = (evt) -> {
                String refByIndex = String.format("{%s[%d,%d]}", _curTable.getDisplayName(), tableTable.getSelectedRow()+1, tableTable.getSelectedColumn()+1);
                cellRefByIndexLabel.setText(refByIndex);  // NOI18N
                cellRefByIndex.setText(ReferenceUtil.getReference(symbolTable, refByIndex));  // NOI18N
                cellRefByIndexButton.setEnabled(true);

                Object rowHeaderObj = _curTable.getCell(tableTable.getSelectedRow()+1, 0);
                Object columnHeaderObj = _curTable.getCell(0, tableTable.getSelectedColumn()+1);
                String rowHeader = rowHeaderObj != null ? rowHeaderObj.toString() : "";
                String columnHeader = columnHeaderObj != null ? columnHeaderObj.toString() : "";
                if (!rowHeader.isEmpty() && !columnHeader.isEmpty()) {
                    cellRefByHeaderButton.setEnabled(true);
                    String refByHeader = String.format("{%s[%s,%s]}", _curTable.getDisplayName(), _curTable.getCell(tableTable.getSelectedRow()+1,0), _curTable.getCell(0,tableTable.getSelectedColumn()+1));
                    cellRefByHeaderLabel.setText(refByHeader);  // NOI18N
                    cellRefByHeader.setText(ReferenceUtil.getReference(symbolTable, refByIndex));  // NOI18N
                } else {
                    cellRefByHeaderButton.setEnabled(false);
                    cellRefByHeaderLabel.setText("");    // NOI18N
                    cellRefByHeader.setText("");        // NOI18N
                }
            };
            tableTable.getSelectionModel().addListSelectionListener(selectCellListener);
            tableTable.getColumnModel().getSelectionModel().addListSelectionListener(selectCellListener);

            ListModel<Object> lm = new RowHeaderListModel();

            JList<Object> rowHeader = new JList<>(lm);
            rowHeader.setFixedCellHeight(
                    tableTable.getRowHeight()
//                    tableTable.getRowHeight() + tableTable.getRowMargin()
//                    + table.getIntercellSpacing().height
            );
            rowHeader.setCellRenderer(new RowHeaderRenderer(tableTable));

            JScrollPane tableTableScrollPane = new JScrollPane(tableTable);
            tableTableScrollPane.setRowHeaderView(rowHeader);
            Dimension dim = tableTable.getPreferredSize();
            dim.height = 450;
            tableTableScrollPane.getViewport().setPreferredSize(dim);
            contentPane.add(tableTableScrollPane);

            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            panel4.add(cellRefByIndexButton);
            panel4.add(cellRefByIndexLabel);
            panel4.add(cellRefByIndex);
            contentPane.add(panel4);

            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(cellRefByHeaderButton);
            panel5.add(cellRefByHeaderLabel);
            panel5.add(cellRefByHeader);
            contentPane.add(panel5);

            // add buttons at bottom of window
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            // Bottom Buttons - Cancel NamedTable
            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N
            panel6.add(cancelButton);
            cancelButton.addActionListener((e) -> {
                finishDone();
            });
//            done.setToolTipText(Bundle.getMessage("CancelButtonHint"));  // NOI18N
            // Bottom Buttons - Ok NamedTable
            JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
            panel6.add(okButton);
            okButton.addActionListener((e) -> {
                okPressed(e);
            });
//            done.setToolTipText(Bundle.getMessage("OkButtonHint"));  // NOI18N
            // Delete NamedTable
            JButton delete = new JButton(Bundle.getMessage("ButtonDelete"));  // NOI18N
            panel6.add(delete);
            delete.addActionListener((e) -> {
                deletePressed();
            });
            delete.setToolTipText(Bundle.getMessage("DeleteLogixNGButtonHint"));  // NOI18N
            contentPane.add(panel6);
        }

        _editLogixNGFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (_inEditMode) {
                    okPressed(null);
                } else {
                    finishDone();
                }
            }
        });
        _editLogixNGFrame.pack();
        _editLogixNGFrame.setVisible(true);
    }

    @Override
    public void bringToFront() {
        if (_editLogixNGFrame != null) {
            _editLogixNGFrame.setVisible(true);
        }
    }

    /**
     * Display reminder to save.
     */
    void showSaveReminder() {
        if (_showReminder) {
            if (InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class) != null) {
                InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage(Bundle.getMessage("ReminderTitle"), // NOI18N
                                Bundle.getMessage("ReminderSaveString", // NOI18N
                                        Bundle.getMessage("MenuItemLogixNGTable")), // NOI18N
                                getClassName(),
                                "remindSaveLogixNG"); // NOI18N
            }
        }
    }

    /**
     * Respond to the Ok button in the Edit NamedTable window.
     * <p>
     * Note: We also get here if the Edit NamedTable window is dismissed, or if the
     * Add button is pressed in the LogixNG Table with an active Edit NamedTable
     * window.
     *
     * @param e The event heard
     */
    private void okPressed(ActionEvent e) {
//        if (checkEditConditionalNG()) {
//            return;
//        }
        // Check if the User Name has been changed
        String uName = editUserName.getText().trim();
        if (!(uName.equals(_curTable.getUserName()))) {
            // user name has changed - check if already in use
            if (uName.length() > 0) {
                NamedTable p = _tableManager.getByUserName(uName);
                if (p != null) {
                    // NamedTable with this user name already exists
                    log.error("Failure to update NamedTable with Duplicate User Name: " // NOI18N
                            + uName);
                    JOptionPane.showMessageDialog(_editLogixNGFrame,
                            Bundle.getMessage("Error6"),
                            Bundle.getMessage("ErrorTitle"), // NOI18N
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // user name is unique, change it
            // user name is unique, change it
            tableData.clear();
            tableData.put("chgUname", uName);  // NOI18N
            fireEditorEvent();
        }
        if (_curTable instanceof DefaultCsvNamedTable) {
            String csvFileName = editCsvTableName.getText().trim();

            try {
                // NamedTable does not exist, create a new NamedTable
                AbstractNamedTable.loadTableFromCSV_File(
                        "IQT1",     // Arbitrary LogixNG table name
//                        InstanceManager.getDefault(NamedTableManager.class).getAutoSystemName(),
                        null, csvFileName, false);
            } catch (java.nio.file.NoSuchFileException ex) {
                log.error("Cannot load table due since the file is not found", ex);
                JOptionPane.showMessageDialog(_editLogixNGFrame,
                        Bundle.getMessage("TableEditor_Error_FileNotFound", csvFileName),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return;
            } catch (IOException ex) {
                log.error("Cannot load table due to I/O error", ex);
                JOptionPane.showMessageDialog(_editLogixNGFrame,
                        ex.getLocalizedMessage(),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return;
            } catch (RuntimeException ex) {
                log.error("Cannot load table due to an error", ex);
                JOptionPane.showMessageDialog(_editLogixNGFrame,
                        ex.getLocalizedMessage(),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            ((DefaultCsvNamedTable)_curTable).setFileName(csvFileName);
        }
        // complete update and activate NamedTable
        finishDone();
    }

    void finishDone() {
        showSaveReminder();
        _inEditMode = false;
        if (_editLogixNGFrame != null) {
            _editLogixNGFrame.setVisible(false);
            _editLogixNGFrame.dispose();
            _editLogixNGFrame = null;
        }
        tableData.clear();
        tableData.put("Finish", _curTable.getSystemName());   // NOI18N
        fireEditorEvent();
    }

    /**
     * Respond to the Delete button in the Edit NamedTable window.
     */
    void deletePressed() {
/*
        if (!checkConditionalNGReferences(_curLogixNG.getSystemName())) {
            return;
        }
*/
        _showReminder = true;
        tableData.clear();
        tableData.put("Delete", _curTable.getSystemName());   // NOI18N
        fireEditorEvent();
        finishDone();
    }

    // ------------ Table Models ------------

    /**
     * Table model for Tables in the Edit NamedTable pane.
     */
    public final class TableTableModel extends AbstractTableModel {

        @Override
        public int getColumnCount() {
            return _curTable.numColumns();
        }

        @Override
        public int getRowCount() {
            return _curTable.numRows();
        }

        @Override
        public String getColumnName(int col) {
            Object data = _curTable.getCell(0, col+1);
            return data != null ? data.toString() : "<null>";
        }

        @Override
        public Object getValueAt(int row, int col) {
            return _curTable.getCell(row+1, col+1);
        }
    }

    private class RowHeaderListModel extends AbstractListModel<Object> {
        @Override
        public int getSize() {
            return _curTable.numRows();
        }

        @Override
        public Object getElementAt(int index) {
            // Ensure the header has at least five characters and ensure
            // there are at least two spaces at the end since the last letter
            // doesn't fully fit at the row.
            Object data = _curTable.getCell(index+1, 0);
            String padding = "  ";     // Two spaces
            String str = data != null ? data.toString().concat(padding) : padding;
            return str.length() < 5 ? str.concat("     ").substring(0, 7) : str;
        }
    }

    private static final class RowHeaderRenderer extends JLabel implements ListCellRenderer<Object> {

        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(CENTER);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    protected String getClassName() {
        return TableEditor.class.getName();
    }


    // ------------ NamedTable Notifications ------------
    // The Table views support some direct changes to the parent logix.
    // This custom event is used to notify the parent NamedTable that changes are requested.
    // When the event occurs, the parent NamedTable can retrieve the necessary information
    // to carry out the actions.
    //
    // 1) Notify the calling NamedTable that the NamedTable user name has been changed.
    // 2) Notify the calling NamedTable that the table view is closing
    // 3) Notify the calling NamedTable that it is to be deleted
    /**
     * Create a custom listener event.
     */
    public interface TableEventListener extends EventListener {

        void tableEventOccurred();
    }

    /**
     * Maintain a list of listeners -- normally only one.
     */
    List<EditorEventListener> listenerList = new ArrayList<>();

    /**
     * This contains a list of commands to be processed by the listener
     * recipient.
     */
    private final HashMap<String, String> tableData = new HashMap<>();

    /**
     * Add a listener.
     *
     * @param listener The recipient
     */
    @Override
    public void addEditorEventListener(EditorEventListener listener) {
        listenerList.add(listener);
    }

    /**
     * Remove a listener -- not used.
     *
     * @param listener The recipient
     */
    @Override
    public void removeEditorEventListener(EditorEventListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Notify the listeners to check for new data.
     */
    private void fireEditorEvent() {
        for (EditorEventListener l : listenerList) {
            l.editorEventOccurred(tableData);
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TableEditor.class);

}
