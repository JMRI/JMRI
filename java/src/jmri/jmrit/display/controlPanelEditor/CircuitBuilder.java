package jmri.jmrit.display.controlPanelEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;

import jmri.jmrit.display.*;
import jmri.jmrit.display.Editor.TargetPane;
import jmri.jmrit.display.palette.IndicatorItemPanel;
import jmri.jmrit.display.palette.IndicatorTOItemPanel;
import jmri.jmrit.picker.PickListModel;

import java.awt.*;
import java.awt.event.*;
//import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.*;

import jmri.jmrit.display.Editor;
import jmri.NamedBeanHandle;
//import jmri.jmrit.catalog.NamedIcon;

import jmri.jmrit.logix.*;
import jmri.util.JmriJFrame;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2011
 * @version $Revision: 21062 $
 * 
 */

public class CircuitBuilder  {

    static int STRUT_SIZE = 10;

    private JMenu _circuitMenu;
    private JMenu _todoMenu;
    
    // map track icon to OBlock to which it belongs
    private HashMap<Positionable, OBlock> _iconMap = new HashMap<Positionable, OBlock>();

    // map OBlock to List of icons that represent it
    private HashMap<OBlock, ArrayList<Positionable>> _circuitMap;
    
    // list of track icons not belonging to an OBlock
    private ArrayList<Positionable> _darkTrack;
    
    // list of OBlocks with no icons
    private ArrayList<OBlock> _bareBlock;
    
    // list of icons needing converting
    private ArrayList<Positionable> _unconvertedTrack;
    
    // list of OBlocks whose icons need converting
    private ArrayList<OBlock> _convertBlock;
    
    // map of Portals without PortalIcons or misplaced icons
    private HashMap<String, Portal> _badPortalIcon;
    
    // map of PortalIcons by portal name
    private HashMap<String, PortalIcon> _portalIconMap;
    
    // map of Portals by portal name
    private HashMap<String, Portal> _portalMap;
    
    // OBlock list to open edit frames 
    private PickListModel _oblockModel;
    private boolean hasOBlocks = false;

    // "Editing Frames" - Called from menu in Main Frame
    private EditCircuitFrame _editCircuitFrame;
    private EditPortalFrame  _editPortalFrame; 
    private EditCircuitPaths _editPathsFrame;
    private EditPortalDirection _editDirectionFrame; 

    // list of icons making a circuit - used by editing frames to indicate block(s) being worked on
    private ArrayList<Positionable> _circuitIcons;      // Dark Blue

    private JTextField _sysNameBox = new JTextField();
    private JTextField _userNameBox = new JTextField();
    private OBlock  _currentBlock;
    private JDialog _dialog;
    protected ControlPanelEditor _editor;

//    public final static ResourceBundle rbcp = ControlPanelEditor.rbcp;
    public final static Color _editGroupColor = new Color(100, 200, 255);
    public final static Color _pathColor = Color.green;
    public final static Color _highlightColor = new Color(255, 100, 200);
    
    /******************************************************************/

    public CircuitBuilder() {
//        _menuBar = new JMenuBar();
        log.error("CircuitBuilder ctor requires an Editor class");
    }

    public CircuitBuilder(ControlPanelEditor ed) {
         _editor = ed;
    }

    /**
     * Makes menu for ControlPanelEditor
     * Called by ControlPanelEditor at init before contents have been loaded
     */
    protected JMenu makeMenu() {
        _circuitMenu = new JMenu(Bundle.getMessage("CircuitBuilder"));
        _circuitMap = new HashMap<OBlock, ArrayList<Positionable>>();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            OBlock block = manager.getBySystemName(sysNames[i]);
            _circuitMap.put(block, new ArrayList<Positionable>());
        }
        // delay error detection until ControlPanelEditor is fully loaded
        JMenuItem mi = new JMenuItem (Bundle.getMessage("circuitErrorsItem"));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                checkCircuits();
            }
        });
       makeCircuitMenu();
       _circuitMenu.add(mi);
       return _circuitMenu;
    }
        
    /**
    * Add icon 'pos' to circuit 'block'
    */
    private void addIcon(OBlock block, Positionable pos) {
    	ArrayList<Positionable> icons = _circuitMap.get(block);
        if (icons==null) {
            icons = new ArrayList<Positionable>();
        }
        if (pos!=null) {
            if (!icons.contains(pos)) {
                icons.add(pos);
            }
            _iconMap.put(pos, block);
        }
        _circuitMap.put(block, icons);
        _darkTrack.remove(pos);
        if (log.isDebugEnabled()) log.debug("addIcon: block "+block.getDisplayName()+" has "+icons.size()+" icons.");
    }

    // display "todo" (Error correction) items
    private void makeToDoMenu() {
        _todoMenu.removeAll();

        JMenu blockNeeds = new JMenu(Bundle.getMessage("blockNeedsIconsItem"));
        _todoMenu.add(blockNeeds);
        ActionListener editCircuitAction = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    String sysName = event.getActionCommand();
                    editCircuitError(sysName);
                }
        };
        if (_bareBlock.size()>0) {
            for (int i=0; i<_bareBlock.size(); i++) {
                OBlock block = _bareBlock.get(i);
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        Bundle.getMessage("OpenCircuitItem"), block.getDisplayName()));
                mi.setActionCommand(block.getSystemName());
                mi.addActionListener(editCircuitAction);
                blockNeeds.add(mi);                                                  
            }
        } else {
        	if (hasOBlocks) {
        		blockNeeds.add(new JMenuItem(Bundle.getMessage("circuitsHaveIcons")));
        	} else {
        		blockNeeds.add(new JMenuItem(Bundle.getMessage("noTrackCircuits")));        		
        	}
        }

        blockNeeds = new JMenu(Bundle.getMessage("blocksNeedConversionItem"));
        _todoMenu.add(blockNeeds);
        if (_convertBlock.size()>0) {
            for (int i=0; i<_convertBlock.size(); i++) {
                OBlock block = _convertBlock.get(i);
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        Bundle.getMessage("OpenCircuitItem"), block.getDisplayName()));
                mi.setActionCommand(block.getSystemName());
                mi.addActionListener(editCircuitAction);
                blockNeeds.add(mi);                                                  
            }
        } else {
        	if (hasOBlocks) {
                blockNeeds.add(new JMenuItem(Bundle.getMessage("circuitIconsConverted")));
        	} else {
        		blockNeeds.add(new JMenuItem(Bundle.getMessage("noTrackCircuits")));        		
        	}
        }
        JMenuItem iconNeeds;
        if (_unconvertedTrack.size()>0) {
            iconNeeds = new JMenuItem(Bundle.getMessage("iconsNeedConversionItem"));
            iconNeeds.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                	ArrayList<Positionable> group = new ArrayList<Positionable>();
                    for (int i=0; i<_unconvertedTrack.size(); i++) {
                    	group.add(_unconvertedTrack.get(i));
                    }
                    _editor.setSelectionGroup(group);
                }
             });
        } else {
            iconNeeds = new JMenuItem(Bundle.getMessage("IconsConverted"));
        }
        _todoMenu.add(iconNeeds);

        if (_darkTrack.size()>0) {
            iconNeeds = new JMenuItem(Bundle.getMessage("iconsNeedsBlocksItem"));
            iconNeeds.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                	ArrayList<Positionable> group = new ArrayList<Positionable>();
                    for (int i=0; i<_darkTrack.size(); i++) {
                    	group.add(_darkTrack.get(i));
                    }
                    _editor.setSelectionGroup(group);
                }
             });
        } else {
        	if (hasOBlocks) {
                iconNeeds = new JMenuItem(Bundle.getMessage("IconsHaveCircuits"));
        	} else {
        		iconNeeds = new JMenuItem(Bundle.getMessage("noTrackCircuits"));        		
        	}
        }
        _todoMenu.add(iconNeeds);

        blockNeeds = new JMenu(Bundle.getMessage("portalsMisplaced"));
        _todoMenu.add(blockNeeds);
        ActionListener portalCircuitAction = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    String portalName = event.getActionCommand();
                    portalCircuitError(portalName);
                }
        };
        if (_badPortalIcon.size()>0) {
        	Iterator<String> it = _badPortalIcon.keySet().iterator();
        	while (it.hasNext()) {
        		String portalName = it.next();
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        Bundle.getMessage("OpenPortalTitle"), portalName));
                mi.setActionCommand(portalName);
                mi.addActionListener(portalCircuitAction);
                blockNeeds.add(mi);                                                  
            }
        } else {
        	if (hasOBlocks) {
                blockNeeds.add(new JMenuItem(Bundle.getMessage("portalsInPlace")));
        	} else {
        		blockNeeds.add(new JMenuItem(Bundle.getMessage("noTrackCircuits")));        		
        	}
        }

        JMenuItem pError = new JMenuItem(Bundle.getMessage("CheckPortalPaths"));
        _todoMenu.add(pError);
        pError.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    errorCheck();
                }
            });

    }

    private void errorCheck() {
        WarrantTableAction.initPathPortalCheck();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            WarrantTableAction.checkPathPortals(manager.getBySystemName(sysNames[i]));
        }
        if (!WarrantTableAction.showPathPortalErrors()) {
	        JOptionPane.showMessageDialog(_editCircuitFrame,
                    Bundle.getMessage("blocksEtcOK"), Bundle.getMessage("OK"),
					javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    
    private void makeCircuitMenu() {
        _circuitMenu.removeAll();

        JMenuItem circuitItem = new JMenuItem(Bundle.getMessage("newCircuitItem"));
        _circuitMenu.add(circuitItem);
        circuitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    newCircuit();
                }
            });

        JMenuItem editCircuitItem = new JMenuItem(Bundle.getMessage("editCircuitItem"));
        _circuitMenu.add(editCircuitItem);
        JMenuItem editPortalsItem = new JMenuItem(Bundle.getMessage("editPortalsItem"));
        _circuitMenu.add(editPortalsItem);
        JMenuItem editCircuitPathsItem = new JMenuItem(Bundle.getMessage("editCircuitPathsItem"));
        _circuitMenu.add(editCircuitPathsItem);
        JMenuItem editDirectionItem = new JMenuItem(Bundle.getMessage("editDirectionItem"));
        _circuitMenu.add(editDirectionItem);

        if ( _circuitMap.keySet().size()>0) {
            editCircuitItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        editCircuit("editCircuitItem");
                    }
                });
            editPortalsItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        editPortals("editPortalsItem");
                    }
                });
            editCircuitPathsItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        editCircuitPaths("editCircuitPathsItem");
                    }
                });
            editDirectionItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                	editPortalDirection("editDirectionItem");
                }
            });
        } else {
            editCircuitItem.add(new JMenuItem(Bundle.getMessage("noCircuitsItem")));
            editPortalsItem.add(new JMenuItem(Bundle.getMessage("noCircuitsItem")));
            editCircuitPathsItem.add(new JMenuItem(Bundle.getMessage("noCircuitsItem")));
            editDirectionItem.add(new JMenuItem(Bundle.getMessage("noCircuitsItem")));
        }
    }

    /**************** Set up editing Frames *****************/

    protected void newCircuit() {
        if (editingOK()) {
            addCircuitDialog();
            if (_currentBlock!=null) {
                if (_editCircuitFrame==null) {
                    checkCircuits();
                	_editor.setSelectionGroup(null);
                    _editor.disableMenus();
                    TargetPane targetPane = (TargetPane)_editor.getTargetPanel();
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
            if (_currentBlock!=null) {
                 checkCircuits();
                 _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, false));
                _editor.disableMenus();
                TargetPane targetPane = (TargetPane)_editor.getTargetPanel();
                targetPane.setSelectGroupColor(_editGroupColor);
                targetPane.setHighlightColor(_editGroupColor);
                _editCircuitFrame = new EditCircuitFrame(Bundle.getMessage("OpenCircuitItem"), this, _currentBlock);
            }
        }
    }

    private void editCircuitError(String sysName) {
        if (editingOK()) {
            _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBySystemName(sysName);
            if (_currentBlock!=null) {
                checkCircuits();
                _editor.setSelectionGroup(makeSelectionGroup(_currentBlock, false));
                _editor.disableMenus();
                _editCircuitFrame = new EditCircuitFrame(Bundle.getMessage("OpenCircuitItem"), this, _currentBlock);
            }
        }
    }

    protected void editPortals(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock!=null) {
                checkCircuits();
                _circuitIcons = _circuitMap.get(_currentBlock);
                // check icons to be indicator type
                if (iconsConverted(_currentBlock)) {
                	_editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                	_editor.disableMenus();
                    TargetPane targetPane = (TargetPane)_editor.getTargetPanel();
                    targetPane.setSelectGroupColor(_editGroupColor);
                    targetPane.setHighlightColor(_highlightColor);
                   _editPortalFrame = new EditPortalFrame(Bundle.getMessage("OpenPortalTitle"), this, _currentBlock, false);
                }
            }

        }
    }

    private void portalCircuitError(String portalName) {
        if (editingOK()) {
        	Portal portal = _portalMap.get(portalName);
        	_currentBlock = portal.getToBlock();
        	OBlock adjacentBlock = null;
        	if (_currentBlock==null) {
        		_currentBlock = portal.getFromBlock();
        	} else {
        		adjacentBlock = portal.getFromBlock();
        	}
            if (_currentBlock!=null) {
                // checkCircuits();
                // check icons to be indicator type
                _circuitIcons = _circuitMap.get(_currentBlock);
                if (iconsConverted(_currentBlock)&& iconsConverted(adjacentBlock)) {
                	_editor.setSelectionGroup(makeSelectionGroup(_currentBlock, false));
                	_editor.setSecondSelectionGroup(makeSelectionGroup(adjacentBlock, false));
                	_editor.disableMenus();
                    TargetPane targetPane = (TargetPane)_editor.getTargetPanel();
                    targetPane.setSelectGroupColor(_editGroupColor);
                    targetPane.setHighlightColor(_highlightColor);
                    PortalIcon icon = _portalIconMap.get(portalName);
                    if (icon!=null) {
                    	icon.setStatus(PortalIcon.VISIBLE);
                    }
                   _editPortalFrame = new EditPortalFrame(Bundle.getMessage("OpenPortalTitle"), this, 
                		   _currentBlock, portal, adjacentBlock);
                }
            }
        }
    }
    
    protected void editPortalDirection(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock!=null) {
                checkCircuits();
                _circuitIcons = _circuitMap.get(_currentBlock);
                // check icons to be indicator type
                if (iconsConverted(_currentBlock)) {
                	_editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                	_editor.disableMenus();
                    TargetPane targetPane = (TargetPane)_editor.getTargetPanel();
                    targetPane.setSelectGroupColor(_editGroupColor);
                    targetPane.setHighlightColor(_highlightColor);
                   _editDirectionFrame = new EditPortalDirection(Bundle.getMessage("OpenDirectionTitle"), this, _currentBlock);
                }
            }

        }
    }

    protected void editCircuitPaths(String title) {
        if (editingOK()) {
            editCircuitDialog(title);
            if (_currentBlock!=null) {
                checkCircuits();
                // check icons to be indicator type
                _circuitIcons = _circuitMap.get(_currentBlock);
                if (iconsConverted(_currentBlock)) {
                	_editor.setSelectionGroup(makeSelectionGroup(_currentBlock, true));
                    _currentBlock.setState(OBlock.UNOCCUPIED);
                    // A temporary path "TEST_PATH" is used to display the icons representing a path
                    _currentBlock.allocate(EditCircuitPaths.TEST_PATH);
                    _editor.disableMenus();
                    TargetPane targetPane = (TargetPane)_editor.getTargetPanel();
                    targetPane.setSelectGroupColor(_editGroupColor);
                    targetPane.setHighlightColor(_editGroupColor);
                    _editPathsFrame = new EditCircuitPaths(Bundle.getMessage("OpenPathTitle"), this, _currentBlock);
                }
            }
        }
    }

    private void hidePortalIcons() {
        Iterator<PortalIcon> it = _portalIconMap.values().iterator();
        while (it.hasNext()) {
        	it.next().setStatus(PortalIcon.HIDDEN);
        }        	
    }

    private boolean editingOK() {
        if (_editCircuitFrame!=null || _editPathsFrame!=null || _editPortalFrame!=null || _editDirectionFrame!=null) {
			// Already editing a circuit, ask for completion of that edit
	        JOptionPane.showMessageDialog(_editCircuitFrame,
                    Bundle.getMessage("AlreadyEditing"), Bundle.getMessage("errorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
            if (_editPathsFrame!=null) {
                _editPathsFrame.toFront();
                _editPathsFrame.setVisible(true);
            } else if (_editCircuitFrame!=null) {
                _editCircuitFrame.toFront();
                _editCircuitFrame.setVisible(true);
            } else if (_editPortalFrame!=null) {
                _editPortalFrame.toFront();
                _editPortalFrame.setVisible(true);
            } else if (_editDirectionFrame!=null) {
            	_editDirectionFrame.toFront();
            	_editDirectionFrame.setVisible(true);
            }
            return false;
        }
        return true;
    }

    /**
    * Create a new OBlock
    * Used by New to set up _editCircuitFrame
    * Sets _currentBlock to created new OBlock
    */
    private void addCircuitDialog() {
        _dialog = new JDialog(_editor, Bundle.getMessage("TitleCircuitDialog"), true);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10,10));
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
        _dialog.setLocation(_editor.getLocation().x+100, _editor.getLocation().y+100);
        _dialog.pack();
        _dialog.setVisible(true);
    }

    /**
    * Edit existing OBlock
    * Used by edit to set up _editCircuitFrame
    * Sets _currentBlock to chosen OBlock or null if none selected
    */
    private void editCircuitDialog(String title) {
        _dialog = new JDialog(_editor, Bundle.getMessage(title), true);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10,10));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel p = new JPanel();
        p.add(new JLabel(Bundle.getMessage("selectOBlock")));
        mainPanel.add(p);

        _oblockModel = PickListModel.oBlockPickModelInstance();
        JTable table = _oblockModel.makePickTable();
        mainPanel.add(new JScrollPane(table));
        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        mainPanel.add(makeDoneButtonPanel(false));
        panel.add(mainPanel);
        _dialog.getContentPane().add(panel);
        _dialog.setLocation(_editor.getLocation().x+100, _editor.getLocation().y+100);
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
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(new JLabel(Bundle.getMessage("SystemName")), c);
        c.gridy = 1;
        p.add(new JLabel(Bundle.getMessage("UserName")),c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_sysNameBox,c);
        c.gridy = 1;
        p.add(_userNameBox,c);
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
            doneButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        if (doAddAction()) {
                            _dialog.dispose();
                        }
                    }
            });
        } else {
            doneButton = new JButton(Bundle.getMessage("ButtonOpenCircuit"));
            doneButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        if (doOpenAction()) {
                            _dialog.dispose();
                        }
                    }
            });
        }
        panel0.add(doneButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _sysNameBox.setText("");
                    _currentBlock = null;
                    _dialog.dispose();
                }
        });
        panel0.add(cancelButton);
        buttonPanel.add(panel0);
        return buttonPanel;
    }

    private boolean doAddAction() {
    	boolean retOK = false;
        String sysname = _sysNameBox.getText();
        if (sysname != null && sysname.length() > 1) {
            String uname = _userNameBox.getText();
            if (uname!=null && uname.trim().length()==0) {
                uname = null;
            }
            _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).createNewOBlock(sysname, uname);
            if (_currentBlock!=null) {
            	retOK = true;
            } else {
                int result = JOptionPane.showConfirmDialog(_editor, java.text.MessageFormat.format(
                                Bundle.getMessage("blockExists"), sysname), 
                                Bundle.getMessage("AskTitle"), JOptionPane.YES_NO_OPTION, 
                                JOptionPane.QUESTION_MESSAGE);
                if (result==JOptionPane.YES_OPTION) {
                    _currentBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBySystemName(sysname);
                    if (_currentBlock==null) {
                    	retOK = false;
                    }
                    checkCircuits();
                    _editor.setSelectionGroup(_circuitMap.get(_currentBlock));
                    _editCircuitFrame = new EditCircuitFrame(Bundle.getMessage("OpenCircuitItem"), this, _currentBlock);
                    retOK = true;
                }            }
        }
        if (!retOK) {
            JOptionPane.showMessageDialog(_editor, Bundle.getMessage("createOBlock"), 
                    Bundle.getMessage("NeedDataTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
        return retOK;
    }

    private boolean doOpenAction() {
        int row = _oblockModel.getTable().getSelectedRow();
        if (row >= 0) {
           _currentBlock = (OBlock)_oblockModel.getBeanAt(row);
           return true;
        } else {
            JOptionPane.showMessageDialog(_editor, Bundle.getMessage("selectOBlock"), 
                            Bundle.getMessage("NeedDataTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
        _currentBlock = null;
        return false;
    }
    /************************** end setup frames ******************************/

    /************************* Closing Editing Frames ********************/

    /**
    * Update block data in menus
    */
    protected void checkCircuitFrame(OBlock block) {
        if (block!=null) {
        	java.util.List<Positionable> group = _editor.getSelectionGroup();
            // check icons to be indicator type
            setIconGroup(block, group);
            iconsConverted(block);
        }
        closeCircuitFrame();
    }

    protected void closeCircuitFrame() {
        _editCircuitFrame = null;
        closeCircuitBuilder();
    }

    /**
    *  Edit frame closing, set block's icons
    */
    private void setIconGroup(OBlock block, java.util.List<Positionable> selections) {
    	java.util.List<Positionable> oldIcons = _circuitMap.get(block);
        if (oldIcons!=null) {
            for (int i=0; i<oldIcons.size(); i++) {
                Positionable pos = oldIcons.get(i);
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack)pos).setOccBlockHandle(null);
                }
                _iconMap.remove(pos);
            }
        }
        // the selectionGroup for all edit frames is full collection of icons
        // comprising the block.  Gather them and store in the block's hashMap
        ArrayList<Positionable> icons = new ArrayList<Positionable>();
        if (selections!=null) {
            if (log.isDebugEnabled()) log.debug("setIconGroup: selectionGroup has "+
            		selections.size()+" icons.");
            NamedBeanHandle<OBlock> handle = new NamedBeanHandle<OBlock>(block.getSystemName(), block);
            for (int i=0; i<selections.size(); i++) {
                Positionable pos = selections.get(i);
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack)pos).setOccBlockHandle(handle);
                }
                icons.add(pos);
                _iconMap.put(pos, block);
            }
            java.util.List<Portal> portals = block.getPortals();
            for (int i=0; i<portals.size(); i++) {
                PortalIcon icon = _portalIconMap.get(portals.get(i).getName());
                if (icon!=null) {
                    _iconMap.put(icon, block);
                }
            }
        }
        _circuitMap.put(block, icons);
        if (log.isDebugEnabled()) log.debug("setIconGroup: block "+block.getDisplayName()+
                                            " has "+icons.size()+" icons.");
    }

    protected void closePathFrame(OBlock block) {
        _currentBlock.deAllocate(null);
        _editPathsFrame = null;
        closeCircuitBuilder();
    }

    protected void closePortalFrame(OBlock block) {
        _editPortalFrame = null;
        _editor.setSecondSelectionGroup(null);
        closeCircuitBuilder();
    }
    protected void closePortalDirection(OBlock block) {
    	_editDirectionFrame = null;
        closeCircuitBuilder();
    }
    
    private void closeCircuitBuilder() {
        _circuitIcons = null;
        _currentBlock = null;
        checkCircuits();
        hidePortalIcons();
        _editor.resetEditor();
    }
    /**************** end closing frames ********************/
    /**
    * Find the blocks with no icons and the blocks with icons that need conversion
    * Setup for main Frame - used in both initialization and close of an editing frame
    * Build Lists that are used to create menu items
    */
    private void checkCircuits() {

        _portalIconMap = new HashMap<String, PortalIcon>();
        _darkTrack = new ArrayList<Positionable>();
        _unconvertedTrack = new ArrayList<Positionable>();
        
        Iterator<Positionable> it = _editor.getContents().iterator();
        while (it.hasNext()) {
            Positionable pos = it.next();
//            if (log.isDebugEnabled()) log.debug("class: "+pos.getClass().getName());
            if (pos instanceof IndicatorTrack) {
                OBlock block = ((IndicatorTrack)pos).getOccBlock();
                ((IndicatorTrack)pos).removePath(EditCircuitPaths.TEST_PATH);
                if (block!=null) {
                    addIcon(block, pos);
                } else {
                    _darkTrack.add(pos);
                }
            } else if (pos instanceof PortalIcon) {
                PortalIcon pIcon = (PortalIcon)pos;
                if(!_portalIconMap.containsKey(pIcon.getName())) {
                    _portalIconMap.put(pIcon.getName(), pIcon);
                }
            } else if (isUnconvertedTrack(pos)) {
                if (!_unconvertedTrack.contains(pos)) {
                    _unconvertedTrack.add(pos);
                }
            }
        }

        _bareBlock = new ArrayList<OBlock>();
        _convertBlock = new ArrayList<OBlock>();
        _portalMap = new HashMap<String, Portal>();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        hasOBlocks = (sysNames.length>0);
        for (int i = 0; i < sysNames.length; i++) {
            OBlock block = manager.getBySystemName(sysNames[i]);
            
            java.util.List<Portal> list = block.getPortals();
            if (list!=null) {
            	Iterator<Portal> iter = list.iterator();
            	while (iter.hasNext()) {
            		Portal portal = iter.next();
                    // make map of all Portals
            		String name = portal.getName();
                    _portalMap.put(name, portal);
                    // make circuitMap
                    PortalIcon pi = _portalIconMap.get(portal.getName());
                    if (pi!=null) {
                       addIcon(block, pi);
                    }              		
            	}
            }     
            java.util.List<Positionable> icons = _circuitMap.get(block);
            if (log.isDebugEnabled()) log.debug("checkCircuits: block "+block.getDisplayName()
                                                +" has "+icons.size()+" icons.");
            if (icons==null || icons.size()==0) {
                _bareBlock.add(block);
            } else {
                _bareBlock.remove(block);
                for (int k=0; k<icons.size(); k++) {
                    Positionable pos = icons.get(k);
                    if (!(pos instanceof IndicatorTrack) && !(pos instanceof PortalIcon)) {
                        if (!_convertBlock.contains(block)) {
                            _convertBlock.add(block);
                            break;
                        }
                    }
                }
            }
        }
        
        _badPortalIcon = new HashMap<String, Portal>();
        Iterator<Entry<String, Portal>> iter = _portalMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Portal> entry = iter.next();
            String name = entry.getKey();
        	PortalIcon pi = _portalIconMap.get(name);
           	Portal portal = entry.getValue();
           	if (pi!=null) {
             	OBlock b = portal.getToBlock();
            	OBlock c = portal.getFromBlock();
            	if (b==null || c==null) {
                	_badPortalIcon.put(name, portal);        		            		
            	} else {
                	if (!EditPortalFrame.portalIconOK(_circuitMap.get(b), pi, _circuitMap.get(c))) {
                      	_badPortalIcon.put(name, portal);        		            		
                	}           		
            	}
            } else {	// no icon for this Portal
            	_badPortalIcon.put(name, portal);        		
        	}
        }
        if (_todoMenu==null) {
        	_circuitMenu.remove(_circuitMenu.getItem(_circuitMenu.getItemCount()-1));
            _todoMenu =  new JMenu(Bundle.getMessage("circuitErrorsItem"));       	
            _circuitMenu.add(_todoMenu);
        }
        makeToDoMenu();
    }   // end checkCircuits

    /*************** Frame Utilities **************/
    
    /**
    * Used by Portal Frame
    */
    protected java.util.List<Positionable> getCircuitIcons(OBlock block) {
        return _circuitMap.get(block);
    }

    /**
    * Used by Portal Frame
    */
    protected OBlock getBlock(Positionable pos) {
        return _iconMap.get(pos);
    }

    /**
    * Used by Path Frame
    */
    protected java.util.List<Positionable> getCircuitGroup() {
        return _circuitIcons;
    }

    /**
    * Used by Portal Frame
    */    
    protected HashMap<String, PortalIcon> getPortalIconMap() {
        return _portalIconMap;
    }
    
    protected Portal getPortalByName(String name) {
    	return _portalMap.get(name);
    }
    
    protected void changePortalName(String oldName, String newName) {
    	PortalIcon icon = _portalIconMap.get(oldName);
    	Portal portal = _portalMap.get(oldName);
    	_portalMap.remove(oldName);
       	_portalIconMap.remove(oldName);
       	_portalMap.put(newName, portal);
     	_portalIconMap.put(newName, icon);
    }
    
    protected void removePortal(String name) {
    	_portalMap.remove(name);
    	_portalIconMap.remove(name);    	
    }
    
    protected void addPortalIcon(PortalIcon icon) {
    	//Portal portal = icon.getPortal();
    	String name = icon.getName();
        // Eliminate possible duplicate icons for this portal
        if(!_portalIconMap.containsKey(name)) {
        	_portalMap.put(name, icon.getPortal());
        	_portalIconMap.put(name, icon);
        }
        // 
    	if (_circuitIcons!=null) {
    		_circuitIcons.add(icon);
    	}
    }
    
    /**
    * Remove block, but keep the track icons. Set block reference in icon null
    */
    protected void removeBlock (OBlock block) {
        java.util.List<Positionable> list = _circuitMap.get(block);
        if (list!=null) {
            for (int i=0; i<list.size(); i++) {
                Positionable pos = list.get(i);
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack)pos).setOccBlockHandle(null);
                } else if (pos instanceof PortalIcon) {
                    pos.remove();
                }
                _darkTrack.add(pos);
            }
        }
//        InstanceManager.oBlockManagerInstance().deregister(block);
        _circuitMap.remove(block);
        block.dispose();
    }

    /***************** Overriden methods of Editor *******************

    public void paintTargetPanel(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        if (_circuitIcons!=null){
            java.awt.Stroke stroke = g2d.getStroke();
            Color color = g2d.getColor();
            g2d.setColor(_editGroupColor);
            g2d.setStroke(new java.awt.BasicStroke(2.0f));
            for(int i=0; i<_circuitIcons.size();i++){
                g.drawRect(_circuitIcons.get(i).getX(), _circuitIcons.get(i).getY(), 
                           _circuitIcons.get(i).maxWidth(), _circuitIcons.get(i).maxHeight());
            }
            g2d.setColor(color);
            g2d.setStroke(stroke);
        }
    }

/********************* convert plain track to indicator track **************/

    IndicatorItemPanel _trackPanel;
    IndicatorTOItemPanel _trackTOPanel;
    PositionableLabel _oldIcon;
    JmriJFrame _convertFrame;     // must be modal dialog to halt convetIcons loop
    JDialog _convertDialog;     // must be modal dialog to halt convetIcons loop

    /**
    * Check if the block being edited has all its icons converted to indicator icons
    */
    protected boolean iconsConverted(OBlock block) {
    	java.util.List<Positionable> list = _circuitMap.get(block);
        if (list!=null && list.size()>0) {
            for (int i=0; i<list.size(); i++) {
                Positionable pos = list.get(i);
                if (!(pos instanceof IndicatorTrack) && !(pos instanceof PortalIcon) ) {
                    int result = JOptionPane.showConfirmDialog(_editor, Bundle.getMessage("notIndicatorIcon"), 
                                    Bundle.getMessage("incompleteCircuit"), JOptionPane.YES_NO_OPTION, 
                                    JOptionPane.QUESTION_MESSAGE);
                    if (result==JOptionPane.YES_OPTION) {
                        convertIcons();
                        break;
                    } else {
                        JOptionPane.showMessageDialog(_editor, Bundle.getMessage("needConversion", block.getDisplayName()), 
                                Bundle.getMessage("noIcons"), JOptionPane.INFORMATION_MESSAGE);
                     return false;
                    }
                }
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(_editor, Bundle.getMessage("needIcons", block.getDisplayName()), 
                            Bundle.getMessage("noIcons"), JOptionPane.INFORMATION_MESSAGE);
        }
        return false;
    }

    protected void convertIcons() {
        // converting an icon will update the editor's selectionGroup list.  Thus make copy array
    	_circuitIcons = _editor.getSelectionGroup();
        if (_circuitIcons!=null) {
            Positionable[] group = new Positionable[_circuitIcons.size()];
            for (int i=0; i<_circuitIcons.size(); i++) {
                group[i] = _circuitIcons.get(i);
            }
            TargetPane targetPane = (TargetPane)_editor.getTargetPanel();
           
            targetPane.setHighlightColor(_highlightColor);
            for (int i=0; i<group.length; i++) {
                if (!(group[i] instanceof IndicatorTrack)) {
                    if (log.isDebugEnabled()) log.debug("convertIcons: #"+i+" pos= "+group[i].getClass().getName());
                    convertIcon(group[i]);
                }
            }
            _circuitMap.put(_currentBlock, _circuitIcons);	// _circuitIcons references _editor._selectionGroup
            targetPane.setHighlightColor(_editGroupColor);
            _editor.highlight(null);
        }
    }

    private void convertIcon(Positionable pos) {
        _oldIcon = (PositionableLabel)pos;
        _editor.highlight(_oldIcon);
        _editor.toFront();
        _editor.repaint();
        if (pos instanceof TurnoutIcon) {
            makePalettteFrame("IndicatorTO");
            _trackTOPanel = new IndicatorTOItemPanel(_convertFrame, "IndicatorTO", null, null, _editor);
 //           _convertDialog.add(_trackTOPanel);
            ActionListener updateAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    convertTO();
                }
            };
            _trackTOPanel.init(updateAction);
            _convertDialog.add(_trackTOPanel);
        } else {
            makePalettteFrame("IndicatorTrack");
            _trackPanel = new IndicatorItemPanel(_convertFrame, "IndicatorTrack", null, _editor);
//            _convertDialog.add(_trackPanel);
            ActionListener updateAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    convertSeg();
                }
            };
            _trackPanel.init(updateAction);
            _convertDialog.add(_trackPanel);
        }
        _convertDialog.pack();
        _convertDialog.setVisible(true);
        _editor.repaint();
    }

    private void makePalettteFrame(String title) {
    	jmri.jmrit.display.palette.ItemPalette.loadIcons(_editor);
    	_convertDialog = new JDialog(_editor, java.text.MessageFormat.format(
        		Bundle.getMessage("EditItem"), Bundle.getMessage(title)), true);
    	_convertFrame = new convertFrame(_convertDialog);

        _convertDialog.setLocationRelativeTo(_editor);
        _convertDialog.toFront();
    }

    /*
     * gimmick to get JDialog to re-layout contents and repaint 
     */
    static class convertFrame extends JmriJFrame {
    	JDialog _dialog;
    	convertFrame (JDialog dialog) {
    		super(false, false);
    		_dialog = dialog;
    	}
    	public void pack() {
    		super.pack();
    		_dialog.pack();
    	}
	}

    private void convertTO() {
        IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(_editor);
        t.setOccBlockHandle(new NamedBeanHandle<OBlock>(_currentBlock.getSystemName(), _currentBlock));       
        t.setTurnout( ((TurnoutIcon)_oldIcon).getNamedTurnout());
        t.setFamily(_trackTOPanel.getFamilyName());

        HashMap <String, HashMap <String, NamedIcon>> iconMap = _trackTOPanel.getIconMaps();
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
        t.setOccBlockHandle(new NamedBeanHandle<OBlock>(_currentBlock.getSystemName(), _currentBlock));       
        t.setFamily(_trackPanel.getFamilyName());

        HashMap<String, NamedIcon> iconMap = _trackPanel.getIconMap();
        if (iconMap!=null) {
            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                if (log.isDebugEnabled()) log.debug("key= "+entry.getKey());
                t.setIcon(entry.getKey(), new NamedIcon(entry.getValue()));
            }
        }
        t.setLevel(Editor.TURNOUTS);
        t.setScale(_oldIcon.getScale());
        t.rotate(_oldIcon.getDegrees());
        finishConvert(t);
    }

    private void finishConvert(Positionable pos) {
    	_circuitIcons.remove(_oldIcon);
        _oldIcon.remove();
        pos.setLocation(_oldIcon.getLocation());
        _editor.putItem(pos);
        _circuitIcons.add(pos);
        pos.updateSize();

        _oldIcon = null;
        _trackPanel = null;
        _trackTOPanel = null;
        _convertDialog.dispose();
        _convertDialog = null;
        _convertFrame = null;
    }
    /*************** end convert icons *******************/

/**************** select - deselect track icons ************************/

    /**
    * select block's track icons for editing -***could be _circuitMap.get(block) is sufficient
    */
    private ArrayList<Positionable> makeSelectionGroup(OBlock block, boolean showPortal) {
    	ArrayList<Positionable> group = new ArrayList<Positionable>();
    	List<Positionable> circuitIcons = _circuitMap.get(block);
    	Iterator<Positionable> iter = circuitIcons.iterator();
        while(iter.hasNext()) {
        	Positionable p = iter.next();
        	if (p instanceof PortalIcon) {
        		if (showPortal) {
                	((PortalIcon)p).setStatus(PortalIcon.VISIBLE);
                	group.add(p);        		        			
            	}
    		} else {
            	group.add(p);        		    			
    		}
        }
        return group;
    }

    protected boolean isTrack(Positionable pos) {
        if (pos instanceof IndicatorTrack) {
            return true;
        } else if (pos instanceof TurnoutIcon) {
            return true;
        } else if (pos instanceof PositionableLabel) {
            PositionableLabel pl = (PositionableLabel)pos;
            if (pl.isIcon()) {
                 NamedIcon icon = (NamedIcon)pl.getIcon();
                 if (icon!=null) {
                     String fileName = icon.getURL();
                     // getURL() returns Unix separatorChar= "/" even on windows
                     // so don't use java.io.File.separatorChar
                     if (fileName.contains("/track/") ||
                            (fileName.contains("/tracksegments/") && !fileName.contains("circuit"))) {
                         return true;
                     }
                 }
            }

        }
        return false;
    }

    private boolean isUnconvertedTrack(Positionable pos) {
        if (pos instanceof IndicatorTrack) {
            return false;
        } else  if (pos instanceof TurnoutIcon) {
            return true;
        } else if (pos instanceof PositionableLabel) {
            PositionableLabel pl = (PositionableLabel)pos;
            if (pl.isIcon()) {
                 NamedIcon icon = (NamedIcon)pl.getIcon();
                 if (icon!=null) {
                     String fileName = icon.getURL();
                     if (log.isDebugEnabled()) log.debug("isUnconvertedTrack Test: url= "+fileName);
                     // getURL() returns Unix separatorChar= "/" even on windows
                     // so don't use java.io.File.separatorChar
                     if ( (fileName.contains("/track/") || fileName.contains("/tracksegments/"))
                                 && (fileName.indexOf("circuit")<0) ) {
                         return true;
                     }
                 }
            }

        }
        return false;
    }

    /**
    * Can a path in this circuit be drawn through this icon? 
    */
    private boolean okPath(Positionable pos, OBlock block) {
        java.util.List<Positionable> icons = _circuitMap.get(block);
        if (pos instanceof PortalIcon) {
            Portal portal = ((PortalIcon)pos).getPortal();
            if (portal!=null) {
                if (block.equals(portal.getFromBlock()) || block.equals(portal.getToBlock())) {
                    ((PortalIcon)pos).setStatus(PortalIcon.PATH);
                    return true;
                }
            }
            JOptionPane.showMessageDialog(_editor, java.text.MessageFormat.format(
                                Bundle.getMessage("portalNotInCircuit"),block.getDisplayName()), 
                            Bundle.getMessage("badPath"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!icons.contains(pos)) {
            JOptionPane.showMessageDialog(_editor, java.text.MessageFormat.format(
                                Bundle.getMessage("iconNotInCircuit"),block.getDisplayName()), 
                            Bundle.getMessage("badPath"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
    * Can this track icon be added to the circuit?
    * N.B. Be sure Positionable pos passes isTrack() call
    */
    private boolean okToAdd(Positionable pos, OBlock editBlock) {
        if (pos instanceof IndicatorTrack) {
            OBlock block = ((IndicatorTrack)pos).getOccBlock();
            if (block!=null) {
                if (!block.equals(editBlock)) {
                    int result = JOptionPane.showConfirmDialog(_editor, java.text.MessageFormat.format(
                                        Bundle.getMessage("iconBlockConflict"), 
                                        block.getDisplayName(), editBlock.getDisplayName()),
                                    Bundle.getMessage("whichCircuit"), JOptionPane.YES_NO_OPTION, 
                                    JOptionPane.QUESTION_MESSAGE);
                    if (result==JOptionPane.YES_OPTION) {
                        // move icon from block to editBlock 
                        java.util.List<Positionable> ic = _circuitMap.get(block);
                        ic.remove(pos);
                        ((IndicatorTrack)pos).setOccBlockHandle(
                                new NamedBeanHandle<OBlock>(editBlock.getSystemName(), editBlock));
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**************************** Mouse *************************/

    ArrayList<Positionable> _saveSelectionGroup;
    /**
    * Keep selections when editing.  Restore what editor nulls
    */
    protected void saveSelectionGroup(ArrayList<Positionable> selectionGroup) {
    	_saveSelectionGroup = selectionGroup;
    }
    
    protected void doMousePressed(MouseEvent event, Positionable selection) {
    	if (_editCircuitFrame!=null) {
    		_editCircuitFrame.toFront();
            _editor.setSelectionGroup(_saveSelectionGroup);
    	} else if (_editPathsFrame!=null) {
    		_editPathsFrame.toFront();
            _editor.setSelectionGroup(_saveSelectionGroup);
    	} else if (_editPortalFrame!=null) {
    		_editPortalFrame.toFront();
            _editor.setSelectionGroup(_saveSelectionGroup);
            _editPortalFrame.setSelection(selection);
    	} else if (_editDirectionFrame!=null) {
    		_editDirectionFrame.toFront();
            _editor.setSelectionGroup(_saveSelectionGroup);
    	}
    }
   
    public boolean doMouseReleased(Positionable selection, MouseEvent event) {
        if (_editCircuitFrame!=null || _editPathsFrame!=null || _editDirectionFrame!=null) {
        	return true;
        } else if (_editPortalFrame!=null) {
        	if (selection instanceof PortalIcon) {
            	_editPortalFrame.checkPortalIconForUpdate((PortalIcon)selection);	
        	}
        	return true;
        }
        return false;
    }

    protected boolean doMouseClicked(Positionable selection, MouseEvent event) {
        if (_editCircuitFrame!=null || _editPathsFrame!=null || (_editPortalFrame!=null) || _editDirectionFrame!=null) {
            if (_editPathsFrame!=null && event.isShiftDown()) {
            	selection.doMouseClicked(event);
            }
            if (!event.isPopupTrigger() && !event.isMetaDown() && !event.isAltDown()) {
            	handleSelection(selection, event);
            }
            return true;
        }
        return false;
    }

    /**
    * No dragging when editing
    */
    public boolean doMouseDragged(Positionable selection, MouseEvent event) {
        if (_editCircuitFrame!=null || _editPathsFrame!=null || _editDirectionFrame!=null) {
            return true;     // no dragging when editing
        }
        if (_editPortalFrame!=null) {
            if (selection instanceof PortalIcon) {
                return false;		// OK to drag portal icon
            } else {
            	return true;
            }
        }
        return false;
    }
    /**
     * Second call needed to only drag the portal icon and not entire selection
     * @return true if portal frame is open
     */
    public boolean dragPortal() {
    	return (_editPortalFrame!=null);
    }

    /*
    * For the param, selection, Add to or delete from selectionGroup. 
    * If not there, add.
    * If there, delete.
    */
    private void handleSelection(Positionable selection, MouseEvent event) {
        if (_editCircuitFrame!=null) {
        	ArrayList<Positionable> selectionGroup = _editor.getSelectionGroup();
            if (isTrack(selection)) {
                if (selectionGroup==null) {
                    selectionGroup = new ArrayList<Positionable>();
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
        } else if (_editPathsFrame!=null) {
            if (selection instanceof IndicatorTrack || selection instanceof PortalIcon) {
                OBlock block = _editPathsFrame.getBlock();
                // A temporary path "TEST_PATH" is used to display the icons representing a path
                // the OBlock has allocated TEST_PATH
                // pathGroup collects the icons and the actual path is edited or
                // created with a save in _editPathsFrame
                java.util.List<Positionable> pathGroup = _editPathsFrame.getPathGroup();
                if (!event.isShiftDown()) {
                    if (pathGroup.contains(selection)) {
                        pathGroup.remove(selection);
                        if (selection instanceof PortalIcon) {
                            ((PortalIcon)selection).setStatus(PortalIcon.VISIBLE);
                        } else {
                        	((IndicatorTrack)selection).setStatus(Sensor.INACTIVE);
                            ((IndicatorTrack)selection).removePath(EditCircuitPaths.TEST_PATH);
                            if (log.isDebugEnabled()) log.debug("removePath TEST_PATH");
                        }
                    } else if (okPath(selection, block)) {
                        pathGroup.add(selection);
                        // okPath() sets PortalIcons to status PortalIcon.PATH
                        if (selection instanceof IndicatorTrack) {
                            ((IndicatorTrack)selection).addPath(EditCircuitPaths.TEST_PATH);
                        }
                    } else {
                        return;
                    }
                } else {
                    if (selection instanceof PortalIcon) {
                        ((PortalIcon)selection).setStatus(PortalIcon.VISIBLE);
                    }
                }
                int state = block.getState() | OBlock.ALLOCATED;
                block.pseudoPropertyChange("state", Integer.valueOf(0), Integer.valueOf(state));
                _editPathsFrame.updatePath(true);
            }
             _editPathsFrame.toFront();
        } else if (_editPortalFrame!=null) {
            if (log.isDebugEnabled()) log.debug("selection= "+(selection==null?"null":
                                                            selection.getClass().getName()));
            if (selection instanceof PortalIcon && _circuitIcons.contains(selection)) {
                _editPortalFrame.checkPortalIconForUpdate((PortalIcon)selection);
                //_editor.getSelectionGroup().add(selection);
                _editor.highlight(getPortalIconMap().get(((PortalIcon)selection).getName()));
            }
            _editPortalFrame.toFront();
        } else if (_editDirectionFrame!=null) {
            if (selection instanceof PortalIcon && _circuitIcons.contains(selection)) {
            	_editDirectionFrame.setPortalIcon((PortalIcon)selection);
            }
        	_editDirectionFrame.toFront();
        }
    }

    /**************************** static methods ************************/

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
        c.gridwidth  = 1;
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
        if (tooltip!=null) {
            textField.setToolTipText(Bundle.getMessage(tooltip));
            l.setToolTipText(Bundle.getMessage(tooltip));
            panel.setToolTipText(Bundle.getMessage(tooltip));
        }
        return panel;
    }
    
    static Logger log = LoggerFactory.getLogger(CircuitBuilder.class.getName());
}

