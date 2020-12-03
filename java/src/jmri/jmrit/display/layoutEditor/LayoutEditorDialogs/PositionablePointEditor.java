package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import javax.annotation.Nonnull;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTrackView;

/**
 * MVC Editor component for PositionablePoint objects.
 *
 * Note there might not be anything for this class to do;
 * LayoutTrackEditors has a comment saying that PositionablePoint
 * doesn't have an editor.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class PositionablePointEditor extends LayoutTrackEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public PositionablePointEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    /**
     * Edit a PositionablePoint
     */
    @Override
    public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrackView) {
        log.error("no editor installed for PositionablePoint", new Exception("traceback"));
    }
    

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionablePointEditor.class);
}
