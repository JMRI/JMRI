package jmri.jmrix.direct;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the DirectSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DirectSystemConnectionMemoTest extends SystemConnectionMemoTestBase<DirectSystemConnectionMemo> {

    // Ctor etc are tested in MemoTestBase

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       scm = new DirectSystemConnectionMemo();
    }

    @Override
    @AfterEach
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
