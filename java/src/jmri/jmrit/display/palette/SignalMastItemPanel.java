package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.image.BufferedImage;
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
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JmriJFrame;
import jmri.util.swing.DrawSquares;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalMastItemPanel extends TableItemPanel implements ListSelectionListener {

    SignalMast _mast;

    public SignalMastItemPanel(JmriJFrame parentFrame, String type, String family, PickListModel<jmri.SignalMast> model, Editor editor) {
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
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        _iconFamilyPanel.setOpaque(true);
        if (!_update) {
            _iconFamilyPanel.add(instructions());
        }
        if (_table != null) {
            int row = _table.getSelectedRow();
            getIconMap(row); // sets _currentIconMap + _mast, if they exist.
        }
        makeDragIconPanel(1);
        makeDndIconPanel(null, null);
        _iconPanel = new ImagePanel();
        _iconPanel.setOpaque(false);
        _iconPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage("PreviewBorderTitle")));
        addIconsToPanel(_currentIconMap);
        // _iconFamilyPanel.add(_dragIconPanel, 1); // added twice? illegal position error

//        // create array of backgrounds
//        _backgrounds = new BufferedImage[5];
//        _currentBackground = _editor.getTargetPanel().getBackground(); // start using Panel background color
//        _backgrounds[0] = DrawSquares.getImage(_iconPanel, 20, _currentBackground, _currentBackground);
//        for (int i = 1; i <= 3; i++) {
//            _backgrounds[i] = DrawSquares.getImage(_iconPanel, 20, colorChoice[i - 1], colorChoice[i - 1]); // choice 0 is not in colorChoice[]
//        }
//        _backgrounds[4] = DrawSquares.getImage(_iconPanel, 20, Color.white, _grayColor);

        JPanel panel = new JPanel();
        if (_mast != null) {
            panel.add(new JLabel(Bundle.getMessage("IconSetName", _mast.getSignalSystem().getSystemName())));
        } else {
            panel.add(new JLabel(Bundle.getMessage("PickRowMast")));
        }
        _iconFamilyPanel.add(panel);
        _iconFamilyPanel.add(_iconPanel);
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
        initIconFamiliesPanel();
        _bottom1Panel.add(makeButtonPanel(_iconPanel, _backgrounds));
        add(_bottom1Panel);
    }

//    /**
//     * Create panel element containing [Set background:] drop down list.
//     * @see jmri.jmrit.catalog.PreviewDialog#setupPanel()
//     * @see DecoratorPanel
//     * @see FamilyItemPanel
//     *
//     * @return a JPanel with label and drop down
//     */
//    private JPanel makeButtonPanel() {
//        JComboBox<String> bgColorBox = new JComboBox<>();
//        bgColorBox.addItem(Bundle.getMessage("PanelBgColor")); // PanelColor key is specific for CPE, too long for combo
//        bgColorBox.addItem(Bundle.getMessage("White"));
//        bgColorBox.addItem(Bundle.getMessage("LightGray"));
//        bgColorBox.addItem(Bundle.getMessage("DarkGray"));
//        // bgColorBox.addItem(Bundle.getMessage("Checkers")); // checkers option not yet in combobox, under development
//        bgColorBox.setSelectedIndex(0); // panel bg color
//        bgColorBox.addActionListener((ActionEvent e) -> {
//            if (bgColorBox.getSelectedIndex() == 0) {
//                // use panel background color
//                _currentBackground = _editor.getTargetPanel().getBackground();
//                _squaresPanel.setVisible(false);
//                _iconFamilyPanel.setBackground(_currentBackground);
//            } else if (bgColorBox.getSelectedIndex() == 4) { // display checkers background, under development 4.9.6
//                _squaresPanel.setVisible(true);
//                log.debug("FamilyItemPanel checkers visible");
//                _iconFamilyPanel.setOpaque(false);
//            } else {
//                _currentBackground = colorChoice[bgColorBox.getSelectedIndex() - 1]; // choice 0 is not in colorChoice[]
//                _squaresPanel.setVisible(false);
//                _iconFamilyPanel.setBackground(_currentBackground);
//            }
//        });
//        JPanel backgroundPanel = new JPanel();
//        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
//        JPanel pp = new JPanel();
//        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
//        pp.add(new JLabel(Bundle.getMessage("setBackground")));
//        pp.add(bgColorBox);
//        backgroundPanel.add(pp);
//        backgroundPanel.setMaximumSize(backgroundPanel.getPreferredSize());
//        return backgroundPanel;
//    }

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
        if (log.isDebugEnabled()) {
            log.debug("Table valueChanged: row= " + row);
        }
        remove(_iconFamilyPanel);
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
        initIconFamiliesPanel();
        add(_iconFamilyPanel, 1);
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
