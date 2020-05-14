package jmri.jmris.srcp.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPVisitor} class.
 *
 * @author Paul Bender Copyright (C) 2012,2017
 * 
 */
public class SRCPVisitorTest {

    @Test
    public void testCTor() {
        // test the constructor.
        SRCPVisitor v = new SRCPVisitor();
        assertThat(v).isNotNull();
    }

    @Test
    public void testGetServer() {
        // test that an inbound "GET 0 SERVER" returns the
        // expected response.
        String code = "GET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SRCPVisitor v = new SRCPVisitor();
        Throwable thrown = catchThrowable( () -> {
                    SimpleNode e = p.command();
                    e.jjtAccept(v, null);
                });
        assertThat("100 INFO 0 SERVER RUNNING").isEqualTo(v.getOutputString());
        assertThat(thrown).isNull();
    }

    @Test
    public void testResetServer() {
        // test that an inbound "RESET 0 SERVER" returns the
        // expected response.
        String code = "RESET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SRCPVisitor v = new SRCPVisitor();
        Throwable thrown = catchThrowable( () -> {
            SimpleNode e = p.command();
            e.jjtAccept(v, null);
        });
        assertThat(thrown).isNull();
        assertThat("413 ERROR temporarily prohibited").isEqualTo(v.getOutputString());
    }

    @Test
    public void testTERMServer() {
        // test that an inbound "TERM 0 SERVER" returns the
        // expected response.
        String code = "TERM 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SRCPVisitor v = new SRCPVisitor();
        Throwable thrown = catchThrowable( () -> {
            SimpleNode e = p.command();
            e.jjtAccept(v, null);
        });
        assertThat("200 OK").isEqualTo(v.getOutputString());
        assertThat(thrown).isNull();
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
