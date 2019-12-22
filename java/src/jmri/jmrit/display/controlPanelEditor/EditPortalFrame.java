package jmri.jmrit.display.controlPanelEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

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
public class EditPortalFrame extends EditFrame implements ListSelectionListener {

    private OBlock _adjacentBlock;
    private PortalList _portalList;
    private JTextField _portalName;
    private JPanel _dndPanel;

    /* Ctor for fix a portal error  */
    public EditPortalFrame(String title, CircuitBuilder parent, OBlock block, Portal portal, PortalIcon icon) {
        this(title, parent, block);
        String name = portal.getName();
        _portalName.setText(name);
        _adjacentBlock = portal.getOpposingBlock(block);

        String msg = null;
        if (icon != null) {
            setSelected(icon);
        } else {
            msg = Bundle.getMessage("portalHasNoIcon", name); 
        }
        if (_canEdit) {
            msg = _parent.checkForPortals(block, "BlockPaths");
            if (msg != null) {
                StringBuilder sb = new StringBuilder(msg);
                sb.append("\n");
                sb.append(Bundle.getMessage("portalIconPosition"));
                msg = sb.toString();
            }
        }
        if (_canEdit && msg == null) {
            msg = _parent.checkForPortalIcons(block, "DirectionArrow");
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("incompleteCircuit"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public EditPortalFrame(String title, CircuitBuilder parent, OBlock block) {
        super(title, parent, block);
        pack();
        String msg = _parent.checkForTrackIcons(block, "BlockPortals");
        if (msg != null) {
            _canEdit = false;
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("incompleteCircuit"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    protected JPanel makeContentPanel() {
        JPanel portalPanel = new JPanel();
        portalPanel.setLayout(new BoxLayout(portalPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("PortalTitle", _homeBlock.getDisplayName())));
        portalPanel.add(panel);
        _portalName = new JTextField();
        _portalList = new PortalList(_homeBlock, this);
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
        _portalName.setToolTipText(Bundle.getMessage("TooltipPortalName", _homeBlock.getDisplayName()));
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
        portalPanel.add(makeDoneButtonPanel());
        return portalPanel;
    }

    @Override
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
            List<PortalIcon> piArray = _parent.getPortalIconMap(portal);
            if (piArray.isEmpty()) {
                _parent._editor.highlight(null);
            } else {
                PortalIcon icon = piArray.get(0);
                icon.setStatus(PortalIcon.VISIBLE);
                _parent._editor.highlight(icon);
            }
        } else {
            _portalName.setText(null);
        }
    }

    protected void setSelected(PortalIcon icon) {
        if (!canEdit()) {
            return;
        }
        Portal portal = icon.getPortal();
        _portalList.setSelectedValue(portal, true);
        _parent._editor.highlight(icon);
        
    }

    /*
     * *********************** end setup *************************
     */

    private void changePortalName() {
        Portal portal = _portalList.getSelectedValue();
        String name = _portalName.getText();
        if (portal == null || name == null || name.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("changePortalName", Bundle.getMessage("buttonChangeName")),
                    Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String msg = portal.setName(name);
        if (msg == null) {
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
            portal = portalMgr.getPortal(name);
        }
        if (portal == null) {
            return;
        }        
        if (!_suppressWarnings) {
            int val = JOptionPane.showOptionDialog(this, Bundle.getMessage("confirmPortalDelete", portal.getName()),
                    Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                            Bundle.getMessage("ButtonYesPlus"),
                            Bundle.getMessage("ButtonNo"),},
                    Bundle.getMessage("ButtonNo")); // default NO
            if (val == 2) {
                return;
            }
            if (val == 1) { // suppress future warnings
                _suppressWarnings = true;
            }
        }
        if (portal.dispose()) {
            _portalList.dataChange();
            _portalName.setText(null);
            OBlock oppBlock = portal.getOpposingBlock(_homeBlock);
            ArrayList<PortalIcon> removeList = new ArrayList<>(_parent.getPortalIconMap(portal));
            for (PortalIcon icon : removeList) {
                _parent.getCircuitIcons(oppBlock).remove(icon);
                icon.remove();  // will call _parent.deletePortalIcon(icon)
            }
        }
    }

    @Override
    protected void closingEvent(boolean close) {
        String msg = _parent.checkForPortals(_homeBlock, "BlockPaths");
        if (_canEdit && msg == null) {
            msg = _parent.checkForPortalIcons(_homeBlock, "BlockPaths");
        }
        closingEvent(close, msg);
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
            _parent.deletePortalIcon(icon);
            log.error("Removed PortalIcon without Portal");
            return null;
        }
        String name = portal.getName();
        String msg = null;
        OBlock fromBlock = portal.getFromBlock();
        OBlock toBlock = portal.getToBlock();
        if (!_homeBlock.equals(fromBlock) && !_homeBlock.equals(toBlock)) {
            _adjacentBlock = null;
           return showIntersectMessage(null, icon, moved); 
        }

        if (_homeBlock.equals(fromBlock)) {
            _adjacentBlock = toBlock;
        } else {
            _adjacentBlock = fromBlock;
        }

        OBlock block = findAdjacentBlock(icon);
        if (log.isDebugEnabled()) {
            log.debug("checkPortalIconForUpdate({}): _homeBlock= \"{}\" _adjacentBlock= \"{}\" findAdjacentBlock= \"{}\"",
                    moved, _homeBlock.getDisplayName(),  (_adjacentBlock==null?"null":_adjacentBlock.getDisplayName()),
                            (block==null?"null":block.getDisplayName()));
        }
        if (_adjacentBlock == null) { // maybe first time
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
            }
        } else {
            if (moved && block != null) {
                if (!block.equals(_adjacentBlock)) {
                    int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("repositionPortal",
                            name, _homeBlock.getDisplayName(), block.getDisplayName()),
                            Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        boolean valid;
                        if (_homeBlock.equals(fromBlock)) {
                            valid = portal.setToBlock(block, true);
                        } else {
                            valid = portal.setFromBlock(block, true);
                        }
                        log.debug("Adjacent block change of {} to {} is {} valid.",
                                _adjacentBlock.getDisplayName(), block.getDisplayName(), (valid?"":"NOT"));
                        _adjacentBlock = block;
                    }
                }
            }
        }

        List<PortalIcon> piArray = _parent.getPortalIconMap(portal);
        boolean checkHomeBlock = true;
        if (piArray.size() > 0) {
            if (_parent.iconIntersectsBlock(icon, _homeBlock)) {
                block = _adjacentBlock;
            } else {
                block = _homeBlock;
            }
            for (PortalIcon ic : piArray) {
                if (!ic.equals(icon) && _parent.iconIntersectsBlock(ic, block)) {
                    if (_parent.iconIntersectsBlock(ic, fromBlock) && _parent.iconIntersectsBlock(ic, toBlock)) {
                        _parent.deletePortalIcon(icon);
//                       _parent._editor.highlight(ic);
                       return Bundle.getMessage("duplicatePortalIcon", name);
                    }
                    // ic is a companion icon for non-contiguous blocks and it covers _homeBlock
                    checkHomeBlock = false; 
                }
            }
        }
        
        if (checkHomeBlock && !_parent.iconIntersectsBlock(icon, _homeBlock)) {
            return showIntersectMessage(_homeBlock, icon, moved); 
        }

        setSelected(icon);
        return msg;
    }

    private String showIntersectMessage(OBlock block, PortalIcon icon, boolean moved) {
        String msg = null;
        if (block == null) {
            msg = Bundle.getMessage("iconNotOnBlock", _homeBlock.getDisplayName(), icon.getNameString());
        } else {
            List<Positionable> list = _parent.getCircuitIcons(block);
            if (list.isEmpty()) {
                msg = Bundle.getMessage("needIcons", block.getDisplayName(), Bundle.getMessage("BlockPortals"));
            } else {
                msg = Bundle.getMessage("iconNotOnBlock", block.getDisplayName(), icon.getNameString());
            }
        }
        if (moved) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
        }
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
            if (_parent.iconIntersectsBlock(icon, block)) {
                neighbors.add(block);
            }
        }
        OBlock block = null;
        if (neighbors.size() == 1) {
            block = neighbors.get(0);
        } else if (neighbors.size() > 1) {
            // show list
            block = neighbors.get(0);
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
                        break;
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

        boolean addSecondIcon = false;

        public IconDragJLabel(DataFlavor flavor) {
            super(flavor);
        }

        @Override
        protected boolean okToDrag() {
            String name = _portalName.getText();
            if (name == null || name.trim().length() == 0) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("needPortalName"),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            PortalManager portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);
            Portal portal = portalMgr.getPortal(name);
            if (portal == null) {
                return true;
            }
            OBlock toBlock = portal.getToBlock();
            OBlock fromBlock = portal.getFromBlock();
            if (!_homeBlock.equals(fromBlock) && !_homeBlock.equals(toBlock)) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("portalNeedsBlock", name, fromBlock, toBlock),
                        Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            List<PortalIcon> piArray = _parent.getPortalIconMap(portal);
            for (PortalIcon pi : piArray) {
                _parent._editor.highlight(pi);
            }
            switch (piArray.size()) {
                case 0:
                    return true;
                case 1:
                    PortalIcon i = piArray.get(0);
                    if (_parent.iconIntersectsBlock(i, toBlock) && _parent.iconIntersectsBlock(i,fromBlock)) {
                        JOptionPane.showMessageDialog(this, Bundle.getMessage("portalIconExists", name),
                                Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                        return false;
                    }
                    if (addSecondIcon) {
                        return true;
                    }
                    int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("portalWant2Icons", name),
                            Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        addSecondIcon = true;
                        return true;
                    }
                    break;
                default:
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("portalIconExists", name),
                            Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            }
            return false;
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
            Portal portal = _homeBlock.getPortalByName(name);
            if (portal == null) {
                PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
                portal = portalMgr.createNewPortal(name);
                portal.setFromBlock(_homeBlock, false);
                _portalList.dataChange();
            }
            addSecondIcon = false;
            PortalIcon icon = new PortalIcon(_parent._editor, portal);
            ArrayList<Positionable> group = _parent.getCircuitIcons(_homeBlock);
            group.add(icon);
            _parent.getPortalIconMap(portal).add(icon);
            _parent._editor.setSelectionGroup(group);
            icon.setLevel(Editor.MARKERS);
            icon.setStatus(PortalIcon.VISIBLE);
//            setSelected(icon);
            return icon;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditPortalFrame.class);
}
