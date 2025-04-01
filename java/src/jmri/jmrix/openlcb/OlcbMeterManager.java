package jmri.jmrix.openlcb;

import jmri.*;
import jmri.implementation.*;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.openlcb.*;
import org.openlcb.implementations.LocationServiceUtils;

import javax.annotation.Nonnull;

/**
 * Central functions for OlcbMeters.
 *
 * This sends a "Identify Consumers 01.02.00.00.00.00.00.00" when created to get
 * all existing sources of Location Services info to reply.
 *
 * When it sees an EWP PCER for Location Services, it parses the message
 * for current (Amperes) or voltage (Volts) analog content.  If found,
 * it first makes sure a meter exists for that quantity, then updates
 * that meter object.
 *
 * System names SystemPrefix-NodeID-UnitCode-OrdinalNumber-AnyGivenText
 * User name is (at start) "NodeNameFromSnip AnyGivenText (Unit)"
 *
 * @author Bob Jacobsen Copyright (C) 2025
 */
public class OlcbMeterManager extends jmri.managers.AbstractMeterManager {

    /**
     * Create a new MeterManager instance.
     * 
     * @param memo the system connection
     */
    public OlcbMeterManager(@Nonnull CanSystemConnectionMemo memo) {
        super(memo);
        this.memo = memo;
        this.iface = memo.get(OlcbInterface.class);
        iface.registerMessageListener(new EWPListener());
        this.store = memo.get(MimicNodeStore.class);
    }

    private final OlcbInterface iface;
    private final CanSystemConnectionMemo memo;
    private final MimicNodeStore store;
    
    class EWPListener extends MessageDecoder {
        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
            
            LocationServiceUtils.Content content = LocationServiceUtils.parse(msg);
            if (content == null) return; // not location services
            
            // process the blocks looking for an analog block
            int ordinal = 1;
            var scannedNode = content.getScannedDevice();
            String scannedName ="";
            if (store != null) scannedName = store.findNode(scannedNode).getSimpleNodeIdent().getUserName();
            log.debug("Retrieved scannedNode {} scannedName {}", scannedNode, scannedName);
            if (scannedName == null || scannedName.isEmpty()) scannedName = scannedNode.toString();
            
            for (LocationServiceUtils.Block block : content.getBlocks()) {
                log.debug("  Block of type {}", block.getType());
                if (block instanceof LocationServiceUtils.AnalogBlock ) {
                    var analog = (LocationServiceUtils.AnalogBlock) block;
                    // analog block: find an existing meter or make a new one
                    var text = analog.getText();
                    var unit = analog.getUnit();
                    
                    var systemLetter = memo.getSystemPrefix();
                    var sysName  = systemLetter+typeLetter()+" "+scannedNode.toString()+" "+ordinal+" "+text;
                    var userName = scannedName+" "+ordinal+" "+text;
                    
                    log.debug("  Unit: {}, Text: '{}'  systemName: '{}'", unit, text, sysName);
                    
                    var meter = getBySystemName(sysName);
                                        
                    // did we get one?
                    if (meter == null) {
                        // no, create it
                        log.debug("Creating new meter '{}' of type '{}'",
                                text, unit);
                        if (unit == LocationServiceUtils.AnalogBlock.Unit.VOLTS) {
                            meter = createVoltageMeter(sysName);
                        } else if (unit == LocationServiceUtils.AnalogBlock.Unit.AMPERES) {
                            meter = createCurrentMeter(sysName);
                        } else {
                            log.warn("Meters of type {} are not supported yet", unit);
                            continue;
                        }
                        //store meter by incoming name for lookup later
                        InstanceManager.getDefault(MeterManager.class).register(meter);
                    }
                    
                    // set the user name to keep it updated
                    meter.setUserName(userName);
                    
                    // meter exists here - give it a value
                    ((AbstractAnalogIO)meter).setValue(analog.getValue());
                    
                    // and go on to the next one
                    ordinal++;
                }
            }
        }
    }

    public static Meter createVoltageMeter(String sysName) {
        var meter = new DefaultMeter.DefaultVoltageMeter(
                           sysName, Meter.Unit.NoPrefix, 0., 50., 0.001, null); // no updateTask
        InstanceManager.getDefault(MeterManager.class).register(meter);
        return meter;
    }
    
    public static Meter createCurrentMeter(String sysName) {
        var meter = new DefaultMeter.DefaultCurrentMeter(
                           sysName, Meter.Unit.NoPrefix, 0., 50., 0.001, null); // no updateTask
        InstanceManager.getDefault(MeterManager.class).register(meter);
        return meter;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbMeterManager.class);
    
}
