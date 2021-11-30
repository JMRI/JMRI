package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
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
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make panels for WarrantFrame and NXFrame windows that create and edit
 * Warrants.
 * <p>
 * Input panels for defining a train's route from an eNtry OBlock to an eXit
 * OBlock. Routes are defined by choosing the originating block, the path on
 * which the train start and the exit Portal through which it will leave the
 * block. Also it is required that a Destination block is chosen and the path
 * and Portal through which the train will arrive. The Portal selections
 * establish the direction information. Optionally, additional blocks can be
 * specified requiring the train to pass through or avoid entering.
 * <p>
 * Input panels to describe the train. accesses the roster for some info.
 *
 * @author Peter Cressman
 *
 */
abstract class WarrantRoute extends jmri.util.JmriJFrame implements ActionListener, PropertyChangeListener {

    enum Location {
        ORIGIN, DEST, VIA, AVOID
    }
    enum Display {
        MPH("mph"), KPH("kph"), MMPS("mmps"), INPS("inps"), IN("in"), CM("cm");
        String _bundleKey;
        Display(String bundleName) {
            _bundleKey = bundleName;
        }
        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
    }
    static class DisplayButton extends JButton {
        Display pref;
        DisplayButton(Display p) {
            super();
            setDisplayPref(p);
        }
        void setDisplayPref(Display p) {
            pref = p;
            setText(p.toString());
        }
        Display getDisplyPref() {
            return pref;
        }
    }
    protected RouteLocation _origin = new RouteLocation(Location.ORIGIN);
    protected RouteLocation _destination = new RouteLocation(Location.DEST);
    protected RouteLocation _via = new RouteLocation(Location.VIA);
    protected RouteLocation _avoid = new RouteLocation(Location.AVOID);
    protected RouteLocation _focusedField;

    protected SpeedUtil _speedUtil;
    protected Display _displayPref; // speed units preference
    protected Display _units;       // distance units preference
    protected float _scale = 87.1f;

    static int STRUT_SIZE = 10;
    private int _depth = 20;

    static String PAD = "               ";
    private JDialog _pickRouteDialog;
    private final RouteTableModel _routeModel;
    protected ArrayList<BlockOrder> _orders;
    private JFrame _debugFrame;
    private RouteFinder _routeFinder;
    private final JTextField _searchDepth = new JTextField(5);
    private JButton _calculateButton = new JButton(Bundle.getMessage("Calculate"));
    private JButton _stopButton;

    private final JComboBox<String> _rosterBox = new JComboBox<>();
    private final AddressTextField _dccNumBox = new AddressTextField();
    private final JTextField _trainNameBox = new JTextField(6);
    private final JButton _viewProfile = new JButton(Bundle.getMessage("ViewProfile"));
    private JmriJFrame _spTable = null;
    private JmriJFrame _pickListFrame;


    /**
     * Only subclasses can create this
     */
    protected WarrantRoute() {
        super(false, true);
        if (log.isDebugEnabled()) log.debug("newInstance");
        _searchDepth.setText(Integer.toString(_depth));
        _routeModel = new RouteTableModel();
        _speedUtil = new SpeedUtil();

        int interpretation = SignalSpeedMap.SPEED_KMPH;
        WarrantPreferences wp = WarrantPreferences.getDefault();
        if (wp != null) {
            interpretation = WarrantPreferences.getDefault().getInterpretation();
            _scale = wp.getLayoutScale();
        }
        if (interpretation == SignalSpeedMap.SPEED_MPH) {
            _displayPref = Display.MPH;
            _units = Display.IN;
        } else if (interpretation == SignalSpeedMap.SPEED_KMPH) {
            _displayPref = Display.KPH;
            _units = Display.CM;
        } else {
            _displayPref = Display.INPS;
            _units = Display.IN;
        }
        setupRoster();
    }

    protected abstract void selectedRoute(ArrayList<BlockOrder> orders);
    protected abstract void maxThrottleEventAction();

    @Override
    public abstract void propertyChange(java.beans.PropertyChangeEvent e);
    
    protected void setSpeedUtil(SpeedUtil sp) {
        _speedUtil = sp;
    }

    static class AddressTextField extends JTextField implements FocusListener {
        public AddressTextField() {
            super();
            addFocusListener(this);
        }
        @Override
        public void focusGained(FocusEvent e) {
            
        }
        @Override
        public void focusLost(FocusEvent e) {
            fireActionPerformed();
        }
    }

    /* ************************* Panel for Route search depth **********************/
    /**
     * @return How many nodes deep the tree search should be
     */
    private int getDepth() {
        try {
            int i = Integer.parseInt(_searchDepth.getText());
            if (i > 2 ) {
                _depth = i;
            }
        } catch (NumberFormatException nfe) {
            // ignore
        }
        return _depth;
    }

    protected JPanel searchDepthPanel(boolean vertical) {
        _searchDepth.setText(Integer.toString(_depth));
        JPanel p = new JPanel();
        p.add(Box.createHorizontalGlue());
        p.add(makeTextBoxPanel(vertical, _searchDepth, "SearchDepth", "ToolTipSearchDepth"));
        _searchDepth.setColumns(5);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    protected JPanel calculatePanel(boolean vertical) {
        _calculateButton.setMaximumSize(_calculateButton.getPreferredSize());
        _calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearTempWarrant();
                calculate();
            }
        });

        _stopButton = new JButton(Bundle.getMessage("Stop"));
        _stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopRouteFinder();
            }
        });

        JPanel panel = new JPanel();
        panel.add(makeTextBoxPanel(vertical, _calculateButton, "CalculateRoute", null));
        panel.add(makeTextBoxPanel(vertical, _stopButton, "StopSearch", null));
        return panel;
    }
    public JPanel makePickListPanel() {
        JButton button = new JButton(Bundle.getMessage("MenuBlockPicker"));
        button.setMaximumSize(_calculateButton.getPreferredSize());
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (_pickListFrame !=null) {
                    _pickListFrame.dispose();
                }
                _pickListFrame = new JmriJFrame();
                PickListModel<OBlock> model = PickListModel.oBlockPickModelInstance();
                _pickListFrame.add(new JScrollPane(model.makePickTable()));
                _pickListFrame.pack();
                _pickListFrame.setVisible(true);
            }
        });
        JPanel p = new JPanel();
        p.add(button);
        return p;
    }


    /* ************************* Train ID info: Loco Address, etc **********************/
    /**
     * Make panel containing TextFields for Train name and address and ComboBox
     * for Roster entries. called from:
     * WarrantFrame.makeBorderedTrainPanel() at init of WarrantFrame
     * NXFrame.makeAutoRunPanel() at init of NXFrame
     * 
     *
     * @param comp optional panel to add
     * @return panel
     */
    protected JPanel makeTrainIdPanel(JPanel comp) {
        JPanel trainPanel = new JPanel();
        trainPanel.setLayout(new BoxLayout(trainPanel, BoxLayout.LINE_AXIS));
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(makeTextBoxPanel(false, _trainNameBox, "TrainName", "noTrainName"));
        panel.add(makeTextBoxPanel(false, _rosterBox, "Roster", null));
        panel.add(Box.createVerticalStrut(2));
        panel.add(makeTextBoxPanel(false, _dccNumBox, "DccAddress", null));
        _dccNumBox.addActionListener((ActionEvent e) -> {
            checkAddress();
        });

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        p.add(_viewProfile);
        _viewProfile.addActionListener((ActionEvent e) -> {
            showProfile();
        });
        panel.add(p);
        if (comp != null) {
            panel.add(comp);
        }
        trainPanel.add(panel);
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        
        return trainPanel;
    }

    private void setupRoster() {
        List<RosterEntry> list = Roster.getDefault().matchingList(null, null, null, null, null, null, null);
        _rosterBox.setRenderer(new jmri.jmrit.roster.swing.RosterEntryListCellRenderer());
        _rosterBox.addItem(" ");
        _rosterBox.addItem(Bundle.getMessage("noSuchAddress"));
        for (int i = 0; i < list.size(); i++) {
            RosterEntry r = list.get(i);
            _rosterBox.addItem(r.titleString());
        }
        _rosterBox.setMaximumSize(_rosterBox.getPreferredSize());
        _rosterBox.addActionListener((ActionEvent e) -> {
            checkAddress();
        });
    }

    private void showProfile() {
        closeProfileTable();

        String id = _speedUtil.getRosterId();
        if (id == null || id.isEmpty()) {
            return;
        }
        if (Roster.getDefault().getEntryForId(id) == null) {
            DccLocoAddress dccAddr = _speedUtil.getDccAddress();
            String rosterId = JOptionPane.showInputDialog(this,
                    Bundle.getMessage("makeRosterEntry", _speedUtil.getAddress()),
                    Bundle.getMessage("QuestionTitle"),
                    JOptionPane.QUESTION_MESSAGE);
            if (log.isDebugEnabled()) {
                log.debug("Create roster entry {}", rosterId);
            }
            if (rosterId != null && !rosterId.isEmpty()) {
                RosterEntry rosterEntry = new RosterEntry();
                Roster.getDefault().addEntry(rosterEntry);
                rosterEntry.setId(rosterId);
                rosterEntry.setDccAddress(String.valueOf(dccAddr.getNumber()));
                rosterEntry.setProtocol(dccAddr.getProtocol());
                rosterEntry.ensureFilenameExists();
                WarrantManager mgr = InstanceManager.getDefault(WarrantManager.class);
                RosterSpeedProfile mergeProfile = _speedUtil.getMergeProfile();
                mgr.setMergeProfile(rosterId, mergeProfile);
                mgr.getMergeProfiles().remove(id);
                _speedUtil.setRosterId(rosterId);
            }
        }

        JPanel viewPanel = makeViewPanel(id);
        if (viewPanel == null) {
            if (id.charAt(0) != '$' || id.charAt(id.length()-1) != '$') {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("NoSpeedProfile", id),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            } 
            return;
        }
        _spTable = new JmriJFrame(false, true);
        JPanel framePanel = new JPanel();
        framePanel.setLayout(new BoxLayout(framePanel, BoxLayout.PAGE_AXIS));
        framePanel.add(Box.createGlue());

        framePanel.add(viewPanel);
        _spTable.getContentPane().add(framePanel);
        _spTable.pack();
        _spTable.setVisible(true);
    }

    JPanel makeViewPanel(String id) {
        RosterSpeedProfile speedProfile = _speedUtil.getMergeProfile();
        RosterEntry re = Roster.getDefault().getEntryForId(id);
        RosterSpeedProfile rosterSpeedProfile;
        if (re != null) {
            rosterSpeedProfile = re.getSpeedProfile();
        } else {
            rosterSpeedProfile = null;
        }
        if ((speedProfile == null || speedProfile.getProfileSize() == 0) && 
                (rosterSpeedProfile == null || rosterSpeedProfile.getProfileSize() == 0)) {
            _viewProfile.setEnabled(false);
            return null;
        } else {
            _viewProfile.setEnabled(true);
        }
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.PAGE_AXIS));
        viewPanel.add(Box.createGlue());
        JPanel panel = new JPanel();
        panel.add(MergePrompt.makeEditInfoPanel(id));
        viewPanel.add(panel);

        JPanel spPanel = new JPanel();
        spPanel.setLayout(new BoxLayout(spPanel, BoxLayout.LINE_AXIS));
        spPanel.add(Box.createGlue());

        if (rosterSpeedProfile != null ) {
            Map<Integer, Boolean> anomilies = MergePrompt.validateSpeedProfile(rosterSpeedProfile);
            spPanel.add(MergePrompt.makeSpeedProfilePanel("rosterSpeedProfile", rosterSpeedProfile,  false, anomilies));
            spPanel.add(Box.createGlue());
        }
        if (speedProfile != null) {
            Map<Integer, Boolean> anomaly = MergePrompt.validateSpeedProfile(speedProfile);
            spPanel.add(MergePrompt.makeSpeedProfilePanel("mergedSpeedProfile", speedProfile, true, anomaly));
            spPanel.add(Box.createGlue());
        }

        viewPanel.add(spPanel);
        return viewPanel;
    }


    protected void closeProfileTable() {
        if (_spTable != null) {
            _spTable.dispose();
            _spTable = null;
        }            
    }

    // called by WarrantFrame.setup()
    protected String setTrainInfo(String name) {
        if (log.isDebugEnabled()) {
            log.debug("setTrainInfo for: {}", name);
        }
        setTrainName(name);
        _dccNumBox.setText(_speedUtil.getAddress());
        setRosterBox();
        if (name == null) {
            RosterEntry re = _speedUtil.getRosterEntry();
            if (re != null) {
                setTrainName(re.getRoadNumber());
                setRosterBox();
            } else {
                setTrainName(_speedUtil.getAddress()); 
            }
        }
        return null;
    }
    
    private void setRosterBox() {
        String id = _speedUtil.getRosterId();
        if (id != null && id.equals(_rosterBox.getSelectedItem())) {
            return;
        }
        if (id != null && id.charAt(0) != '$' && id.charAt(id.length()-1) !='$') {
            _rosterBox.setSelectedItem(id);
        } else {
            _rosterBox.setSelectedItem(Bundle.getMessage("noSuchAddress"));
        }
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

    private void checkAddress() {
        String msg = setAddress();
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Called to make final consistency check on loco address before running warrant
     * @return error message
     */
    protected String setAddress() {
        String msg = null;
        String suAddr = _speedUtil.getAddress();
        String addrText = _dccNumBox.getText();
        String suId = _speedUtil.getRosterId();
        boolean textChange = false;
        if ( !addrText.equals(suAddr) || suId == null) {
            textChange = true;
            if (!_speedUtil.setAddress(_dccNumBox.getText())) {
                msg = Bundle.getMessage("BadDccAddress", _dccNumBox.getText());
            } else {   // else address OK.
                suAddr = _speedUtil.getAddress();
                _dccNumBox.setText(suAddr);  // add protocol string
                suId = _speedUtil.getRosterId();
                maxThrottleEventAction();
                if (suId != null && !(suId.charAt(0) == '$' && suId.charAt(suId.length()-1) =='$')) {
                    _rosterBox.setSelectedItem(suId);
                } else {
                    _rosterBox.setSelectedItem(Bundle.getMessage("noSuchAddress"));
                    return null;
                }
            }
        }

        String id = (String)_rosterBox.getSelectedItem();
        RosterEntry re = Roster.getDefault().getEntryForId(id);
        boolean isRoster = (re != null);
        suId = _speedUtil.getRosterId();
        if (suId != null && suId.charAt(0) == '$' && suId.charAt(suId.length()-1) =='$') {
            isRoster = true;
        }
        if (!textChange && !isRoster) {
            _dccNumBox.setText(null);
            return null;
        }
        if (re != null) {
           if (!re.getDccLocoAddress().equals(_speedUtil.getDccAddress())) {
               _speedUtil.setRosterId(id);
           }
           _dccNumBox.setText(re.getDccLocoAddress().toString());
           maxThrottleEventAction();
           msg = null;
        } else if (msg == null) {
            _rosterBox.setSelectedItem(Bundle.getMessage("noSuchAddress"));
        }
        return msg;
    }

    protected String getAddress() {
        return _dccNumBox.getText();
    }

    protected String checkLocoAddress() {
        if (_speedUtil.getDccAddress() == null) {
            return Bundle.getMessage("BadDccAddress", _dccNumBox.getText());
        }
        return null;
    }

    protected void calculate() {
        String msg = findRoute();
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /* ****************************** route info *******************/
    /**
     * Does the action on each of the 4 RouteLocation panels
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (log.isTraceEnabled()) {
            log.trace("actionPerformed: source {} id= {}, ActionCommand= ", ((Component) obj).getName(), e.getID(), e.getActionCommand());
        }
        doAction(obj);
    }

    @SuppressWarnings("unchecked") // parameter can be any of several types, including JComboBox<String>
    void doAction(Object obj) {
        if (obj instanceof JTextField) {
            JTextField box = (JTextField) obj;
            if (!_origin.checkBlockBox(box)) {
                if (!_destination.checkBlockBox(box)) {
                    if (!_via.checkBlockBox(box)) {
                        _avoid.checkBlockBox(box);
                    }
                }
            }
        } else {
            JComboBox<String> box = (JComboBox<String>) obj;
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

    protected JPanel makeBlockPanels(boolean add) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel oPanel = _origin.makePanel("OriginBlock", "OriginToolTip", "PathName", "ExitPortalName", this);
        panel.add(oPanel);

        oPanel = _destination.makePanel("DestBlock", "DestToolTip", "PathName", "EntryPortalName", this);
        panel.add(oPanel);

        oPanel = _via.makePanel("ViaBlock", "ViaToolTip", "PathName", null, this);

        JPanel aPanel = _avoid.makePanel("AvoidBlock", "AvoidToolTip", "PathName", null, this);

        if (add) {
            JPanel pLeft = new JPanel();
            pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.PAGE_AXIS));
            pLeft.add(oPanel);
            pLeft.add(aPanel);
            
            JPanel pRight = new JPanel();
            pRight.setLayout(new BoxLayout(pRight, BoxLayout.PAGE_AXIS));
            pRight.add(searchDepthPanel(true));
            pRight.add(makePickListPanel());
            pRight.add(calculatePanel(true));
            
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
            p.add(pLeft);
            p.add(pRight);
            panel.add(p);
        } else {
            panel.add(oPanel);
            panel.add(aPanel);
        }
        return panel;
    }
    
    private JPanel makeLabelCombo(String title, JComboBox<String> box, String tooltip) {

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setToolTipText(Bundle.getMessage(tooltip));
        box.setToolTipText(Bundle.getMessage(tooltip));
        JLabel l = new JLabel(PAD + Bundle.getMessage(title) + PAD);
        p.add(l, BorderLayout.NORTH);
        l.setLabelFor(box);
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

    /* ********** route blocks **************************/
    protected class RouteLocation extends java.awt.event.MouseAdapter {

        Location location;
        private BlockOrder order;
        JTextField blockBox = new JTextField();
        private final JComboBox<String> pathBox = new JComboBox<>();
        JComboBox<String> portalBox;

        RouteLocation(Location loc) {
            location = loc;
            if (location == Location.ORIGIN || location == Location.DEST) {
                portalBox = new JComboBox<>();
            }
        }

        private JPanel makePanel(String title, String tooltip, String box1Name, String box2Name, WarrantRoute parent) {
            JPanel oPanel = new JPanel();
            oPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                    Bundle.getMessage(title),
                    javax.swing.border.TitledBorder.CENTER,
                    javax.swing.border.TitledBorder.TOP));
            JPanel hPanel = new JPanel();
            hPanel.setLayout(new BoxLayout(hPanel, BoxLayout.LINE_AXIS));
            hPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            hPanel.add(makeBlockBox(tooltip));
            hPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            JPanel pPanel = new JPanel();
            pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.LINE_AXIS));
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
            JLabel l = new JLabel(Bundle.getMessage("BlockName"));
            p.add(l, BorderLayout.NORTH);
            l.setLabelFor(blockBox);
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
                if (portalBox != null) {
                    setPortalBox(order);
                }
                return true;
            }
            return false;
        }

        private boolean checkPortalBox(JComboBox<String> box) {
            return (box == portalBox);
        }

        private void setOrderEntryPortal() {
            if (order != null) {
                order.setEntryName((String) portalBox.getSelectedItem());
            }
        }

        private void setOrderExitPortal() {
            if (order != null) {
                order.setExitName((String) portalBox.getSelectedItem());
            }
        }

        protected void setOrder(BlockOrder o) {
            if (o != null) {
                // setting blockBox text triggers doAction, so allow that to finish
                order = new BlockOrder(o);
                OBlock block = o.getBlock();
                blockBox.setText(block.getDisplayName());
                setPathBox(block);
                setPathName(o.getPathName());
                setPortalBox(o);
                if (location == Location.DEST) {
                    setPortalName(o.getEntryName());
                } else if (location == Location.ORIGIN) {
                    setPortalName(o.getExitName());
                }
            }
        }

        protected BlockOrder getOrder() {
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
            if (idx > 0) {
                if (idx + 1 < text.length()) {
                    text = text.substring(idx + 1);
                } else {
                    text = text.substring(0, idx);
                }
            }
            blockBox.setText(text);
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getOBlock(text);
            if (block == null && text.length() > 0) {
                JOptionPane.showMessageDialog(blockBox.getParent(), Bundle.getMessage("BlockNotFound", text),
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
                if (order != null && block == order.getBlock()
                        && pathIsValid(block, order.getPathName()) == null) {
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
                if (block != null) {
                    blockBox.setText(block.getDisplayName());
                }
                order.setPathName((String) pathBox.getSelectedItem());
                if (location == Location.DEST) {
                    order.setEntryName((String) portalBox.getSelectedItem());
                } else if (location == Location.ORIGIN) {
                    order.setExitName((String) portalBox.getSelectedItem());
                }
                setNextLocation();
            } else {
                blockBox.setText(null);
                pathBox.removeAllItems();
                if (portalBox != null) {
                    portalBox.removeAllItems();
                }
            }
            return result;
        }

        private boolean setPathBox(OBlock block) {
            pathBox.removeAllItems();
            if (portalBox != null) {
                portalBox.removeAllItems();
            }
            if (block == null) {
                return false;
            }
            List<Path> list = block.getPaths();
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("NoPaths", block.getDisplayName()),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
            for (int i = 0; i < list.size(); i++) {
                pathBox.addItem(((OPath) list.get(i)).getName());
            }
            return true;
        }

        private void setPortalBox(BlockOrder order) {
            if (portalBox == null) {
                return;
            }
            portalBox.removeAllItems();
            if (order == null) {
                return;
            }
            String pathName = (String) pathBox.getSelectedItem();
            order.setPathName(pathName);
            OPath path = order.getPath();
            if (path != null) {
                Portal portal = path.getFromPortal();
                if (portal != null) {
                    String name = portal.getName();
                    if (name != null) {
                        portalBox.addItem(name);
                    }
                }
                portal = path.getToPortal();
                if (portal != null) {
                    String name = portal.getName();
                    if (name != null) {
                        portalBox.addItem(name);
                    }
                }
                if (log.isTraceEnabled()) {
                    log.debug("setPortalBox: Path {} set in block {}", path.getName(), order.getBlock().getDisplayName());
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("setPortalBox: Path {} not found in block {}", pathName, order.getBlock().getDisplayName());
                }
                order.setPathName(null);
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
                default:
                    log.warn("Unhandled next location code: {}", location);
                    break;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            _focusedField = this;
        }
        @Override
        public void mousePressed(MouseEvent e) {
            _focusedField = this;
        }
    }       // end RouteLocation

    protected void mouseClickedOnBlock(OBlock block) {
        if (_focusedField != null) {
            _focusedField.setBlock(block);
        } else {
            _origin.setBlock(block);
        }
    }

    private boolean pathsAreValid(OBlock block) {
        List<Path> list = block.getPaths();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("NoPaths", block.getDisplayName()),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            OPath path = (OPath) list.get(i);
            if (path.getFromPortal() == null && path.getToPortal() == null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("PathNeedsPortal", path.getName(), block.getDisplayName()),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /* ****************************** Finding the route ********************************/
    /**
     * Gather parameters to search for a route
     *
     * @return Error message, if any
     */
    private String findRoute() {
        // read and verify origin and destination blocks/paths/portals
        String msg;
        BlockOrder order;
        String pathName;
        if (setOriginBlock()) {
            order = _origin.getOrder();
            pathName = order.getPathName();
            if (pathName != null) {
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
        if (msg == null) {
            if (setDestinationBlock()) {
                order = _destination.getOrder();
                pathName = order.getPathName();
                if (pathName != null) {
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
        if (msg == null) {
            if (setViaBlock()) {
                order = _via.getOrder();
                if (order != null && order.getPathName() == null) {
                    msg = Bundle.getMessage("SetPath", Bundle.getMessage("ViaBlock"));
                }
            }
        }
        if (msg == null) {
            if (setAvoidBlock()) {
                order = _avoid.getOrder();
                if (order != null && order.getPathName() == null) {
                    msg = Bundle.getMessage("SetPath", Bundle.getMessage("AvoidBlock"));
                }
            }
        }
        if (msg == null) {
            if (log.isDebugEnabled()) {
                log.debug("Params OK. findRoute() is creating a RouteFinder");
            }
            _routeFinder = new RouteFinder(this, _origin.getOrder(), _destination.getOrder(),
                    _via.getOrder(), _avoid.getOrder(), getDepth());
            jmri.util.ThreadingUtil.newThread(_routeFinder).start();
        }
        return msg;
    }

    protected void stopRouteFinder() {
        if (_routeFinder != null) {
            _routeFinder.quit();
            _routeFinder = null;
        }
    }

    /* *********************************** Route Selection **************************************/
    protected List<BlockOrder> getOrders() {
        return _orders;
    }

    protected BlockOrder getViaBlockOrder() {
        return _via.getOrder();
    }

    protected BlockOrder getAvoidBlockOrder() {
        return _avoid.getOrder();
    }

    private Warrant _tempWarrant;   // only used in pickRoute() method

    protected void clearTempWarrant() {
        if (_tempWarrant != null) {
            _tempWarrant.deAllocate();
        }
    }

    private void showTempWarrant(ArrayList<BlockOrder> orders) {
        String s = ("" + Math.random()).substring(4);
        if (_tempWarrant == null) {
            _tempWarrant = new Warrant("IW" + s + "TEMP", null);
            _tempWarrant.setBlockOrders(orders);
        }
        _tempWarrant.setRoute(true, orders);
        // Don't clutter with message - this is a temp display
    }

    /**
     * Callback from RouteFinder - several routes found
     *
     * @param destNodes the destination blocks
     * @param routeTree the routes
     */
    protected void pickRoute(List<DefaultMutableTreeNode> destNodes, DefaultTreeModel routeTree) {
        if (destNodes.size() == 1) {
            showRoute(destNodes.get(0), routeTree);
            selectedRoute(_orders);
            showTempWarrant(_orders);
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
        mainPanel.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage("NumberRoutes1", destNodes.size())));
        panel.add(new JLabel(Bundle.getMessage("NumberRoutes2")));
        JPanel wrapper = new JPanel();
        wrapper.add(panel);
        mainPanel.add(wrapper, BorderLayout.NORTH);
        ButtonGroup buttons = new ButtonGroup();

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (int i = 0; i < destNodes.size(); i++) {
            JRadioButton button = new JRadioButton(Bundle.getMessage("RouteSize", i + 1,
                    destNodes.get(i).getLevel() + 1));
            button.setActionCommand("" + i);
            button.addActionListener((ActionEvent e) -> {
                clearTempWarrant();
            });
            buttons.add(button);
            panel.add(button);
            if (destNodes.size() == 1) {
                button.setSelected(true);
            }
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        javax.swing.JViewport vp = scrollPane.getViewport();
        JRadioButton button = new JRadioButton(Bundle.getMessage("RouteSize", 000, 000));
        vp.setPreferredSize(new Dimension(button.getWidth(), _depth*button.getHeight()));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton ok = new JButton(Bundle.getMessage("ButtonSelect"));
        ok.addActionListener(new ActionListener() {
            ButtonGroup buts;
            JDialog dialog;
            List<DefaultMutableTreeNode> dNodes;
            DefaultTreeModel tree;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (buts.getSelection() != null) {
                    clearTempWarrant();
                    int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                    showRoute(dNodes.get(i), tree);
                    selectedRoute(_orders);
                    showTempWarrant(_orders);
                    dialog.dispose();
                } else {
                    showWarning(Bundle.getMessage("SelectRoute"));
                }
            }

            ActionListener init(ButtonGroup bg, JDialog d, List<DefaultMutableTreeNode> dn,
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
            List<DefaultMutableTreeNode> destinationNodes;
            DefaultTreeModel tree;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (buts.getSelection() != null) {
                    clearTempWarrant();
                    int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                    showRoute(destinationNodes.get(i), tree);
                    showTempWarrant(_orders);
                } else {
                    showWarning(Bundle.getMessage("SelectRoute"));
                }
            }

            ActionListener init(ButtonGroup bg, List<DefaultMutableTreeNode> dn,
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
        panel.add(Box.createHorizontalGlue());
        panel.add(show);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(ok);
        panel.add(Box.createHorizontalGlue());
        wrapper = new JPanel();
        wrapper.add(panel);
        mainPanel.add(wrapper, BorderLayout.SOUTH);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(makeRouteTablePanel());
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(mainPanel);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));

        _pickRouteDialog.getContentPane().add(panel);
        _pickRouteDialog.setLocation(getLocation().x - 20, getLocation().y + 150);
        _pickRouteDialog.pack();
        _pickRouteDialog.setVisible(true);
    }

    protected void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Callback from RouteFinder - exactly one route found
     *
     * @param destNode destination block
     * @param tree     possible routes
     */
    private void showRoute(DefaultMutableTreeNode destNode, DefaultTreeModel tree) {
        TreeNode[] nodes = tree.getPathToRoot(destNode);
        _orders = new ArrayList<>();
        for (TreeNode node : nodes) {
            _orders.add((BlockOrder) ((DefaultMutableTreeNode) node).getUserObject());
        }
        _routeModel.fireTableDataChanged();
        if (log.isDebugEnabled()) {
            log.debug("showRoute: Route has {} orders.", _orders.size());
        }
    }

    protected JPanel makeRouteTablePanel() {
        JTable routeTable = new JTable(_routeModel);
        routeTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        //routeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < _routeModel.getColumnCount(); i++) {
            int width = _routeModel.getPreferredWidth(i);
            routeTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        JScrollPane tablePane = new JScrollPane(routeTable);
        Dimension dim = routeTable.getPreferredSize();
        dim.height = routeTable.getRowHeight() * 11;
        tablePane.getViewport().setPreferredSize(dim);

        JPanel routePanel = new JPanel();
        routePanel.setLayout(new BoxLayout(routePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(Bundle.getMessage("RouteTableTitle"));
        routePanel.add(title, BorderLayout.NORTH);
        routePanel.add(tablePane);
        routePanel.add(Box.createVerticalGlue());
        return routePanel;
    }

    /**
     * Callback from RouteFinder - no routes found
     *
     * @param tree   routes
     * @param origin starting block
     * @param dest   ending block
     */
    protected void debugRoute(DefaultTreeModel tree, BlockOrder origin, BlockOrder dest) {
        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, Bundle.getMessage("NoRoute",
                new Object[]{origin.getBlock().getDisplayName(),
                    origin.getPathName(), origin.getExitName(), dest.getBlock().getDisplayName(),
                    dest.getEntryName(), dest.getPathName(), getDepth()}),
                Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE)) {
            return;
        }
        if (_debugFrame != null) {
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
        _orders = new ArrayList<>();
        clearFrames();
        clearFields();
        _focusedField = _origin;
        _routeModel.fireTableDataChanged();
    }

    private void clearFrames() {

        if (_debugFrame != null) {
            _debugFrame.dispose();
            _debugFrame = null;
        }
        if (_pickRouteDialog != null) {
            _pickRouteDialog.dispose();
            _pickRouteDialog = null;
        }
        closeProfileTable();

        if (_pickListFrame != null) {
            _pickListFrame.dispose();
            _pickListFrame = null;
        }
    }

    private void clearFields() {
        _origin.clearFields();
        _destination.clearFields();
        _via.clearFields();
        _avoid.clearFields();
    }

    protected String routeIsValid() {
        if (_orders == null || _orders.isEmpty()) {
            return Bundle.getMessage("noBlockOrders");
        }
        if (_orders.size() < 2) {
            return Bundle.getMessage("NoRouteSet", _origin.getBlockName(), _destination.getBlockName());
        }
        BlockOrder blockOrder = _orders.get(0);
        String msg = pathIsValid(blockOrder.getBlock(), blockOrder.getPathName());
        if (msg == null) {
            for (int i = 1; i < _orders.size(); i++) {
                BlockOrder nextBlockOrder = _orders.get(i);
                msg = pathIsValid(nextBlockOrder.getBlock(), nextBlockOrder.getPathName());
                if (msg != null) {
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

    static protected String pathIsValid(OBlock block, String pathName) {
        if (block == null) {
            return Bundle.getMessage("PathInvalid", pathName, "null");
        }
        List<Path> list = block.getPaths();
        if (list.isEmpty()) {
            return Bundle.getMessage("WarningTitle");
        }
        if (pathName != null) {
            for (int i = 0; i < list.size(); i++) {
                OPath path = (OPath) list.get(i);
                //if (log.isDebugEnabled()) log.debug("pathIsValid: pathName= "+pathName+", i= "+i+", path is "+path.getName());  
                if (pathName.equals(path.getName())) {
                    if (path.getFromPortal() == null && path.getToPortal() == null) {
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

    /* ************************ Route Table ******************************/
    class RouteTableModel extends AbstractTableModel {

        static final int BLOCK_COLUMN = 0;
        static final int ENTER_PORTAL_COL = 1;
        static final int PATH_COLUMN = 2;
        static final int DEST_PORTAL_COL = 3;
        static final int NUMCOLS = 4;

        RouteTableModel() {
            super();
        }

        @Override
        public int getColumnCount() {
            return NUMCOLS;
        }

        @Override
        public int getRowCount() {
            if (_orders==null) {
                return 0;
            }
            return _orders.size();
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case BLOCK_COLUMN:
                    return Bundle.getMessage("BlockCol");
                case ENTER_PORTAL_COL:
                    return Bundle.getMessage("EnterPortalCol");
                case PATH_COLUMN:
                    return Bundle.getMessage("PathCol");
                case DEST_PORTAL_COL:
                    return Bundle.getMessage("DestPortalCol");
                default:
                    // fall through
                    break;
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

        @Override
        public Object getValueAt(int row, int col) {
            // some error checking
            if (_orders==null || row >= _orders.size()) {
                return "";
            }
            BlockOrder bo = _orders.get(row);
            // some error checking
            if (bo == null) {
                log.error("BlockOrder is null");
                return "";
            }
            switch (col) {
                case BLOCK_COLUMN:
                    OBlock b = bo.getBlock();
                    if (b == null) {
                        return "null";
                    }
                    return bo.getBlock().getDisplayName();
                case ENTER_PORTAL_COL:
                    return bo.getEntryName();
                case PATH_COLUMN:
                    return bo.getPathName();
                case DEST_PORTAL_COL:
                    if (row == _orders.size() - 1) {
                        return "";
                    }
                    return bo.getExitName();
                default:
                    // fall through
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (_orders==null) {
                return;
            }
            BlockOrder bo = _orders.get(row);
            switch (col) {
                case BLOCK_COLUMN:
                    OBlock block = InstanceManager.getDefault(OBlockManager.class).getOBlock((String) value);
                    if (block != null) {
                        bo.setBlock(block);
                    }
                    break;
                case ENTER_PORTAL_COL:
                    bo.setEntryName((String) value);
                    break;
                case PATH_COLUMN:
                    bo.setPathName((String) value);
                    break;
                case DEST_PORTAL_COL:
                    bo.setExitName((String) value);
                    break;
                default:
                // do nothing
            }
            fireTableRowsUpdated(row, row);
        }
    }

    /**
     * Puts label message to the Left
     *
     * @param vertical Label orientation true = above, false = left
     * @param comp     Component to put into JPanel
     * @param text    Bundle keyword for label message
     * @param tooltip  Bundle keyword for tooltip message
     * @return Panel containing Component
     */
    static protected JPanel makeTextBoxPanel(boolean vertical, JComponent comp, String text, String tooltip) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage(text));
        if (vertical) {
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            comp.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
        } else {
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            comp.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        }
        panel.add(label);
        if (!vertical) {
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        }
        panel.add(comp);
        if (vertical) {
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
        } else {
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        }
        if (comp instanceof JTextField || comp instanceof JComboBox) {
            comp.setBackground(Color.white);
        }
        if (tooltip != null) {
            String tipText = Bundle.getMessage(tooltip);
            panel.setToolTipText(tipText);
            comp.setToolTipText(tipText);
            label.setToolTipText(tipText);
        }
        panel.setMaximumSize(new Dimension(350, comp.getPreferredSize().height));
        panel.setMinimumSize(new Dimension(80, comp.getPreferredSize().height));
        return panel;
    }

    /**
     * Make a horizontal panel for the input of data
     * Puts label message to the Left, 2nd component (button) to the right
     *
     * @param comp     Component for input of data 
     * @param button   2nd Component for panel, usually a button
     * @param label    Bundle keyword for label message
     * @param tooltip  Bundle keyword for tooltip message
     * @return Panel containing Components
     */
    static protected JPanel makeTextAndButtonPanel(JComponent comp, JComponent button, JLabel label, String tooltip) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(label);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(Box.createHorizontalGlue());

        comp.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        panel.add(comp);
        if (comp instanceof JTextField || comp instanceof JComboBox) {
            comp.setBackground(Color.white);
        }
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        button.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        panel.add(button);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        
        if (tooltip != null) {
            String tipText = Bundle.getMessage(tooltip);
            panel.setToolTipText(tipText);
            comp.setToolTipText(tipText);
            button.setToolTipText(tipText);
            label.setToolTipText(tipText);
        }
        panel.setMaximumSize(new Dimension(350, comp.getPreferredSize().height));
        panel.setMinimumSize(new Dimension(50, comp.getPreferredSize().height));
        return panel;        
    }
    /**
     * Puts label message to the Right
     *
     * @param comp    Component to put into JPanel
     * @param label   Bundle keyword for label message
     * @param tooltip Bundle keyword for tooltip message
     * @return Panel containing Component
     */
    static protected JPanel makeTextBoxPanel(JComponent comp, String label, String tooltip) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(300, comp.getPreferredSize().height));
        comp.setMinimumSize(new Dimension(30, comp.getPreferredSize().height));
        panel.add(comp);
        if (comp instanceof JTextField || comp instanceof JComboBox) {
            comp.setBackground(Color.white);
            JLabel l = new JLabel(Bundle.getMessage(label));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            l.setToolTipText(Bundle.getMessage(tooltip));
            panel.add(l);
        } else if (comp instanceof AbstractButton) {
            ((AbstractButton) comp).setText(Bundle.getMessage(label));
        }
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        if (tooltip != null) {
            String tipText = Bundle.getMessage(tooltip);
            panel.setToolTipText(tipText);
            comp.setToolTipText(tipText);
        }
        panel.setMaximumSize(new Dimension(350, comp.getPreferredSize().height));
        panel.setMinimumSize(new Dimension(80, comp.getPreferredSize().height));
        return panel;
    }

    private final static Logger log = LoggerFactory.getLogger(WarrantRoute.class);
}
