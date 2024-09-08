package jmri.jmrit.display.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
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
 * @author Pete Cressman Copyright (c) 2010, 2020
 */
public class IndicatorItemPanel extends FamilyItemPanel {

    private DetectionPanel _detectPanel;

    /*
     * Constructor for track icons.
     */
    public IndicatorItemPanel(DisplayFrame parentFrame, String type, String family) {
        super(parentFrame, type, family);
    }

    /**
     * Init for creation of insert panels for detection and train id.
     */
    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _detectPanel = new DetectionPanel(this);
            add(_detectPanel, 1);
        }
        hideIcons();
    }

    @Override
    protected void hideIcons() {
        if (_detectPanel != null) {
            _detectPanel.setVisible(true);
            _detectPanel.invalidate();
        }
        super.hideIcons();
    }

    @Override
    protected void showIcons() {
        if (_detectPanel != null) {
            _detectPanel.setVisible(false);
            _detectPanel.invalidate();
        }
        super.showIcons();
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
     * CircuitBuilder init for conversion of plain track to indicator track.
     */
    @Override
    public void init(JPanel bottomPanel) {
        super.init(bottomPanel);
        add(_iconFamilyPanel, 0);
    }

    @Override
    public void dispose() {
        if (_detectPanel != null) {
            _detectPanel.dispose();
        }
    }

    @Override
    protected String getDisplayKey() {
        return "ClearTrack";
    }

    /**
     * ************* pseudo inheritance to DetectionPanel ******************
     * @return getShowTrainName status from detection panel.
     */
    public boolean getShowTrainName() {
        return _detectPanel.getShowTrainName();
    }

    public void setShowTrainName(boolean show) {
        _detectPanel.setShowTrainName(show);
    }

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
     * {@inheritDoc} 
     */
    @Override
    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
        return new IndicatorDragJLabel(flavor, map, icon);
    }

    protected class IndicatorDragJLabel extends DragJLabel {

        private final HashMap<String, NamedIcon> iconMap;

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
            log.debug("IndicatorDragJLabel.getTransferData");
            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                IndicatorTrackIcon t = new IndicatorTrackIcon(_frame.getEditor());

                t.setOccBlock(_detectPanel.getOccBlock());
                t.setOccSensor(_detectPanel.getOccSensor());
                t.setShowTrain(_detectPanel.getShowTrainName());
                t.setFamily(_family);

                for (Entry<String, NamedIcon> entry : iconMap.entrySet()) {
                    t.setIcon(entry.getKey(), new NamedIcon(entry.getValue()));
                }
                t.setLevel(Editor.TURNOUTS);
                return t;                
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                return _itemType + " icons";
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorItemPanel.class);

}
