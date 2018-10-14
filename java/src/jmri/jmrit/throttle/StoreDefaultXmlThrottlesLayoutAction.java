package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;

/**
 * Save throttles to XML
 *
 * @author Lionel Jeanson Copyright 2009
 */
public class StoreDefaultXmlThrottlesLayoutAction extends AbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public StoreDefaultXmlThrottlesLayoutAction(String s) {
        super(s);
        // disable this ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    /**
     * The action is performed. Let the user choose the file to save to. Write
     * XML for each ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        StoreXmlThrottlesLayoutAction sxta = new StoreXmlThrottlesLayoutAction();
        sxta.saveThrottlesLayout(new File(ThrottleFrame.getDefaultThrottleFilename()));
    }

}
