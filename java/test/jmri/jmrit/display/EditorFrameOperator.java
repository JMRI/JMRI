package jmri.jmrit.display;

import javax.swing.JFrame;
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

    private static final String hideThreadName = "EditorFrameOperator: Hide Dialog Close Thread";
    private static final String deleteThreadName = "EditorFrameOperator: Delete Dialog Close Thread";
    private static final int joinDelayMillis = 100;

    public void closeFrameWithConfirmations(){
        // if OK to here, close window
        this.requestClose();

        dismissClosingDialogs();
    }

    public void deleteViaFileMenuWithConfirmations(){
        JMenuOperator jmo = new JMenuOperator(this,Bundle.getMessage("MenuFile"));
        jmo.pushMenuNoBlock(Bundle.getMessage("MenuFile") +"/"+ Bundle.getMessage("DeletePanel"), "/");
        dismissClosingDialogs();

    }

    private void dismissClosingDialogs(){
        // the reminder dialog doesn't appear every time we close, so put
        // pressing the button in that dialog into a thread by itself.  If
        // the dialog appears, it will get clicked, but it's not an error
        // if it doesn't appear.
        Thread t = new Thread( () -> {
            try {
                JDialogOperator d = new JDialogOperator(Bundle.getMessage("PanelHideTitle"));
                // Find the button that deletes the panel
                JButtonOperator bo = new JButtonOperator(d,Bundle.getMessage("ButtonHide"));

                // Click button to delete panel and close window
                bo.push();
            } catch (Exception e) {
                // exceptions in this thread are not considered an error.
            }
        });
        t.setName(hideThreadName);
        t.start();

        Thread t2 = new Thread( () -> {
            try {
                JDialogOperator d = new JDialogOperator(Bundle.getMessage("DeleteVerifyTitle"));
                // Find the button that deletes the panel
                JButtonOperator bo = new JButtonOperator(d,Bundle.getMessage("ButtonYesDelete"));

                // Click button to delete panel and close window
                bo.push();
            } catch (Exception e) {
                // exceptions in this thread are not considered an error.
            }
        });
        t2.setName(deleteThreadName);
        t2.start();

    }

    /**
     * Call this at the end of tests that have invoked dismissClosingDialogs
     * to clean up any threads that have been left hanging.
     */
    public static void clearEditorFrameOperatorThreads() {
        ThreadGroup main = Thread.currentThread().getThreadGroup();
        while (main.getParent() != null ) {main = main.getParent(); }
        Thread[] list = new Thread[main.activeCount()+2];  // space on end
        int max = main.enumerate(list);

        for (int i = 0; i<max; i++) {
            Thread t = list[i];
            if (t.getState() == Thread.State.TERMINATED) { // going away, just not cleaned up yet
                continue;
            }
            String name = t.getName();
            if (name.equals(hideThreadName) || name.equals(deleteThreadName)) {
                try {
                    // give it a chance to end
                    t.join(joinDelayMillis);
                    // then interrupt it to end it
                    t.interrupt();
                } catch (InterruptedException e) {
                    // not an error, not recorded
                }
            }
        }
    }
}
