package jmri.jmrit.display.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrackIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel for for Indicating track blocks.
 */
public class IndicatorItemPanel extends FamilyItemPanel {

    private DetectionPanel _detectPanel;

    /**
     * Constructor for plain icons and backgrounds.
     */
    public IndicatorItemPanel(DisplayFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, family, editor);
    }

    /**
     * Init for creation of insert panels for detection and train id.
     */
    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _detectPanel = new DetectionPanel(this);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createVerticalGlue());
            panel.add(_detectPanel);
            add(panel, 0);
        }
    }

    /**
     * Init for update of existing track block.
     * _bottom3Panel has "Update Panel" button put onto _bottom1Panel.
     */
    @Override
    public void init(ActionListener doneAction, HashMap<String, NamedIcon> iconMap) {
        super.init(doneAction, iconMap);
        _detectPanel = new DetectionPanel(this);
        add(_detectPanel, 0);
    }

    /**
     * Init for conversion of plain track to indicator track.
     */
    @Override
    public void init(ActionListener doneAction) {
        super.init(doneAction);
    }

    @Override
    public void dispose() {
        if (_detectPanel != null) {
            _detectPanel.dispose();
        }
    }

    @Override
    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        super.makeDndIconPanel(iconMap, "ClearTrack");
    }

    /**
     * ************* pseudo inheritance to DetectionPanel ******************
     */
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

    /**
     * ****************************************************
     */
    @Override
    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
        return new IndicatorDragJLabel(flavor, map, icon);
    }

    protected class IndicatorDragJLabel extends DragJLabel {

        HashMap<String, NamedIcon> iconMap;

        public IndicatorDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
            super(flavor, icon);
            iconMap = new HashMap<>(map);
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return super.isDataFlavorSupported(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (iconMap == null) {
                log.error("IconDragJLabel.getTransferData: iconMap is null!");
                return null;
            }
            if (log.isDebugEnabled()) {
                log.debug("IndicatorDragJLabel.getTransferData");
            }
            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
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
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" icons");
                return  sb.toString();
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorItemPanel.class);

}
