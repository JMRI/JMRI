package jmri.jmrit.display;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.*;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.NamedTable.NamedTablePropertyChangeEvent;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.implementation.*;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.swing.*;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * An icon to display a NamedTable and let the user edit it.
 *
 * @author Pete Cressman    Copyright (c) 2009
 * @author Daniel Bergqvist Copyright (C) 2025
 * @since 5.15.1
 */
public final class LogixNGTableIcon extends PositionableJPanel {

    private TableModel _tableModel = null;

    // the associated NamedTable object
    private final JTable _table;
    private final JList<Object> _rowHeader;
    private final JScrollPane _scrollPane;

    private final java.awt.event.MouseListener _mouseListener = JmriMouseListener.adapt(this);

    public LogixNGTableIcon(String tableName, Editor editor) {
        super(editor);
        setDisplayLevel(Editor.LABELS);

        setLayout(new java.awt.BorderLayout());

        _tableModel = new TableModel(tableName);

        _table = new JTable(_tableModel);
        _table.setCellSelectionEnabled(true);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        _table.getTableHeader().setReorderingAllowed(false);

        for (int col=0; col < _tableModel.getColumnCount(); col++) {
            TableModel.HeaderType headerType = _tableModel._headers[col];
            if (headerType._cellEditor != null) {
                _table.getColumnModel().getColumn(col).setCellEditor(headerType._cellEditor);
            }
        }

        ButtonRenderer buttonRenderer = new ButtonRenderer();
        _table.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor editButEditor = new ButtonEditor(new JButton());
        _table.setDefaultEditor(JButton.class, editButEditor);

        ListModel<Object> lm = new RowHeaderListModel();

        _rowHeader = new JList<>(lm);
        _rowHeader.setFixedCellHeight(
                _table.getRowHeight()
        );
        _rowHeader.setCellRenderer(new RowHeaderRenderer(_table));

        _scrollPane = new JScrollPane(_table);
        _scrollPane.setRowHeaderView(_rowHeader);
        add(_scrollPane, BorderLayout.CENTER);

        _table.addMouseListener(_mouseListener);
        _rowHeader.addMouseListener(_mouseListener);
        _scrollPane.addMouseListener(_mouseListener);
        setPopupUtility(new PositionablePopupUtil(this, _table));
    }

    @Override
    public Positionable deepClone() {
        LogixNGTableIcon pos = new LogixNGTableIcon(
                _tableModel.getTable().getDisplayName(), _editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(LogixNGTableIcon pos) {
        _tableModel.setTable(_tableModel.getTable());
        return super.finishClone(pos);
    }

    public TableModel getTableModel() {
        return _tableModel;
    }

    public JTable getJTable() {
        return _table;
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_LogixNGTableIcon");
    }

    @Override
    public String getNameString() {
        String name;
        if (_tableModel.getTable() == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = _tableModel.getTable().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    private void configureLogixNGTable() {
        NamedTable namedTable = _tableModel.getTable();

        JComboBox<ModuleItem> _validateModuleComboBox;
        _validateModuleComboBox = new JComboBox<>();
        _validateModuleComboBox.addItem(new ModuleItem(null));
        for (Module m : InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet()) {
            if ("DefaultFemaleDigitalActionSocket".equals(m.getRootSocketType().getName())
                    && m.isVisible()) {
                ModuleItem mi = new ModuleItem(m);
                _validateModuleComboBox.addItem(mi);
                Module _validateModule = _tableModel.getValidateModule();
                if (_validateModule == m) {
                    _validateModuleComboBox.setSelectedItem(mi);
                }
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(_validateModuleComboBox);

        Map<String,Integer> columnIndexes = new HashMap<>();
        List<String> columns = new ArrayList<>();
        for (int col=0; col < namedTable.numColumns(); col++) {
            String header = getObjectAsString(namedTable.getCell(0, col+1));
            if (!header.isEmpty()) {
                columns.add(header);
                columnIndexes.put(header, col);
            }
        }
        JList<String> columnList = new JList<>(columns.toArray(new String[0]));
        for (String header : _tableModel._editableColumnsList) {
            int index = columnIndexes.getOrDefault(header,-1);
            if (index != -1) {
                columnList.getSelectionModel().addSelectionInterval(index, index);
            }
        }
        columnList.setBorder(BorderFactory.createLineBorder(Color.black));

        JDialog dialog = new JDialog();
        dialog.setTitle(Bundle.getMessage("ConfigureLogixNGTable"));
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        JPanel p;
        p = new JPanel();
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        //c.gridx = 0;
        c.gridy = 1;
        JLabel validateLabel = new JLabel(Bundle.getMessage("LogixNGTableIcon_ValidateLogixNGModule"));
        p.add(validateLabel, c);
        validateLabel.setLabelFor(_validateModuleComboBox);
        c.gridy = 2;
        JLabel allowEditLabel = new JLabel(Bundle.getMessage("LogixNGTableIcon_AllowEditTable"));
        JCheckBox allowEdit = new JCheckBox();
        allowEdit.setSelected(_tableModel.isEditable());
        p.add(allowEditLabel, c);
        allowEditLabel.setLabelFor(allowEdit);
        c.gridy = 3;
        p.add(new JLabel(Bundle.getMessage("LogixNGTableIcon_EditableColumns")), c);
        c.gridx = 1;
        c.gridy = 0;
        p.add(Box.createHorizontalStrut(5), c);
        c.gridx = 2;
        c.gridy = 2;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(allowEdit, c);
        c.gridx = 2;
        c.gridy = 1;
        p.add(_validateModuleComboBox, c);
//        sys.setToolTipText(Bundle.getMessage("SysNameToolTip", "Y"));
        c.gridy = 3;
        p.add(new JScrollPane(columnList), c);
        c.gridy = 4;
        p.add(Box.createVerticalStrut(5), c);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 3;
        p.add(new JLabel(Bundle.getMessage("LogixNGTableIcon_EditableColumnsInfo")), c);
        add(p);

        // cancel + add buttons at bottom of window
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new FlowLayout(FlowLayout.TRAILING));

        JButton cancel;
        panelBottom.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((evt) -> {
            dialog.dispose();
        });

        JButton ok;
//        panelBottom.add(ok = new JButton(Bundle.getMessage(addButtonLabel)));
        panelBottom.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((evt) -> {
            _tableModel.setValidateModule(_validateModuleComboBox.getItemAt(_validateModuleComboBox.getSelectedIndex())._module);
            _tableModel.setEditable(allowEdit.isSelected());
            _tableModel.setEditableColumns(String.join("\t", columnList.getSelectedValuesList()));
            dialog.dispose();
        });
//        p.add(panelBottom);

        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 99;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.fill = java.awt.GridBagConstraints.NONE;
        p.add(panelBottom, c);
        add(p);

        dialog.getContentPane().add(p);
        dialog.pack();
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    @Override
    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameLogixNGTable"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });

        txt = Bundle.getMessage("ConfigureLogixNGTable");
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                configureLogixNGTable();
            }
        });
        return true;
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "LogixNGTable", true, _iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.namedTablePickModelInstance());
        ActionListener addIconAction = a -> editNamedTable();
        _iconEditor.makeIconPanel(false);
        _iconEditor.complete(addIconAction, false, false, true);
        _iconEditor.setSelection(_tableModel.getTable());
    }

    void editNamedTable() {
        _tableModel.setTable(_iconEditor.getTableSelection().getDisplayName());
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        validate();
    }

    @Override
    void cleanup() {
        if (_scrollPane != null) {
            _table.removeMouseListener(_mouseListener);
            _rowHeader.removeMouseListener(_mouseListener);
            _scrollPane.removeMouseListener(_mouseListener);
        }
        _tableModel.setTable((NamedTable)null);
        _tableModel.setValidateModule(null);
    }

    private static String getObjectAsString(Object o) {
        return o != null ? o.toString() : "";
    }

    @Override
    public void remove() {
        _tableModel.dispose();
    }

    // ------------ Table Models ------------

    /**
     * Table model for Tables in the Edit NamedTable pane.
     */
    public final class TableModel extends AbstractTableModel
            implements PropertyChangeListener {

        private class HeaderType {
            Class<?> _class;
            String _type;
            String _parameters;
            String _header;
            TableCellEditor _cellEditor;
        }

        private NamedBeanHandle<NamedTable> _namedTable;
        private NamedBeanHandle<Module> _validateModule;
        private boolean _editable = false;
        private String _editableColumns = "";
        List<String> _editableColumnsList;

        private final LogixNG validateModuleLogixNG = new DefaultLogixNG("IQ:JMRI:LogixNGTableIcon", null);
        private final ConditionalNG validateModuleConditionalNG =
                new DefaultConditionalNG("IQC:JMRI:LogixNGTableIcon", null, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        private final DigitalCallModule validateModuleAction = new DigitalCallModule("IQDA:JMRI:LogixNGTableIcon", null);
        private Map<String, Object> _variablesWithValues;
        private final MyData _myData = new MyData();
        private final HeaderType[] _headers;


        public TableModel(String tableName) {
            initCallModule();
            setEditableColumns("");
            setTable(tableName);

            if (_namedTable != null) {
                _headers = new HeaderType[_namedTable.getBean().numColumns()];
                initHeaders();
            } else {
                _headers = null;
            }
        }

        private void initHeaders() {
            for (int col=0; col < _namedTable.getBean().numColumns(); col++) {
                HeaderType headerType = new HeaderType();
                _headers[col] = headerType;

                Object headerObj = _namedTable.getBean().getCell(0, col+1);
                String header;
                if (headerObj != null) {
                    header = headerObj.toString();
                } else {
                    header = "";
                }
                if (header.startsWith("{{{")) {
                    Pattern pattern = Pattern.compile("\\{\\{\\{(.+?)(\\:.+?)?\\}\\}\\}(.*)");
                    Matcher matcher = pattern.matcher(header);
                    if (matcher.matches()) {
                        headerType._header = matcher.group(3);
                        headerType._parameters = matcher.group(2);
                        headerType._type = matcher.group(1).toLowerCase();
                        switch (headerType._type) {
                            case "button":
                                headerType._class = JButton.class;
                                break;
                            case "list":
                                String comboBoxTableName = headerType._parameters.substring(1);
                                NamedTable comboBoxTable = InstanceManager
                                        .getDefault(NamedTableManager.class)
                                        .getNamedTable(comboBoxTableName);
                                if (comboBoxTable != null) {
                                    headerType._class = JComboBox.class;
                                    JComboBox<String> comboBox = new JComboBox<>();
                                    for (int row=1; row <= comboBoxTable.numRows(); row++) {
                                        Object val = comboBoxTable.getCell(row, 1);
                                        String value = val != null ? val.toString() : "";
                                        comboBox.addItem(value);
                                    }
                                    headerType._cellEditor = new DefaultCellEditor(comboBox);
                                } else {
                                    log.warn("Cannot load LogixNG Table: {}", comboBoxTableName);
                                }
                                break;
                            default:
                                log.warn("Unknown column type: {}", matcher.group(1));
                                headerType._type = null;
                        }
                    } else {
                        log.warn("Unknown column definition: {}", header);
                    }
                } else {
                    _headers[col]._header = header;
                }
            }
        }

        public NamedTable getTable() {
            return _namedTable != null ? _namedTable.getBean() : null;
        }

        public void setTable(String tableName) {
            if (_namedTable != null) {
                _namedTable.getBean().removePropertyChangeListener(this);
            }

            NamedTable table = null;
            if (tableName != null) {
                table = InstanceManager.getDefault(NamedTableManager.class)
                        .getNamedTable(tableName);
            }

            if (table != null) {
                _namedTable = InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(table.getDisplayName(), table);
                if (_namedTable != null) {
                    _namedTable.getBean().addPropertyChangeListener(this);
                }
            } else {
                _namedTable = null;
            }

            fireTableStructureChanged();
        }

        public void setTable(NamedTable table) {
            if (table != null) {
                _namedTable = InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(table.getDisplayName(), table);
            } else {
                _namedTable = null;
            }
            fireTableStructureChanged();
        }

        public Module getValidateModule() {
            return _validateModule != null ? _validateModule.getBean() : null;
        }

        public void setValidateModule(Module module) {
            if (module != null) {
                _validateModule = InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(module.getDisplayName(), module);
            } else {
                _validateModule = null;
            }
        }

        public boolean isEditable() {
            return _editable;
        }

        public void setEditable(boolean editable) {
            _editable = editable;
        }

        public String getEditableColumns() {
            return _editableColumns;
        }

        public void setEditableColumns(String editableColumns) {
            _editableColumns = editableColumns;
            _editableColumnsList = Arrays.asList(_editableColumns.split("\t"));
        }

        @Override
        public int getColumnCount() {
            if (_namedTable != null) {
                return _namedTable.getBean().numColumns();
            } else {
                return 0;
            }
        }

        @Override
        public int getRowCount() {
            if (_namedTable != null) {
                return _namedTable.getBean().numRows();
            } else {
                return 0;
            }
        }

        @Override
        public String getColumnName(int col) {
            return _headers[col]._header;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (_namedTable == null) {
                return null;
            }
            return _namedTable.getBean().getCell(row+1, col+1);
        }

        @Override
        public void setValueAt(Object val, int row, int col) {
            callValidateModule(val, row, col);
        }

        @Override
        public Class<?> getColumnClass(int col) {
            Class<?> clazz = _headers[col]._class;
            if (clazz != null) {
                return clazz;
            }
            return super.getColumnClass(col);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (!_editable) {
                return false;
            }

            String columnHeader = getObjectAsString(_namedTable.getBean().getCell(0, columnIndex+1));
            boolean allowColumn = _editableColumns.isEmpty();
            allowColumn |= _editableColumnsList.contains(columnHeader);

            return allowColumn;
        }

        private void initCallModule() {
            validateModuleLogixNG.addConditionalNG(validateModuleConditionalNG);

            DigitalMany many = new DigitalMany("IQDA:JMRI:LogixNGTableIcon", null);
            MaleSocket maleSocketMany = new DefaultMaleDigitalActionSocket(
                    InstanceManager.getDefault(DigitalActionManager.class), many);
            many.setParent(maleSocketMany);

            maleSocketMany.addLocalVariable("iconId", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("tableName", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("row", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("column", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("type", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("oldValue", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("newValue", SymbolTable.InitialValueType.String, null);

            try {
                validateModuleConditionalNG.getFemaleSocket().connect(maleSocketMany);
            } catch (SocketAlreadyConnectedException e) {
                log.error("Exception when creating error handling LogixNG: ", e);
            }

            SetLocalVariables setLocalVariables = new SetLocalVariables("IQDA:JMRI:LogixNGTableIcon", null);
            MaleSocket maleSocketSetLocalVariables = new DefaultMaleDigitalActionSocket(
                    InstanceManager.getDefault(DigitalActionManager.class), setLocalVariables);
            setLocalVariables.setParent(maleSocketSetLocalVariables);
            _variablesWithValues = setLocalVariables.getMap();

            try {
                maleSocketMany.getChild(maleSocketMany.getChildCount()-1).connect(maleSocketSetLocalVariables);
            } catch (SocketAlreadyConnectedException e) {
                log.error("Exception when creating error handling LogixNG: ", e);
            }

            RunFinally runFinally = new RunFinally("IQDA:JMRI:LogixNGTableIcon", null, this::handleResult, _myData);
            MaleSocket maleSocketRunFinally = new DefaultMaleDigitalActionSocket(
                    InstanceManager.getDefault(DigitalActionManager.class), runFinally);
            runFinally.setParent(maleSocketRunFinally);

            try {
                maleSocketMany.getChild(maleSocketMany.getChildCount()-1).connect(maleSocketRunFinally);
            } catch (SocketAlreadyConnectedException e) {
                log.error("Exception when creating error handling LogixNG: ", e);
            }

            validateModuleAction.addParameter("__iconId__", SymbolTable.InitialValueType.LocalVariable, "iconId", Module.ReturnValueType.None, null);
            validateModuleAction.addParameter("__tableName__", SymbolTable.InitialValueType.LocalVariable, "tableName", Module.ReturnValueType.None, null);
            validateModuleAction.addParameter("__row__", SymbolTable.InitialValueType.LocalVariable, "row", Module.ReturnValueType.None, null);
            validateModuleAction.addParameter("__column__", SymbolTable.InitialValueType.LocalVariable, "column", Module.ReturnValueType.None, null);
            validateModuleAction.addParameter("__type__", SymbolTable.InitialValueType.LocalVariable, "type", Module.ReturnValueType.None, null);
            validateModuleAction.addParameter("__oldValue__", SymbolTable.InitialValueType.LocalVariable, "oldValue", Module.ReturnValueType.None, null);
            validateModuleAction.addParameter("__newValue__", SymbolTable.InitialValueType.LocalVariable, "newValue", Module.ReturnValueType.LocalVariable, "newValue");
            MaleSocket maleSocket = new DefaultMaleDigitalActionSocket(InstanceManager.getDefault(DigitalActionManager.class), validateModuleAction);
            validateModuleAction.setParent(maleSocket);
            try {
                runFinally.getChild(0).connect(maleSocket);
            } catch (SocketAlreadyConnectedException e) {
                log.error("Exception when creating error handling LogixNG: ", e);
            }
            List<String> errors = new ArrayList<>();
            validateModuleLogixNG.setParentForAllChildren(errors);
            if (!errors.isEmpty()) {
                for (String s : errors) {
                    log.error("Error: {}", s);
                }
            }
        }

        private void callValidateModule(Object val, int row, int col) {
            if (_validateModule == null ||
                    !_validateModule.getBean().getRootSocket().isConnected()) {
                _namedTable.getBean().setCell(val, row+1, col+1);
                return;
            }
            validateModuleAction.getSelectNamedBean().setNamedBean(_validateModule);

            NamedTable table = _namedTable.getBean();
            _variablesWithValues.put("iconId", LogixNGTableIcon.this.getId());
            _variablesWithValues.put("tableName", _namedTable.getName());
            _variablesWithValues.put("row", table.getCell(row+1, 0));
            _variablesWithValues.put("column", table.getCell(0, col+1));
            _variablesWithValues.put("type", _headers[col]._type);
            _variablesWithValues.put("oldValue", table.getCell(row+1, col+1));
            _variablesWithValues.put("newValue", val);
            _myData.setValues(_namedTable.getBean(), row, col);
            validateModuleConditionalNG.execute();
        }

        private void handleResult(ConditionalNG conditionalNG, Exception ex, RunFinally.Data data) {
            if (ex != null) {
                if (ex instanceof ValidationErrorException) {
                    JOptionPane.showMessageDialog(LogixNGTableIcon.this,
                            ex.getMessage(),
                            Bundle.getMessage("LogixNGTableIcon_ValidationError"),
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(LogixNGTableIcon.this,
                            ex.getMessage(),
                            Bundle.getMessage("LogixNGTableIcon_Error"),
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (!(data instanceof MyData)) {
                    throw new IllegalArgumentException("data is not a MyData");
                }
                MyData myData = (MyData)data;
                Object newValue = conditionalNG.getSymbolTable().getValue("newValue");
                myData._namedTable.setCell(newValue, myData._row+1, myData._col+1);
                TableModel.this.fireTableCellUpdated(myData._row, myData._col);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt instanceof NamedTablePropertyChangeEvent) {
                var tableEvt = (NamedTablePropertyChangeEvent)evt;
                if (NamedTable.PROPERTY_CELL_CHANGED.equals(evt.getPropertyName())) {
                    fireTableCellUpdated(tableEvt.getRow()-1, tableEvt.getColumn()-1);
                }
            }
        }

        private void dispose() {
            if (_namedTable != null) {
                _namedTable.getBean().removePropertyChangeListener(this);
            }
        }

    }


    private static class MyData implements RunFinally.Data {

        private NamedTable _namedTable;
        private int _row;
        private int _col;

        private void setValues(NamedTable namedTable, int row, int col) {
            this._namedTable = namedTable;
            this._row = row;
            this._col = col;
        }
    }


    private class RowHeaderListModel extends AbstractListModel<Object> {
        @Override
        public int getSize() {
            return _tableModel.getTable().numRows();
        }

        @Override
        public Object getElementAt(int index) {
            // Ensure the header has at least five characters and ensure
            // there are at least two spaces at the end since the last letter
            // doesn't fully fit at the row.
            Object data = _tableModel.getTable().getCell(index+1, 0);
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


    private static class ModuleItem {

        private final Module _module;

        public ModuleItem(Module m) {
            _module = m;
        }

        @Override
        public String toString() {
            if (_module == null) return "";
            else return _module.getDisplayName();
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGTableIcon.class);
}
