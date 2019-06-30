package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
public class EditPortalDirection extends jmri.util.JmriJFrame implements ActionListener, ListSelectionListener {

    private final OBlock _homeBlock;
    private final CircuitBuilder _parent;
    private PortalIcon _icon;
    private JRadioButton _toButton;
    private JRadioButton _fromButton;
    private JRadioButton _noButton;

    private PortalList _portalList;

    static int STRUT_SIZE = 10;
    static Point _loc = new Point(-1, -1);
    static Dimension _dim = null;

    public EditPortalDirection(String title, CircuitBuilder parent, OBlock block) {
        _homeBlock = block;
        _parent = parent;
        setTitle(java.text.MessageFormat.format(title, _homeBlock.getDisplayName()));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent();
            }
        });
        addHelpMenu("package.jmri.jmrit.display.CircuitBuilder", true);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        javax.swing.border.Border padding = BorderFactory.createEmptyBorder(10, 5, 4, 5);
        contentPane.setBorder(padding);

        contentPane.add(new JScrollPane(makePortalPanel()));
        setContentPane(contentPane);

        pack();
        if (_loc.x < 0) {
            setLocation(jmri.util.PlaceWindow. nextTo(_parent._editor, null, this));
        } else {
            setLocation(_loc);
            setSize(_dim);
        }
        setVisible(true);
    }

    private JPanel makeDoneButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                closingEvent();
            }
        });
        panel.add(doneButton);
        buttonPanel.add(panel);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(buttonPanel);

        return panel;
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

    private JPanel makePortalPanel() {
        JPanel portalPanel = new JPanel();
        portalPanel.setLayout(new BoxLayout(portalPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("PortalTitle", _homeBlock.getDisplayName())));
        portalPanel.add(panel);

        _portalList = new PortalList(_homeBlock);
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
            PortalIcon icon = _parent.getPortalIconMap().get(portal.getName());
            setPortalIcon(icon, false);
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

    protected void closingEvent() {
        _parent.closePortalDirection(_homeBlock);
        storeLocDim(getLocation(_loc), getSize(_dim));
        dispose();
    }

    private static void storeLocDim(@Nonnull Point location, @Nonnull Dimension size) {
        _loc = location;
        _dim = size;
    }

    protected OBlock getHomeBlock() {
        return _homeBlock;
    }

}
