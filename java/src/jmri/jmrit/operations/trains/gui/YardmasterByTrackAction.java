package jmri.jmrit.operations.trains.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.gui.YardmasterByTrackFrame;

/**
 * Swing action open the yardmaster by track frame.
 *
 * @author Daniel Boudreau Copyright (C) 2026
 * 
 */
public class YardmasterByTrackAction extends AbstractAction {
    
    public YardmasterByTrackAction(TrainConductorPanel tcp) {
        super(Bundle.getMessage("TitleYardmasterByTrack"));
        _tcp = tcp;
    }

    TrainConductorPanel _tcp;
    YardmasterByTrackFrame _ytf;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a frame
        if (_ytf != null) {
            _ytf.dispose();
        }
        _ytf = new YardmasterByTrackFrame(_tcp.getCurrentLocation());
    }
}


