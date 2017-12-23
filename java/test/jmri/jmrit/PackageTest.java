package jmri.jmrit;

import jmri.util.JUnitUtil;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrit.AbstractIdentifyTest.class,
        BundleTest.class,
        DccLocoAddressSelectorTest.class,
        MemoryContentsTest.class,
        SoundTest.class,
        XmlFileTest.class,
        jmri.jmrit.automat.PackageTest.class,
        jmri.jmrit.beantable.PackageTest.class,
        jmri.jmrit.blockboss.PackageTest.class,
        jmri.jmrit.catalog.PackageTest.class,
        jmri.jmrit.conditional.PackageTest.class,
        jmri.jmrit.decoderdefn.PackageTest.class,
        jmri.jmrit.dispatcher.PackageTest.class,
        jmri.jmrit.display.PackageTest.class,
        jmri.jmrit.entryexit.PackageTest.class,
        jmri.jmrit.jython.PackageTest.class,
        jmri.jmrit.log.PackageTest.class,
        jmri.jmrit.logix.PackageTest.class,
        jmri.jmrit.operations.PackageTest.class,
        jmri.jmrit.progsupport.PackageTest.class,
        jmri.jmrit.mastbuilder.PackageTest.class,
        jmri.jmrit.mailreport.PackageTest.class,
        jmri.jmrit.powerpanel.PackageTest.class,
        jmri.jmrit.roster.PackageTest.class,
        jmri.jmrit.sendpacket.PackageTest.class,
        jmri.jmrit.sensorgroup.PackageTest.class,
        jmri.jmrit.simpleclock.PackageTest.class,
        jmri.jmrit.symbolicprog.PackageTest.class,
        jmri.jmrit.tracker.PackageTest.class,
        jmri.jmrit.ussctc.PackageTest.class,
        jmri.jmrit.consisttool.PackageTest.class,
        jmri.jmrit.withrottle.PackageTest.class,
        jmri.jmrit.ampmeter.PackageTest.class,
        jmri.jmrit.lcdclock.PackageTest.class,
        jmri.jmrit.throttle.PackageTest.class,
        jmri.jmrit.audio.PackageTest.class,
        jmri.jmrit.turnoutoperations.PackageTest.class,
        jmri.jmrit.dualdecoder.PackageTest.class,
        jmri.jmrit.nixieclock.PackageTest.class,
        jmri.jmrit.simpleprog.PackageTest.class,
        jmri.jmrit.signalling.PackageTest.class,
        jmri.jmrit.picker.PackageTest.class,
        jmri.jmrit.speedometer.PackageTest.class,
        jmri.jmrit.analogclock.PackageTest.class,
        jmri.jmrit.revhistory.PackageTest.class,
        jmri.jmrit.sound.PackageTest.class,
        jmri.jmrit.vsdecoder.PackageTest.class,
        jmri.jmrit.simplelightctrl.PackageTest.class,
        jmri.jmrit.simpleturnoutctrl.PackageTest.class,
        MemoryFrameActionTest.class,
        ToolsMenuTest.class,
        XmlFileLocationActionTest.class,
        XmlFileValidateActionTest.class,
        XmlFileValidateStartupActionFactoryTest.class,
        DebugMenuTest.class,
        LogixLoadActionTest.class,
        XmlFileCheckActionTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}

