package jmri.jmris.srcp;

import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TimeStampedOutputTest {

    @Test
    public void testCTor() {
        TimeStampedOutput t = new TimeStampedOutput(new ByteArrayOutputStream());
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testWrite() throws IOException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try(TimeStampedOutput t = new TimeStampedOutput(baos)) {
                t.write("hello world".getBytes());
                assertThat(baos.toString()).isNotEmpty().isEqualTo("12345678.910 hello world");
            }
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        Timebase timebase = Mockito.mock(Timebase.class);
        InstanceManager.setDefault(Timebase.class,timebase);
        Date d = new Date(12345678910L );
        Mockito.when(timebase.getTime()).thenReturn(d);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TimeStampedOutputTest.class);

}
