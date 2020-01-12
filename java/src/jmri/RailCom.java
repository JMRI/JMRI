package jmri;

import java.util.List;

/**
 * RailCom represents a RailCom enabled decoder that might be fitted to a
 * specific piece of rolling stock to uniquely identify it.<br>
 * RailCom is a registered trademark of Lenz GmbH.
 * <p>
 * This implementation of RailCom is an extension of @see IdTag and holds the
 * additional information that can be supplied by the decoder as defined in
 * RP-9.3.2
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @since 2.99.4
 */
public interface RailCom extends AddressedIdTag {

    /**
     * Constant representing that we do not know the address type of the
     * decoder. This is the initial state of a newly created object before.
     */
    public final static int NO_ADDRESS = 0x00;

    /**
     * Constant representing that the address type reported back is Short.
     */
    public final static int SHORT_ADDRESS = 0x02;

    /**
     * Constant representing that the address type reported back is Long.
     */
    public final static int LONG_ADDRESS = 0x04;

    /**
     * Constant representing that the address type reported back is part of a
     * Consist.
     */
    public final static int CONSIST_ADDRESS = 0x08;

    final public static int ORIENTA = 0x10;
    final public static int ORIENTB = 0x20;

    /**
     * Method for a RailCom Reader to set the orientation reported back from a
     * device
     *
     * @param type the orientation to set
     */
    public void setOrientation(int type);

    /**
     * Gets the Orientation of the Rail Com device on the track
     *
     * @return current orientation
     */
    public int getOrientation();

    /**
     * Gets the address reported back as a {@link jmri.LocoAddress}.
     *
     * @return current DCC loco address
     * @deprecated since 4.15.4  Use getLocoAddress() instead.
     */
    @Deprecated
    default public DccLocoAddress getDccLocoAddress(){
       return (DccLocoAddress) getLocoAddress();
    }

    /**
     * Method for a RailCom Reader to set the Address type reported back from a
     * device
     *
     * @param type set type of address
     * @deprecated since 4.15.4  Use {@link jmri.LocoAddress.Protocol} instead.
     */
    @Deprecated
    public void setAddressType(int type);

    /**
     * Gets the actual type of address reported back by the RailCom device
     *
     * @return -1 if not set.
     * @deprecated since 4.15.4  Use {@link jmri.LocoAddress.Protocol} instead.
     */
    @Deprecated
    public int getAddressType();

    /**
     * Gets the actual address type as a String.
     *
     * @return the address type
     * @deprecated since 4.15.4  Use {@link jmri.LocoAddress.Protocol} instead.
     */
    @Deprecated
    public String getAddressTypeAsString();

    /**
     * Method for a RailCom Reader to set the Actual speed reported back from a
     * device
     *
     * @param actualSpeed the speed
     */
    public void setActualSpeed(int actualSpeed);

    /**
     * Gets the actual speed reported by the RailCom device as a representation
     * 128 speed steps
     *
     * @return -1 if not set.
     */
    public int getActualSpeed();

    /**
     * Method for a RailCom Reader to set the Actual Load back from a device.
     *
     * @param actualLoad the load
     */
    public void setActualLoad(int actualLoad);

    /**
     * Gets the actual load reported by decoder the RailCom device.
     *
     * @return -1 if not set.
     */
    public int getActualLoad();

    /**
     * Method for a RailCom Reader to set the actual temperate reported back
     * from a device.
     *
     * @param actualTemp the temperature
     */
    public void setActualTemperature(int actualTemp);

    /**
     * Gets the actual temperate reported by the RailCom device. Location is
     * configured in CV876 (RP.9.3.2)
     *
     * @return -1 if not set.
     */
    public int getActualTemperature();

    /**
     * Method for a RailCom Reader to set the fuel level reported back from a
     * device.
     *
     * @param fuelLevel the fuel level
     */
    public void setFuelLevel(int fuelLevel);

    /**
     * Method for a RailCom Reader to set the water level reported back from a
     * device.
     *
     * @param waterLevel the water level
     */
    public void setWaterLevel(int waterLevel);

    /**
     * Gets the remaining fuel level as a % Fuel level CV879 (RP.9.3.2)
     *
     * @return -1 if not set.
     */
    public int getFuelLevel();

    /**
     * Gets the remaining fuel level as a % Water level CV878 (RP.9.3.2)
     *
     * @return -1 if not set.
     */
    public int getWaterLevel();

    /**
     * Method for a RailCom Reader to set the location reported back from a
     * device.
     *
     * @param location the location
     */
    public void setLocation(int location);

    /**
     * Gets the Last Location that the RailCom device was identified in Location
     * is configured in CV876 (RP.9.3.2)
     *
     * @return -1 if not set.
     */
    public int getLocation();

    /**
     * Method for a RailCom Reader to set the routing number reported back from
     * a device.
     *
     * @param routingno the routing number
     */
    public void setRoutingNo(int routingno);

    /**
     * Gets the routing number that the RailCom device wishes to travel. Route
     * Number is configured in CV874 (RP.9.3.2)
     *
     * @return -1 if not set.
     */
    public int getRoutingNo();

    /**
     * Gets the value of a CV reported back from the RailCom device.
     *
     * @param cv CV number that the value relates to.
     * @return the value of the CV, or 0 if none has yet been collected
     */
    public int getCV(int cv);

    /**
     * Sets the value of a CV reported back from the decoder.
     *
     * @param cv    CV number that the value relates to.
     * @param value Value of the CV
     */
    public void setCV(int cv, int value);

    /**
     * Set the CV number of the next expected value to be returned in a RailCom
     * Packet.
     *
     * @param cv the expected CV
     */
    public void setExpectedCv(int cv);

    /**
     * Get the expected CV to be returned in a RailCom Packet.
     *
     * @return the expected CV
     */
    public int getExpectedCv();

    /**
     * Set the value of the CV that has been read from the RailCom packet.
     *
     * @param value the CV value
     */
    public void setCvValue(int value);

    /**
     * Get a list of the CVs last seen for this RailCom device.
     *
     * @return a list of CVs
     */
    public List<Integer> getCVList();

}
