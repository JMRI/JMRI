package jmri.jmrit.blockboss.configurexml;

import jmri.util.JUnitUtil;
import jmri.jmrit.blockboss.*;
import jmri.implementation.*;
import jmri.*;

import java.util.Enumeration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.jdom2.*;

/**
 * BlockBossLogicXmlTest.java
 *
 * Description: tests for the BlockBossLogicXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class BlockBossLogicXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("BlockBossLogicXml constructor",new BlockBossLogicXml());
    }
    
    int count() {
        int ret = 0;
        Enumeration<BlockBossLogic> en = BlockBossLogic.entries();
        
        while (en.hasMoreElements()) {
            en.nextElement();
            ret++;
        }
        return ret;
    }

    @Test
    public void testOneElement(){
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH1") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        Sensor is1 = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1");
        
        Element el = new Element("signalelements")
                .setAttribute("class", "jmri.jmrit.blockboss.configurexml.BlockBossLogicXml")
                .addContent(
                        new Element("signalelement")
                        .setAttribute("signal", "IH1")
                        .setAttribute("mode", "2")
                        .addContent(
                            new Element("sensorname")
                                .addContent("IS1")
                        )
                );

        Assert.assertEquals("zero before", count(), 0);
        
        BlockBossLogicXml bb = new BlockBossLogicXml();
        bb.load(el, null);

        Assert.assertEquals("one after", count(), 1);
    }

    @Test
    public void testBadSignalName(){
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH2") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        Sensor is1 = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1");
        
        Element el = new Element("signalelements")
                .setAttribute("class", "jmri.jmrit.blockboss.configurexml.BlockBossLogicXml")
                .addContent(
                        new Element("signalelement")
                        .setAttribute("signal", "IH1")
                        .setAttribute("mode", "2")
                        .addContent(
                            new Element("sensorname")
                                .addContent("IS1")
                        )
                );

        Assert.assertEquals("zero before", count(), 0);
        
        BlockBossLogicXml bb = new BlockBossLogicXml();
        bb.load(el, null);

        Assert.assertEquals("zero after", count(), 0);

        jmri.util.JUnitAppender.assertErrorMessage("SignalHead IH1 not defined, <signalelement> element referring to it is ignored");
    }

    @Test
    public void testMissingSignalAttribute(){
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH1") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        Sensor is1 = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1");
        
        Element el = new Element("signalelements")
                .setAttribute("class", "jmri.jmrit.blockboss.configurexml.BlockBossLogicXml")
                .addContent(
                        new Element("signalelement")
                        .setAttribute("mode", "2")
                        .addContent(
                            new Element("sensorname")
                                .addContent("IS1")
                        )
                );

        Assert.assertEquals("zero before", count(), 0);
        
        BlockBossLogicXml bb = new BlockBossLogicXml();
        bb.load(el, null);

        Assert.assertEquals("zero after", count(), 0);
        
        jmri.util.JUnitAppender.assertErrorMessage("Ignoring a <signalelement> element with no signal attribute value");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

