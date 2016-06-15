package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalMastItemPanel extends TableItemPanel implements ListSelectionListener {

    /**
     *
     */
    private static final long serialVersionUID = -7308622179668168349L;
    SignalMast _mast;

    public SignalMastItemPanel(JmriJFrame parentFrame, String type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    public void init() {
        super.init();
        _table.getSelectionModel().addListSelectionListener(this);
        _showIconsButton.setEnabled(false);
        _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipPickRowToShowIcon"));
        initIconFamiliesPanel();
        add(_iconFamilyPanel, 1);
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

    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        if (!_update) {
            _iconFamilyPanel.add(instructions());
        }
        if (_table != null) {
            int row = _table.getSelectedRow();
            getIconMap(row);        // sets _currentIconMap & _mast, if they exist.
        }
        _dragIconPanel = new JPanel();
        makeDndIconPanel(null, null);
        _iconPanel = new JPanel();
        addIconsToPanel(_currentIconMap);
        _iconFamilyPanel.add(_dragIconPanel);
        JPanel panel = new JPanel();
        if (_mast != null) {
            panel.add(new JLabel(Bundle.getMessage("IconSetName") + " "
                    + _mast.getSignalSystem().getSystemName()));
        }
        _iconFamilyPanel.add(panel);
        _iconFamilyPanel.add(_iconPanel);
        _iconPanel.setVisible(false);
        hideIcons();
    }

    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        if (_update) {
            return;
        }
        _dragIconPanel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));

        NamedIcon icon = getDragIcon();
        JPanel panel = new JPanel();
        String borderName = ItemPalette.convertText("dragToPanel");
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                borderName));
        JLabel label;
        try {
            label = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
            label.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        } catch (java.lang.ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            label = new JLabel();
        }
        label.setIcon(icon);
        label.setName(borderName);
        panel.add(label);
        int width = Math.max(100, panel.getPreferredSize().width);
        panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
        panel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        _dragIconPanel.add(panel);
    }

    protected void makeBottomPanel() {
        JPanel panel = new JPanel();
        _showIconsButton = new JButton(Bundle.getMessage("ShowIcons"));
        _showIconsButton.addActionListener(new ActionListener() {
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
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(panel);
        add(bottomPanel);
    }

    private void getIconMap(int row) {
        if (row < 0) {
            _currentIconMap = null;
            _family = null;
            return;
        }
        NamedBean bean = _model.getBeanAt(row);

        if (bean == null) {
            if (log.isDebugEnabled()) {
                log.debug("getIconMap: NamedBean is null at row " + row);
            }
            _currentIconMap = null;
            _family = null;
            return;
        }
        _mast = InstanceManager.signalMastManagerInstance().provideSignalMast(bean.getDisplayName());
        if (_mast == null) {
            log.error("getIconMap: No SignalMast called " + bean.getDisplayName());
            _currentIconMap = null;
            return;
        }
        _family = _mast.getSignalSystem().getSystemName();
        _currentIconMap = new HashMap<String, NamedIcon>();
        SignalAppearanceMap appMap = _mast.getAppearanceMap();
        Enumeration<String> e = _mast.getAppearanceMap().getAspects();
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            String s = appMap.getProperty(aspect, "imagelink");
            NamedIcon n = new NamedIcon(s, s);
            _currentIconMap.put(aspect, n);
        }
        if (log.isDebugEnabled()) {
            log.debug("getIconMap: for " + _family
                    + " size= " + _currentIconMap.size());
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

    /**
     * ListSelectionListener action
     */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) {
            log.debug("Table valueChanged: row= " + row);
        }
        remove(_iconFamilyPanel);
        initIconFamiliesPanel();
        add(_iconFamilyPanel, 1);
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
            _showIconsButton.setEnabled(false);
            _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipPickRowToShowIcon"));
        }
        validate();
    }

    protected JLabel getDragger(DataFlavor flavor) {
        return new IconDragJLabel(flavor);
    }

    protected class IconDragJLabel extends DragJLabel {

        /**
         *
         */
        private static final long serialVersionUID = -2350506428940610321L;

        public IconDragJLabel(DataFlavor flavor) {
            super(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            NamedBean bean = getNamedBean();
            if (bean == null) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("noRowSelected"),
                        Bundle.getMessage("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }

            SignalMastIcon sm = new SignalMastIcon(_editor);
            sm.setSignalMast(bean.getDisplayName());
            sm.setLevel(Editor.SIGNALS);
            return sm;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastItemPanel.class.getName());
}
