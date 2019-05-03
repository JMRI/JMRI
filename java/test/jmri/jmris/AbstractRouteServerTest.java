package jmri.jmris;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * tests for decendents of the jmri.jmris.AbstractRouteServer class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractRouteServerTest {

    protected AbstractRouteServer rs = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(rs);
    }

    @Before
    public void setUp(){
       jmri.util.JUnitUtil.setUp();
       rs = new AbstractRouteServer(){
          @Override
          public void sendStatus(String route, int Status) throws IOException {
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
