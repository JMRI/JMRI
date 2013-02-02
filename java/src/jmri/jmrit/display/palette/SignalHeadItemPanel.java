package jmri.jmrit.display.palette;

import org.apache.log4j.Logger;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;

//import javax.swing.event.ListSelectionListener;
//import javax.swing.event.ListSelectionEvent;

import jmri.util.JmriJFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;

import jmri.NamedBean;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.display.SignalHeadIcon;

public class SignalHeadItemPanel extends TableItemPanel {//implements ListSelectionListener {

    public SignalHeadItemPanel(JmriJFrame parentFrame, String  type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    protected JPanel initTablePanel(PickListModel model, Editor editor) {
        _table = model.makePickTable();
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

    protected void makeDndIconPanel(Hashtable<String, NamedIcon> iconMap, String displayKey) {
        super.makeDndIconPanel(iconMap, "SignalHeadStateRed");
    }
    
    protected void showIcons() {
        //updateFamiliesPanel();
    	_iconFamilyPanel.remove(_iconPanel);
        _iconPanel = new JPanel();
        _iconFamilyPanel.add(_iconPanel, 0);
    	addIconsToPanel(_currentIconMap);
        _iconPanel.setVisible(true);
        if (!_update) {
            _dragIconPanel.setVisible(false);
        }
        _showIconsButton.setText(ItemPalette.rbp.getString("HideIcons"));
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
            if (_family!=null) {            	
                _currentIconMap = getFilteredIconMap(ItemPalette.getIconMap(_itemType, _family));
            }
        } else {
            _updateButton.setEnabled(false);
            _updateButton.setToolTipText(ItemPalette.rbp.getString("ToolTipPickFromTable"));
        }
        if (_iconPanel.isVisible()) {
        	showIcons();
        }
 //       hideIcons();
    }

    protected Hashtable<String, NamedIcon> getFilteredIconMap(Hashtable<String, NamedIcon> allIconsMap) {
        if (allIconsMap==null) {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                   ItemPalette.rbp.getString(_itemType), _family), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (_table==null || _table.getSelectedRow()<0) {
            return allIconsMap;
        }

        SignalHead sh = (SignalHead)getNamedBean();
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

    protected JLabel getDragger(DataFlavor flavor, Hashtable<String, NamedIcon> map) {
        return new IconDragJLabel(flavor, map);
    }

    protected class IconDragJLabel extends DragJLabel {
        Hashtable <String, NamedIcon> iconMap;

        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP2") // icon map is within package 
        public IconDragJLabel(DataFlavor flavor, Hashtable <String, NamedIcon> map) {
            super(flavor);
            iconMap = map;
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

            SignalHeadIcon sh = new SignalHeadIcon(_editor);
            sh.setSignalHead(bean.getDisplayName());
            Hashtable <String, NamedIcon> map = getFilteredIconMap(iconMap);
            Iterator<Entry<String, NamedIcon>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, NamedIcon> ent = iter.next();
                sh.setIcon(ItemPalette.rbean.getString(ent.getKey()), new NamedIcon(ent.getValue()));
            }
            sh.setFamily(_family);
            sh.setLevel(Editor.SIGNALS);
            return sh;
        }
    }

    static Logger log = Logger.getLogger(SignalHeadItemPanel.class.getName());
}
