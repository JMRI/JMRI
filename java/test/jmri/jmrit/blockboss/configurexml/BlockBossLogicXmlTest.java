package jmri.jmrit.blockboss.configurexml;

import jmri.util.JUnitUtil;
import jmri.jmrit.blockboss.*;
import jmri.implementation.*;
import jmri.*;

import java.util.ArrayList;
import java.util.Enumeration;

import org.junit.jupiter.api.AfterEach;

import org.jdom2.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BlockBossLogicXmlTest.java
 *
 * Test for the BlockBossLogicXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class BlockBossLogicXmlTest {

    @Test
    public void testCtor(){
      assertThat(new BlockBossLogicXml()).withFailMessage("BlockBossLogicXml constructor").isNotNull();
    }

    int count() {
        int ret = 0;

        for (BlockBossLogic b : InstanceManager.getDefault(BlockBossLogicProvider.class).provideAll()) {
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
        InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1");

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

        assertThat(count()).withFailMessage("zero before").isEqualTo(0);

        BlockBossLogicXml bb = new BlockBossLogicXml();
        bb.load(el, null);

        assertThat(count()).withFailMessage("one after").isEqualTo(1);
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
        InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1");

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

        assertThat(count()).withFailMessage("zero before").isEqualTo(0);

        BlockBossLogicXml bb = new BlockBossLogicXml();
        bb.load(el, null);

        assertThat(count()).withFailMessage("zero after").isEqualTo(0);

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
        InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1");

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

        assertThat(count()).withFailMessage("zero before").isEqualTo(0);

        BlockBossLogicXml bb = new BlockBossLogicXml();
        bb.load(el, null);

        assertThat(count()).withFailMessage("zero after").isEqualTo(0);

        jmri.util.JUnitAppender.assertErrorMessage("Ignoring a <signalelement> element with no signal attribute value");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();

        // clear the BlockBossLogic static list
        ArrayList<SignalHead> heads = new ArrayList<>();

        for (BlockBossLogic b : InstanceManager.getDefault(BlockBossLogicProvider.class).provideAll()) {
            heads.add(b.getDrivenSignalNamedBean().getBean());
        }
        for (SignalHead head : heads) {  // avoids ConcurrentModificationException
            BlockBossLogic.getStoppedObject(head);
        }
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearBlockBossLogic();
        JUnitUtil.tearDown();
    }

}

