package jmri.implementation.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

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
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SwingShutDownTaskTest.class
})
public class SwingShutDownTaskDemo {

    // Main entry point
    static public void main(String[] args) {
        System.setProperty("modalDialogStopsTest", "true");
        org.junit.runner.JUnitCore.main(SwingShutDownTaskDemo.class.getName());
    }

}
