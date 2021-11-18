package jmri.jmrix.rps;

import jmri.*;
import jmri.PhysicalLocationReporter.Direction;

/**
 * RPS report.
 * 
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class RpsReport implements jmri.ExtendedReport {

    private final int regionId;
    private final Direction direction;
    private final PhysicalLocation physicalLocation;

    public RpsReport(int regionId, Direction direction, PhysicalLocation physicalLocation) {
        this.regionId = regionId;
        this.direction = direction;
        this.physicalLocation = physicalLocation;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasLocoAddress() {
        return Engine.instance().getTransmitter(regionId) != null;
    }

    /** {@inheritDoc} */
    @Override
    public LocoAddress getLocoAddress() {
        // The id is the ID of the locomotive (I think)
        log.debug("Parsed ID: {}", regionId);
        // I have no idea what kind of loco address an RPS reporter uses,
        // so we'll default to DCC for now.
        int addr = Engine.instance().getTransmitter(regionId).getAddress();
        return (new DccLocoAddress(addr, LocoAddress.Protocol.DCC));
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasDirection() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Direction getDirection() {
        return direction;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasPhysicalLocation() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public PhysicalLocation getPhysicalLocation() {
        return physicalLocation;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasRegionId() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getRegionId() {
        return Integer.toString(regionId);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getRegionId();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RpsReport.class);
}
