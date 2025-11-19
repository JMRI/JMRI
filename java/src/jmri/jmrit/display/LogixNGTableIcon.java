package jmri.jmrit.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.util.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a NamedTable and let the user edit it.
 *
 * @author Pete Cressman    Copyright (c) 2009
 * @author Daniel Bergqvist Copyright (C) 2025
 * @since 5.15.1
 */
public final class LogixNGTableIcon extends PositionableJPanel implements java.beans.PropertyChangeListener {

    private TableModel tableModel = null;

    // the associated NamedTable object
    private NamedBeanHandle<NamedTable> _namedTable;
    private final JTable _table;
    private final JList<Object> _rowHeader;
    private final JScrollPane _scrollPane;

    private final java.awt.event.MouseListener _mouseListener = JmriMouseListener.adapt(this);

    public LogixNGTableIcon(String tableName, Editor editor) {
        super(editor);
        setDisplayLevel(Editor.LABELS);

        setLayout(new java.awt.BorderLayout());

        setNamedTable(tableName);

        tableModel = new TableModel();
        _table = new JTable(tableModel);
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
        LogixNGTableIcon pos = new LogixNGTableIcon(_namedTable.getName(), _editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(LogixNGTableIcon pos) {
        pos.setNamedTable(_namedTable.getName());
        return super.finishClone(pos);
    }

    @Override
    public void mouseExited(JmriMouseEvent e) {
        updateNamedTable();
        super.mouseExited(e);
    }

    /**
     * Attached a named NamedTable to this display item
     *
     * @param pName Used as a system/user name to lookup the NamedTable object
     */
    public void setNamedTable(String pName) {
        log.debug("setNamedTable for namedTable= {}", pName);
        if (InstanceManager.getNullableDefault(NamedTableManager.class) != null) {
            try {
                NamedTable namedTable = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(pName);
                setNamedTable(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, namedTable));
            } catch (IllegalArgumentException e) {
                log.error("NamedTable '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No NamedTableManager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attached a named NamedTable to this display item
     *
     * @param m The NamedTable object
     */
    public void setNamedTable(NamedBeanHandle<NamedTable> m) {
        if (_namedTable != null) {
            getNamedTable().removePropertyChangeListener(this);
        }
        _namedTable = m;
        if (_namedTable != null) {
            getNamedTable().addPropertyChangeListener(this, _namedTable.getName(), "NamedTable Icon");
            displayState();
            setName(_namedTable.getName());
        }
    }

    public NamedBeanHandle<NamedTable> getNamedNamedTable() {
        return _namedTable;
    }

    public NamedTable getNamedTable() {
        if (_namedTable == null) {
            return null;
        }
        return _namedTable.getBean();
    }

    public JTable getJTable() {
        return _table;
    }

    // update icon as state of NamedTable changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_LogixNGTableIcon");
    }

    @Override
    public String getNameString() {
        String name;
        if (_namedTable == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getNamedTable().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    @Override
    public void mouseMoved(JmriMouseEvent e) {
        updateNamedTable();
    }

    private void updateNamedTable() {
//        if (_namedTable == null) {
//            return;
//        }
//        String str = _textBox.getText();
//        getNamedTable().setValue(str);
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
        return true;
    }

    @Override
    protected void edit() {
/*
        _iconEditor = new IconAdder("NamedTable") {
            final JSpinner spinner = new JSpinner(_spinModel);

            @Override
            protected void addAdditionalButtons(JPanel p) {
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(2);
                spinner.setMaximumSize(spinner.getPreferredSize());
//                spinner.setValue(_textBox.getColumns());
                JPanel p2 = new JPanel();
                //p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
                //p2.setLayout(new FlowLayout(FlowLayout.TRAILING));
                p2.add(new JLabel(Bundle.getMessage("NumColsLabel")));
                p2.add(spinner);
                p.add(p2);
                p.setVisible(true);
            }
        };

        makeIconEditorFrame(this, "NamedTable", true, _iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.namedTablePickModelInstance());
        ActionListener addIconAction = a -> editNamedTable();
        _iconEditor.makeIconPanel(false);
        _iconEditor.complete(addIconAction, false, false, true);
        _iconEditor.setSelection(getNamedTable());
*/
    }
/*
    void editNamedTable() {
        setNamedTable(_iconEditor.getTableSelection().getDisplayName());
//        _nCols = _spinModel.getNumber().intValue();
//        _textBox.setColumns(_nCols);
        setSize(getPreferredSize().width + 1, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        validate();
    }
*/
    /**
     * Drive the current state of the display from the state of the NamedTable.
     */
    public void displayState() {
//        log.debug("displayState");
//        if (_namedTable == null) {  // leave alone if not connected yet
//            return;
//        }
//        Object show = getNamedTable().getValue();
//        if (show != null) {
//            _textBox.setText(show.toString());
//        } else {
//            _textBox.setText("");
//        }
    }

    @Override
    void cleanup() {
        if (_namedTable != null) {
            getNamedTable().removePropertyChangeListener(this);
        }
        if (_scrollPane != null) {
            _table.removeMouseListener(_mouseListener);
            _rowHeader.removeMouseListener(_mouseListener);
            _scrollPane.removeMouseListener(_mouseListener);
        }
        _namedTable = null;
    }


    // ------------ Table Models ------------

    /**
     * Table model for Tables in the Edit NamedTable pane.
     */
    public final class TableModel extends AbstractTableModel {

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
            return true;
        }

    }

    private class RowHeaderListModel extends AbstractListModel<Object> {
        @Override
        public int getSize() {
            return _namedTable.getBean().numRows();
        }

        @Override
        public Object getElementAt(int index) {
            // Ensure the header has at least five characters and ensure
            // there are at least two spaces at the end since the last letter
            // doesn't fully fit at the row.
            Object data = _namedTable.getBean().getCell(index+1, 0);
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


    private final static Logger log = LoggerFactory.getLogger(LogixNGTableIcon.class);
}
