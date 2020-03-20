package jmri.jmris.srcp;

import cucumber.api.java.ca.Cal;
import jmri.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TimeStampedOutputTest {

    InstanceManagerDelegate instanceManagerDelegate;

    @Test
    public void testCTor() {
        TimeStampedOutput t = new TimeStampedOutput(new ByteArrayOutputStream());
        assertThat(t).isNotNull().withFailMessage("exists");
    }

    @Test
    public void testWrite() throws IOException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try(TimeStampedOutput t = new TimeStampedOutput(baos,instanceManagerDelegate)) {
                t.write("hello world".getBytes());
                assertThat(baos.toString()).isNotEmpty().isEqualTo("12345678.910 hello world");
            }
        }

    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        instanceManagerDelegate = Mockito.mock(InstanceManagerDelegate.class);
        Timebase timebase = Mockito.mock(Timebase.class);
        Mockito.when(instanceManagerDelegate.getDefault(Timebase.class)).thenReturn(timebase);
        Date d = new Date(12345678910L );
        Mockito.when(timebase.getTime()).thenReturn(d);
    }

    @AfterEach
    public void tearDown() {
        instanceManagerDelegate = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(TimeStampedOutputTest.class);

}
