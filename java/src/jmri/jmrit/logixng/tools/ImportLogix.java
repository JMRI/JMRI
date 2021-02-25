package jmri.jmrit.logixng.tools;

import jmri.*;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;

/**
 * Imports Logixs to LogixNG
 * 
 * @author Daniel Bergqvist 2019
 */
public class ImportLogix {

    private final Logix _logix;
    private final LogixNG _logixNG;
    private final boolean _dryRun;
    
    public ImportLogix(Logix logix) {
        this(logix, false);
    }
    
    public ImportLogix(Logix logix, boolean allowSystemImport) {
        this(logix, allowSystemImport, false);
    }
    
    /**
     * Create instance of ImportConditional
     * @param logix             the parent Logix of the conditional to import
     * @param allowSystemImport true if system logixs is allowed to be imported,
     *                          false otherwise
     * @param dryRun            true if import without creating any new beans,
     *                          false if to create new beans
     */
    public ImportLogix(Logix logix, boolean allowSystemImport, boolean dryRun) {
        
//        System.out.format("RTX: %s%n", jmri.jmrit.beantable.LRouteTableAction.getLogixInitializer());
        
        _dryRun = dryRun;
        
        if ("SYS".equals(logix.getSystemName())) {
            if (allowSystemImport) {
                log.warn("Warning. Trying to import SYS from Logix to LogixNG: ", logix.getSystemName());
            } else {
                throw new IllegalArgumentException("Cannot import Logix SYS to LogixNG");
            }
        }
        if (logix.getSystemName().startsWith(
                InstanceManager.getDefault(LogixManager.class).getSystemNamePrefix() + ":RTX")) {
            if (allowSystemImport) {
                log.warn("Warning. Trying to import RTX from Logix to LogixNG: ", logix.getSystemName());
            } else {
                throw new IllegalArgumentException("Cannot import Logix RTX to LogixNG");
            }
        }
        
        _logix = logix;
        _logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                .createLogixNG("Logix: "+_logix.getDisplayName());
        
        log.debug("Import Logix {} to LogixNG {}", _logix.getSystemName(), _logixNG.getSystemName());
    }
    
    public void doImport() throws JmriException {
        for (int i=0; i < _logix.getNumConditionals(); i++) {
            Conditional c = _logix.getConditional(_logix.getConditionalByNumberOrder(i));
            log.warn("Import Conditional {} to ConditionalNG {}", c.getSystemName(), _logixNG.getSystemName());
            ImportConditional ic = new ImportConditional(
                    _logix, c, _logixNG,
                    InstanceManager.getDefault(ConditionalNG_Manager.class).getAutoSystemName(),
                    _dryRun);
            
            if (!_dryRun) _logixNG.addConditionalNG(ic.getConditionalNG());
            
            try {
                ic.doImport();
            } catch (SocketAlreadyConnectedException ex) {
//                ex.printStackTrace();
                log.warn("Import Conditional {} to ConditionalNG {}", c.getSystemName(), _logixNG.getSystemName(), ex);
            }
            
            if (!_dryRun) ic.getConditionalNG().setEnabled(true);
        }
    }
    
    public LogixNG getLogixNG() {
        return _logixNG;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportLogix.class);

}
