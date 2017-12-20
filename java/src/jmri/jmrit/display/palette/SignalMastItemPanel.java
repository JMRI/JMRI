package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SignalAppearanceMap;
import jmri.SignalMast;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TableItemPanel extension for placing of SignalMast items with a fixed set of icons.
 *
 * @author Pete Cressman Copyright (c) 2010, 2011
 * @author Egbert Broerse 2017
 */
public class SignalMastItemPanel extends TableItemPanel implements ListSelectionListener {

    SignalMast _mast;

    public SignalMastItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<jmri.SignalMast> model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _table.getSelectionModel().addListSelectionListener(this);
            _showIconsButton.setEnabled(false);
            _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipPickRowToShowIcon"));
            initIconFamiliesPanel();
            add(_iconFamilyPanel, 1);
        }
    }

    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("AddToPanel")));
        blurb.add(new JLabel(Bundle.getMessage("DragIconPanel")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("ToolTipPickRowToShowIcon")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    @Override
    protected void initIconFamiliesPanel() {
        boolean initialize = false;
        if (_iconFamilyPanel == null) {
            log.debug("new _iconFamilyPanel created");
            initialize = true;
            _iconFamilyPanel = new JPanel();
            _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
            _iconFamilyPanel.setOpaque(true);
            _iconFamilyPanel.add(instructions());
        }
        if (_table != null) {
            int row = _table.getSelectedRow();
            getIconMap(row); // sets _currentIconMap + _mast, if they exist.
        }
        makeDragIconPanel(1);
        makeDndIconPanel(null, null);
        if (_iconPanel == null) { // keep an existing panel
            _iconPanel = new ImagePanel();
            _iconPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
                    Bundle.getMessage("PreviewBorderTitle")));
        }
        if (_backgrounds != null) {
            _iconPanel.setImage(_backgrounds[_paletteFrame.getPreviewBg()]); // pick up shared setting
        } else {
            log.debug("SignalMastItemPanel - no value for previewBgSet");
        }
        addIconsToPanel(_currentIconMap);

        if (initialize) {
            JPanel panel = new JPanel();
            if (_mast != null) {
                panel.add(new JLabel(Bundle.getMessage("IconSetName", _mast.getSignalSystem().getSystemName())));
            } else {
                panel.add(new JLabel(Bundle.getMessage("PickRowMast")));
            }
            _iconFamilyPanel.add(panel);
            _iconFamilyPanel.add(_iconPanel);
        }
        _iconPanel.setVisible(false);
        hideIcons();
    }

    @Override
    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        if (_update) {
            return;
        }
        _dragIconPanel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));

        NamedIcon icon = getDragIcon();
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        String borderName = ItemPalette.convertText("dragToPanel");
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                borderName));
        JLabel label;
        try {
            label = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR), icon);
            label.setOpaque(false);
            label.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        } catch (java.lang.ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            label = new JLabel();
        }
        label.setName(borderName);
        panel.add(label);
        int width = Math.max(100, panel.getPreferredSize().width);
        panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
        panel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        _dragIconPanel.add(panel);
    }

    @Override
    protected void makeBottomPanel(ActionListener doneAction) {
        JPanel panel = new JPanel();
        _showIconsButton = new JButton(Bundle.getMessage("ShowIcons"));
        _showIconsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (_iconPanel.isVisible()) {
                    hideIcons();
                } else {
                    showIcons();
                }
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
        updateBackgrounds(); // create array of backgrounds
        _bottom1Panel.add(makeBgButtonPanel(_dragIconPanel, _iconPanel, _backgrounds, _paletteFrame));
        add(_bottom1Panel);
    }

    private void getIconMap(int row) {
        if (row < 0) {
            _currentIconMap = null;
            _family = null;
            return;
        }
        NamedBean bean = _model.getBeanAt(row);

        if (bean == null) {
            log.debug("getIconMap: NamedBean is null at row {}", row);
            _mast = null;
            _currentIconMap = null;
            _family = null;
            return;
        }
        
        try {
            _mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(bean.getDisplayName());
        } catch (IllegalArgumentException ex) {
            log.error("getIconMap: No SignalMast called {}", bean.getDisplayName());
            _currentIconMap = null;
            return;
        }
        _family = _mast.getSignalSystem().getSystemName();
        _currentIconMap = new HashMap<String, NamedIcon>();
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
                _currentIconMap.put(aspect, n);                
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("getIconMap for {}  size= {}", _family, _currentIconMap.size());
        }
    }

    private NamedIcon getDragIcon() {
        if (_currentIconMap != null) {
            if (_currentIconMap.keySet().contains("Stop")) {
                return _currentIconMap.get("Stop");
            }
            Iterator<String> e = _currentIconMap.keySet().iterator();
            if (e.hasNext()) {
                return _currentIconMap.get(e.next());
            }
        }
        String fileName = "resources/icons/misc/X-red.gif";
        return new NamedIcon(fileName, fileName);
    }

    @Override
    protected void setEditor(Editor ed) {
        _editor = ed;
        if (_initialized) {
            makeDragIconPanel(0);
            makeDndIconPanel(_currentIconMap, ""); // empty key OK, this uses getDragIcon()
        }
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
        validate();
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
                SignalMastIcon sm = new SignalMastIcon(_editor);
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
