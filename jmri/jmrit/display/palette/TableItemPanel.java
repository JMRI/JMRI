package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.TransferHandler;


import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;

import jmri.NamedBean;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.*;
import jmri.jmrit.picker.PickListModel;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public class TableItemPanel extends FamilyItemPanel {

    int ROW_HEIGHT;

    protected JTable        _table;
    protected PickListModel _model;

    JScrollPane _scrollPane;
    JDialog     _addItemDialog;
    JTextField  _sysNametext = new JTextField();
    JTextField  _userNametext = new JTextField();
    JButton     _addTableButton;

    /**
    * Constructor for all table types.  When item is a bean, the itemType is the name key 
    * for the item in jmri.NamedBeanBundle.properties
    */
    public TableItemPanel(ItemPalette parentFrame, String  itemType, PickListModel model, Editor editor) {
        super(parentFrame,  itemType, editor);
        _model = model;
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragTableRow"));
    }

    /**
    * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel depending on
    * whether icon families exist.  They are made first because they are referenced in
    * initIconFamiliesPanel()
    */
    public void init() {
        _bottom1Panel = makeBottom1Panel();
        _bottom2Panel = makeBottom2Panel();
        initTablePanel(_model, _editor);      // NORTH Panel
        initIconFamiliesPanel();    // CENTER Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
    }

    /**
    *  NORTH Panel
    */
    protected void initTablePanel(PickListModel model, Editor editor) {
        _table = model.makePickTable();
        _table.setTransferHandler(new DnDTableItemHandler(editor));
        ROW_HEIGHT = _table.getRowHeight();
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JLabel(model.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
        _scrollPane = new JScrollPane(_table);
        topPanel.add(_scrollPane, BorderLayout.CENTER);
        topPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragTableRow"));

        JPanel panel = new JPanel();
        _addTableButton = new JButton(ItemPalette.rbp.getString("CreateNewItem"));
        _addTableButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    makeAddToTableWindow();
                }
        });
        _addTableButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddToTable"));
        panel.add(_addTableButton);
        JButton clearSelectionButton = new JButton(ItemPalette.rbp.getString("ClearSelection"));
        clearSelectionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _table.clearSelection();
                }
        });
        clearSelectionButton.setToolTipText(ItemPalette.rbp.getString("ToolTipClearSelection"));
        panel.add(clearSelectionButton);
        topPanel.add(panel, BorderLayout.SOUTH);
        _table.setToolTipText(ItemPalette.rbp.getString("ToolTipDragTableRow"));
        _scrollPane.setToolTipText(ItemPalette.rbp.getString("ToolTipDragTableRow"));
        topPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragTableRow"));
        add(topPanel);
    }

    protected void makeAddToTableWindow() {
        _addItemDialog = new JDialog(_paletteFrame, ItemPalette.rbp.getString("AddToTableTitle"), true);
        ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addToTable();
                }
            };
        jmri.util.swing.JmriPanel addPanel = new jmri.jmrit.beantable.AddNewDevicePanel(
                    _sysNametext, _userNametext, "addToTable", listener);
        _addItemDialog.getContentPane().add(addPanel);
        _addItemDialog.pack();
        _addItemDialog.setSize(_paletteFrame.getSize().width-20, _addItemDialog.getPreferredSize().height);
        _addItemDialog.setLocation(10,35);
        _addItemDialog.setLocationRelativeTo(_paletteFrame);
        _addItemDialog.toFront();
        _addItemDialog.setVisible(true);
    }

    protected void addToTable() {
        String name = _sysNametext.getText();
        if (name != null && name.length() > 1) {
            PickListModel model = (PickListModel)_table.getModel();
            jmri.NamedBean bean = model.addBean(name, _userNametext.getText());
            int setRow = model.getIndexOf(bean);
            if (log.isDebugEnabled()) log.debug("addToTable: row= "+setRow+", bean= "+bean.getDisplayName());
            _table.setRowSelectionInterval(setRow, setRow);
            // 2nd element of topPanel
            _scrollPane.getVerticalScrollBar().setValue(setRow*ROW_HEIGHT);
        }
        _sysNametext.setText("");
        _userNametext.setText("");
        _addItemDialog.dispose();
    }

    /**
    * Used by Panel Editor to make the final installation of the icon(s)
    * into the user's Panel.
    * <P>Note! the selection is cleared. When two successive calls are made, the
    * 2nd will always return null, regardless of the 1st return.
    */
    public NamedBean getTableSelection() {
        int row = _table.getSelectedRow();
        if (row >= 0) {
            PickListModel model = (PickListModel)_table.getModel();
            NamedBean b = model.getBeanAt(row);
            _table.clearSelection();
            if (log.isDebugEnabled()) log.debug("getTableSelection: row= "+row+", bean= "+b.getDisplayName());
            return b;
        } else if (log.isDebugEnabled()) log.debug("getTableSelection: row=0");
        return null;
    }

    /**
    *  Return from icon dialog
    */
    protected void reset() {
        hideIcons();
        _table.clearSelection();
    }
    
    protected void openEditDialog() {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\"");
        if (_itemType.equals("MultiSensor")) {
            new MultiSensorIconDialog(_itemType, _family, this);
        } else {
            new IconDialog(_itemType, _family, this);
        }
    }

    /**
    * Export a Positionable item from PickListTable 
    */
    protected class DnDTableItemHandler extends TransferHandler {

        protected Editor _editor;

        DnDTableItemHandler(Editor editor) {
            _editor = editor;
        }

        public int getSourceActions(JComponent c) {
            return COPY;
        }


        public Transferable createTransferable(JComponent c) {
            if (log.isDebugEnabled()) log.debug("DnDTableItemHandler.createTransferable:");
            if (c instanceof JTable) {
                return createPositionableDnD((JTable)c);
            }
            return null;
        }

        public Transferable createPositionableDnD(JTable table) {
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (log.isDebugEnabled()) log.debug("TransferHandler.createPositionableDnD: from table \""+_itemType+ "\" at ("
                                                +row+", "+col+") for data \""
                                                +table.getModel().getValueAt(row, col)+"\" in family \""+_family+"\".");
            if (col<0 || row<0) {
                return null;
            }            
            Hashtable <String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap==null) {
                JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            PickListModel model = (PickListModel)table.getModel();
            NamedBean bean = model.getBeanAt(row);

            if (_itemType.equals("Turnout")) {
                TurnoutIcon t = new TurnoutIcon(_editor);
                t.setTurnout(bean.getDisplayName());
                Enumeration <String> e = iconMap.keys();
                while (e.hasMoreElements()) {
                    String key = e.nextElement();
                    t.setIcon(key, iconMap.get(key));
                }
                t.setDisplayLevel(Editor.TURNOUTS);
                return new PositionableDnD(t, bean.getDisplayName());
            } else if (_itemType.equals("Sensor")) {
                SensorIcon s = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"), _editor);
                s.setInactiveIcon(iconMap.get("SensorStateInactive"));
                s.setActiveIcon(iconMap.get("SensorStateActive"));
                s.setInconsistentIcon(iconMap.get("BeanStateInconsistent"));
                s.setUnknownIcon(iconMap.get("BeanStateUnknown"));
                s.setSensor(bean.getDisplayName());
                s.setDisplayLevel(Editor.SENSORS);
                return new PositionableDnD(s, bean.getDisplayName());
            } else if (_itemType.equals("Light")) {
                LightIcon l = new LightIcon(_editor);
                l.setOffIcon(iconMap.get("LightStateOff"));
                l.setOnIcon(iconMap.get("LightStateOn"));
                l.setInconsistentIcon(iconMap.get("BeanStateInconsistent"));
                l.setUnknownIcon(iconMap.get("BeanStateUnknown"));
                l.setLight((jmri.Light)bean);
                l.setDisplayLevel(Editor.LIGHTS);
                return new PositionableDnD(l, bean.getDisplayName());
           }
            return null;
        }

        public void exportDone(JComponent c, Transferable t, int action) {
            if (log.isDebugEnabled()) log.debug("TransferHandler.exportDone ");
        }
    }

    static protected class PositionableDnD implements Transferable {
        Positionable _pos;
        String _name;
        DataFlavor _dataFlavor;

        PositionableDnD(Positionable pos, String name) {
            _pos = pos;
            _name = name;
            try {
                _dataFlavor = new DataFlavor(Editor.POSITIONABLE_FLAVOR);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (flavor.equals(_dataFlavor)) {
                return _pos;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return _name;
            }
            throw new UnsupportedFlavorException(flavor);
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { _dataFlavor, DataFlavor.stringFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.equals(_dataFlavor)) {
                return true;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return true;
            }
            return false;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TableItemPanel.class.getName());
}
