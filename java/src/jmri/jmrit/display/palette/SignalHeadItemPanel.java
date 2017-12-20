package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalHeadItemPanel extends TableItemPanel { //implements ListSelectionListener {

    public SignalHeadItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<SignalHead> model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    @Override
    protected JPanel initTablePanel(PickListModel model, Editor editor) {
        _table = model.makePickTable();
        ROW_HEIGHT = _table.getRowHeight();
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
    protected void showIcons() {
        if (_iconPanel == null) { // create a new one
            _iconPanel = new ImagePanel();
            _iconPanel.setOpaque(false);
            _iconPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
                    Bundle.getMessage("PreviewBorderTitle")));
            _iconFamilyPanel.add(_iconPanel, 0);
        } else {
            _iconPanel.removeAll(); // clear old icons
        }
        addIconsToPanel(_currentIconMap);
        _iconPanel.setVisible(true);
        if (!_update && _dragIconPanel != null) { // prevent NPE
            _dragIconPanel.setVisible(false);
        }
        _showIconsButton.setText(Bundle.getMessage("HideIcons"));
    }

    @Override
    protected void addIconsToPanel(HashMap<String, NamedIcon> allIconsMap) {
        HashMap<String, NamedIcon> iconMap = getFilteredIconMap(allIconsMap);
        if (iconMap == null) {
            iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap == null) { // none found
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
            }
        } else {
            super.addIconsToPanel(iconMap);
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Table valueChanged: row= {}", row);
        }
        if (row >= 0) {
            _updateButton.setEnabled(true);
            _updateButton.setToolTipText(null);
            if (_family != null) {
                _currentIconMap = getFilteredIconMap(ItemPalette.getIconMap(_itemType, _family));
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

        SignalHead sh = (SignalHead) getDeviceNamedBean();
        if (sh != null) {
            String[] states = sh.getValidStateNames();
            if (states.length == 0) {
                return allIconsMap;
            }
            HashMap<String, NamedIcon> iconMap = new HashMap<String, NamedIcon>();
            Iterator<Entry<String, NamedIcon>> it = allIconsMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                String name = entry.getKey();
                String borderName = ItemPalette.convertText(name);
                for (int j = 0; j < states.length; j++) {
                    if (borderName.equals(states[j])
                            || name.equals("SignalHeadStateDark")
                            || name.equals("SignalHeadStateHeld")) {
                        iconMap.put(name, entry.getValue());
                        break;
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("filteredMap size= " + iconMap.size());
            }
            return iconMap;
        }
        if (log.isDebugEnabled()) {
            log.debug("Map NOT filtered, size= " + allIconsMap.size());
        }
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
            NamedBean bean = getDeviceNamedBean();
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
            NamedBean bean = getDeviceNamedBean();
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
