package jmri.web;

import cucumber.api.Scenario;
import cucumber.api.java8.En;
import org.junit.Assert;

import jmri.web.server.WebServer;

/**
 * Cucumber helper to handle starting and stoping the web server
 * during web tests.
 *
 * @author  Paul Bender Copyright (C) 2017
 */
public class WebServerScaffold implements En {

   private WebServer server = null;
   String[] tags = {"@webtest"};

   public WebServerScaffold() {

      Before(tags,()->{
          jmri.util.JUnitUtil.setUp();
          jmri.util.JUnitUtil.resetProfileManager();
          jmri.util.JUnitUtil.initShutDownManager();
          jmri.util.JUnitUtil.initDebugPowerManager();
          server = new WebServer(); // a webserver using default preferences.
          server.start();
          jmri.util.JUnitUtil.waitFor(()->{ return server.isStarted(); },"Server Failed to Start in time");
          jmri.util.JUnitOperationsUtil.resetOperationsManager();
      });

      After(tags, ()->{
         try {
             try {
                 server.stop();
                 jmri.util.JUnitUtil.waitFor(()->{ return server.isStopped(); },"Server failed to Stop in time");
             } catch(java.lang.NullPointerException npe) {
                //npe.printStackTrace();
                //Assert.fail("Null Pointer Exception while stopping web server:" + npe);
             } catch(Exception ex) {
                 // Exception is thrown by the stop call above.
                 // if an Exception occurs here, we may want to raise a flag,
                 ex.printStackTrace();
                 Assert.fail("Exception occured during web server shutdown:" + ex);
             }
             jmri.util.JUnitUtil.tearDown();
         } catch(java.lang.NullPointerException npe2) {
             //npe2.printStackTrace();
             //Assert.fail("Null Pointer Exception occured during teardown:" + npe2);
         }
      });
   }
}
