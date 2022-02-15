package jmri.jmrix.can.cbus.simulator.moduletypes;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.simulator.CbusSimulatedModuleProvider;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;

import static jmri.jmrix.can.cbus.CbusConstants.MANU_MERG;

import org.openide.util.lookup.ServiceProvider;

/**
 * MERG CANMIO-SVO CBUS Simulation Module Provider.
 * @author Steve Young Copyright (C) 2022
 */
@ServiceProvider(service = CbusSimulatedModuleProvider.class)
public class MergCanmiosvo extends CbusSimulatedModuleProvider {

    @Override
    public int getManufacturerId() {
        return MANU_MERG;
    }

    @Override
    public int getModuleId() {
        return 50;
    }
    
    @Override
    public void setDummyNodeParameters(@Nonnull CbusDummyNode node) {
        int[] _params = new int[]{
            20, /* 0 num parameters   */
            MANU_MERG, /* 1 manufacturer ID   */
            01, /* 2 Minor code version   */
            50, /* 3 Manufacturer module identifier   */
            128, /* 4 Number of supported events     */
            4, /* 5 Number of Event Variables per event  
                ;EV1 = servo number.  Can have several servos per event.
                ;EV2 = polarity for that event
                ;EV3 = Used to teach the feedback responses.
                ;EV4 = Available but not used yet. */
            37, /* 6 Number of Node Variables   */
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

        int[] _nvArray = new int[38];
        java.util.Arrays.fill(_nvArray, 0);
        _nvArray[0]=37;

        node.getNodeNvManager().setNVs( _nvArray ); //
        node.setNodeNameFromName("MIO-SVO");
    }

}
