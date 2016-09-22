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
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalHeadItemPanel extends TableItemPanel {//implements ListSelectionListener {

    /**
     *
     */
    private static final long serialVersionUID = -2071814434938345310L;

    public SignalHeadItemPanel(JmriJFrame parentFrame, String type, String family, PickListModel model, Editor editor) {
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
        _table.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        _scrollPane.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        JPanel panel = new JPanel();
        JButton clearSelectionButton = new JButton(Bundle.getMessage("ClearSelection"));
        clearSelectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                _table.clearSelection();
            }
        });
        clearSelectionButton.setToolTipText(Bundle.getMessage("ToolTipClearSelection"));
        panel.add(clearSelectionButton);
        topPanel.add(panel, BorderLayout.SOUTH);
        return topPanel;
    }

    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
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
        _showIconsButton.setText(Bundle.getMessage("HideIcons"));
    }

    protected void addIconsToPanel(HashMap<String, NamedIcon> allIconsMap) {
        HashMap<String, NamedIcon> iconMap = getFilteredIconMap(allIconsMap);
        if (iconMap == null) {
            iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap == null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
            }
        } else {
            super.addIconsToPanel(iconMap);
        }
    }

    /**
     * ListSelectionListener action
     */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null || _updateButton == null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) {
            log.debug("Table valueChanged: row= " + row);
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
        //       hideIcons();
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

        SignalHead sh = (SignalHead) getNamedBean();
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

    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map) {
        return new IconDragJLabel(flavor, map);
    }

    protected class IconDragJLabel extends DragJLabel {

        /**
         *
         */
        private static final long serialVersionUID = 1379306442765612241L;
        HashMap<String, NamedIcon> iconMap;

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2") // icon map is within package 
        public IconDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map) {
            super(flavor);
            iconMap = map;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (iconMap == null) {
                log.error("IconDragJLabel.getTransferData: iconMap is null!");
                return null;
            }
            NamedBean bean = getNamedBean();
            if (bean == null) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("noRowSelected"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }

            SignalHeadIcon sh = new SignalHeadIcon(_editor);
            sh.setSignalHead(bean.getDisplayName());
            HashMap<String, NamedIcon> map = getFilteredIconMap(iconMap);
            Iterator<Entry<String, NamedIcon>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, NamedIcon> ent = iter.next();
                sh.setIcon(Bundle.getMessage(ent.getKey()), new NamedIcon(ent.getValue()));
            }
            sh.setFamily(_family);
            sh.setLevel(Editor.SIGNALS);
            return sh;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadItemPanel.class.getName());
}
