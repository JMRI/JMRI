package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.display.layoutEditor.*;

/**
 * MVC root Editor component for LayoutTrack hierarchy objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 *
 */
abstract public class LayoutTrackEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LayoutTrackEditor(@Nonnull LayoutEditor layoutEditor) {
         this.layoutEditor = layoutEditor;
    }

    // temporary method to get a correct-type *Editor or subclass.
    // Eventually, this will go away once *Editor's are created
    // in type-specific *View classes
    // TODO: should be made not necessary
    @Nonnull
    static public LayoutTrackEditor makeTrackEditor(@Nonnull LayoutTrack layoutTrack, @Nonnull LayoutEditor layoutEditor) {

        if (layoutTrack instanceof LayoutTurnout) {

            if (layoutTrack instanceof LayoutRHTurnout) { return new LayoutRHTurnoutEditor(layoutEditor); }
            if (layoutTrack instanceof LayoutLHTurnout) { return new LayoutLHTurnoutEditor(layoutEditor); }
            if (layoutTrack instanceof LayoutWye) { return new LayoutWyeEditor(layoutEditor); }

            if (layoutTrack instanceof LayoutXOver) {
                if (layoutTrack instanceof LayoutRHXOver) { return new LayoutRHXOverEditor(layoutEditor); }
                if (layoutTrack instanceof LayoutLHXOver) { return new LayoutLHXOverEditor(layoutEditor); }
                if (layoutTrack instanceof LayoutDoubleXOver) { return new LayoutDoubleXOverEditor(layoutEditor); }

                return new LayoutXOverEditor(layoutEditor);
            }

            if (layoutTrack instanceof LayoutSlip) {
                if (layoutTrack instanceof LayoutSingleSlip) { return new LayoutSingleSlipEditor(layoutEditor); }
                if (layoutTrack instanceof LayoutDoubleSlip) { return new LayoutDoubleSlipEditor(layoutEditor); }

                return new LayoutSlipEditor(layoutEditor);
            }

            return new LayoutTurnoutEditor(layoutEditor);
        }
        if (layoutTrack instanceof TrackSegment) { return new TrackSegmentEditor(layoutEditor); }
        if (layoutTrack instanceof PositionablePoint) { return new PositionablePointEditor(layoutEditor); }
        if (layoutTrack instanceof LevelXing) { return new LevelXingEditor(layoutEditor); }
        if (layoutTrack instanceof LayoutTurntable) { return new LayoutTurntableEditor(layoutEditor); }

        log.error("makeTrackEditor did not match type of {}", layoutTrack, new Exception("traceback"));
        return new LayoutTrackEditor(layoutEditor){
            @Override
            public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrackView) {
                log.error("Not a valid LayoutTrackEditor implementation", new Exception("traceback"));
            }
        };
    }

    /**
     * Launch the editor for a particular LayoutTrack-tree object.
     * @param layoutTrackView the layout track view to edit.
     */
    abstract public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrackView);

    final protected LayoutEditor layoutEditor;

    List<String> sensorList = new ArrayList<>();

    protected void addDoneCancelButtons(JPanel target, JRootPane rp, ActionListener doneCallback, ActionListener cancelCallback) {
        // Done
        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        target.add(doneButton);  // NOI18N
        doneButton.addActionListener(doneCallback);
        doneButton.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));  // NOI18N

        // Cancel
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel")); // NOI18N
        target.add(cancelButton);
        cancelButton.addActionListener(cancelCallback);
        cancelButton.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));  // NOI18N

        rp.setDefaultButton(doneButton);
        // bind ESC to close window
        rp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close"); // NOI18N
    }


    /**
     * Display a message describing the reason for the block selection combo box
     * being disabled. An option is provided to hide the message. Note: The
     * PanelMenu class is being used to satisfy the showInfoMessage requirement
     * for a default manager type class.
     *
     * @since 4.11.2
     */
    @InvokeOnGuiThread
    void showSensorMessage() {
        if (sensorList.isEmpty()) {
            return;
        }
        StringBuilder msg = new StringBuilder(Bundle.getMessage("BlockSensorLine1"));  // NOI18N
        msg.append(Bundle.getMessage("BlockSensorLine2"));  // NOI18N
        String chkDup = "";
        sensorList.sort(null);
        for (String sName : sensorList) {
            if (!sName.equals(chkDup)) {
                msg.append("<br>&nbsp;&nbsp;&nbsp; " + sName);  // NOI18N
            }
            chkDup = sName;
        }
        msg.append("<br>&nbsp;</html>");  // NOI18N
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                showInfoMessage(
                        Bundle.getMessage("BlockSensorTitle"), // NOI18N
                        msg.toString(),
                        "jmri.jmrit.display.PanelMenu", // NOI18N
                        "BlockSensorMessage");  // NOI18N
    }


    /**
     * Create a list of NX sensors that refer to the current layout block. This
     * is used to disable block selection in the edit dialog. The list is built
     * by {@link jmri.jmrit.entryexit.EntryExitPairs#layoutBlockSensors}.
     *
     * @since 4.11.2
     * @param loBlk The current layout block.
     * @return true if sensors are affected.
     */
    boolean hasNxSensorPairs(LayoutBlock loBlk) {
        if (loBlk == null) {
            return false;
        }
        List<String> blockSensors = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class)
                .layoutBlockSensors(loBlk);
        if (blockSensors.isEmpty()) {
            return false;
        }
        sensorList.addAll(blockSensors);
        return true;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditor.class);
}
