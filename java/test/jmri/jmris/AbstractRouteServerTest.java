package jmri.jmris;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * tests for decendents of the jmri.jmris.AbstractRouteServer class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractRouteServerTest {

    protected AbstractRouteServer rs = null;

    @Test
    public void testCtor() {
        assertThat(rs).isNotNull();
    }

    @BeforeEach
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

    @AfterEach
    public void tearDown(){
       jmri.util.JUnitUtil.tearDown();
    }

}
