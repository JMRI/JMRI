package jmri.jmrix.ecos.utilities;

import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosTrafficController;

public class RemoveObjectFromEcos implements EcosListener {

    public RemoveObjectFromEcos() {
    }

    private String _ecosObject;
    private int ecosretry;

    private EcosTrafficController tc;

    //Need to deal with the fact this method has a contructor name.
    public void removeObjectFromEcos(String ecosObject, EcosTrafficController etc) {
        tc = etc;
        _ecosObject = ecosObject;
        log.debug("Call to delete Object {} from the Ecos", ecosObject);
        tc = etc;
        String message = "request(" + _ecosObject + ", control, view)";
        EcosMessage m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }

    @Override
    public void reply(EcosReply m) {

        String msg = m.toString();
        String[] lines = msg.split("\n");
        if(m.getResultCode()==0){
            if (lines[0].startsWith("<REPLY request(" + _ecosObject + ",")) {
                deleteObject();
            }
        } else if (m.getResultCode()==25){
            /**
             * This section deals with no longer having control over the ecos
             * loco object. we try three times to request control, on the fourth
             * attempt we try a forced control, if that fails we inform the user
             * and reset the counter to zero.
             */
            log.info("We have no control over the ecos object {}Retry Counter = {}", _ecosObject, ecosretry);
            retryControl();
        }
    }

    private void retryControl() {
        if (ecosretry < 3) {
            //It might be worth adding in a sleep/pause of description between retries.
            ecosretry++;

            String message = "request(" + _ecosObject + ", control)";
            EcosMessage ms = new EcosMessage(message);
            tc.sendEcosMessage(ms, this);
            log.error("JMRI has no control over the ecos object {}. Retrying Attempt {}", _ecosObject, ecosretry);
        } //We do not want to do a force control over an object when we are trying to delete it, bad things might happen on the layout if we do this!
        else {
            jmri.util.swing.JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("DeleteFromEcosWarning"),
                    Bundle.getMessage("WarningTitle"), jmri.util.swing.JmriJOptionPane.WARNING_MESSAGE);
            ecosretry = 0;
        }
    }

    private void deleteObject() {
        EcosMessage m;
        String message = "delete(" + _ecosObject + ")";
        m = new EcosMessage(message);
        tc.sendEcosMessage(m, this);
    }

    @Override
    public void message(EcosMessage m) {
        // messages are ignored
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RemoveObjectFromEcos.class);

}
