package jmri.jmrit.display;

import org.apache.log4j.Logger;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.ArrayList;
import jmri.Sensor;
import jmri.jmrit.display.controlPanelEditor.shape.PositionableRoundRect;
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
	private LocoLable _loco = null;

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
    	_loco = new LocoLable(ed);
        FontMetrics metrics = ed.getFontMetrics(ed.getFont());
    	int width = metrics.stringWidth(trainName);
    	int height = metrics.getHeight();
    	_loco.setLineWidth(1);
    	_loco.setLineColor(Color.RED);
    	_loco.setAlpha(150);
    	_loco.setFillColor(Color.WHITE);
    	_loco.setText(trainName);
    	_loco.setWidth(width+height/2);
    	_loco.setHeight(height);
    	_loco.setCornerRadius(height/2);
    	_loco.makeShape();
    	_loco.setDisplayLevel(Editor.MARKERS);
        _loco.updateSize();
        pt.x = pt.x + (size.width - _loco.maxWidth())/2;
        pt.y = pt.y + (size.height - _loco.maxHeight())/2;
        _loco.setLocation(pt);
        ed.putItem(_loco);
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
    
    static class LocoLable extends PositionableRoundRect {
    	
    	String _text;
    	
        public LocoLable(Editor editor) {
        	super(editor);
        }

        public LocoLable(Editor editor, Shape shape) {
           	super(editor, shape);
        }
        
        public void setText(String text) {
        	_text = text;        	
        }
        
        public void paint(Graphics g) {
        	super.paint(g);
            Graphics2D g2d = (Graphics2D)g;
            if (_transform!=null ) {
            	g2d.transform(_transform);
            }        
            g2d.setFont(getFont().deriveFont(Font.BOLD));
        	int textWidth = getFontMetrics(getFont()).stringWidth(_text);
        	int textHeight = getFontMetrics(getFont()).getHeight();
    		int hOffset = Math.max((maxWidth()-textWidth)/2, 0);
    		int vOffset = Math.max((maxHeight()-textHeight)/2, 0) + getFontMetrics(getFont()).getAscent();
            g2d.setColor(Color.BLACK);
            g2d.drawString(_text, hOffset, vOffset);
        }
    }
      
    static Logger log = Logger.getLogger(IndicatorTrackPaths.class.getName());
 }
