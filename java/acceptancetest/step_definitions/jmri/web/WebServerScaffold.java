package jmri.web;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.Scenario;

import jmri.web.server.WebServer;

/**
 * Cucumber helper to handle starting and stoping the web server
 * during web tests.
 *
 * @author  Paul Bender Copyright (C) 2017
 */
public class WebServerScaffold {
   private WebServer server = null;

   @Before(value="@webtest")
   public void startServer() throws Exception {
      server = new WebServer(); // a webserver using default preferences.
      server.start();
   }

   @After(value="@webtest")
   public void stopServer() throws Exception {
      server.stop();
   }
}
