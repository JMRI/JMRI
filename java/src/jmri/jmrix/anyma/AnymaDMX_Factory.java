package jmri.jmrix.anyma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to Swing components for the AnymaDMX subsystem.
 *
 * @author George Warner Copyright (c) 2017
 * @since 4.9.6
 */
public class AnymaDMX_Factory {

    private static AnymaDMX_SystemConnectionMemo memo;

    public AnymaDMX_Factory(AnymaDMX_SystemConnectionMemo memo) {
        log.info("*	constructor({})", memo);
        this.memo = memo;
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_Factory.class);
}
