package jmri.jmrit.display.controlPanelEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.display.*;

import jmri.jmrit.logix.*;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2011
 * 
 */

public class EditPortalFrame extends jmri.util.JmriJFrame implements ListSelectionListener {

    private OBlock _homeBlock;
    private CircuitBuilder _parent;
    private OBlock _adjacentBlock;

    private JList       _portalList;
    private PortalListModel _portalListModel; 
    private String _currentPortalName;			// name of last portal Icon made
    
    private JTextField  _portalName = new JTextField();
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

        if (_parent.getPortalIconMap().get(name)==null) {
        	PortalIcon pi = new PortalIcon(_parent._editor, portal);
        	pi.setLevel(Editor.MARKERS);
        	pi.setStatus(PortalIcon.VISIBLE);
            _parent.addPortalIcon(pi);
            java.util.List<Positionable> list = _parent.getCircuitGroup();
            Iterator<Positionable> iter = list.iterator();
            while(iter.hasNext()) {
            	Positionable pos = iter.next();
            	if (pos instanceof IndicatorTrack) {
            		int x = pos.getX() + pos.getWidth()/2;
            		int y = pos.getY() + pos.getHeight()/2;            		
            		pi.setLocation(x, y);
            		parent._editor.putItem(pi);
            	}
            }
        }
    }
    public EditPortalFrame(String title, CircuitBuilder parent, OBlock block, boolean update) {
        _homeBlock = block;
        _parent = parent;
        setTitle(java.text.MessageFormat.format(title, _homeBlock.getDisplayName()));

        addWindowListener(new java.awt.event.WindowAdapter() {
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
        border.setLayout(new java.awt.BorderLayout(10,10));
        border.add(contentPane);
        setContentPane(border);
        pack();
        if (_firstInstance) {
            setLocationRelativeTo(_parent._editor);
            setSize(500,500);
            _firstInstance = false;
        } else {
            setLocation(_loc);
            setSize(_dim);
        }
        setVisible(true);
    }

    private JPanel MakeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
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

        _portalListModel =  new PortalListModel();
        _portalList = new JList();
        _portalList.setModel(_portalListModel);
        _portalList.setCellRenderer(new PortalCellRenderer());
        _portalList.addListSelectionListener(this);
        portalPanel.add(new JScrollPane(_portalList));
        _portalList.setPreferredSize(new Dimension(300,150));

        JButton clearButton = new JButton(Bundle.getMessage("buttonClearSelection"));
        clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    clearListSelection();
                }
        });
        //clearButton.setToolTipText(ItemPalette.rbp.getString("ToolTipClearSelection"));
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
                public void actionPerformed(ActionEvent a) {
                    changePortalName();
                }
        });
        changeButton.setToolTipText(Bundle.getMessage("ToolTipChangeName"));
        panel.add(changeButton);
 
        JButton deleteButton = new JButton(Bundle.getMessage("buttonDeletePortal"));
        deleteButton.addActionListener(new ActionListener() {
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
        } else{
            JLabel l = new JLabel(Bundle.getMessage("enterNameToDrag"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.add(l);
            l = new JLabel(Bundle.getMessage("dragNewIcon"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.add(l);
            panel.add(Box.createVerticalStrut(STRUT_SIZE/2));
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
        portalPanel.add(MakeButtonPanel());        	
        return portalPanel;
    }

    private static class PortalCellRenderer extends JLabel implements ListCellRenderer {
     
        public Component getListCellRendererComponent(
           JList list,              // the list
           Object value,            // value to display
           int index,               // cell index
           boolean isSelected,      // is the cell selected
           boolean cellHasFocus)    // does the cell have focus
        {
             String s = ((Portal)value).getDescription();
             setText(s);
             if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             } else {
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }
             setEnabled(list.isEnabled());
             setFont(list.getFont());
             setOpaque(true);
             return this;
        }
    }

    private void clearListSelection() {
        _portalList.clearSelection();
        _portalName.setText(null);
        _parent._editor.highlight(null);
    }

    public void valueChanged(ListSelectionEvent e) {
        Portal portal = (Portal)_portalList.getSelectedValue();
        if (portal!=null) {
            _portalName.setText(portal.getName());
            PortalIcon icon = _parent.getPortalIconMap().get(portal.getName());
            if (icon!=null) {
                icon.setStatus(PortalIcon.VISIBLE);            	
            }
            _parent._editor.highlight(icon);
        } else {
            _portalName.setText(null);
        }
    }
    
    protected void setSelection(Positionable pos) {
    	if (pos instanceof PortalIcon) {
            _portalName.setText(((PortalIcon)pos).getName());    		
    	}
    }

    class PortalListModel extends AbstractListModel {
        public int getSize() {
            return _homeBlock.getPortals().size();
        }
        public Object getElementAt(int index) {
            return _homeBlock.getPortals().get(index);
        }
        public void dataChange() {
            fireContentsChanged(this, 0, 0);
        }
    }

    /************************* end setup **************************/

    /**
    * Is location of icon reasonable? if so, add it
    */
    private boolean checkPortalIcon(PortalIcon icon) {
    	String msg = testPortalIcon(icon);
    	if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg, 
                    Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            _parent._editor.highlight(icon);
    		return false;
    	}
     	return true;
    }
    private String testPortalIcon(PortalIcon icon) {
        java.util.List<Positionable> list = _parent.getCircuitIcons(_homeBlock);
        String msg = null;
        if (list==null || list.size()==0) {
            msg = Bundle.getMessage("needIcons");
            return msg;
        }
        Portal portal = icon.getPortal();
        boolean ok = false;
        Rectangle homeRect = new Rectangle();
        Rectangle adjRect = new Rectangle();
        Positionable comp = null;
        _adjacentBlock = null;
        for (int i=0; i<list.size(); i++) {
            if (list.get(i) instanceof IndicatorTrack) {
                homeRect = list.get(i).getBounds(homeRect);
               if (iconIntersectsRect(icon, homeRect)) {
                   ok = true;
                   break;
                }
            }
        }
        if (!ok) {
            msg = Bundle.getMessage("iconNotOnCircuit", 
                           icon.getNameString(), _homeBlock.getDisplayName());
            return msg;
        }

        ok = false;
        ArrayList<OBlock> neighbors = new ArrayList<OBlock>();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        for (int j = 0; j < sysNames.length; j++) {
            OBlock block = manager.getBySystemName(sysNames[j]);
            if (!block.equals(_homeBlock)) {
                list = _parent.getCircuitIcons(block);
                for (int i=0; i<list.size(); i++) {
                    comp = list.get(i);
                    if (_parent.isTrack(comp)) {
                        adjRect = comp.getBounds(adjRect);
                        if (iconIntersectsRect(icon, adjRect)) {
                            ok = true;
                            neighbors.add(block);
                        }
                    }
                }
            }
        }
        if (!ok) {
            msg = Bundle.getMessage("iconNotOnAdjacent", 
                       icon.getNameString(), _homeBlock.getDisplayName());
            return msg;
        }
        _adjacentBlock = neighbors.get(0);
        if (neighbors.size()>1) {
        	// show list
        	String[] selects = new String[neighbors.size()];
        	Iterator<OBlock> iter = neighbors.iterator();
        	int i = 0;
        	while (iter.hasNext()) {
        		selects[i++] =  iter.next().getDisplayName();                			
        	}
        	Object select = JOptionPane.showInputDialog(this,Bundle.getMessage("multipleSelections", portal.getName()),
        					Bundle.getMessage("questionTitle"), JOptionPane.QUESTION_MESSAGE, 
        					null, selects, null);
        	if (select !=null) {
            	iter = neighbors.iterator();
            	while (iter.hasNext()) {
            		OBlock block = iter.next();
            		if (((String)select).equals(block.getDisplayName())) {
                        _adjacentBlock = block;
                		break;
            		}
            	}
        	}        	
        }
        if (portal.getToBlock()!=null && !_adjacentBlock.equals(portal.getToBlock())
                         && !_adjacentBlock.equals(portal.getFromBlock()) ) {
            msg = Bundle.getMessage("iconNotOnBlocks", icon.getNameString(), 
            		portal.getFromBlockName(), _adjacentBlock.getDisplayName());
            return msg;
        }
        return msg;
    }		// end testPortalIcon

    /**
    * Called after click on portal icon
    */
    protected void checkPortalIconForUpdate(PortalIcon icon) {
        Portal portal = icon.getPortal();
        if (!checkPortalIcon(icon)) {
        	if (_adjacentBlock==null) {
                return;        		
        	}
        	String name = portal.getName();
            int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("repositionPortal",
            		name, _homeBlock.getDisplayName(), _adjacentBlock.getDisplayName()), Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION, 
                            JOptionPane.QUESTION_MESSAGE);
            if (result==JOptionPane.NO_OPTION) {
            	return;
            }
        	// Change portal position to join different pair of blocks
            portal.dispose();
            PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        	portal = portalMgr.createNewPortal(null, name);
        	if (portal==null) {
            	portal = portalMgr.providePortal(name);
        	}
            portal.setToBlock(_homeBlock, false);
            portal.setFromBlock(_adjacentBlock, false);
            icon.setPortal(portal);
        }
        OBlock block = portal.getToBlock();
        if (block==null) {
            portal.setToBlock(_adjacentBlock, false);
        } else {
            if (!block.equals(_homeBlock)) {
                if (changeBlock(block)) {
                    portal.setToBlock(_adjacentBlock, true);
                }
            } else {
                block = portal.getFromBlock();
                if (block==null) {
                    portal.setFromBlock(_adjacentBlock, false);
                } else {
                    if (!block.equals(_homeBlock)) {
                        if (changeBlock(block)) {
                            portal.setFromBlock(_adjacentBlock, true);
                        }
                    } else {
                        log.info("Portal has Home block "+_homeBlock+" on both sides. Is this is a Balloon track circuit?");
                    }
                }
            }
        }
        
        checkForDuplication(portal);
        
        _parent._editor.highlight(icon);        
        _parent.getPortalIconMap().put(icon.getName(), icon);
        clearListSelection();
        _portalListModel.dataChange();
    }

    private void checkForDuplication(Portal portal) {
        // check for duplication
        OBlock toBlock = portal.getToBlock();
        OBlock fromBlock = portal.getFromBlock();
        java.util.List<Portal> list = toBlock.getPortals();
        if (list.size()>0) {
            Iterator<Portal> iter = list.iterator();
            while (iter.hasNext()) {
            	Portal p = iter.next();
            	if (p.equals(portal)) {
            		continue;
            	}
            	if ((toBlock.equals(p.getToBlock()) && fromBlock.equals(p.getFromBlock()) )
            	        || (toBlock.equals(p.getFromBlock()) && fromBlock.equals(p.getToBlock()) ) ) {

            	    notifyDuplicatePortal(portal, p, toBlock, fromBlock);
            		break;
            	}
            }
        }
    }
    
    private void notifyDuplicatePortal(Portal primary, Portal secondary, OBlock toBlock, OBlock fromBlock) {
        JOptionPane.showMessageDialog(this, Bundle.getMessage("parallelPortal",
                primary.getName(), secondary.getName(),toBlock.getDisplayName(), fromBlock.getDisplayName()), 
                Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
    }
    
    private boolean changeBlock(OBlock block) {
        if (block.equals(_adjacentBlock)) {
            return false;     // no change
        }
        //replace block and switch paths to new block.
        int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("replacePortalBlock", 
                        _homeBlock.getDisplayName(), block.getDisplayName(), 
                        _adjacentBlock.getDisplayName()), 
                        Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
        if (result==JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }

    private boolean checkPortalIcons() {
        java.util.List<Portal> portals = _homeBlock.getPortals();
        HashMap<String, PortalIcon> iconMap = _parent.getPortalIconMap();
        if (log.isDebugEnabled()) log.debug("checkPortalIcons: "+_homeBlock.getDisplayName()+
                                            " has "+portals.size()+" portals, iconMap has "+
                                            iconMap.size()+" icons");
        for (int i=0; i<portals.size(); i++) {
        	PortalIcon icon = iconMap.get(portals.get(i).getName());
            if (icon==null) {
                int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("noPortalIcon",
                		portals.get(i).getName()), Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION, 
                                JOptionPane.QUESTION_MESSAGE);
                if (result==JOptionPane.YES_OPTION) {
                	return false;
                }
            }
        }
        return true;
    }
    
    /**
    * Check if icon is placed on the track icons of a block
    * Called when EditPortalFrame is closed
    * list is the set of icons representing a block
    */
    static boolean portalIconOK(java.util.List<Positionable> aList, PortalIcon icon, java.util.List<Positionable> bList) {
        if (icon==null || aList==null || bList==null) {
            return false;
        }
        boolean ok = false;
        String status = icon.getStatus();
        icon.setStatus(PortalIcon.VISIBLE);
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

    static boolean iconIntersectsRect(Positionable icon, Rectangle rect) {
        Rectangle iconRect = icon.getBounds(new Rectangle());
        return (iconRect.intersects(rect));
    }

    private void changePortalName() {
        Portal portal = (Portal)_portalList.getSelectedValue();
        String oldName = null;
        if (portal!=null) {
            oldName = portal.getName();        	
        }
        String name = _portalName.getText();
        if (portal==null || name==null || name.trim().length()==0 ) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("changePortalName"), 
                            Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String msg = portal.setName(name);
        if (msg==null) {
            _parent.changePortalName(oldName, name);		// update maps
            _portalListModel.dataChange();
        } else {
            JOptionPane.showMessageDialog(this, msg, 
                            Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deletePortal() {
        String name = _portalName.getText();
        Portal portal = (Portal)_portalList.getSelectedValue();
        if (portal==null) {
        	portal = _parent.getPortalByName(name);
        }
        if (portal !=null) {
            int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("confirmPortalDelete",
            		portal.getName()), Bundle.getMessage("makePortal"), JOptionPane.YES_NO_OPTION, 
                            JOptionPane.QUESTION_MESSAGE);
            if (result==JOptionPane.YES_OPTION) {
            	// removes portal and stubs all paths through it.
                portal.dispose();
                _portalListModel.dataChange();
            }
        }
        PortalIcon icon = _parent.getPortalIconMap().get(name);
        if (icon!=null) {
        	deletePortalIcon(icon);
        }
    }

    private void deletePortalIcon(PortalIcon icon) {
        _parent.removePortal(icon.getName());
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

    protected OBlock getHomeBlock() {
        return _homeBlock;
    }
    
    /********************** DnD *****************************/

    protected JPanel makeDndIconPanel() {
    	_dndPanel = new JPanel();

        String fileName = "resources/icons/throttles/RoundRedCircle20.png";
        NamedIcon icon = new NamedIcon(fileName, fileName);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                         Bundle.getMessage("portal")));
        try {
            JLabel label = new IconDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
            label.setIcon(icon);
            label.setName(Bundle.getMessage("portal"));
            panel.add(label);
        } catch (java.lang.ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        _dndPanel.add(panel);
        return _dndPanel;
    }    

    public class IconDragJLabel extends DragJLabel {

        public IconDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String name = _portalName.getText();
            if (name==null || name.trim().length()==0) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("needPortalName"), 
                                Bundle.getMessage("makePortal"), JOptionPane.INFORMATION_MESSAGE);
                return null;
            } else {
                Portal p = _parent.getPortalByName(name);	// check all portals for name
                if (p!=null && !(_homeBlock.equals(p.getFromBlock()) || _homeBlock.equals(p.getToBlock())) ) {
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
                } else {
                    Portal portal = _homeBlock.getPortalByName(name);
                    if (portal==null) {
                    	PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
                    	portal = portalMgr.createNewPortal(null, name);
                        portal.setToBlock(_homeBlock, false);
                        _portalListModel.dataChange();
                    }
                	pi = new PortalIcon(_parent._editor, portal);
                	pi.setLevel(Editor.MARKERS);
                	pi.setStatus(PortalIcon.VISIBLE);
                    _parent.addPortalIcon(pi);
                }
                _currentPortalName = name;
                return pi;
            }
        }
    }

    static Logger log = LoggerFactory.getLogger(EditPortalFrame.class.getName());
}

