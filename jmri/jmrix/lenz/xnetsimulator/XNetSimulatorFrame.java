// XNetSimulatorFrame.java

package jmri.jmrix.lenz.xnetsimulator;


/**
 * Frame to control and connect XPressNet via XNetSimulator interface and comm port
 * @author			Paul Bender    Copyright (C) 2009
 * @version			$Revision: 1.5 $
 * @Deprecated
 */
public class XNetSimulatorFrame extends jmri.util.JmriJFrame{

        private XNetSimulatorAdapter adapter = null;


	public XNetSimulatorFrame() {
		super();
                adapter = new XNetSimulatorAdapter();
                adapter.configure();
                //hide this frame
                setVisible(false);
        }

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetSimulatorFrame.class.getName());

}
