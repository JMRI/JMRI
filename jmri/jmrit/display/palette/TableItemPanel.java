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

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;

import jmri.NamedBean;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.*;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JmriJFrame;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public class TableItemPanel extends FamilyItemPanel implements ListSelectionListener {

    int ROW_HEIGHT;

    protected JTable        _table;
    protected PickListModel _model;

    JScrollPane _scrollPane;
    JDialog     _addItemDialog;
    JTextField  _sysNametext = new JTextField();
    JTextField  _userNametext = new JTextField();
    JButton     _addTableButton;
    JButton     _updateButton;

    /**
    * Constructor for all table types.  When item is a bean, the itemType is the name key 
    * for the item in jmri.NamedBeanBundle.properties
    */
    public TableItemPanel(JmriJFrame parentFrame, String  type, String family, PickListModel model, Editor editor) {
        super(parentFrame,  type, family, editor);
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
        add(initTablePanel(_model, _editor));      // NORTH Panel
        initIconFamiliesPanel();    // CENTER Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
    }

    /**
    * Init for update of existing indicator turnout
    * _bottom3Panel has "Update Panel" button put into _bottom1Panel
    */
    public void init(ActionListener doneAction, Hashtable<String, NamedIcon> iconMap) {
        if (iconMap!=null) {
            checkCurrentMap(iconMap);   // is map in families?, does user want to add it? etc
        }
        _bottom1Panel = makeBottom1Panel();
        _bottom2Panel = makeBottom2Panel();
        _bottom1Panel = makeBottom3Panel(doneAction);
        add(initTablePanel(_model, _editor));      // NORTH Panel
        initIconFamiliesPanel();
        _table.setTransferHandler(null);        // no DnD
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for update family "+_family);
    }

    protected JPanel makeBottom3Panel(ActionListener doneAction) {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(_bottom1Panel);
        JPanel updatePanel = new JPanel();
        _updateButton = new JButton(ItemPalette.rbp.getString("updateButton"));
        _updateButton.addActionListener(doneAction);
        updatePanel.add(_updateButton);
        bottomPanel.add(updatePanel);
        return bottomPanel;
    }
    
    /**
    *  NORTH Panel
    */
    protected JPanel initTablePanel(PickListModel model, Editor editor) {
        _table = model.makePickTable();
        _table.setTransferHandler(new DnDTableItemHandler(editor));
        _table.getSelectionModel().addListSelectionListener(this);
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
        return topPanel;
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
        String sysname = _sysNametext.getText();
        if (sysname != null && sysname.length() > 1) {
            PickListModel model = (PickListModel)_table.getModel();
            String uname = _userNametext.getText();
            if (uname!=null && uname.trim().length()==0) {
                uname = null;
            }
            jmri.NamedBean bean = model.addBean(sysname, uname);
            if (bean!=null) {
                int setRow = model.getIndexOf(bean);
                if (log.isDebugEnabled()) log.debug("addToTable: row= "+setRow+", bean= "+bean.getDisplayName());
                _table.setRowSelectionInterval(setRow, setRow);
                _scrollPane.getVerticalScrollBar().setValue(setRow*ROW_HEIGHT);
            }
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

    public void setSelection(NamedBean bean) {
        int row = _model.getIndexOf(bean);
        if (row>=0) {
            _table.addRowSelectionInterval(row, row);
            _scrollPane.getVerticalScrollBar().setValue(row*ROW_HEIGHT);
        } else {
            log.debug("setSelection failed: NamedBean= "+bean+", row= "+row);
            valueChanged(null);
        }
    }

    /**
    *  When a Pick list is installed, table selection controls the Add button
    */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null || _updateButton==null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) log.debug("Table valueChanged: row= "+row);
        if (row >= 0) {
            _updateButton.setEnabled(true);
            _updateButton.setToolTipText(null);

        } else {
            _updateButton.setEnabled(false);
            _updateButton.setToolTipText(ItemPalette.rbp.getString("ToolTipPickFromTable"));
        }
    }

    /**
    *  Return from icon dialog
    */
    protected void reset() {
        hideIcons();
        _table.clearSelection();
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

        public NamedBean getNamedBean(JTable table) {
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (log.isDebugEnabled()) log.debug("TransferHandler.createPositionableDnD: from table \""+_itemType+ "\" at ("
                                                +row+", "+col+") for data \""
                                                +table.getModel().getValueAt(row, col)+"\" in family \""+_family+"\".");
            if (col<0 || row<0) {
                return null;
            }
            PickListModel model = (PickListModel)table.getModel();
            return model.getBeanAt(row);
        }

        public Hashtable <String, NamedIcon> getIconMap() {
            Hashtable <String, NamedIcon> iconMap = null;
            if (_updateWithSameMap) {
                iconMap = _currentIconMap;
            } else {
                iconMap = ItemPalette.getIconMap(_itemType, _family);
            }
            if (iconMap==null) {
                JOptionPane.showMessageDialog(_paletteFrame, 
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                       ItemPalette.rbp.getString(_itemType), _family), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            }
            return iconMap;
        }

        public Transferable createPositionableDnD(JTable table) {
            Hashtable <String, NamedIcon> iconMap = getIconMap();
            if (iconMap==null) {
                return null;
            }
            NamedBean bean = getNamedBean(table);
            if (bean==null) {
                return null;
            }

            if (_itemType.equals("Turnout")) {
                TurnoutIcon t = new TurnoutIcon(_editor);
                t.setTurnout(bean.getDisplayName());
                Enumeration <String> e = iconMap.keys();
                while (e.hasMoreElements()) {
                    String key = e.nextElement();
                    t.setIcon(key, iconMap.get(key));
                }
                t.setLevel(Editor.TURNOUTS);
                return new PositionableDnD(t, bean.getDisplayName());
            } else if (_itemType.equals("Sensor")) {
                SensorIcon s = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"), _editor);
                Enumeration <String> e = iconMap.keys();
                while (e.hasMoreElements()) {
                    String key = e.nextElement();
                    s.setIcon(key, iconMap.get(key));
                }
                s.setSensor(bean.getDisplayName());
                s.setLevel(Editor.SENSORS);
                return new PositionableDnD(s, bean.getDisplayName());
            } else if (_itemType.equals("Light")) {
                LightIcon l = new LightIcon(_editor);
                l.setOffIcon(iconMap.get("LightStateOff"));
                l.setOnIcon(iconMap.get("LightStateOn"));
                l.setInconsistentIcon(iconMap.get("BeanStateInconsistent"));
                l.setUnknownIcon(iconMap.get("BeanStateUnknown"));
                l.setLight((jmri.Light)bean);
                l.setLevel(Editor.LIGHTS);
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
