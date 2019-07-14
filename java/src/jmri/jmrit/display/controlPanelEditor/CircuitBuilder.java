package jmri.jmrit.display.controlPanelEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Editor.TargetPane;
import jmri.jmrit.display.IndicatorTrack;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.TurnoutIcon;
import jmri.jmrit.display.palette.FamilyItemPanel;
import jmri.jmrit.display.palette.IndicatorItemPanel;
import jmri.jmrit.display.palette.IndicatorTOItemPanel;
import jmri.jmrit.display.palette.ItemPanel;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.jmrit.logix.WarrantTableAction;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ControlPanelEditor CircuitBuilder tools.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2011
 */
public class CircuitBuilder {

    static int STRUT_SIZE = 10;

    private JMenu _circuitMenu;
    private JMenu _todoMenu;   // error checking items

    // map track icon to OBlock to which it belongs
    private final HashMap<Positionable, OBlock> _iconMap = new HashMap<>();

    // map OBlock to List of icons that represent it
    private HashMap<OBlock, ArrayList<Positionable>> _circuitMap;

    // list of track icons not belonging to an OBlock
    private final ArrayList<Positionable> _darkTrack = new ArrayList<>();

    // list of OBlocks with no track icons
    private final ArrayList<OBlock> _bareBlock = new ArrayList<>();

    // list of circuit icons needing converting
    private final ArrayList<Positionable> _unconvertedTrack = new ArrayList<>();

    // list of OBlocks whose track icons need converting
    private final ArrayList<OBlock> _convertBlock = new ArrayList<>();

    // list of Portals with no PortalIcon
    private final ArrayList<Portal> _noPortalIcon = new ArrayList<>();

    // list of misplaced PortalIcons
    private final ArrayList<PortalIcon> _misplacedIcon = new ArrayList<>();

    // map of PortalIcons by portal name
    private final HashMap<String, PortalIcon> _portalIconMap = new HashMap<>();
    
    private boolean _hasIndicatorTrackIcons;
    private boolean _hasPortalIcons;

    // OBlock list to open edit frames
    private PickListModel<OBlock> _oblockModel;

    // "Editing Frames" - Called from menu in Main Frame
    private EditCircuitFrame _editCircuitFrame;
    private EditPortalFrame _editPortalFrame;
    private EditCircuitPaths _editPathsFrame;
    private EditPortalDirection _editDirectionFrame;

    // list of icons making a circuit (OBlock) - used by editing frames to indicate block(s) being worked on
    private ArrayList<Positionable> _circuitIcons;      // Dark Blue

    private final JTextField _sysNameBox = new JTextField();
    private final JTextField _userNameBox = new JTextField();
    private OBlock _currentBlock;
    private int _origStateCurBlk = OBlock.UNOCCUPIED;
    private JDialog _dialog;
    protected ControlPanelEditor _editor;

    public final static Color _editGroupColor = new Color(100, 200, 255);
    public final static Color _pathColor = Color.green;
    public final static Color _highlightColor = new Color(255, 150, 220);

    /**
     * ***************************************************************
     */
    public CircuitBuilder() {
        log.error("CircuitBuilder ctor requires an Editor class");
    }

    public CircuitBuilder(ControlPanelEditor ed) {
        _editor = ed;
    }

    /**
     * Makes menu for ControlPanelEditor. Called by ControlPanelEditor at init
     * before contents have been loaded.
     *
     * @return the menu, created if needed
     */
    protected JMenu makeMenu() {
        _circuitMenu = new JMenu(Bundle.getMessage("CircuitBuilder"));
        _circuitMap = new HashMap<>();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        SortedSet<OBlock> oblocks = manager.getNamedBeanSet();
        for (OBlock block : oblocks) {
            _circuitMap.put(block, new ArrayList<>());
        }
        checkCircuits();
        return _circuitMenu;
    }

    private void makeNoOBlockMenu() {
        JMenuItem circuitItem = new JMenuItem(Bundle.getMessage("newCircuitItem"));
        _circuitMenu.add(circuitItem);
        circuitItem.addActionListener((ActionEvent event) -> {
            newCircuit();
        });
        _circuitMenu.add(new JMenuItem(Bundle.getMessage("noCircuitsItem")));

    }
    private void makeCircuitMenu() {
        JMenuItem editItem = new JMenuItem(Bundle.getMessage("newCircuitItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            newCircuit();
        });
        editItem = new JMenuItem(Bundle.getMessage("editCircuitItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            editCircuit("editCircuitItem");
        });
        editItem = new JMenuItem(Bundle.getMessage("editPortalsItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            editPortals("editPortalsItem");
        });
        editItem = new JMenuItem(Bundle.getMessage("editCircuitPathsItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            editCircuitPaths("editCircuitPathsItem");
        });
        editItem = new JMenuItem(Bundle.getMessage("editDirectionItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            editPortalDirection("editDirectionItem");
        });
        _todoMenu = new JMenu(Bundle.getMessage("circuitErrorsItem"));
        _circuitMenu.add(_todoMenu);
        makeToDoMenu();
    }

    /**
     * Add icon 'pos' to circuit 'block'
     */
    private void addIcon(OBlock block, Positionable pos) {
        ArrayList<Positionable> icons = _circuitMap.get(block);
        if (icons == null) {
            icons = new ArrayList<>();
        }
        if (pos != null) {
            if (!icons.contains(pos)) {
                icons.add(pos);
            }
            _iconMap.put(pos, block);
        }
        _circuitMap.put(block, icons);
        _darkTrack.remove(pos);
        // if (log.isDebugEnabled()) log.debug("addIcon: block "+block.getDisplayName()+" has "+icons.size()+" icons.");
    }

    // display "todo" (Error correction) items
    // Rebuild after any edit change
    private void makeToDoMenu() {
        _todoMenu.removeAll();

        JMenu blockNeeds = new JMenu(Bundle.getMessage("blockNeedsIconsItem"));
        ActionListener editCircuitAction = (ActionEvent event) -> {
            String sysName = event.getActionCommand();
            editCircuitError(sysName);
        };
        if (_bareBlock.size() > 0) {
            for (int i = 0; i < _bareBlock.size(); i++) {
                OBlock block = _bareBlock.get(i);
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        Bundle.getMessage("OpenCircuitItem"), block.getDisplayName()));
                mi.setActionCommand(block.getSystemName());
                mi.addActionListener(editCircuitAction);
                blockNeeds.add(mi);
            }
        } else {
            blockNeeds.add(new JMenuItem(Bundle.getMessage("circuitsHaveIcons")));
        }
        _todoMenu.add(blockNeeds);  // #1

        blockNeeds = new JMenu(Bundle.getMessage("blocksNeedConversionItem"));
        if (_convertBlock.size() > 0) {
            for (int i = 0; i < _convertBlock.size(); i++) {
                OBlock block = _convertBlock.get(i);
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        Bundle.getMessage("OpenCircuitItem"), block.getDisplayName()));
                mi.setActionCommand(block.getSystemName());
                mi.addActionListener(editCircuitAction);
                blockNeeds.add(mi);
            }
        } else {
            blockNeeds.add(new JMenuItem(Bundle.getMessage("circuitIconsConverted")));
        }
        _todoMenu.add(blockNeeds);  // #2
        
        JMenuItem iconNeeds = new JMenuItem(Bundle.getMessage("iconsNeedConversionItem"));
        if (_unconvertedTrack.size() > 0) {
            iconNeeds.addActionListener((ActionEvent event) -> {
                if (editingOK()) {
                    hidePortalIcons();
                    ArrayList<Positionable> group = new ArrayList<>();
                    for (int i = 0; i < _unconvertedTrack.size(); i++) {
                        group.add(_unconvertedTrack.get(i));
                    }
                    _editor.setSelectionGroup(group);
                }
            });
        } else {
            iconNeeds = new JMenuItem(Bundle.getMessage("noneNeedConversion"));
        }
        _todoMenu.add(iconNeeds);   // #3

        iconNeeds = new JMenuItem(Bundle.getMessage("iconsNeedsBlocksItem"));
        if (_darkTrack.size() > 0) {
            iconNeeds.addActionListener((ActionEvent event) -> {
                if (editingOK()) {
                    hidePortalIcons();
                    ArrayList<Positionable> group = new ArrayList<>();
                    for (int i = 0; i < _darkTrack.size(); i++) {
                        group.add(_darkTrack.get(i));
                    }
                    _editor.setSelectionGroup(group);
                }
            });
        } else {
            if (_hasIndicatorTrackIcons) {
                iconNeeds = new JMenuItem(Bundle.getMessage("IconsHaveCircuits"));
            } else {
                iconNeeds = new JMenuItem(Bundle.getMessage("noIndicatorTrackIcon"));
            }
        }
        _todoMenu.add(iconNeeds);   // #4

        blockNeeds = new JMenu(Bundle.getMessage("portalsMisplaced"));
        ActionListener editPortalAction = (ActionEvent event) -> {
            String portalName = event.getActionCommand();
            editPortalError(portalName);
        };
        if (_misplacedIcon.size() > 0) {
            Iterator<PortalIcon> iter = _misplacedIcon.iterator();
            while (iter.hasNext()) {
                Portal portal = iter.next().getPortal();
                OBlock block = portal.getFromBlock();
                if (block == null) {
                    block = portal.getToBlock();
                }
                JMenuItem mi = new JMenuItem(block.getDisplayName());
                mi.setActionCommand(portal.getSystemName());
                mi.addActionListener(editPortalAction);
                blockNeeds.add(mi);
            }
        } else {
            if (_hasPortalIcons) {
                blockNeeds.add(new JMenuItem(Bundle.getMessage("portalsInPlace")));
            } else {
                blockNeeds.add(new JMenuItem(Bundle.getMessage("NoPortalIcons")));
            }
        }
        _todoMenu.add(blockNeeds);  //#5

        iconNeeds = new JMenuItem(Bundle.getMessage("iconsNeedPositioning"));
        if (_misplacedIcon.size() > 0) {
            iconNeeds.addActionListener((ActionEvent event) -> {
                if (editingOK()) {
                    ArrayList<Positionable> group = new ArrayList<>();
                    for (int i = 0; i < _misplacedIcon.size(); i++) {
                        PortalIcon pi = _misplacedIcon.get(i);
                        group.add(pi);
                        pi.setStatus(PortalIcon.VISIBLE);
                    }
                    _editor.setSelectionGroup(group);
                }
            });
        } else {
            if (_hasPortalIcons) {
                iconNeeds = new JMenuItem(Bundle.getMessage("portalsInPlace"));
            } else {
                iconNeeds = new JMenuItem(Bundle.getMessage("NoPortalIcons"));
            }
        }
        _todoMenu.add(iconNeeds);   // #6

        blockNeeds = new JMenu(Bundle.getMessage("portalNeedsIcon"));
        if (_noPortalIcon.size() > 0) {
            for (int i = 0; i < _noPortalIcon.size(); i++) {
                Portal portal = _noPortalIcon.get(i);
                JMenuItem mi = new JMenuItem(portal.toString());
                mi.setActionCommand(portal.getSystemName());
                mi.addActionListener(editPortalAction);
                blockNeeds.add(mi);
            }
        } else {
            blockNeeds.add(new JMenuItem(Bundle.getMessage("portalsHaveIcons")));
        }
        _todoMenu.add(blockNeeds);

        JMenuItem pError = new JMenuItem(Bundle.getMessage("CheckPortalPaths"));
        _todoMenu.add(pError);
        pError.addActionListener((ActionEvent event) -> {
            errorCheck();
        });

    }

    private void errorCheck() {
        WarrantTableAction.initPathPortalCheck();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        SortedSet<OBlock> oblocks = manager.getNamedBeanSet();
        for (OBlock block : oblocks) {
            WarrantTableAction.checkPathPortals(block);
        }
        if (!WarrantTableAction.showPathPortalErrors()) {
            JOptionPane.showMessageDialog(_editCircuitFrame,
                    Bundle.getMessage("blocksEtcOK"), Bundle.getMessage("ButtonOK"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * ************** Set up editing Frames ****************
     */
    protected void newCircuit() {
        if (editingOK()) {
            addCircuitDialog();
            if (_currentBlock != null) {
                if (_editCircuitFrame == null) {
                    _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, false));
                    _editor.disableMenus();
                    TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                    targetPane.setSelectGroupColor(_editGroupColor);
                    targetPane.setHighlightColor(_editGroupColor);
                    _editCircuitFrame = new EditCircuitFrame(Bundle.getMessage("newCircuitItem"), this, _currentBlock);
                }
            }
        }
    }

    protected void editCircuit(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock != null) {
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, false));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_editGroupColor);
                _editCircuitFrame = new EditCircuitFrame(Bundle.getMessage("OpenCircuitItem"), this, _currentBlock);
            }
        }
    }

    private void editCircuitError(String sysName) {
        hidePortalIcons();
        if (editingOK()) {
            _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBySystemName(sysName);
            if (_currentBlock != null) {
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, false));
                _editor.disableMenus();
                _editCircuitFrame = new EditCircuitFrame(Bundle.getMessage("OpenCircuitItem"), this, _currentBlock);
            }
        }
    }

    protected void editPortals(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock != null) {
                _circuitIcons = _circuitMap.get(_currentBlock);
                // check icons to be indicator type
                if (!iconsConverted(_currentBlock)) {
                    queryConvertIcons(_currentBlock);
                }
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_highlightColor);
                _editPortalFrame = new EditPortalFrame(Bundle.getMessage("OpenPortalTitle"), this, _currentBlock);
            }

        }
    }
    private void editPortalError(String sysName) {
        if (editingOK()) {
            Portal portal = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class).getBySystemName(sysName);
            OBlock homeBlock = portal.getFromBlock();
            OBlock adjacentBlock = null;
            if (homeBlock == null) {
                homeBlock = portal.getToBlock();
            } else {
                adjacentBlock = portal.getToBlock();
            }
            _currentBlock = homeBlock;
            if (_currentBlock != null) {
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                _circuitIcons = _circuitMap.get(_currentBlock);
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_highlightColor);
                _editPortalFrame = new EditPortalFrame(Bundle.getMessage("OpenPortalTitle"), this,
                        _currentBlock, portal, adjacentBlock);
            } else {
                portal.dispose();
            }
        }
    }

    protected void editPortalDirection(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock != null) {
                _circuitIcons = _circuitMap.get(_currentBlock);
                // check icons to be indicator type
                if (!iconsConverted(_currentBlock)) {
                    queryConvertIcons(_currentBlock);
                }
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_highlightColor);
                setPortalsPositionable(_currentBlock, true);
                _editDirectionFrame = new EditPortalDirection(Bundle.getMessage("OpenDirectionTitle"), this, _currentBlock);
            }

        }
    }

    protected void editCircuitPaths(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock != null) {
                // check icons to be indicator type
                _circuitIcons = _circuitMap.get(_currentBlock);
                // must have converted icons for paths
                if (!iconsConverted(_currentBlock)) {
                    JOptionPane.showMessageDialog(_editor,
                            Bundle.getMessage("needConversion", _currentBlock.getDisplayName(), Bundle.getMessage("ButtonEdit"), Bundle.getMessage("EditTrackSegment")),
                            Bundle.getMessage("noIcons"), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                    // A temporary path "TEST_PATH" is used to display the icons representing a path
                    _currentBlock.allocate(EditCircuitPaths.TEST_PATH);
                    _editor.disableMenus();
                    TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                    targetPane.setSelectGroupColor(_editGroupColor);
                    targetPane.setHighlightColor(_editGroupColor);
                    _origStateCurBlk = _currentBlock.getState();
                    _currentBlock.setState(OBlock.UNOCCUPIED);
                    _editPathsFrame = new EditCircuitPaths(Bundle.getMessage("OpenPathTitle"), this, _currentBlock);
                }
            }
        }
    }

    protected void hidePortalIcons() {
        if (_editPortalFrame != null) {
            _editPortalFrame.clearListSelection();
        } else if (_editPathsFrame != null) {
            _editPathsFrame.clearListSelection();
        } else if (_editDirectionFrame != null) {
            _editDirectionFrame.clearListSelection();
        } else {
            Iterator<PortalIcon> it = _portalIconMap.values().iterator();
            while (it.hasNext()) {
                it.next().setStatus(PortalIcon.HIDDEN);
            }
        }
    }

    private boolean editingOK() {
        if (_editCircuitFrame != null || _editPathsFrame != null || _editPortalFrame != null || _editDirectionFrame != null) {
            // Already editing a circuit, ask for completion of that edit
            JOptionPane.showMessageDialog(_editCircuitFrame,
                    Bundle.getMessage("AlreadyEditing"), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            if (_editPathsFrame != null) {
                _editPathsFrame.toFront();
                _editPathsFrame.setVisible(true);
            } else if (_editCircuitFrame != null) {
                _editCircuitFrame.toFront();
                _editCircuitFrame.setVisible(true);
            } else if (_editPortalFrame != null) {
                _editPortalFrame.toFront();
                _editPortalFrame.setVisible(true);
            } else if (_editDirectionFrame != null) {
                _editDirectionFrame.toFront();
                _editDirectionFrame.setVisible(true);
            }
            return false;
        }
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        if (manager.getObjectCount() == 0) {
            return true;
        } else {
            for (OBlock block : manager.getNamedBeanSet()) {
                if ((block.getState() & OBlock.ALLOCATED) != 0) {
                    JOptionPane.showMessageDialog(_editor, Bundle.getMessage("cannotEditCB", block.getWarrant().getDisplayName()),
                            Bundle.getMessage("editCiruit"), JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
            }
        }
        checkCircuits();
        return true;
    }

    /**
     * Create a new OBlock. Used by New to set up _editCircuitFrame.
     * Sets _currentBlock to created new OBlock.
     */
    private void addCircuitDialog() {
        _dialog = new JDialog(_editor, Bundle.getMessage("TitleCircuitDialog"), true);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel p = new JPanel();
        p.add(new JLabel(Bundle.getMessage("createOBlock")));
        mainPanel.add(p);

        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        mainPanel.add(makeSystemNamePanel());
        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        mainPanel.add(makeDoneButtonPanel(true));
        panel.add(mainPanel);
        _dialog.getContentPane().add(panel);
        _dialog.setLocation(_editor.getLocation().x + 100, _editor.getLocation().y + 100);
        _dialog.pack();
        _dialog.setVisible(true);
    }

    /**
     * Edit existing OBlock. Used by edit to set up _editCircuitFrame.
     * Sets _currentBlock to chosen OBlock or null if none selected.
     */
    private void editCircuitDialog(String title) {
        _dialog = new JDialog(_editor, Bundle.getMessage(title), true);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel p = new JPanel();
        p.add(new JLabel(Bundle.getMessage("selectOBlock")));
        p.setMaximumSize(new Dimension(300, p.getPreferredSize().height));
        mainPanel.add(p);

        _oblockModel = PickListModel.oBlockPickModelInstance();
        JTable table = _oblockModel.makePickTable();
        mainPanel.add(new JScrollPane(table));
        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        mainPanel.add(makeDoneButtonPanel(false));
        panel.add(mainPanel);
        _dialog.getContentPane().add(panel);
        _dialog.setLocation(_editor.getLocation().x + 100, _editor.getLocation().y + 100);
        _dialog.pack();
        _dialog.setVisible(true);
    }

    private JPanel makeSystemNamePanel() {
        _sysNameBox.setText("");
        _userNameBox.setText("");
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(new JLabel(Bundle.getMessage("ColumnSystemName")), c);
        c.gridy = 1;
        p.add(new JLabel(Bundle.getMessage("ColumnUserName")), c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_sysNameBox, c);
        c.gridy = 1;
        p.add(_userNameBox, c);
        namePanel.add(p);
        return namePanel;
    }

    private JPanel makeDoneButtonPanel(boolean add) {
        JPanel buttonPanel = new JPanel();
        JPanel panel0 = new JPanel();
        panel0.setLayout(new FlowLayout());
        JButton doneButton;
        if (add) {
            doneButton = new JButton(Bundle.getMessage("ButtonAddCircuit"));
            doneButton.addActionListener((ActionEvent a) -> {
                if (doAddAction()) {
                    _dialog.dispose();
                }
            });
        } else {
            doneButton = new JButton(Bundle.getMessage("ButtonOpenCircuit"));
            doneButton.addActionListener((ActionEvent a) -> {
                if (doOpenAction()) {
                    _dialog.dispose();
                }
            });
        }
        panel0.add(doneButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener((ActionEvent a) -> {
            _sysNameBox.setText("");
            _currentBlock = null;
            _dialog.dispose();
        });
        panel0.add(cancelButton);
        buttonPanel.add(panel0);
        buttonPanel.setMaximumSize(new Dimension(300, buttonPanel.getPreferredSize().height));
        return buttonPanel;
    }

    private boolean doAddAction() {
        boolean retOK = false;
        String sysname = _sysNameBox.getText();
        if (sysname != null && sysname.length() > 1) {
            String uname = _userNameBox.getText();
            if (uname != null && uname.trim().length() == 0) {
                uname = null;
            }
            _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).createNewOBlock(sysname, uname);
            if (_currentBlock != null) {
                _circuitMap.put(_currentBlock, new ArrayList<>());
                if (jmri.InstanceManager.getDefault(OBlockManager.class).getNamedBeanSet().size() == 2) {
                    _editor.makeWarrantMenu(true, false);
                }
               retOK = true;
            } else {
                int result = JOptionPane.showConfirmDialog(_editor, java.text.MessageFormat.format(
                        Bundle.getMessage("blockExists"), sysname, uname),
                        Bundle.getMessage("AskTitle"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getByUserName(uname);
                    if (_currentBlock == null) {
                        _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBySystemName(sysname);
                    }
                    if (_currentBlock != null) {
                        retOK = true;
                    }
                }
            }
        }
        if (!retOK) {
            JOptionPane.showMessageDialog(_editor, Bundle.getMessage("sysnameOBlock"),
                    Bundle.getMessage("NeedDataTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
        return retOK;
    }

    private boolean doOpenAction() {
        int row = _oblockModel.getTable().getSelectedRow();
        if (row >= 0) {
            row = _oblockModel.getTable().convertRowIndexToModel(row);
            _currentBlock = _oblockModel.getBeanAt(row);
            return true;
        }
        JOptionPane.showMessageDialog(_editor, Bundle.getMessage("selectOBlock"),
                Bundle.getMessage("NeedDataTitle"), JOptionPane.INFORMATION_MESSAGE);
        _currentBlock = null;
        return false;
    }

    /*
     * ************************ end setup frames *****************************
     */
    private void setPortalsPositionable(OBlock block, boolean set) {
        List<Positionable> circuitIcons = _circuitMap.get(block);
        Iterator<Positionable> iter = circuitIcons.iterator();
        while (iter.hasNext()) {
            Positionable p = iter.next();
            if (p instanceof PortalIcon) {
                p.setPositionable(set);
            }
        }
    }

    ////////////////////////// Closing Editing Frames //////////////////////////
    protected void closeCircuitFrame() {
        _editCircuitFrame = null;
        closeCircuitBuilder();
    }

    /**
     * Edit frame closing, set block's icons
     * @param block OBlock to set icon selections into data maps
     * @return error message, if any
     */
    protected String setIconGroup(OBlock block) {
        java.util.List<Positionable> selections = _editor.getSelectionGroup();
        java.util.List<Positionable> oldIcons = _circuitMap.get(block);
        if (oldIcons != null) {
            for (int i = 0; i < oldIcons.size(); i++) {
                Positionable pos = oldIcons.get(i);
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack) pos).setOccBlockHandle(null);
                }
                _iconMap.remove(pos);
            }
        }
        // the selectionGroup for all edit frames is full collection of icons
        // comprising the block.  Gather them and store in the block's hashMap
        ArrayList<Positionable> icons = new ArrayList<>();
        if (selections == null || selections.size() == 0) {
            _circuitMap.put(block, icons);
            return Bundle.getMessage("needIcons", block.getDisplayName());
        }
        NamedBeanHandle<OBlock> handle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(block.getSystemName(), block);
        for (int i = 0; i < selections.size(); i++) {
            Positionable pos = selections.get(i);
            if (pos instanceof IndicatorTrack) {
                ((IndicatorTrack) pos).setOccBlockHandle(handle);
            }
            icons.add(pos);
            _iconMap.put(pos, block);
        }

        java.util.List<Portal> portals = block.getPortals();
        for (int i = 0; i < portals.size(); i++) {
            PortalIcon icon = _portalIconMap.get(portals.get(i).getName());
            if (icon != null) {
                _iconMap.put(icon, block);
            }
        }
        _circuitMap.put(block, icons);
        if (log.isDebugEnabled()) {
            log.debug("setIconGroup: block \"{}\" has {} icons.", block.getDisplayName(), icons.size());
        }
        return null;
    }

    protected void closePathFrame(OBlock block) {
        if (_currentBlock != null) {
            _currentBlock.deAllocate(null);
            _currentBlock.setState(_origStateCurBlk);
        }
        _editPathsFrame = null;
        closeCircuitBuilder();
    }

    protected void closePortalFrame(OBlock block) {
        setPortalsPositionable(block, false);
        _editPortalFrame = null;
        _editor.setSecondSelectionGroup(null);
        closeCircuitBuilder();
    }

    protected void closePortalDirection(OBlock block) {
        setPortalsPositionable(block, false);
        _editDirectionFrame = null;
        closeCircuitBuilder();
    }

    private void closeCircuitBuilder() {
        _currentBlock = null;
        _circuitIcons = null;
        checkCircuits();
        hidePortalIcons();
        _editor.resetEditor();
    }

    /*
     * ************** end closing frames *******************
     */

    /**
     * Find the blocks with no icons and the blocks with icons that need
     * conversion Setup for main Frame - used in both initialization and close
     * of an editing frame Build Lists that are used to create menu items
     */
    private void checkCircuits() {

        _portalIconMap.clear();
        _darkTrack.clear();
        _unconvertedTrack.clear();
        _hasIndicatorTrackIcons = false;
        _hasPortalIcons = false;
        PortalManager portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);

        Iterator<Positionable> it = _editor.getContents().iterator();
        ArrayList<PortalIcon> removeList = new ArrayList<>();
        while (it.hasNext()) {
            Positionable pos = it.next();
            // if (log.isDebugEnabled()) log.debug("class: "+pos.getClass().getName());
            if (pos instanceof IndicatorTrack) {
                _hasIndicatorTrackIcons = true;
                OBlock block = ((IndicatorTrack) pos).getOccBlock();
                ((IndicatorTrack) pos).removePath(EditCircuitPaths.TEST_PATH);
                if (block != null) {
                    addIcon(block, pos);
                } else {
                    _darkTrack.add(pos);
                }
            } else if (pos instanceof PortalIcon) {
                _hasPortalIcons = true;
                PortalIcon pIcon = (PortalIcon) pos;
                String name = pIcon.getName();
                Portal portal = portalMgr.getByUserName(name);
                if (portal == null) {
                    log.error("No Portal for PortalIcon called \"{}\". Discarding icon.", name);
                    removeList.add(pIcon);
                } else {
                    PortalIcon pi = _portalIconMap.get(name);
                    if (pi != null) {
                        log.error("Removing duplicate PortalIcon for Portal \"{}\".", name);
                        removeList.add(pi);
                    }
                    if (pIcon.getPortal() == null) {    // no portal for the icon
                        removeList.add(pIcon);                        
                    }
                    _portalIconMap.put(name, pIcon);
                }
            } else if (isUnconvertedTrack(pos)) {
                if (!_unconvertedTrack.contains(pos)) {
                    _unconvertedTrack.add(pos);
                }
            }
        }
        Iterator<PortalIcon> its = removeList.iterator();
        while (its.hasNext()) {
            its.next().remove();
        }
        Iterator<Entry<OBlock, ArrayList<Positionable>>> iters = _circuitMap.entrySet().iterator();
        while (iters.hasNext()) {
            Entry<OBlock, ArrayList<Positionable>> entry = iters.next();
            Iterator<Positionable> iter = entry.getValue().iterator();
            while (iter.hasNext()) {
                Positionable pos = iter.next();
                if (isUnconvertedTrack(pos)) {
                    if (!_unconvertedTrack.contains(pos)) {
                        _unconvertedTrack.add(pos);
                    }
                }
            }
        }
        _bareBlock.clear();
        _convertBlock.clear();
        _misplacedIcon.clear();
        _noPortalIcon.clear();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        SortedSet<OBlock> oblocks = manager.getNamedBeanSet();
        boolean hasOBlocks = (oblocks.size() > 0);
        for (OBlock block : oblocks) {
            java.util.List<Portal> list = block.getPortals();
            Iterator<Portal> iter = list.iterator();
            while (iter.hasNext()) {
                Portal portal = iter.next();
                // update circuitMap
                PortalIcon pi = _portalIconMap.get(portal.getName());
                if (pi != null) {
                    addIcon(block, pi);
                }
            }
            java.util.List<Positionable> icons = _circuitMap.get(block);
            if (log.isDebugEnabled()) {
                log.debug("checkCircuits: block " + block.getDisplayName()
                        + " has " + icons.size() + " icons.");
            }
            if (icons == null || icons.isEmpty()) {
                _bareBlock.add(block);
            } else {
                boolean hasTrackIcon = false;
                boolean iconNeedsConversion = false;
                for (int k = 0; k < icons.size(); k++) {
                    Positionable pos = icons.get(k);
                    if (!(pos instanceof PortalIcon)) {
                        hasTrackIcon = true;
                        if (!(pos instanceof IndicatorTrack)) {
                            iconNeedsConversion = true;                        }
                    }
                }
                if (hasTrackIcon) {
                    _bareBlock.remove(block);
                } else if (!_bareBlock.contains(block)) {
                    _bareBlock.add(block);
                }
                if (iconNeedsConversion && !_convertBlock.contains(block)) {
                    _convertBlock.add(block);
                }
            }
        }
        Set<Portal> set = portalMgr.getNamedBeanSet();
        Iterator<Portal> iter = set.iterator();
        while (iter.hasNext()) {
            Portal portal =  iter.next();
            String name = portal.getName();
            PortalIcon pi = _portalIconMap.get(name);
            if (pi != null) {
                if (!checkPortalIcon(portal, pi)) {
                    _misplacedIcon.add(pi);
                }
            } else { // no icon for this Portal
                _noPortalIcon.add(portal);
            }
        }
        if (hasOBlocks) {
            if (_circuitMenu.getItemCount() <= 2) {
                _circuitMenu.removeAll();
                makeCircuitMenu();
            } else {
                makeToDoMenu();
            }
        } else {
            _circuitMenu.removeAll();
            makeNoOBlockMenu();
        }
    }   // end checkCircuits

    private boolean checkPortalIcon(Portal portal, PortalIcon icon) {
        OBlock a = portal.getToBlock();
        OBlock b = portal.getFromBlock();
        java.util.List<Positionable> aList = _circuitMap.get(a);
        java.util.List<Positionable> bList = _circuitMap.get(b);
        if (aList == null || bList == null) {
            return false;   // missing 1 or 2 blocks
        }
        boolean ok = false; // icon misplaced
        String status = icon.getStatus();
//        icon.setStatus(PortalIcon.VISIBLE);
        Rectangle homeRect = new Rectangle();
        Iterator<Positionable> iter = aList.iterator();
        while (iter.hasNext()) {
            Positionable pos = iter.next();
            if (pos instanceof PortalIcon) {
                continue;
            }
            homeRect = pos.getBounds(homeRect);
            if (iconIntersectsRect(icon, homeRect)) {
                ok = true;
                break;
            }

        }
        if (!ok) {
            icon.setStatus(status);
            return false;
        }
        ok = false;
        iter = bList.iterator();
        while (iter.hasNext()) {
            Positionable pos = iter.next();
            if (pos instanceof PortalIcon) {
                continue;
            }
            homeRect = pos.getBounds(homeRect);
            if (iconIntersectsRect(icon, homeRect)) {
                ok = true;
                break;
            }

        }
        icon.setStatus(status);
        return ok;
    }

    private static boolean iconIntersectsRect(Positionable icon, Rectangle rect) {
        Rectangle iconRect = icon.getBounds(new Rectangle());
        return (iconRect.intersects(rect));
    }

    ////////////////////////// Frame Utilities //////////////////////////
    protected List<Positionable> getCircuitIcons(OBlock block) {
        return _circuitMap.get(block);
    }

    protected OBlock getBlock(Positionable pos) {
        return _iconMap.get(pos);
    }

    protected List<Positionable> getCircuitGroup() {
        return _circuitIcons;
    }

    protected HashMap<String, PortalIcon> getPortalIconMap() {
        return _portalIconMap;
    }

    protected void changePortalName(String oldName, String newName) {
        PortalIcon icon = _portalIconMap.get(oldName);
        if (icon != null) {
            icon.setName(newName);
            _portalIconMap.remove(oldName);
            _portalIconMap.put(newName, icon);
        }
    }

    protected void removePortalIcon(String name) {
        _portalIconMap.remove(name);
    }

    protected void addPortalIcon(PortalIcon icon) {
        //Portal portal = icon.getPortal();
        String name = icon.getName();
        _portalIconMap.put(name, icon);
        if (_circuitIcons != null) {
            _circuitIcons.add(icon);
        }
    }

    /**
     * Remove block, but keep the track icons. Sets block reference in icon to
     * null.
     *
     * @param block the block to remove
     */
    protected void removeBlock(OBlock block) {
        java.util.List<Positionable> list = _circuitMap.get(block);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Positionable pos = list.get(i);
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack) pos).setOccBlockHandle(null);
                } else if (pos instanceof PortalIcon) {
                    pos.remove();
                }
                _darkTrack.add(pos);
            }
        }
        _circuitMap.remove(block);
        block.dispose();
        if (jmri.InstanceManager.getDefault(OBlockManager.class).getNamedBeanSet().size() < 2) {
            _editor.makeWarrantMenu(true, false);
        }
    }

    /*
     * *************** Overridden methods of Editor *******************
     *
     * public void paintTargetPanel(Graphics g) { Graphics2D g2d =
     * (Graphics2D)g; if (_circuitIcons!=null){ java.awt.Stroke stroke =
     * g2d.getStroke(); Color color = g2d.getColor();
     * g2d.setColor(_editGroupColor); g2d.setStroke(new
     * java.awt.BasicStroke(2.0f)); for(int i=0; i<_circuitIcons.size();i++){
     * g.drawRect(_circuitIcons.get(i).getX(), _circuitIcons.get(i).getY(),
     * _circuitIcons.get(i).maxWidth(), _circuitIcons.get(i).maxHeight()); }
     * g2d.setColor(color); g2d.setStroke(stroke); } }
     *
     * /********************* convert plain track to indicator track
     * *************
     */
    FamilyItemPanel _trackPanel;
    IndicatorTOItemPanel _trackTOPanel;
    PositionableLabel _oldIcon;
    ConvertFrame _convertFrame;     // must be modal dialog to halt convetIcons loop

    /**
     * Check if the block being edited has all its icons converted to indicator
     * icons
     * @param block OBlock to check
     * @return true if all track icons are all Indicator Track icons
     */
    protected boolean iconsConverted(OBlock block) {
        if (block == null) {
            return true;
        }
        java.util.List<Positionable> list = _circuitMap.get(block);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Positionable pos = list.get(i);
                if (!(pos instanceof IndicatorTrack) && !(pos instanceof PortalIcon)) {
                    if (log.isDebugEnabled()) {
                        log.debug("icon needs Convertion " + pos.getClass().getName());
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Icons need conversion. ask if user wants to convert them
     */
    private void queryConvertIcons(OBlock block) {
        if (block == null) {
            return;
        }
        java.util.List<Positionable> list = _circuitMap.get(block);
        if (list != null && list.size() > 0) {
            int result = JOptionPane.showConfirmDialog(_editor, Bundle.getMessage("notIndicatorIcon"),
                    Bundle.getMessage("incompleteCircuit"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                convertIcons(_circuitMap.get(block));
            }
        } else {
            JOptionPane.showMessageDialog(_editor, Bundle.getMessage("needIcons", block.getDisplayName()),
                    Bundle.getMessage("noIcons"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void convertIcons(ArrayList<Positionable> iconList) {
        if (iconList == null || iconList.isEmpty()) {
            return;
        }
        // use global member for finishConvert to remove and add converted icons,
        _circuitIcons = iconList;
        // since iconList will be modified, use a copy to find unconverted icons
        ArrayList<Positionable> list = new ArrayList<>();
        for (int i = 0; i < iconList.size(); i++) {
            list.add(iconList.get(i));
        }
        if (list.size() > 0) {
            TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
            targetPane.setHighlightColor(_highlightColor);

            for (int i = 0; i < list.size(); i++) {
                Positionable pos = list.get(i);
                if (!(pos instanceof IndicatorTrack) && !(pos instanceof PortalIcon)) {
                    if (log.isDebugEnabled()) {
                        log.debug("convertIcons: #" + i + " pos= " + pos.getClass().getName());
                    }
                    convertIcon(pos);
                }
            }
            targetPane.setHighlightColor(_editGroupColor);
            _editor.highlight(null);
        }
    }

    /**
     * Converts icon to IndicatorTrack
     */
    private void convertIcon(Positionable p) {
        PositionableLabel pos = (PositionableLabel) p;
        _oldIcon = pos;
        _editor.highlight(_oldIcon);
        _editor.toFront();
        _editor.repaint();
        if (pos instanceof TurnoutIcon) {
            _convertFrame = new ConvertFrame("IndicatorTO", pos);
            _trackTOPanel = new IndicatorTOItemPanel(_convertFrame._paletteFrame, "IndicatorTO", null, null, _editor);
            ActionListener updateAction = (ActionEvent a) -> {
                convertTO();
            };
            initConvertFrame(_trackTOPanel, updateAction);
        } else {
            _convertFrame = new ConvertFrame("IndicatorTrack", pos);
            _trackPanel = new IndicatorItemPanel(_convertFrame._paletteFrame, "IndicatorTrack", null, _editor);
            ActionListener updateAction = (ActionEvent a) -> {
                convertSeg();
            };
            initConvertFrame(_trackPanel, updateAction);
        }
        _editor.repaint();
    }


    private void initConvertFrame(ItemPanel itemPanel, ActionListener updateAction) {
        itemPanel.init(updateAction);
        Dimension dim = itemPanel.getPreferredSize();
        JScrollPane sp = new JScrollPane(itemPanel);
        dim = new Dimension(dim.width +25, dim.height + 25);
        sp.setPreferredSize(dim);
        _convertFrame._dialog.add(sp);
        _convertFrame._dialog.pack();
        _convertFrame._dialog.setVisible(true);
    }

    /*
     * gimmick to get JDialog to wait until user makes a decision to convert each track icon.
     * Holding the modal dialog does the trick.
     * Also does re-layout contents and repaint
     */
    class ConvertFrame extends JmriJFrame {

        JDialog _dialog;
        DisplayFrame _paletteFrame;

        ConvertFrame(String title, PositionableLabel pos) {
            super(false, false);
            jmri.jmrit.display.palette.ItemPalette.loadIcons(_editor);
            _paletteFrame = pos.makePaletteFrame(title);
            _dialog = new JDialog(_editor, java.text.MessageFormat.format(
                    Bundle.getMessage("EditItem"), Bundle.getMessage(title)), true);

            _dialog.setLocationRelativeTo(_editor);
            _dialog.toFront();
        }

        @Override
        public void dispose() {
            _dialog.dispose();
            super.dispose();
        }
    }

    private void convertTO() {
        IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(_editor);
        t.setOccBlockHandle(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(_currentBlock.getSystemName(), _currentBlock));
        if (_oldIcon instanceof TurnoutIcon) {
            t.setTurnout(((TurnoutIcon) _oldIcon).getNamedTurnout());
        }
        t.setFamily(_trackTOPanel.getFamilyName());

        HashMap<String, HashMap<String, NamedIcon>> iconMap = _trackTOPanel.getIconMaps();
        Iterator<Entry<String, HashMap<String, NamedIcon>>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HashMap<String, NamedIcon>> entry = it.next();
            String status = entry.getKey();
            Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, NamedIcon> ent = iter.next();
                t.setIcon(status, ent.getKey(), new NamedIcon(ent.getValue()));
            }
        }
        t.setLevel(Editor.TURNOUTS);
        t.setScale(_oldIcon.getScale());
        t.rotate(_oldIcon.getDegrees());
        finishConvert(t);
    }

    private void convertSeg() {
        IndicatorTrackIcon t = new IndicatorTrackIcon(_editor);
        t.setOccBlockHandle(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(_currentBlock.getSystemName(), _currentBlock));
        t.setFamily(_trackPanel.getFamilyName());

        HashMap<String, NamedIcon> iconMap = _trackPanel.getIconMap();
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            if (log.isDebugEnabled()) {
                log.debug("key= " + entry.getKey());
            }
            t.setIcon(entry.getKey(), new NamedIcon(entry.getValue()));
        }
        t.setLevel(Editor.TURNOUTS);
        t.setScale(_oldIcon.getScale());
        t.rotate(_oldIcon.getDegrees());
        finishConvert(t);
    }

    /*
     * Replace references to _oldIcon with pos
     */
    private void finishConvert(Positionable pos) {
        ArrayList<Positionable> selectionGroup = _editor.getSelectionGroup();
        selectionGroup.remove(_oldIcon);
        selectionGroup.add(pos);
        _circuitIcons.remove(_oldIcon);
        _circuitIcons.add(pos);
        pos.setLocation(_oldIcon.getLocation());
        _oldIcon.remove();
        _editor.putItem(pos);
        _circuitIcons.add(pos);
        pos.updateSize();

        _oldIcon = null;
        _trackPanel = null;
        _trackTOPanel = null;
        _convertFrame.dispose();
        _convertFrame = null;
    }

    /**
     * ************* end convert icons ******************
     */
    /**
     * ************** select - deselect track icons ***********************
     */
    /**
     * select block's track icons for editing -***could be
     * _circuitMap.get(block) is sufficient
     */
    private ArrayList<Positionable> makeSelectionGroup(OBlock block, boolean showPortal) {
        ArrayList<Positionable> group = new ArrayList<>();
        List<Positionable> circuitIcons = _circuitMap.get(block);
        if (circuitIcons == null) {
            return group;
        }
        Iterator<Positionable> iter = circuitIcons.iterator();
        while (iter.hasNext()) {
            Positionable p = iter.next();
            if (p instanceof PortalIcon) {
                if (showPortal) {
                    ((PortalIcon) p).setStatus(PortalIcon.VISIBLE);
                    group.add(p);
                }
            } else {
                group.add(p);
            }
        }
        return group;
    }

    protected static boolean isTrack(Positionable pos) {
        if (pos instanceof IndicatorTrack) {
            return true;
        } else if (pos instanceof TurnoutIcon) {
            return true;
        } else if (pos instanceof PositionableLabel) {
            PositionableLabel pl = (PositionableLabel) pos;
            if (pl.isIcon()) {
                NamedIcon icon = (NamedIcon) pl.getIcon();
                if (icon != null) {
                    String fileName = icon.getURL();
                    // getURL() returns Unix separatorChar= "/" even on windows
                    // so don't use java.io.File.separatorChar
                    if (fileName != null && (fileName.contains("/track/")
                            || (fileName.contains("/tracksegments/") && !fileName.contains("circuit")))) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private static boolean isUnconvertedTrack(Positionable pos) {
        if (pos instanceof IndicatorTrack) {
            return false;
        } else if (pos instanceof TurnoutIcon) {
            return true;
        } else if (pos instanceof PositionableLabel) {
            PositionableLabel pl = (PositionableLabel) pos;
            if (pl.isIcon()) {
                NamedIcon icon = (NamedIcon) pl.getIcon();
                if (icon != null) {
                    String fileName = icon.getURL();
                    if (log.isDebugEnabled()) {
                        log.debug("isUnconvertedTrack Test: url= " + fileName);
                    }
                    // getURL() returns Unix separatorChar= "/" even on windows
                    // so don't use java.io.File.separatorChar
                    if (fileName != null
                            && (fileName.contains("/track/") || fileName.contains("/tracksegments/"))
                            && !fileName.contains("circuit")) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    /**
     * Can this track icon be added to the circuit? N.B. Be sure Positionable
     * pos passes isTrack() call
     */
    private boolean okToAdd(Positionable pos, OBlock editBlock) {
        if (pos instanceof IndicatorTrack) {
            OBlock block = ((IndicatorTrack) pos).getOccBlock();
            if (block != null) {
                if (!block.equals(editBlock)) {
                    int result = JOptionPane.showConfirmDialog(_editor, java.text.MessageFormat.format(
                            Bundle.getMessage("iconBlockConflict"),
                            block.getDisplayName(), editBlock.getDisplayName()),
                            Bundle.getMessage("whichCircuit"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        // move icon from block to editBlock
                        java.util.List<Positionable> ic = _circuitMap.get(block);
                        ic.remove(pos);
                        ((IndicatorTrack) pos).setOccBlockHandle(
                                InstanceManager.getDefault(NamedBeanHandleManager.class)
                                        .getNamedBeanHandle(editBlock.getSystemName(), editBlock));
                        return true;
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * ************************** Mouse ************************
     */
    ArrayList<Positionable> _saveSelectionGroup;

    /**
     * Keep selections when editing. Editor calls at entry to mousePressed().
     * CircuitBuilder keeps is own concept of what is "selected".
     *
     * @param selectionGroup the selection group to save
     * @return true if retaining a reference to a frame
     */
    protected boolean saveSelectionGroup(ArrayList<Positionable> selectionGroup) {
        _saveSelectionGroup = selectionGroup;
        return _editCircuitFrame != null
                || _editPortalFrame != null
                || _editPathsFrame != null
                || _editDirectionFrame != null;
    }

    /**
     * If CircuitBuilder is in progress, restore what editor nulls.
     *
     * @param event     the triggering event
     * @param selection ignored
     * @return true if the selection group is restored; false otherwise
     */
    protected boolean doMousePressed(MouseEvent event, Positionable selection) {
        if (_editCircuitFrame != null) {
            _editCircuitFrame.toFront();
            _editor.setSelectionGroup(_saveSelectionGroup);
        } else if (_editPathsFrame != null) {
            _editPathsFrame.toFront();
            _editor.setSelectionGroup(_saveSelectionGroup);
        } else if (_editPortalFrame != null) {
            _editPortalFrame.toFront();
            _editor.setSelectionGroup(_saveSelectionGroup);
            // _editPortalFrame.setSelection(selection);
        } else if (_editDirectionFrame != null) {
            _editDirectionFrame.toFront();
            _editor.setSelectionGroup(_saveSelectionGroup);
        } else {
            return false;
        }
        return true;
    }

    public boolean doMouseReleased(Positionable selection, boolean dragging) {
        if (_editCircuitFrame != null || _editPathsFrame != null || _editDirectionFrame != null) {
            return true;
        } else if (_editPortalFrame != null) {
            if (selection instanceof PortalIcon && _circuitIcons.contains(selection)) {
                if (dragging) {
                    _editPortalFrame.checkPortalIconForUpdate((PortalIcon) selection, true);
                } else {
                    _editPortalFrame.setSelected((PortalIcon)selection);
                }
            }
            return true;
        }
        return false;
    }

    protected boolean doMouseClicked(List<Positionable> selections, MouseEvent event) {
        if (_editCircuitFrame != null || _editPathsFrame != null
                || _editPortalFrame != null || _editDirectionFrame != null) {
            if (selections != null && selections.size() > 0) {
                ArrayList<Positionable> tracks = new ArrayList<>();
                Iterator<Positionable> iter = selections.iterator();
                if (_editCircuitFrame != null) {
                    while (iter.hasNext()) {
                        Positionable pos = iter.next();
                        if (isTrack(pos)) {
                            tracks.add(pos);
                        }
                    }
                } else if (_editPathsFrame != null) {
                    while (iter.hasNext()) {
                        Positionable pos = iter.next();
                        if (isTrack(pos) || pos instanceof PortalIcon) {
                            tracks.add(pos);
                        }
                    }
                } else {
                    while (iter.hasNext()) {
                        Positionable pos = iter.next();
                        if (pos instanceof PortalIcon) {
                            tracks.add(pos);
                        }
                    }
                }
                if (tracks.size() > 0) {
                    Positionable selection;
                    if (tracks.size() == 1) {
                        selection = tracks.get(0);
                    } else {
                        selection = getSelection(tracks);
                    }
                    if (_editPathsFrame != null && event.isShiftDown() && !event.isControlDown()) {
                        selection.doMouseClicked(event);
                    }
                    handleSelection(selection, event);
                }
            }
            return true;
        }
        return false;
    }

    private Positionable getSelection(List<Positionable> tracks) {
        if (tracks.size() > 0) {
            if (tracks.size() == 1) {
                return tracks.get(0);
            }
            if (tracks.size() > 1) {
                String[] selects = new String[tracks.size()];
                Iterator<Positionable> iter = tracks.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    selects[i++] = iter.next().getNameString();
                }
                Object select = JOptionPane.showInputDialog(_editor, Bundle.getMessage("multipleSelections"),
                        Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE,
                        null, selects, null);
                if (select != null) {
                    iter = tracks.iterator();
                    while (iter.hasNext()) {
                        Positionable pos = iter.next();
                        if (((String) select).equals(pos.getNameString())) {
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Prevent dragging when CircuitBuilder is in progress, except for
     * PortalIcon.
     *
     * @param selection the item(s) being dragged
     * @param event     the triggering event
     * @return true to prevent dragging; false otherwise
     */
    public boolean doMouseDragged(Positionable selection, MouseEvent event) {
        if (_editCircuitFrame != null || _editPathsFrame != null) {
            return true;     // no dragging when editing
        }
        if (selection instanceof PortalIcon) {
            if (_editPortalFrame != null) {
                _editPortalFrame.setSelected((PortalIcon)selection);
                return false;  // OK to drag portal icon
            } else if (_editDirectionFrame != null) {
                return false;  // OK to drag portal arrow
            }
        }
        return false;
    }

    /**
     * Second call needed to only drag the portal icon and not entire selection
     *
     * @return true if portal frame is open
     */
    public boolean dragPortal() {
        return (_editPortalFrame != null || _editDirectionFrame != null);
    }

    /*
     * For the param, selection, Add to or delete from selectionGroup.
     * If not there, add.
     * If there, delete.
     */
    private void handleSelection(Positionable selection, MouseEvent event) {
        if (_editCircuitFrame != null) {
            ArrayList<Positionable> selectionGroup = _editor.getSelectionGroup();
            if (isTrack(selection)) {
                if (selectionGroup == null) {
                    selectionGroup = new ArrayList<>();
                }
                if (selectionGroup.contains(selection)) {
                    selectionGroup.remove(selection);
                } else if (okToAdd(selection, _editCircuitFrame.getBlock())) {
                    selectionGroup.add(selection);
                }
            }
            _editCircuitFrame.updateIconList(selectionGroup);
            _editCircuitFrame.toFront();
            _editor.setSelectionGroup(selectionGroup);
        } else if (_editPathsFrame != null) {
            if (selection instanceof IndicatorTrack || selection instanceof PortalIcon) {
                _editPathsFrame.updateSelections(!event.isShiftDown(), selection);
            }
            _editPathsFrame.toFront();
        } else if (_editPortalFrame != null) {
            if (log.isDebugEnabled()) {
                log.debug("selection= " + (selection == null ? "null"
                        : selection.getClass().getName()));
            }
            if (selection instanceof PortalIcon) {
                _editPortalFrame.setSelected((PortalIcon)selection);
            }
            _editPortalFrame.toFront();
        } else if (_editDirectionFrame != null) {
            if (selection instanceof PortalIcon) {
                PortalIcon icon = (PortalIcon) selection;
                if (_circuitIcons.contains(selection)) {
                    _editDirectionFrame.setPortalIcon(icon, true);
                } else {
                    _editDirectionFrame.setPortalIcon(null, false);
                    JOptionPane.showMessageDialog(_editDirectionFrame, Bundle.getMessage("iconNotOnBlock",
                            _editDirectionFrame.getHomeBlock().getDisplayName(), icon.getPortal().getDescription()),
                            Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                _editDirectionFrame.setPortalIcon(null, false);
            }
            _editDirectionFrame.toFront();
        }
    }

    ////////////////////////// static methods //////////////////////////
    protected static void doSize(JComponent comp, int max, int min) {
        Dimension dim = comp.getPreferredSize();
        dim.width = max;
        comp.setMaximumSize(dim);
        dim.width = min;
        comp.setMinimumSize(dim);
    }

    protected static JPanel makeTextBoxPanel(boolean vertical, JTextField textField, String label,
            boolean editable, String tooltip) {
        JPanel panel = makeBoxPanel(vertical, textField, label, tooltip);
        textField.setEditable(editable);
        textField.setBackground(Color.white);
        return panel;
    }

    protected static JPanel makeBoxPanel(boolean vertical, JComponent textField, String label,
            String tooltip) {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        JLabel l = new JLabel(Bundle.getMessage(label));
        if (vertical) {
            c.anchor = java.awt.GridBagConstraints.SOUTH;
            l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            textField.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        } else {
            c.anchor = java.awt.GridBagConstraints.EAST;
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            textField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        }
        panel.add(l, c);
        if (vertical) {
            c.anchor = java.awt.GridBagConstraints.NORTH;
            c.gridy = 1;
        } else {
            c.anchor = java.awt.GridBagConstraints.WEST;
            c.gridx = 1;
        }
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        doSize(textField, 9000, 200);    // default
        panel.add(textField, c);
        if (tooltip != null) {
            textField.setToolTipText(Bundle.getMessage(tooltip));
            l.setToolTipText(Bundle.getMessage(tooltip));
            panel.setToolTipText(Bundle.getMessage(tooltip));
        }
        return panel;
    }

    private final static Logger log = LoggerFactory.getLogger(CircuitBuilder.class);
}
