package jmri.jmrit.display.palette;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;

import java.awt.datatransfer.Transferable; 
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import jmri.util.JmriJFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;

import jmri.NamedBean;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.SignalHeadIcon;

public class SignalHeadItemPanel extends TableItemPanel implements ListSelectionListener {

    int _selectedRow = 0;

    public SignalHeadItemPanel(JmriJFrame parentFrame, String  type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    protected JPanel initTablePanel(PickListModel model, Editor editor) {
        _table = model.makePickTable();
        _table.setTransferHandler(new SignalHeadDnD(editor));
        ROW_HEIGHT = _table.getRowHeight();
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JLabel(model.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
        _scrollPane = new JScrollPane(_table);
        topPanel.add(_scrollPane, BorderLayout.CENTER);
        _table.getSelectionModel().addListSelectionListener(this);
        _table.setToolTipText(ItemPalette.rbp.getString("ToolTipDragTableRow"));
        _scrollPane.setToolTipText(ItemPalette.rbp.getString("ToolTipDragTableRow"));
        topPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragTableRow"));
        JPanel panel = new JPanel();
        JButton clearSelectionButton = new JButton(ItemPalette.rbp.getString("ClearSelection"));
        clearSelectionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _table.clearSelection();
                }
        });
        clearSelectionButton.setToolTipText(ItemPalette.rbp.getString("ToolTipClearSelection"));
        panel.add(clearSelectionButton);
        topPanel.add(panel, BorderLayout.SOUTH);
        return topPanel;
    }

    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        _selectedRow = _table.getSelectedRow();
        boolean visible = _iconPanel.isVisible();
        _iconFamilyPanel.remove(_iconPanel);
        _iconPanel = new JPanel();
        Hashtable<String, NamedIcon>  iconMap = _currentIconMap;
        if (_currentIconMap==null) {
            iconMap = ItemPalette.getIconMap(_itemType, _family);
        }
        addIconsToPanel(iconMap);
        _iconPanel.setVisible(visible);
        _iconFamilyPanel.add(_iconPanel, 0);
//        _paletteFrame.pack();
    }

    protected void addIconsToPanel(Hashtable<String, NamedIcon> allIconsMap) {
        Hashtable<String, NamedIcon> iconMap = getFilteredIconMap(allIconsMap);
        if (iconMap==null) {
            iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap==null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(ItemPalette.rbp.getString("ToolTipPickFromTable"));
            }
        } else {
            super.addIconsToPanel(iconMap);
        }
    }
/*
    protected void makeIconPanel() {
        _iconPanel = new JPanel();
        if (log.isDebugEnabled()) log.debug("makeIconPanel() _family= \""+_family+"\"");
        /*
        Hashtable<String, NamedIcon> iconMap = null;
        if (_family!=null) {
            iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap==null) {
                if (log.isDebugEnabled()) log.debug("makeIconPanel() iconMap==null for type \""+_itemType+"\", family \""+_family+"\"");
                // Thread.dumpStack();
                JOptionPane.showMessageDialog(_paletteFrame, 
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"),
                                                       ItemPalette.rbp.getString(_itemType), _family), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                _family = null;
            }
        }
        *
        if (_family==null) {
            Hashtable <String, Hashtable<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
            if (families!=null) {
                Iterator <String> it = families.keySet().iterator();
                while (it.hasNext()) {
                    _family = it.next();
                }
            }
        }

        Hashtable<String, NamedIcon> allIconsMap = ItemPalette.getIconMap(_itemType, _family);
        Hashtable<String, NamedIcon> iconMap = getFilteredIconMap(allIconsMap);
        if (iconMap==null) {
            iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap==null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(ItemPalette.rbp.getString("ToolTipPickFromTable"));
            }
        } else {
            addIconsToPanel(iconMap);
        }
    }
*/
    private NamedBean getSelectedBean() {
        int row = _table.getSelectedRow();
        if (row >= 0) {
            PickListModel model = (PickListModel)_table.getModel();
            NamedBean b = model.getBeanAt(row);
            if (log.isDebugEnabled()) log.debug("getSelectedBean: row= "+row+", bean= "+b.getDisplayName());
            return b;
        } else if (log.isDebugEnabled()) log.debug("getSelectedBean: row=0");
        return null;
    }

    protected Hashtable<String, NamedIcon> getFilteredIconMap(Hashtable<String, NamedIcon> allIconsMap) {
        if (allIconsMap==null) {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                   ItemPalette.rbp.getString(_itemType), _family), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        SignalHead sh = (SignalHead)getSelectedBean();
        if (sh!=null) {
            String[] states = sh.getValidStateNames();
            if (states.length == 0) {
                return allIconsMap;
            }
            Hashtable<String, NamedIcon> iconMap = new Hashtable<String, NamedIcon>(); 
            Iterator<Entry<String, NamedIcon>> it = allIconsMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                String name = entry.getKey();
                String borderName = ItemPalette.convertText(name);
                for (int j=0; j<states.length; j++) {
                    if (borderName.equals(states[j]) ||
                            name.equals("SignalHeadStateDark") ||
                            name.equals("SignalHeadStateHeld")) {
                        iconMap.put(name, entry.getValue());
                        break;
                    }
                }
            }
            if (log.isDebugEnabled()) log.debug("filteredMap size= "+iconMap.size());
            return iconMap;
        }
        if (log.isDebugEnabled()) log.debug("Map NOT filtered, size= "+allIconsMap.size());
        return allIconsMap;
    }

    protected void openEditDialog() {
        IconDialog dialog = new SignalHeadIconDialog(_itemType, _family, this);
        dialog.sizeLocate();
    }

    protected void createNewFamily(String type) {
        IconDialog dialog = new SignalHeadIconDialog(_itemType, null, this);
        dialog.sizeLocate();
    }

    /**
    * Extend handler to export from JList and import to PicklistTable
    */
    protected class SignalHeadDnD extends DnDTableItemHandler {

        SignalHeadDnD(Editor editor) {
            super(editor);
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

            SignalHeadIcon sh = new SignalHeadIcon(_editor);
            sh.setSignalHead(bean.getDisplayName());
            Enumeration <String> e = iconMap.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                sh.setIcon(ItemPalette.rbean.getString(key), iconMap.get(key));
            }
            sh.setLevel(Editor.SIGNALS);
            return new PositionableDnD(sh, bean.getDisplayName());
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadItemPanel.class.getName());
}
