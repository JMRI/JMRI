package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.annotation.*;
import javax.swing.*;
import javax.swing.border.*;
import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.*;
import jmri.jmrit.display.layoutEditor.LayoutTurntable.RayTrack;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;

/**
 * Editors for all layout track objects (PositionablePoint, TrackSegment,
 * LayoutTurnout, LayoutSlip, LevelXing and LayoutTurntable).
 *
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutTrackEditors {

    // private LayoutEditor layoutEditor = null;

    /**
     * constructor method
     */
    public LayoutTrackEditors(@Nonnull LayoutEditor layoutEditor) {
        // this.layoutEditor = layoutEditor;
        this.trackSegmentEditor = new TrackSegmentEditor(layoutEditor);
        this.layoutTurnoutEditor = new LayoutTurnoutEditor(layoutEditor);
        this.layoutTurntableEditor = new LayoutTurntableEditor(layoutEditor);
        this.layoutSlipEditor = new LayoutSlipEditor(layoutEditor);
        this.levelXingEditor = new LevelXingEditor(layoutEditor);
    }

    final TrackSegmentEditor trackSegmentEditor;
    final LayoutTurnoutEditor layoutTurnoutEditor;
    final LayoutTurntableEditor layoutTurntableEditor;
    final LayoutSlipEditor layoutSlipEditor;
    final LevelXingEditor levelXingEditor;
    
    /*======================*\
    | Edit Layout Track Types|
    \*======================*/
    @InvokeOnGuiThread
    public void editLayoutTrack(@Nonnull LayoutTrack layoutTrack) {
        log.trace("editLayoutTrack invoked on {}, traceback follows", layoutTrack, new Exception("traceback"));
        if (layoutTrack instanceof PositionablePoint) {
            // PositionablePoint's don't have an editor...
        } else if (layoutTrack instanceof TrackSegment) {
            trackSegmentEditor.editTrackSegment((TrackSegment) layoutTrack); // partly converted
        } else // this has to be before LayoutTurnout
        if (layoutTrack instanceof LayoutSlip) {
            layoutSlipEditor.editLayoutSlip((LayoutSlip) layoutTrack);
        } else if (layoutTrack instanceof LayoutTurnout) {
            editLayoutTurnout((LayoutTurnout) layoutTrack);
        } else if (layoutTrack instanceof LevelXing) {
            levelXingEditor.editLevelXing((LevelXing) layoutTrack);
        } else if (layoutTrack instanceof LayoutTurntable) {
            layoutTurntableEditor.editLayoutTurntable((LayoutTurntable) layoutTrack);
        } else {
            log.error("editLayoutTrack unknown LayoutTrack subclass:{}", layoutTrack.getClass().getName());  // NOI18N
        }
    }

    // temporary pass-through for call from TrackSegment itself, which
    // should eventually be architected away via MVC
    public void editLayoutTurnout(LayoutTurnout layoutTurnout) {
        layoutTurnoutEditor.editLayoutTurnout(layoutTurnout);
    }
    
    // temporary pass-through for call from TrackSegment itself, which
    // should eventually be architected away via MVC
    public void editTrackSegment(TrackSegment layoutTrack) {
        trackSegmentEditor.editTrackSegment(layoutTrack);
    }
    
    // temporary pass-through for call from TrackSegment itself, which
    // should eventually be architected away via MVC
    public void editLayoutTurntable(LayoutTurntable layoutTurntable) {
        layoutTurntableEditor.editLayoutTurntable(layoutTurntable);
    }
    
    // temporary pass-through for call from TrackSegment itself, which
    // should eventually be architected away via MVC
    public void editLayoutSlip(LayoutSlip layoutSlip) {
        layoutSlipEditor.editLayoutSlip(layoutSlip);
    }
    
    // temporary pass-through for call from TrackSegment itself, which
    // should eventually be architected away via MVC
    public void editLevelXing(LevelXing levelXing) {
        levelXingEditor.editLevelXing(levelXing);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditors.class);
}
