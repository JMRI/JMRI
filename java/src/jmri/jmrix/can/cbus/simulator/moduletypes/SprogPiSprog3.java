package jmri.jmrix.can.cbus.simulator.moduletypes;

import javax.annotation.Nonnull;

import static jmri.jmrix.can.cbus.CbusConstants.SPROG_DCC;

import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.CbusSimulatedModuleProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * Sprog Pi3 CBUS Simulation Module Provider.
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
        return 1;
    }

    @Override
    public void configureDummyNode(@Nonnull CbusDummyNode node) {
        int[] _params = new int[]{ 
            20, /* 0 num parameters   */
            SPROG_DCC, /* 1 manufacturer ID   */
            'f', /* 2 Minor code version   */
            1, /* 3 Manufacturer module identifier   */
            0, /* 4 Number of supported events   */
            0, /* 5 Number of Event Variables per event   */
            21, /* 6 Number of Node Variables   */
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
            21, // 21 NV's, defined in position 0
            0, // 1 command station number
            0x76, // 2 User flags
            0, // 3 Operations flags
            0, // 4 Debug flags
            0, // 5 Prog track power mode
            250, // 6 Prog track trip limit
            0, // 7 Read only vinsense
            0, // 8 Read only isense_main
            0x02, // 9 DCC accessory packet repeat count
            0x00, // 10 Multimeter enable
            0, // 11 NN to map to DCC hi byte
            0, // 12 NN to map to DCC lo byte
            250, // 13 Main track trip limit
            0, // 14 Read only isense_prog
            0, // 15 Read only main current sense high water mark
            0, // 16 Read only prog current sense high water mark
            0, // 17 Setup mode
            114, // 18 Default CAN ID
            0xFF, // 19 Node number hi byte
            0xFE, // 20 Node number lo byte
            16, // 21 Number of pre-amble bits
        }; 
        node.getNodeNvManager().setNVs( _nvArray );

        node.setNodeNameFromName("PiSPRG3");
    }

}
