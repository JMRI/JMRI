package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;

import jmri.NamedBean;
import jmri.jmrit.catalog.DragJLabel;
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
    JDialog     _addTableDialog;
    JTextField  _sysNametext = new JTextField();
    JTextField  _userNametext = new JTextField();
    JButton     _addTableButton;

    /**
    * Constructor for all table types.  When item is a bean, the itemType is the name key 
    * for the item in jmri.NamedBeanBundle.properties
    */
    public TableItemPanel(JmriJFrame parentFrame, String  type, String family, PickListModel model, Editor editor) {
        super(parentFrame,  type, family, editor);
        _model = model;
    }

    /**
    * Init for creation
    * insert table
    */
    public void init() {
        super.init();
        add(initTablePanel(_model, _editor), 0);      // top of Panel
    }

    /**
    * Init for update of existing indicator turnout
    * _bottom3Panel has "Update Panel" button put into _bottom1Panel
    */
    public void init(ActionListener doneAction, Hashtable<String, NamedIcon> iconMap) {
        super.init(doneAction, iconMap);
        add(initTablePanel(_model, _editor), 0);
    }
    
    /**
    *  top Panel
    */
    protected JPanel initTablePanel(PickListModel model, Editor editor) {
        _table = model.makePickTable();
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
        _addTableDialog = new JDialog(_paletteFrame, ItemPalette.rbp.getString("AddToTableTitle"), true);
        ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addToTable();
                }
            };
        jmri.util.swing.JmriPanel addPanel = new jmri.jmrit.beantable.AddNewDevicePanel(
                    _sysNametext, _userNametext, "addToTable", listener);
        _addTableDialog.getContentPane().add(addPanel);
        _addTableDialog.pack();
        _addTableDialog.setSize(_paletteFrame.getSize().width-20, _addTableDialog.getPreferredSize().height);
        _addTableDialog.setLocation(10,35);
        _addTableDialog.setLocationRelativeTo(_paletteFrame);
        _addTableDialog.toFront();
        _addTableDialog.setVisible(true);
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
        _addTableDialog.dispose();
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
        } else if (log.isDebugEnabled()) log.debug("getTableSelection: row= "+row);
        return null;
    }

    public void setSelection(NamedBean bean) {
        int row = _model.getIndexOf(bean);
        log.debug("setSelection: NamedBean= "+bean+", row= "+row);
        if (row>=0) {
            _table.addRowSelectionInterval(row, row);
            _scrollPane.getVerticalScrollBar().setValue(row*ROW_HEIGHT);
        } else {
            valueChanged(null);
        }
    }

    /**
    *  ListSelectionListener action
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

    protected NamedBean getNamedBean() {
        if (_table == null) {
            return null;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) log.debug("getNamedBean: from table \""+_itemType+ "\" at row "+row);
        if (row<0) {
            JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("noRowSelected"), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        PickListModel model = (PickListModel)_table.getModel();
        return model.getBeanAt(row);
    }

    /**
    *  Return from icon dialog
    */
    protected void reset() {
        hideIcons();
        _table.clearSelection();
    }

    protected JLabel getDragger(DataFlavor flavor, Hashtable<String, NamedIcon> map) {
        return new IconDragJLabel(flavor, map);
    }

    protected class IconDragJLabel extends DragJLabel {
        Hashtable <String, NamedIcon> iconMap;

        public IconDragJLabel(DataFlavor flavor, Hashtable <String, NamedIcon> map) {
            super(flavor);
            iconMap = map;
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return super.isDataFlavorSupported(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (iconMap==null) {
                log.error("IconDragJLabel.getTransferData: iconMap is null!");
                return null;
            }
            NamedBean bean = getNamedBean();
            if (bean==null) {
                log.error("IconDragJLabel.getTransferData: NamedBean is null!");
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
                t.setFamily(_family);
                t.setLevel(Editor.TURNOUTS);
                return t;
            } else if (_itemType.equals("Sensor")) {
                SensorIcon s = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"), _editor);
                Enumeration <String> e = iconMap.keys();
                while (e.hasMoreElements()) {
                    String key = e.nextElement();
                    s.setIcon(key, iconMap.get(key));
                }
                s.setSensor(bean.getDisplayName());
                s.setFamily(_family);
                s.setLevel(Editor.SENSORS);
                return s;
            } else if (_itemType.equals("Light")) {
                LightIcon l = new LightIcon(_editor);
                l.setOffIcon(iconMap.get("LightStateOff"));
                l.setOnIcon(iconMap.get("LightStateOn"));
                l.setInconsistentIcon(iconMap.get("BeanStateInconsistent"));
                l.setUnknownIcon(iconMap.get("BeanStateUnknown"));
                l.setLight((jmri.Light)bean);
                l.setLevel(Editor.LIGHTS);
                return l;
           }
            return null;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TableItemPanel.class.getName());
}
