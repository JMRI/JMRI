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
import java.util.SortedSet;
import javax.annotation.Nonnull;
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author Pete Cressman Copyright: Copyright (c) 2011
 */
public class EditPortalFrame extends jmri.util.JmriJFrame implements ListSelectionListener {

    private final OBlock _homeBlock;
    private final CircuitBuilder _parent;
    private OBlock _adjacentBlock;

    private PortalList _portalList;
    private String _currentPortalName;   // name of last portal Icon made

    private final JTextField _portalName = new JTextField();
    private JPanel _dndPanel;

    static int STRUT_SIZE = 10;
    static Point _loc = new Point(-1, -1);
    static Dimension _dim = null;

    /* Ctor for fix a portal error  */
    public EditPortalFrame(String title, CircuitBuilder parent, OBlock block, Portal portal, OBlock adjacent) {
        this(title, parent, block);
        _adjacentBlock = adjacent;
        String name = portal.getName();
        _portalName.setText(name);
        _currentPortalName = name;
        _portalName.setText(name);

        PortalIcon pi = _parent.getPortalIconMap().get(name);
        if (_parent.getPortalIconMap().get(name) == null) {
            pi = new PortalIcon(_parent._editor, portal);
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
        setSelected(pi);
        _parent._editor.highlight(pi);
        JOptionPane.showMessageDialog(this, Bundle.getMessage("portalIconPosition"),
                Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
   }

    public EditPortalFrame(String title, CircuitBuilder parent, OBlock block) {
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

    private JPanel makePortalPanel() {
        JPanel portalPanel = new JPanel();
        portalPanel.setLayout(new BoxLayout(portalPanel, BoxLayout.Y_AXIS));

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
        portalPanel.add(makeButtonPanel());
        return portalPanel;
    }

    protected void clearListSelection() {
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

    protected void setSelected(PortalIcon icon) {
        Portal portal = icon.getPortal();
        _portalList.setSelectedValue(portal, true);
        _currentPortalName = portal.getName();
        _parent._editor.highlight(icon);
        
    }

    /*
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

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    private void deletePortal() {
        String name = _portalName.getText();
        if (name == null || name.length() == 0) {
            return;
        }
        Portal portal = _portalList.getSelectedValue();
        if (portal == null) {
            PortalManager portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);
            portal = portalMgr.getByUserName(name);
        }
        if (portal != null) {
            OBlock oppBlock = portal.getOpposingBlock(_homeBlock);
            String[] options = {Bundle.getMessage("deleteBoth"), Bundle.getMessage("iconOnly"), Bundle.getMessage("cancel")};
            int result = JOptionPane.showOptionDialog(this, Bundle.getMessage("confirmPortalDelete",
                    portal.getName()), Bundle.getMessage("makePortal"), JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, null);
            switch (result) {
                case 0:
                    // removes portal and stubs all paths through it.
                    portal.dispose();
                    _portalList.dataChange();
                    _portalName.setText(null);
                    // fall through to remove icon
                case 1:
                    _currentPortalName = null;
                    PortalIcon icon = _parent.getPortalIconMap().get(name);
                    if (icon != null) {
                        deletePortalIcon(icon);
                        _parent.getCircuitIcons(oppBlock).remove(icon);
                    }
                    break;
                default:
                    break;
            }
        }
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
                msg = checkPortal(icon);
                if (msg != null) {
                    break;
                }
                msg = checkPortalIconForUpdate(icon, false);
                if (msg != null) {
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
        _parent.closePortalFrame(_homeBlock);
        storeLocDim(getLocation(_loc), getSize(_dim));
        dispose();
    }

    private static void storeLocDim(@Nonnull Point location, @Nonnull Dimension size) {
        _loc = location;
        _dim = size;
    }
    /*
     * ***************** end button actions **********
     */
    private String checkPortal(PortalIcon icon) {
        Portal portal = icon.getPortal();
        if (portal == null) {
            deletePortalIcon(icon);
        } else {
            OBlock block = portal.getToBlock();
            if (block == null) {
                _parent._editor.highlight(icon);
                return Bundle.getMessage("portalNeedsBlock", portal.getDisplayName());
            }
            boolean home = _homeBlock.equals(block);
            block = portal.getFromBlock();
            if (block == null) {
                _parent._editor.highlight(icon);
                return Bundle.getMessage("portalNeedsBlock", portal.getDisplayName());
            }
            if (!home) {
                home = _homeBlock.equals(block);
            }
            if (!home) {    // portal must belong to either toBlock or fromBlock
                if (!_homeBlock.equals(block)) {
                    _parent._editor.highlight(icon);
                    return Bundle.getMessage("portalNotInCircuit", portal.getDisplayName(), _homeBlock.getDisplayName());
                }
            }
        }
        return null;
    }

    /**
     * Called after click on portal icon moved recheck block connections.
     *
     * @param icon  the icon to check
     * @param moved true if moved; false otherwise
     * @return true if no changes to connections; false otherwise
     */
    protected String checkPortalIconForUpdate(PortalIcon icon, boolean moved) {
        Portal portal = icon.getPortal();
        if (portal == null) {
            icon.remove();
            log.error("Removed PortalIcon without Portal");
            return null;
        }
        String name = portal.getDisplayName();
        String msg = null;
        OBlock fromBlock = portal.getFromBlock();
        OBlock toBlock = portal.getToBlock();
        if (!_homeBlock.equals(fromBlock) && !_homeBlock.equals(toBlock)) {
            msg = Bundle.getMessage("iconNotOnBlock", _homeBlock.getDisplayName(), icon.getName());
        }
        if (msg == null) {
            msg = iconIntersectsBlock(icon, _homeBlock);
            if (log.isDebugEnabled()) {
                log.debug("checkPortalIconForUpdate: Icon {} on _homeBlock \"{}\". {}",
                        (msg==null?"is":"not"), _homeBlock.getDisplayName(), (msg==null?"":msg));
            }
        }
        
        if (_homeBlock.equals(fromBlock)) {
            _adjacentBlock = toBlock;
        } else {
            _adjacentBlock = fromBlock;
        }

        if (msg == null) {
            if (_adjacentBlock == null) { // maybe first time
                OBlock block = findAdjacentBlock(icon);
                if (log.isDebugEnabled()) {
                    log.debug("checkPortalIconForUpdate({}): _homeBlock \"{}\" _adjacentBlock= \"{}\" findAdjacentBlock= \"{}\"",
                            moved, _homeBlock.getDisplayName(), 
                            (_adjacentBlock==null?"null":_adjacentBlock.getDisplayName()),
                                    (block==null?"null":block.getDisplayName()));
                }
                if (block != null) {
                    boolean valid;
                    if (_homeBlock.equals(fromBlock)) {
                        valid = portal.setToBlock(block, true);
                    } else {
                        valid = portal.setFromBlock(block, true);
                    }
                    log.debug("Adjacent block change of null to {} is {} valid.",
                            block.getDisplayName(), (valid?"":"NOT"));
                    _adjacentBlock = block;
                } else {
                    msg = Bundle.getMessage("iconNotOnAdjacent", icon.getNameString(), _homeBlock.getDisplayName());
                }
            } else {
                if (moved) {
                    OBlock block = findAdjacentBlock(icon);
                    if (log.isDebugEnabled()) {
                        log.debug("checkPortalIconForUpdate({}): _homeBlock= \"{}\" _adjacentBlock= \"{}\" findAdjacentBlock= \"{}\"",
                                moved, _homeBlock.getDisplayName(), 
                                (_adjacentBlock==null?"null":_adjacentBlock.getDisplayName()),
                                        (block==null?"null":block.getDisplayName()));
                    }
                    if ((block != null && !block.equals(_adjacentBlock)) || (block == null && _adjacentBlock != null)) {
                         {
                            int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("repositionPortal",
                                    name, _homeBlock.getDisplayName(), (block ==null?"null":block.getDisplayName())),
                                    Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
                            if (result == JOptionPane.YES_OPTION) {
                                boolean valid;
                                if (_homeBlock.equals(fromBlock)) {
                                    valid = portal.setToBlock(block, (block!=null));
                                } else {
                                    valid = portal.setFromBlock(block, (block!=null));
                                }
                                log.debug("Adjacent block change of {} to {} is {} valid.",
                                        _adjacentBlock.getDisplayName(), (block==null?"null":block.getDisplayName()), (valid?"":"NOT"));
                                _adjacentBlock = block;
                            }
                        }
                    }
                } else {
                    msg = iconIntersectsBlock(icon, _adjacentBlock);
                    if (log.isDebugEnabled()) {
                        log.debug("checkPortalIconForUpdate: Icon {} on _adjacentBlock \"{}\". {}",
                                (msg==null?"is":"not"), _adjacentBlock.getDisplayName(), (msg==null?"":msg));
                    }
                }
            }
        }
        setSelected(icon);
        return msg;
    }

    /*
     * If icon is on the home block, find another intersecting block.
     */
    private OBlock findAdjacentBlock(PortalIcon icon) {
        ArrayList<OBlock> neighbors = new ArrayList<>();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        SortedSet<OBlock> oblocks = manager.getNamedBeanSet();
        for (OBlock block : oblocks) {
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
            block = _adjacentBlock;
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
            log.debug("findAdjacentBlock: neighbors.size()= {} return {}",
                    neighbors.size(), (block == null ? "null" : block.getDisplayName()));
        }
        return block;
    }

    /**
     * Query whether icon intersects any track icons of block.
     *
     * @return null if intersection, otherwise a messages
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

        /** {@inheritDoc} */
        @Override
        protected boolean okToDrag() {
            String name = _portalName.getText();
            if (name == null || name.trim().length() == 0) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("needPortalName"),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            PortalManager portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);
            Portal p = portalMgr.getByUserName(name);
            if (p != null && !(_homeBlock.equals(p.getFromBlock()) || _homeBlock.equals(p.getToBlock()))) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("portalExists", name, p.getFromBlockName(), p.getToBlockName()),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            PortalIcon pi = _parent.getPortalIconMap().get(name);
            if (name.equals(_currentPortalName) || pi != null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("portalIconExists", name),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                _parent._editor.highlight(pi);
                return false;
            }
            return true;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (DataFlavor.stringFlavor.equals(flavor)) {
                return null;
            }
            String name = _portalName.getText();
            if (name == null || name.trim().length() == 0) {
                log.warn(Bundle.getMessage("needPortalName"),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return null;
            }
            PortalManager portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);
            Portal p = portalMgr.getByUserName(name);
            if (p != null && !(_homeBlock.equals(p.getFromBlock()) || _homeBlock.equals(p.getToBlock()))) {
                log.warn(Bundle.getMessage("portalExists", name, p.getFromBlockName(), p.getToBlockName()),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return null;
            }
            PortalIcon pi = _parent.getPortalIconMap().get(name);
            if (name.equals(_currentPortalName) || pi != null) {
                log.warn(Bundle.getMessage("portalIconExists", name),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
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
            setSelected(pi);
            return pi;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditPortalFrame.class);

}
