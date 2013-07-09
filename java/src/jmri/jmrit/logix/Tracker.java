package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Track an occupied block to its adjacent blocks.
 *
 * @version $Revision: 22833 $
 * @author	Pete Cressman  Copyright (C) 2013
 */

final public class Tracker {

	OBlock _currentBlock;
	OBlock _prevBlock;
	String _trainName;
	ArrayList<OBlock> _range;
	private long _time;
	static final int NO_BLOCK = 0;
	static final int ENTER_BLOCK = 1;
	static final int LEAVE_BLOCK = 2;
	static final int ERROR_BLOCK = 3;
	
    Tracker(OBlock block, String name) {
    	_currentBlock = block;
    	_prevBlock = block;
    	_trainName = name;
    	_range = new ArrayList<OBlock>();
    	makeRange();
        _time = System.currentTimeMillis();
        showBlockValue(_currentBlock);
    }
 
    /*
     * Jiggle state so Indicator icons show block value
     */
    private void showBlockValue(OBlock block) {
    	block.setState(block.getState() | OBlock.RUNNING);    	
    }

    protected String getTrainName() {
    	return _trainName;
    }
    
    protected OBlock getPreviousBlock() {
    	return _prevBlock;
    }
    
    protected OBlock getCurrentBlock() {
    	return _currentBlock;
    }
    
    protected String getStatus() {
    	long et = (System.currentTimeMillis()-_time)/1000;
    	return Bundle.getMessage("TrackerStatus", _trainName, _currentBlock.getDisplayName(), et/60, et%60);
    }
    
    protected void makeRange() {
    	List <Portal> list = _currentBlock.getPortals();
    	_range = new ArrayList<OBlock>();
       	_currentBlock.setValue(_trainName);
       	showBlockValue(_currentBlock);
    	_range.add(_currentBlock);
    	Iterator<Portal> iter = list.iterator();
    	while (iter.hasNext()) {
    		OBlock b = iter.next().getOpposingBlock(_currentBlock);
    		_range.add(b);
    		if ((b.getState() & OBlock.OCCUPIED) != 0 && !b.equals(_prevBlock) &&
    						!_trainName.equals(b.getValue())) {
    			log.info("Adjacent block \""+b.getDisplayName()+"\" is already occupied.  Tracking of \""+
    					_trainName+"\" may fail to be accurate.");
    	        if (log.isDebugEnabled()) log.debug("Occupied block= "+b.getDisplayName()+
    	        		", _prevBlock= "+_prevBlock.getDisplayName()+", _currentBlock= "+_currentBlock.getDisplayName());
    		}
            _time = System.currentTimeMillis();
    	}
        if (log.isDebugEnabled()) {
        	log.debug("makeRange for currentBlock= "+_currentBlock.getDisplayName());
        	Iterator<OBlock> it = _range.iterator();
        	while (it.hasNext()) {
        		OBlock b = it.next();
        		log.debug("   "+b.getDisplayName()+" value= "+b.getValue());    			
        	}
        }
    }
    
    protected List<OBlock> getRange() {
    	ArrayList<OBlock> range = new ArrayList<OBlock>();
    	Iterator<OBlock> iter = _range.iterator();
    	while (iter.hasNext()) {
    		range.add(iter.next());    			
    	}
    	return range;
    }
    
    protected void dropRange() {
    	Iterator<OBlock> iter = _range.iterator();
    	while (iter.hasNext()) {
    		OBlock b = iter.next();
    		if (_trainName.equals(b.getValue())) {
        		b.setValue(null);
            	b.setState(b.getState() & ~OBlock.RUNNING);
    		}
    	}
    }
    
    protected int move(OBlock block, int state) {
        if (log.isDebugEnabled()) {
        	log.debug("move( "+block.getDisplayName()+", "+state+") _prevBlock= "+
        		_prevBlock.getDisplayName()+", _currentBlock= "+_currentBlock.getDisplayName());
        	Iterator<OBlock> iter = _range.iterator();
        	while (iter.hasNext()) {
        		OBlock b = iter.next();
        		log.debug("   "+b.getDisplayName()+" value= "+b.getValue());    			
        	}
        }
        if (_range.size()==0) {
        	return LEAVE_BLOCK;
        }
        else if (!_range.contains(block)) {
    		return ERROR_BLOCK;    		
    	}
    	if ((state & OBlock.OCCUPIED) != 0) {
	        _prevBlock = _currentBlock;
            _currentBlock = block;
            _currentBlock.setMarkerBackground(_prevBlock.getMarkerBackground());
            _currentBlock.setMarkerForeground(_prevBlock.getMarkerForeground());
         	makeRange();
         	return ENTER_BLOCK;
    	} else if ((state & OBlock.UNOCCUPIED) != 0) {
    		if (block.equals(_currentBlock)) {
    			if ((_prevBlock.getState()& OBlock.OCCUPIED) != 0 ) {
    				_currentBlock = _prevBlock;
    				_prevBlock = block;
    				makeRange();           			
    	         	return ENTER_BLOCK;
    			} else {
    			}	
    		} else if (block.equals(_prevBlock)) {
    			_prevBlock = _currentBlock;
	         	return LEAVE_BLOCK;
    		}
    	}  	
    	Iterator<OBlock> iter = _range.iterator();
    	while (iter.hasNext()) {
    		OBlock b = iter.next();
    		if ((b.getState() & OBlock.OCCUPIED) != 0 && _trainName.equals(b.getValue())) {
    	        _prevBlock = _currentBlock;
        		_currentBlock = b;    			
				makeRange();           			
	         	return ENTER_BLOCK;
    		}
    	}
  		return NO_BLOCK;
    }

    static Logger log = LoggerFactory.getLogger(Tracker.class.getName());
}