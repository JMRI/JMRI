package jmri.jmrit.display.controlPanelEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.InstanceManager;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrack;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * @author Pete Cressman Copyright: Copyright (c) 2011
 *
 */
public class EditPortalFrame extends jmri.util.JmriJFrame implements ListSelectionListener {

    private OBlock _homeBlock;
    private CircuitBuilder _parent;
    private OBlock _adjacentBlock;

    private PortalList _portalList;
    private String _currentPortalName;   // name of last portal Icon made

    private JTextField _portalName = new JTextField();
    private JPanel _dndPanel;

    static int STRUT_SIZE = 10;
    static boolean _firstInstance = true;
    static Point _loc = null;
    static Dimension _dim = null;

    /* Ctor for fix a portal error  */
    public EditPortalFrame(String title, CircuitBuilder parent, OBlock block, Portal portal, OBlock adjacent) {
        this(title, parent, block, true);
        _adjacentBlock = adjacent;
        String name = portal.getName();
        _portalName.setText(name);
        _currentPortalName = name;
        _portalName.setText(name);

        if (_parent.getPortalIconMap().get(name) == null) {
            PortalIcon pi = new PortalIcon(_parent._editor, portal);
            pi.setLevel(Editor.MARKERS);
            pi.setStatus(PortalIcon.VISIBLE);
            _parent.addPortalIcon(pi);
            java.util.List<Positionable> list = _parent.getCircuitGroup();
            Iterator<Positionable> iter = list.iterator();
            while (iter.hasNext()) {
                Positionable pos = iter.next();
                if (pos instanceof IndicatorTrack) {
                    int x = pos.getX() + pos.getWidth() / 2;
                    int y = pos.getY() + pos.getHeight() / 2;
                    pi.setLocation(x, y);
                    parent._editor.putItem(pi);
                    break;
                }
            }
        }
    }

    public EditPortalFrame(String title, CircuitBuilder parent, OBlock block, boolean update) {
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

        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));
        contentPane.add(makePortalPanel(update));
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel border = new JPanel();
        border.setLayout(new java.awt.BorderLayout(10, 10));
        border.add(contentPane);
        setContentPane(border);
        pack();
        if (_firstInstance) {
            setLocationRelativeTo(_parent._editor);
//            setSize(500,500);
            _firstInstance = false;
        } else {
            setLocation(_loc);
            setSize(_dim);
        }
        setVisible(true);
    }

    private JPanel makeButtonPanel() {
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

    /**
     *
     * @param update true frame for correcting icon position
     * @return JPanel
     */
    private JPanel makePortalPanel(boolean update) {
        JPanel portalPanel = new JPanel();
        portalPanel.setLayout(new BoxLayout(portalPanel, BoxLayout.Y_AXIS));
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("PortalTitle", _homeBlock.getDisplayName())));
        portalPanel.add(panel);

        _portalList = new PortalList(_homeBlock);
        _portalList.addListSelectionListener(this);
        portalPanel.add(new JScrollPane(_portalList));

        JButton clearButton = new JButton(Bundle.getMessage("buttonClearSelection"));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                clearListSelection();
            }
        });
        panel = new JPanel();
        panel.add(clearButton);
        portalPanel.add(panel);
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.add(CircuitBuilder.makeTextBoxPanel(
                false, _portalName, "portalName", true, null));
        _portalName.setPreferredSize(new Dimension(300, _portalName.getPreferredSize().height));
        _portalName.setToolTipText(Bundle.getMessage("TooltipPortalName",
                _homeBlock.getDisplayName()));
        portalPanel.add(panel);

        panel = new JPanel();
        JButton changeButton = new JButton(Bundle.getMessage("buttonChangeName"));
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                changePortalName();
            }
        });
        changeButton.setToolTipText(Bundle.getMessage("ToolTipChangeName"));
        panel.add(changeButton);

        JButton deleteButton = new JButton(Bundle.getMessage("buttonDeletePortal"));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                deletePortal();
            }
        });
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeletePortal"));
        panel.add(deleteButton);

        portalPanel.add(panel);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        if (update) {
            JLabel l = new JLabel(Bundle.getMessage("portalIconPosition"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.add(l);
            l = new JLabel(Bundle.getMessage("placeIconOnGap"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.add(l);
            JPanel p = new JPanel();
            p.add(panel);
            portalPanel.add(p);
        } else {
            JLabel l = new JLabel(Bundle.getMessage("enterNameToDrag"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.add(l);
            l = new JLabel(Bundle.getMessage("dragNewIcon"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.add(l);
            panel.add(Box.createVerticalStrut(STRUT_SIZE / 2));
            l = new JLabel(Bundle.getMessage("selectPortal"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.add(l);
            l = new JLabel(Bundle.getMessage("dragIcon"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.add(l);
            JPanel p = new JPanel();
            p.add(panel);
            portalPanel.add(p);

            portalPanel.add(makeDndIconPanel());
            portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        }
        portalPanel.add(makeButtonPanel());
        return portalPanel;
    }

    private void clearListSelection() {
        _portalList.clearSelection();
        _portalName.setText(null);
        _parent._editor.highlight(null);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        Portal portal = _portalList.getSelectedValue();
        if (portal != null) {
            _portalName.setText(portal.getName());
            PortalIcon icon = _parent.getPortalIconMap().get(portal.getName());
            if (icon != null) {
                icon.setStatus(PortalIcon.VISIBLE);
            }
            _parent._editor.highlight(icon);
        } else {
            _portalName.setText(null);
        }
    }

    /**
     * *********************** end setup *************************
     */
    private void changePortalName() {
        Portal portal = _portalList.getSelectedValue();
        String oldName = null;
        if (portal != null) {
            oldName = portal.getName();
        }
        String name = _portalName.getText();
        if (portal == null || name == null || name.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("changePortalName", Bundle.getMessage("buttonChangeName")),
                    Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String msg = portal.setName(name);
        if (msg == null) {
            _parent.changePortalName(oldName, name);  // update maps
            _portalList.dataChange();
        } else {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deletePortal() {
        String name = _portalName.getText();
        Portal portal = _portalList.getSelectedValue();
        if (portal == null) {
            PortalManager portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);
            portal = portalMgr.getByUserName(name);
        }
        if (portal != null) {
            int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("confirmPortalDelete",
                    portal.getName()), Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                // removes portal and stubs all paths through it.
                portal.dispose();
                _portalList.dataChange();
            }
        }
        _portalName.setText(null);
        PortalIcon icon = _parent.getPortalIconMap().get(name);
        if (icon != null) {
            deletePortalIcon(icon);
        }
        _currentPortalName = null;
    }

    private void deletePortalIcon(PortalIcon icon) {
        if (log.isDebugEnabled()) {
            log.debug("deletePortalIcon: " + icon.getName());
        }
        _parent.removePortalIcon(icon.getName());
        _parent.getCircuitIcons(_homeBlock).remove(icon);
        icon.remove();
        _parent._editor.getSelectionGroup().remove(icon);
        _parent._editor.repaint();
    }

    protected void closingEvent(boolean close) {
        if (checkPortalIcons() || close) {
            _parent.closePortalFrame(_homeBlock);
            _loc = getLocation(_loc);
            _dim = getSize(_dim);
            dispose();
        }
    }

    private boolean checkPortalIcons() {
        java.util.List<Portal> portals = _homeBlock.getPortals();
        HashMap<String, PortalIcon> iconMap = _parent.getPortalIconMap();
        if (log.isDebugEnabled()) {
            log.debug("checkPortalIcons: " + _homeBlock.getDisplayName()
                    + " has " + portals.size() + " portals, iconMap has "
                    + iconMap.size() + " icons");
        }
        for (int i = 0; i < portals.size(); i++) {
            PortalIcon icon = iconMap.get(portals.get(i).getName());
            if (icon == null) {
                int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("noPortalIcon",
                        portals.get(i).getName()), Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    return false;
                }
            } else {
                if (!checkPortal(icon)) {
                    return false;
                }
                if (!checkPortalIconForUpdate(icon, false)) {
                    _parent._editor.highlight(icon);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * ***************** end button actions **********
     */
    private boolean checkPortal(PortalIcon icon) {
        Portal portal = icon.getPortal();
        if (portal == null) {
            deletePortalIcon(icon);
        } else {
            boolean home = false;
            OBlock block = portal.getToBlock();
            if (block == null) {
                _parent._editor.highlight(icon);
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("portalNeedsBlock", portal.getDisplayName()),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            home = _homeBlock.equals(block);
            block = portal.getFromBlock();
            if (block == null) {
                _parent._editor.highlight(icon);
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("portalNeedsBlock", portal.getDisplayName()),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            } else if (!home) {
                home = _homeBlock.equals(block);
            }
            if (!home) {
                _parent._editor.highlight(icon);
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("portalNotInCircuit", _homeBlock.getDisplayName()),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Called after click on portal icon moved recheck block connections.
     *
     * @param icon  the icon to check
     * @param moved true if moved; false otherwise
     * @return true if no changes to connections; false otherwise
     */
    protected boolean checkPortalIconForUpdate(PortalIcon icon, boolean moved) {
        Portal portal = icon.getPortal();
        if (portal == null) {
            icon.remove();
            log.error("Removed PortalIcon without Portal");
            return false;
        }
        String name = portal.getDisplayName();
        String msg = null;
        OBlock fromBlock = portal.getFromBlock();
        OBlock toBlock = portal.getToBlock();
        if (!_homeBlock.equals(fromBlock) && !_homeBlock.equals(toBlock)) {
            msg = Bundle.getMessage("iconNotOnBlock", _homeBlock.getDisplayName(),
                    portal.getDescription());
            _adjacentBlock = null;
        }
        if (msg == null) {
            msg = iconIntersectsBlock(icon, _homeBlock);
        }
        if (log.isDebugEnabled()) {
            log.debug("checkPortalIconForUpdate: _homeBlock= " + _homeBlock.getDisplayName()
                    + " icon On _homeBlock " + (msg == null) + ".  msg = " + msg);
        }
        if (msg == null) {
            if (_homeBlock.equals(fromBlock)) {
                _adjacentBlock = toBlock;
            } else {
                _adjacentBlock = fromBlock;
            }
            if (_adjacentBlock == null) { // maybe first time
                _adjacentBlock = findAdjacentBlock(icon);
                if (_homeBlock.equals(fromBlock)) {
                    portal.setToBlock(_adjacentBlock, false);
                } else {
                    portal.setFromBlock(_adjacentBlock, false);
                }
                _portalList.dataChange();
            }
            if (moved) {
                OBlock block = findAdjacentBlock(icon);
                if (log.isDebugEnabled()) {
                    log.debug("Icon also on " + (block != null ? block.getDisplayName() : "null")
                            + ", _adjacentBlock= " + (_adjacentBlock != null ? _adjacentBlock.getDisplayName() : "null"));
                }
                if (block != null && block.equals(_adjacentBlock)) {
                    _portalList.setSelectedValue(portal, true);
                    return true;  // no change  in connection
                }
            }
            if (_adjacentBlock != null) {
                msg = iconIntersectsBlock(icon, _adjacentBlock);
                if (msg == null) {
                    _portalList.setSelectedValue(portal, true);
                    return true;  // no change  in connection
                }
            }
            if (_adjacentBlock == null) {
                // icon not on Portal blocks Find a new _adjacentBlock
                _adjacentBlock = findAdjacentBlock(icon);
                if (_adjacentBlock != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Current position: _homeBlock= " + _homeBlock.getDisplayName()
                                + " and _adjacentBlock= " + _adjacentBlock.getDisplayName());
                    }
                    int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("repositionPortal",
                            name, _homeBlock.getDisplayName(), _adjacentBlock.getDisplayName()),
                            Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        // Change portal position to join different pair of blocks
                        if (_homeBlock.equals(fromBlock)) {
                            portal.setToBlock(_adjacentBlock, true);
                        } else {
                            portal.setFromBlock(_adjacentBlock, true);
                        }
                    }
                    msg = null;
                } else {
                    msg = Bundle.getMessage("iconNotOnAdjacent", icon.getNameString(), _homeBlock.getDisplayName());
                }
            }
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            clearListSelection();
            return false;
        }
        _portalList.setSelectedValue(portal, true);
        _portalList.dataChange();
        return true;
    }

    /*
     * If icon is on the home block, find another intersecting block
     */

    private OBlock findAdjacentBlock(PortalIcon icon) {
        ArrayList<OBlock> neighbors = new ArrayList<OBlock>();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        for (int j = 0; j < sysNames.length; j++) {
            OBlock block = manager.getBySystemName(sysNames[j]);
            if (block.equals(_homeBlock)) {
                continue;
            }
            if (iconIntersectsBlock(icon, block) == null) {
                neighbors.add(block);
            }
        }
        OBlock block = null;
        if (neighbors.size() == 1) {
            block = neighbors.get(0);
        } else if (neighbors.size() > 1) {
            // show list
            String[] selects = new String[neighbors.size()];
            Iterator<OBlock> iter = neighbors.iterator();
            int i = 0;
            while (iter.hasNext()) {
                selects[i++] = iter.next().getDisplayName();
            }
            Object select = JOptionPane.showInputDialog(this, Bundle.getMessage("multipleBlockSelections",
                    _homeBlock.getDisplayName()), Bundle.getMessage("QuestionTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, selects, null);
            if (select != null) {
                iter = neighbors.iterator();
                while (iter.hasNext()) {
                    block = iter.next();
                    if (((String) select).equals(block.getDisplayName())) {
                        return block;
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("findAdjacentBlock: neighbors.size()= " + neighbors.size()
                    + " return " + (block == null ? "null" : block.getDisplayName()));
        }
        return block;
    }

    /**
     * Query whether icon intersects any track icons of block
     *
     * @return null if intersection, otherwise a messags
     */
    private String iconIntersectsBlock(PortalIcon icon, OBlock block) {
        java.util.List<Positionable> list = _parent.getCircuitIcons(block);
        if (list == null || list.size() == 0) {
            return Bundle.getMessage("needIcons", block.getDisplayName(), Bundle.getMessage("editCircuitItem"));
        }
        Rectangle rect = new Rectangle();
        Rectangle iconRect = icon.getBounds(new Rectangle());
        for (int i = 0; i < list.size(); i++) {
            Positionable comp = list.get(i);
            if (CircuitBuilder.isTrack(comp)) {
                rect = list.get(i).getBounds(rect);
                if (iconRect.intersects(rect)) {
                    return null;
                }
            }
        }
        return Bundle.getMessage("iconNotOnBlock",
                block.getDisplayName(), icon.getNameString());
    }

    //////////////////////////// DnD ////////////////////////////
    protected JPanel makeDndIconPanel() {
        _dndPanel = new JPanel();

        String fileName = "resources/icons/throttles/RoundRedCircle20.png";
        NamedIcon icon = new NamedIcon(fileName, fileName);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                Bundle.getMessage("BeanNamePortal")));
        try {
            JLabel label = new IconDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
            label.setIcon(icon);
            label.setName(Bundle.getMessage("BeanNamePortal"));
            panel.add(label);
        } catch (java.lang.ClassNotFoundException cnfe) {
            log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
        }
        _dndPanel.add(panel);
        return _dndPanel;
    }

    public class IconDragJLabel extends DragJLabel {

        public IconDragJLabel(DataFlavor flavor) {
            super(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String name = _portalName.getText();
            if (name == null || name.trim().length() == 0) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("needPortalName"),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return null;
            }
            PortalManager portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);
            Portal p = portalMgr.getByUserName(name);
            if (p != null && !(_homeBlock.equals(p.getFromBlock()) || _homeBlock.equals(p.getToBlock()))) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("portalExists", name, p.getFromBlockName(), p.getToBlockName()),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return null;
            }
            PortalIcon pi = _parent.getPortalIconMap().get(name);
            if (name.equals(_currentPortalName) || pi != null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("portalIconExists", name),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                _parent._editor.highlight(pi);
                return null;
            }
            Portal portal = _homeBlock.getPortalByName(name);
            if (portal == null) {
                portalMgr = InstanceManager.getDefault(PortalManager.class);
                portal = portalMgr.createNewPortal(null, name);
                portal.setFromBlock(_homeBlock, false);
                _portalList.dataChange();
            }
            pi = new PortalIcon(_parent._editor, portal);
            pi.setLevel(Editor.MARKERS);
            pi.setStatus(PortalIcon.VISIBLE);
            _parent.addPortalIcon(pi);
            _portalList.setSelectedValue(portal, true);
            _currentPortalName = name;
            return pi;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditPortalFrame.class);
}
