package jmri.jmrit.display;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.*;
import jmri.util.swing.*;


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

        _tableModel = new TableModel();

        _tableModel.setTable(tableName);

        _table = new JTable(_tableModel);
        _table.setCellSelectionEnabled(true);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        _table.getTableHeader().setReorderingAllowed(false);

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
//            System.out.format("Root socket type: %s%n", m.getRootSocketType().getName());
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
        JList<String> columnList = new JList<>(columns.toArray(String[]::new));
        for (String header : _tableModel._editableColumnsList) {
            int index = columnIndexes.getOrDefault(header,-1);
            if (index != -1) {
                columnList.getSelectionModel().addSelectionInterval(index, index);
            }
        }
        columnList.setBorder(BorderFactory.createLineBorder(Color.black));

        Map<String,Integer> rowIndexes = new HashMap<>();
        List<String> rows = new ArrayList<>();
        for (int row=0; row < namedTable.numRows(); row++) {
            String header = getObjectAsString(namedTable.getCell(row+1, 0));
            if (!header.isEmpty()) {
                rows.add(header);
                rowIndexes.put(header, row);
            }
        }
        JList<String> rowList = new JList<>(rows.toArray(String[]::new));
        for (String header : _tableModel._editableRowsList) {
            int index = rowIndexes.getOrDefault(header,-1);
            if (index != -1) {
                rowList.getSelectionModel().addSelectionInterval(index, index);
            }
        }
        rowList.setBorder(BorderFactory.createLineBorder(Color.black));

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
        c.gridy = 5;
        p.add(new JLabel(Bundle.getMessage("LogixNGTableIcon_EditableRows")), c);
        c.gridx = 1;
        c.gridy = 0;
        p.add(Box.createHorizontalStrut(5), c);
        c.gridx = 2;
        c.gridy = 2;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(allowEdit, c);
//        p.add(autoSystem, c);
        c.gridx = 3;
//        p.add(addRange, c);
        c.gridx = 2;
        c.gridy = 1;
        p.add(_validateModuleComboBox, c);
//        sys.setToolTipText(Bundle.getMessage("SysNameToolTip", "Y"));
        c.gridy = 3;
        p.add(new JScrollPane(columnList), c);
//        p.add(finishLabel, c);
        c.gridy = 4;
        p.add(Box.createVerticalStrut(5), c);
        c.gridy = 5;
        p.add(new JScrollPane(rowList), c);
//        p.add(endRange, c);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 3;
        p.add(new JLabel(Bundle.getMessage("LogixNGTableIcon_EditableColumnsRowsInfo")), c);
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
            _tableModel.setEditableRows(String.join("\t", rowList.getSelectedValuesList()));
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


    // ------------ Table Models ------------

    /**
     * Table model for Tables in the Edit NamedTable pane.
     */
    public final class TableModel extends AbstractTableModel {

        private NamedBeanHandle<NamedTable> _namedTable;
        private NamedBeanHandle<Module> _validateModule;
        private boolean _editable = false;
        private String _editableColumns = "";
        private String _editableRows = "";
        List<String> _editableColumnsList;
        List<String> _editableRowsList;

        public TableModel() {
            setEditableColumns("");
            setEditableRows("");
        }

        public NamedTable getTable() {
            return _namedTable != null ? _namedTable.getBean() : null;
        }

        public void setTable(String tableName) {
            NamedTable table = null;
            if (tableName != null) {
                table = InstanceManager.getDefault(NamedTableManager.class)
                        .getNamedTable(tableName);
            }

            if (table != null) {
                _namedTable = InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(table.getDisplayName(), table);
            } else {
                _namedTable = null;
            }
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

        public String getEditableRows() {
            return _editableRows;
        }

        public void setEditableRows(String editableRows) {
            _editableRows = editableRows;
            _editableRowsList = Arrays.asList(_editableRows.split("\t"));
        }

        @Override
        public int getColumnCount() {
            return _namedTable.getBean().numColumns();
        }

        @Override
        public int getRowCount() {
            return _namedTable.getBean().numRows();
        }

        @Override
        public String getColumnName(int col) {
            Object data = _namedTable.getBean().getCell(0, col+1);
            return data != null ? data.toString() : "<null>";
        }

        @Override
        public Object getValueAt(int row, int col) {
            return _namedTable.getBean().getCell(row+1, col+1);
        }

        @Override
        public void setValueAt(Object val, int row, int col) {
            _namedTable.getBean().setCell(val, row+1, col+1);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (!_editable) {
                return false;
            }

            String columnHeader = getObjectAsString(_namedTable.getBean().getCell(0, columnIndex+1));
            boolean allowColumn = _editableColumns.isEmpty();
            allowColumn |= _editableColumnsList.contains(columnHeader);

            String rowHeader = getObjectAsString(_namedTable.getBean().getCell(rowIndex+1, 0));
            boolean allowRow = _editableRows.isEmpty();
            allowRow |= _editableRowsList.contains(rowHeader);

            return allowColumn && allowRow;
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


//    private final static Logger log = LoggerFactory.getLogger(LogixNGTableIcon.class);
}
