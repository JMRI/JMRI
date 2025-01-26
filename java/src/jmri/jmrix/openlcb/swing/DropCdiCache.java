package jmri.jmrix.openlcb.swing;

import jmri.util.swing.JmriJOptionPane;
import jmri.util.JmriJFrame;

import java.awt.event.WindowEvent;

import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;

/** 
 * Single static method to handle dropping
 * the CDI cache for a node.
 *
 * Finds all open CDI windows and closes them
 *   with a prompt as to whether to cancel or not
 * Then drops the CDI
 */
 
public class DropCdiCache {

    public static void drop(NodeID destNodeID, OlcbInterface iface) {
        // close relevant frames
        boolean first = true;
        var frames = JmriJFrame.getFrameList();
        for (var frame : frames) {
            log.trace("frame: {} type:{}", frame, frame.getClass());
            if (frame instanceof NodeSpecificFrame) {
                if ( ((NodeSpecificFrame)frame).getNodeID().equals(destNodeID)) {
                    // This window references the node and should be closed
                    
                    // Notify the user to handle any prompts before continuing.
                    if (first) {
                        int decision = JmriJOptionPane.showConfirmDialog(null, 
                            Bundle.getMessage("OpenWindowMessage"),
                            "Close CDI Window?",
                            JmriJOptionPane.OK_CANCEL_OPTION
                        );
                        
                        if (decision == JmriJOptionPane.CANCEL_OPTION) return;  // don't clear cache
                        
                        first = false;
                        
                    }
                    
                    // Close the window - force onto the queue before a possible next modal dialog
                    jmri.util.ThreadingUtil.runOnGUI(() -> {
                        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    });
                    
                    // Depending on the state of the window, and how the user handles
                    // a prompt to discard changes or cancel, the window might 
                    // still be open.  If so, don't clear the cache.
                    if (JmriJFrame.getFrameList().contains(frame)) {return;}
                }
            }
        }

        // de-cache CDI information so next window opening will reload
        iface.dropConfigForNode(destNodeID);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DropCdiCache.class);
}
