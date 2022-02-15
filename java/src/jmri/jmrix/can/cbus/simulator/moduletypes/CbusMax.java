package jmri.jmrix.can.cbus.simulator.moduletypes;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.CbusSimulatedModuleProvider;

import org.openide.util.lookup.ServiceProvider;

/**
 * CbusMax CBUS Simulation Module Provider.
 * A simulation which has 255 NVs, 255 Events, and 255 EVs per Event.
 * @author Steve Young Copyright (C) 2022
 */
@ServiceProvider(service = CbusSimulatedModuleProvider.class)
public class CbusMax extends CbusSimulatedModuleProvider {

    @Override
    public int getManufacturerId() {
        return 0;
    }

    @Override
    public int getModuleId() {
        return 255;
    }

    @Override
    public void setDummyNodeParameters(@Nonnull CbusDummyNode node) {
        // 255 parameters could have unintended future
        // consequences so staying with the standard 20
        // for now
        int[] _params = new int[]{
            20, /* 0 num parameters   */
            0, /* 1 manufacturer ID   */
            89, /* 2 Minor code version   */
            255, /* 3 Manufacturer module identifier   */
            255, /* 4 Number of supported events   */
            255, /* 5 Number of Event Variables per event   */
            255, /* 6 Number of Node Variables   */
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
        int[] _nvArray = new int[256];
        _nvArray[0]=255;
        for (int i = 1; i < _nvArray.length; i++) {
            _nvArray[i] = i;
        }
        // create array of event variables
        int[] evVarArray = new int[255];
        for (int i = 0; i < evVarArray.length; i++) {
            evVarArray[i] = i+1;
        }
        for (int i = 1; i < 256; i++) {

            CbusNodeEvent singleEv = new CbusNodeEvent(node.getMemo(),i,i,-1,i,255);
            singleEv.setEvArr(evVarArray);
            node.getNodeEventManager().addNewEvent(singleEv);

        }
        node.getNodeNvManager().setNVs( _nvArray );
        node.setNodeNameFromName("MAXND");
    }

    @Override
    public String getModuleType() {
        return "CBUS TestMax";
    }
    
    @Override
    public String getToolTipText() {
        return "Simulated Module which has 255 NVs, 255 Events, and 255 EVs per Event.";
    }
    
}
