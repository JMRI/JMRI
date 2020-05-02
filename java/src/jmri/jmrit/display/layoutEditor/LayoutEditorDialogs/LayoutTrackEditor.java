package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import javax.annotation.*;
import javax.swing.*;
import javax.swing.border.*;

import jmri.*;
import jmri.jmrit.display.layoutEditor.*;
import jmri.swing.NamedBeanComboBox;
import jmri.util.*;

/**
 * MVC Editor component for LayoutTrack hierarchy objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutTrackEditor {

    /**
     * constructor method
     */
    public LayoutTrackEditor(@Nonnull LayoutEditor layoutEditor) {
         this.layoutEditor = layoutEditor;
    }

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


    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditor.class);
}
