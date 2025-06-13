package jmri.jmrit.display;

import javax.swing.JFrame;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.util.JUnitUtil;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuOperator;


/**
 * Jemmy Operator for Editor panels swing interfaces.
 *
 * @author Bob Jacobsen Copyright 2009, 2010
 * @author  Paul Bender Copyright 2017
 */
public class EditorFrameOperator extends JFrameOperator {

    public EditorFrameOperator(String title){
       super(title);
    }

    public EditorFrameOperator(JFrame frame){
       super(frame);
    }

    private static final String HIDE_THREAD_NAME = "EditorFrameOperator: Hide Dialog Close Thread";
    private static final String DELETE_THREAD_NAME = "EditorFrameOperator: Delete Dialog Close Thread";

    public void closeFrameWithConfirmations(){
        // if OK to here, close window

        dismissClosingDialogs();
        this.requestClose();
        JUnitUtil.dispose( getWindow());
        this.waitClosed();
    }

    public void deleteViaFileMenuWithConfirmations(){
        JMenuOperator jmo = new JMenuOperator(this,Bundle.getMessage("MenuFile"));
        dismissClosingDialogs();
        jmo.pushMenu(Bundle.getMessage("MenuFile") +"/"+ Bundle.getMessage("DeletePanel"), "/");

    }

    private void dismissClosingDialogs(){
        // the reminder dialog doesn't appear every time we close, so put
        // pressing the button in that dialog into a thread by itself.  If
        // the dialog appears, it will get clicked, but it's not an error
        // if it doesn't appear.
        Thread t = new Thread( () -> {
            triggerPanelHideOperators();
        });
        t.setName(HIDE_THREAD_NAME);
        t.start();

        Thread t2 = new Thread( () -> {
            triggerDeleteYesOperators();
        });
        t2.setName(DELETE_THREAD_NAME);
        t2.start();

    }

    @SuppressFBWarnings( value = {"DCN_NULLPOINTER_EXCEPTION", "DE_MIGHT_IGNORE"},
        justification = "ok for JDialog not to be present")
    private void triggerPanelHideOperators() {
        try {
            JDialogOperator d = new JDialogOperator(Bundle.getMessage("PanelHideTitle"));
            // Find the button that deletes the panel
            JButtonOperator bo = new JButtonOperator(d,Bundle.getMessage("ButtonHide"));

            // Click button to delete panel and close window
            bo.push();
        } catch (NullPointerException e) {
            // exceptions in this thread are not considered an error.
        }
    }

    @SuppressFBWarnings( value = {"DCN_NULLPOINTER_EXCEPTION", "DE_MIGHT_IGNORE"},
        justification = "ok for JDialog not to be present")
    private void triggerDeleteYesOperators() {
        try {
            JDialogOperator d = new JDialogOperator(Bundle.getMessage("DeleteVerifyTitle"));
            // Find the button that deletes the panel
            JButtonOperator bo = new JButtonOperator(d,Bundle.getMessage("ButtonYesDelete"));

            // Click button to delete panel and close window
            bo.push();
        } catch (NullPointerException e) {
            // exceptions in this thread are not considered an error.
        }
    }

    /**
     * Call this at the end of tests that have invoked dismissClosingDialogs
     * to clean up any threads that have been left hanging.
     */
    public static void clearEditorFrameOperatorThreads() {
        jmri.util.JUnitUtil.removeMatchingThreads(HIDE_THREAD_NAME);
        jmri.util.JUnitUtil.removeMatchingThreads(DELETE_THREAD_NAME);
    }

}
