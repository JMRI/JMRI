package jmri.jmrix.tams;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the TamsSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class TamsSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {
     
    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo(new TamsInterfaceScaffold());
       memo.configureManagers();
       scm = memo;
    }

    @Override
    @After
    public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
       JUnitUtil.tearDown();
    }

}
