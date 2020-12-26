package jmri.jmrix.tams;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the TamsSystemConnectionMemo class
 * <p>
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TamsSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase<TamsSystemConnectionMemo> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new TamsSystemConnectionMemo(new TamsInterfaceScaffold());
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
