package jmri.jmris;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base set of tests for decendents of the jmri.jmris.AbstractLightServer class
 *
 * @author Paul Bender Copyright (C) 2017
 */
abstract public class AbstractLightServerTestBase {

    protected AbstractLightServer ls = null;

    @Test
    public void testCtor() {
        assertThat(ls).isNotNull();
    }

    @Test
    public void testInitLight(){
        ls.initLight("IL1");
    }

    abstract public void setUp(); // must setup ls as a light server instance;

}
