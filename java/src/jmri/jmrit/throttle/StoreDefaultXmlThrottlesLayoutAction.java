package jmri.jmrit.throttle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

/**
 * Save throttles to XML
 *
 *  @author		Lionel Jeanson      Copyright 2009
 *  @version $Revision$
 */
public class StoreDefaultXmlThrottlesLayoutAction extends AbstractAction {

	/**
	 * Constructor
	 * @param s Name for the action.
	 */
	public StoreDefaultXmlThrottlesLayoutAction(String s) {
		super(s);
		// disable this ourselves if there is no throttle Manager
		if (jmri.InstanceManager.throttleManagerInstance() == null) {
			setEnabled(false);
		}
	}

	/**
	 * The action is performed. Let the user choose the file to save to.
	 * Write XML for each ThrottleFrame.
	 * @param e The event causing the action.
	 */
	public void actionPerformed(ActionEvent e) {
		StoreXmlThrottlesLayoutAction sxta = new StoreXmlThrottlesLayoutAction();
		sxta.saveThrottlesLayout(new File(ThrottleFrame.getDefaultThrottleFilename()));
	}

	// initialize logging
	static Logger log = LoggerFactory.getLogger(StoreXmlThrottlesLayoutAction.class.getName());

}
