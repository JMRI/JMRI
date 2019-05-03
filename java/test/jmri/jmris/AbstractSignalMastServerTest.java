package jmri.jmris;

import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Base set of tests for decendents of the jmri.jmris.AbstractSignalMastServer class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractSignalMastServerTest {

    protected AbstractSignalMastServer sms = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(sms);
    }

    @Before
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

    @After
    public void tearDown(){
       jmri.util.JUnitUtil.tearDown();
    }

}
