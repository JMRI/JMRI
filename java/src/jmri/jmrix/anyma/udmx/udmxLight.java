package jmri.jmrix.anyma.udmx;

import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.jmrix.AbstractLight to handle udmx lights
 *
 * @author George Warner Copyright (C) 2017
 */
public class udmxLight extends AbstractLight {

    public udmxLight(String prefix, int pNumber) {  // a human-readable light number must be specified!
        super(prefix, "" + pNumber);
        log.info("*udmxLight('{}', {})", prefix, pNumber);
    }
    private final static Logger log = LoggerFactory.getLogger(udmxLight.class);

}
