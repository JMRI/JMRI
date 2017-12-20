package jmri.jmrix;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AbstractMRTrafficControllerTest.class,
        AbstractMRNodeTrafficControllerTest.class,

        jmri.jmrix.AbstractProgrammerTest.class,
        jmri.jmrix.AbstractMRReplyTest.class,
        AbstractThrottleTest.class,
        BundleTest.class,
        jmri.jmrix.ConnectionConfigManagerTest.class,

        jmri.jmrix.acela.PackageTest.class,
        jmri.jmrix.anyma.PackageTest.class,
        jmri.jmrix.bachrus.PackageTest.class,
        jmri.jmrix.can.PackageTest.class,
        jmri.jmrix.configurexml.PackageTest.class,
        jmri.jmrix.cmri.PackageTest.class,
        jmri.jmrix.dcc.PackageTest.class,
        jmri.jmrix.dcc4pc.PackageTest.class,
        jmri.jmrix.direct.PackageTest.class,
        jmri.jmrix.dccpp.PackageTest.class,
        jmri.jmrix.easydcc.PackageTest.class,
        jmri.jmrix.ecos.PackageTest.class,
        jmri.jmrix.grapevine.PackageTest.class,
        jmri.jmrix.ieee802154.PackageTest.class,
        jmri.jmrix.internal.PackageTest.class,
        jmri.jmrix.jmriclient.PackageTest.class,
        jmri.jmrix.lenz.PackageTest.class,
        jmri.jmrix.loconet.PackageTest.class,
        jmri.jmrix.maple.PackageTest.class,
        jmri.jmrix.marklin.PackageTest.class,
        jmri.jmrix.merg.PackageTest.class,
        jmri.jmrix.modbus.PackageTest.class,
        jmri.jmrix.mrc.PackageTest.class,
        jmri.jmrix.nce.PackageTest.class,
        jmri.jmrix.oaktree.PackageTest.class,
        jmri.jmrix.openlcb.PackageTest.class,
        jmri.jmrix.pi.PackageTest.class,
        jmri.jmrix.powerline.PackageTest.class,
        jmri.jmrix.pricom.PackageTest.class,
        jmri.jmrix.qsi.PackageTest.class,
        jmri.jmrix.rfid.PackageTest.class,
        jmri.jmrix.roco.PackageTest.class,
        jmri.jmrix.rps.PackageTest.class,
        jmri.jmrix.secsi.PackageTest.class,
        jmri.jmrix.sprog.PackageTest.class,
        jmri.jmrix.srcp.PackageTest.class,
        jmri.jmrix.tams.PackageTest.class,
        jmri.jmrix.tmcc.PackageTest.class,
        jmri.jmrix.wangrow.PackageTest.class,
        jmri.jmrix.xpa.PackageTest.class,
        jmri.jmrix.zimo.PackageTest.class,
        jmri.jmrix.jinput.PackageTest.class,
        jmri.jmrix.serialsensor.PackageTest.class,
        jmri.jmrix.ncemonitor.PackageTest.class,
        AbstractMRTrafficControllerTest.class,
        NetworkConfigExceptionTest.class,
        SerialConfigExceptionTest.class,
        ConnectionStatusTest.class,
        jmri.jmrix.swing.PackageTest.class,
        ActiveSystemsMenuTest.class,
        DCCManufacturerListTest.class,
        OtherConnectionTypeListTest.class,
        jmri.jmrix.debugthrottle.PackageTest.class,
        AbstractMessageTest.class,
        AbstractMRMessageTest.class,
        NetMessageTest.class,
        AbstractNodeTest.class,
        AbstractMonFrameTest.class,
        AbstractMonPaneTest.class,
        SystemConnectionMemoTest.class,
        AbstractThrottleManagerTest.class,
        JmrixConfigPaneTest.class,
        jmri.jmrix.ztc.PackageTest.class,
        jmri.jmrix.libusb.PackageTest.class,
        SystemConnectionMemoManagerTest.class,
        UsbPortAdapterTest.class,
})

/**
 * Set of tests for the jmri.jmrix package
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 */
public class PackageTest  {
}
