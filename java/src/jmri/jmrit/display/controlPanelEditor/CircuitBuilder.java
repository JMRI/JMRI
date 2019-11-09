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
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor.TargetPane;
import jmri.jmrit.display.IndicatorTrack;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.display.TurnoutIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.jmrit.logix.WarrantTableAction;
import jmri.jmrit.picker.PickListModel;
import jmri.util.HelpUtil;

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

    // map OBlock to List of icons (track, portal, signal that represent it
    private HashMap<OBlock, ArrayList<Positionable>> _circuitMap = new HashMap<>();

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

    // list of OBlocks with no Portal
    private final ArrayList<OBlock> _noPortals = new ArrayList<>();

    // list of OBlocks with no Path
    private final ArrayList<OBlock> _noPaths = new ArrayList<>();

    // list of misplaced PortalIcons
    private final ArrayList<PortalIcon> _misplacedPortalIcon = new ArrayList<>();

    // map of PortalIcons by portal. A Portal may have 2 icons to connect non-adjacent blocks
    private final HashMap<Portal, ArrayList<PortalIcon>> _portalIconMap = new HashMap<>();
    
    // map of SignalMastIcon or SignalHeadicon by Mast name
    private final HashMap<NamedBean, ArrayList<PositionableIcon>> _signalIconMap = new HashMap<>();
    
    // list of SignalMastIcon and SignalHeadicon not protecting a block
    private final ArrayList<PositionableIcon> _unattachedMastIcon = new ArrayList<>();

    // map SignalMasts and SignalHeads to the Portal where it is configured
    private HashMap<NamedBean, Portal> _signalMap = new HashMap<>();

    private boolean _hasIndicatorTrackIcons;
    private boolean _hasPortalIcons;
    private boolean _hasMastIcons;

    // OBlock list to open edit frames
    private PickListModel<OBlock> _oblockModel;
    private JTable _blockTable;
    jmri.util.JmriJFrame _cbFrame;

    // "Editing Frames" - Called from menu in Main Frame
    private EditFrame _editFrame;

    private final JTextField _sysNameBox = new JTextField();
    private final JTextField _userNameBox = new JTextField();
    private OBlock _currentBlock;
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
        _oblockModel = PickListModel.oBlockPickModelInstance();
        _blockTable = _oblockModel.makePickTable();

    }

    /**
     * Makes menu for ControlPanelEditor. Called by ControlPanelEditor at init
     * before contents have been loaded.
     *
     * @return the menu, created if needed
     */
    protected JMenu makeMenu() {
        _circuitMenu = new JMenu(Bundle.getMessage("CircuitBuilder"));
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        SortedSet<OBlock> oblocks = manager.getNamedBeanSet();
        for (OBlock block : oblocks) {
            _circuitMap.put(block, new ArrayList<>());
        }
        checkCircuits();  // need content for this
        int num = Math.min(manager.getObjectCount(), 20) + 5;
        _blockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, _blockTable.getRowHeight() * num));
        return _circuitMenu;
    }

    protected void openCBWindow() {
        if (_cbFrame != null) {
            _cbFrame.toFront();
        } else {
            _cbFrame = new CBFrame(Bundle.getMessage("CircuitBuilder"));
        }
    }

    private void makeNoOBlockMenu() {
        JMenuItem circuitItem = new JMenuItem(Bundle.getMessage("newCircuitItem"));
        _circuitMenu.add(circuitItem);
        circuitItem.addActionListener((ActionEvent event) -> {
            newCircuit();
        });
        _circuitMenu.add(new JMenuItem(Bundle.getMessage("noCircuitsItem")));
        JMenuItem helpItem = new JMenuItem(Bundle.getMessage("AboutCircuitBuilder"));
        HelpUtil.getGlobalHelpBroker().enableHelpOnButton(helpItem, "package.jmri.jmrit.display.CircuitBuilder", null);
        _circuitMenu.add(helpItem);

    }
    private void makeCircuitMenu() {
        JMenuItem editItem = new JMenuItem(Bundle.getMessage("newCircuitItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            closeCBWindow();
            newCircuit();
        });
        editItem = new JMenuItem(Bundle.getMessage("editCircuitItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            closeCBWindow();
            editCircuit("editCircuitItem", true);
        });
        editItem = new JMenuItem(Bundle.getMessage("editPortalsItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            closeCBWindow();
            editPortals("editPortalsItem", true);
        });
        editItem = new JMenuItem(Bundle.getMessage("editCircuitPathsItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            closeCBWindow();
            editCircuitPaths("editCircuitPathsItem", true);
        });
        editItem = new JMenuItem(Bundle.getMessage("editDirectionItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            closeCBWindow();
            editPortalDirection("editDirectionItem", true);
        });
        editItem = new JMenuItem(Bundle.getMessage("editSignalItem"));
        _circuitMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            closeCBWindow();
            editSignalFrame("editSignalItem", true);
        });
        _todoMenu = new JMenu(Bundle.getMessage("circuitErrorsItem"));
        _circuitMenu.add(_todoMenu);
        JMenuItem helpItem = new JMenuItem(Bundle.getMessage("AboutCircuitBuilder"));
        HelpUtil.getGlobalHelpBroker().enableHelpOnButton(helpItem, "package.jmri.jmrit.display.CircuitBuilder", null);
        _circuitMenu.add(helpItem);
        makeToDoMenu();
    }

    /**
     * Add icon 'pos' to circuit 'block'
     */
    private void addIcon(OBlock block, Positionable pos) {
        List<Positionable> icons = getCircuitIcons(block);
        if (pos != null) {
            if (!icons.contains(pos)) {
                icons.add(pos);
            }
        }
        _darkTrack.remove(pos);
        // if (log.isDebugEnabled()) log.debug("addIcon: block "+block.getDisplayName()+" has "+icons.size()+" icons.");
    }

    // Rebuild after any edit change
    private void makeToDoMenu() {
        if (_todoMenu == null) {
            _todoMenu = new JMenu(Bundle.getMessage("circuitErrorsItem"));
            _circuitMenu.add(_todoMenu);
        } else {
            _todoMenu.removeAll();
        }

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

        if (_darkTrack.size() > 0) {
            iconNeeds = new JMenuItem(Bundle.getMessage("iconsNeedsBlocksItem"));
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

        if (!_noPortals.isEmpty()) {
            blockNeeds = new JMenu(Bundle.getMessage("blockNeedsPortals"));
            ActionListener editPortalAction = (ActionEvent event) -> {
                String sysName = event.getActionCommand();
                _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(sysName);
                editPortals(null, false);
            };
            for (OBlock block : _noPortals) {
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        Bundle.getMessage("OpenPortalTitle"), block.getDisplayName()));
                mi.setActionCommand(block.getSystemName());
                mi.addActionListener(editPortalAction);
                blockNeeds.add(mi);
            }
        } else if (_noPaths.size() > 0) {
            blockNeeds = new JMenu(Bundle.getMessage("blockNeedsPaths"));
            ActionListener editPortalAction = (ActionEvent event) -> {
                String sysName = event.getActionCommand();
                _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(sysName);
                editCircuitPaths(null, false);
            };
            for (OBlock block : _noPaths) {
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        Bundle.getMessage("OpenPathTitle"), block.getDisplayName()));
                mi.setActionCommand(block.getSystemName());
                mi.addActionListener(editPortalAction);
                blockNeeds.add(mi);
            }
        } else {
            blockNeeds = new JMenu(Bundle.getMessage("circuitsHavePortalsPaths"));
        }
        _todoMenu.add(blockNeeds);  // #5

        blockNeeds = new JMenu(Bundle.getMessage("portalsMisplaced"));
        if (_misplacedPortalIcon.size() > 0) {
            for (PortalIcon icon : _misplacedPortalIcon) {
                Portal portal = icon.getPortal();
                OBlock fromBlock = portal.getFromBlock();
                OBlock toBlock = portal.getToBlock();
                if (fromBlock != null) {
                    JMenuItem mi = new JMenuItem(Bundle.getMessage("OpenPortalTitle", fromBlock.getDisplayName()));
                    mi.addActionListener((ActionEvent event) -> {
                        editPortalError(fromBlock, portal, icon);
                    });
                    blockNeeds.add(mi);
                } else if (toBlock != null) {
                    JMenuItem mi = new JMenuItem(Bundle.getMessage("OpenPortalTitle", toBlock.getDisplayName()));
                    mi.addActionListener((ActionEvent event) -> {
                        editPortalError(toBlock, portal, icon);
                    });
                    blockNeeds.add(mi);
                }
            }
        } else {
            if (_hasPortalIcons) {
                blockNeeds.add(new JMenuItem(Bundle.getMessage("portalsInPlace")));
            } else {
                blockNeeds.add(new JMenuItem(Bundle.getMessage("NoPortalIcons")));
            }
        }
        _todoMenu.add(blockNeeds);  //#6

        if (_misplacedPortalIcon.size() > 0) {
            iconNeeds = new JMenuItem(Bundle.getMessage("iconsNeedPositioning"));
            iconNeeds.addActionListener((ActionEvent event) -> {
                if (editingOK()) {
                    ArrayList<Positionable> group = new ArrayList<>();
                    for (int i = 0; i < _misplacedPortalIcon.size(); i++) {
                        PortalIcon pi = _misplacedPortalIcon.get(i);
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
        _todoMenu.add(iconNeeds);   // #7

        if (_unattachedMastIcon.size() > 0) {
            iconNeeds = new JMenuItem(Bundle.getMessage("UnattachedMasts"));
            iconNeeds.addActionListener((ActionEvent event) -> {
                if (editingOK()) {
                    ArrayList<Positionable> group = new ArrayList<>();
                    for (int i = 0; i < _unattachedMastIcon.size(); i++) {
                        PositionableIcon pi = _unattachedMastIcon.get(i);
                        group.add(pi);
                    }
                    _editor.setSelectionGroup(group);
                }
            });
        } else {
            if (_hasMastIcons) {
                iconNeeds = new JMenuItem(Bundle.getMessage("mastsInPlace"));
            } else {
                iconNeeds = new JMenuItem(Bundle.getMessage("NoMastIcons"));
            }
        }
        _todoMenu.add(iconNeeds);   // #8

        blockNeeds = new JMenu(Bundle.getMessage("portalNeedsIcon"));
        ActionListener editPortalAction = (ActionEvent event) -> {
            String portalName = event.getActionCommand();
            editPortalError(portalName);
            };
        if (_noPortalIcon.size() > 0) {
            for (int i = 0; i < _noPortalIcon.size(); i++) {
                Portal portal = _noPortalIcon.get(i);
                JMenuItem mi = new JMenuItem(portal.toString());
                mi.setActionCommand(portal.getName());
                mi.addActionListener(editPortalAction);
                blockNeeds.add(mi);
            }
        } else {
            blockNeeds.add(new JMenuItem(Bundle.getMessage("portalsHaveIcons")));
        }
        _todoMenu.add(blockNeeds);  // #9

        JMenuItem pError = new JMenuItem(Bundle.getMessage("CheckPortalPaths"));
        _todoMenu.add(pError);      // #10
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
            JOptionPane.showMessageDialog(_editFrame,
                    Bundle.getMessage("blocksEtcOK"), Bundle.getMessage("ButtonOK"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // used for testing only
    protected EditFrame getEditFrame() {
        return _editFrame;
    }

    /**
     * ************** Set up editing Frames ****************
     */
    protected void newCircuit() {
        if (editingOK()) {
            _blockTable.clearSelection();
            addCircuitDialog();
            if (_currentBlock != null) {
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, false));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_highlightColor);
                _editFrame = new EditCircuitFrame(Bundle.getMessage("newCircuitItem"), this, _currentBlock);
            }
        }
    }

    protected void editCircuit(String title, boolean fromMenu) {
        if (editingOK()) {
            if (fromMenu) {
                editCircuitDialog(title);
            }
            if (_currentBlock != null) {
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, false));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_highlightColor);
                _editFrame = new EditCircuitFrame(Bundle.getMessage("OpenCircuitItem"), this, _currentBlock);
            } else if (!fromMenu) {
                selectPrompt();
            }
        }
    }

    protected void editCircuitError(String sysName) {
        hidePortalIcons();
        if (editingOK()) {
            _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBySystemName(sysName);
            if (_currentBlock != null) {
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, false));
                _editor.disableMenus();
                _editFrame = new EditCircuitFrame(Bundle.getMessage("OpenCircuitItem"), this, _currentBlock);
            }
        }
    }

    protected void editPortals(String title, boolean fromMenu) {
        if (editingOK()) {
            if (fromMenu) {
                editCircuitDialog(title);
            }
            if (_currentBlock != null) {
                // check icons to be indicator type
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_highlightColor);
                setPortalsPositionable(_currentBlock, true);
                _editFrame = new EditPortalFrame(Bundle.getMessage("OpenPortalTitle"), this, _currentBlock);
                _editFrame.canEdit();   // will close _editFrame if editing cannot be done
            } else if (!fromMenu) {
                selectPrompt();
            }
        }
    }

    protected void editPortalError(String name) {
        if (editingOK()) {
            Portal portal = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class).getPortal(name);
            _currentBlock = portal.getFromBlock();
            if (_currentBlock == null) {
                _currentBlock = portal.getToBlock();
            }
            editPortals(null, false);
        }
    }

    protected void editPortalError(OBlock block, Portal portal, PortalIcon icon) {
        if (editingOK()) {
            _currentBlock = block;
            if (_currentBlock != null) {
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_highlightColor);
                setPortalsPositionable(_currentBlock, true);
                _editFrame = new EditPortalFrame(Bundle.getMessage("OpenPortalTitle"), this,
                        _currentBlock, portal, icon);
            }
        }
    }

    protected void editPortalDirection(String title, boolean fromMenu) {
        if (editingOK()) {
            if (fromMenu) {
                editCircuitDialog(title);
            }
            if (_currentBlock != null) {
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_highlightColor);
                setPortalsPositionable(_currentBlock, true);
                _editFrame = new EditPortalDirection(Bundle.getMessage("OpenDirectionTitle"), this, _currentBlock);
                _editFrame.canEdit();   // will close _editFrame if editing cannot be done
            } else if (!fromMenu) {
                selectPrompt();
            }
        }
    }

    protected void editSignalFrame(String title, boolean fromMenu) {
        if (editingOK()) {
            if (fromMenu) {
                editCircuitDialog(title);
            }
            if (_currentBlock != null) {
                // check icons to be indicator type
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_highlightColor);
                _editFrame = new EditSignalFrame(Bundle.getMessage("OpenSignalsTitle"), this, _currentBlock);
                _editFrame.canEdit();   // will close _editFrame if editing cannot be done
            } else if (!fromMenu) {
                selectPrompt();
            }
        }
    }

    protected void editCircuitPaths(String title, boolean fromMenu) {
        if (editingOK()) {
            if (fromMenu) {
                editCircuitDialog(title);
            }
            if (_currentBlock != null) {
                // check icons to be indicator type
                // must have converted icons for paths
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                // A temporary path "TEST_PATH" is used to display the icons representing a path
                _currentBlock.allocate(EditCircuitPaths.TEST_PATH);
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane) _editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_editGroupColor);
                _currentBlock.setState(OBlock.UNOCCUPIED);
                _editFrame = new EditCircuitPaths(Bundle.getMessage("OpenPathTitle"), this, _currentBlock);
                _editFrame.canEdit();   // will close _editFrame if editing cannot be done
            } else if (!fromMenu) {
                selectPrompt();
            }
        }
    }
    
    // for testing only
    protected void setCurrentBlock(OBlock b) {
        _currentBlock = b;
    }

    protected void hidePortalIcons() {
        if (_editFrame != null) {
            _editFrame.clearListSelection();
        } else {
            for (ArrayList<PortalIcon> array : _portalIconMap.values()) {
                for (PortalIcon pi : array) {
                    pi.setStatus(PortalIcon.HIDDEN);
                }
            }
        }
    }

    private boolean editingOK() {
        if (_editFrame != null) {
            // Already editing a circuit, ask for completion of that edit
            JOptionPane.showMessageDialog(_editFrame,
                    Bundle.getMessage("AlreadyEditing"), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            _editFrame.toFront();
            _editFrame.setVisible(true);
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

        mainPanel.add(new JScrollPane(_blockTable));
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
                OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
                int num = manager.getObjectCount();
                if (num == 2) {
                    _editor.makeWarrantMenu(true, false);
                }
                num = Math.min(num,  20) + 5;
                _blockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, _blockTable.getRowHeight() * num));
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
        int row = _blockTable.getSelectedRow();
        if (row >= 0) {
            row = _blockTable.convertRowIndexToModel(row);
            _currentBlock = _oblockModel.getBeanAt(row);
            return true;
        }
        selectPrompt();
        return false;
    }
    private void selectPrompt() {
        JOptionPane.showMessageDialog(_editor, Bundle.getMessage("selectOBlock"),
                Bundle.getMessage("NeedDataTitle"), JOptionPane.INFORMATION_MESSAGE);
        _currentBlock = null;
    }

    /*
     * ************************ end setup frames *****************************
     */
    private void setPortalsPositionable(OBlock block, boolean set) {
        List<Positionable> circuitIcons = getCircuitIcons(block);
        Iterator<Positionable> iter = circuitIcons.iterator();
        while (iter.hasNext()) {
            Positionable p = iter.next();
            if (p instanceof PortalIcon) {
                p.setPositionable(set);
            }
        }
    }

    ////////////////////////// Closing Editing Frames //////////////////////////
    /**
     * Edit frame closing, set block's icons to support OBlock's state changes
     * @param block OBlock to set icon selections into data maps
     */
    protected void setIconGroup(OBlock block) {
        for (Positionable pos : getCircuitIcons(block)) {
            if (pos instanceof IndicatorTrack) {
                ((IndicatorTrack) pos).setOccBlockHandle(null);
            }
        }
        // the selectionGroup for all edit frames is full collection of icons
        // comprising the block.  Gather them and store in the block's hashMap
        List<Positionable> selections = _editor.getSelectionGroup();
        List<Positionable> icons = getCircuitIcons(block);
        icons.clear();
        if (!selections.isEmpty()) {
            NamedBeanHandle<OBlock> handle = 
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(block.getSystemName(), block);
             for (Positionable pos : selections) {
                 if (pos instanceof IndicatorTrack) {
                     ((IndicatorTrack) pos).setOccBlockHandle(handle);
                 }
                 icons.add(pos);
             }
        }
        if (log.isDebugEnabled()) {
            log.debug("setIconGroup: block \"{}\" has {} icons.", block.getDisplayName(), icons.size());
        }
    }

    protected void closeCircuitBuilder(OBlock block) {
        _currentBlock = null;
        _editFrame = null;
        checkCircuits();
        setPortalsPositionable(block, false);
        hidePortalIcons();
        _editor.setSecondSelectionGroup(null);
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
        _signalIconMap.clear();
        _signalMap.clear();
        _unattachedMastIcon.clear();
        _darkTrack.clear();
        _unconvertedTrack.clear();
        _hasIndicatorTrackIcons = false;
        _hasPortalIcons = false;
        _hasMastIcons = false;
        ArrayList<Positionable> removeList = new ArrayList<>();
        PortalManager portalMgr = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);

        Iterator<Positionable> it = _editor.getContents().iterator();
        while (it.hasNext()) {
            Positionable pos = it.next();
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
                Portal portal = pIcon.getPortal();
                if (portal == null) {
                    log.error("No Portal for PortalIcon called \"{}\". Discarding icon.", pIcon.getName());
                    removeList.add(pIcon);
                } else {
                    List<PortalIcon> piArray = getPortalIconMap(portal);
                    piArray.add(pIcon);
                }
            } else if (pos instanceof SignalMastIcon) {
                _hasMastIcons = true;
                SignalMastIcon sIcon = (SignalMastIcon)pos;
                NamedBean mast = sIcon.getSignalMast();
                if (mast == null) {
                    log.error("No SignalMast for SignalMastIcon called \"{}\".", sIcon.getNameString());
                    removeList.add(sIcon);
                } else {
                    List<PositionableIcon> siArray = getSignalIconMap(mast);
                    siArray.add(sIcon);
                    _unattachedMastIcon.add(sIcon);
                }
            } else if (pos instanceof SignalHeadIcon) {
                _hasMastIcons =true;
                SignalHeadIcon sIcon = (SignalHeadIcon)pos;
                NamedBean mast = sIcon.getSignalHead();
                if (mast == null) {
                    log.error("No SignalHead for SignalHeadIcon called \"{}\".", sIcon.getNameString());
                    removeList.add(sIcon);
                } else {
                    List<PositionableIcon> siArray = getSignalIconMap(mast);
                    siArray.add(sIcon);
                    _unattachedMastIcon.add(sIcon);
                }
            } else if (isUnconvertedTrack(pos)) {
                if (!_unconvertedTrack.contains(pos)) {
                    _unconvertedTrack.add(pos);
                }
            }
        }
        Iterator<Positionable> its = removeList.iterator();
        while (its.hasNext()) {
            its.next().remove();
        }

        _bareBlock.clear();         // blocks with no track icons
        _convertBlock.clear();      // blocks with at least one unconverted track icon
        _misplacedPortalIcon.clear();
        _noPortalIcon.clear();
        _noPortals.clear();
        _noPaths.clear();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        SortedSet<OBlock> oblocks = manager.getNamedBeanSet();
        for (OBlock block : oblocks) {
            List<Portal> portals = block.getPortals();
            if (portals.isEmpty()) {
                _noPortals.add(block);
            } else {
                // first add PortalIcons and SignalIcons to circuitMap 
                for (Portal portal : portals) {
                    List<PortalIcon> piArray = getPortalIconMap(portal);
                    for (PortalIcon pi : piArray) {
                        addIcon(block, pi);
                    }
                    NamedBean mast = portal.getSignalProtectingBlock(block);
                    if (mast != null) {
                        List<PositionableIcon> siArray = getSignalIconMap(mast);
                        for (PositionableIcon si : siArray) {
                            addIcon(block, si);
                            _unattachedMastIcon.remove(si);

                        }
                        _signalMap.put(mast, portal);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Portal {} in block {} has {} icons", portal.getName(), block.getDisplayName(), piArray.size());
                    }
                }
            }
            List<jmri.Path> paths = block.getPaths();
            if (paths ==null || paths.isEmpty()) {
                _noPaths.add(block);
            }

            List<Positionable> icons = getCircuitIcons(block);
            if (log.isDebugEnabled()) {
                log.debug("checkCircuits: block " + block.getDisplayName()
                        + " has " + icons.size() + " icons.");
            }
            if (icons.isEmpty()) {
                _bareBlock.add(block);
            } else {
                boolean hasTrackIcon = false;
                boolean iconNeedsConversion = false;
                for (int k = 0; k < icons.size(); k++) {
                    Positionable pos = icons.get(k);
                    if (!(pos instanceof PortalIcon) && !(pos instanceof SignalMastIcon) && !(pos instanceof SignalHeadIcon) ) {
                        hasTrackIcon = true;
                        if (!(pos instanceof IndicatorTrack)) {
                            iconNeedsConversion = true;
                        }
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

        // check positioning of portal icons for 'direction arrow' state.
        for (Portal portal : portalMgr.getPortalSet()) {
            List<PortalIcon> piArray = getPortalIconMap(portal);
            if (piArray.isEmpty()) {
                _noPortalIcon.add(portal);
            } else {
                PortalIcon icon1 = piArray.get(0);
                if (piArray.size() == 1) {
                    if (!iconIntersectsBlock(icon1, portal.getToBlock()) ||
                            !iconIntersectsBlock(icon1, portal.getFromBlock())) {
                        _misplacedPortalIcon.add(icon1);
                    }
                } else {
                    boolean fromOK = false;
                    boolean toOK = false;
                    PortalIcon icon = null;
                    for (PortalIcon ic : piArray) {
                        if (!toOK && iconIntersectsBlock(ic, portal.getToBlock()) &&
                                !iconIntersectsBlock(ic, portal.getFromBlock())) {
                            toOK = true;
                        } else if (!fromOK && !iconIntersectsBlock(ic, portal.getToBlock()) &&
                                iconIntersectsBlock(ic, portal.getFromBlock())) {
                            fromOK = true;
                        } else {
                            icon = ic;
                        }
                    }
                    if (!toOK || !fromOK) {
                        _misplacedPortalIcon.add(icon);
                    }
                }
            }
        }

        if (oblocks.size() > 1) {
            if (_circuitMenu.getItemCount() <= 3) {
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

    protected boolean iconIntersectsBlock(Positionable icon, OBlock block) {
        java.util.List<Positionable> list = getCircuitIcons(block);
        if (list.isEmpty()) {
            return false;
        }
        Rectangle rect = new Rectangle();
        Rectangle iconRect = icon.getBounds(new Rectangle());
        for (int i = 0; i < list.size(); i++) {
            Positionable comp = list.get(i);
            if (CircuitBuilder.isTrack(comp)) {
                rect = list.get(i).getBounds(rect);
                if (iconRect.intersects(rect)) {
                    return true;
                }
            }
        }
        return false;
    }

    ////////////////////////// Frame Utilities //////////////////////////
    @Nonnull
    protected ArrayList<Positionable> getCircuitIcons(OBlock block) {
        // return empty array when block == null
        ArrayList<Positionable> array = _circuitMap.get(block);
        if (array == null) {
            array = new ArrayList<Positionable>();
            _circuitMap.put(block, array);
        }
        return array;
    }

    @Nonnull 
    protected List<PortalIcon> getPortalIconMap(@Nonnull Portal portal) {
        ArrayList<PortalIcon> array = _portalIconMap.get(portal);
        if (array == null) {
            array = new ArrayList<PortalIcon>();
            _portalIconMap.put(portal, array);
        }
        return array;
    }

    @Nonnull 
    protected List<PositionableIcon> getSignalIconMap(@Nonnull NamedBean mast) {
        ArrayList<PositionableIcon> array = _signalIconMap.get(mast);
        if (array == null) {
            array = new ArrayList<PositionableIcon>();
            _signalIconMap.put(mast, array);
        }
        return array;
    }

    protected Portal getSignalPortal(@Nonnull NamedBean mast) {
        return _signalMap.get(mast);
    }

    protected void putSignalPortal(@Nonnull NamedBean mast, Portal portal) {
        if (portal == null) {
            _signalMap.remove(mast);
        }
        _signalMap.put(mast, portal);
    }

    /**
     * Remove block, but keep the track icons. Sets block reference in icon to
     * null.
     *
     * @param block the block to remove
     */
    protected void removeBlock(OBlock block) {
        java.util.List<Positionable> list = getCircuitIcons(block);
        for (Positionable pos : list) {
            if (pos instanceof IndicatorTrack) {
                ((IndicatorTrack) pos).setOccBlockHandle(null);
/*            } else if (pos instanceof PortalIcon) {
                pos.remove();*/
            }
            _darkTrack.add(pos);
        }
        block.dispose();
        if (jmri.InstanceManager.getDefault(OBlockManager.class).getNamedBeanSet().size() < 2) {
            _editor.makeWarrantMenu(true, false);
        }
    }

    protected String checkForPortals(@Nonnull OBlock block, String key) {
        List<Portal> portals = block.getPortals();
        if (portals.isEmpty()) {
            return Bundle.getMessage("needPortal", block.getDisplayName(), Bundle.getMessage(key));
        } else {
            for (Portal portal : portals) {
                if (portal.getToBlock() == null || portal.getFromBlock() == null) {
                    return  Bundle.getMessage("portalNeedsBlock", portal.getName());
                }
            }
            for (Portal portal : portals) {
                if (!block.equals(portal.getToBlock()) && !block.equals(portal.getFromBlock())) {
                    return Bundle.getMessage("portalNotInCircuit", portal.getName(), block.getDisplayName());
                }
            }
        }
        return null;
    }

    /**
     * Check that there is at least one PortalIcon
     * @param block check icons of this block
     * @param key properties key
     * @return true if at least one PortalIcon found
     */
    protected String checkForPortalIcons(@Nonnull OBlock block, String key) {
        List<Portal> portals = block.getPortals();
        for (Portal portal : portals) {
            List<PortalIcon> iconMap = getPortalIconMap(portal);
            if (iconMap.isEmpty()) {
                return Bundle.getMessage("noPortalIcon", portal.getName(), Bundle.getMessage(key));
            } else {
                for (PortalIcon icon : iconMap) {
                    Portal p = icon.getPortal();
                    if (p == null) {
                        deletePortalIcon(icon);
                    } else if (_editFrame instanceof EditPortalFrame) {
                        return ((EditPortalFrame)_editFrame).checkPortalIconForUpdate(icon, false);
                    } 
                }
            }
        }
        // block has pPortals
        boolean ok = false;
        List<Positionable> list = getCircuitIcons(block);
        if (!list.isEmpty()) {
            for (Positionable pos : list) {
                if ((pos instanceof PortalIcon)) {
                    ok = true;
                }
            }
        }
        if (!ok) {
            return Bundle.getMessage("needPortalIcons", block.getDisplayName(), Bundle.getMessage(key));
        }
        return null;
    }

    protected String checkForTrackIcons(@Nonnull OBlock block, String key) {
        String msg = null;
        List<Positionable> list = getCircuitIcons(block);
        if (list.isEmpty()) {
            msg = Bundle.getMessage("needIcons", block.getDisplayName(), Bundle.getMessage(key));
        } else {
            boolean ok = true;
            for (Positionable p : list) {
                PositionableLabel pos = (PositionableLabel) p;
                if (CircuitBuilder.isUnconvertedTrack(pos)) {
                    ok = false;
                    break;
                }
            }
            if (!ok) {
                StringBuilder sb = new StringBuilder(Bundle.getMessage("cantSaveIcon", block.getDisplayName()));
                sb.append("\n");
                sb.append(Bundle.getMessage("needIcons", block.getDisplayName(), Bundle.getMessage(key)));
                msg = sb.toString();
            }
        }
        return msg;
    }

    protected void deletePortalIcon(PortalIcon icon) {
        if (log.isDebugEnabled()) {
            log.debug("deletePortalIcon: " + icon.getName());
        }
        Portal portal = icon.getPortal();
        if (portal != null) {
            getCircuitIcons(portal.getToBlock()).remove(icon);
            getCircuitIcons(portal.getFromBlock()).remove(icon);
            getPortalIconMap(portal).remove(icon);
        }
        List<Positionable> selections = _editor.getSelectionGroup();
        if (selections != null) {
            _editor.getSelectionGroup().remove(icon);
        }
        _editor.repaint();
    }

    /**
      * Check if the block being edited has all its track icons converted to indicator icons
     * If icons need conversion. ask if user wants to convert them
     * @param block OBlock to check
     * @param key properties key
     * @return true if all track icons are IndicatorTrack icons
     */
    protected boolean queryConvertTrackIcons(@Nonnull OBlock block, String key) {
        // since iconList will be modified, use a copy to find unconverted icons
        ArrayList<Positionable> list = new ArrayList<>(getCircuitIcons(block));
        String msg = null;
        if (list.isEmpty()) {
            msg = Bundle.getMessage("needIcons", block.getDisplayName(), Bundle.getMessage(key));
        } else {
            for (Positionable p : list) {
                PositionableLabel pos = (PositionableLabel) p;
                if (CircuitBuilder.isUnconvertedTrack(pos)) {
                    _editor.highlight(pos);
                    int result = JOptionPane.showConfirmDialog(_editFrame, Bundle.getMessage("notIndicatorIcon"),
                             Bundle.getMessage("incompleteCircuit"), JOptionPane.YES_NO_OPTION,
                             JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                         convertIcon(pos, block);
                    } else {
                         msg = Bundle.getMessage("cantSaveIcon", block.getDisplayName(), Bundle.getMessage(key));
                         _editor.highlight(null);
                    }
                }
            }
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(_editFrame, msg,
                    Bundle.getMessage("noIcons"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Converts icon to IndicatorTrack
     */
    private void convertIcon(PositionableLabel pos, OBlock block) {
        _editor.toFront();
        _editor.repaint();
        new ConvertDialog(this, pos, block);
        _editor.repaint();
    }


    //////////////// select - deselect track icons //////////
    /**
     * Select block's track icons for editing. filter for what icon types to show and highlight
     */
    private ArrayList<Positionable> makeSelectionGroup(OBlock block, boolean showPortal) {
        ArrayList<Positionable> group = new ArrayList<>();
        for (Positionable p : getCircuitIcons(block)) {
            if (p instanceof PortalIcon) {
                if (showPortal) {
                    ((PortalIcon) p).setStatus(PortalIcon.VISIBLE);
                    group.add(p);
                }
            } else if (!(p instanceof SignalMastIcon) && !(p instanceof SignalHeadIcon)) {
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
        } else if ((pos instanceof PortalIcon) ||
                (pos instanceof SignalMastIcon) || (pos instanceof SignalHeadIcon)) {
            return false;
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
        if (pos instanceof IndicatorTrack || (pos instanceof PortalIcon) ||
                (pos instanceof SignalMastIcon) || (pos instanceof SignalHeadIcon)) {
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
                        getCircuitIcons(block).remove(pos);
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
        return _editFrame != null;
    }

    /**
     * If CircuitBuilder is in progress, restore what editor nulls.
     *
     * @param event     the triggering event
     * @param selection ignored
     * @return true if the selection group is restored; false otherwise
     */
    protected boolean doMousePressed(MouseEvent event, Positionable selection) {
        if (_editFrame != null) {
            _editFrame.toFront();
            _editor.setSelectionGroup(_saveSelectionGroup);
        } else {
            return false;
        }
        return true;
    }

    public boolean doMouseReleased(Positionable selection, boolean dragging) {
        if (_editFrame != null) {
            if (_editFrame instanceof EditPortalFrame) {
                if (selection instanceof PortalIcon && getCircuitIcons(_editFrame._homeBlock).contains(selection)) {
                    if (dragging) {
                        ((EditPortalFrame)_editFrame).checkPortalIconForUpdate((PortalIcon) selection, true);
                    } else {
                        ((EditPortalFrame)_editFrame).setSelected((PortalIcon)selection);
                    }
                }
            }
            return true;
        }
        return false;
    }

    // Return true if CircuitBuilder is editing
    protected boolean doMouseClicked(List<Positionable> selections, MouseEvent event) {
        if (_editFrame != null) {
            if (selections != null && selections.size() > 0) {
                ArrayList<Positionable> tracks = new ArrayList<>();
                Iterator<Positionable> iter = selections.iterator();
                if (_editFrame instanceof EditCircuitFrame) {
                    while (iter.hasNext()) {
                        Positionable pos = iter.next();
                        if (isTrack(pos)) {
                            tracks.add(pos);
                        }
                    }
                } else if (_editFrame instanceof EditCircuitPaths) {
                    while (iter.hasNext()) {
                        Positionable pos = iter.next();
                        if (isTrack(pos) || pos instanceof PortalIcon) {
                            tracks.add(pos);
                        }
                    }
                } else if (_editFrame instanceof EditSignalFrame) {
                    while (iter.hasNext()) {
                        Positionable pos = iter.next();
                        if (pos instanceof PortalIcon || pos instanceof SignalMastIcon || pos instanceof SignalHeadIcon) {
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
                    if (_editFrame instanceof EditCircuitPaths && event.isShiftDown() && !event.isControlDown()) {
                        selection.doMouseClicked(event);
                    }
                    handleSelection(selection, event);
                }
                _editFrame.toFront();
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
        if (_editFrame != null) {
            if (selection instanceof PortalIcon) {
                if (_editFrame instanceof EditPortalFrame) {
                    ((EditPortalFrame)_editFrame).setSelected((PortalIcon)selection);
                    return false;  // OK to drag portal icon
                } else if (_editFrame instanceof EditPortalDirection) {
                    return false;  // OK to drag portal arrow
                }
            } if (selection instanceof SignalMastIcon || selection instanceof SignalHeadIcon) {
                if (_editFrame instanceof EditSignalFrame) {
                    return false;  // OK to drag signal
                }
            }
            return true;     // no dragging when editing
        }
        return false;
    }

    /**
     * Second call needed to only drag the portal icon and not entire selection
     *
     * @return true if portal frame is open
     */
    public boolean dragPortal() {
        return (_editFrame instanceof EditPortalFrame || _editFrame instanceof EditPortalDirection);
    }

    /*
     * For the param, selection, Add to or delete from selectionGroup.
     * If not there, add.
     * If there, delete.
     */
    private void handleSelection(Positionable selection, MouseEvent event) {
        if (_editFrame == null) {
            return;
        }
        if (_editFrame instanceof EditCircuitFrame) {
            EditCircuitFrame editCircuitFrame = (EditCircuitFrame)_editFrame;
            ArrayList<Positionable> selectionGroup = _editor.getSelectionGroup();
            if (selectionGroup == null) {
                selectionGroup = new ArrayList<>();
            }
            if (selectionGroup.contains(selection)) {
                selectionGroup.remove(selection);
            } else if (okToAdd(selection, editCircuitFrame._homeBlock)) {
                selectionGroup.add(selection);
            }
            editCircuitFrame.updateIconList(selectionGroup);
            _editor.setSelectionGroup(selectionGroup);
        } else if (_editFrame instanceof EditCircuitPaths) {
            EditCircuitPaths editPathsFrame = (EditCircuitPaths)_editFrame;
            editPathsFrame.updateSelections(!event.isShiftDown(), selection);
        } else if (_editFrame instanceof EditPortalFrame) {
            EditPortalFrame editPortalFrame = (EditPortalFrame)_editFrame;
            if (selection instanceof PortalIcon) {
                editPortalFrame.setSelected((PortalIcon)selection);
            }
        } else if (_editFrame instanceof EditPortalDirection) {
            EditPortalDirection editDirectionFrame = (EditPortalDirection)_editFrame;
            if (selection instanceof PortalIcon) {
                editDirectionFrame.setPortalIcon((PortalIcon)selection, true);
            }
        } else if (_editFrame instanceof EditSignalFrame) {
            EditSignalFrame editSignalFrame = (EditSignalFrame)_editFrame;
            editSignalFrame.setSelected((PositionableIcon)selection);
        }
    }

    protected void closeCBWindow() {
        if (_cbFrame !=null) {
            _cbFrame.dispose();
        }
    }
    
    static int NONE = 0;
    static int OBLOCK = 1;
    static int PORTAL = 2;
    static int OPATH = 3;
    static int ARROW = 4;
    static int SIGNAL = 5;
    class CBFrame extends jmri.util.JmriJFrame implements ListSelectionListener  {

        ButtonGroup _buttonGroup = new ButtonGroup();
        int _which = 0;
        OBlock selectedBlock = null;
        
        CBFrame(String title) {
            super(false, false);
            setTitle(title);
            addHelpMenu("package.jmri.jmrit.display.CircuitBuilder", true);
            
            _blockTable.getSelectionModel().addListSelectionListener(this);

            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            javax.swing.border.Border padding = BorderFactory.createEmptyBorder(10, 5, 4, 5);
            contentPane.setBorder(padding);

            JPanel panel0 = new JPanel();
            panel0.setLayout(new BoxLayout(panel0, BoxLayout.X_AXIS));
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JRadioButton b = makeButton("newCircuitItem", NONE);
            panel.add(b);
            panel.add(makeButton("editCircuitItem", OBLOCK));
            panel.add(makeButton("editPortalsItem", PORTAL));
            panel.add(makeButton("editCircuitPathsItem", OPATH));
            panel.add(makeButton("editDirectionItem", ARROW));
            panel.add(makeButton("editSignalItem", SIGNAL));
            b.setSelected(true);
            panel0.add(panel);

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JScrollPane(_blockTable));
            panel0.add(panel);
            contentPane.add(panel0);

            panel0 = new JPanel();
            panel0.setLayout(new BoxLayout(panel0, BoxLayout.X_AXIS));
            panel = new JPanel();
            JButton button = new JButton(Bundle.getMessage("ButtonOpen"));
            button.addActionListener((ActionEvent event) -> {
                if (editingOK()) {
                    _currentBlock = selectedBlock;
                    if (_which == NONE) {
                        newCircuit();
                    } else if (_which == OBLOCK) {
                        editCircuit("editCircuitItem", false);
                    } else if (_which == PORTAL) {
                        editPortals("editPortalsItem", false);
                    } else if (_which == OPATH) {
                        editCircuitPaths("editCircuitPathsItem", false);
                    } else if (_which == ARROW) {
                        editPortalDirection("editDirectionItem", false);
                    } else if (_which == SIGNAL) {
                        editSignalFrame("SignalTitle", false);
                    }
                }
            });
            panel.add(button);

            button = new JButton(Bundle.getMessage("ButtonDone"));
            button.addActionListener((ActionEvent a) -> {
                _currentBlock = null;
                this.dispose();
            });
            panel.add(button);
            panel.setMaximumSize(new Dimension(300, panel.getPreferredSize().height));
            panel0.add(panel);
            contentPane.add(panel0);

            setContentPane(contentPane);
            _blockTable.clearSelection();
            pack();
            setLocation(jmri.util.PlaceWindow.nextTo(_editor, null, this));
            setVisible(true);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
//            doOpenAction();
            int row = _blockTable.getSelectedRow();
            if (row >= 0) {
                row = _blockTable.convertRowIndexToModel(row);
                selectedBlock = _oblockModel.getBeanAt(row);
            } else {
                selectedBlock = null;
            }
        }

        JRadioButton makeButton(String title, int which) {
            JRadioButton button = new JRadioButton(Bundle.getMessage(title));
            button.addActionListener((ActionEvent event) -> {
                _which = which;
            });
            _buttonGroup.add(button);
            return button;
        }

        @Override
        public void dispose() {
            _cbFrame = null;
            super.dispose();
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
