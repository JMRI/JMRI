package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;

/**
 * Yardmaster by track frame. Shows work at one location listed by track.
 *
 * @author Dan Boudreau Copyright (C) 2015
 * 
 */
public class YardmasterByTrackFrame extends OperationsFrame {

    public YardmasterByTrackFrame(Location location) {
        super(Bundle.getMessage("TitleYardmasterByTrack"), new YardmasterByTrackPanel(location));
        this.initComponents(location);
    }

    private void initComponents(Location location) {
        super.initComponents();

        if (location != null) {
            setTitle(Bundle.getMessage("TitleYardmasterByTrack") + " (" + location.getName() + ")");
        }

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }
}
