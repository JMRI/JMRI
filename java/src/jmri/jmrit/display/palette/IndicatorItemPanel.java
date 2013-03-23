package jmri.jmrit.display.palette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import java.awt.event.ActionListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
//import java.awt.dnd.*;
import java.io.IOException;

import javax.swing.*;

//import jmri.Sensor;
import jmri.util.JmriJFrame;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.catalog.DragJLabel;

/**
*  ItemPanel for for Indicating track blocks 
*/
public class IndicatorItemPanel extends FamilyItemPanel {

    private DetectionPanel  _detectPanel;

    /**
    * Constructor for plain icons and backgrounds
    */
    public IndicatorItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, family, editor);
    }

    /**
    * Init for creation
    * insert panels for detection and train id
    */
    public void init() {
    	if (!_initialized) {
            super.init();
            _detectPanel= new DetectionPanel(this);
            add(_detectPanel, 0);
    	}
    }

    /**
    * Init for update of existing track block
    * _bottom3Panel has "Update Panel" button put into _bottom1Panel
    */
    public void init(ActionListener doneAction, HashMap<String, NamedIcon> iconMap) {
        super.init(doneAction, iconMap);
        _detectPanel= new DetectionPanel(this);
        add(_detectPanel, 0);
    }

    /**
    * Init for conversion of plain track to indicator track
    */
    public void init(ActionListener doneAction) {
        super.init(doneAction, null);
    }

    public void dispose() {
        if (_detectPanel!=null) {
            _detectPanel.dispose();
        }
    }

    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        super.makeDndIconPanel(iconMap, "ClearTrack");
    }

    /*************** pseudo inheritance to DetectionPanel *******************/

    public boolean getShowTrainName() {
        return _detectPanel.getShowTrainName();
    }

    public void setShowTrainName(boolean show) {
        _detectPanel.setShowTrainName(show);
    }
/*
    public String getErrSensor() {
        return _detectPanel.getErrSensor();
    }

    public void setErrSensor(String name) {
        _detectPanel.setErrSensor(name);
    }
*/
    public String getOccSensor() {
        return _detectPanel.getOccSensor();
    }

    public String getOccBlock() {
        return _detectPanel.getOccBlock();
    }

    public void setOccDetector(String name) {
        _detectPanel.setOccDetector(name);
    }

    public ArrayList<String> getPaths() {
        return _detectPanel.getPaths();
    }

    public void setPaths(ArrayList<String> paths) {
        _detectPanel.setPaths(paths);
    }

    /*******************************************************/

    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map) {
        return new IndicatorDragJLabel(flavor, map);
    }

    protected class IndicatorDragJLabel extends DragJLabel {
        HashMap <String, NamedIcon> iconMap;

        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP2") // icon map is within package 
        public IndicatorDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map) {
            super(flavor);
            iconMap = map;
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return super.isDataFlavorSupported(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (iconMap==null) {
                log.error("IconDragJLabel.getTransferData: iconMap is null!");
                return null;
            }
            if (log.isDebugEnabled()) log.debug("IndicatorDragJLabel.getTransferData");
            IndicatorTrackIcon t = new IndicatorTrackIcon(_editor);

            t.setOccBlock(_detectPanel.getOccBlock());
            t.setOccSensor(_detectPanel.getOccSensor());
            t.setShowTrain(_detectPanel.getShowTrainName());
            t.setFamily(_family);

            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                t.setIcon(entry.getKey(), new NamedIcon(entry.getValue()));
            }
            t.setLevel(Editor.TURNOUTS);
            return t;
        }
    }

    static Logger log = LoggerFactory.getLogger(IndicatorItemPanel.class.getName());
}

