package jmri.jmrit.display.palette;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;

import java.awt.datatransfer.Transferable; 
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;

import jmri.NamedBean;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.SignalHeadIcon;

public class SignalHeadItemPanel extends TableItemPanel implements ListSelectionListener {

    int _selectedRow = 0;

    public SignalHeadItemPanel(ItemPalette parentFrame, String  itemType, PickListModel model, Editor editor) {
        super(parentFrame, itemType, model, editor);
    }

    protected void initTablePanel(PickListModel model, Editor editor) {
        super.initTablePanel(model, editor);
        _table.setTransferHandler(new SignalHeadDnD(editor));
        _table.getSelectionModel().addListSelectionListener(this);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        _selectedRow = _table.getSelectedRow();
        boolean visible = _iconPanel.isVisible();
        _iconFamilyPanel.remove(_iconPanel);
        makeIconPanel();
        _iconPanel.setVisible(visible);
        _iconFamilyPanel.add(_iconPanel, 0);
//        _paletteFrame.pack();
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
        Hashtable<String, NamedIcon> allIconsMap = ItemPalette.getIconMap(_itemType, _family);
        if (allIconsMap==null) {
            JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        Hashtable<String, NamedIcon> iconMap = null; 
        if (_selectedRow > 0) {
            iconMap = filterIconMap((SignalHead)_model.getBeanAt(_selectedRow), allIconsMap);
        } else {
           iconMap = allIconsMap;
        }
        addIconsToPanel(iconMap);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="WMI_WRONG_MAP_ITERATOR", justification="iterator really short, efficiency not as important as clarity here")
    Hashtable<String, NamedIcon> filterIconMap(SignalHead sh, Hashtable<String, NamedIcon> allIconsMap) {
        String[] states = sh.getValidStateNames();
        if (states.length == 0) {
            return ItemPalette.cloneMap(allIconsMap);
        }
        Hashtable<String, NamedIcon> iconMap = new Hashtable<String, NamedIcon>(); 
        
        Iterator <String> it = allIconsMap.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
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
            for (int j=0; j<states.length; j++) {
                if (borderName.equals(states[j]) ||
                        name.equals("SignalHeadStateDark") ||
                        name.equals("SignalHeadStateHeld")) {
                    iconMap.put(name, allIconsMap.get(name));
                    break;
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("filterIconMap: iconMap.size()= "+iconMap.size());
        return iconMap;
    }

    protected void openEditDialog() {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\"");
        if (_family!=null) {
            Hashtable<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
            Hashtable<String, NamedIcon> iconMap = new Hashtable<String, NamedIcon>();
            SignalHead sh = (SignalHead)getTableSelection();
            if (sh!=null) {
                iconMap = filterIconMap(sh, map);
            } else {
                iconMap = map;
            }
            new IconDialog(_itemType, _family, iconMap, this);
        } else {
            Hashtable<String, NamedIcon> map = makeNewIconMap(_itemType);
            new IconDialog(_itemType, map, this);
        }
    }

    /**
    * Extend handler to export from JList and import to PicklistTable
    */
    protected class SignalHeadDnD extends DnDTableItemHandler {

        SignalHeadDnD(Editor editor) {
            super(editor);
        }

        public Transferable createPositionableDnD(JTable table) {
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (log.isDebugEnabled()) log.debug("TransferHandler.createTransferable: from table \""+_itemType+ "\" at ("
                                                +row+", "+col+") for data \""
                                                +table.getModel().getValueAt(row, col)+"\"");
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

            SignalHeadIcon sh = new SignalHeadIcon(_editor);
            sh.setSignalHead(bean.getDisplayName());
            Enumeration <String> e = iconMap.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                sh.setIcon(ItemPalette.rbean.getString(key), iconMap.get(key));
            }
            sh.setDisplayLevel(Editor.SIGNALS);
            return new PositionableDnD(sh, bean.getDisplayName());
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadItemPanel.class.getName());
}
