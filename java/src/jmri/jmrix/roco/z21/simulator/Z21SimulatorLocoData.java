package jmri.jmrix.roco.z21.simulator;

/*
 *  This class stores some information for
 *  passing data between the Throttle and the Z21
 *   
 *  @author      Paul Bender, Copyright (C) 2016
 */
class Z21SimulatorLocoData {

    byte address_msb;
    byte address_lsb;
    byte speed_byte;

    /**
     * Construct an object of this type with all the data we are recording.
     */
    public Z21SimulatorLocoData(byte addr_msb, byte addr_lsb, byte speed){
       address_msb = addr_msb;
       address_lsb = addr_lsb;
       speed_byte = speed;
    }

    byte getAddressLsb(){
         return address_lsb;
    }

    byte getAddressMsb(){
         if (address_msb == 0) {
             return address_msb;
         } else {
             // this is a long address with an offset added.
             int address = ( (0xff00 & (address_msb<<8) ) + (0xff & address_lsb)) - 0xC000;
             return (byte)(( 0xff00 & address)>>8);
         }
    }

    byte getSpeed(){
         return speed_byte;
    }
}
