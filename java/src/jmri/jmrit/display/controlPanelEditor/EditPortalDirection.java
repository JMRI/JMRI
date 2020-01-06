package jmri.jmrit.display.controlPanelEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;

/**
 *
 * @author Pete Cressman Copyright: Copyright (c) 2013
 *
 */
public class EditPortalDirection extends EditFrame implements ActionListener, ListSelectionListener {

    private PortalIcon _icon;
    private JRadioButton _toButton;
    private JRadioButton _fromButton;
    private JRadioButton _noButton;

    private PortalList _portalList;

    public EditPortalDirection(String title, CircuitBuilder parent, OBlock block) {
        super(title, parent, block);
        pack();
        String msg = _parent.checkForPortals(block, "DirectionArrow");
        if (msg == null) {
            msg = _parent.checkForPortalIcons(block, "DirectionArrow");
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("incompleteCircuit"), JOptionPane.INFORMATION_MESSAGE);
            _canEdit = false;
        }
    }

    private JPanel makeArrowPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.black),
                Bundle.getMessage("ArrowIconsTitle")));
        panel.add(Box.createHorizontalStrut(200));

        ButtonGroup group = new ButtonGroup();
        _toButton = new JRadioButton(_parent._editor.getPortalIcon(PortalIcon.TO_ARROW));
        _toButton.setActionCommand(PortalIcon.TO_ARROW);
        _toButton.addActionListener(this);
        group.add(_toButton);
        panel.add(_toButton);

        _fromButton = new JRadioButton(_parent._editor.getPortalIcon(PortalIcon.FROM_ARROW));
        _fromButton.setActionCommand(PortalIcon.FROM_ARROW);
        _fromButton.addActionListener(this);
        group.add(_fromButton);
        panel.add(_fromButton);

        _noButton = new JRadioButton(Bundle.getMessage("noIcon"), _parent._editor.getPortalIcon(PortalIcon.HIDDEN));
        _noButton.setVerticalTextPosition(AbstractButton.CENTER);
        _noButton.setHorizontalTextPosition(AbstractButton.CENTER);
        _noButton.setActionCommand(PortalIcon.HIDDEN);
        _noButton.addActionListener(this);
        group.add(_noButton);
        panel.add(_noButton);

        return panel;
    }

    @Override
    protected JPanel makeContentPanel() {
        JPanel portalPanel = new JPanel();
        portalPanel.setLayout(new BoxLayout(portalPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("PortalTitle", _homeBlock.getDisplayName())));
        portalPanel.add(panel);

        _portalList = new PortalList(_homeBlock, this);
        _portalList.addListSelectionListener(this);
        portalPanel.add(new JScrollPane(_portalList));

        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(Bundle.getMessage("PortalDirection1", _homeBlock.getDisplayName()));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE / 2));
        l = new JLabel(Bundle.getMessage("PortalDirection2"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("PortalDirection3"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        l = new JLabel(Bundle.getMessage("PortalDirection4"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("PortalDirection5"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        portalPanel.add(panel);

        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel = new JPanel();
        panel.add(makeArrowPanel());
        portalPanel.add(panel);

        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        portalPanel.add(makeDoneButtonPanel());
        return portalPanel;
    }

    @Override
    protected void clearListSelection() {
        _portalList.clearSelection();
        _parent._editor.highlight(null);
    }

    /**
     * *********************** end setup *************************
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        Portal portal = _portalList.getSelectedValue();
        if (portal != null) {
            List<PortalIcon> piArray = _parent.getPortalIconMap(portal);
            if (piArray.isEmpty()) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("portalHasNoIcon", portal.getName()),
                        Bundle.getMessage("incompleteCircuit"), JOptionPane.INFORMATION_MESSAGE);
                clearListSelection();
            } else {
                for (PortalIcon icon : piArray) {
                    setPortalIcon(icon, false);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_icon == null) {
            return;
        }
        if (PortalIcon.TO_ARROW.equals(e.getActionCommand())) {
            _icon.setIcon(PortalIcon.TO_ARROW, _parent._editor.getPortalIcon(PortalIcon.TO_ARROW));
            _icon.setIcon(PortalIcon.FROM_ARROW, _parent._editor.getPortalIcon(PortalIcon.FROM_ARROW));
            _icon.setArrowOrientatuon(true);
            _icon.setHideArrows(false);
        } else if (PortalIcon.FROM_ARROW.equals(e.getActionCommand())) {
            _icon.setIcon(PortalIcon.TO_ARROW, _parent._editor.getPortalIcon(PortalIcon.FROM_ARROW));
            _icon.setIcon(PortalIcon.FROM_ARROW, _parent._editor.getPortalIcon(PortalIcon.TO_ARROW));
            _icon.setArrowOrientatuon(false);
            _icon.setHideArrows(false);
        } else if (PortalIcon.HIDDEN.equals(e.getActionCommand())) {
//         _icon.setIcon(PortalIcon.TO_ARROW, _parent._editor.getPortalIcon(PortalIcon.HIDDEN));      
//         _icon.setArrowOrientatuon(true);
            _icon.setHideArrows(true);
            _icon.setStatus(PortalIcon.HIDDEN);
            return;
        }
        _icon.setStatus(PortalIcon.TO_ARROW);
    }

    protected void setPortalIcon(PortalIcon icon, boolean setValue) {
        if (!canEdit()) {
            return;
        }
        _parent._editor.highlight(icon);
        if (_icon != null) {
            _icon.setStatus(PortalIcon.VISIBLE);
        }
        _icon = icon;
        if (_icon != null) {
            if (_icon.getArrowHide()) {
                _icon.setStatus(PortalIcon.HIDDEN);
            } else {
                OBlock toBlock = _icon.getPortal().getToBlock();
                if (_homeBlock.equals(toBlock)) {
                    _icon.setStatus(PortalIcon.TO_ARROW);
                } else {
                    _icon.setStatus(PortalIcon.FROM_ARROW);
                }
            }
            _toButton.setIcon(_icon.getIcon(PortalIcon.TO_ARROW));
            _fromButton.setIcon(_icon.getIcon(PortalIcon.FROM_ARROW));
            if (setValue) {
                _portalList.setSelectedValue(_icon.getPortal(), true);
            }
        }
    }

    @Override
    protected void closingEvent(boolean close) {
        String msg = _parent.checkForPortals(_homeBlock, "BlockPaths");
        if (msg == null) {
            msg = _parent.checkForPortalIcons(_homeBlock, "DirectionArrow");
        }
        if (msg != null) {
            close = true;
        }
        closingEvent(close, msg);
    }

}
