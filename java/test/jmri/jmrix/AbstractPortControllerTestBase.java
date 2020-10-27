package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.SystemConnectionMemo;

import org.mockito.Mockito;

/**
 * @author Bob Jacobsen Copyright (C) 2015
 */
public abstract class AbstractPortControllerTestBase {

    @Test
    public void testisDirtyNotNPE() {
        apc.isDirty();
    }

    @Test
    public void testDefaultMethod() {
        Assert.assertFalse("default false", apc.isOptionTypeText("foo"));
        jmri.util.JUnitAppender.assertErrorMessage("did not find option foo for type");
    }

    // from here down is testing infrastructure
    protected AbstractPortController apc;

    @BeforeEach
    public void setUp() {
        SystemConnectionMemo memo = Mockito.mock(SystemConnectionMemo.class);
        apc = new AbstractPortControllerScaffold(memo);
    }

    @AfterEach
    public void tearDown(){
       apc = null;
    }

    public static class AbstractPortControllerScaffold extends AbstractPortController {

        public AbstractPortControllerScaffold(SystemConnectionMemo memo) {
            super(memo);
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
