package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;

/**
 *
 * @author Pete Cressman Copyright: Copyright (c) 2013
 *
 */
public class EditSignalFrame extends jmri.util.JmriJFrame implements ActionListener, ListSelectionListener {

    private final OBlock _homeBlock;
    private final CircuitBuilder _parent;
    private PortalIcon _icon;
    private JRadioButton _noButton;

    private PortalList _portalList;

    static int STRUT_SIZE = 10;
    static Point _loc = new Point(-1, -1);
    static Dimension _dim = null;

    public EditSignalFrame(String title, CircuitBuilder parent, OBlock block) {
        _homeBlock = block;
        _parent = parent;
        setTitle(java.text.MessageFormat.format(title, _homeBlock.getDisplayName()));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent(true);
            }
        });
        addHelpMenu("package.jmri.jmrit.display.CircuitBuilder", true);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        javax.swing.border.Border padding = BorderFactory.createEmptyBorder(10, 5, 4, 5);
        contentPane.setBorder(padding);

        contentPane.add(new JScrollPane(makeSignalPanel()));
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
                closingEvent(false);
            }
        });
        panel.add(doneButton);
        buttonPanel.add(panel);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(buttonPanel);

        return panel;
    }

    private JPanel makeSignalPanel() {
        JPanel signalPanel = new JPanel();
        signalPanel.setLayout(new BoxLayout(signalPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("SignalTitle", _homeBlock.getDisplayName())));
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
        signalPanel.add(makeDoneButtonPanel());
        return signalPanel;
    }

    protected void clearListSelection() {
        _portalList.clearSelection();
        _parent._editor.highlight(null);
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
            PortalIcon icon = _parent.getPortalIconMap().get(portal.getName());
            setPortalIcon(icon, false);
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
        HashMap<String, PortalIcon> iconMap = _parent.getPortalIconMap();
        if (log.isDebugEnabled()) {
            log.debug("checkPortalIcons: block {} has {} portals, iconMap has {} icons",
                    _homeBlock.getDisplayName(), portals.size(), iconMap.size());
        }
        if (portals.size() == 0) {
            msg = Bundle.getMessage("needPortal", _homeBlock.getDisplayName());
        }
        if (msg == null) {
            for (int i = 0; i < portals.size(); i++) {
                PortalIcon icon = iconMap.get(portals.get(i).getName());
                if (icon == null) {
                    msg = Bundle.getMessage("noPortalIcon", portals.get(i).getName());
                    break;
                }
            }
        }
        if (msg != null) {
            if (close) {
                JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder sb = new StringBuilder(msg);
                sb.append(" ");
                sb.append(Bundle.getMessage("exitQuestion"));
                int answer = JOptionPane.showConfirmDialog(this, sb.toString(), Bundle.getMessage("makePortal"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }
        _parent.closeEditSignalFrame(_homeBlock);
        _loc = getLocation(_loc);
        _dim = getSize(_dim);
        dispose();
    }


    protected OBlock getHomeBlock() {
        return _homeBlock;
    }

    private final static Logger log = LoggerFactory.getLogger(EditPortalFrame.class);
}
