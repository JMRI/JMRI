package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.JButton;
import jmri.DccThrottle;

import jmri.jmrit.catalog.NamedIcon;

public class StopAllButton extends JButton {
	private static final ResourceBundle throttleBundle = ThrottleBundle.bundle();

	public StopAllButton() {
		//   	stop.setText(throttleBundle.getString("ThrottleToolBarStopAll"));
		setIcon(new NamedIcon("resources/icons/throttles/estop.png","resources/icons/throttles/estop.png"));
		setToolTipText(throttleBundle.getString("ThrottleToolBarStopAllToolTip"));
		setVerticalTextPosition(JButton.BOTTOM);
		setHorizontalTextPosition(JButton.CENTER);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Enumeration<ThrottleFrame> tpi = jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesListPanel().getEnumeration() ;
				while ( tpi.hasMoreElements() ) {
                                    DccThrottle th = tpi.nextElement().getAddressPanel().getThrottle();
                                    if ( th!=null)
					th.setSpeedSetting(-1);
                                }
			}
		});		
	}
}
