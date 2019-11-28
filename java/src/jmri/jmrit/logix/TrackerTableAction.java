package jmri.jmrit.logix;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickPanel;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class displays a table of the occupancy detection trackers. It does
 * the listening of block sensors for all the Trackers and chooses the tracker most
 * likely to have entered a block becoming active or leaving a block when it 
 * becomes inactive.
 *
 * @author Peter Cressman
 *
 */
public class TrackerTableAction extends AbstractAction implements PropertyChangeListener{

    static int STRUT_SIZE = 10;

    private ArrayList<Tracker> _trackerList = new ArrayList<Tracker>();
    private HashMap<OBlock, ArrayList<Tracker>> _trackerBlocks = new HashMap<>();
    protected TableFrame _frame;
    private ChooseTracker _trackerChooser;
    private boolean _requirePaths;

    private TrackerTableAction(String menuOption) {
        super(menuOption);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_frame != null) {
            _frame.setVisible(true);
        } else {
            _frame = new TableFrame();
        }
    }

    synchronized public boolean mouseClickedOnBlock(OBlock block) {
        if (_frame != null) {
            return _frame.mouseClickedOnBlock(block);
        }
        return false;
    }

    /**
     * Create and register a new Tracker.
     * @param block starting head block of the Tracker
     * @param name name of the Tracker
     * @param marker LocoIcon dropped on the block (optional)
     * @return true if successfully created.
     */
    public boolean markNewTracker(OBlock block, String name, LocoIcon marker) {
        if (_frame == null) {
            _frame = new TableFrame();
        }
        if (name == null && marker != null) {
            name = marker.getUnRotatedText();
        }
        return makeTracker(block, name, marker);
    }

    private boolean makeTracker(OBlock block, String name, LocoIcon marker) {
        String msg = null;
        
        if ((block.getState() & OBlock.OCCUPIED) == 0) {
            msg = Bundle.getMessage("blockUnoccupied", block.getDisplayName());
        } else if (name == null || name.length() == 0) {
            msg = Bundle.getMessage("noTrainName");
        } else if (nameInuse(name)) {
            msg = Bundle.getMessage("duplicateName", name);
        } else {
            Tracker t = findTrackerIn(block);
            if (t != null && !name.equals(block.getValue())) {
                msg = Bundle.getMessage("blockInUse", t.getTrainName(), block.getDisplayName());
            }
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(_frame, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        block.setValue(name);
        new Tracker(block, name, marker, this);
        return true;
    }

    /**
     * Deprecated - use markNewTracker instead.
     * @param block starting head block of the Tracker
     * @param name name of the Tracker
     * @return the new Tracker
     */
    @Deprecated
    public Tracker addTracker(OBlock block, String name) {
        markNewTracker(block, name, null);
        return findTrackerIn(block);
    }

    protected void addTracker(Tracker t) {
        _trackerList.add(t);
        addBlockListeners(t);
        if (_frame == null) {
            _frame = new TableFrame();
        }
        _frame._model.fireTableDataChanged();
        _frame.setStatus(Bundle.getMessage("startTracker",
               t.getTrainName(), t.getHeadBlock().getDisplayName()));
    }

    protected boolean checkBlock(OBlock b) {
        if (findTrackerIn(b) == null && b.getWarrant() == null) {
            b.setValue(null);
            return true;
        }
        return false;
    }

    boolean nameInuse(String name) {
        for (Tracker t : _trackerList) {
            if (name.equals(t.getTrainName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Stop a Tracker from tracking and remove from list
     * @param t Tracker to be stopped
     * @param b Block Tracker of its last move. Optional, for display purpose only.
     */
   public void stopTracker(Tracker t, OBlock b) {
        if (_frame == null) {
            _frame = new TableFrame();
        }
        stopTrain(t, b);
    }

   protected void setStatus(String msg) {
       _frame.setStatus(msg);
   }
    /**
     * See if any Trackers are occupying block
     * @param b Block being queried
     * @return Tracker if found
     */
    public Tracker findTrackerIn(OBlock b) {
        for (Tracker t : _trackerList) {
            if (t.getBlocksOccupied().contains(b)) {
                return t;
            }
        }
        return null;
    }

    public void updateStatus() {
        _frame._model.fireTableDataChanged();

    }
    /**
     * Adds listeners to all blocks in the range of a Tracker. Called when a
     * new tracker is created.
     * @param tracker Tracker to start
     */
    protected void addBlockListeners(Tracker tracker) {
        List<OBlock> range = tracker.makeRange();
        Iterator<OBlock> iter = range.iterator();
        while (iter.hasNext()) {
            addBlockListener(iter.next(), tracker);
        }
    }

    /**
     * Adds listener to a block when a tracker enters.
     */
    private void addBlockListener(OBlock block, Tracker tracker) {
        ArrayList<Tracker> trackers = _trackerBlocks.get(block);
        if (trackers == null) {
            trackers = new ArrayList<Tracker>();
            trackers.add(tracker);
            if ((block.getState() & OBlock.UNDETECTED) == 0) {
                _trackerBlocks.put(block, trackers);
                block.addPropertyChangeListener(this);
            }
        } else {
            if (trackers.isEmpty()) {
                if ((block.getState() & OBlock.UNDETECTED) == 0) {
                    block.addPropertyChangeListener(this);
                }
            }
            if (!trackers.contains(tracker)) {
                trackers.add(tracker);
            }
        }
    }

    /**
     * Do Venn Diagram between the two sets. Keep listeners held in common.
     * Add new listeners. Remove old.
     */
    private void adjustBlockListeners(List<OBlock> oldRange, List<OBlock> newRange, Tracker tracker) {
         for (OBlock b : newRange) {
            if (oldRange.contains(b)) {
                oldRange.remove(b);
                continue; // held in common. keep listener
            }
            addBlockListener(b, tracker);       // new block.  Add Listener
        }
        // blocks left in oldRange that were not found in newRange.  Remove Listeners
        for (OBlock b :oldRange) {
            removeBlockListener(b, tracker);
        }

    }

    protected void removeBlockListeners(Tracker tracker) {
        for (OBlock block : _trackerBlocks.keySet()) {
            removeBlockListener(block, tracker);
        }
    }

    private void removeBlockListener(OBlock block, Tracker tracker) {
        List<Tracker> trackers = _trackerBlocks.get(block);
        if (trackers != null) {
            trackers.remove(tracker);
            if (trackers.isEmpty()) {
                block.removePropertyChangeListener(this);
            }
        }
    }

    @Override
    synchronized public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            OBlock block = (OBlock) evt.getSource();
            int state = ((Number) evt.getNewValue()).intValue();
            int oldState = ((Number) evt.getOldValue()).intValue();
            // The "jiggle" (see tracker.showBlockValue() causes some state changes to be duplicated.
            // The following washes out the extra notifications
            if ((state & OBlock.UNOCCUPIED) == (oldState & OBlock.UNOCCUPIED)
                    && (state & OBlock.OCCUPIED) == (oldState & OBlock.OCCUPIED)) {
                return;
            }
            ArrayList<Tracker> trackerListeners = _trackerBlocks.get(block);
            if (trackerListeners == null || trackerListeners.isEmpty()) {
                log.error("No Trackers found for block \"{}\" going to state= {}", 
                        block.getDisplayName(), state);
                block.removePropertyChangeListener(this);
                return;
            }
            if ((state & OBlock.OCCUPIED) != 0) {   // going occupied
                List<Tracker> trackers = getAvailableTrackers(block);
                if (trackers.isEmpty()) {
                    return;
                }
                if (trackers.size() > 1) { // if several trackers listen for this block, user must identify which one.
                    if (_trackerChooser != null) {
                        _trackerChooser.dispose();
                    }
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    _trackerChooser = new ChooseTracker(block, trackers, state);
                    return;
                }
               
                Tracker tracker = trackers.get(0);
                if (block.getValue() != null &&  !block.getValue().equals(tracker.getTrainName())) {
                    log.error("Block \"{} \" going active with value= {} for Tracker {}! Who/What is \"{}\"?",
                            block.getDisplayName(), block.getValue(), tracker.getTrainName(), block.getValue());
                    return;
               } else {
                   if (!_requirePaths) {
                       try {
                           tracker.hasPathInto(block);
                       } catch (JmriException je) {
                           log.error("{} {}", tracker.getTrainName(), je.getMessage());
                           return;
                       }
                   }
                   processTrackerStateChange(tracker, block, state);
               }
            } else if ((state & OBlock.UNOCCUPIED) != 0) {
                if (_trackerChooser != null) {
                    _trackerChooser.checkClose(block);
                }
                for (Tracker t : trackerListeners) {
                    if (t.getBlocksOccupied().contains(block)) {
                        processTrackerStateChange(t, block, state);
                        break;
                    }
                }
            }
        }
        _frame._model.fireTableDataChanged();
    }

    private List<Tracker> getAvailableTrackers(OBlock block) {
        List<Tracker> trackers = new ArrayList<>();
        ArrayList<Tracker> trackerListeners = _trackerBlocks.get(block);
        if (_requirePaths) {
            ArrayList<Tracker> partials = new ArrayList<>();
            // filter for trackers with paths set into block
            for (Tracker t : trackerListeners) {
                try {
                    switch (t.hasPathInto(block)) {
                        case SET:
                            trackers.add(t);
                            break;
                        case PARTIAL:
                            partials.add(t);
                            break;
                        default:
                            break;
                    }
                } catch (JmriException je) {
                    log.error("{} {}", t.getTrainName(), je.getMessage());
                }
            }
            if (trackers.isEmpty()) {   // nobody has paths set.
                // even so, likely to be possible for somebody to get there
                if (!partials.isEmpty()) {
                    trackers = partials; // OK, maybe not all switches are lined up
                } else {
                    trackers = trackerListeners; // maybe even this bad. 
                }
            }
        } else {
            trackers = trackerListeners;
        }
        return trackers;
    }
    /**
     * Called when a state change has occurred for one the blocks listened
     * to for this tracker. Tracker.move makes the changes to OBlocks to
     * indicate the new occupancy positions of the train. Upon return,
     * update the listeners for the trains next move
     * <p>
     */
    private void processTrackerStateChange(Tracker tracker, OBlock block, int state) {
        List<OBlock> oldRange = tracker.makeRange();// total range in effect when state change was detected
        if (tracker.move(block, state)) {   // new total range has been made after move was done.
            block._entryTime = System.currentTimeMillis();
            if (tracker._statusMessage != null) {
                _frame.setStatus(tracker._statusMessage);
            } else {
                adjustBlockListeners(oldRange, tracker.makeRange(), tracker);
                _frame.setStatus(Bundle.getMessage("TrackerBlockEnter",
                        tracker.getTrainName(), block.getDisplayName()));
            }
        } else {
            if (tracker._statusMessage != null) {
                _frame.setStatus(tracker._statusMessage);
            } else if (_trackerList.contains(tracker)) {
                adjustBlockListeners(oldRange, tracker.makeRange(), tracker);
                long et = (System.currentTimeMillis() - block._entryTime) / 1000;
                _frame.setStatus(Bundle.getMessage("TrackerBlockLeave", tracker.getTrainName(),
                        block.getDisplayName(), et / 60, et % 60));
            }
        }
    }

    private void stopTrain(Tracker t, OBlock b) {
        t.stop();
        removeBlockListeners(t);
        _trackerList.remove(t);
        long et = (System.currentTimeMillis() - t._startTime) / 1000;
        String location;
        if (b!= null) {
            location = b.getDisplayName(); 
        }else {
            location = Bundle.getMessage("BeanStateUnknown");
        }
        _frame.setStatus(Bundle.getMessage("TrackerStopped", 
                t.getTrainName(), location, et / 60, et % 60));
        _frame._model.fireTableDataChanged();
    }

    class ChooseTracker extends JDialog implements ListSelectionListener {
        OBlock block;
        List<Tracker> trackers;
        int state;
        JList<Tracker> _jList;
        
        ChooseTracker(OBlock b, List<Tracker> ts, int s) {
            super(_frame);
            setTitle(Bundle.getMessage("TrackerTitle"));
            block = b;
            trackers = ts;
            state = s;
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            contentPanel.add(Box.createVerticalStrut(STRUT_SIZE));
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel(Bundle.getMessage("MultipleTrackers", block.getDisplayName())));
            panel.add(new JLabel(Bundle.getMessage("ChooseTracker", block.getDisplayName())));
            JPanel p = new JPanel();
            p.add(panel);
            contentPanel.add(p);
            panel = new JPanel();
            panel.setBorder(javax.swing.BorderFactory .createLineBorder(Color.black, 2));
            _jList = new JList<>();
            _jList.setModel(new TrackerListModel());
            _jList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            _jList.addListSelectionListener(this);
            panel.add(_jList);
            p = new JPanel();
            p.add(panel);
            contentPanel.add(p);
            
            contentPanel.add(Box.createVerticalStrut(STRUT_SIZE));
            panel = new JPanel();
            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener((ActionEvent a) -> {
                dispose();
            });
            panel.add(cancelButton);

            contentPanel.add(panel);
            setContentPane(contentPanel);
            pack();
            setLocation(_frame.getLocation());
            setAlwaysOnTop(true);
            setVisible(true);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            Tracker tr = _jList.getSelectedValue();
            if (tr != null) {
                processTrackerStateChange(tr, block, state);
                dispose();
            }
        }

        void checkClose(OBlock b) {
            if (block.equals(b)) {
                dispose();
            }
        }

        class TrackerListModel extends AbstractListModel<Tracker> {
            @Override
            public int getSize() {
                return trackers.size();
            }

            @Override
            public Tracker getElementAt(int index) {
                return trackers.get(index);
            }
        }
    }

    /**
     * Holds a table of Trackers that follow adjacent occupancy. Needs to be a
     * singleton to be opened and closed for trackers to report to it
     *
     * @author Peter Cressman
     *
     */
    class TableFrame extends JmriJFrame implements MouseListener {

        private TrackerTableModel _model;
        private JmriJFrame _pickFrame;
        JDialog _dialog;
        JTextField _trainNameBox = new JTextField(30);
        JTextField _trainLocationBox = new JTextField(30);
        JTextField _status = new JTextField(80);
        ArrayList<String> _statusHistory = new ArrayList<String>();
        public int _maxHistorySize = 20;

        TableFrame() {
            super(true, true);
            setTitle(Bundle.getMessage("TrackerTable"));
            _model = new TrackerTableModel();
            JTable table = new JTable(_model);
            TableRowSorter<TrackerTableModel> sorter = new TableRowSorter<>(_model);
            table.setRowSorter(sorter);
            table.getColumnModel().getColumn(TrackerTableModel.STOP_COL).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(TrackerTableModel.STOP_COL).setCellRenderer(new ButtonRenderer());
            for (int i = 0; i < _model.getColumnCount(); i++) {
                int width = _model.getPreferredWidth(i);
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            table.setDragEnabled(true);
            table.setTransferHandler(new jmri.util.DnDTableExportHandler());
            JScrollPane tablePane = new JScrollPane(table);
            Dimension dim = table.getPreferredSize();
            int height = new JButton("STOPIT").getPreferredSize().height;
            dim.height = height * 2;
            tablePane.getViewport().setPreferredSize(dim);

            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
            JLabel title = new JLabel(Bundle.getMessage("TrackerTable"));
            tablePanel.add(title, BorderLayout.NORTH);
            tablePanel.add(tablePane, BorderLayout.CENTER);

            JPanel panel = new JPanel();
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("lastEvent")));
            p.add(_status);
            _status.setEditable(false);
            _status.setBackground(Color.white);
            _status.addMouseListener(this);
            panel.add(p);

            tablePanel.add(makeButtonPanel(), BorderLayout.CENTER);
            tablePanel.add(panel, BorderLayout.CENTER);

            setContentPane(tablePanel);

            JMenuBar menuBar = new JMenuBar();
            JMenu optionMenu = new JMenu(Bundle.getMessage("MenuMoreOptions"));
            optionMenu.add(makePathRequirement());

            JMenuItem pickerMenu = new JMenuItem(Bundle.getMessage("MenuBlockPicker"));
            pickerMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    openPickList();
                }
            });
            optionMenu.add(pickerMenu);

            optionMenu.add(WarrantTableAction.makeLogMenu());
            menuBar.add(optionMenu);
            setJMenuBar(menuBar);
            addHelpMenu("package.jmri.jmrit.logix.Tracker", true);

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                    if (_pickFrame != null) {
                        _pickFrame.dispose();
                    }
                    _model.fireTableDataChanged();
                }
            });
            setLocation(0, 100);
            setVisible(true);
            pack();
        }

        private JPanel makeButtonPanel() {
            JPanel panel = new JPanel();
            JButton button = new JButton(Bundle.getMessage("MenuNewTracker"));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    newTrackerDialog();
                }
            });
            panel.add(button);

            button = new JButton(Bundle.getMessage("MenuRefresh"));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    _model.fireTableDataChanged();
                }
            });
            panel.add(button);

            return panel;
        }

        
        
        private JMenuItem makePathRequirement() {
            JMenu pathkMenu = new JMenu(Bundle.getMessage("MenuPathRanking"));
            ButtonGroup pathButtonGroup = new ButtonGroup();
            JRadioButtonMenuItem r;
            r = new JRadioButtonMenuItem(Bundle.getMessage("showAllTrackers"));
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _requirePaths = false;
                }
            });
            pathButtonGroup.add(r);
            if (_requirePaths) {
                r.setSelected(false);
            } else {
                r.setSelected(true);
            }
            pathkMenu.add(r);

            r = new JRadioButtonMenuItem(Bundle.getMessage("showMostLikely"));
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _requirePaths = true;
                }
            });
            pathButtonGroup.add(r);
            if (_requirePaths) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            pathkMenu.add(r);

            return pathkMenu;
        }

        protected boolean mouseClickedOnBlock(OBlock block) {
            if (_dialog != null) {
                if ((block.getState() & OBlock.OCCUPIED) != 0 && block.getValue() != null) {
                    markNewTracker(block, (String) block.getValue(), null);
                    return true;
                }
                _trainLocationBox.setText(block.getDisplayName());
                if (block.getValue() != null) {
                    _trainNameBox.setText((String) block.getValue());
                }
                return true;
            }
            return false;
        }

        private void newTrackerDialog() {
            _dialog = new JDialog(this, Bundle.getMessage("MenuNewTracker"), false);
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(10, 10));
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

            mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("createTracker")));
            mainPanel.add(p);

            mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
            mainPanel.add(makeTrackerNamePanel());
            mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
            mainPanel.add(makeDoneButtonPanel());
            panel.add(mainPanel);
            _dialog.getContentPane().add(panel);
            _dialog.setLocation(this.getLocation().x + 100, this.getLocation().y + 100);
            _dialog.pack();
            _dialog.setVisible(true);
        }

        private JPanel makeTrackerNamePanel() {
            _trainNameBox.setText("");
            _trainLocationBox.setText("");
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
            p.add(new JLabel(Bundle.getMessage("TrainName")), c);
            c.gridy = 1;
            p.add(new JLabel(Bundle.getMessage("TrainLocation")), c);
            c.gridx = 1;
            c.gridy = 0;
            c.anchor = java.awt.GridBagConstraints.WEST;
            c.weightx = 1.0;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
            p.add(_trainNameBox, c);
            c.gridy = 1;
            p.add(_trainLocationBox, c);
            namePanel.add(p);
            return namePanel;
        }

        private JPanel makeDoneButtonPanel() {
            JPanel buttonPanel = new JPanel();
            JPanel panel0 = new JPanel();
            panel0.setLayout(new FlowLayout());
            JButton doneButton;
            doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            doneButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (doDoneAction()) {
                        _dialog.dispose();
                        _dialog = null;
                    }
                }
            });
            panel0.add(doneButton);
            buttonPanel.add(panel0);
            return buttonPanel;
        }

        private boolean doDoneAction() {
            boolean retOK = false;
            String blockName = _trainLocationBox.getText();
            if (blockName != null) {
                OBlock block = InstanceManager.getDefault(OBlockManager.class).getOBlock(blockName);
                if (block == null) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("BlockNotFound", blockName),
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                } else {
                    retOK = makeTracker(block, _trainNameBox.getText(), null);
                }
            }
            return retOK;
        }

        void openPickList() {
            _pickFrame = new JmriJFrame();
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            JPanel blurb = new JPanel();
            blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
            blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
            blurb.add(new JLabel(Bundle.getMessage("DragBlockName")));
            blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
            JPanel panel = new JPanel();
            panel.add(blurb);
            content.add(panel);
            PickListModel[] models = {PickListModel.oBlockPickModelInstance()};
            content.add(new PickPanel(models));

            _pickFrame.setContentPane(content);
            _pickFrame.setLocationRelativeTo(this);
            _pickFrame.toFront();
            _pickFrame.setVisible(true);
            _pickFrame.pack();
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
            for (int i = _statusHistory.size() - 1; i >= 0; i--) {
                popup.add(_statusHistory.get(i));
            }
            popup.show(_status, 0, 0);
        }

        @Override
        public void mousePressed(MouseEvent event) {
        }

        @Override
        public void mouseEntered(MouseEvent event) {
        }

        @Override
        public void mouseExited(MouseEvent event) {
        }

        @Override
        public void mouseReleased(MouseEvent event) {
        }

        private void setStatus(String msg) {
            _status.setText(msg);
            if (msg != null && msg.length() > 0) {
                WarrantTableAction.writetoLog(msg);
                _statusHistory.add(msg);
                while (_statusHistory.size() > _maxHistorySize) {
                    _statusHistory.remove(0);
                }
            }
        }
    }

    private class TrackerTableModel extends AbstractTableModel {

        public static final int NAME_COL = 0;
        public static final int STATUS_COL = 1;
        public static final int STOP_COL = 2;
        public static final int NUMCOLS = 3;

        public TrackerTableModel() {
            super();
        }

        @Override
        public int getColumnCount() {
            return NUMCOLS;
        }

        @Override
        public int getRowCount() {
            return _trackerList.size();
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case NAME_COL:
                    return Bundle.getMessage("TrainName");
                case STATUS_COL:
                    return Bundle.getMessage("status");
                default:
                    // fall out
                    break;
            }
            return "";
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case NAME_COL:
                    return _trackerList.get(rowIndex).getTrainName();
                case STATUS_COL:
                    return _trackerList.get(rowIndex).getStatus();
                case STOP_COL:
                    return Bundle.getMessage("Stop");
                default:
                    // fall out
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == STOP_COL) {
                Tracker t = _trackerList.get(row);
                stopTrain(t, t.getHeadBlock());
                fireTableDataChanged();
                return;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == STOP_COL) {
                return true;
            }
            return false;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == STOP_COL) {
                return JButton.class;
            }
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case NAME_COL:
                    return new JTextField(20).getPreferredSize().width;
                case STATUS_COL:
                    return new JTextField(60).getPreferredSize().width;
                case STOP_COL:
                    return new JButton("STOPIT").getPreferredSize().width;
                default:
                    // fall out
                    break;
            }
            return 5;
        }

    }

    private final static Logger log = LoggerFactory.getLogger(TrackerTableAction.class);

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(TrackerTableAction.class)) {
                return new TrackerTableAction(Bundle.getMessage("MenuTrackers"));
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(TrackerTableAction.class);
            return set;
        }
    }
}
