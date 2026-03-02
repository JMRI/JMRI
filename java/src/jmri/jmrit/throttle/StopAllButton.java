package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;

public class StopAllButton extends JButton {

    public StopAllButton() {
        super();
        initGUI();
    }
        
    private void initGUI() {    
        //    stop.setText(Bundle.getMessage("ThrottleToolBarStopAll"));
        setIcon(new NamedIcon("resources/icons/throttles/estop.png", "resources/icons/throttles/estop.png"));
        setToolTipText(Bundle.getMessage("ThrottleToolBarStopAllToolTip"));
        setVerticalTextPosition(JButton.BOTTOM);
        setHorizontalTextPosition(JButton.CENTER);
        addActionListener((ActionEvent e) -> {
            InstanceManager.getDefault(ThrottleFrameManager.class).emergencyStopAll();
        });
    }
}
