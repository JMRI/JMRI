package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.SignalMast;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.picker.PickListModel;

/**
 *
 * @author Pete Cressman Copyright: Copyright (c) 2019
 *
 */
public class EditSignalFrame extends EditFrame implements ActionListener, ListSelectionListener {

    private PortalIcon _icon;

    private JTextField _mastName;
    private PortalList _portalList;
    OpenPickListButton<SignalMast> _pickTable;

    public EditSignalFrame(String title, CircuitBuilder parent, OBlock block) {
        super(title, parent, block);
        pack();
    }

    @Override
    protected JPanel makeContentPanel() {
        JPanel signalPanel = new JPanel();
        signalPanel.setLayout(new BoxLayout(signalPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("PortalTitle", _homeBlock.getDisplayName())));
        signalPanel.add(panel);

        _portalList = new PortalList(_homeBlock);
        _portalList.addListSelectionListener(this);
        signalPanel.add(new JScrollPane(_portalList));
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        JButton clearButton = new JButton(Bundle.getMessage("buttonClearSelection"));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                clearListSelection();
            }
        });
        signalPanel.add(clearButton);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("selectPortalProtection", _homeBlock.getDisplayName())));
        signalPanel.add(panel);

        panel = new JPanel();
        _mastName = new JTextField();
        panel.add(CircuitBuilder.makeTextBoxPanel(
                false, _mastName, "mastName", true, null));
        _mastName.setPreferredSize(new Dimension(300, _mastName.getPreferredSize().height));
        _mastName.setToolTipText(Bundle.getMessage("ToolTipMastName", _homeBlock.getDisplayName()));
        signalPanel.add(panel);

        String[] blurbLines = {Bundle.getMessage("DragMast", Bundle.getMessage("mastName"))};
        _pickTable = new OpenPickListButton<SignalMast>(blurbLines, PickListModel.signalMastPickModelInstance(), this);
        signalPanel.add(_pickTable.getButtonPanel());
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        signalPanel.add(makeDoneButtonPanel());
        return signalPanel;
    }

    protected void clearListSelection() {
        _portalList.clearSelection();
        _parent._editor.highlight(null);
        _mastName.setText(null);
    }

    protected void setSelected(PortalIcon icon) {
        Portal portal = icon.getPortal();
        _portalList.setSelectedValue(portal, true);
        if (log.isDebugEnabled()) {
            log.debug("setSelected: portal {}", portal.getName());
        }
        _parent._editor.highlight(icon);
        
    }

    /**
     * *********************** end setup *************************
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        Portal portal = _portalList.getSelectedValue();
        if (portal != null) {
            SignalMast mast =_parent.getProtectingSignal(_homeBlock, portal);
            if (mast != null) {
                _mastName.setText(mast.getDisplayName());
            } else {
                _mastName.setText(null);
            }
            java.util.List<PortalIcon> piArray = _parent.getPortalIconMap(portal);
            for (PortalIcon icon : piArray) {
                setPortalIcon(icon, false);
            }
        } else {
            _mastName.setText(null);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
    }

    protected void setPortalIcon(PortalIcon icon, boolean setValue) {
        _parent._editor.highlight(icon);
        if (_icon != null) {
            _icon.setStatus(PortalIcon.VISIBLE);
        }
        _icon = icon;
        if (_icon != null) {
            if (setValue) {
                _portalList.setSelectedValue(_icon.getPortal(), true);
            }
        }
    }

    protected void closingEvent(boolean close) {
        String msg = null;
        java.util.List<Portal> portals = _homeBlock.getPortals();
        if (log.isDebugEnabled()) {
            log.debug("checkPortalIcons: block {} has {} portals",
                    _homeBlock.getDisplayName(), portals.size());
        }
        if (portals.size() == 0) {
            msg = Bundle.getMessage("needPortal", _homeBlock.getDisplayName());
        }
        closingEvent(close, msg);
    }


    protected OBlock getHomeBlock() {
        return _homeBlock;
    }

    private final static Logger log = LoggerFactory.getLogger(EditPortalFrame.class);
}
