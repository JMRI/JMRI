#
# This script will populate the tables with many default entries for a Signal LCC node.
# It will populate:
#   'inactive' sensors matching the logic cell default values
#   sensors for masts and rules
#   turnouts for direct lamp testing
#   and more
#   it will create the sensors if they don't exist
#   once created or found AND the user name is blank, it adds the username
#
# Ken Cameron, October 2022
#
# Usage:
#   run the script via the Scripting->Run Script...
#   open Scripting->Script Entry
#   enter createSignalLCCNodeDefaults('your_node_address','your_node_name_prefix')
#   or you can call the individual parts if you only want one or more parts created

import jmri

def createSignalLCCNodeDefaults( nodeAddr, nodeNamePrefix ) :
    print "processing node: " + nodeAddr + " " + nodeNamePrefix
    createSignalLCCPortDefaults( nodeAddr, nodeNamePrefix )
    createSignalLCCLogicDefaults( nodeAddr, nodeNamePrefix )
    createSignalLCCMastDefaults( nodeAddr, nodeNamePrefix )
    createSignalLCCTrackDefaults( nodeAddr, nodeNamePrefix )
    createSignalLCCLampDefaults( nodeAddr, nodeNamePrefix )
    createSignalLCCPowerDefaults( nodeAddr, nodeNamePrefix )
    print "finished node: " + nodeAddr + " " + nodeNamePrefix

def createSignalLCCPortDefaults( nodeAddr, nodeNamePrefix ) :
    print "Creating default port values: " + nodeAddr + " " + nodeNamePrefix
    nParts = nodeAddr.split( '.')
    nBase = str(nParts[0]) + "." + str(nParts[1]) + "." + str(nParts[2]) + "." + str(nParts[3]) + "." + str(nParts[4]) + "." + str(nParts[5])
    baseAddr = 0x00
    idx = 0
    while idx < 8 :
        addr = baseAddr + (12 * idx)
        idx = idx + 1
        # create command defaults
        ii = 0
        while ii < 6 :
            addr2 = addr + ii
            ii = ii + 1
            active = "{0}.{1:02X}.{2:02X}".format(nBase, addr2 >> 8, addr2 & 0xFF)
            s = sensors.provideSensor("MS" + active)
            if (s.getUserName() == None) :
                s.setUserName(nodeNamePrefix + " Port " + str(idx) + " Cmd " + str(ii) + " Default")
            s.setAuthoritative(False)
        # create indicator defaults
        ii = 0
        while ii < 6 :
            addr2 = addr + 6 + ii
            ii = ii + 1
            active = "{0}.{1:02X}.{2:02X}".format(nBase, addr2 >> 8, addr2 & 0xFF)
            s = sensors.provideSensor("MS" + active)
            if (s.getUserName() == None) :
                s.setUserName(nodeNamePrefix + " Port " + str(idx) + " Ind " + str(ii) + " Default")
            s.setAuthoritative(False)
    print "Finished default port values: " + nodeAddr + " " + nodeNamePrefix

def createSignalLCCLogicDefaults( nodeAddr, nodeNamePrefix ) :
    print "Creating default logix cell values: " + nodeAddr + " " + nodeNamePrefix
    nParts = nodeAddr.split( '.')
    nBase = str(nParts[0]) + "." + str(nParts[1]) + "." + str(nParts[2]) + "." + str(nParts[3]) + "." + str(nParts[4]) + "." + str(nParts[5])
    baseAddr = 0xA0
    idx = 0
    while idx < 32 :
        addr = baseAddr + (8 * idx)
        idx = idx + 1
        # create V1 defaults
        active = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 0) & 0xFF)
        inactive = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 1) & 0xFF)
        s = sensors.provideSensor("MS" + active + ";" + inactive)
        if (s.getUserName() == None) :
            s.setUserName(nodeNamePrefix + " Logic " + str(idx) + " V1 Default")
        s.setKnownState(INACTIVE)
        s.setAuthoritative(False)
        # create V2 defaults
        active = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 2) & 0xFF)
        inactive = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 3) & 0xFF)
        s = sensors.provideSensor("MS" + active + ";" + inactive)
        if (s.getUserName() == None) :
            s.setUserName(nodeNamePrefix + " Logic " + str(idx) + " V2 Default")
        s.setKnownState(INACTIVE)
        s.setAuthoritative(False)
        # create action defaults
        ii = 0
        while ii < 4 :
            ii = ii + 1
            active = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 3 + ii) & 0xFF)
            s = sensors.provideSensor("MS" + active)
            if (s.getUserName() == None) :
                s.setUserName(nodeNamePrefix + " Logic " + str(idx) + " Action " + str(ii) + " Default")
            s.setKnownState(INACTIVE)
            s.setAuthoritative(False)
    print "Finished default logix cell values: " + nodeAddr + " " + nodeNamePrefix

def createSignalLCCMastDefaults( nodeAddr, nodeNamePrefix ) :
    print "Creating default mast values: " + nodeAddr + " " + nodeNamePrefix
    nParts = nodeAddr.split( '.')
    nBase = str(nParts[0]) + "." + str(nParts[1]) + "." + str(nParts[2]) + "." + str(nParts[3]) + "." + str(nParts[4]) + "." + str(nParts[5])
    baseAddr = 0x01A0
    idxMast = 0
    while idxMast < 8 :
        idxRule = 0
        while idxRule < 8 :
            addr = baseAddr + (32 * idxMast) + ( 3 * idxRule) + 8   # skip TC, other script handles that.
            setter = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 0) & 0xFF)
            active = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 1) & 0xFF)
            inactive = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 2) & 0xFF)
            s = sensors.provideSensor("MS" + setter)
            if (s.getUserName() == None) :
                s.setUserName(nodeNamePrefix + " Mast " + str(idxMast + 1) + " Rule " + str(idxRule + 1) + " Setter Default")
            # s.setKnownState(INACTIVE)
            s.setAuthoritative(False)
            s = sensors.provideSensor("MS" + active + ";" + inactive)
            if (s.getUserName() == None) :
                s.setUserName(nodeNamePrefix + " Mast " + str(idxMast + 1) + " Rule " + str(idxRule + 1) + " Default")
            s.setAuthoritative(False)
            idxRule = idxRule + 1
        idxMast = idxMast + 1
    print "Finished default mast values: " + nodeAddr + " " + nodeNamePrefix

def createSignalLCCTrackDefaults( nodeAddr, nodeNamePrefix ) :
    print "Creating default Track values: " + nodeAddr + " " + nodeNamePrefix
    nParts = nodeAddr.split( '.')
    nBase = str(nParts[0]) + "." + str(nParts[1]) + "." + str(nParts[2]) + "." + str(nParts[3]) + "." + str(nParts[4]) + "." + str(nParts[5])
    baseAddr = 0x0060
    idxTrack = 0
    # Track Circuits
    while idxTrack < 8 :
        addr = baseAddr + (8 * idxTrack)
        setter = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 0) & 0xFF)
        s = sensors.provideSensor("MS" + setter)
        if (s.getUserName() == None) :
            s.setUserName(nodeNamePrefix + " TC " + str(idxTrack + 1) + " Default")
        s.setAuthoritative(False)
        idxTrack = idxTrack + 1
    print "Finished default Track values: " + nodeAddr + " " + nodeNamePrefix

def createSignalLCCLampDefaults( nodeAddr, nodeNamePrefix ) :
    print "Creating default Lamp values: " + nodeAddr + " " + nodeNamePrefix
    nParts = nodeAddr.split( '.')
    nBase = str(nParts[0]) + "." + str(nParts[1]) + "." + str(nParts[2]) + "." + str(nParts[3]) + "." + str(nParts[4]) + "." + str(nParts[5])
    baseAddr = 0x02A2
    idxLamp = 0
    while idxLamp < 16 :
        addr = baseAddr + (2 * idxLamp)
        active = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 0) & 0xFF)
        inactive = "{0}.{1:02X}.{2:02X}".format(nBase, addr >> 8, (addr + 1) & 0xFF)
        s = turnouts.provideTurnout("MT" + active + ";" + inactive)
        if (s.getUserName() == None) :
            s.setUserName(nodeNamePrefix + " Direct Lamp " + str(idxLamp + 1) + " Default")
        s.setAuthoritative(False)
        idxLamp = idxLamp + 1
    print "Finished default lamp values: " + nodeAddr + " " + nodeNamePrefix

def createSignalLCCPowerDefaults( nodeAddr, nodeNamePrefix ) :
    print "Creating default Power values: " + nodeAddr + " " + nodeNamePrefix
    nParts = nodeAddr.split( '.')
    nBase = str(nParts[0]) + "." + str(nParts[1]) + "." + str(nParts[2]) + "." + str(nParts[3]) + "." + str(nParts[4]) + "." + str(nParts[5])
    baseAddr = 0x02A0
    setter = "{0}.{1:02X}.{2:02X}".format(nBase, baseAddr >> 8, (baseAddr + 0) & 0xFF)
    s = sensors.provideSensor("MS" + setter)
    if (s.getUserName() == None) :
        s.setUserName(nodeNamePrefix + " Power OK Default")
    s.setAuthoritative(False)
    setter = "{0}.{1:02X}.{2:02X}".format(nBase, baseAddr >> 8, (baseAddr + 1) & 0xFF)
    s = sensors.provideSensor("MS" + setter)
    if (s.getUserName() == None) :
        s.setUserName(nodeNamePrefix + " Power Not OK Default")
    s.setAuthoritative(False)
    print "Finished default Power values: " + nodeAddr + " " + nodeNamePrefix
