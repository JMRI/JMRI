package jmri.jmrix.can.cbus.simulator.moduletypes;

import static jmri.jmrix.can.cbus.CbusConstants.MANU_MERG;

import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.CbusSimulatedModuleProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * MERG CANPAN CBUS Simulation Module Provider.
 * @author Steve Young Copyright (C) 2018
 */
@ServiceProvider(service = CbusSimulatedModuleProvider.class)
public class MergCanpan extends CbusSimulatedModuleProvider {

    @Override
    public int getManufacturerId() {
        return MANU_MERG;
    }

    @Override
    public int getModuleId() {
        return 29;
    }

    @Override
    public void setDummyNodeParameters(CbusDummyNode node) {
        int[] _params = new int[]{
            20, /* 0 num parameters   */
            MANU_MERG, /* 1 manufacturer ID   */
            89, /* 2 Minor code version   */
            29, /* 3 Manufacturer module identifier   */
            128, /* 4 Number of supported events   */
            13, /* 5 Number of Event Variables per event   */
            1, /* 6 Number of Node Variables   */
            1, /* 7 Major version   */
            13, /* 8 Node flags   */
            13, /* 9 Processor type   */
            1, /* 10 Bus type   */
            0, /* 11 load address, 1/4 bytes   */
            8, /* 12 load address, 2/4 bytes   */
            0, /* 13 load address, 3/4 bytes   */
            0, /* 14 load address, 4/4 bytes   */
            0, /* 15 CPU manufacturer's id 1/4  */
            0, /* 16 CPU manufacturer's id 2/4  */
            0, /* 17 CPU manufacturer's id 3/4  */
            0, /* 18 CPU manufacturer's id 4/4  */
            1, /* 19 CPU manufacturer code   */
            1, /* 20 Beta revision   */
        };
        node.getNodeParamManager().setParameters(_params);
        node.getNodeNvManager().setNVs( new int[]{ 1 , 0 } ); // 1 NV, NV1 set at 0
        node.setNodeNameFromName("PAN");
    }

}
