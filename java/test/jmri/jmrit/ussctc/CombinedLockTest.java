package jmri.jmrit.ussctc;

import java.util.*;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for CombinedLock classes in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class CombinedLockTest {

    @Test
    public void testEmpty() {
        ArrayList<Lock> list = new ArrayList<>();
        
        CombinedLock lock = new CombinedLock(list);
        
        Assert.assertTrue(lock.isLockClear());
    }

    @Test
    public void testOnePass() {
        ArrayList<Lock> list = new ArrayList<>();
        list.add(new Lock() {
            @Override
            public boolean isLockClear() { return true; }
        });

        CombinedLock lock = new CombinedLock(list);
        
        Assert.assertTrue(lock.isLockClear());
    }

    @Test
    public void testOneFail() {
        ArrayList<Lock> list = new ArrayList<>();
        list.add(new Lock() {
            @Override
            public boolean isLockClear() { return false; }
        });

        CombinedLock lock = new CombinedLock(list);
        
        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testSecondFail() {
        ArrayList<Lock> list = new ArrayList<>();
        list.add(new Lock() {
            @Override
            public boolean isLockClear() { return true; }
        });
        list.add(new Lock() {
            @Override
            public boolean isLockClear() { return false; }
        });
 
        CombinedLock lock = new CombinedLock(list);
        
        Assert.assertTrue( ! lock.isLockClear());
    }

        
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
