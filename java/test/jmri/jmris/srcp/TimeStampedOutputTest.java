package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TimeStampedOutputTest {

    @Test
    public void testCTor() {
        TimeStampedOutput t = new TimeStampedOutput(new ByteArrayOutputStream());
        assertThat(t).isNotNull().withFailMessage("exists");
    }

    @Test
    public void testWrite() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TimeStampedOutput t = new TimeStampedOutput(baos);
        t.write("hello world".getBytes());
        assertThat(baos.toString()).isNotEmpty().endsWith("hello world");

    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TimeStampedOutputTest.class);

}
