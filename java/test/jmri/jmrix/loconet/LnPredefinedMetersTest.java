package jmri.jmrix.loconet;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

public class LnPredefinedMetersTest {

    LocoNetInterfaceScaffold lnis;
    SlotManager slotmanager;
    LocoNetSystemConnectionMemo memo;

    @Test
    public void testLnMeter() {
        LnPredefinedMeters lm = new LnPredefinedMeters(memo);
        Assert.assertNotNull("exists",lm);

        Assert.assertEquals("initial number of meters", 0,
                InstanceManager.getDefault(MeterManager.class).getNamedBeanSet().size());

        lm.requestUpdateFromLayout();
        // expect one messages
        Assert.assertEquals("sent", 1, lnis.outbound.size());
        // set CS
        Assert.assertEquals("message 1", "BB 79 41 00", lnis.outbound.get(0).toString());
        int ia[]={0xE6, 0x15, 0x01, 0x79, 0x4B, 0x4D, 0x05, 0x32, 0x70, 0x00, 0x36, 0x00,
                0x33, 0x00, 0x00, 0x00, 0x1C, 0x7F, 0x68, 0x03, 0x38 };
        LocoNetMessage msg = new LocoNetMessage(ia);
        lm.message(msg);

        Assert.assertEquals("number of meters after first query", 2,
                InstanceManager.getDefault(MeterManager.class).getNamedBeanSet().size());

        Assert.assertEquals("Check for correct system name and initial value for amperage bean",
                0.5f,
                getBeanValue(lm, "LVDCS240(s/n488)InputCurrent"),
                0);
        Assert.assertEquals("Check for correct system name and initial value for voltage bean",
                15f,
                getBeanValue(lm, "LVDCS240(s/n488)Voltage"),
                0);

        Assert.assertEquals("check current value of non-existant meter variable",-999.0f,
                getBeanValue(lm, "LvCommandStationCurrent"), 0);
        Assert.assertEquals("check voltage value of non-existant meter variable", -999.0f,
                getBeanValue(lm, "LvCommandStationVoltage"), 0);

        msg.setElement(4,12);
        lm.message(msg);

        Assert.assertEquals("number of meters after second response", 2,
                InstanceManager.getDefault(MeterManager.class).getNamedBeanSet().size());

        Assert.assertEquals("Check for correct system name and second value for amperage bean after 2nd reply",
                0.5f,
                getBeanValue(lm, "LVDCS240(s/n488)InputCurrent"),
                0);
        Assert.assertEquals("Check for correct system name and initial value for voltage bean after 2nd reply",
                2.4f,
                getBeanValue(lm, "LVDCS240(s/n488)Voltage"),
                0.001f);

        msg.setElement(6,20);
        lm.message(msg);

        Assert.assertEquals("number of meters after second response", 2,
                InstanceManager.getDefault(MeterManager.class).getNamedBeanSet().size());

        Assert.assertEquals("Check for correct system name and second value for amperage bean after 3rd reply",
                2.0f,
                getBeanValue(lm, "LVDCS240(s/n488)InputCurrent"),
                0);
        Assert.assertEquals("Check for correct system name and initial value for voltage bean after 3rd reply",
                2.4f,
                getBeanValue(lm, "LVDCS240(s/n488)Voltage"),
                0.001f);

        LocoNetMessage msg2 = new LocoNetMessage(msg);
        msg2.setElement(4, 100);
        msg2.setElement(6, 31);
        msg2.setElement(16,0x1b);
        lm.message(msg2);

        Assert.assertEquals("number of meters after fourth response", 4,
                InstanceManager.getDefault(MeterManager.class).getNamedBeanSet().size());

        Assert.assertEquals("Check for correct system name and second value for amperage bean after 4th reply",
                2.0f,
                getBeanValue(lm, "LVDCS240(s/n488)InputCurrent"),
                0);
        Assert.assertEquals("Check for correct system name and initial value for voltage bean after 4th reply",
                2.4f,
                getBeanValue(lm, "LVDCS240(s/n488)Voltage"),
                0.001f);

        Assert.assertEquals("Check for correct 2nd system name and second value for amperage bean after 4th reply",
                3.1f,
                getBeanValue(lm, "LVDCS210(s/n488)InputCurrent"),
                0.001f);
        Assert.assertEquals("Check for correct 2nd system name and initial value for voltage bean after 4th reply",
                20.0f,
                getBeanValue(lm, "LVDCS210(s/n488)Voltage"),
                0.001f);

    }

    @Test
    public void testSN() {

        LnPredefinedMeters lm = new LnPredefinedMeters(memo);
        Assert.assertNotNull("exists",lm);
        MeterManager mm = InstanceManager.getDefault(MeterManager.class);

        Assert.assertEquals("initial number of meters", 0,
                mm.getNamedBeanSet().size());

        int ia[]={0xE6, 0x15, 0x01, 0x79, 0x4B, 0x4D, 0x05, 0x32, 0x70, 0x00, 0x36, 0x00,
                0x33, 0x00, 0x00, 0x00, 0x34, 0x7F, 0x00, 0x00, 0x38 };
        LocoNetMessage msg = new LocoNetMessage(ia);

        for (Integer oneBitPos = 0; oneBitPos<14; oneBitPos++) {
            Integer serNum = 2<<oneBitPos;
            msg.setElement(18,serNum & 0x7f);
            msg.setElement(19, serNum >>7);
            lm.message(msg);
            Assert.assertEquals("Number of beans at testSN() iteration "+oneBitPos.toString(),
                    2*(oneBitPos+1), mm.getNamedBeanSet().size());
            Assert.assertNotNull("Bean Name (amps) at testSN() iteration"+oneBitPos.toString(),
                    mm.getBySystemName("LVDCS52(s/n"+serNum.toString()+")InputCurrent"));
            Assert.assertNotNull("Bean Name (amps) at testSN() iteration"+oneBitPos.toString(),
                    mm.getBySystemName("LVDCS52(s/n"+serNum.toString()+")Voltage"));
        }
    }

    @Test
    public void testAmps() {

        LnPredefinedMeters lm = new LnPredefinedMeters(memo);
        Assert.assertNotNull("exists",lm);
        MeterManager mm = InstanceManager.getDefault(MeterManager.class);

        Assert.assertEquals("initial number of meters", 0,
                mm.getNamedBeanSet().size());

        int ia[]={0xE6, 0x15, 0x01, 0x79, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x34, 0x00, 0x02, 0x01, 0x38 };
        LocoNetMessage msg = new LocoNetMessage(ia);

        for (Integer oneBitPos = 0; oneBitPos<7; oneBitPos++) {
            Integer ampsVal = 1<<oneBitPos;
            msg.setElement(6, ampsVal & 0x7f);
            lm.message(msg);
            Assert.assertEquals("Number of beans at testAmps() iteration "+oneBitPos.toString(),
                    2, mm.getNamedBeanSet().size());
            Assert.assertNotNull("Bean Name (amps) not null at testAmps() iteration "+oneBitPos.toString(),
                    mm.getBySystemName("LVDCS52(s/n130)InputCurrent"));
            Assert.assertNotNull("Bean Name (volts) not null at testAmps() iteration "+oneBitPos.toString(),
                    mm.getBySystemName("LVDCS52(s/n130)Voltage"));
            Assert.assertEquals("Bean current at testAmps() iteration "+oneBitPos.toString(),
                    ampsVal*.1f, getBeanValue(lm, "LVDCS52(s/n130)InputCurrent"), 0.001);
            Assert.assertEquals("Bean voltage at testAmps() iteration "+oneBitPos.toString(),
                    0.0f, getBeanValue(lm, "LVDCS52(s/n130)Voltage"), 0.001);
        }
    }

    @Test
    public void testVolts() {

        LnPredefinedMeters lm = new LnPredefinedMeters(memo);
        Assert.assertNotNull("exists",lm);
        MeterManager mm = InstanceManager.getDefault(MeterManager.class);

        Assert.assertEquals("initial number of meters", 0,
                mm.getNamedBeanSet().size());

        int ia[]={0xE6, 0x15, 0x01, 0x79, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x34, 0x00, 0x02, 0x01, 0x38 };
        LocoNetMessage msg = new LocoNetMessage(ia);

        for (Integer oneBitPos = 0; oneBitPos<7; oneBitPos++) {
            Integer voltsVal = 1<<oneBitPos;
            msg.setElement(4, voltsVal & 0x7f);
            lm.message(msg);
            Assert.assertEquals("Number of beans at testVolts() iteration "+oneBitPos.toString(),
                    2, mm.getNamedBeanSet().size());
            Assert.assertNotNull("Bean Name (amps) not null at testVolts() iteration "+oneBitPos.toString(),
                    mm.getBySystemName("LVDCS52(s/n130)InputCurrent"));
            Assert.assertNotNull("Bean Name (volts) not null at testVolts() iteration "+oneBitPos.toString(),
                    mm.getBySystemName("LVDCS52(s/n130)Voltage"));
            Assert.assertEquals("Bean current at testVolts() iteration "+oneBitPos.toString(),
                    0.0f, getBeanValue(lm, "LVDCS52(s/n130)InputCurrent"), 0.001);
            Assert.assertEquals("Bean voltage at testVolts() iteration "+oneBitPos.toString(),
                    voltsVal*.2f, getBeanValue(lm, "LVDCS52(s/n130)Voltage"), 0.001);
        }
    }

    @Test
    public void testSlotNum() {

        LnPredefinedMeters lm = new LnPredefinedMeters(memo);
        MeterManager mm = InstanceManager.getDefault(MeterManager.class);
        Assert.assertEquals("TestSlotNum initial number of meters", 0,
                mm.getNamedBeanSet().size());

        int ia[]={0xE6, 0x15, 0x01, 0x79, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x34, 0x00, 0x02, 0x01, 0x38 };
        LocoNetMessage msg = new LocoNetMessage(ia);
        int numBeans = 0;
        for (Integer slotNum = 0; slotNum < 512; slotNum++) {
            msg.setElement(2, slotNum >> 7);
            msg.setElement(3, slotNum & 0x7f);
            lm.message(msg);
            if (slotNum  == 249) {
                numBeans+=2;
            }
            Assert.assertEquals("Number of beans at testSlot() iteration "+String.valueOf(slotNum),
                    numBeans, mm.getNamedBeanSet().size());
        }
    }

    public double getBeanValue(LnPredefinedMeters lm, String meterName) {
        Meter meter = InstanceManager.getDefault(MeterManager.class).getBySystemName(meterName);
        if (meter == null) {
            return -999.0f;
        }
        return meter.getKnownAnalogValue();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        // This test requires a registered connection config since ProxyMeterManager
        // auto creates system meter managers using the connection configs.
        InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        jmri.jmrix.NetworkPortAdapter pa = new jmri.jmrix.loconet.loconetovertcp.LnTcpDriverAdapter();
        pa.setSystemPrefix("L");
        jmri.jmrix.ConnectionConfig cc = new jmri.jmrix.loconet.loconetovertcp.ConnectionConfig(pa);
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class).add(cc);

        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis,slotmanager);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        lnis.dispose();
        JUnitUtil.tearDown();
    }
}
