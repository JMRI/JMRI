package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import jmri.InstanceManager;
import jmri.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Input window for defining a train's route from an eNtry OBlock to an eXit OBlock.
 * Routes are defined by choosing the originating block, the path on which the train 
 * start and the exit Portal through which it will leave the block.  Also it is required
 * that a Destination block is chosen and the path and Portal through which the train will
 * arrive. The Portal selections establish the direction information.  Optionally, 
 * additional blocks can be specified requiring the train to pass through or avoid entering.
 * 
 * @author Peter Cressman
 *
 */

public abstract class WarrantRoute extends jmri.util.JmriJFrame implements ActionListener, PropertyChangeListener {

	private BlockOrder  _originBlockOrder;
	private BlockOrder  _destBlockOrder;
	private BlockOrder  _viaBlockOrder;
	private BlockOrder  _avoidBlockOrder;

    protected JTextField  _originBlockBox = new JTextField();
    protected JTextField  _destBlockBox = new JTextField();
    protected JTextField  _viaBlockBox =  new JTextField();
    protected JTextField  _avoidBlockBox =  new JTextField();
    private JComboBox _originPathBox = new JComboBox();
    private JComboBox _destPathBox = new JComboBox();
    private JComboBox _viaPathBox = new JComboBox();
    private JComboBox _avoidPathBox = new JComboBox();
    private JComboBox _originPortalBox = new JComboBox();     // exit
    private JComboBox _destPortalBox = new JComboBox();       // entrance
//    int _thisActionEventId;     // id for the listener of the above items
	
    static int STRUT_SIZE = 10;
    private JDialog			_pickRouteDialog;
    private RouteTableModel	_routeModel;
    private ArrayList <BlockOrder> _orders = new ArrayList <BlockOrder>();
    private JFrame      _debugFrame;
    private RouteFinder _routeFinder;
	
	WarrantRoute() {		
        super(false, false);
        _routeModel = new RouteTableModel();
	}
	
	public abstract void selectedRoute(ArrayList <BlockOrder> orders);
	/**
	 * Extensions must implement and include code for the following
	 * PropertyChangeEvent property 
        if (property.equals("DnDrop")) {
        	doAction(e.getSource());
        }
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
    public abstract void propertyChange(java.beans.PropertyChangeEvent e);
	
	protected void init() {
        doSize(_originBlockBox, 500, 100);
        doSize(_destBlockBox, 500, 100);
        doSize(_viaBlockBox, 500, 100);
        doSize(_avoidBlockBox, 500, 100);
        doSize(_originPathBox, 500, 100);
        doSize(_destPathBox, 500, 100);
        doSize(_viaPathBox, 500, 100);
        doSize(_avoidPathBox, 500, 100);
        doSize(_originPortalBox, 500, 100);
        doSize(_destPortalBox, 500, 100);		
	}
	
	protected void doSize(JComponent comp, int max, int min) {
        Dimension dim = comp.getPreferredSize();
        dim.width = max;
        comp.setMaximumSize(dim);
        dim.width = min;
        comp.setMinimumSize(dim);
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
//        _thisActionEventId = e.getID();
        if (log.isDebugEnabled()) log.debug("actionPerformed: source "+((Component)obj).getName()+
                     " id= "+e.getID()+", ActionCommand= "+e.getActionCommand());
        doAction(obj);
    }
    
    void doAction(Object obj) {
        if (obj instanceof JTextField)
        {
            JTextField box = (JTextField)obj;
            //String text = box.getText();
            if (box == _originBlockBox) {
                setOriginBlock();
            } else if (box == _destBlockBox) {
                setDestinationBlock();
            } else if (box == _viaBlockBox) {
                setViaBlock();
            } else if (box == _avoidBlockBox) {
                setAvoidBlock();
            }
        } else {
            JComboBox box = (JComboBox)obj;
            if (box == _originPathBox) {
                setPortalBox(_originPathBox, _originPortalBox, _originBlockOrder);
            } else if (box == _originPortalBox) {
                _originBlockOrder.setExitName((String)_originPortalBox.getSelectedItem());
            } else if (box == _destPathBox) {
                setPortalBox(_destPathBox, _destPortalBox, _destBlockOrder);
            } else if (box == _destPortalBox) {
                _destBlockOrder.setEntryName((String)_destPortalBox.getSelectedItem());
            } else if (box == _viaPathBox) {
                String pathName = (String)_viaPathBox.getSelectedItem();
                _viaBlockOrder.setPathName(pathName);
            } else if (box == _avoidPathBox) {
                String pathName = (String)_avoidPathBox.getSelectedItem();
                _avoidBlockOrder.setPathName(pathName);
            }
            clearRoute();
        }
    }
    
    protected JPanel makeBlockPanels() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel oPanel = makeEndPoint("OriginBlock", makeBlockBox(_originBlockBox, "OriginToolTip"), 
                                     makeLabelCombo("PathName", _originPathBox, "OriginToolTip"), 
                                     makeLabelCombo("ExitPortalName", _originPortalBox, "OriginToolTip"),
                                     "OriginToolTip");
        panel.add(oPanel);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));

        oPanel = makeEndPoint("DestBlock", makeBlockBox(_destBlockBox, "DestToolTip"), 
                              makeLabelCombo("EntryPortalName", _destPortalBox, "DestToolTip"),
                              makeLabelCombo("PathName", _destPathBox, "DestToolTip"),
                              "DestToolTip");
        panel.add(oPanel);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));

        oPanel = makeEndPoint("ViaBlock", makeBlockBox(_viaBlockBox, "ViaToolTip"), 
                              makeLabelCombo("PathName", _viaPathBox, "ViaToolTip"),
                              null, "ViaToolTip");
        panel.add(oPanel);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));

        oPanel = makeEndPoint("AvoidBlock", makeBlockBox(_avoidBlockBox, "AvoidToolTip"), 
                              makeLabelCombo("PathName", _avoidPathBox, "AvoidToolTip"),
                              null, "AvoidToolTip");
        panel.add(oPanel);
    	return panel;
    }

    private JPanel makeEndPoint(String title, JPanel p0, JPanel p1, JPanel p2, String tooltip) {
        JPanel oPanel = new JPanel();
        oPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage(title),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        JPanel hPanel = new JPanel();
        hPanel.setLayout(new BoxLayout(hPanel, BoxLayout.X_AXIS));
        hPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        hPanel.add(p0);
        hPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.X_AXIS));
        pPanel.add(p1);
        pPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        if (p2!=null) { 
            pPanel.add(p2); 
            pPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        }
        hPanel.add(pPanel);
        oPanel.add(hPanel);
        pPanel.setToolTipText(Bundle.getMessage(tooltip));
        hPanel.setToolTipText(Bundle.getMessage(tooltip));
        oPanel.setToolTipText(Bundle.getMessage(tooltip));
        oPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        return oPanel;
    }
    
   private JPanel makeBlockBox(JTextField blockBox, String tooltip) {
        blockBox.setDragEnabled(true);
        blockBox.setTransferHandler(new jmri.util.DnDStringImportHandler());
        blockBox.setColumns(15);
        blockBox.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        //blockBox.setMaximumSize(new Dimension(100, blockBox.getPreferredSize().height));
        //blockBox.setDropMode(DropMode.USE_SELECTION);
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage("BlockName")));
        p.setToolTipText(Bundle.getMessage(tooltip));
        blockBox.setToolTipText(Bundle.getMessage(tooltip));
        p.add(pp, BorderLayout.NORTH);
        p.add(blockBox, BorderLayout.CENTER);
        blockBox.addActionListener(this);
        blockBox.addPropertyChangeListener(this);
        return p;
    }

    private JPanel makeLabelCombo(String title, JComboBox box, String tooltip) {

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage(title)));
        p.setToolTipText(Bundle.getMessage(tooltip));
        box.setToolTipText(Bundle.getMessage(tooltip));
        p.add(pp, BorderLayout.NORTH);
        p.add(box, BorderLayout.CENTER);
        box.addActionListener(this);
//        box.addPropertyChangeListener(this);
        box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        //box.setMaximumSize(new Dimension(100, box.getPreferredSize().height));
        return p;
    }

    private OBlock getEndPointBlock(JTextField textBox) {
        String text = textBox.getText();
        int idx = text.indexOf(java.awt.event.KeyEvent.VK_TAB);
        if (idx > 0){
            if (idx+1 < text.length()) {
                text = text.substring(idx+1);
            } else {
                text = text.substring(0, idx);
            }
        }
        textBox.setText(text);
        OBlock block = InstanceManager.getDefault(OBlockManager.class).getOBlock(text);
        if (block == null && text.length()>0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("BlockNotFound", text),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
        return block;
    }

    private boolean setPathBox(JComboBox pathBox, JComboBox portalBox, OBlock block) {
    	if(log.isDebugEnabled()) log.debug("setPathBox: block= "+block.getDisplayName()); 
        pathBox.removeAllItems();
        if (portalBox!=null) {
            portalBox.removeAllItems();
        }
        List <Path> list = block.getPaths();
        if (list.size()==0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("NoPaths", block.getDisplayName()),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            //pack();
            return false;
        }
        for (int i=0; i<list.size(); i++) {
             pathBox.addItem(((OPath)list.get(i)).getName());
        }
        if (log.isDebugEnabled()) log.debug("setPathBox: Block "+
                     block.getDisplayName()+" has "+list.size()+" paths.");
        return true;
    }

    private void setPortalBox(JComboBox pathBox, JComboBox portalBox, BlockOrder order) {
    	if(log.isDebugEnabled()) log.debug("setPortalBox: block= "+order.getBlock().getDisplayName());
        portalBox.removeAllItems();
        String pathName = (String)pathBox.getSelectedItem();
        order.setPathName(pathName);
        OPath path = order.getPath();
        if (path != null) {
            Portal portal = path.getFromPortal();
            if (portal!=null) {
                String name = portal.getName();
                if (name!=null) { portalBox.addItem(name); }
            }
            portal = path.getToPortal();
            if (portal!=null) {
                String name = portal.getName();
                if (name!=null) { portalBox.addItem(name); }
            }
            if (log.isDebugEnabled()) log.debug("setPortalBox: Path "+path.getName()+
                         " set in block "+order.getBlock().getDisplayName());
        } else {
            if (log.isDebugEnabled()) log.debug("setPortalBox: Path set to null in block"
                         +order.getBlock().getDisplayName());
        }
    }
    
    protected void setOriginBoxes(BlockOrder order) {
        _originBlockOrder = order;
        if (order!=null) {
            OBlock block = order.getBlock();
            String pathName = order.getPathName();
            String portalName = order.getExitName();
            _originBlockBox.setText(block.getDisplayName());
            setPathBox(_originPathBox, _originPortalBox, block);
            _originPathBox.setSelectedItem(pathName);
            setPortalBox(_originPathBox, _originPortalBox, _originBlockOrder);
            _originPortalBox.setSelectedItem(portalName);
        }
    }

    protected boolean setOriginBlock() {
        OBlock block = getEndPointBlock(_originBlockBox);
        boolean result = true;
        if (block == null) {
            result = false;
        } else {
            if (_originBlockOrder!= null && block==_originBlockOrder.getBlock() &&
                    pathIsValid(block, _originBlockOrder.getPathName())==null) {
                return true; 
            } else {
                if (pathsAreValid(block)) {
                    _originBlockOrder = new BlockOrder(block);
                    if (!setPathBox(_originPathBox, _originPortalBox, block)) {
                        result = false;
                        _originBlockBox.setText("");
                    }
                } else {
                    _originBlockBox.setText("");
                    result = false;
                }
            }
        }
        if (!result) {
            _originPathBox.removeAllItems();
            _originPortalBox.removeAllItems();
        }
        return result; 
    }

    protected void setDestinationBoxes(BlockOrder order) {
        _destBlockOrder = order;
        if (order!=null) {
            OBlock block = order.getBlock();          
            String pathName = order.getPathName();
            String portalName = order.getExitName();
            _destBlockBox.setText(block.getDisplayName());
            setPathBox(_destPathBox, _destPortalBox, block);
            _destPathBox.setSelectedItem(pathName);
            setPortalBox(_destPathBox, _destPortalBox, _destBlockOrder);
            _destPortalBox.setSelectedItem(portalName);
        }
    }
    
    protected boolean setDestinationBlock() {
        OBlock block = getEndPointBlock(_destBlockBox);
        boolean result = true;
        if (block == null) {
            result = false;
        } else {
            if (_destBlockOrder!= null && block==_destBlockOrder.getBlock() &&
                    pathIsValid(block, _destBlockOrder.getPathName())==null) {
                return true; 
            } else {
                if (pathsAreValid(block)) {
                    _destBlockOrder = new BlockOrder(block);
                    if (!setPathBox(_destPathBox, _destPortalBox, block)) {
                        result = false;
                        _destBlockBox.setText("");
                    }
                } else {
                    _destBlockBox.setText("");
                    result = false;
                }
            }
        }
        if (!result) {
        	_destPathBox.removeAllItems();
        	_destPortalBox.removeAllItems();
        }
        return result; 
    }

    protected boolean setViaBlock() {
        OBlock block = getEndPointBlock(_viaBlockBox);
        if (block == null) {
            _viaPathBox.removeAllItems();
            _viaBlockOrder = null;
            return true;
        } else {
            if (_viaBlockOrder!=null && block==_viaBlockOrder.getBlock() &&
                    pathIsValid(block, _viaBlockOrder.getPathName())==null) {
                return true;
            } else {
                if (pathsAreValid(block)) {
                    _viaBlockOrder = new BlockOrder(block);
                    if (!setPathBox(_viaPathBox, null, block)) {
                        _viaPathBox.removeAllItems();
                        _viaBlockBox.setText("");
                        return false;
                    }
                }
            }
        }
        return false;
    }
    
    protected void setViaBoxes(BlockOrder order) {
        _viaBlockOrder = order;
        if (order!=null) {
            OBlock block = order.getBlock();
            String pathName = order.getPathName();
            _viaBlockBox.setText(block.getDisplayName());
            setPathBox(_viaPathBox, null, block);
            _viaPathBox.setSelectedItem(pathName);
        }    	
    }

    protected boolean setAvoidBlock() {
        OBlock block = getEndPointBlock(_avoidBlockBox);
        if (block == null) {
            _avoidPathBox.removeAllItems();
            _avoidBlockOrder = null;
            return true;
        } else {
            if (_avoidBlockOrder!=null && block==_avoidBlockOrder.getBlock() &&
                    pathIsValid(block, _avoidBlockOrder.getPathName())==null) {
                return true;
            } else {
                if (pathsAreValid(block)) {
                    _avoidBlockOrder = new BlockOrder(block);
                    if (!setPathBox(_avoidPathBox, null, block)) {
                        _avoidPathBox.removeAllItems();
                        _avoidBlockBox.setText("");
                        return false;
                    }
                }
            }
        }
        return false;
    }
    
    protected void setAvoidBoxes(BlockOrder order) {
    	_avoidBlockOrder = order;
        if (order!=null) {
            OBlock block = order.getBlock();
            String pathName = order.getPathName();
            _avoidBlockBox.setText(block.getDisplayName());
            setPathBox(_avoidPathBox, null, block);
            _avoidPathBox.setSelectedItem(pathName);
        }    	
    }
    
    private boolean pathsAreValid(OBlock block) {
        List <Path> list = block.getPaths();
        if (list.size()==0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("NoPaths", block.getDisplayName()),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        for (int i=0; i<list.size(); i++) {
            OPath path = (OPath)list.get(i);
            if (path.getFromPortal()==null && path.getToPortal()==null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("PathNeedsPortal", path.getName(), block.getDisplayName()),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /******************************* Finding the route ********************************/

    /**
    * Gather parameters to search for a route
    */
    protected String findRoute(int depth) {
        // read and verify origin and destination blocks/paths/portals
        String msg = null;
        String pathName = null;
        if (setOriginBlock()) {
            pathName = _originBlockOrder.getPathName();
            if (pathName!=null) {
                if (_originBlockOrder.getExitName() == null) {
                    msg = Bundle.getMessage("SetExitPortal", Bundle.getMessage("OriginBlock"));
                } else {
                    msg = pathIsValid(_originBlockOrder.getBlock(), pathName);
                }
            } else {
                msg = Bundle.getMessage("SetPath", Bundle.getMessage("OriginBlock"));
            }
        } else {
            msg = Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock"));
        }
        if (msg==null) {
            if (setDestinationBlock()) {
                pathName = _destBlockOrder.getPathName();
                if (pathName!=null) {
                    if (_destBlockOrder.getEntryName() == null) {
                        msg = Bundle.getMessage("SetEntryPortal", Bundle.getMessage("DestBlock"));
                    } else {
                        msg = pathIsValid(_destBlockOrder.getBlock(), pathName);
                    }
                } else {
                    msg = Bundle.getMessage("SetPath", Bundle.getMessage("DestBlock"));
                }
            } else {
                msg = Bundle.getMessage("SetEndPoint", Bundle.getMessage("DestBlock"));
            }
        }
        if (msg==null) {
            if (setViaBlock()) {
                if (_viaBlockOrder!=null && _viaBlockOrder.getPathName()==null) {
                    msg = Bundle.getMessage("SetPath", Bundle.getMessage("ViaBlock"));
                }
            } else {
                msg = Bundle.getMessage("SetEndPoint", Bundle.getMessage("ViaBlock"));
            }
        }
        if (msg==null) {
            if (setAvoidBlock()) {
                if (_avoidBlockOrder!=null && _avoidBlockOrder.getPathName()==null) {
                    msg = Bundle.getMessage("SetPath", Bundle.getMessage("AvoidBlock"));
                }
            } else {
                msg = Bundle.getMessage("SetEndPoint", Bundle.getMessage("AvoidBlock"));
            }
        }
        if (msg==null) {
            _routeFinder = new RouteFinder(this, _originBlockOrder, _destBlockOrder,
                    _viaBlockOrder, _avoidBlockOrder, depth);
            new Thread(_routeFinder).start();        	
        }       
        return msg;
    }
    
    protected void stopRouteFinder() {
        if (_routeFinder!=null) {
            _routeFinder.quit();
            _routeFinder = null;
        }    	
    }
    
/************************************ Route Selection **************************************/
    
	public void setOrders(List <BlockOrder> oList) {
	    for (int i=0; i<oList.size(); i++) {
	        BlockOrder bo = new BlockOrder(oList.get(i));
	        _orders.add(bo);
	    }		
	}
	
	public List<BlockOrder> getOrders() {
		return _orders;
	}
	
	public BlockOrder getViaBlockOrder() {
		return _viaBlockOrder;
	}
	
	public BlockOrder getAvoidBlockOrder() {
		return _avoidBlockOrder;
	}
	
   /**
    *  Callback from RouteFinder - several routes found
    */
    protected void pickRoute(List <DefaultMutableTreeNode> destNodes, DefaultTreeModel tree) {
        if (destNodes.size()==1) {
            showRoute(destNodes.get(0), tree);
            selectedRoute(_orders);
            return;
        }
        _pickRouteDialog = new JDialog(this, Bundle.getMessage("DialogTitle"), false);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5,5));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage("NumberRoutes1", Integer.valueOf(destNodes.size()))));
        panel.add(new JLabel(Bundle.getMessage("NumberRoutes2")));

        mainPanel.add(panel, BorderLayout.NORTH);
        ButtonGroup buttons = new ButtonGroup();

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (int i=0; i<destNodes.size(); i++) {
            JRadioButton button = new JRadioButton(Bundle.getMessage("RouteSize", Integer.valueOf(i+1), 
                    Integer.valueOf(destNodes.get(i).getLevel())) );
            button.setActionCommand(""+i);
            buttons.add(button);
            panel.add(button);
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        JButton ok = new JButton(Bundle.getMessage("ButtonSelect"));
        ok.addActionListener(new ActionListener() {
                ButtonGroup buttons;
                JDialog dialog;
                List <DefaultMutableTreeNode> destNodes;
                DefaultTreeModel tree;
                public void actionPerformed(ActionEvent e) {
                    if (buttons.getSelection()!=null) {
                        int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                        showRoute(destNodes.get(i), tree);
                        selectedRoute(_orders);
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("SelectRoute"),
                                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    }
                }
                ActionListener init(ButtonGroup bg, JDialog d, List <DefaultMutableTreeNode> dn,
                                    DefaultTreeModel t) {
                    buttons = bg;
                    dialog = d;
                    destNodes = dn;
                    tree = t;
                    return this;
                }
            }.init(buttons, _pickRouteDialog, destNodes, tree));
        ok.setMaximumSize(ok.getPreferredSize());
        JButton show = new JButton(Bundle.getMessage("ButtonReview"));
        show.addActionListener(new ActionListener() {
                ButtonGroup buttons;
                List <DefaultMutableTreeNode> destNodes;
                DefaultTreeModel tree;
                public void actionPerformed(ActionEvent e) {
                    if (buttons.getSelection()!=null) {
                        int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                        showRoute(destNodes.get(i), tree);
                    } else {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("SelectRoute"),
                                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    }
                }
                ActionListener init(ButtonGroup bg, List <DefaultMutableTreeNode> dn,
                                    DefaultTreeModel t) {
                    buttons = bg;
                    destNodes = dn;
                    tree = t;
                    return this;
                }
            }.init(buttons, destNodes, tree));
        show.setMaximumSize(show.getPreferredSize());
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(show);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(ok);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        mainPanel.add(panel, BorderLayout.SOUTH);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(makeRouteTablePanel());
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(mainPanel);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));

        _pickRouteDialog.getContentPane().add(panel);
        _pickRouteDialog.setLocation(getLocation().x+50, getLocation().y+150);
        _pickRouteDialog.pack();
        _pickRouteDialog.setVisible(true);
    }

    /**
    *  Callback from RouteFinder - exactly one route found
    */
    protected void showRoute(DefaultMutableTreeNode destNode, DefaultTreeModel tree) {
        TreeNode[] nodes = tree.getPathToRoot(destNode);
        _orders.clear();
        for (int i=0; i<nodes.length; i++) {
            _orders.add((BlockOrder)((DefaultMutableTreeNode)nodes[i]).getUserObject());
        }
        _routeModel.fireTableDataChanged();
        if (log.isDebugEnabled()) log.debug("showRoute: Route has "+_orders.size()+" orders.");
    }

    protected JPanel makeRouteTablePanel() {
        JTable routeTable = new JTable(_routeModel);
        routeTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        //routeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i=0; i<_routeModel.getColumnCount(); i++) {
            int width = _routeModel.getPreferredWidth(i);
            routeTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        JScrollPane tablePane = new JScrollPane(routeTable);
        Dimension dim = routeTable.getPreferredSize();
        dim.height = routeTable.getRowHeight()*8;
        tablePane.getViewport().setPreferredSize(dim);

        JPanel routePanel = new JPanel();
        routePanel.setLayout(new BoxLayout(routePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(Bundle.getMessage("RouteTableTitle"));
        routePanel.add(title, BorderLayout.NORTH);
        routePanel.add(tablePane);
        return routePanel;
    }

    /**
    *  Callback from RouteFinder - no routes found
    */
    protected void debugRoute(DefaultTreeModel tree, BlockOrder origin, BlockOrder dest) {
        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, Bundle.getMessage("NoRoute",  
                            new Object[] {origin.getBlock().getDisplayName(), 
        								origin.getPathName(), origin.getExitName(),
        								dest.getBlock().getDisplayName(), dest.getPathName() }),
                            Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                                                    JOptionPane.WARNING_MESSAGE)) {
            return; 
        }
        if (_debugFrame!=null) {
            _debugFrame.dispose();
        }
        _debugFrame = new JFrame(Bundle.getMessage("DebugRoute"));
        javax.swing.JTree dTree = new javax.swing.JTree(tree);
        dTree.setShowsRootHandles(true);
        dTree.setScrollsOnExpand(true);
        dTree.setExpandsSelectedPaths(true);
        JScrollPane treePane = new JScrollPane(dTree);
        treePane.getViewport().setPreferredSize(new Dimension(900, 300));
        _debugFrame.getContentPane().add(treePane);
        _debugFrame.setVisible(true);
        _debugFrame.pack();
    }
    
    protected void clearRoute() {
        _orders = new ArrayList <BlockOrder>();
        _routeModel.fireTableDataChanged();
        if (_debugFrame!=null) {
            _debugFrame.dispose();
            _debugFrame = null;
        }
        if (_pickRouteDialog!=null) {
            _pickRouteDialog.dispose();
            _pickRouteDialog = null;
        }    	
    }
    
    protected String routeIsValid() {
    	String msg = null;
        if (_orders.size() == 0) {
            return Bundle.getMessage("noBlockOrders");
        }
        BlockOrder blockOrder = _orders.get(0);
        msg =pathIsValid(blockOrder.getBlock(), blockOrder.getPathName());
        if (msg==null){
            for (int i=1; i<_orders.size(); i++){
                BlockOrder nextBlockOrder = _orders.get(i);
                msg = pathIsValid(nextBlockOrder.getBlock(), nextBlockOrder.getPathName());
                if (msg!=null) {
                    return msg;
                }
                if (!blockOrder.getExitName().equals(nextBlockOrder.getEntryName())) {
                	return Bundle.getMessage("disconnectedRoute", 
                			blockOrder.getBlock().getDisplayName(), nextBlockOrder.getBlock().getDisplayName());
                }
                blockOrder = nextBlockOrder;
            }
        }
        return msg;
    }

    private String pathIsValid(OBlock block, String pathName) {
        List <Path> list = block.getPaths();
        if (list.size()==0) {
            return Bundle.getMessage("WarningTitle");
        }
        if (pathName!=null) {
            for (int i=0; i<list.size(); i++) {
                OPath path = (OPath)list.get(i);
                //if (log.isDebugEnabled()) log.debug("pathIsValid: pathName= "+pathName+", i= "+i+", path is "+path.getName());  
                if (pathName.equals(path.getName()) ){
                    if (path.getFromPortal()==null && path.getToPortal()==null) {
                        return Bundle.getMessage("PathNeedsPortal", pathName, block.getDisplayName());
                    }
                    return null;
                }
            }
        }
        return Bundle.getMessage("PathInvalid", pathName, block.getDisplayName());
    }
    
    public void dispose() {
    	clearRoute();    	
        super.dispose();
    }

    /************************* Route Table ******************************/
    class RouteTableModel extends AbstractTableModel {
        public static final int BLOCK_COLUMN = 0;
        public static final int ENTER_PORTAL_COL =1;
        public static final int PATH_COLUMN = 2;
        public static final int DEST_PORTAL_COL = 3;
        public static final int NUMCOLS = 4;

        public RouteTableModel() {
            super();
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public int getRowCount() {
            return _orders.size();
        }

        public String getColumnName(int col) {
            switch (col) {
                case BLOCK_COLUMN: return Bundle.getMessage("BlockCol");
                case ENTER_PORTAL_COL: return Bundle.getMessage("EnterPortalCol");
                case PATH_COLUMN: return Bundle.getMessage("PathCol");
                case DEST_PORTAL_COL: return Bundle.getMessage("DestPortalCol");
            }
            return "";
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            return new JTextField(20).getPreferredSize().width;
        }

        public Object getValueAt(int row, int col) {
        	// some error checking
        	if (row >= _orders.size()){
        		log.debug("row is greater than _orders");
        		return "";
        	}
            BlockOrder bo = _orders.get(row);
          	// some error checking
        	if (bo == null){
        		log.debug("BlockOrder is null");
        		return "";
        	}
            switch (col) {
                case BLOCK_COLUMN: 
                    return bo.getBlock().getDisplayName();
                case ENTER_PORTAL_COL: 
                    return bo.getEntryName();
                case PATH_COLUMN:
                    return bo.getPathName();
                case DEST_PORTAL_COL:
                    if (row==_orders.size()-1) { return ""; }
                    return bo.getExitName();
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
            BlockOrder bo = _orders.get(row);
            OBlock block = null;
            switch (col) {
                case BLOCK_COLUMN:
                    block = InstanceManager.getDefault(OBlockManager.class).getOBlock((String)value);
                    if (block != null) { bo.setBlock(block); }
                    break;
                case ENTER_PORTAL_COL: 
                    bo.setEntryName((String)value);
                    break;
                case PATH_COLUMN:
                    bo.setPathName((String)value);
                    break;
                case DEST_PORTAL_COL: 
                    bo.setExitName((String)value);
                    break;
            }
            fireTableRowsUpdated(row, row);
        }
    }
	
    static Logger log = LoggerFactory.getLogger(WarrantRoute.class.getName());
}