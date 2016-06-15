package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ResourceBundle;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class AbstractPortControllerTest extends TestCase {

    public void testisDirtyNotNPE() {
        apc.isDirty();
    }

    // from here down is testing infrastructure
    AbstractPortController apc;

    public AbstractPortControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AbstractPortControllerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractPortControllerTest.class);
        return suite;
    }

    @Override
    public void setUp() {
        apc = new AbstractPortControllerScaffold();
    }

    public static class AbstractPortControllerScaffold extends AbstractPortController {

        public AbstractPortControllerScaffold() {
            super(new SystemConnectionMemo("", "") {

                @Override
                protected ResourceBundle getActionModelResourceBundle() {
                    return null;
                }
            });
        }

        @Override
        public DataInputStream getInputStream() {
            return null;
        }

        @Override
        public DataOutputStream getOutputStream() {
            return null;
        }

        @Override
        public String getCurrentPortName() {
            return "";
        }

        @Override
        public void dispose() {
            super.dispose();
        }

        @Override
        public void recover() {
        }

        @Override
        public void connect() {
        }

        @Override
        public void configure() {
        }
    }
}
