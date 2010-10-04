package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.TransferHandler;


import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.*;
import jmri.NamedBean;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.*;
import jmri.jmrit.picker.PickListModel;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public class TableItemPanel extends ItemPanel {

    private static int ROW_HEIGHT;

    protected JTable        _table;
    protected PickListModel _model;

    JScrollPane _scrollPane;
    JDialog     _addItemDialog;
    JTextField  _sysNametext = new JTextField();
    JTextField  _userNametext = new JTextField();
    JButton     _addTableButton;
    JPanel      _iconFamilyPanel;
    JPanel      _iconPanel;
    JButton     _showIconsButton;

    /**
    * Constructor for all table types.  When item is a bean, the itemType is the name key 
    * for the item in jmri.NamedBeanBundle.properties
    */
    public TableItemPanel(ItemPalette parentFrame, String  itemType, PickListModel model, Editor editor) {
        super(parentFrame,  itemType, editor);
        _model = model;
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragTableRow"));
    }

    public void init() {
        initTablePanel(_model, _editor);      // NORTH Panel
        initIconFamiliesPanel();    // CENTER Panel
        initButtonPanel();          // SOUTH Panel
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

        if (!_itemType.equals("SignalHead")) {
            JPanel panel = new JPanel();
            _addTableButton = new JButton(ItemPalette.rbp.getString("CreateNewItem"));
            _addTableButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        makeAddToTableWindow();
                    }
            });
            _addTableButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddToTable"));
            panel.add(_addTableButton);
            topPanel.add(panel, BorderLayout.SOUTH);
        }
        add(topPanel, BorderLayout.NORTH);
    }

    private void makeAddToTableWindow() {
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

    void addToTable() {
        String name = _sysNametext.getText();
        if (name != null && name.length() > 1) {
            PickListModel model = (PickListModel)_table.getModel();
            jmri.NamedBean bean = model.addBean(name, _userNametext.getText());
            int setRow = model.getIndexOf(bean);
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

    protected void setFamily(String family) {
        _family = family;
        if (log.isDebugEnabled()) log.debug("setFamily: for type \""+_itemType+"\", family \""+family+"\"");
        boolean visible = _iconPanel.isVisible();
        _iconFamilyPanel.remove(_iconPanel);
        makeIconPanel();        // need to have family identified  before calling
        _iconPanel.setVisible(visible);
        _iconFamilyPanel.add(_iconPanel, 0);
        hideIcons();
        _paletteFrame.pack();
    }

    protected void removeIconFamiliesPanel() {
        remove(_iconFamilyPanel);
    }

    protected void removeIconMap(String family) {
        if (log.isDebugEnabled()) log.debug("removeIconMap() before family= \""+family+"\", _family \""+_family+"\"");
        if (_family.equals(family)) {
            _family = null;
        }
        _paletteFrame.removeIconMap(_itemType, family);
    }

    /**
    *  CENTER Panel
    */
    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));

        Hashtable <String, Hashtable<String, NamedIcon>> families = _paletteFrame.getFamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            String txt = java.text.MessageFormat.format(ItemPalette.rbp.getString("IconFamilies"), _itemType);
            _iconFamilyPanel.add(new JLabel(txt));
            ButtonGroup group = new ButtonGroup();
            Iterator <String> it = families.keySet().iterator();
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
            String family = null;
            JRadioButton button = null;
            while (it.hasNext()) {
                family = it.next();
                button = new JRadioButton(family);
                button.addActionListener(new ActionListener() {
                        String family;
                        public void actionPerformed(ActionEvent e) {
                            setFamily(family);
                        }
                        ActionListener init(String f) {
                            family = f;
                            if (log.isDebugEnabled()) log.debug("ActionListener.init : for type \""+_itemType+"\", family \""+family+"\"");
                            return this;
                        }
                    }.init(family));
                if (family.equals(_family)) {
                    button.setSelected(true);
                }
                buttonPanel.add(button);
                group.add(button);
            }
            if (_family==null) {
                _family = family;       // let last familiy be the selected one
                if (button != null) button.setSelected(true);
            }
            makeIconPanel();        // need to have family identified  before calling
            _iconFamilyPanel.add(_iconPanel);
            _iconPanel.setVisible(false);
            _iconFamilyPanel.add(buttonPanel);
        } else {
            //log.error("Item type \""+_itemType+"\" has "+(families==null ? "null" : families.size())+ " families.");
            JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
        }
        add(_iconFamilyPanel, BorderLayout.CENTER);
    }

    protected void makeIconPanel() {
        _iconPanel = new JPanel();
        if (log.isDebugEnabled()) log.debug("makeIconPanel() _family= \""+_family+"\"");
        if (_family==null) {
            Hashtable <String, Hashtable<String, NamedIcon>> families = _paletteFrame.getFamilyMaps(_itemType);
            if (families!=null) {
                Iterator <String> it = families.keySet().iterator();
                while (it.hasNext()) {
                    _family = it.next();
                }
            }
        }
        Hashtable<String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
        if (iconMap==null) {
            if (log.isDebugEnabled()) log.debug("makeIconPanel() iconMap==null for type \""+_itemType+"\", family \""+_family+"\"");
            Thread.dumpStack();
            JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            AddIconsToPanel(iconMap);
        }
    }

    protected void AddIconsToPanel(Hashtable<String, NamedIcon> iconMap) {
        Iterator <String> it = iconMap.keySet().iterator();
        while (it.hasNext()) {
           String name = it.next();
           NamedIcon icon = new NamedIcon(iconMap.get(name));    // make copy for possible reduction
           icon.reduceTo(100, 100, 0.2);
           JPanel panel = new JPanel();
           String borderName = null;
           try {
               borderName = ItemPalette.rbean.getString(name);
           } catch (java.util.MissingResourceException mre) {
               try {
                   borderName = ItemPalette.rbp.getString(name);
               } catch (java.util.MissingResourceException mre2) {
                   borderName = name;
               }
           }
           panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                            borderName));
           panel.add(new JLabel(icon));
           _iconPanel.add(panel);
        }
    }

    /**
    *  SOUTH Panel
    */
    protected void initButtonPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
        _showIconsButton = new JButton(ItemPalette.rbp.getString("ShowIcons"));
        _showIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_iconPanel.isVisible()) {
                        hideIcons();
                    } else {
                        _iconPanel.setVisible(true);
                        _showIconsButton.setText(ItemPalette.rbp.getString("HideIcons"));
                    }
                }
        });
        _showIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipShowIcons"));
        bottomPanel.add(_showIconsButton);

        JButton editIconsButton = new JButton(ItemPalette.rbp.getString("EditIcons"));
        editIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    openEditDialog();
                }
        });
        editIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipEditIcons"));
        bottomPanel.add(editIconsButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    protected void hideIcons() {
        _iconPanel.setVisible(false);
        _showIconsButton.setText(ItemPalette.rbp.getString("ShowIcons"));
    }

    protected void openEditDialog() {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\"");
        if (_family!=null) {
            if (_itemType.equals("MultiSensor")) {
                new MultiSensorIconDialog(_itemType, _family, this);
            } else {
                new IconDialog(_itemType, _family, this);
            }
        } else {
            Hashtable<String, NamedIcon> map = makeNewIconMap(_itemType);
            if (_itemType.equals("MultiSensor")) {
                new MultiSensorIconDialog(_itemType, map, this);
            } else {
                new IconDialog(_itemType, map, this);
            }
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
            if (c instanceof JTable) {
                return createPositionableDnD((JTable)c);
            }
            return null;
        }

        public Transferable createPositionableDnD(JTable table) {
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (log.isDebugEnabled()) log.debug("TransferHandler.createTransferable: from table \""+_itemType+ "\" at ("
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
                t.setClosedIcon(iconMap.get("TurnoutStateClosed"));
                t.setThrownIcon(iconMap.get("TurnoutStateThrown"));
                t.setInconsistentIcon(iconMap.get("BeanStateInconsistent"));
                t.setUnknownIcon(iconMap.get("BeanStateUnknown"));
                t.setTurnout(bean.getDisplayName());
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

    protected class PositionableDnD implements Transferable {
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
