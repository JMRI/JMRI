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
          server = new WebServer(); // a webserver using default preferences.
          server.start();
      });

      After(tags, ()->{
         try {
             server.stop();
         } catch(Exception ex) {
             // if an exception occurs here, we may want to raise a flag,
         }
         jmri.util.JUnitUtil.tearDown();
      });
   }
}
