package jmri.jmrix.maple.assignment;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ListFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ListFrameTest extends jmri.util.JmriJFrameTestBase {

    private MapleSystemConnectionMemo _memo = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        _memo = new MapleSystemConnectionMemo("K", "Maple");
        if(!GraphicsEnvironment.isHeadless()){
           frame = new ListFrame(_memo);
	}
    }

    @After
    @Override
    public void tearDown() {
	    _memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	super.tearDown();    
    }
}
