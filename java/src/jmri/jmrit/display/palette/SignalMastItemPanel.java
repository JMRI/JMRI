package jmri.jmrit.display.palette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalMastIcon;

import jmri.jmrit.picker.PickListModel;
import jmri.util.JmriJFrame;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SignalMast;

import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;

public class SignalMastItemPanel extends TableItemPanel implements ListSelectionListener {

    SignalMast _mast;

    public SignalMastItemPanel(JmriJFrame parentFrame, String  type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }
    
    public void init() {
        super.init();
        _table.getSelectionModel().addListSelectionListener(this);
        _showIconsButton.setEnabled(false);
        _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipPickRowToShowIcon"));
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
        if (_table!=null) {
            int row = _table.getSelectedRow();
            getIconMap(row);        // sets _currentIconMap & _mast, if they exist.
        }
        _dragIconPanel = new JPanel();
        makeDndIconPanel(null, null);
        _iconPanel = new JPanel();
        addIconsToPanel(_currentIconMap);
        _iconFamilyPanel.add(_dragIconPanel);
        JPanel panel = new JPanel();
        if (_mast!=null) {
            panel.add(new JLabel(Bundle.getMessage("IconSetName")+" "+
                                 _mast.getSignalSystem().getSystemName()));
        }
        _iconFamilyPanel.add(panel);
        _iconFamilyPanel.add(_iconPanel);
        _iconPanel.setVisible(false);
    }

    protected void makeDndIconPanel(Hashtable<String, NamedIcon> iconMap, String displayKey) {
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

    protected JPanel makeBottom1Panel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
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
        bottomPanel.add(_showIconsButton);
        return bottomPanel;
    }

    protected JPanel makeBottom2Panel() {
        return new JPanel();
    }
    private void getIconMap(int row) {
        if (row<0) {
            _currentIconMap = null;
            _family = null;
            return;
        }
        NamedBean bean = _model.getBeanAt(row);

        if (bean==null) {
            if (log.isDebugEnabled()) log.debug("getIconMap: NamedBean is null at row "+row);
            _currentIconMap = null;
            _family = null;
            return;
        }
        _mast = InstanceManager.signalMastManagerInstance().provideSignalMast(bean.getDisplayName());
        if (_mast == null) {
            log.error("getIconMap: No SignalMast called "+bean.getDisplayName());
            _currentIconMap = null;
            return;
        }
        _family = _mast.getSignalSystem().getSystemName();
        _currentIconMap = new java.util.Hashtable<String, NamedIcon>();
        Enumeration<String> e = _mast.getAppearanceMap().getAspects();
        while (e.hasMoreElements()) {
            String s = _mast.getAppearanceMap().getProperty(e.nextElement(), "imagelink");
            s = s.substring(s.indexOf("resources"));
            NamedIcon n = new NamedIcon(s,s);
            _currentIconMap.put(s, n);
        }
        if (log.isDebugEnabled()) log.debug("getIconMap: for "+_family+
                                            " size= "+_currentIconMap.size());
    }

    private NamedIcon getDragIcon() {
        if (_currentIconMap!=null) {
            if (_currentIconMap.contains("Stop")) {
                return _currentIconMap.get("Stop");
            }
            Enumeration<String> e = _currentIconMap.keys();
            if (e.hasMoreElements()) {
                return _currentIconMap.get(e.nextElement());
            }
        }
        String fileName = "resources/icons/misc/X-red.gif";
        return new NamedIcon(fileName, fileName);
    }


    /**
    *  ListSelectionListener action
    */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) log.debug("Table valueChanged: row= "+row);
        remove(_iconFamilyPanel);
        initIconFamiliesPanel();
        add(_iconFamilyPanel, 1);
        if (row >= 0) {
            if (_updateButton!=null) {
                _updateButton.setEnabled(true);
                _updateButton.setToolTipText(null);
            }
            _showIconsButton.setEnabled(true);
            _showIconsButton.setToolTipText(null);
        } else {
            if (_updateButton!=null) {
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

        public IconDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            NamedBean bean = getNamedBean();
            if (bean==null) {
                log.error("IconDragJLabel.getTransferData: NamedBean is null!");
                return null;
            }

            SignalMastIcon sm  = new SignalMastIcon(_editor);
            sm.setSignalMast(bean.getDisplayName());
            sm.setLevel(Editor.SIGNALS);
            return sm;
        }
    }
    
    static Logger log = LoggerFactory.getLogger(SignalMastItemPanel.class.getName());
}
