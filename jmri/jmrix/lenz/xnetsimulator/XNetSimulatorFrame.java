// XNetSimulatorFrame.java

package jmri.jmrix.lenz.xnetsimulator;

import javax.swing.JOptionPane;
import jmri.util.JmriJFrame;

/**
 * Frame to control and connect XPressNet via XNetSimulator interface and comm port
 * @author			Paul Bender    Copyright (C) 2009
 * @version			$Revision: 1.2 $
 */
public class XNetSimulatorFrame extends jmri.util.JmriJFrame{

        private XNetSimulatorAdapter adapter = null;


	public XNetSimulatorFrame() {
		super();
                adapter = new XNetSimulatorAdapter();
                adapter.configure();
        }

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetSimulatorFrame.class.getName());

}
