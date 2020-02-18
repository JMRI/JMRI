package jmri.jmris;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base set of tests for decendents of the jmri.jmris.AbstractSignalMastServer class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractSignalMastServerTest {

    protected AbstractSignalMastServer sms = null;

    @Test
    public void testCtor() {
        assertThat(sms).isNotNull();
    }

    @BeforeEach
    public void setUp(){
       jmri.util.JUnitUtil.setUp();
       sms = new AbstractSignalMastServer(){
          @Override
          public void sendStatus(String signalMast, String Status) throws IOException {
          }
          @Override
          public void sendErrorStatus(String route) throws IOException {
          }
          @Override
          public void parseStatus(String statusString) throws IOException {
          }

       };
    }

    @AfterEach
    public void tearDown(){
       jmri.util.JUnitUtil.tearDown();
    }

}
