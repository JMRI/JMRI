package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Comparator;
import java.util.ResourceBundle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.NamedBean;

/**
 * @author Bob Jacobsen Copyright (C) 2015
 */
public abstract class AbstractPortControllerTestBase {

    @Test
    public void testisDirtyNotNPE() {
        apc.isDirty();
    }

    // from here down is testing infrastructure
    protected AbstractPortController apc;

    @Before
    public void setUp() {
        apc = new AbstractPortControllerScaffold();
    }

    @After
    public void tearDown(){
       apc = null;
    }

    public static class AbstractPortControllerScaffold extends AbstractPortController {

        public AbstractPortControllerScaffold() {
            super(new SystemConnectionMemo("", "") {

                @Override
                protected ResourceBundle getActionModelResourceBundle() {
                    return null;
                }

                @Override
                public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
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
