package jmri.jmrit.display;

import java.awt.Point;
import java.awt.Dimension;
import java.util.ArrayList;
import jmri.Sensor;
import jmri.jmrit.logix.OBlock;
/**
 * A utility class replacing common methods formerly implementing the 
 * IndicatorTrack interface.
 * <P>
 *
 * @author Pete Cressman Copyright (c) 2012
 * @version $Revision: 1 $
 */
	class IndicatorTrackPaths  {

	protected ArrayList <String> _paths;      // list of paths that this icon displays
	protected boolean _showTrain; 		// this track icon should display _loco when occupied
	protected LocoIcon _loco = null;
     
    protected IndicatorTrackPaths() {    	 
	}
     
	protected IndicatorTrackPaths deepClone() {
		IndicatorTrackPaths p = new IndicatorTrackPaths();
        if (_paths!=null) {
        	p._paths = new ArrayList<String>();
            for (int i=0; i<_paths.size(); i++) {
                p._paths.add(_paths.get(i));
            }
        }
        p._showTrain = _showTrain;
		return p;   	 
    }
	
	protected ArrayList<String> getPaths() {
        return _paths;
    }
	protected void setPaths(ArrayList<String>paths) {
		if (paths == null) {
	        _paths = paths;			
		} else {
			_paths = new ArrayList<String>();			
            for (int i=0; i<paths.size(); i++) {
            	_paths.add(paths.get(i).trim());
            }
		}
    }

	protected void addPath(String path) {
        if (_paths==null) {
            _paths = new ArrayList<String>();
        }
        if (path!=null && path.length()>0) {
            path = path.trim();
            if (!_paths.contains(path)) {
                _paths.add(path);
            }        	
        }
        if (log.isDebugEnabled()) log.debug("addPath \""+path+"\" #paths= "+_paths.size());
    }
	protected void removePath(String path) {
        if (_paths!=null) {
            if (path!=null && path.length()>0) {
                path = path.trim();
                _paths.remove(path);
            }
        }
    }
	
	protected void setShowTrain(boolean set) {
        _showTrain = set;
    }
	protected boolean showTrain() {
        return _showTrain;
    }
	protected String setStatus(OBlock block, int state) {
        String pathName = block.getAllocatedPathName();
        String status;
    	removeLocoIcon();
        if ((state & OBlock.TRACK_ERROR)!=0) {
            status = "ErrorTrack";
        } else if ((state & OBlock.OUT_OF_SERVICE)!=0) {
            status = "DontUseTrack";
        } else if ((state & OBlock.ALLOCATED)!=0) {
            if (_paths!=null && _paths.contains(pathName)) {
            	if ((state & OBlock.RUNNING)!=0) {
                    status = "PositionTrack";           		
            	} else {
                    status = "AllocatedTrack";            		
            	}
            } else {
            	status = "ClearTrack";     // icon not on path
            }
        } else if ((state & OBlock.OCCUPIED)!=0) {
           	status = "OccupiedTrack";       	
        } else if ((state & Sensor.UNKNOWN)!=0) {
            status = "DontUseTrack";
        } else {
        	status = "ClearTrack";             	       	
        }
        return status;
    }

    private void removeLocoIcon() {
        if (_loco!=null) {
            _loco.remove();
            _loco = null;
        }    	
    }

    protected void setLocoIcon(String trainName, Point pt, Dimension size, Editor ed) {
        if (!_showTrain) {
            return;
        }
        if (trainName==null) {
        	removeLocoIcon();
            return;
        }
        if (_loco!=null) {
            return;
        }
        trainName = trainName.trim();
        _loco = ed.selectLoco(trainName);
        if (_loco==null) {
            _loco = ed.addLocoIcon(trainName);
        }
        if (_loco!=null) {
            pt.x = pt.x + (size.width - _loco.getWidth())/2;
            pt.y = pt.y + (size.height - _loco.getHeight())/2;
            _loco.setLocation(pt);
//            log.debug("Display Loco \""+trainName+"\" ("+_status+") at ("+pt.x+", "+pt.y+")");
        }
    }

    protected String setStatus(int state) {
    	String status;
        if (state==Sensor.ACTIVE) {
            status = "OccupiedTrack";
        } else if (state==Sensor.INACTIVE) {
            status = "ClearTrack";
        } else if (state==Sensor.UNKNOWN) {
            status = "DontUseTrack";
        } else {
            status = "ErrorTrack";
        }
        return status;
    }
       
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTrackPaths.class.getName());
 }
