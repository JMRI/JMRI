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

        _dryRun = dryRun;
        LogixNG logixNG = null;

        if (!_dryRun) {
            int counter = 0;
            while ((logixNG == null) && counter < 100) {
                String name = counter > 0 ? " - " + Integer.toString(counter) : "";
                logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                        .createLogixNG("Logix: " + logix.getDisplayName() + name);
                counter++;
            }

            if (logixNG == null) throw new RuntimeException("Cannot create new LogixNG with name: \"Logix: " + logix.getDisplayName()+"\"");

            log.debug("Import Logix {} to LogixNG {}", logix.getSystemName(), logixNG.getSystemName());
        }

        _logix = logix;
        _logixNG = logixNG;
    }

    public void doImport() throws JmriException {
        for (int i=0; i < _logix.getNumConditionals(); i++) {
            Conditional c = _logix.getConditional(_logix.getConditionalByNumberOrder(i));

            if (!_dryRun) {
                log.warn("Import Conditional '{}' to LogixNG '{}'", c.getSystemName(), _logixNG.getSystemName());
            }

            ImportConditional ic = new ImportConditional(
                    _logix, c, _logixNG,
                    InstanceManager.getDefault(ConditionalNG_Manager.class).getAutoSystemName(),
                    _dryRun);

            try {
                ic.doImport();
            } catch (SocketAlreadyConnectedException ex) {
                if (!_dryRun) {
                    log.warn("Exception during import of Conditional {} to ConditionalNG {}",
                            c.getSystemName(), _logixNG.getSystemName(), ex);
                }
            }

            if (!_dryRun) ic.getConditionalNG().setEnabled(true);
        }
    }

    public LogixNG getLogixNG() {
        return _logixNG;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportLogix.class);

}
