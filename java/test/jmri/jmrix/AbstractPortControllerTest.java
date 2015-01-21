// AbstractPortControllerTest.java

package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author      Bob Jacobsen  Copyright (C) 2015
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
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractPortControllerTest.class);
        return suite;
    }

    public void setUp() {
        apc = new AbstractPortController() {
            public  DataInputStream getInputStream() { return null; }
            public  DataOutputStream getOutputStream() { return null; }
            public String getCurrentPortName() { return "";}
            public void dispose() {}
            public void recover() {}
            public SystemConnectionMemo getSystemConnectionMemo() { return null; }
            public void connect() {}
            public void configure() {}
        };
    }
}