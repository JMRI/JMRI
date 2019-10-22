package apps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import cucumber.api.java8.En;
import java.io.File;
import org.apache.commons.io.FileUtils;
import java.nio.file.Files;
import java.lang.reflect.Method;
import jmri.ShutDownManager;
import jmri.util.JmriJFrame;
import jmri.util.MockShutDownManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Cucumber step definitions for Application Acceptance tests.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class ApplicationTestAcceptanceSteps implements En {

    String[] tags = {"@apptest"};
    File tempFolder;

    public ApplicationTestAcceptanceSteps() {

        Before(tags, () -> {
            JUnitUtil.setUp();
            JUnitUtil.clearShutDownManager();
            JUnitUtil.resetApplication();
            JUnitUtil.resetAppsBase();
        });

        Given("^I am using profile (.*)$", (String profile) -> {
            boolean exceptionThrown = false;
            try {
                // create a custom profile
                tempFolder = Files.createTempDirectory("AppTest").toFile();
                File profileDir = new File(tempFolder, "Name");
                FileUtils.copyDirectory(new File(profile), profileDir);
                System.setProperty("jmri.prefsdir", tempFolder.getAbsolutePath());
                System.setProperty("org.jmri.profile", profileDir.getAbsolutePath());
            } catch (java.io.IOException ioe) {
                exceptionThrown = true;
            }
            assertThat(!exceptionThrown);
        });

        When("^starting application (.*) with (.*)", (String application, String frametitle) -> {
            try {
                // use redirection to start the application.
                Class<?> appclass = Class.forName(application);
                Method method = appclass.getMethod("main", String[].class);
                String[] params = new String[]{frametitle};
                method.invoke(null, (Object) params);
            } catch (java.lang.ClassNotFoundException cnf) {
                fail("Class {} not found",application);
            } catch (java.lang.NoSuchMethodException
                    | java.lang.IllegalAccessException ex) {
                fail("Error calling main method",ex);
            }
        });

        Then("^a frame with title (.*) is displayed$", (String frameTitle) -> {
            JUnitUtil.waitFor(() -> {
                return JmriJFrame.getFrame(frameTitle) != null;
            }, frameTitle + " up");
        });

        Then("^(.*) is printed to the console$", (String infoLine) -> {
            JUnitUtil.waitFor(() -> {
                return JUnitAppender.checkForMessageStartingWith(infoLine) != null;
            }, "first Info line seen");
        });

        After(tags, () -> {
            dismissClosingDialogs(); // this method starts a new thread
            try {
                // gracefully shutdown, but don't exit
                ShutDownManager sdm = jmri.InstanceManager.getDefault(ShutDownManager.class);
                if (sdm instanceof MockShutDownManager) {
                    // ShutDownManagers other than MockShutDownManager really shutdown
                    sdm.shutdown();
                }
            } finally {
                // wait for threads, etc
                jmri.util.JUnitUtil.releaseThread(this, 5000);
            }
            FileUtils.deleteDirectory(tempFolder);
            System.clearProperty("org.jmri.profile");
            JUnitUtil.clearShutDownManager();
            JUnitUtil.resetAppsBase();
            JUnitUtil.resetFileUtilSupport();
            JUnitUtil.resetWindows(false,false);
            JUnitUtil.tearDown();
        });

    }

    private void dismissClosingDialogs() {
        // the Unsaved Changes dialog doesn't appear every time we close,
        // so put pressing No button in that dialog into a thread by itself.
        // If the dialog appears, the button will be clicked, but it's not
        // an error if the dialog doesn't appear.
        Thread t = new Thread(() -> {
            try {
                JDialogOperator d = new JDialogOperator(Bundle.getMessage("UnsavedChangesTitle"));
                // Find the button that deletes the panel
                JButtonOperator bo = new JButtonOperator(d, Bundle.getMessage("ButtonNo"));

                // Click button to delete panel and close window
                bo.push();
            } catch (Exception e) {
                // exceptions in this thread are not considered an error.
            }
        });
        t.setName("Unsaved Changes Dialog Close Thread");
        t.start();
    }

}
