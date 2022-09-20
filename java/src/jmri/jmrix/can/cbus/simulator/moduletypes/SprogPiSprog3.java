package jmri.jmrix.can.cbus.simulator.moduletypes;

import javax.annotation.Nonnull;

import static jmri.jmrix.can.cbus.CbusConstants.MTYP_CANPiSPRG3;
import static jmri.jmrix.can.cbus.CbusConstants.SPROG_DCC;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.simulator.CbusSimulatedModuleProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Sprog DCC Pi-SPROG 3 (not v2) CBUS Simulation Module Provider.
 * @author Andrew Crosland Copyright (C) 2021
 * @author Steve Young Copyright (C) 2022
 */
@ServiceProvider(service = CbusSimulatedModuleProvider.class)
public class SprogPiSprog3 extends CbusSimulatedModuleProvider {

    @Override
    public int getManufacturerId() {
        return SPROG_DCC;
    }

    @Override
    public int getModuleId() {
        return MTYP_CANPiSPRG3;
    }

    @Override
    public void configureDummyNode(@Nonnull CbusNode node) {
        int[] _params = new int[]{ 
            20, /* 0 num parameters   */
            SPROG_DCC, /* 1 manufacturer ID   */
            'f', /* 2 Minor code version   */
            MTYP_CANPiSPRG3, /* 3 Manufacturer module identifier   */
            0, /* 4 Number of supported events   */
            0, /* 5 Number of Event Variables per event   */
            13, /* 6 Number of Node Variables   */
            3, /* 7 Major version   */
            0, /* 8 Node flags   */ 
            25, /* 9 Processor type   */
            0, /* 10 Bus type   */
            0, /* 11 load address, 1/4 bytes   */
            8, /* 12 load address, 2/4 bytes   */
            4, /* 13 load address, 3/4 bytes   */
            0, /* 14 load address, 4/4 bytes   */
            0, /* 15 CPU manufacturer's id 1/4  */
            0, /* 16 CPU manufacturer's id 2/4  */
            0, /* 17 CPU manufacturer's id 3/4  */
            0, /* 18 CPU manufacturer's id 4/4  */
            1, /* 19 CPU manufacturer code   */
            1, /* 20 Beta revision   */
        };
        node.getNodeParamManager().setParameters(_params);

        int[] _nvArray = new int[]{
            13, // 13 NV's, defined in position 0
            0, // 1 Setup mode
            0, // 2 ZTC mode
            0, // 3 Blueline mode
            0, // 4 ACK sensitivity
            0, // 5 Command station mode
            250, // 6 Track trip limit
            0, // 7 Read only vinsense
            0, // 8 Read only isense
            0x02, // 9 DCC accessory packet repeat count
            0x00, // 10 Multimeter enable
            16, // 11 Number of pre-amble bits
            0x76, // 12 User flags
            0, // 13 Operations flags
        }; 
        node.getNodeNvManager().setNVs( _nvArray );

        node.setNodeNameFromName("PiSPRG3");
    }

}
