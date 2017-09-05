package jmri.jmrix.roco.z21;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.RailComManager;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Z21ReporterManager implements the Reporter Manager interface
 * for Roco Z21 systems.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21ReporterManager extends jmri.managers.AbstractReporterManager {

    private Z21SystemConnectionMemo _memo = null;

    /**  
     * Create a new Z21ReporterManager
     * @param memo an instance of Z21SystemConnectionMemo this manager 
     *             is associated with.
     */
@SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
            justification = "False positive from findbugs.  The Value may not be null if there is another railcom manager installed")
    public Z21ReporterManager(Z21SystemConnectionMemo memo){
        _memo = memo;
        if(InstanceManager.getDefault(RailComManager.class)==null){
              // there is no RailComManager, so create a new one
              InstanceManager.setDefault(RailComManager.class,
                                     new jmri.managers.DefaultRailComManager());
        }
    }

    @Override
    public String getSystemPrefix(){
        return _memo.getSystemPrefix();
    }

    @Override
    public Reporter createNewReporter(String systemName, String userName){
        if(!systemName.matches(getSystemPrefix() + typeLetter() + "[" + 1 + "]")) {
            log.warn("Invalid Reporter name: " + systemName + " - only one reporter supported ");
            throw new IllegalArgumentException("Invalid Reporter name: " + systemName + " - only one reporter supported ");
        }
        // make sure we are going to get railcom data from the command station
        // set the broadcast flags so we get messages we may want to hear
        _memo.getRocoZ21CommandStation().setRailComMessagesFlag(true);

        // and forward the flags to the command station.
        _memo.getTrafficController().sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(
                           _memo.getRocoZ21CommandStation().getZ21BroadcastFlags()),null);

        // then create and register the reporter
        Reporter r = new Z21Reporter(systemName,userName,_memo);
        register(r);
        return r;
    }

    @Override
    public void dispose(){
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(Z21ReporterManager.class);

}
