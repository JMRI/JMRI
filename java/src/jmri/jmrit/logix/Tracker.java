package jmri.jmrit.logix;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Track an occupied block to its adjacent blocks.
 *
 * @author	Pete Cressman Copyright (C) 2013
 */
public class Tracker {

//	OBlock _currentBlock;
//	OBlock _prevBlock;
    private String _trainName;
    private ArrayList<OBlock> _headRange;	// blocks reachable from head block
    private ArrayList<OBlock> _tailRange;	// blocks reachable from tail block
    private ArrayList<OBlock> _lostRange;	// reachable block occupied by someone else
    private LinkedList<OBlock> _occupies;	// blocks occupied by train
    private Portal _headPortal;
    private Portal _tailPortal;
    private long _time;
    static final int NO_BLOCK = 0;
    static final int ENTER_BLOCK = 1;
    static final int LEAVE_BLOCK = 2;
    static final int ERROR_BLOCK = 3;
    private Color _markerForeground;
    private Color _markerBackground;
    private Font _markerFont;

    /**
     * Must Call setupCheck() after constructor to check environment of train
     *
     */
    Tracker(OBlock block, String name) {
        _trainName = name;
        _occupies = new LinkedList<OBlock>();
        _markerForeground = block.getMarkerForeground();
        _markerBackground = block.getMarkerBackground();
        _markerFont = block.getMarkerFont();
        _occupies.addFirst(block);
        _time = System.currentTimeMillis();
        block._entryTime = _time;
        showBlockValue(block);
    }

    /**
     * Creator of a tracker must call immediately after constructor to verify
     * the blocks occupied by the train at start. _occupies should contain
     * exactly one block.
     */
    public void setupCheck() {
        if (log.isDebugEnabled()) {
            log.debug("setupCheck() for \"" + _trainName + "\"");
        }
        List<OBlock> adjacentBlocks = makeRange();
        ArrayList<OBlock> occupy = new ArrayList<OBlock>();
        Iterator<OBlock> it = adjacentBlocks.iterator();
        while (it.hasNext()) {
            OBlock b = it.next();
            if (!b.equals(getHeadBlock()) && b.getValue() == null && (b.getState() & OBlock.OCCUPIED) != 0) {
                occupy.add(b);
            }
        }
        if (occupy.size() > 0) {
            String[] blocks = new String[occupy.size() + 1];
            Iterator<OBlock> iter = occupy.iterator();
            int i = 0;
            blocks[i++] = Bundle.getMessage("none");
            while (iter.hasNext()) {
                blocks[i++] = iter.next().getDisplayName();
            }
            Object selection = JOptionPane.showInputDialog(null,
                    Bundle.getMessage("MultipleStartBlocks", _occupies.peekFirst().getDisplayName(), _trainName),
                    Bundle.getMessage("WarningTitle"), JOptionPane.INFORMATION_MESSAGE, null, blocks, null);
            iter = occupy.iterator();
            while (iter.hasNext()) {
                OBlock b = iter.next();
                if (b.getDisplayName().equals(selection)) {
                    showBlockValue(b);
                    _occupies.addLast(b);	// make additional block the tail
                    _headPortal = getPortalBetween(getHeadBlock(), getTailBlock());
                    _tailPortal = _headPortal;
                    break;
                }
            }
            makeRange();
        }
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
        long et = (System.currentTimeMillis() - _time) / 1000;
        if (getHeadBlock() == null) {
            return Bundle.getMessage("TrackerLocationLost", _trainName);
        }
        return Bundle.getMessage("TrackerStatus", _trainName, getHeadBlock().getDisplayName(), et / 60, et % 60);
    }

    static private Portal getPortalBetween(OBlock blkA, OBlock blkB) {
        List<Portal> listA = blkA.getPortals();
        ArrayList<Portal> list = new ArrayList<Portal>();

        Iterator<Portal> iter = listA.iterator();
        while (iter.hasNext()) {
            Portal p = iter.next();
            if (blkB.equals(p.getOpposingBlock(blkA))) {
                list.add(p);
            }
        }
        int size = list.size();
        if (size == 0) {
            log.error("No portal between blocks \"" + blkA.getDisplayName() + "\" and \""
                    + blkB.getDisplayName() + "\".");
            return null;
        }
        if (size > 1) {
            log.info(size + " portals between blocks \"" + blkA.getDisplayName() + "\" and \""
                    + blkB.getDisplayName() + "\".");
        }
        return list.get(0);
    }

    /**
     * Build array of blocks reachable from head and tail portals
     */
    private List<OBlock> makeRange() {
        if (log.isDebugEnabled()) {
            log.debug("Make range for \"" + _trainName + "\"");
        }
        _headRange = new ArrayList<OBlock>();
        _tailRange = new ArrayList<OBlock>();
        _lostRange = new ArrayList<OBlock>();
        OBlock headBlock = getHeadBlock();
        OBlock tailBlock = getTailBlock();
        if (_headPortal == null) {
            List<Portal> list = headBlock.getPortals();
            Iterator<Portal> iter = list.iterator();
            while (iter.hasNext()) {
                OBlock b = iter.next().getOpposingBlock(headBlock);
                addtoHeadRange(b);
            }
        } else {
            List<OPath> pathList = _headPortal.getPathsWithinBlock(headBlock);
            Iterator<OPath> iter = pathList.iterator();
            while (iter.hasNext()) {
                OPath path = iter.next();
                Portal p = path.getToPortal();
                OBlock b = null;
                if (p != null && !_headPortal.equals(p)) {
                    b = p.getOpposingBlock(headBlock);
                } else {
                    p = path.getFromPortal();
                    if (p != null && !_headPortal.equals(p)) {
                        b = p.getOpposingBlock(headBlock);
                    }
                }
                addtoHeadRange(b);
            }
            pathList = _tailPortal.getPathsWithinBlock(tailBlock);
            iter = pathList.iterator();
            while (iter.hasNext()) {
                OPath path = iter.next();
                Portal p = path.getToPortal();
                OBlock b = null;
                if (p != null && !_tailPortal.equals(p)) {
                    b = p.getOpposingBlock(tailBlock);
                } else {
                    p = path.getFromPortal();
                    if (p != null && !_tailPortal.equals(p)) {
                        b = p.getOpposingBlock(tailBlock);
                    }
                }
                addtoTailRange(b);
            }
        }
        _time = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("   _headRange.size()= " + _headRange.size());
            log.debug("   _tailRange.size()= " + _tailRange.size());
            log.debug("   _lostRange.size()= " + _lostRange.size());
            log.debug("   _occupies.size()= " + _occupies.size());
        }

        return getRange();
    }

    private void addtoHeadRange(OBlock b) {
        if (b != null && !_headRange.contains(b) && !_occupies.contains(b)) {
            if ((b.getState() & OBlock.OCCUPIED) == 0) {
                _headRange.add(b);
            } else {
                _lostRange.add(b);
                if (log.isDebugEnabled()) {
                    log.debug("Adjacent block \"" + b.getDisplayName() + "\" is already occupied.  Tracking of \""
                            + _trainName + "\" from headBlock= \"" + getHeadBlock().getDisplayName() + "\" may fail to be accurate.");
                }
            }
        }
    }

    private void addtoTailRange(OBlock b) {
        if (b != null && !_tailRange.contains(b) && !_occupies.contains(b)) {
            if ((b.getState() & OBlock.OCCUPIED) == 0) {
                _tailRange.add(b);
            } else {
                _lostRange.add(b);
                if (log.isDebugEnabled()) {
                    log.debug("Adjacent block \"" + b.getDisplayName() + "\" is already occupied.  Tracking of \""
                            + _trainName + "\" from tailBlock= \"" + getTailBlock().getDisplayName() + "\" may fail to be accurate.");
                }
            }
        }
    }

    /**
     * Note: Caller will modify List
     */
    protected List<OBlock> getRange() {
        ArrayList<OBlock> range = new ArrayList<OBlock>();
        if (log.isDebugEnabled()) {
            log.debug("Get range for \"" + _trainName + "\"");
        }
        if (_occupies == null || _occupies.size() == 0) {
            return range;
        }
        Iterator<OBlock> it = _occupies.iterator();
        while (it.hasNext()) {
            OBlock b = it.next();
            range.add(b);
            if (log.isDebugEnabled()) {
                log.debug("   " + b.getDisplayName() + " value= " + b.getValue());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("_headRange for headBlock= " + getHeadBlock().getDisplayName());
        }
        it = _headRange.iterator();
        while (it.hasNext()) {
            OBlock b = it.next();
            range.add(b);
            if (log.isDebugEnabled()) {
                log.debug("   " + b.getDisplayName() + " value= " + b.getValue());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("_tailRange for tailBlock= " + getTailBlock().getDisplayName());
        }
        it = _tailRange.iterator();
        while (it.hasNext()) {
            OBlock b = it.next();
            range.add(b);
            if (log.isDebugEnabled()) {
                log.debug("   " + b.getDisplayName() + " value= " + b.getValue());
            }
        }
        it = _lostRange.iterator();
        while (it.hasNext()) {
            OBlock b = it.next();
            range.add(b);
            if (log.isDebugEnabled()) {
                log.debug("   " + b.getDisplayName() + " value= " + b.getValue());
            }
        }
        return range;
    }

    protected List<OBlock> getBlocksOccupied() {
        return _occupies;
    }

    protected void removeBlock(OBlock block) {
        _occupies.remove(block);
        List<Portal> list = block.getPortals();
        Iterator<Portal> iter = list.iterator();
        while (iter.hasNext()) {
            OBlock b = iter.next().getOpposingBlock(block);
            if ((b.getState() & OBlock.DARK) !=0) {
                _occupies.remove(b);                
                removeName(b);
                _lostRange.add(b);  // needed to find on recovery
            }
        }
        removeName(block);
    }
    private void removeName(OBlock block) {
        if (_trainName.equals(block.getValue())) {
            block.setValue(null);
            block.setState(block.getState() & ~OBlock.RUNNING);
        }        
    }

    protected int move(OBlock block, int state) {
        if (log.isDebugEnabled()) {
            log.debug("move( " + block.getDisplayName() + ", " + state+")");
        }
        if ((state & OBlock.OCCUPIED) != 0) {
            if (_occupies.contains(block)) {
                if (log.isDebugEnabled()) {
                    log.debug("Block \"" + block.getDisplayName() + "\" already occupied by \"" + _trainName + "\"!");
                }
            }
            if (_lostRange.contains(block)) {
                if (log.isDebugEnabled()) {
                    log.debug("\"" + _trainName + "\" should not be listening to block \"" + block.getDisplayName() + "\"");
                }
            }
            if (_headRange.contains(block)) {
                _headPortal = getPortalBetween(getHeadBlock(), block);
                _occupies.addFirst(block);
                showBlockValue(block);
                if (_tailPortal == null) {
                    _tailPortal = _headPortal;
                }
            } else if (_tailRange.contains(block)) {
                _tailPortal = getPortalBetween(getTailBlock(), block);
                _occupies.addLast(block);
                showBlockValue(block);
                if (_headPortal == null) {
                    _headPortal = _tailPortal;
                }
            } else {
                return ERROR_BLOCK;
            }
            makeRange();
            return ENTER_BLOCK;
        } else if ((state & OBlock.UNOCCUPIED) != 0) {
            if (_lostRange.contains(block)) {
                //OK, just do makeRange
            } else {
                if (!_occupies.contains(block)) {
                    // a different train can leave an adjacent block held in _lostRange.
                    if (log.isDebugEnabled()) {
                        log.debug("Block \"" + block.getDisplayName() + "\" is not occupied by \"" + _trainName + "\"!");
                    }
                    return ERROR_BLOCK;
                }
            }
            removeBlock(block);
            int size = _occupies.size();
            if (size == 0) {
                log.error("\"" + block.getDisplayName() + "\", going inactive, is last block occupied by \"" + _trainName + "\"!");
                if (recovery(block)) {
                    return ENTER_BLOCK;
                }
                return NO_BLOCK;
            } else if (size == 1) {
                _headPortal = null;
                _tailPortal = null;
            } else {
                _headPortal = getPortalBetween(_occupies.get(1), _occupies.getFirst());
                _tailPortal = getPortalBetween(_occupies.get(size - 2), _occupies.getLast());
            }
            makeRange();
            return LEAVE_BLOCK;
        }
        return NO_BLOCK;
    }

    /**
     * Called when _occupies is empty
     *
     */
    private boolean recovery(OBlock block) {
        if (_occupies.size()==0) {
            Iterator<OBlock> it = _headRange.iterator();
            while (it.hasNext()) {
                OBlock b = it.next();
                if ((b.getState() & (OBlock.DARK | OBlock.OCCUPIED)) != 0) {
                    _lostRange.add(b);
                    if (log.isDebugEnabled()) {
                        log.debug("  _lostRange.add " + b.getDisplayName() + " value= " + b.getValue());
                    }
                }
            }
            it = _tailRange.iterator();
            while (it.hasNext()) {
                OBlock b = it.next();
                if ((b.getState() & (OBlock.DARK | OBlock.OCCUPIED)) != 0) {
                    _lostRange.add(b);
                    if (log.isDebugEnabled()) {
                        log.debug("  _lostRange.add " + b.getDisplayName() + " value= " + b.getValue());
                    }
                }
            }            
        }
        if (log.isDebugEnabled()) {
            log.debug("recovery( " + block.getDisplayName() + ") + _lostRange.size()= "+ _lostRange.size());
        }
        if (_lostRange == null || _lostRange.size() == 0) {
            return false;
        }
        OBlock blk = (OBlock) JOptionPane.showInputDialog(null, Bundle.getMessage("TrackerNoCurrentBlock",
                _trainName, block.getDisplayName()) + "\n" + Bundle.getMessage("PossibleLocation"),
                Bundle.getMessage("WarningTitle"), JOptionPane.INFORMATION_MESSAGE, null,
                _lostRange.toArray(), null);
        if (blk != null) {
            _occupies.addFirst(blk);
            showBlockValue(blk);
        } else {
            return false;
        }
        setupCheck();
        return true;
    }

    public String toString() {
        return _trainName;
    }

    private final static Logger log = LoggerFactory.getLogger(Tracker.class.getName());
}
