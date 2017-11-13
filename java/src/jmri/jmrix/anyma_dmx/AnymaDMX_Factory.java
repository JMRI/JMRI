package jmri.jmrix.anyma_dmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnymaDMX_Factory {

    private static AnymaDMX_SystemConnectionMemo memo;

    public AnymaDMX_Factory(AnymaDMX_SystemConnectionMemo memo) {
        log.info("*	AnymaDMX_Factory constructor called");
        //AnymaDMX_Factory.controller = controller;
        this.memo = memo;
    }

    private final static Logger log = 
            LoggerFactory.getLogger(AnymaDMX_Factory.class);
}
