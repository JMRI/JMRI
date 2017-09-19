package jmri.jmrit.display;

import javax.swing.JFrame;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.WindowOperator;


/**
 * Jemmy Operator for Editor panels swing interfaces.
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
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

        // that pops dialog, find and press Delete
        JDialogOperator d = new JDialogOperator(Bundle.getMessage("ReminderTitle"));

        // Find the button that deletes the panel
        JButtonOperator bo = new JButtonOperator(d,Bundle.getMessage("ButtonDeletePanel"));

        // Click button to delete panel and close window
        bo.push();

        // that pops dialog, find and press Yes - Delete
        d = new JDialogOperator(Bundle.getMessage("DeleteVerifyTitle"));

        // Find the button that deletes the panel
        bo = new JButtonOperator(d,Bundle.getMessage("ButtonYesDelete"));

        // Click button to delete panel and close window
        bo.push();
    }
}
