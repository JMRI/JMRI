package jmri.web;

import cucumber.api.Scenario;
import cucumber.api.java8.En;

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
          jmri.util.JUnitUtil.initShutDownManager();
          jmri.util.JUnitUtil.initDebugPowerManager();
          server = new WebServer(); // a webserver using default preferences.
          server.start();
          jmri.util.JUnitUtil.waitFor(()->{ return server.isStarted(); });
      });

      After(tags, ()->{
         try {
             server.stop();
             jmri.util.JUnitUtil.waitFor(()->{ return server.isStopped(); });
             jmri.util.JUnitUtil.tearDown();
         } catch(Exception ex) {
             // if an exception occurs here, we may want to raise a flag,
         }
      });
   }
}
