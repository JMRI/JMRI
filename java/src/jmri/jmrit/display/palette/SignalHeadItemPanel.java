package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import jmri.SignalHead;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalHeadItemPanel extends TableItemPanel<SignalHead> { 

    public SignalHeadItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<SignalHead> model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    @Override
    protected JPanel initTablePanel(PickListModel<SignalHead> model, Editor editor) {
        _table = model.makePickTable();
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JLabel(model.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
        _scrollPane = new JScrollPane(_table);
        topPanel.add(_scrollPane, BorderLayout.CENTER);
        _table.getSelectionModel().addListSelectionListener(this);
        _table.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        _scrollPane.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        JPanel panel = new JPanel();
        JButton clearSelectionButton = new JButton(Bundle.getMessage("ClearSelection"));
        clearSelectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                _table.clearSelection();
            }
        });
        clearSelectionButton.setToolTipText(Bundle.getMessage("ToolTipClearSelection"));
        panel.add(clearSelectionButton);
        topPanel.add(panel, BorderLayout.SOUTH);
        return topPanel;
    }

    @Override
    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        super.makeDndIconPanel(iconMap, "SignalHeadStateRed");
    }

    @Override
    protected void openDialog(String type, String family, HashMap<String, NamedIcon> iconMap) {
        closeDialogs();
        _dialog = new SignalHeadIconDialog(type, family, this, iconMap);
    }

    /**
     * ListSelectionListener action.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null || _updateButton == null) {
            return;
        }
        int row = _table.getSelectedRow();
        log.debug("Table valueChanged: row= {}, {}({})",
                row, _table.getValueAt(row, 0), _table.getValueAt(row, 1));
        if (row >= 0) {
            _updateButton.setEnabled(true);
            _updateButton.setToolTipText(null);
            if (_family != null) {
                // get raw map of all appearances for row's head type.
                HashMap<String, NamedIcon> fullmap = getFilteredIconMap(ItemPanel.makeNewIconMap(_itemType));
                // icon map of appearances for type of current bean.
                HashMap<String, NamedIcon> currentmap = (getIconMap());
                log.debug("currentmap keys = {}", currentmap.keySet().toString());
                // use current images for as many of the fullMap's members as possible
                HashMap<String, NamedIcon> iconMap = new HashMap<>();
                for (Entry<String, NamedIcon> entry : fullmap.entrySet()) {
                    String key = entry.getKey();
                    String newKey = ItemPalette.convertText(key);
                    log.debug("fullmap key = {}, converts to {}", key, newKey);
                    NamedIcon icon = currentmap.get(newKey);
                    if (icon != null) {
                        iconMap.put(newKey, icon);
                    } else {
                        iconMap.put(newKey, entry.getValue());
                    }
                }
                log.debug("set Signal Head {} map size= {}", _table.getValueAt(row, 0), iconMap.size());
                setIconMap(iconMap);
            }
        } else {
            _updateButton.setEnabled(false);
            _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        }
        if (_iconPanel.isVisible()) {
            showIcons();
        }
    }

    protected HashMap<String, NamedIcon> getFilteredIconMap(HashMap<String, NamedIcon> allIconsMap) {
        if (allIconsMap == null) {
            JOptionPane.showMessageDialog(_paletteFrame,
                    Bundle.getMessage("FamilyNotFound", _itemType, _family),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (_table == null || _table.getSelectedRow() < 0) {
            return allIconsMap;
        }

        SignalHead sh = getDeviceNamedBean();
        if (sh != null) {
            String[] states = sh.getValidStateNames();
            if (states.length == 0) {
                return allIconsMap;
            }
            HashMap<String, NamedIcon> iconMap = new HashMap<>();
            for (Entry<String, NamedIcon> entry : allIconsMap.entrySet()) {
                String name = entry.getKey();
                String borderName = ItemPalette.convertText(name);
                for (String state : states) {
                    if (borderName.equals(state) || name.equals("SignalHeadStateDark") || name.equals(ItemPalette.convertText("SignalHeadStateDark")) || name.equals("SignalHeadStateHeld") || name.equals(ItemPalette.convertText("SignalHeadStateHeld"))) {
                        iconMap.put(name, entry.getValue());
                        break;
                    }
                }
            }
            log.debug("filteredMap size= {}", iconMap.size());
            return iconMap;
        }
        log.debug("Map NOT filtered, size= {}", allIconsMap.size());
        return allIconsMap;
    }

    @Override
    protected JLabel getDragger(DataFlavor flavor, HashMap<String, 
            NamedIcon> map, NamedIcon icon) {
        return new IconDragJLabel(flavor, map, icon);
    }

    protected class IconDragJLabel extends DragJLabel {

        HashMap<String, NamedIcon> iMap;

        public IconDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map, 
                NamedIcon icon) {
            super(flavor, icon);
            iMap = map;
        }

        @Override
        protected boolean okToDrag() {
            SignalHead bean = getDeviceNamedBean();
            if (bean == null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("noRowSelected"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            SignalHead bean = getDeviceNamedBean();
            if (bean == null) {
                return null;
            }

            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                SignalHeadIcon sh = new SignalHeadIcon(_editor);
                sh.setSignalHead(bean.getDisplayName());
                HashMap<String, NamedIcon> map = getFilteredIconMap(iMap);
                Iterator<Entry<String, NamedIcon>> iter = map.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    sh.setIcon(Bundle.getMessage(ent.getKey()), new NamedIcon(ent.getValue()));
                }
                sh.setFamily(_family);
                sh.setLevel(Editor.SIGNALS);
                return sh;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" icons for \"");
                sb.append(bean.getDisplayName());
                sb.append("\"");
                return  sb.toString();
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadItemPanel.class);

}
