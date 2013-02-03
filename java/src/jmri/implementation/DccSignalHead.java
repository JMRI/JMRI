// This file is part of JMRI.
//
// JMRI is free software; you can redistribute it and/or modify it under
// the terms of version 2 of the GNU General Public License as published
// by the Free Software Foundation. See the "COPYING" file for a copy
// of this license.
//
// JMRI is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// for more details.

package jmri.implementation;

import jmri.*;

/**
 * This class implements a SignalHead the maps the various appearances values to
 * aspect values in the <B>Extended Accessory Decoder Control Packet Format</B> and
 * outputs that packet to the DCC System via the generic CommandStation interface
 * <P>
 * The mapping is as follows:
 * <P>
 *    0 = RED         <BR>
 *    1 = YELLOW      <BR>
 *    2 = GREEN       <BR>
 *    3 = LUNAR       <BR>
 *    4 = FLASHRED    <BR>
 *    5 = FLASHYELLOW <BR>
 *    6 = FLASHGREEN  <BR>
 *    7 = FLASHLUNAR  <BR>
 *    8 = DARK        <BR>
 * <P>
 * The FLASH appearances are expected to be implemented in the decoder.
 *
 * @author Alex Shepherd Copyright (c) 2008
 * @version $Revision$
 */
public class DccSignalHead extends AbstractSignalHead {

  public DccSignalHead( String sys, String user ) {
    super(sys, user);
    configureHead(sys);
  }

  public DccSignalHead( String sys ) {
    super(sys);
    configureHead(sys);
  }

  void configureHead(String sys){
    //New method seperates the system name and address using $
    if(sys.contains("$")){
        dccSignalDecoderAddress = Integer.parseInt(sys.substring(sys.indexOf("$")+1, sys.length()));
        String commandStationPrefix = sys.substring(0, sys.indexOf("$")-1);
        java.util.List<Object> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);
        if(connList!=null){
            for(int x = 0; x < connList.size(); x++){
                jmri.CommandStation station = (jmri.CommandStation) connList.get(x);
                if(station.getSystemPrefix().equals(commandStationPrefix)){
                    c = station;
                    break;
                }
            }
        }
        if(c==null){
            c = InstanceManager.commandStationInstance();
            log.error("No match against the command station for " + sys + ", so will use the default");
        }
    } else {
        c = InstanceManager.commandStationInstance();
        if (( sys.length() > 2 ) && (( sys.charAt(1) == 'H' ) || ( sys.charAt(1) == 'h' )))
          dccSignalDecoderAddress = Integer.parseInt(sys.substring(2,sys.length()));
        else
          dccSignalDecoderAddress = Integer.parseInt(sys);
    }
  }

  public void setAppearance(int newAppearance) {
    int oldAppearance = mAppearance;
    mAppearance = newAppearance;

    if (oldAppearance != newAppearance) {
      updateOutput();

      // notify listeners, if any
      firePropertyChange("Appearance", Integer.valueOf(oldAppearance), Integer.valueOf(newAppearance));
    }
  }

  public void setLit(boolean newLit) {
    boolean oldLit = mLit;
    mLit = newLit;
    if (oldLit != newLit) {
      updateOutput();
      // notify listeners, if any
      firePropertyChange("Lit", Boolean.valueOf(oldLit), Boolean.valueOf(newLit));
    }
  }

  /**
   * Set the held parameter.
   * <P>
   * Note that this does not directly effect the output on the layout;
   * the held parameter is a local variable which effects the aspect
   * only via higher-level logic
   */

  public void setHeld(boolean newHeld) {
    boolean oldHeld = mHeld;
    mHeld = newHeld;
    if (oldHeld != newHeld) {
      // notify listeners, if any
      firePropertyChange("Held", Boolean.valueOf(oldHeld), Boolean.valueOf(newHeld));
    }
  }

  protected void updateOutput() {
    if (c != null) {
      int aspect = 8 ;  // SignalHead.DARK, but default set below

      if( getLit() ) {
        switch( mAppearance ){
          case SignalHead.DARK:        aspect = 8 ; break;
          case SignalHead.RED:         aspect = 0 ; break;
          case SignalHead.YELLOW:      aspect = 1 ; break;
          case SignalHead.GREEN:       aspect = 2 ; break;
          case SignalHead.LUNAR:       aspect = 3 ; break;
          case SignalHead.FLASHRED:    aspect = 4 ; break;
          case SignalHead.FLASHYELLOW: aspect = 5 ; break;
          case SignalHead.FLASHGREEN:  aspect = 6 ; break;
          case SignalHead.FLASHLUNAR:  aspect = 7 ; break;
          default :                    aspect = 8;
                                       log.error("Unknown appearance " + mAppearance+" displays DARK");
                                       break;
        }
      }

      c.sendPacket( NmraPacket.altAccSignalDecoderPkt( dccSignalDecoderAddress, aspect ), 3);
    }
  }
  
  CommandStation c;

  int dccSignalDecoderAddress ;
}
