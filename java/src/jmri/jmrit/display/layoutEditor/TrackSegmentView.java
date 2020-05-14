package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;
import javax.annotation.*;
import javax.swing.JPopupMenu;
import jmri.*;
import jmri.util.*;

/**
 * MVC View component for the TrackSegment class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class TrackSegmentView extends LayoutTrackView {

    /**
     * constructor method.
     * @param track the track segment to view.
     */
    public TrackSegmentView(@Nonnull TrackSegment track) {
        super(track);
        this.trackSegment = track;
    }

    final private TrackSegment trackSegment;
    
    protected boolean isDashed() {
        return trackSegment.isDashed();
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackSegmentView.class);
}
