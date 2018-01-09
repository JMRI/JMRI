package jmri.jmrit.logix;

//import java.util.ArrayList;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.implementation.AbstractInstanceInitializer;
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
 * This singleton class displays a table of the occupancy detection trackers
 *
 * @author Peter Cressman
 *
 */
public class TrackerTableAction extends AbstractAction {

    static int STRUT_SIZE = 10;

    private final ArrayList<Tracker> _trackerList = new ArrayList<Tracker>();
    private TableFrame _frame;

    private TrackerTableAction(String menuOption) {
        super(menuOption);
    }

    /**
     *
     * @return the managed instance
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static TrackerTableAction getInstance() {
        return InstanceManager.getDefault(TrackerTableAction.class);
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

    public Tracker markNewTracker(OBlock block, String name) {
        if (_frame == null) {
            _frame = new TableFrame();
        }
        return _frame.addTracker(block, name);
    }

    public void stopTracker(Tracker t) {
        if (_frame == null) {
            _frame = new TableFrame();
        }
        _frame.stopTrain(t);
    }

    public void stopTrackerIn(OBlock block) {
        Iterator<Tracker> iter = _trackerList.iterator();
        while (iter.hasNext()) {
            Tracker t = iter.next();
            if (t.getBlocksOccupied().contains(block)) {
                if (_frame == null) {
                    _frame = new TableFrame();
                }
                _frame.stopTrain(t);
                return;
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
    private class TableFrame extends JmriJFrame implements PropertyChangeListener, MouseListener {

        private TrackerTableModel _model;
        private JmriJFrame _pickFrame;
        JDialog _dialog;
        JTextField _trainNameBox = new JTextField(30);
        JTextField _trainLocationBox = new JTextField(30);
        JTextField _status = new JTextField(80);
        ArrayList<String> _statusHistory = new ArrayList<String>();
        public int _maxHistorySize = 20;
        HashMap<OBlock, List<Tracker>> _blocks = new HashMap<OBlock, List<Tracker>>();

        TableFrame() {
            setTitle(Bundle.getMessage("TrackerTable"));
            _model = new TrackerTableModel(this);
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

            p = new JPanel();
            JButton button = new JButton(Bundle.getMessage("MenuNewTracker"));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    newTrackerDialog();
                }
            });
            tablePanel.add(p, BorderLayout.CENTER);
            p.add(button);

            button = new JButton(Bundle.getMessage("MenuRefresh"));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    _model.fireTableDataChanged();
                }
            });
            tablePanel.add(p, BorderLayout.CENTER);
            p.add(button);

            button = new JButton(Bundle.getMessage("MenuBlockPicker"));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    openPickList();
                }
            });
            tablePanel.add(p, BorderLayout.CENTER);
            p.add(button);

            panel.add(p);
            tablePanel.add(panel, BorderLayout.CENTER);

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dispose();
                }
            });
            setContentPane(tablePanel);

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                    _model.fireTableDataChanged();
                }
            });
            setLocation(0, 100);
            setVisible(true);
            pack();
        }

        protected boolean mouseClickedOnBlock(OBlock block) {
            if (_dialog != null) {
                _trainLocationBox.setText(block.getDisplayName());
                if (block.getValue() != null) {
                    _trainNameBox.setText((String) block.getValue());
                }
                return true;
            }
            if ((block.getState() & OBlock.OCCUPIED) != 0 && block.getValue() != null) {
                markNewTracker(block, (String) block.getValue());
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
                OBlock block = InstanceManager.getDefault(OBlockManager.class).getOBlock(blockName.trim());
                if (block == null) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("BlockNotFound", blockName),
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                } else {
                    retOK = (addTracker(block, _trainNameBox.getText()) != null);
                }
            }
            return retOK;
        }

        public Tracker addTracker(OBlock block, String name) {
            if ((block.getState() & OBlock.OCCUPIED) == 0) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("blockUnoccupied", block.getDisplayName()),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            } else if (name == null || name.length() == 0) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("noName"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            } else if (nameInuse(name)) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("duplicateName", name),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            } else {
                String oldName = blockInUse(block);
                if (oldName != null && !name.equals(block.getValue())) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("blockInUse", oldName, block.getDisplayName()),
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    return null;
                }
                Tracker newTracker = new Tracker(block, name);
                newTracker.setupCheck();
                _trackerList.add(newTracker);
                addBlockListeners(newTracker);
                _model.fireTableDataChanged();
                setStatus(Bundle.getMessage("startTracker", name, block.getDisplayName()));
                return newTracker;
            }
        }

        protected String blockInUse(OBlock b) {
            Iterator<Tracker> iter = _trackerList.iterator();
            while (iter.hasNext()) {
                Tracker t = iter.next();
                if (t.getBlocksOccupied().contains(b)) {
                    return t.getTrainName();
                }
            }
            return null;
        }

        boolean nameInuse(String name) {
            Iterator<Tracker> iter = _trackerList.iterator();
            while (iter.hasNext()) {
                Tracker t = iter.next();
                if (name.equals(t.getTrainName())) {
                    return true;
                }
            }
            return false;
        }

        void openPickList() {
            _pickFrame = new JmriJFrame();
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            JPanel blurb = new JPanel();
            blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
            blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
//         blurb.add(new JLabel(Bundle.getMessage("DragOccupancyName")));
//         blurb.add(new JLabel(Bundle.getMessage("DragErrorName")));
            blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
            JPanel panel = new JPanel();
            panel.add(blurb);
            content.add(panel);
            PickListModel[] models = {PickListModel.oBlockPickModelInstance()};
            content.add(new PickPanel(models));

            _pickFrame.setContentPane(content);
            /*         _pickFrame.addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent e) {
             closePickList();
             }
             });*/
            _pickFrame.setLocationRelativeTo(this);
            _pickFrame.toFront();
            _pickFrame.setVisible(true);
            _pickFrame.pack();
        }

        /**
         * Adds listeners to all blocks in the range of a Tracker. Called when a
         * new tracker is created.
         * <p>
         */
        private void addBlockListeners(Tracker tracker) {
            List<OBlock> range = tracker.getRange();
            if (log.isDebugEnabled()) {
                log.debug("addBlockListeners for tracker= \"" + tracker.getTrainName()
                        + "\" has range of " + range.size() + " blocks.");
            }
            Iterator<OBlock> iter = range.iterator();
            while (iter.hasNext()) {
                addBlockListener(iter.next(), tracker);
            }
        }

        /**
         * Adds listener to a block when a tracker enters.
         */
        private void addBlockListener(OBlock block, Tracker tracker) {
            List<Tracker> trackers = _blocks.get(block);
            if (trackers == null) {
                trackers = new ArrayList<Tracker>();
                trackers.add(tracker);
                _blocks.put(block, trackers);
                block.addPropertyChangeListener(this);
                if (log.isDebugEnabled()) {
                    log.debug("\taddPropertyChangeListener for block {}", block.getDisplayName());
                }
            } else {
                if (trackers.isEmpty()) {
                    block.addPropertyChangeListener(this);
                    if (log.isDebugEnabled()) {
                        log.debug("\taddPropertyChangeListener for block {}", block.getDisplayName());
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("\tassumed block {} already has listener" + block.getDisplayName());
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
            Iterator<OBlock> iter = newRange.iterator();
            while (iter.hasNext()) {
                OBlock b = iter.next();
                if (oldRange.contains(b)) {
                    oldRange.remove(b);
                    continue; // held in common. keep listener
                }
                addBlockListener(b, tracker);       // new block.  Add Listener
            }
            // blocks left in oldRange were not found in newRange.  Remove Listeners
            iter = oldRange.iterator();
            while (iter.hasNext()) {
                removeBlockListener(iter.next(), tracker);
            }

        }

        private void removeBlockListeners(List<OBlock> range, Tracker tracker) {
            if (log.isDebugEnabled()) {
                log.debug("removeBlockListeners for tracker= \"" + tracker.getTrainName()
                        + "\" has " + range.size() + " blocks to remove.");
            }
            Iterator<OBlock> iter = range.iterator();
            while (iter.hasNext()) {
                removeBlockListener(iter.next(), tracker);
            }
        }

        private void removeBlockListener(OBlock b, Tracker tracker) {
            List<Tracker> trackers = _blocks.get(b);
            if (trackers != null) {
                trackers.remove(tracker);
                if (trackers.size() == 0) {
                    b.removePropertyChangeListener(this);
                    if (log.isDebugEnabled()) {
                        log.debug("removeBlockListener on block " + b.getDisplayName()
                                + " for tracker= " + tracker.getTrainName());
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.error("Block \"" + b.getDisplayName() + "\" has no listeners.  Tracker for train "
                            + tracker.getTrainName() + " expected a listener");
                }
            }
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("state")) {
                OBlock b = (OBlock) evt.getSource();
                int state = ((Number) evt.getNewValue()).intValue();
                int oldState = ((Number) evt.getOldValue()).intValue();
                if (log.isDebugEnabled()) {
                    log.debug("propertyChange to block= " + b.getDisplayName() + " state= " + state + " oldstate= " + oldState);
                }
                // The "jiggle" (see tracker.showBlockValue() causes some state changes to be duplicated.
                // The following washes out the extra notifications
/*                if ((state & (OBlock.UNOCCUPIED | OBlock.RUNNING)) == (OBlock.UNOCCUPIED | OBlock.RUNNING)) {
                    b.setState(state & ~OBlock.RUNNING);
                    return;  // will do the tracker.move() on the next (repeat call
                } else if ((state & OBlock.RUNNING) != 0) {
                    return;  // repeats previous call that was completed.
                }*/
                if ((state & (OBlock.UNOCCUPIED | OBlock.RUNNING)) == (oldState & (OBlock.UNOCCUPIED | OBlock.RUNNING))
                        && (state & (OBlock.OCCUPIED | OBlock.RUNNING)) == (oldState & (OBlock.OCCUPIED | OBlock.RUNNING))) {
                    return;
                }
                List<Tracker> trackers = _blocks.get(b);
                if (trackers == null) {
                    log.error("No Trackers found for block " + b.getDisplayName() + " going to state= " + state);
                    b.removePropertyChangeListener(this);
                } else // perhaps several trackers listen for this block
                if ((state & OBlock.OCCUPIED) != 0) {
                    // going occupied
                    if (b.getValue() == null) {
                        String[] trains = new String[trackers.size()];
                        int i = 0;
                        Warrant w = b.getWarrant();
                        if (w != null) {
                            int idx = w.getCurrentOrderIndex();
                            // Was it a warranted train that entered the block,
                            // is distance of 1 block OK?
                            // Can't tell who got notified first - tracker or warrant?
                            if (w.getIndexOfBlock(b, idx) - idx < 2) {
                                return;
                            }
                        }
                        Iterator<Tracker> iter = trackers.iterator();
                        while (iter.hasNext()) {
                            Tracker t = iter.next();
                            trains[i++] = t.getTrainName();
                        }
                        Tracker t = trackers.get(0);
                        if (i > 1) {
                            Object selection = JOptionPane.showInputDialog(this, Bundle.getMessage("MultipleTrackers",
                                    b.getDisplayName()), Bundle.getMessage("WarningTitle"),
                                    JOptionPane.INFORMATION_MESSAGE, null, trains, null);
                            if (selection != null) {
                                iter = _trackerList.iterator();
                                while (iter.hasNext()) {
                                    t = iter.next();
                                    if (((String) selection).equals(t.getTrainName())) {
                                        break;
                                    }
                                }
                            } else {
                                return;
                            }
                        }
                        processTrackerStateChange(t, b, state);
                    } else {
                        log.warn("Block " + b.getDisplayName() + " going active with value= "
                                + b.getValue() + " Wasup wi dat?");
                    }
                } else if ((state & OBlock.UNOCCUPIED) != 0) {
                    // b going unoccupied.
                    // to avoid ConcurrentModificationException if a tracker is deleted, use a copy
                    Tracker[] copy = new Tracker[trackers.size()];
                    Iterator<Tracker> iter = trackers.iterator();
                    int i = 0;
                    while (iter.hasNext()) {
                        copy[i++] = iter.next();
                    }
                    for (int k = 0; k < i; k++) {
                        processTrackerStateChange(copy[k], b, state);
                    }
                }
                /*               if ((state & OBlock.UNOCCUPIED) != 0) {
                    b.setValue(null);
                }*/
            }
            _model.fireTableDataChanged();
        }

        /**
         * Called when a state change has occurred for one the blocks listened
         * to for this tracker. Tracker.move makes the changes to OBlocks to
         * indicate the new occupancy positions of the train. Upon return,
         * update the listeners for the trains next move
         * <p>
         */
        private void processTrackerStateChange(Tracker tracker, OBlock block, int state) {
            List<OBlock> oldRange = tracker.getRange();// range in effect when state change was detected
            switch (tracker.move(block, state)) {
                case Tracker.NO_BLOCK:
                    adjustBlockListeners(oldRange, tracker.getRange(), tracker);
                    String msg = Bundle.getMessage("TrackerNoCurrentBlock", tracker.getTrainName(),
                            block.getDisplayName()) + "\n" + Bundle.getMessage("TrackingStopped");
                    JOptionPane.showMessageDialog(this, msg,
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    stopTrain(tracker);
                    setStatus(msg);
                    break;
                case Tracker.ENTER_BLOCK:
                    block._entryTime = System.currentTimeMillis();
                    adjustBlockListeners(oldRange, tracker.getRange(), tracker);
                    setStatus(Bundle.getMessage("TrackerBlockEnter", tracker.getTrainName(),
                            block.getDisplayName()));
                    break;
                case Tracker.LEAVE_BLOCK:
                    adjustBlockListeners(oldRange, tracker.getRange(), tracker);
                    long et = (System.currentTimeMillis() - block._entryTime) / 1000;
                    setStatus(Bundle.getMessage("TrackerBlockLeave", tracker.getTrainName(),
                            block.getDisplayName(), et / 60, et % 60));
                    break;
                case Tracker.ERROR_BLOCK:
                    // tracker wrote error message
                    break;
                default:
                    log.warn("Unhandled tracker move: {}", tracker.move(block, state));
                    break;
            }
        }

        protected void stopTrain(Tracker t) {
            List<OBlock> list = t.getRange();
            removeBlockListeners(list, t);
            Iterator<OBlock> iter = list.iterator();
            while (iter.hasNext()) {
                OBlock b = iter.next();
                t.removeBlock(b);
            }
            list = t.getBlocksOccupied();
//   removeBlockListeners(list, t);
            iter = list.iterator();
            while (iter.hasNext()) {
                OBlock b = iter.next();
                long et = (System.currentTimeMillis() - b._entryTime) / 1000;
                setStatus(Bundle.getMessage("TrackerBlockEnd", t.getTrainName(),
                        b.getDisplayName(), et / 60, et % 60));
            }
            _trackerList.remove(t);
            setStatus(Bundle.getMessage("TrackerStopped", t.getTrainName()));
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

        TableFrame _parent;

        public TrackerTableModel(TableFrame f) {
            super();
            _parent = f;
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
                _parent.stopTrain(t);
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
            Set set = super.getInitalizes();
            set.add(TrackerTableAction.class);
            return set;
        }
    }
}
