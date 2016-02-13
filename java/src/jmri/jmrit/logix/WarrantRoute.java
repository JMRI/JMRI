package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
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
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Path;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
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

    private static final long serialVersionUID = 6066050907933847146L;

    enum Location {ORIGIN, DEST, VIA, AVOID}
    protected RouteLocation  _origin = new RouteLocation(Location.ORIGIN);
    protected RouteLocation  _destination = new RouteLocation(Location.DEST);
    protected RouteLocation  _via =  new RouteLocation(Location.VIA);
    protected RouteLocation  _avoid =  new RouteLocation(Location.AVOID);
    RouteLocation _focusedField;
    
    static int STRUT_SIZE = 10;
    static String PAD = "               ";
    private JDialog         _pickRouteDialog;
    private RouteTableModel _routeModel;
    private ArrayList <BlockOrder> _orders = new ArrayList <BlockOrder>();
    private JFrame      _debugFrame;
    private RouteFinder _routeFinder;
    private int         _depth =20;
    private JTextField  _searchDepth =  new JTextField(5);

    private RosterEntry _train;
    private String _trainId = null;
    private JComboBox<String> _rosterBox = new JComboBox<String>();
    private JTextField _dccNumBox = new JTextField();
    private JTextField _trainNameBox = new JTextField(6);

    WarrantRoute() {        
        super(false, true);
        _routeModel = new RouteTableModel();
        getRoster();
    }
    
    public abstract void selectedRoute(ArrayList <BlockOrder> orders);
    public abstract void propertyChange(java.beans.PropertyChangeEvent e);
    
    public int getDepth() {
        try {
            _depth = Integer.parseInt(_searchDepth.getText());
        } catch (NumberFormatException nfe) {
            _searchDepth.setText(Integer.toString(_depth));
        }
        return _depth;
    }
    public void setDepth(int d) {
        _depth = d;
        _searchDepth.setText(Integer.toString(_depth));
    }
    
    public JPanel searchDepthPanel(boolean vertical) {
        _searchDepth.setText(Integer.toString(_depth));
        JPanel p = new JPanel();
        p.add(Box.createHorizontalGlue());
        p.add(makeTextBoxPanel(vertical, _searchDepth, "SearchDepth", "ToolTipSearchDepth"));
        _searchDepth.setColumns(5);
        p.add(Box.createHorizontalGlue());
        return p;
    }
    
/************************** Loco Address **********************/

    protected JPanel makeTrainPanel() {
        JPanel trainPanel = new JPanel();
        trainPanel.setLayout(new BoxLayout(trainPanel, BoxLayout.LINE_AXIS));
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
//        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(false, _trainNameBox, "TrainName", "noTrainName"));
//        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(false, _rosterBox, "Roster", null));
//        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(false, _dccNumBox, "DccAddress", null));
        trainPanel.add(panel);
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        _dccNumBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTrainInfo(_dccNumBox.getText());
            }
        });
        JPanel x = new JPanel();
        x.setLayout(new BoxLayout(x, BoxLayout.PAGE_AXIS));
        x.add(trainPanel);
//        x.add(Box.createRigidArea(new Dimension(600, 2)));
        return x;
    }
    
    private void getRoster() {
        List<RosterEntry> list = Roster.instance().matchingList(null, null, null, null, null, null, null);
        _rosterBox.setRenderer(new jmri.jmrit.roster.swing.RosterEntryListCellRenderer());
        _rosterBox.addItem(" ");
        _rosterBox.addItem(Bundle.getMessage("noSuchAddress"));
        for (int i = 0; i < list.size(); i++) {
            RosterEntry r = list.get(i);
            _rosterBox.addItem(r.titleString());
        }
        //_rosterBox = Roster.instance().fullRosterComboBox();
        _rosterBox.setMaximumSize(_rosterBox.getPreferredSize());
        _rosterBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selection = (String)_rosterBox.getSelectedItem();
                if (Bundle.getMessage("noSuchAddress").equals(selection)) {
                    _dccNumBox.setText(null);                                
                } else {
                    setTrainInfo(selection);                    
                }
            }
        });
    }

    /**
     * Set the roster entry, if it exists, or train id string if not.
     * i.e. set enough info to get a dccLocoAddress
     * @param name may be roster Id or address
     * @return
     */
    protected String setTrainInfo(String name) {
        if (log.isDebugEnabled()) {
            log.debug("setTrainInfo for: " + name);
        }
        _train = Roster.instance().entryFromTitle(name);
        if (_train == null) {
            if (name==null || name.trim().length()==0) {
                _trainId = null;
                return Bundle.getMessage("NoLoco");
            }
            int index = name.indexOf('(');
            String numId;
            boolean isLong = true;
            if (index >= 0) {
                if ((index + 1) < name.length()
                        && (name.charAt(index + 1) == 'S' || name.charAt(index + 1) == 's')) {
                    isLong = false;
                }                    
                numId = name.substring(0, index);
            } else {
                Character ch = name.charAt(name.length()-1);
                if (!Character.isDigit(ch)) {
                    if (ch == 'S' || ch == 's') {
                        isLong = false;                        
                    }
                    numId = name.substring(0, name.length()-1);
                } else {
                    numId = name;                    
                }
            }
            List<RosterEntry> l = Roster.instance().matchingList(null, null, numId, null, null, null, null);
            if (l.size() > 0) {
                _train = l.get(0);
            } else {
                _train = null;
                try {
                    int num = Integer.parseInt(numId);
                    isLong = (name.charAt(0) == '0' || num > 255);  // leading zero means long
                    _trainId = num+"("+(isLong?'L':'S')+")";
                } catch (NumberFormatException e) {
                    _trainId = null;
                    return Bundle.getMessage("BadDccAddress", name);
                }            
            }
        }
        if (_train != null) {
            _trainId =  _train.getId();
            _rosterBox.setSelectedItem(_train.getId());
            _dccNumBox.setText(_train.getDccLocoAddress().toString());
        } else {
            _rosterBox.setSelectedItem(Bundle.getMessage("noSuchAddress"));
            _dccNumBox.setText(_trainId);            
        }
        String n = _trainNameBox.getText();
        if (n == null || n.length() == 0) {
            if (_train != null) {
                _trainNameBox.setText(_train.getRoadNumber()); 
            } else {
                _trainNameBox.setText(_trainId);                
            }
        }
        return null;
    }

    protected RosterEntry getTrain() {
        return _train;
    }

    protected void setTrainName(String name) {
        _trainNameBox.setText(name);        
    }
    
    protected String getTrainName() {
        String trainName = _trainNameBox.getText();
        if (trainName == null || trainName.length() == 0) {
            trainName = _dccNumBox.getText();
        }
        return trainName;
    }
    
    protected void setAddress(String address) {
        _dccNumBox.setText(address);
        if (address==null) {
            _rosterBox.setSelectedIndex(0);
        }
    }
    
    protected String getAddress() {
        return _dccNumBox.getText();        
    }
    
    protected String getTrainId() {
        return _trainId;
    }
    
    protected DccLocoAddress getLocoAddress() {
        if (_train!=null) {
            return _train.getDccLocoAddress();
        }
        if (_trainId!=null) {
            String numId;
            int index = _trainId.indexOf('(');
            if (index >= 0) {
                numId = _trainId.substring(0, index);
            } else {
                numId = _trainId;
            }
            boolean isLong = true;
            if ((index + 1) < _trainId.length()
                    && (_trainId.charAt(index + 1) == 'S' || _trainId.charAt(index + 1) == 's')) {
                isLong = false;
            }
            try {
                int num = Integer.parseInt(numId);
                return new DccLocoAddress(num, isLong);
            } catch (NumberFormatException e) {
                return null;
            }            
        }
        return null;
    }
    
    protected String checkLocoAddress() {
        if (_train != null || _trainId != null) {
            return null;
        }
        return setTrainInfo(_dccNumBox.getText());
    }

/******************************* route info *******************/
    /**
     * Does the action on each of the 4 RouteLocation panels
     */
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
//        if (log.isDebugEnabled()) log.debug("actionPerformed: source "+((Component)obj).getName()+
//                     " id= "+e.getID()+", ActionCommand= "+e.getActionCommand());
        doAction(obj);
    }
    
    @SuppressWarnings("unchecked") // parameter can be any of several types, including JComboBox<String>
    void doAction(Object obj) {
        if (obj instanceof JTextField) {
            JTextField box = (JTextField)obj;
            if (!_origin.checkBlockBox(box)) {
                if (!_destination.checkBlockBox(box)) {
                    if (!_via.checkBlockBox(box)) {
                        if (!_avoid.checkBlockBox(box)) {
                        }
                    }
                }
            }
        } else {
            JComboBox<String> box = (JComboBox<String>)obj;
            if (!_origin.checkPathBox(box)) {
                if (!_destination.checkPathBox(box)) {
                    if (!_via.checkPathBox(box)) {
                        if (!_avoid.checkPathBox(box)) {
                            if (_origin.checkPortalBox(box)) {
                                _origin.setOrderExitPortal();
                            }
                            if (_destination.checkPortalBox(box)) {
                                _destination.setOrderEntryPortal();
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected JPanel makeBlockPanels() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel oPanel = _origin.makePanel("OriginBlock", "OriginToolTip", "PathName", "ExitPortalName", this);
        panel.add(oPanel);

        oPanel = _destination.makePanel("DestBlock", "DestToolTip", "PathName", "EntryPortalName", this);
        panel.add(oPanel);

        oPanel = _via.makePanel("ViaBlock", "ViaToolTip", "PathName", null, this);
        panel.add(oPanel);

        oPanel = _avoid.makePanel("AvoidBlock", "AvoidToolTip", "PathName", null, this);
        panel.add(oPanel);
        return panel;
    }

    private JPanel makeLabelCombo(String title, JComboBox<String> box, String tooltip) {

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setToolTipText(Bundle.getMessage(tooltip));
        box.setToolTipText(Bundle.getMessage(tooltip));
        p.add(new JLabel(PAD+Bundle.getMessage(title)+PAD), BorderLayout.NORTH);
        p.add(box, BorderLayout.CENTER);
        box.setBackground(Color.white);           
        box.addActionListener(this);
        box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        return p;
    }

    private boolean setOriginBlock() {
        return _origin.setBlock();
    }

    private boolean setDestinationBlock() {
        return _destination.setBlock();
    }
    
    private boolean setViaBlock() {
        return _via.setBlock();
    }
    
    private boolean setAvoidBlock() {
        return _avoid.setBlock();
    }
        
    /*********** route blocks **************************/
    
    protected class RouteLocation extends java.awt.event.MouseAdapter  {

        Location location;
        private BlockOrder order;
        JTextField blockBox = new JTextField();
        private JComboBox<String> pathBox = new JComboBox<String>();
        private JComboBox<String> portalBox;
 
        RouteLocation(Location loc) {
            location = loc;
            if (location==Location.ORIGIN ||location==Location.DEST) {
                portalBox = new JComboBox<String>();
            }
        }
        
        private JPanel makePanel(String title, String tooltip, String box1Name, String box2Name, WarrantRoute parent) {
            JPanel oPanel = new JPanel();
            oPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                    Bundle.getMessage(title),
                    javax.swing.border.TitledBorder.CENTER,
                    javax.swing.border.TitledBorder.TOP));
            JPanel hPanel = new JPanel();
            hPanel.setLayout(new BoxLayout(hPanel, BoxLayout.X_AXIS));
            hPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            hPanel.add(makeBlockBox(tooltip));
            hPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            JPanel pPanel = new JPanel();
            pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.X_AXIS));
            pPanel.add(makeLabelCombo(box1Name, pathBox, tooltip));
            pPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            
            if (box2Name != null) {
                pPanel.add(makeLabelCombo(box2Name, portalBox, tooltip)); 
                pPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            }
            hPanel.add(pPanel);
            oPanel.add(hPanel);
            pPanel.setToolTipText(Bundle.getMessage(tooltip));
            hPanel.setToolTipText(Bundle.getMessage(tooltip));
            oPanel.setToolTipText(Bundle.getMessage(tooltip));

            blockBox.addActionListener(parent);
            blockBox.addPropertyChangeListener(parent);
            blockBox.addMouseListener(this);

            return oPanel;          
        }
        private JPanel makeBlockBox(String tooltip) {
            blockBox.setDragEnabled(true);
            blockBox.setTransferHandler(new jmri.util.DnDStringImportHandler());
            blockBox.setColumns(20);
            blockBox.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.setToolTipText(Bundle.getMessage(tooltip));
            blockBox.setToolTipText(Bundle.getMessage(tooltip));
            p.add(new JLabel(Bundle.getMessage("BlockName")), BorderLayout.NORTH);
            p.add(blockBox, BorderLayout.CENTER);
            return p;
        }
        
        private void clearFields() {
            setBlock(null);
        }

        private boolean checkBlockBox(JTextField box) {
            if (box == blockBox) {
                setBlock(getEndPointBlock());
                return true;
            }
            return false;
        }
        private boolean checkPathBox(JComboBox<String> box) {
            if (box == pathBox) {
                if (portalBox!=null) {
                    setPortalBox(order);                    
                }
                return true;
            }
            return false;
        }
        private boolean checkPortalBox(JComboBox<String> box) {
            return (box == portalBox);
        }
        private void setOrderEntryPortal()  {
            if (order!=null) {
                order.setEntryName((String)portalBox.getSelectedItem());
            }
        }
        private void setOrderExitPortal()  {
            if (order!=null) {
                order.setExitName((String)portalBox.getSelectedItem());         
            }
        }

        protected void setOrder(BlockOrder o) {
            if (o!=null) {
                // setting blockBox text triggers doAction, so allow that to finish
                order = new BlockOrder(o);
                OBlock block = o.getBlock();
                blockBox.setText(block.getDisplayName());
                setPathBox(block);
                setPathName(o.getPathName());               
                setPortalBox(o);
                if (location==Location.DEST) {
                    setPortalName(o.getEntryName());            
                } else if (location==Location.ORIGIN){
                    setPortalName(o.getExitName());             
                }
            }
         }
        private BlockOrder getOrder() {
            return order;
        }
        private void setPortalName(String name) {
            portalBox.setSelectedItem(name);
        }
        private void setPathName(String name) {
            pathBox.setSelectedItem(name);
        }
        protected String getBlockName() {
            return blockBox.getText();
        }
        
        private OBlock getEndPointBlock() {
            String text = blockBox.getText();
            int idx = text.indexOf(java.awt.event.KeyEvent.VK_TAB);
            if (idx > 0){
                if (idx+1 < text.length()) {
                    text = text.substring(idx+1);
                } else {
                    text = text.substring(0, idx);
                }
            }
            blockBox.setText(text);
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getOBlock(text);
            if (block == null && text.length()>0) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("BlockNotFound", text),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
            return block;
        }

        private boolean setBlock() {
            return setBlock(getEndPointBlock());
        }
        private boolean setBlock(OBlock block) {
            boolean result = true;
            if (block == null) {
                result = false;
                order = null;
            } else {
                if (order!= null && block==order.getBlock() &&
                        pathIsValid(block, order.getPathName())==null) {
                    result = true; 
                } else {
                    if (pathsAreValid(block)) {
                        order = new BlockOrder(block);
                        if (!setPathBox(block)) {
                            result = false;
                        } else {
                            setPortalBox(order);                            
                        }
                    } else {
                        result = false;
                    }
                }
            }
            if (result) {
                // block cannot be null here. it is protected by result==true
                if (block!=null) blockBox.setText(block.getDisplayName());
                order.setPathName((String)pathBox.getSelectedItem());
                if (location==Location.DEST) {
                    order.setEntryName((String)portalBox.getSelectedItem());            
                } else if (location==Location.ORIGIN){
                    order.setExitName((String)portalBox.getSelectedItem());             
                }
                setNextLocation();
            } else {
                blockBox.setText(null);
                pathBox.removeAllItems();
                if (portalBox!=null) {
                    portalBox.removeAllItems();                 
                }
            }
            return result; 
        }
        private boolean setPathBox(OBlock block) {
            pathBox.removeAllItems();
            if (portalBox!=null) {
                portalBox.removeAllItems();
            }
            if (block==null) {
                return false;
            }
            List <Path> list = block.getPaths();
            if (list.size()==0) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("NoPaths", block.getDisplayName()),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
            for (int i=0; i<list.size(); i++) {
                 pathBox.addItem(((OPath)list.get(i)).getName());
            }
            return true;
        }
        private void setPortalBox(BlockOrder order) {
            if (portalBox==null) {
                return;
            }
            portalBox.removeAllItems();
            String pathName = (String)pathBox.getSelectedItem();
            if (order==null) {
                return;
            }
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
//                if (log.isDebugEnabled()) log.debug("setPortalBox: Path "+path.getName()+
//                             " set in block "+order.getBlock().getDisplayName());
            } else {
//                if (log.isDebugEnabled()) log.debug("setPortalBox: Path set to null in block"
//                             +order.getBlock().getDisplayName());
            }
        }
        
        private void setNextLocation() {
            switch (location) {
                case ORIGIN:
                    _focusedField = _destination;
                    break;
                case DEST:
                    _focusedField = _via;
                    break;
                case VIA:
                    _focusedField = _avoid;
                    break;
                case AVOID:
                    _focusedField = _origin;
                    break;
            }               
         }

        @Override
        public void mouseClicked(MouseEvent e) {
            _focusedField = this;
        }
    }       // end RouteLocation

    protected void mouseClickedOnBlock(OBlock block) {
        if (_focusedField!=null) {
            _focusedField.setBlock(block);
        } else {
            _origin.setBlock(block);
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
    protected String findRoute() {
        // read and verify origin and destination blocks/paths/portals
        String msg = null;
        BlockOrder order = null;
        String pathName = null;
        if (setOriginBlock()) {
            order = _origin.getOrder();
            pathName = order.getPathName();
            if (pathName!=null) {
                if (order.getExitName() == null) {
                    msg = Bundle.getMessage("SetExitPortal", Bundle.getMessage("OriginBlock"));
                } else {
                    msg = pathIsValid(order.getBlock(), pathName);
                }
            } else {
                msg = Bundle.getMessage("SetPath", Bundle.getMessage("OriginBlock"));
            }
        } else {
            msg = Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock"));
        }
        if (msg==null) {
            if (setDestinationBlock()) {
                order = _destination.getOrder();
                pathName = order.getPathName();
                if (pathName!=null) {
                    if (order.getEntryName() == null) {
                        msg = Bundle.getMessage("SetEntryPortal", Bundle.getMessage("DestBlock"));
                    } else {
                        msg = pathIsValid(order.getBlock(), pathName);
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
                order = _via.getOrder();
                if (order!=null && order.getPathName()==null) {
                    msg = Bundle.getMessage("SetPath", Bundle.getMessage("ViaBlock"));
                }
            }
        }
        if (msg==null) {
            if (setAvoidBlock()) {
                order = _avoid.getOrder();
                if (order!=null && order.getPathName()==null) {
                    msg = Bundle.getMessage("SetPath", Bundle.getMessage("AvoidBlock"));
                }
            }
        }
        if (msg==null) {
            if (log.isDebugEnabled()) log.debug("Params OK. findRoute() is creating a RouteFinder");
            _routeFinder = new RouteFinder(this, _origin.getOrder(), _destination.getOrder(),
                    _via.getOrder(), _avoid.getOrder(), getDepth());
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
        return _via.getOrder();
    }
    
    public BlockOrder getAvoidBlockOrder() {
        return _avoid.getOrder();
    }
    
    private Warrant _tempWarrant;   // only used in pickRoute() method
    protected void clearTempWarrant() {
        if (_tempWarrant!=null) {
            _tempWarrant.deAllocate();
            _tempWarrant = null;
        }        
    }
    private void showTempWarrant(ArrayList<BlockOrder> orders) {
        String s = (""+Math.random()).substring(4);
        _tempWarrant = new Warrant("IW"+s+"TEMP", null);
        _tempWarrant.setBlockOrders(orders);
        String msg = _tempWarrant.setRoute(0, orders);
        if (msg!=null) {
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);            
        }
    }

   /**
    *  Callback from RouteFinder - several routes found
    */
    protected void pickRoute(List <DefaultMutableTreeNode> destNodes, DefaultTreeModel routeTree) {
        if (destNodes.size()==1) {
            showRoute(destNodes.get(0), routeTree);
            selectedRoute(_orders);
            return;
        }
        _pickRouteDialog = new JDialog(this, Bundle.getMessage("DialogTitle"), false);
        _pickRouteDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                clearTempWarrant();
            }
        });
        _tempWarrant = null;
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
                    Integer.valueOf(destNodes.get(i).getLevel())+1) );
            button.setActionCommand(""+i);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    clearTempWarrant();
                }               
            });
            buttons.add(button);
            panel.add(button);
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        JButton ok = new JButton(Bundle.getMessage("ButtonSelect"));
        ok.addActionListener(new ActionListener() {
                ButtonGroup buts;
                JDialog dialog;
                List <DefaultMutableTreeNode> dNodes;
                DefaultTreeModel tree;
                public void actionPerformed(ActionEvent e) {
                    if (buts.getSelection()!=null) {
                        clearTempWarrant();
                        int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                        showRoute(dNodes.get(i), tree);
                        selectedRoute(_orders);
                        dialog.dispose();
                    } else {
                        showWarning(Bundle.getMessage("SelectRoute"));
                    }
                }
                ActionListener init(ButtonGroup bg, JDialog d, List <DefaultMutableTreeNode> dn,
                                    DefaultTreeModel t) {
                    buts = bg;
                    dialog = d;
                    dNodes = dn;
                    tree = t;
                    return this;
                }
            }.init(buttons, _pickRouteDialog, destNodes, routeTree));
        ok.setMaximumSize(ok.getPreferredSize());
        JButton show = new JButton(Bundle.getMessage("ButtonReview"));
        show.addActionListener(new ActionListener() {
                ButtonGroup buts;
                List <DefaultMutableTreeNode> destinationNodes;
                DefaultTreeModel tree;
                public void actionPerformed(ActionEvent e) {
                    if (buts.getSelection()!=null) {
                        clearTempWarrant();
                        int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                        showRoute(destinationNodes.get(i), tree);
                        showTempWarrant(_orders);
                    } else {
                        showWarning(Bundle.getMessage("SelectRoute"));
                    }
                }
                ActionListener init(ButtonGroup bg, List <DefaultMutableTreeNode> dn,
                                    DefaultTreeModel t) {
                    buts = bg;
                    destinationNodes = dn;
                    tree = t;
                    return this;
                }
            }.init(buttons, destNodes, routeTree));
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
        _pickRouteDialog.setLocation(getLocation().x-20, getLocation().y+150);
        _pickRouteDialog.pack();
        _pickRouteDialog.setVisible(true);
    }
    
    public void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);        
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
                                        origin.getPathName(), origin.getExitName(), dest.getBlock().getDisplayName(),
                                         dest.getEntryName(), dest.getPathName(), Integer.valueOf(getDepth()) }),                                       
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
        clearFrames();
        clearFields();
        _focusedField = _origin;
        _routeModel.fireTableDataChanged();
    }
    private void clearFrames() {
        
        if (_debugFrame!=null) {
            _debugFrame.dispose();
            _debugFrame = null;
        }
        if (_pickRouteDialog!=null) {
            _pickRouteDialog.dispose();
            _pickRouteDialog = null;
        }       
    }
    private void clearFields() {
        _origin.clearFields();
        _destination.clearFields();
        _via.clearFields();
        _avoid.clearFields();       
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

    static private String pathIsValid(OBlock block, String pathName) {
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
    
    @Override
    public void dispose() {
        clearFrames();
        super.dispose();
    }

    /************************* Route Table ******************************/
    class RouteTableModel extends AbstractTableModel {
        /**
         * 
         */
        private static final long serialVersionUID = 1966890806689115258L;
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

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case BLOCK_COLUMN: return Bundle.getMessage("BlockCol");
                case ENTER_PORTAL_COL: return Bundle.getMessage("EnterPortalCol");
                case PATH_COLUMN: return Bundle.getMessage("PathCol");
                case DEST_PORTAL_COL: return Bundle.getMessage("DestPortalCol");
            }
            return "";
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            return new JTextField(15).getPreferredSize().width;
        }

        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _orders.size()){
                return "";
            }
            BlockOrder bo = _orders.get(row);
            // some error checking
            if (bo == null){
                log.error("BlockOrder is null");
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

        @Override
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
    
    /**
    *
    * @param vertical  Label orientation true = above, false = left
    * @param textField
    * @param label String label message
    * @return
    */
   static protected JPanel makeTextBoxPanel(boolean vertical, JComponent textField, String label, String tooltip) {
       JPanel panel = new JPanel();
       JLabel l = new JLabel(Bundle.getMessage(label));
       if (vertical) {
           panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
           l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
           textField.setAlignmentX(JComponent.CENTER_ALIGNMENT);
           panel.add(Box.createVerticalStrut(STRUT_SIZE));
       } else {
           panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
           l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
           textField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
           panel.add(Box.createHorizontalStrut(STRUT_SIZE));
       }
       panel.add(l);
       if (!vertical) {
           panel.add(Box.createHorizontalStrut(STRUT_SIZE));
       }
       textField.setMaximumSize(new Dimension(300, textField.getPreferredSize().height));
       textField.setMinimumSize(new Dimension(30, textField.getPreferredSize().height));
       panel.add(textField);
       if (vertical) {
           panel.add(Box.createVerticalStrut(STRUT_SIZE));
       } else {
           panel.add(Box.createHorizontalStrut(STRUT_SIZE));
       }
       if (textField instanceof JTextField || textField instanceof JComboBox) {
           textField.setBackground(Color.white);           
       }
       if (tooltip!=null) {
           panel.setToolTipText(tooltip);
           textField.setToolTipText(Bundle.getMessage(tooltip));
           l.setToolTipText(Bundle.getMessage(tooltip));           
       }
       return panel;
   }
    
    private final static Logger log = LoggerFactory.getLogger(WarrantRoute.class.getName());
}
