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
        t.setName("Hide Dialog Close Thread");
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
        t2.setName("Delete Dialog Close Thread");
        t2.start();

    }

}
