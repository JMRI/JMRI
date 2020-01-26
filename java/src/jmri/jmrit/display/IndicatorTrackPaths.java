package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import jmri.Sensor;
import jmri.jmrit.display.controlPanelEditor.shape.LocoLabel;
import jmri.jmrit.logix.OBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class replacing common methods formerly implementing the
 * IndicatorTrack interface.
 *
 * @author Pete Cressman Copyright (c) 2012
 */
public class IndicatorTrackPaths {

    protected ArrayList<String> _paths;      // list of paths that this icon displays
    private boolean _showTrain;         // this track icon should display _loco when occupied
    private LocoLabel _loco = null;

    protected IndicatorTrackPaths() {
    }

    protected IndicatorTrackPaths deepClone() {
        IndicatorTrackPaths p = new IndicatorTrackPaths();
        if (_paths != null) {
            p._paths = new ArrayList<>();
            for (int i = 0; i < _paths.size(); i++) {
                p._paths.add(_paths.get(i));
            }
        }
        p._showTrain = _showTrain;
        return p;
    }

    protected ArrayList<String> getPaths() {
        return _paths;
    }

    protected void setPaths(ArrayList<String> paths) {
        _paths = paths;
    }

    protected void addPath(String path) {
        if (_paths == null) {
            _paths = new ArrayList<>();
        }
        if (path != null && path.length() > 0) {
            path = path.trim();
            if (!_paths.contains(path)) {
                _paths.add(path);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("addPath \"" + path + "\" #paths= " + _paths.size());
        }
    }

    protected void removePath(String path) {
        if (_paths != null) {
            if (path != null && path.length() > 0) {
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

    protected String getStatus(OBlock block, int state) {
        String pathName = block.getAllocatedPathName();
        String status;
        removeLocoIcon();
        if ((state & OBlock.TRACK_ERROR) != 0) {
            status = "ErrorTrack";
        } else if ((state & OBlock.OUT_OF_SERVICE) != 0) {
            status = "DontUseTrack";
        } else if ((state & OBlock.ALLOCATED) != 0) {
            if (_paths != null && _paths.contains(pathName)) {
                if ((state & OBlock.RUNNING) != 0) {
                    status = "PositionTrack";   //occupied by train on a warrant
                } else if ((state & OBlock.OCCUPIED) != 0) {
                    status = "OccupiedTrack";   // occupied by rouge train
                } else {
                    status = "AllocatedTrack";
                }
            } else {
                status = "ClearTrack";     // icon not on path
            }
        } else if ((state & OBlock.OCCUPIED) != 0) {
            status = "OccupiedTrack";
//        } else if ((state & Sensor.UNKNOWN)!=0) {
//            status = "DontUseTrack";
        } else {
            status = "ClearTrack";
        }
        return status;
    }

    public void removeLocoIcon() {
        if (_loco != null) {
            _loco.remove();
            _loco = null;
        }
    }

    protected void setLocoIcon(OBlock block, Point pt, Dimension size, Editor ed) {
        if (!_showTrain) {
            removeLocoIcon();
            return;
        }
        String trainName = (String) block.getValue();
        if (trainName == null) {
            removeLocoIcon();
            return;
        }
        if (_loco != null || pt == null) {
            return;
        }
        trainName = trainName.trim();
        _loco = new LocoLabel(ed);
        Font font = block.getMarkerFont();
        if (font == null) {
            font = ed.getFont();
        }
        int width = ed.getFontMetrics(font).stringWidth(trainName);
        int height = ed.getFontMetrics(ed.getFont()).getHeight();   // limit height to locoIcon height
        _loco.setLineWidth(1);
        _loco.setLineColor(Color.BLACK);
        _loco.setFillColor(block.getMarkerBackground());
        _loco.setBlock(block);
        _loco.setWidth(width + height / 2);
        _loco.setHeight(height + 2);
        _loco.setCornerRadius(height);
        _loco.setDisplayLevel(Editor.MARKERS);
        _loco.updateSize();
        pt.x = pt.x + (size.width - _loco.maxWidth()) / 2;
        pt.y = pt.y + (size.height - _loco.maxHeight()) / 2;
        _loco.setLocation(pt);
        ed.putItem(_loco);
    }

    /*
     * Return track name for known state of occupancy sensor
     */
    protected String getStatus(int state) {
        String status;
        switch (state) {
            case Sensor.ACTIVE:
                status = "OccupiedTrack";
                break;
            case Sensor.INACTIVE:
                status = "ClearTrack";
                break;
            case Sensor.UNKNOWN:
                status = "DontUseTrack";
                break;
            default:
                status = "ErrorTrack";
                break;
        }
        return status;
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorTrackPaths.class);
}
