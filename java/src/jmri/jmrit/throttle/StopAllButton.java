package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.JButton;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;

public class StopAllButton extends JButton {

    public StopAllButton() {
        //    stop.setText(Bundle.getMessage("ThrottleToolBarStopAll"));
        setIcon(new NamedIcon("resources/icons/throttles/estop.png", "resources/icons/throttles/estop.png"));
        setToolTipText(Bundle.getMessage("ThrottleToolBarStopAllToolTip"));
        setVerticalTextPosition(JButton.BOTTOM);
        setHorizontalTextPosition(JButton.CENTER);
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Iterator<ThrottleFrame> tpi = InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesListPanel().getTableModel().iterator();
                while (tpi.hasNext()) {
                    DccThrottle th = tpi.next().getAddressPanel().getThrottle();
                    if (th != null) {
                        th.setSpeedSetting(-1);
                    }
                }
            }
        });
    }
}
