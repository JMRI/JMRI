package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.NamedBean;
import jmri.jmrit.beantable.AbstractTableAction;
import jmri.jmrit.beantable.BeanTableFrame;
import jmri.jmrit.beantable.TurnoutTableAction;
import jmri.jmrit.beantable.SensorTableAction;
import jmri.jmrit.beantable.LightTableAction;
import jmri.jmrit.beantable.ReporterTableAction;
import jmri.jmrit.beantable.SignalHeadTableAction;
import jmri.jmrit.beantable.SignalMastTableAction;
import jmri.jmrit.beantable.MemoryTableAction;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LightIcon;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.display.TurnoutIcon;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FamilyItemPanel extension for placing of CPE item types that come from tool Tables
 * - e.g. Turnouts, Sensors, Lights, Signal Heads, etc.
 *
 * @author Pete Cressman Copyright (c) 2010, 2011, 2020
 */

public class TableItemPanel<E extends NamedBean> extends FamilyItemPanel implements ListSelectionListener {

    int ROW_HEIGHT;

    protected JTable _table;
    protected PickListModel<E> _model;
    AbstractTableAction<E> _tableAction;

    JScrollPane _scrollPane;
    JDialog _addTableDialog;
    JTextField _sysNametext = new JTextField();
    JTextField _userNametext = new JTextField();
    JButton _addTableButton;

    /**
     * Constructor for all table types. When item is a bean, the itemType is the
     * name key for the item in jmri.NamedBeanBundle.properties.
     *
     * @param parentFrame the enclosing parentFrame
     * @param type        item type
     * @param family      icon family
     * @param model       list model
     */
    @SuppressWarnings("unchecked")
    public TableItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<E> model) {
        super(parentFrame, type, family);
        _model = model;
        _tableAction = (AbstractTableAction<E>)getTableAction(type);
    }

    /**
     * Init for creation insert table.
     */
    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            add(initTablePanel(_model), 0); // top of Panel
        }
        hideIcons();
    }

    /**
     * Init for update of existing indicator turnout _bottom3Panel has "Update
     * Panel" button put into _bottom1Panel.
     */
    @Override
    public void init(ActionListener doneAction, HashMap<String, NamedIcon> iconMap) {
        super.init(doneAction, iconMap);
        add(initTablePanel(_model), 0);
    }

    private AbstractTableAction<?> getTableAction(String type) {
        switch (type) {
            case "Turnout":
            case "IndicatorTO":
                return new TurnoutTableAction(Bundle.getMessage("CreateNewItem"));
            case "Sensor":
            case "MultiSensor":
                return new SensorTableAction(Bundle.getMessage("CreateNewItem"));
            case "SignalHead":
                return new SignalHeadTableAction(Bundle.getMessage("CreateNewItem"));
            case "SignalMast":
                return new SignalMastTableAction(Bundle.getMessage("CreateNewItem"));
            case "Memory":
                return new MemoryTableAction(Bundle.getMessage("CreateNewItem"));
            case "Light":
                return new LightTableAction(Bundle.getMessage("CreateNewItem"));
            case "Reporter":
                return new ReporterTableAction(Bundle.getMessage("CreateNewItem"));
            default:
                return null;
        }
    }
    /*
     * Top Panel.
     */
    protected JPanel initTablePanel(PickListModel<E> model) {
        _table = model.makePickTable();
        ROW_HEIGHT = _table.getRowHeight();
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JLabel(model.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
        _scrollPane = new JScrollPane(_table);
        int cnt = Math.max(Math.min(10, _table.getRowCount()), 4);  // at least 4 rows, no more than 10
        _scrollPane.setPreferredSize(new Dimension(_scrollPane.getPreferredSize().width, cnt*ROW_HEIGHT));
        topPanel.add(_scrollPane, BorderLayout.CENTER);
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));

        JPanel panel = new JPanel();
        _addTableButton = new JButton(Bundle.getMessage("CreateNewItem"));
        _addTableButton.addActionListener(_tableAction);
        _addTableButton.setToolTipText(Bundle.getMessage("ToolTipAddToTable"));
        panel.add(_addTableButton);
        JButton clearSelectionButton = new JButton(Bundle.getMessage("ClearSelection"));
        clearSelectionButton.addActionListener(a -> {
            _table.clearSelection();
            hideIcons();
        });
        clearSelectionButton.setToolTipText(Bundle.getMessage("ToolTipClearSelection"));
        panel.add(clearSelectionButton);
        topPanel.add(panel, BorderLayout.SOUTH);
        _table.getSelectionModel().addListSelectionListener(this);
        _table.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        _scrollPane.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        return topPanel;
    }

    protected void makeAddToTableWindow() {
        _addTableDialog = new JDialog(_frame, Bundle.getMessage("AddToTableTitle"), true);

        ActionListener cancelListener = this::cancelPressed;
        ActionListener okListener = new ActionListener() {
            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent a) {
                addToTable();
            }
        };
        jmri.util.swing.JmriPanel addPanel = new jmri.jmrit.beantable.AddNewDevicePanel(
                _sysNametext, _userNametext, "addToTable", okListener, cancelListener);
        _addTableDialog.getContentPane().add(addPanel);
        _addTableDialog.pack();
        _addTableDialog.setSize(_frame.getSize().width - 20, _addTableDialog.getPreferredSize().height);
        _addTableDialog.setLocation(10, 35);
        _addTableDialog.setLocationRelativeTo(_frame);
        _addTableDialog.toFront();
        _addTableDialog.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        _addTableDialog.setVisible(false);
        _addTableDialog.dispose();
        _addTableDialog = null;
    }

    protected void addToTable() {
        String sysname = _sysNametext.getText();
        if (sysname != null && sysname.length() > 1) {
            String uname = _userNametext.getText();
            if (uname != null && uname.trim().length() == 0) {
                uname = null;
            }
            try {
                E bean = _model.addBean(sysname, uname);
                if (bean != null) {
                    int setRow = _model.getIndexOf(bean);
                    if (log.isDebugEnabled()) {
                        log.debug("addToTable: row = {}, bean = {}", setRow, bean.getDisplayName());
                    }
                    _table.setRowSelectionInterval(setRow, setRow);
                    _scrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
                }
                _addTableDialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(_frame, ex.getMessage(),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
        }
        _sysNametext.setText("");
        _userNametext.setText("");
    }

    /**
     * Used by Panel Editor to make the final installation of the icon(s) into
     * the user's Panel.
     * <p>
     * Note: the selection is cleared. When two successive calls are made, the
     * 2nd will always return null, regardless of the 1st return.
     *
     * @return bean selected in the table
     */
    public E getTableSelection() {
        int row = _table.getSelectedRow();
        row = _table.convertRowIndexToModel(row);
        if (row >= 0) {
            E b = _model.getBeanAt(row);
            _table.clearSelection();
            if (log.isDebugEnabled()) {
                log.debug("getTableSelection: row = {}, bean = {}", row, (b == null ? "null" : b.getDisplayName()));
            }
            return b;
        } else if (log.isDebugEnabled()) {
            log.debug("getTableSelection: row = {}", row);
        }
        return null;
    }

    public void setSelection(E bean) {
        int row = _model.getIndexOf(bean);
        row = _table.convertRowIndexToView(row);
        log.debug("setSelection: NamedBean = {}, row = {}", bean, row);
        if (row >= 0) {
            _table.addRowSelectionInterval(row, row);
            _scrollPane.getVerticalScrollBar().setValue(row * ROW_HEIGHT);
        } else {
            valueChanged(null);
        }
    }

    /**
     * ListSelectionListener action.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        log.debug("Table valueChanged: row = {}", row);
        if (_updateButton != null) {
            if (row >= 0) {
                _updateButton.setEnabled(true);
                _updateButton.setToolTipText(null);

            } else {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
            }
        }
        hideIcons();
    }

    protected E getDeviceNamedBean() {
        int row = _table.getSelectedRow();
        log.debug("getDeviceNamedBean: from table \"{}\" at row {}", _itemType, row);
        if (row < 0) {
            return null;
        }
        return _model.getBySystemName((String) _table.getValueAt(row, 0));
    }

    @Override
    protected String getDisplayKey() {
        if (_itemType.equals("Turnout")) {
            return "TurnoutStateClosed";
        }
        if (_itemType.equals("Sensor")) {
            return "SensorStateActive";
        }
        if (_itemType.equals("Light")) {
            return "StateOn";
        }
        return null;
    }

    @Override
    public void closeDialogs() {
        if (_tableAction != null) {
            _tableAction.dispose();
            BeanTableFrame<E> frame = _tableAction.getFrame();
            if (frame != null) {
                frame.dispose();
            }
        }
        super.closeDialogs();
    }

    /** {@inheritDoc} */
    @Override
    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
        return new IconDragJLabel(flavor, map, icon);
    }

    protected class IconDragJLabel extends DragJLabel {

        HashMap<String, NamedIcon> iMap;

        public IconDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
            super(flavor, icon);
            iMap = map;
        }

        /** {@inheritDoc} */
        @Override
        protected boolean okToDrag() {
            E bean = getDeviceNamedBean();
            if (bean == null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("noRowSelected"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            E bean = getDeviceNamedBean();
            if (bean == null) {
                return null;
            }
            // jmri.NamedBeanHandle<E> bh = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(bean.getDisplayName(), bean);

            Editor editor = _frame.getEditor();
            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                switch (_itemType) {
                    case "Turnout":
                        TurnoutIcon t = new TurnoutIcon(editor);
                        t.setTurnout(bean.getDisplayName());
                        for (Entry<String, NamedIcon> ent : iMap.entrySet()) {
                            t.setIcon(ent.getKey(), new NamedIcon(ent.getValue()));
                        }
                        t.setFamily(_family);
                        t.setLevel(Editor.TURNOUTS);
                        return t;
                    case "Sensor":
                        SensorIcon s = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"), editor);
                        for (Entry<String, NamedIcon> ent : iMap.entrySet()) {
                            s.setIcon(ent.getKey(), new NamedIcon(ent.getValue()));
                        }
                        s.setSensor(bean.getDisplayName());
                        s.setFamily(_family);
                        s.setLevel(Editor.SENSORS);
                        return s;
                    case "SignalMast":
                        SignalMastIcon sm = new SignalMastIcon(_frame.getEditor());
                        sm.setSignalMast(bean.getDisplayName());
                        sm.setLevel(Editor.SIGNALS);
                        return sm;
                    case "Light":
                        LightIcon l = new LightIcon(editor);
                        l.setOffIcon(iMap.get("StateOff"));
                        l.setOnIcon(iMap.get("StateOn"));
                        l.setInconsistentIcon(iMap.get("BeanStateInconsistent"));
                        l.setUnknownIcon(iMap.get("BeanStateUnknown"));
                        l.setLight((jmri.Light) bean);
                        l.setLevel(Editor.LIGHTS);
                        return l;
                    default:
                        return null;
                }
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                return _itemType + " icons for \"" + bean.getDisplayName() + "\"";
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TableItemPanel.class);

}
