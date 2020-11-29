package jmri.jmris.srcp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPServerTest {

    @Test
    public void testCtor() {
        JmriSRCPServer a = new JmriSRCPServer();
        assertThat(a).isNotNull();
    }

    @Test
    public void testCtorwithParameter() {
        JmriSRCPServer a = new JmriSRCPServer(2048);
        assertThat(a).isNotNull();
    }

}
