package jmri.jmrix.can.cbus.simulator;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulating a MERG CBUS Command Station + other network objects.
 * <p>
 * By default starts with 1 command station, a node in SLiM mode,
 * and an event request responder.
 * <p>
 * All simulation responses can be sent as {@link jmri.jmrix.can.CanMessage} or {@link jmri.jmrix.can.CanReply}
 *
 * @author Steve Young Copyright (C) 2018
 * @see CbusDummyCS
 * @see CbusEventResponder
 * @see CbusDummyNode
 * @since 4.15.2
 */
public class CbusSimulator implements jmri.Disposable {

    private final CanSystemConnectionMemo memo;
    public ArrayList<CbusDummyCS> _csArr;
    public ArrayList<CbusDummyNode> _ndArr;
    public ArrayList<CbusEventResponder> _evResponseArr;

    public CbusSimulator(@Nonnull CanSystemConnectionMemo sysmemo){
        memo = sysmemo;
        _csArr = new ArrayList<>();
        _ndArr = new ArrayList<>();
        _evResponseArr = new ArrayList<>();
        init();
    }

    public final void init(){
        log.info("Starting CBUS Network Simulation Tools");
        
        _csArr.add(new CbusDummyCS(memo)); // type, id, memo
        
        
        // _ndArr.add(new CbusDummyNode(0,165,0,0,memo)); // nn, manufacturer, type, canid, memo
        
        
        _evResponseArr.add(new CbusEventResponder(memo) );
    }
    
    public int getNumCS(){
        return _csArr.size();
    }
    
    public int getNumNd(){
        return _ndArr.size();
    }
    
    public int getNumEv(){
        return _evResponseArr.size();
    }
    
    public CbusDummyCS getCS(int id){
        return _csArr.get(id);
    } 
    
    public CbusDummyNode getNd(int id){
        return _ndArr.get(id);
    }

    public void addNode ( CbusDummyNode nd) {
        _ndArr.add(nd);
    }

    public void removeNode(CbusDummyNode nd) {
        _ndArr.remove(nd);
    }

    public CbusEventResponder getEv( int id ){
        return _evResponseArr.get(id);
    }
    
    /**
     * @return the simulated command stations, in the order made
     * @since 5.19.1
     */
    @Nonnull
    public List<CbusDummyCS> getCommandStations() {
        return new ArrayList<>(_csArr);
    }

    /**
     * Is this the simulator of a CAN Simulation connection, with no real bus?
     * Only then may simulated trains use it.
     *
     * @return true if the connection's traffic controller is CAN Simulation's
     * @since 5.19.1
     */
    public boolean isOnCanSimulation() {
        return memo.getTrafficController() instanceof jmri.jmrix.can.adapters.loopback.LoopbackTrafficController;
    }

    /**
     * Speed commands for simulated trains, from this simulator's command
     * stations. Made when first asked for, and kept until disposed.
     *
     * @return the speed source
     * @since 5.19.1
     */
    @Nonnull
    public synchronized CbusSimulatedSpeedSource getSimulatedSpeedSource() {
        if (speedSource == null) {
            speedSource = new CbusSimulatedSpeedSource(this);
        }
        return speedSource;
    }

    /**
     * Where simulated trains report detection on this connection's CBUS
     * sensors. Made when first asked for, and kept until disposed.
     *
     * @return the output
     * @since 5.19.1
     */
    @Nonnull
    public synchronized CbusSimulatedSensorOutput getSimulatedSensorOutput() {
        if (sensorOutput == null) {
            sensorOutput = new CbusSimulatedSensorOutput(memo);
        }
        return sensorOutput;
    }

    /**
     * Where simulated trains report their ID tags on this connection's CBUS
     * reporters. Made when first asked for, and kept until disposed.
     *
     * @return the output
     * @since 5.19.1
     */
    @Nonnull
    public synchronized CbusSimulatedReporterOutput getSimulatedReporterOutput() {
        if (reporterOutput == null) {
            reporterOutput = new CbusSimulatedReporterOutput(memo);
        }
        return reporterOutput;
    }

    private CbusSimulatedSpeedSource speedSource;
    private CbusSimulatedSensorOutput sensorOutput;
    private CbusSimulatedReporterOutput reporterOutput;

    public CbusDummyCS getNewCS(){
        CbusDummyCS newcs = new CbusDummyCS(memo);
        _csArr.add(newcs);
        return newcs;
    }

    public CbusEventResponder getNewEv(){
        CbusEventResponder newcs = new CbusEventResponder(memo);
        _evResponseArr.add(newcs);
        return newcs;
    }

    /**
     * Disposes of all simulated objects.
     * CanListeners can be removed and command stations can stop session timers.
     * Does not remove instance from InstanceManager or CAN memo.
     */
    @Override
    public void dispose() {
        synchronized (this) {
            if (speedSource != null) {
                speedSource.dispose();
            }
            if (sensorOutput != null) {
                sensorOutput.dispose();
            }
            if (reporterOutput != null) {
                reporterOutput.dispose();
            }
        }
        log.info("Stopping {} Simulations",_csArr.size()+_ndArr.size()+_evResponseArr.size());
        for ( CbusDummyCS cs : _csArr ) {
            cs.dispose();
        }
        _csArr.clear();

        for ( CbusDummyNode cs : _ndArr ) {
            cs.dispose();
        }
        _ndArr.clear();

        for ( CbusEventResponder cs : _evResponseArr ) {
            cs.dispose();
        }
        _evResponseArr.clear();

    }

    private static final Logger log = LoggerFactory.getLogger(CbusSimulator.class);

}
