package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SignalAppearanceMap;
import jmri.SignalMast;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PreviewPanel;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TableItemPanel extension for placing of SignalMast items with a fixed set of icons.
 *
 * @author Pete Cressman Copyright (c) 2010, 2011, 2020
 * @author Egbert Broerse 2017
 */
public class SignalMastItemPanel extends TableItemPanel<SignalMast> {

    private SignalMast _mast;
    private HashMap<String, NamedIcon> _iconMastMap;
    private JLabel _promptLabel;
    private JPanel _blurb;
    private final NamedIcon _defaultIcon;

    public SignalMastItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<jmri.SignalMast> model) {
        super(parentFrame, type, family, model);
        try {
            _mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0003)");
        } catch (IllegalArgumentException ex) {
            log.error("No SignalMast called IF$vsm:AAR-1946:SL-2-high-abs($0003)");
        }
        makeIconMap();
        NamedIcon icon = getDragIcon();
        if (icon != null ) {
            _defaultIcon = icon;
        } else {
            _defaultIcon =  new NamedIcon(ItemPalette.RED_X, ItemPalette.RED_X);
        }
        _iconMastMap = null;
        _mast = null;
    }

    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _table.getSelectionModel().addListSelectionListener(this);
            _showIconsButton.setEnabled(false);
            _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipPickRowToShowIcon"));
            add(_iconFamilyPanel, 1);
        }
    }

    @Override
    public void init(ActionListener doneAction, HashMap<String, NamedIcon> iconMap) {
        super.init(doneAction, iconMap);
        _table.getSelectionModel().addListSelectionListener(this);
        _previewPanel.setVisible(false);
    }

    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("PickRowMast")));
        blurb.add(new JLabel(Bundle.getMessage("DragReporter")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    @Override
    protected void initIconFamiliesPanel() {
        if (log.isDebugEnabled()) {
            log.debug("initIconFamiliesPanel for= {}, {}", _itemType, _family);
        }
        if (_table != null) {
            int row = _table.getSelectedRow();
            getIconMap(row); // sets _iconMastMap + _mast, if they exist.
        }
        if (_iconFamilyPanel == null) {
            log.debug("new _iconFamilyPanel created");
            _iconFamilyPanel = new JPanel();
            _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
            _iconFamilyPanel.setOpaque(true);
            if (!_update) {
                _blurb = instructions();
                _iconFamilyPanel.add(_blurb);
            }
        }
        if (!_update) {
            makeDragIconPanel(1);
            makeDndIconPanel(null, null);
        }

        if (_iconPanel == null) { // keep an existing panel
            _iconPanel = new ImagePanel();
            _iconPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
            _promptLabel = new JLabel();
            JPanel panel = new JPanel();
            panel.add(_promptLabel);
            _iconFamilyPanel.add(panel);
            if (!_update) {
                _previewPanel = new PreviewPanel(_frame, _iconPanel, _dragIconPanel, true);
            } else {
                _previewPanel = new PreviewPanel(_frame, _iconPanel, null, false);
            }
            _iconFamilyPanel.add(_previewPanel);
        }

        addIconsToPanel(_iconMastMap, _iconPanel, false);

        if (_mast != null) {
            _promptLabel.setText(Bundle.getMessage("IconSetName", _mast.getSignalSystem().getSystemName()));
        } else {
            _promptLabel.setText(Bundle.getMessage("PickRowMast"));
        }

        _iconPanel.setVisible(false);
    }

    @Override
    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        if (_update) {
            return;
        }
        _dragIconPanel.removeAll();
        NamedIcon icon = getDragIcon();
        try {
            JLabel label = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR), icon);
            JPanel panel = makeDragIcon(icon, label);
            _dragIconPanel.add(panel);
        } catch (java.lang.ClassNotFoundException cnfe) {
            log.warn("no DndIconPanel for {}, {} created. {}", _itemType, displayKey, cnfe);
        }
    }

    @Override
    protected void makeBottomPanel(ActionListener doneAction) {
        JPanel panel = new JPanel();
        _showIconsButton = new JButton(Bundle.getMessage("ShowIcons"));
        _showIconsButton.addActionListener(a -> {
            if (_iconPanel.isVisible()) {
                hideIcons();
            } else {
                showIcons();
            }
        });
        _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipShowIcons"));
        panel.add(_showIconsButton);
        _bottom1Panel = new JPanel(new FlowLayout());
        _bottom1Panel.add(panel);
        if (doneAction != null) {
            addUpdateButtonToBottom(doneAction);
        }
        initIconFamiliesPanel(); // (if null: creates and) adds a new _iconFamilyPanel for the new mast map
        add(_bottom1Panel);
    }

    private void getIconMap(int row) {
        _mast = null;
        _iconMastMap = null;
        _family = null;
        if (row < 0) {
            return;
        }
        NamedBean bean = _model.getBySystemName((String) _table.getValueAt(row, 0));
        if (bean == null) {
            log.debug("getIconMap: NamedBean is null at row {}", row);
            return;
        }
        try {
            _mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(bean.getDisplayName());
            makeIconMap();
        } catch (IllegalArgumentException ex) {
            log.error("No SignalMast called {}", bean.getDisplayName());
        }
    }

    private void makeIconMap() {
        if (_mast == null) {
            return;
        }
        _family = _mast.getSignalSystem().getSystemName();
        _iconMastMap = new HashMap<>();
        SignalAppearanceMap appMap = _mast.getAppearanceMap();
        Enumeration<String> e = _mast.getAppearanceMap().getAspects();
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            String s = appMap.getImageLink(aspect, _family);
            if (s !=null && !s.equals("")) {
                if (!s.contains("preference:")) {
                    s = s.substring(s.indexOf("resources"));
                }
                NamedIcon n = new NamedIcon(s, s);
                _iconMastMap.put(aspect, n);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("makeIconMap for {}  size= {}", _family, _iconMastMap.size());
        }
    }

    private NamedIcon getDragIcon() {
        if (_iconMastMap != null) {
            if (_iconMastMap.containsKey("Clear")) {
                return _iconMastMap.get("Clear");
            } else if (_iconMastMap.containsKey("Stop")) {
                return _iconMastMap.get("Stop");
            }
            Iterator<String> e = _iconMastMap.keySet().iterator();
            if (e.hasNext()) {
                return _iconMastMap.get(e.next());
            }
        }
         return _defaultIcon;
    }

    @Override
    protected void setFamily(String family) {
        _family = family;
        _iconPanel.removeAll(); // just clear contents
        HashMap<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
        if (map != null) {
            _iconMastMap = map;
        } else {
            log.warn("Family \"{}\" for type \"{}\" for not found in Catalog.", _family, _itemType);
        }
        if (!_suppressDragging) {
            makeDragIconPanel(0);
            makeDndIconPanel(_iconMastMap, ""); // empty key OK, this uses getDragIcon()
        }
        if (_iconMastMap != null) {
            addIconsToPanel(_iconMastMap, _iconPanel, false);
        }
    }

    @Override
    protected void showIcons() {
        if (log.isDebugEnabled()) {
            log.debug("showIcons for= {}, {}", _itemType, _family);
        }
        boolean isPalette = (_frame instanceof ItemPalette);
        Dimension totalDim;
        if (isPalette) {
            totalDim = ItemPalette._tabPane.getSize();
        } else {
            totalDim = _frame.getSize();
        }
        Dimension oldDim = getSize();
        _iconPanel.setVisible(true);
        _iconPanel.invalidate();
        _previewPanel.setVisible(true);
        _previewPanel.invalidate();
        if (!_update) {
            _dragIconPanel.removeAll();
            _dragIconPanel.setVisible(false);
            _dragIconPanel.invalidate();
            _blurb.setVisible(false);
            _blurb.invalidate();

        }
        reSizeDisplay(isPalette, oldDim, totalDim);
        _showIconsButton.setText(Bundle.getMessage("HideIcons"));
    }

    @Override
    protected void hideIcons() {
        if (log.isDebugEnabled()) {
            log.debug("hideIcons for= {}, {}", _itemType, _family);
        }
        boolean isPalette = (_frame instanceof ItemPalette);
        Dimension totalDim;
        if (isPalette) {
            totalDim = ItemPalette._tabPane.getSize();
        } else {
            totalDim = _frame.getSize();
        }
        Dimension oldDim = getSize();
        _iconPanel.setVisible(false);
        _iconPanel.invalidate();
        if (!_update) {
            _dragIconPanel.setVisible(true);
            makeDndIconPanel(null, null);
            _dragIconPanel.invalidate();
            _blurb.setVisible(true);
            _blurb.invalidate();
            _previewPanel.setVisible(true);
            _previewPanel.invalidate();
        } else {
            _previewPanel.setVisible(false);
            _previewPanel.invalidate();
        }
        reSizeDisplay(isPalette, oldDim, totalDim);
        _showIconsButton.setText(Bundle.getMessage("ShowIcons"));
    }

    /**
     * ListSelectionListener action.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        log.debug("Table valueChanged: row= {}", row);

        // update the family icons
        _iconPanel.removeAll();
        if (row >= 0) {
            if (_updateButton != null) {
                _updateButton.setEnabled(true);
                _updateButton.setToolTipText(null);
            }
            _showIconsButton.setEnabled(true);
            _showIconsButton.setToolTipText(null);
        } else {
            if (_updateButton != null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
            }
            _mast = null;
            _showIconsButton.setEnabled(false);
            _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipPickRowToShowIcon"));
        }
        initIconFamiliesPanel(); // (if null: creates and) adds a new _iconFamilyPanel for the new mast map
        hideIcons();
    }

    protected JLabel getDragger(DataFlavor flavor, NamedIcon icon) {
        return new IconDragJLabel(flavor, icon);
    }

    protected class IconDragJLabel extends DragJLabel {

        public IconDragJLabel(DataFlavor flavor, NamedIcon icon) {
            super(flavor, icon);
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
                SignalMastIcon sm = new SignalMastIcon(_frame.getEditor());
                sm.setSignalMast(bean.getDisplayName());
                sm.setLevel(Editor.SIGNALS);
                return sm;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" icons for \"");
                sb.append(bean.getDisplayName());
                sb.append("\"");
                return sb.toString();
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastItemPanel.class);

}
