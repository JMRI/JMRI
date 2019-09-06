package jmri.jmrit.logix;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.JmriException;
import jmri.InstanceManager;
import jmri.jmrit.display.LocoIcon;
//import jmri.jmrit.logix.TrackerTableAction.ChooseTracker.TrackerListModel;
import jmri.util.SoundTone;

/**
 * Track an occupied block to adjacent blocks becoming occupied.
 *
 * @author Pete Cressman Copyright (C) 2013
 */
public class Tracker {

    TrackerTableAction _parent;
    private String _trainName;
    private ArrayList<OBlock> _headRange; // blocks reachable from head block
    private ArrayList<OBlock> _tailRange; // blocks reachable from tail block
    private ArrayList<OBlock> _lostRange = new ArrayList<OBlock>(); // blocks that lost detection
    private LinkedList<OBlock> _occupies = new LinkedList<>();     // blocks occupied by train
    private ArrayList<OBlock> _range;    // total range of train  
    long _startTime;
    String _statusMessage;
    private Color _markerForeground;
    private Color _markerBackground;
    private Font _markerFont;
    private OBlock _darkBlock = null;
    enum PathSet {NO, PARTIAL, SET};

    /**
     * Must call setupCheck() after creating Tracker to check environment of
     * train.
     *
     * @param block the starting block to track
     * @param name  the name of the train being tracked
     * @param marker icon if LocoIcon was dropped on a block
     * @param tta TrackerTableAction that manages Trackers
     */
    Tracker(OBlock block, String name, LocoIcon marker, TrackerTableAction tta) {
        _trainName = name;
        _parent = tta;
        _markerForeground = block.getMarkerForeground();
        _markerBackground = block.getMarkerBackground();
        _markerFont = block.getMarkerFont();
        block.setState(block.getState() & ~OBlock.RUNNING); // jiggle-jiggle
        addtoOccupies(block, true);
        _startTime = System.currentTimeMillis();
        block._entryTime = _startTime;
        List<OBlock> occupy = initialRange(true, _parent);
        if (occupy.size() > 1) {
            new ChooseStartBlock(block, occupy, this, _parent);
        } else {
            _parent.addTracker(this);
        }
        if (marker != null) {
            marker.dock();
        }
    }

    private List<OBlock> initialRange(boolean firstTime, TrackerTableAction parent) {
        makeRange();
        ArrayList<OBlock> occupy = new ArrayList<>();
        if (firstTime) {
            for (OBlock b : _headRange) {
                if (!b.equals(getHeadBlock())
                        && (b.getState() & OBlock.OCCUPIED) != 0 && parent.checkBlock(b)) {
                    occupy.add(b); // make additional block the tail
                }
            }
        } else {
            for (OBlock b : _tailRange) {
                if (!b.equals(getTailBlock())
                        && (b.getState() & OBlock.OCCUPIED) != 0 && parent.checkBlock(b)) {
                    occupy.add(b); // make additional block the tail
                }
            }
        }
        return occupy;
    }

    /*
     * Jiggle state so Indicator icons show block value
     */
    private void showBlockValue(OBlock block) {
        block.setValue(_trainName);
        block.setMarkerBackground(_markerBackground);
        block.setMarkerForeground(_markerForeground);
        block.setMarkerFont(_markerFont);
        block.setState(block.getState() | OBlock.RUNNING);
    }

    protected String getTrainName() {
        return _trainName;
    }

    final protected OBlock getHeadBlock() {
        return _occupies.peekFirst();
    }

    final protected OBlock getTailBlock() {
        return _occupies.peekLast();
    }

    protected String getStatus() {
        if (getHeadBlock() == null) {
            return Bundle.getMessage("TrackerLocationLost", _trainName);
        }
        long et = (System.currentTimeMillis() - getHeadBlock()._entryTime) / 1000;
        return Bundle.getMessage("TrackerStatus", _trainName, getHeadBlock().getDisplayName(), et / 60, et % 60);
    }

    /**
     * Check if there is a path set between blkA and blkB with at most
     * one dark block between them.
     * blkA is the current Head or Tail block
     * blkB is a block from the headRange or tailRange, where entry may be possible
     */
   private PathSet hasPathBetween(@Nonnull OBlock blkA, @Nonnull OBlock blkB) {
       // first check if there is an exit path set from blkA, to blkB
       OBlock darkBlock = null;
       PathSet hasPathSet = PathSet.NO;
       PathSet darkPathSet = PathSet.NO;
       for (Portal portal : blkA.getPortals()) {
           OBlock block = portal.getOpposingBlock(blkA);
           if (blkB.equals(block)) {
               if (!getPathsSet(blkA, portal).isEmpty()) { // set paths of blkA to portal
                   hasPathSet = PathSet.PARTIAL;
                  if (!getPathsSet(blkB, portal).isEmpty()) { // paths of blkB to portal
                      _darkBlock = null;
                      return PathSet.SET;
                  }
               } else if (!getPathsSet(blkB, portal).isEmpty()) {
                   hasPathSet = PathSet.PARTIAL;
               }
           } else if ((block.getState() & OBlock.UNDETECTED) != 0 && darkPathSet != PathSet.SET) {
               darkBlock = block;
           }
       }
       if (darkBlock != null) {
           for (Portal portal : blkA.getPortals()) {
               OBlock block = portal.getOpposingBlock(blkA);
               if ((block.getState() & OBlock.UNDETECTED) != 0) {
                   // connected to a dark block. Look for a path through it to blkB
                   for (Portal port : block.getPortals()) {
                       OBlock b = port.getOpposingBlock(block);
                       if (blkB.equals(b)) {
                           // "block" is a dark block between blkA and blkB
                           PathSet setA = PathSet.NO; // for paths between blkA and darkBlock
                           PathSet setB = PathSet.NO; // for paths between darkBlock and blkB
                           if (!getPathsSet(blkA, portal).isEmpty()) {
                               setA =  PathSet.PARTIAL;
                               if (!getPathsSet(block, portal).isEmpty()) {
                                   setA = PathSet.SET;
                               }
                           } else if (!getPathsSet(block, portal).isEmpty()) {
                               setA = PathSet.PARTIAL;
                           }
                               
                           if (!getPathsSet(blkB, port).isEmpty()) {
                               setB =  PathSet.PARTIAL;
                               if (!getPathsSet(block, port).isEmpty()) {
                                   setB = PathSet.SET;
                               }
                           } else if (!getPathsSet(block, port).isEmpty()) {
                               setB = PathSet.PARTIAL;
                           }
                           switch (setA) {
                               case SET:
                                   if (setB == PathSet.SET) {
                                       darkPathSet = PathSet.SET;
                                       _darkBlock = block;
                                       break;
                                   } else if (setB == PathSet.PARTIAL) {
                                       if (darkPathSet != PathSet.SET) {
                                           darkPathSet = PathSet.PARTIAL;
                                           _darkBlock = block;
                                       }
                                   }
                                   break;
                               case PARTIAL:
                                   if (setB == PathSet.SET || setB == PathSet.PARTIAL) {
                                       if (darkPathSet != PathSet.SET) {
                                           darkPathSet = PathSet.PARTIAL;
                                           _darkBlock = block;
                                       }
                                   }
                                   break;
                               default:
                                   darkBlock = null;
                                   break;    
                           }                           
                       }
                   }
               }
           }
       }
       if (darkPathSet == PathSet.SET) {
           hasPathSet = darkPathSet;
       } else if (hasPathSet == PathSet.PARTIAL) {
           _darkBlock = darkBlock;
       }
       if (hasPathSet != PathSet.SET) {
           log.info("train {} has no path set from block {} to block {}",
                   _trainName, blkA.getDisplayName(), blkB.getDisplayName());
       }
        return hasPathSet;
    }
    
    protected PathSet hasPathInto(OBlock block) throws JmriException {
        _darkBlock = null;
        OBlock blk = getHeadBlock();
        if (blk != null) {
            PathSet hasPathSet = hasPathBetween(blk, block);
            if (hasPathSet != PathSet.NO) {
                return hasPathSet;
            }
        }
        OBlock b = getTailBlock();
        if (b == null) {
            throw new JmriException();
        }
        if (!b.equals(blk)) {
            return  hasPathBetween(b, block);
        }
        if (log.isDebugEnabled()) {
            log.debug("Tracker {} does not have a path into {}", _trainName, block.getDisplayName());
        }
        return PathSet.NO;
    }

    /**
     * Get All paths in OBlock "block" that are set to go to Portal "portal"
     */
    private List<OPath> getPathsSet(OBlock block, Portal portal) {
        List<OPath> paths = portal.getPathsWithinBlock(block);
        List<OPath> setPaths = new ArrayList<>();
        for (OPath path : paths) {
            if (path.checkPathSet()) {
                log.debug("Train {}. Path {} in block {} is set to portal {}",
                        _trainName, path.getName(), block.getDisplayName(), portal.getDisplayName());
                setPaths.add(path);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Train {}. Path {} in block {} NOT set to portal {}",
                            _trainName, path.getName(), block.getDisplayName(), portal.getDisplayName());
                }
            }
        }
        return setPaths;
    }

    private void addtoHeadRange(OBlock b) {
        if (b != null) {
            if (!_headRange.contains(b) && !_occupies.contains(b) && !_tailRange.contains(b)) {
                _headRange.add(b);
            }
        }
    }

    private void addtoTailRange(OBlock b) {
        if (b != null) {
            if (!_tailRange.contains(b) && !_occupies.contains(b) && !_headRange.contains(b)) {
                _tailRange.add(b);
            }
        }
    }

    private void addtoOccupies(OBlock b, boolean atHead) {
        if (b != null) {
            if (!_occupies.contains(b)) {
                if (atHead) {
                    _occupies.addFirst(b);
                } else {
                    _occupies.addLast(b);
                }
                showBlockValue(b);
                if (_lostRange.contains(b)) {
                    _lostRange.remove(b);
                    if (log.isDebugEnabled()) {
                        log.debug("Tracker {} recovers block \"{}\"",
                            _trainName, b.getDisplayName());
                    }
                }
            } else {
                log.warn("Tracker {} already occupies \"{}\"", 
                        _trainName, b.getDisplayName());
            }
        }
    }

    private void removeFromOccupies(OBlock b) {
        if (b != null) {
            _occupies.remove(b);
            if (_lostRange.contains(b)) {
                _lostRange.remove(b);
                log.debug("Tracker {} recovers block \"{}\"",
                        _trainName, b.getDisplayName());
            }
        }
    }
    /*
     * Note: Caller will modify List
     */
    protected List<OBlock> getRange() {
        return _range;
    }

    /**
     * Build array of blocks reachable from head and tail portals
     */
    private void makeRange() {
        _headRange = new ArrayList<OBlock>();
        _tailRange = new ArrayList<OBlock>();
        OBlock headBlock = getHeadBlock();
        OBlock tailBlock = getTailBlock();
        if (log.isDebugEnabled()) {
            log.debug("Make range for \"{}\"", _trainName);
        }
        if (headBlock != null) {
            for (Portal portal : headBlock.getPortals()) {
                OBlock block = portal.getOpposingBlock(headBlock);
                if (block != null) {
                    if ((block.getState() & OBlock.UNDETECTED) != 0) {
                        for (Portal p : block.getPortals()) {
                            OBlock blk = p.getOpposingBlock(block);
                            if (!blk.equals(headBlock)) {
                                addtoHeadRange(blk);                        
                            }
                        }
                    }  else {
                        addtoHeadRange(block);
                    }
                }
            }
        }
        if (tailBlock != null && !tailBlock.equals(headBlock)) {
            for (Portal portal : tailBlock.getPortals()) {
                OBlock block = portal.getOpposingBlock(tailBlock);
                if (block != null) {
                    if ((block.getState() & OBlock.UNDETECTED) != 0) {
                        for (Portal p : block.getPortals()) {
                            OBlock blk = p.getOpposingBlock(block);
                            if (!blk.equals(tailBlock)) {
                                addtoTailRange(blk);                        
                            }
                        }
                    } else {
                        addtoTailRange(block);
                    }
                 }
            }
        }
        if (log.isTraceEnabled()) {
            log.debug("   _headRange.size()= " + _headRange.size());
            log.debug("   _tailRange.size()= " + _tailRange.size());
            log.debug("   _lostRange.size()= " + _lostRange.size());
            log.debug("   _occupies.size()= " + _occupies.size());
        }

        buildRange();
    }
    private void buildRange() {
        // make new list since tracker table is holding the old list
        _range = new ArrayList<OBlock>();
        if (_occupies.size() == 0) {
            log.warn("{} does not occupy any blocks!", _trainName);
        }
        Iterator<OBlock> it = _occupies.iterator();
        while (it.hasNext()) {
            OBlock b = it.next();
            _range.add(b);
            if (log.isDebugEnabled()) {
                log.debug("   {} occupies \"{}\" value= {}", _trainName, b.getDisplayName(), b.getValue());
            }
        }
        it = _headRange.iterator();
        while (it.hasNext()) {
            OBlock b = it.next();
            _range.add(b);
            if (log.isDebugEnabled()) {
                log.debug("   {} head range from {} includes \"{}\" value= {}",
                        _trainName, getHeadBlock().getDisplayName(), b.getDisplayName(), b.getValue());
            }
        }
        it = _tailRange.iterator();
        while (it.hasNext()) {
            OBlock b = it.next();
            _range.add(b);
            if (log.isDebugEnabled()) {
                log.debug("   {} tail range from {} includes \"{}\" value= {}",
                        _trainName, getTailBlock().getDisplayName(), b.getDisplayName(), b.getValue());
            }
        }
        it = _lostRange.iterator();
        while (it.hasNext()) {
            OBlock b = it.next();
            _range.add(b);
            if (log.isDebugEnabled()) {
                log.debug("   {} lost range contains \"{}\" value= {}", _trainName, b.getDisplayName(), b.getValue());
            }
        }
    }

    protected List<OBlock> getBlocksOccupied() { 
        return _occupies;
    }

    private void removeBlock(@Nonnull OBlock block) {
        int size = _occupies.size();
        int index = _occupies.indexOf(block);
        switch (index) {
            case -1:
                log.error(" Cannot remove. Tracker {} does not occupy block \"{}\"!", _trainName, block.getDisplayName());
                return;
            case 0:
                // Head Block
                if (log.isDebugEnabled() && size > 1) {
                    hasPathBetween(_occupies.getFirst(), _occupies.get(1));
                }
                while (++index < size-1) {
                    OBlock b = _occupies.get(index);
                    if ((b.getState() & OBlock.OCCUPIED) == 0) {
                        // next to first block unoccupied. Remove it also.
                        removeFromOccupies(b);
                        _lostRange.add(b);
                    }
                }
                break;
            default:
                if (index == size-1) {
                    // Tail block
                    if (log.isDebugEnabled() && size > 1) {
                        hasPathBetween(_occupies.getLast(), _occupies.get(size - 2));
                    }
                    while (--index > 0) {
                        OBlock b = _occupies.get(index);
                        if ((b.getState() & OBlock.OCCUPIED) == 0) {
                            // next to last block unoccupied. Remove it also.
                            removeFromOccupies(b);
                            _lostRange.add(b);
                        }
                    }
                } else if (size > 2) {
                    // Mid range. Temporary lost of detection?  Don't remove from _occupies
                    log.warn("Tracker {} lost occupancy mid train at block \"{}\"!", _trainName, block.getDisplayName());
                    _statusMessage = Bundle.getMessage("trackerLostBlock", _trainName, block.getDisplayName());
                    return;
                }
               break;
        }
        removeFromOccupies(block);
        List<Portal> list = block.getPortals();
        Iterator<Portal> iter = list.iterator();
        while (iter.hasNext()) {    // remove associated dark blocks
            OBlock b = iter.next().getOpposingBlock(block);
            if ((b.getState() & OBlock.UNDETECTED) != 0) {
                removeFromOccupies(b);
                removeName(b);
            }
        }
        removeName(block);
        // consider doing _lostRange.add(block); for above unexpected cases that may be temporary lost of detection.
    }

    private void removeName(OBlock block) {
        if (_trainName.equals(block.getValue())) {
            block.setValue(null);
            block.setState(block.getState() & ~OBlock.RUNNING);
        }
    }

    protected boolean move(OBlock block, int state) {
        if (log.isDebugEnabled()) {
            log.debug("move( {}, {}) by {}", block.getDisplayName(), state, _trainName);
        }
        _statusMessage = null;
        if ((state & OBlock.OCCUPIED) != 0) {
            if (_occupies.contains(block)) {
                if (block.getValue() == null) { // must be a regained lost block
                    block.setValue(_trainName);
                    showBlockValue(block);
                    _statusMessage = Bundle.getMessage("TrackerReentry", _trainName, block.getDisplayName());
                    _lostRange.remove(block);
                } else if (!block.getValue().equals(_trainName)) {
                    log.error("Block \"{}\" occupied by \"{}\", but block.getValue()= {}!",
                            block.getDisplayName(),  _trainName, block.getValue());
                }
            } else if (_lostRange.contains(block)) {
                _lostRange.remove(block);
                if (log.isDebugEnabled()) {
                    log.debug("Block \"{}\" is occupied and was in lost range for train {}.", _trainName, block.getDisplayName());
                }
            }
            Warrant w = block.getWarrant();
            if (w != null) {
                String msg = Bundle.getMessage("AllocatedToWarrant", 
                        w.getDisplayName(), block.getDisplayName(), w.getTrainName());
                int idx = w.getCurrentOrderIndex();
                // Was it the warranted train that entered the block?
                // Can't tell who got notified first - tracker or warrant?
                // is distance of 1 block OK?
                if (log.isDebugEnabled()) {
                    log.debug("Block \"{}\" allocated to warrant \"{}\" currentIndex= {} blockIndex= {}",
                            block.getDisplayName(), w.getDisplayName(), w.getCurrentOrderIndex(), w.getIndexOfBlock(block, 0));
                }
                if (Math.abs(w.getIndexOfBlock(block, 0) - idx) < 2) {
                    _statusMessage = msg;
                } else {  // otherwise claim it for tracker
                    log.warn(_statusMessage);
                }
            }
            if (_headRange.contains(block)) {
                if (_darkBlock != null) {
                    addtoOccupies(_darkBlock, true);
                }
                addtoOccupies(block, true);
            } else if (_tailRange.contains(block)) {
                if (_darkBlock != null) {
                    addtoOccupies(_darkBlock, false);
                }
                addtoOccupies(block, false);
            } else if (!_occupies.contains(block)) {
                log.warn("Block \"" + block.getDisplayName() + "\" is not within range of  \"" + _trainName + "\"!");
            }
            makeRange();
            return true;
        } else if ((state & OBlock.UNOCCUPIED) != 0) {
            removeBlock(block);
            int size = _occupies.size();
            if (size == 0) {    // lost tracker
                recover(block);
            } else {    // otherwise head or tail is holding a path fixed through a portal (thrown switch should have derailed train by now)
                if (!_occupies.contains(block)) {
                    if (log.isDebugEnabled() && size > 1) {
                        hasPathBetween(_occupies.getLast(), _occupies.get(size - 2));
                        hasPathBetween(_occupies.getFirst(), _occupies.get(1));
                    }
                }
                makeRange();
            }
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return _trainName;
    }

    private void recover(OBlock block) {
        // make list of possible blocks
        ArrayList<OBlock> list = new ArrayList<>();
        list.addAll(_lostRange);
        list.addAll(_headRange);
        list.addAll(_tailRange);
        list.add(block);
        try {   // sleep for 1 sec.  maybe block with bad detection will come back
            Thread.sleep(1000);    
        } catch (InterruptedException ie) {
            // continue
        }

        // reduce list to occupied blocks 
        List<OBlock> lostRange = new ArrayList<>();
        for (OBlock b : list) {
            if ((b.getState() & (OBlock.UNDETECTED | OBlock.OCCUPIED)) != 0) {
                lostRange.add(b);
                if (log.isDebugEnabled()) {
                    log.debug("  lostRange.add " + b.getDisplayName() + " value= " + b.getValue());
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("recovery: list size= {} filtered size= {}", list.size(), lostRange.size());
        }
        TrackerTableAction parent = InstanceManager.getDefault(TrackerTableAction.class);
        if (lostRange.isEmpty()) {
            parent.stopTracker(this, block);
            parent.setStatus(Bundle.getMessage("TrackerNoCurrentBlock", _trainName,
                    block.getDisplayName()) + "\n" + Bundle.getMessage("TrackingStopped"));
            return;
        }
        try {   // Get user's attention
            //SoundTone.tone(1760, 100);
            SoundTone.tone(880, 400, .7);
        } catch (javax.sound.sampled.LineUnavailableException lue) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }                        
        new ChooseRecoverBlock(block, lostRange, this, parent);
    }

    private class ChooseStartBlock extends ChooseBlock {

        ChooseStartBlock(OBlock b, List<OBlock> l, Tracker t, TrackerTableAction tta) {
            super(b, l, t, tta);
        }

        JPanel makeBlurb() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel(Bundle.getMessage("MultipleStartBlocks", getHeadBlock().getDisplayName(), _trainName)));
            return panel;
        }

        JPanel makeButtonPanel() {
            JPanel panel = new JPanel();
            JButton startButton = new JButton(Bundle.getMessage("ButtonStart"));
            startButton.addActionListener((ActionEvent a) -> {
                _parent.addTracker(tracker);
                dispose();
            });
            panel.add(startButton);
            return panel;
        }

        void doAction() {
            parent.addTracker(tracker);
        }
    }

    private class ChooseRecoverBlock extends ChooseBlock {

        ChooseRecoverBlock(OBlock b, List<OBlock> l, Tracker t, TrackerTableAction tta) {
            super(b, l, t, tta);
        }

        JPanel makeBlurb() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel(Bundle.getMessage("TrackerNoCurrentBlock", _trainName, block.getDisplayName())));
            panel.add(new JLabel(Bundle.getMessage("PossibleLocation", _trainName)));
            return panel;
        }

        JPanel makeButtonPanel() {
            JPanel panel = new JPanel();
            JButton recoverButton = new JButton(Bundle.getMessage("ButtonRecover"));
            recoverButton.addActionListener((ActionEvent a) -> {
                if (_occupies.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                            Bundle.getMessage("RecoverOrExit", _trainName, Bundle.getMessage("ButtonStop")),
                            Bundle.getMessage("WarningTitle"), JOptionPane.INFORMATION_MESSAGE);                    
                } else {
                    doAction();
                }
            });
            panel.add(recoverButton);

            JButton cancelButton = new JButton(Bundle.getMessage("ButtonStop"));
            cancelButton.addActionListener((ActionEvent a) -> {
                parent.stopTracker(tracker, block);
                parent.setStatus(Bundle.getMessage("TrackerNoCurrentBlock", _trainName,
                        block.getDisplayName()) + "\n" + Bundle.getMessage("TrackingStopped"));
                dispose();
            });
            panel.add(cancelButton);
            return panel;
        }        

        void doAction() {
            parent.addBlockListeners(tracker);
            parent.setStatus(Bundle.getMessage("restartTracker",
                    tracker.getTrainName(), tracker.getHeadBlock().getDisplayName()));
        }
    }

    abstract class ChooseBlock extends JDialog implements ListSelectionListener {
        OBlock block;
        TrackerTableAction parent;
        JList<OBlock> _jList;
        Tracker tracker;
         
        ChooseBlock(OBlock b, List<OBlock> list, Tracker t, TrackerTableAction tta) {
            super(tta._frame);
            setTitle(Bundle.getMessage("TrackerTitle"));
            block = b;
            tracker = t;
            parent = tta;

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            contentPanel.add(Box.createVerticalStrut(TrackerTableAction.STRUT_SIZE));
            JPanel p = new JPanel();
            p.add(makeBlurb());
            contentPanel.add(p);

            JPanel panel = new JPanel();
            panel.setBorder(javax.swing.BorderFactory .createLineBorder(Color.black, 2));
            _jList = new JList<>();
            _jList.setModel(new BlockListModel(list));
            _jList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            _jList.addListSelectionListener(this);
            panel.add(_jList);
            p = new JPanel();
            p.add(panel);
            contentPanel.add(p);
            
            contentPanel.add(Box.createVerticalStrut(TrackerTableAction.STRUT_SIZE));
            contentPanel.add(makeButtonPanel());
            setContentPane(contentPanel);
            pack();
            setLocation(parent._frame.getLocation());
            setAlwaysOnTop(true);
            setVisible(true);
        }

        abstract JPanel makeBlurb();
        abstract JPanel makeButtonPanel();
        abstract void doAction();

        @Override
        public void valueChanged(ListSelectionEvent e) {
            OBlock b = _jList.getSelectedValue();
            if (b != null) {
                b.setState(b.getState() & ~OBlock.RUNNING);
                addtoOccupies(b, false); // make additional block the tail
                b._entryTime = System.currentTimeMillis();
                _jList.removeListSelectionListener(this);
                List<OBlock> list = initialRange(false, parent);
                if (list.isEmpty()) {
                    doAction();
                    dispose();
                }
                _jList.setModel(new BlockListModel(list));
                _jList.addListSelectionListener(this);
            }
        }

        class BlockListModel extends AbstractListModel<OBlock> {
            List<OBlock> blockList;

            BlockListModel(List<OBlock> bl) {
                blockList = bl;
            }

            @Override
            public int getSize() {
                return blockList.size();
            }

            @Override
            public OBlock getElementAt(int index) {
                return blockList.get(index);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Tracker.class);
}
