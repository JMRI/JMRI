package apps;

import cucumber.api.java8.En;
import cucumber.api.PendingException;
import org.junit.Assert;
import java.io.File;
import org.apache.commons.io.FileUtils;
import java.nio.file.Files;
import java.lang.reflect.Method;
import jmri.managers.DefaultShutDownManager;
import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

/**
 * Cucumber step definitions for Application Acceptance tests.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class ApplicationTestAcceptanceSteps implements En {
     
   String[] tags = {"@apptest"};
   File tempFolder;
   
   public ApplicationTestAcceptanceSteps(jmri.InstanceManager instance) {


   Before(tags,() -> {
      JUnitUtil.setUp();
      JUnitUtil.resetApplication();
   });

   Given("^I am using profile (.*)$", (String profile) -> {
       try {
            // create a custom profile
            tempFolder =Files.createTempDirectory("AppTest").toFile();
            FileUtils.copyDirectory(new File(profile), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );
       } catch(java.io.IOException ioe) {
         Assert.fail("Unable to create temporary profile");
       }
    });

    When("^starting application (.*) with (.*)", (String application,String frametitle ) -> {
        try {
            // use redirection to start the application.
            Class appclass = Class.forName(application);
            Method method = appclass.getMethod("main",String[].class);
            String[] params = new String[]{frametitle};
            method.invoke(null,(Object)params);
        } catch(java.lang.ClassNotFoundException cnf){
            Assert.fail("Class " + application + " not found");
        } catch(java.lang.NoSuchMethodException | 
                java.lang.IllegalAccessException ex){
            Assert.fail("Error calling main method");
        }
    });

    Then("^a frame with title (.*) is displayed$", (String frameTitle) -> {
       JUnitUtil.waitFor(()->{return JmriJFrame.getFrame(frameTitle) != null;}, "window up");
    });

    Then("^(.*) is printed to the console$", (String infoLine) -> {
       JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith(infoLine) != null;}, "first Info line seen");
    });

    After(tags,() -> {
        try{
           // gracefully shutdown, but don't exit
           ((DefaultShutDownManager)instance.getDefault(jmri.ShutDownManager.class)).shutdown(0, false);
        } finally { 
           // wait for threads, etc
           jmri.util.JUnitUtil.releaseThread(this, 5000);
        }
        FileUtils.deleteDirectory(tempFolder);
        JUnitUtil.tearDown();
    });

   }
}
