package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Track an occupied block toits adjacent blocks recursively.
 *
 * @version $Revision: 22833 $
 * @author	Pete Cressman  Copyright (C) 2013
 */

public class Tracker extends java.beans.PropertyChangeSupport 
		implements java.beans.PropertyChangeListener {

	OBlock _currentBlock;
	OBlock _prevBlock;
	String _trainName;
	ArrayList<OBlock> _range;
	Runner _runner;
	private long _time;
    final ReentrantLock _lock = new ReentrantLock();
	
    Tracker(OBlock block, String name) {
    	super(name);
    	_currentBlock = block;
    	_prevBlock = block;
    	_trainName = name;
    	_range = new ArrayList<OBlock>();
    	getRange();
    	_runner = new Runner();
        new Thread(_runner).start();
        _time = System.currentTimeMillis();
        showBlockValue(_currentBlock);
    }
 
    /*
     * jiggle state so Indicator icons show block value
     */
    private void showBlockValue(OBlock block) {
    	int state = block.getState();
    	block.setState(0);
    	block.setState(state);    	
    }
   
    synchronized protected void stopTrain(boolean set ) {
    	Iterator<OBlock> iter = _range.iterator();
    	while (iter.hasNext()) {
    		OBlock b = iter.next();
    		b.setValue(null);
    		b.removePropertyChangeListener(this);
    		showBlockValue(b);
    	}
    	_runner.stopTrain();
    }
    
    protected String getTrainName() {
    	return _trainName;
    }
    
    protected OBlock getCurrentBlock() {
    	return _currentBlock;
    }
    
    protected String getStatus() {
    	long et = (System.currentTimeMillis()-_time)/1000;
    	return Bundle.getMessage("TrackerStatus", _trainName, _currentBlock.getDisplayName(), et/60, et%60);
    }
    
    private boolean getRange() {
    	dropRange();
    	boolean ret =false;
    	List <Portal> list = _currentBlock.getPortals();
    	_range = new ArrayList<OBlock>();
       	_currentBlock.setValue(_trainName);
    	_currentBlock.addPropertyChangeListener(this);
    	_range.add(_currentBlock);
    	Iterator<Portal> iter = list.iterator();
    	while (iter.hasNext()) {
    		OBlock b = iter.next().getOpposingBlock(_currentBlock);
    		_range.add(b);
    		b.addPropertyChangeListener(this);
    		b.setValue(_trainName);
    		if ((b.getState() & OBlock.OCCUPIED) != 0 && !b.equals(_prevBlock) && !b.equals(_currentBlock)) {
    			log.info("Adjacent block \""+b.getDisplayName()+"\" is already occupied.  Tracking of \""+
    					_trainName+"\" may fail to be accurate.");
    	        firePropertyChange("BlockOccupied", _trainName, b.getDisplayName());
    	        if (log.isDebugEnabled()) log.debug("Occupied block= "+b.getDisplayName()+
    	        		", _prevBlock= "+_prevBlock.getDisplayName()+", _currentBlock= "+_currentBlock.getDisplayName());
    	        ret = true;
    		}
            _time = System.currentTimeMillis();
    	}
    	return ret;
    }
    private void dropRange() {
    	Iterator<OBlock> iter = _range.iterator();
    	while (iter.hasNext()) {
    		OBlock b = iter.next();
    		b.setValue(null);
    		b.removePropertyChangeListener(this);
    	}
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("state") ) {
        	OBlock b = (OBlock)evt.getSource(); 
	        if (log.isDebugEnabled()) log.debug("block= "+b.getDisplayName()+" state= "+
	        		((Number)evt.getNewValue()).intValue()+", _prevBlock= "+_prevBlock.getDisplayName()+
	        		", _currentBlock= "+_currentBlock.getDisplayName());
        	if ((((Number)evt.getNewValue()).intValue() & OBlock.OCCUPIED) != 0) {
                synchronized(_runner) {
                    _currentBlock.setValue(null);
        	        _prevBlock = _currentBlock;
    	            _currentBlock = b;
                 	getRange();
                	_runner.notify();
                }
    	        firePropertyChange("BlockChange", _prevBlock.getDisplayName(), _currentBlock.getDisplayName());
            } else if ((((Number)evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
            	if (b.equals(_prevBlock)) {
            		_prevBlock = _currentBlock;
            	} if (b.equals(_currentBlock)) {
            		if (!_currentBlock.equals(_prevBlock)) {
                        synchronized(_runner) {
                            _currentBlock.setValue(null);
            	            _currentBlock = _prevBlock;
                	        _prevBlock = b;
                        	getRange();
                        	_runner.notify();
                        }          		
               	        firePropertyChange("BlockChange", _prevBlock.getDisplayName(), _currentBlock.getDisplayName());          	
            		} else {
            			log.error("Current Block went unoccupied with unknown previous block.");
               	        firePropertyChange("ErrorNoBlock", _trainName, _currentBlock.getDisplayName());          	
            		}
            	}
             }
	        if (log.isDebugEnabled()) log.debug("\t_prevBlock= "+_prevBlock.getDisplayName()+
	        		", _currentBlock= "+_currentBlock.getDisplayName());
		}
    }
    
    class Runner implements Runnable {
    	boolean _tracking = true;;
    	
    	void stopTrain() {
            synchronized(this) {
        		_tracking =false;
        		notify();
            }
    	}
        public void run() {
            if (log.isDebugEnabled()) log.debug("Tracker for "+_trainName+" started in block "+_currentBlock.getDisplayName());

            while (_tracking) {
                synchronized(this) {
                    try {
                        _lock.lock();
                        wait();
                    } catch (InterruptedException ie) {
                        log.error("InterruptedException "+ie);
                    }
                    finally {
                        _lock.unlock();
                    }
                }
            }
            dropRange();
            // shut down
        }
    }

    static Logger log = LoggerFactory.getLogger(Tracker.class.getName());
}