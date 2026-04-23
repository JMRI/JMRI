package jmri.jmrit.throttle.UIImplementation;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import jmri.jmrit.throttle.interfaces.ThrottleSubControllerUI;

public class ThrottleJInternalFrameSubControl extends JInternalFrame implements ThrottleSubControllerUI {

    public ThrottleJInternalFrameSubControl(String title, JPanel content, boolean visible) {
        super(title, true, true, true, true);           
        setContentPane(content);
        setVisible(visible);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
}
