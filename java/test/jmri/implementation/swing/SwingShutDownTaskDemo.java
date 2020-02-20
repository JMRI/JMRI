package jmri.implementation.swing;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Demo runner for the SwingShutDownTask class.
 * <p>
 * Invoked from higher-level JUnit test as a test class, this does not display the modal dialogs that
 * stop execution until clicked/closed. When invoked via its own main() start
 * point, this does show the modal dialogs.
 * <p>
 * Careful - tests are loaded via a separate class loader!
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
@RunWith(JUnitPlatform.class)
@SelectClasses(jmri.implementation.swing.SwingShutDownTaskDemo.class)
public class SwingShutDownTaskDemo {

    // Main entry point
    static public void main(String[] args) {
        System.setProperty("modalDialogStopsTest", "true");
        org.junit.runner.JUnitCore.main(SwingShutDownTaskDemo.class.getName());
    }

}
