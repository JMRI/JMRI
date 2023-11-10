#   ForEvolution series accessorys 
#   for CS(Routes only) PM74, DS74, DS74V, SE74 (CV's, OpSw's & Routes) et al
#   Author: Steve Gigiel, copyright 2023, 
#     with bits from LnSendTool.by "Bill Robinson with help from Bob Jacobsen", and other parts of JMRI
#   No Warrenty, use at own risk. Disconnect turnouts before messing with solinoid / pulse options.
#   It takes time to read all 64 CS routes so be patient.
#
import array as arr
import jmri
import java
import sys
from javax.swing import ( JFrame, JTable, RowFilter, Box, BoxLayout, JCheckBox, 
    JScrollPane, JButton, JComboBox, JPanel, JSlider, JLabel, JSpinner, JViewport,
    SpinnerNumberModel, JTabbedPane, JTextField, JSplitPane, JFormattedTextField, DefaultCellEditor )
from javax.swing.text import MaskFormatter
from javax.swing.table import DefaultTableModel
from java.lang import String
from java.awt import ( BorderLayout, FlowLayout, GridLayout, Cursor, GridBagLayout,
    GridBagConstraints, Dimension, PointerInfo,  MouseInfo)

class DeviceAttributes() :
    ColID=0
    ColName=1
    ColCVs=2
    ColNumRoutes=3
    ColOpSws=4
    def __init__(self) :
        self.deviceCVAttributes=[]
        self.deviceCVAttributes.append([0x7C,'DS78V',
                                [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,
                                    [25.0,'output 1 start pos'],[26.0,'end pos'],[27.0,'sig start pos'],[28.0,'sig end pos'],
                                    [25.1,'output 2 start pos'],[26.1,'end pos'],[27.1,'sig start pos'],[28.1,'sig end pos'],
                                    [25.2,'output 3 start pos'],[26.2,'end pos'],[27.2,'sig start pos'],[28.2,'sig end pos'],
                                    [25.3,'output 4 start pos'],[26.3,'end pos'],[27.3,'sig start pos'],[28.3,'sig end pos'],
                                    [25.4,'output 5 start pos'],[26.4,'end pos'],[27.4,'sig start pos'],[28.4,'sig end pos'],
                                    [25.5,'output 6 start pos'],[26.5,'end pos'],[27.5,'sig start pos'],[28.5,'sig end pos'],
                                    [25.6,'output 7 start pos'],[26.6,'end pos'],[27.6,'sig start pos'],[28.6,'sig end pos'],
                                    [25.7,'output 8 start pos'],[26.7,'end pos'],[27.7,'sig start pos'],[28.7,'sig end pos']],
                                16,
                                [
                                [1,'1,2,3,4, TCTT 2 position servo (T/C)'],
                                [2,'1,2,3,4, TCCT 3 position servo, (Semaphore)'],
                                [3,'See Above'],
                                [4,'See Above'],
                                [6,'C =Disable internal Routes'],
                                [10,'C= Ignore standard LocoNet SW commands ("Bushby bit")'],
                                [11,'C= 16 input lines trigger Routes. [NoDSXCP1 support]'],
                                [14,'C = DCC SW commands only'],
                                [15,'C = Do not echo Route SW''s to Loconet'],
                                [40,'C= Factory Reset']
                                ]])
        self.deviceCVAttributes.append([0x74,'DS74',[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],
                                16,
                                [
                                [1,'1,2,3,4, CCCC Pulse Solenoid mode (4 out)'],
                                [2,'1,2,3,4, CTTT Slow motion mode (4 out)'],
                                [3,'1,2,3,4, CTCT Light mode (8 out)'],
                                [4,'See Above'],
                                [6,'C = Disable internal Routes'],
                                [10,'C = Bushby bit active'],
                                [11,'C = 8 input lines trigger Routes'],
                                [14,'C = DCC SW commands only'],
                                [15,'C = Do not echo Route SW''s to Loconet'],
                                [16,'C= Use Capacitive Discharge in Pulse solenoid mode'],
                                [40,'C= Factory Reset']
                                ]])
        self.deviceCVAttributes.append([0x4A,'PM74',[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],
                                0,
                                [
                                [1,'C = Change DS1 to auto-reverse mode'],
                                [2,'C = Change DS2 to auto-reverse mode'],
                                [3,'C = Change DS2 to auto-reverse mode'],
                                [4,'C = Change DS4 to auto-reverse mode'],
                                [7,'C = Lower fault trip current'],
                                [8,'C = Raise fault trip current, overrides Opsw7'],
                                [11,'C= Disable Occupancy Detection'],
                                [12,'C= Disable Transponding Detection'],
                                [31,'C= Increase Occupancy sensitivity'],
                                [40,'C= Factory Reset'],
                                [41,'C= Leds show Relay state, not detection']
                                ]])
        self.deviceCVAttributes.append([0x46,'SE74',[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],
                                8,
                                [[1,'C = Slow motion turnouts, T = pulse/solenoids'],
                                [5,'C =DCC Aspect control mode, T = SW control'],
                                [6,  'C =Disable internal Routes'],
                                [10, 'C = Bushby bit active'],
                                [11, 'C = 8 input lines trigger Routes'],
                                [14, 'C = DCC SW commands only'],
                                [15, 'C = Do not echo Route SW''s to Loconet'],
                                [16, 'C = Solenoid pulse hi CD voltage'],
                                [30, 'C = Send B2 (B1 Output State) not B0 (B2 Sensor) input SW messages'],
                                [33, 'C = 2-wire searchlight signals (figures 8/9)'],
                                [34, 'C = Searchlight, T = 3 color signals'],
                                [35, 'C = Common cathode signals, T=common anode'],
                                [36,'36,37 4th Aspect. TT = Flashing Yellow'],
                                [37,' CT=Flashing Red,TC=Dark CC=Flashing Green'],
                                [38, 'C = Disable SW1021/1022 signal broadcast'],
                                [40, 'C = Set Factory defaults'],
                                [41, 'C = Make Lunar aspects Persistent']
                                ]])
        self.deviceCVAttributes.append([0xFF,'CS',[],64,[]])
        self.defaultCV=([0x00,'<unknown>',[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],16,
                                [
                                [1,'Empty'],
                                [2,'Empty'],
                                [3,'Empty'],
                                [4,'Empty'],
                                [5,'Empty'],
                                [6,'Empty'],
                                [7,'Empty'],
                                [8,'Empty'],
                                [9,'Empty'],
                                [10,'Empty'],
                                [11,'Empty'],
                                [12,'Empty'],
                                [13,'Empty'],
                                [14,'Empty'],
                                [15,'Empty'],
                                [16,'Empty'],
                                [17,'Empty'],
                                [18,'Empty'],
                                [19,'Empty'],
                                [20,'Empty'],
                                [21,'Empty'],
                                [22,'Empty'],
                                [23,'Empty'],
                                [24,'Empty'],
                                [25,'Empty'],
                                [26,'Empty'],
                                [27,'Empty'],
                                [28,'Empty'],
                                [29,'Empty'],
                                [30,'Empty'],
                                [31,'Empty'],
                                [32,'Empty'],
                                [33,'Empty'],
                                [34,'Empty'],
                                [35,'Empty'],
                                [36,'Empty'],
                                [37,'Empty'],
                                [38,'Empty'],
                                [39,'Empty'],
                                [40,'Empty'],
                                [41,'Empty'],
                                [42,'Empty'],
                                [43,'Empty'],
                                [44,'Empty'],
                                [45,'Empty'],
                                [46,'Empty'],
                                [47,'Empty'],
                                [47,'Empty']
                                ]])
        self.deviceCVAttributes.append([0x03,'DS64',[],8,[]])

    def isDeviceNameValid(self,deviceName) :
        for dev in self.deviceCVAttributes :
            if dev[DeviceAttributes.ColName]==deviceName :
                return True    
        return False
    def isDeviceIDValid(self,deviceID) :
        for dev in self.deviceCVAttributes :
            if dev[DeviceAttributes.ColID]==deviceID :
                return True    
        return False
    def getDeviceNameFromID(self,deviceID) :
        for dev in self.deviceCVAttributes :
            if dev[DeviceAttributes.ColID]==deviceID :
                return dev[DeviceAttributes.ColName]   
        return None
    def getCVNumbers(self,deviceName) :
        for dev in self.deviceCVAttributes :
            if dev[DeviceAttributes.ColName]==deviceName :
                return  dev[2]    
        return self.defaultCV
    def getCVNumbersByID(self,deviceID) :
        for dev in self.deviceCVAttributes :
            if dev[DeviceAttributes.ColID]==deviceID :
                return dev[2]    
        return self.defaultCV
    def getNumberOfRoutes(self,deviceName) :
        for dev in self.deviceCVAttributes :
            if dev[DeviceAttributes.ColName]==deviceName :
                return dev[3]    
        return 4
    def getNumberOfRoutesByID(self,deviceID) :
        for dev in self.deviceCVAttributes :
            if dev[DeviceAttributes.ColID]==deviceID :
                return dev[3]    
        return 4
    def getOpSws(self,deviceName) :
        for dev in self.deviceCVAttributes :
            if dev[DeviceAttributes.ColName]==deviceName :
                return dev[DeviceAttributes.ColOpSws]
        return None
    def handle(self) :
        return 0
        
class devicesTableModel( DefaultTableModel ) :

    def __init__( self, headings, rows ) :
        DefaultTableModel.__init__( self, headings, rows)

    def isCellEditable( self, row, col ) :
        if ( col == 3 or col == 4 ) :
            return True
        return False

    def getColumnClass( self, col ) :
        if ( col == 1 or col ==2) :
            return String
        return java.lang.Integer

class routesTableModel( DefaultTableModel ) :

    def __init__( self, headings, rows ) :
        DefaultTableModel.__init__( self, headings, rows)
    def setRowCount(self,rows) :
        DefaultTableModel.setRowCount(self,rows)
    def setValueAt(self,value,row,col) :
        if (col==1) :
            if (value < 0 ) :
                value=0
            if (value > 2044) :
                value = 2044
        DefaultTableModel.setValueAt(self,value,row,col)
        
    def isCellEditable( self, row, col ) :
        if col == 0 :
            return False
        return True

    def getColumnClass( self, col ) :
        if (col == 1) :
            return java.lang.Integer
        return String

class OpSwTableModel(DefaultTableModel) :
    def __init__( self, headings, rows ) :
        DefaultTableModel.__init__( self, headings, rows)

    def isCellEditable( self, row, col ) :
        if ( col == 0 or col == 3 ) :
            return False
        return True
    def setOpSwFromCV(self, cvValue, cvNumber) :
        if (cvNumber < 11 or cvNumber > 16 ) :
            return
        base = cvNumber - 11
        firstCV = (base * 8) + 1
        opSwData = cvValue
        for IX in range (0,8) :
            self.setOpSw(firstCV+IX,opSwData & 0x01 ) 
            opSwData=opSwData >> 1
    def setOpSw(self,CV,tORc) :
        for IX in range(0, self.getRowCount()) :
            if (self.getValueAt(IX,0) == CV) :
                if (tORc) :
                    self.setValueAt('c',IX,1)
                else:
                    self.setValueAt('T',IX,1)
                return
    def getOpSw(self,opSw) :
        for IX in range(0, self.getRowCount()) :
            if (self.getValueAt(IX,0) == opSw) :
                if (self.getValueAt(IX,1)=="c") :
                    return 1
                return 0
        return -1
    def getColumnClass( self, col ) :
        if (col == 0  ) :
            return java.lang.Integer
        return String

class cvTableModel( DefaultTableModel ) :

    def __init__( self, headings, rows ) :
        DefaultTableModel.__init__( self, headings, rows)

    def isCellEditable( self, row, col ) :
        if ( col == 0 or col == 3 ) :
            return True
        return True
    def getCVRow(self,CV) :
        for row in range(0, self.getRowCount()) :
            if (CV == self.getValueAt(row,0)) :
                return row
        return -1
    def setValueAt(self,value,row,col) :
        DefaultTableModel.setValueAt(self,value,row,col)
        if ( col != 1 or value == None or self.getValueAt(row,0) == None) :
            return
        CVValue=int(value)
        if ( CVValue < 0 or CVValue > 255) :
            DefaultTableModel.setValueAt(self,str(value) + ' fouled',row,col)
            return
        CVNo=int(self.getValueAt(row,0))
        if (CVNo >10 and CVNo < 17) :
            OpSwStart = ((CVNo-11) * 8) + 1
            OpSwEnd = OpSwStart + 7
            OpSwStates=''
            if ((CVValue & 0x01) == 0x01 ) :
                OpSwStates=OpSwStates + 'c'
            else :
                OpSwStates=OpSwStates + 'T'
            if (CVValue & 0x02 == 0x02 ) :
                OpSwStates=OpSwStates + 'c'
            else :
                OpSwStates=OpSwStates + 'T'
            if (CVValue & 0x04 == 0x04 ) :
                OpSwStates=OpSwStates + 'c'
            else :
                OpSwStates=OpSwStates + 'T'
            if (CVValue & 0x08 == 0x08 ) :
                OpSwStates=OpSwStates + 'c'
            else :
                OpSwStates=OpSwStates + 'T'
            if (CVValue & 0x10 == 0x10 ) :
                OpSwStates=OpSwStates + 'c'
            else :
                OpSwStates=OpSwStates + 'T'
            if (CVValue & 0x20 == 0x20 ) :
                OpSwStates=OpSwStates + 'c'
            else :
                OpSwStates=OpSwStates + 'T'
            if (CVValue & 0x40 == 0x40 ) :
                OpSwStates=OpSwStates + 'c'
            else :
                OpSwStates=OpSwStates + 'T'
            if (CVValue & 0x80 == 0x80 ) :
                OpSwStates=OpSwStates + 'c'
            else :
                OpSwStates=OpSwStates + 'T'
            DefaultTableModel.setValueAt(self,'OpSw ' + str(OpSwStart) + ' thru ' + str(OpSwEnd) + ' ' + OpSwStates,row,3)
    def getColumnClass( self, col ) :
        if (col == 0  ) :
            return java.lang.Float
        if (col == 1  ) :
            return java.lang.Integer
        if (col == 2) :
            return java.lang.Boolean
        return String

class AllRoutesTable() :
    colID=0
    colType=1
    colSerial=2
    colRte=3
    colEnt=4
    colSwitch=5
    colTorC=6
    dataDict={}
    def put(self, devID, devType, devSerial, devRte, devEntry, Switch, tOrC) :
        key1=str(devID)+str(devType)+str(devSerial)
        key2=key1+str(devRte)+str(devEntry)
        AllRoutesTable.dataDict[key2]=[devID, devType, devSerial, 0, 0, 0, 0]
        if (Switch != 0) :
            AllRoutesTable.dataDict[key2]=[devID, devType, devSerial, devRte, devEntry, Switch, tOrC]
        else :
            del  AllRoutesTable.dataDict[key2]
    def get(self, devID, devType, devSerial, devRte, devEntry) :
        key=str(devID)+str(devType)+str(devSerial)+str(devRte)+str(devEntry)
        return AllRoutesTable.dataDict.value(key)
    def getList(self) :
        tList=[]
        for devID, devType, devSerial, devRte, devEntry, Switch, tOrC in AllRoutesTable.dataDict.values() :
            tList.append([devID, devType, devSerial, devRte, devEntry, Switch, tOrC])
        return sorted(tList)
            
class DevicesResponseListener(jmri.jmrix.loconet.LocoNetListener) :
    def __init__(self,identInfo,tableDataModel) :
      self.identityInfo  = identInfo
      self.devicesDataModel  = tableDataModel
    def message(self, msg) :
        if msg.getElement(0)==0xD0 and msg.getElement(1) == 0x62 :
            # Ancient board
            if (msg.getElement(3) & 0x07) ==0x03 :
                deviceType = 'DS64'
                self.devCVs = DeviceAttributes()
                if (not self.devCVs.isDeviceNameValid(deviceType)) :
                    if (not self.devCVs.isDeviceIDValid(msg.getElement(9))) :
                        print ('Invalid Name', deviceType , 'and Id', hex(msg.getElement(3) & 0x07 ==0x03 ))
                        return
                rtes=self.devCVs.getNumberOfRoutes(deviceType)
                Id = (msg.getElement(2) + 1) + ((msg.getElement(1) & 0x1) * 128) 
                serialNum = msg.getElement(4)                # Version
                tData=[0xFE,Id,serialNum,0xFF,0xFF,0xFF]  # invent One
                # Skip duplicates
                for ident in self.identityInfo :
                    if ident == tData :
                        return
                self.identityInfo.append(tData)
                PentriesPerRte = 8
                self.devicesDataModel.addRow([Id,deviceType,str(serialNum) + ' (' + hex(serialNum) + ')', PentriesPerRte, rtes])
                return
        if msg.getElement(0)==0xe6 : 
            if msg.getElement(1)==0x10 and ( msg.getElement(2) == 0x02 or msg.getElement(2) == 0x01) and msg.getElement(3) == 0x00:
                if msg.getElement(2)==0x01 :
                    # Command station
                    deviceType = 'CS'
                    self.devCVs = DeviceAttributes()
                    if (not self.devCVs.isDeviceNameValid(deviceType)) :
                        print ('Invalid CS Name', deviceType , 'and Id', hex(msg.getElement(9)))
                        return
                    rtes=self.devCVs.getNumberOfRoutes(deviceType)
                    tData=[]
                    for iX in range(0,6) : 
                        tData.append(0)
                    # Skip duplicates
                    for ident in self.identityInfo :
                        if ident == tData :
                            return
                    self.identityInfo.append(tData)
                    PentriesPerRte = 16
                else :
                    deviceType = jmri.jmrix.loconet.LnConstants.IPL_NAME(msg.getElement(9))
                    self.devCVs = DeviceAttributes()
                    if (not self.devCVs.isDeviceNameValid(deviceType)) :
                        if (not self.devCVs.isDeviceIDValid(msg.getElement(9))) :
                            print ('Invalid Name', deviceType , 'and Id', hex(msg.getElement(9)))
                            return
                        deviceType = self.devCVs.getDeviceNameFromID(msg.getElement(9))
                        rtes=self.devCVs.getNumberOfRoutesByID(msg.getElement(9))
                    else :
                        rtes=self.devCVs.getNumberOfRoutes(deviceType)
                    tData=[]
                    for iX in range(9,15) :
                        tData.append(msg.getElement(iX))
                    # Skip duplicates
                    for ident in self.identityInfo :
                        if ident == tData :
                            return
                    self.identityInfo.append(tData)
                    PentriesPerRte = msg.getElement(8)
                serialNum = msg.getElement(11) + (msg.getElement(12)<<7)
                Id= msg.getElement(13) + (msg.getElement(14)<<7) + 1
                self.devicesDataModel.addRow([Id,deviceType,str(serialNum) + ' (' + hex(serialNum) + ')', PentriesPerRte, rtes])
       
class RtesResponseListener(jmri.jmrix.loconet.LocoNetListener) :
    def __init__(self, routesDataModel, numRte, entriesPerRte, masterRouteTable, devID, devType, devSerial) :
        self.numRte = numRte
        self.entriesPerRte = entriesPerRte
        self.recordsPerRte = entriesPerRte / 4   # Four are returned in each record.
        self.routesDataModel = routesDataModel
        self.devID = devID
        self.devType = devType
        self.devSerial = devSerial
        self.masterRouteTable = masterRouteTable
    def message(self, msg) :
        #if msg.getElement(0)==0xE6 and msg.getElement(1)==0x10 and msg.getElement(2) == 0x02 and msg.getElement(3) == 0x02:
        if msg.getElement(0)==0xE6 and msg.getElement(1)==0x10 and msg.getElement(3) == 0x02: #also CS station responses
            entry =  msg.getElement(4) + (msg.getElement(5) * 128) # may be masked?
            rteNumber = int(entry / self.recordsPerRte) + 1
            startEnt = ( entry % self.recordsPerRte ) * 4
            for IX in range(0,4) :
                tTNum=self.getTurnoutNum(msg,IX)
                tTState=self.getTurnoutState(msg,IX)
                self.routesDataModel[rteNumber].setValueAt(tTNum,IX+startEnt,1)
                self.routesDataModel[rteNumber].setValueAt(tTState,IX+startEnt,2)
                self.masterRouteTable.put(self.devID, self.devType, self.devSerial, rteNumber, IX+startEnt, tTNum, tTState)
            self.routesDataModel[rteNumber].fireTableDataChanged()

    def getTurnoutNum(self, msg, turnoutEntry) :
        if ((msg.getElement(7+(turnoutEntry*2)) == 0x7f) and (msg.getElement(8+(turnoutEntry*2)) == 0x7f)) :
            return 0
        val = 1 + msg.getElement(7+(turnoutEntry*2)) + ((msg.getElement(8+(turnoutEntry*2)) & 15)<<7)
        return int(val)

    def getTurnoutState(self,msg, turnoutEntry) :
        if ((msg.getElement(7+(turnoutEntry*2)) == 0x7f) and (msg.getElement(8+(turnoutEntry*2)) == 0x7f)) :
            return '';
        if (msg.getElement(8 + (turnoutEntry*2)) & 0x20) == 0x20 :
            return 'c'
        return 'T'

    def getTurnoutDisplayValue(self, msg, turnoutEntry) :
       return str(self.getTurnoutNum(msg, turnoutEntry)) + self.getTurnoutState(msg, turnoutEntry)

class OldRtesResponseListener(jmri.jmrix.loconet.LocoNetListener) :
    WRITE=1
    READ=0
    OFF=-1
    def __init__(self) :
        self.setListening(False)
    def setListening(self,value) :
        self.isListening=value
        self.response = -1
    def getResponse(self) :
        return self.response
    def message(self, msg) :
        if (self.isListening == OldRtesResponseListener.READ) :
            if msg.getElement(0)==0xB4 and msg.getElement(1)==0x50 :
                if (msg.getElement(2) & 0x20) == 0x20 :
                    self.response = 1
                else :
                    self.response = 0
        if (self.isListening == OldRtesResponseListener.WRITE) :
            if msg.getElement(0)==0xB4 and msg.getElement(1)==0x50 and msg.getElement(2)==0x7F :
                self.response = 0
            
    

class CVResponseListener(jmri.jmrix.loconet.LocoNetListener) :
    def __init__(self) :
        self.listening=False
        self.response=-1 
        self.accepted=False
    def setListen(self, listening) :
        self.listening=listening
        self.response=-1 
        self.accepted=False
    def getResponse(self) :
        return self.response
    def message(self, msg) :
        if (self.listening) :
            if (not self.accepted) :
                if msg.getElement(0) == 0xB4 and msg.getElement(1) == 0x6D:
                    self.accepted=True
            else :
                if ( msg.getElement(0) == 0xB4 and (msg.getElement(1) & 0x6C )== 0x6C ):
                    self.response =  msg.getElement(2) + (( msg.getElement(1) & 0x01 ) * 128)
        
 
class queryRteDevices(jmri.jmrit.automat.AbstractAutomaton) :
    def __init__(self, senderA, identityInfo, devicesTableModel ) :
        self.senderB = senderA
        self.identityInfo = identityInfo
        self.listn = DevicesResponseListener(self.identityInfo, devicesTableModel)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().addLocoNetListener(0xFF,self.listn)

    def handle(self) :
        try :
            # New Devices
            lnMsg = jmri.jmrix.loconet.LocoNetMessage([ 0xEE, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ])
            self.senderB.sendLocoNetMessage(lnMsg)
            self.waitMsec(1000)
            #Old Devices
            lnMsg = jmri.jmrix.loconet.LocoNetMessage([ 0xD0, 0x62, 0x00, 0x70, 0x00, 0x3D ])
            self.senderB.sendLocoNetMessage(lnMsg)
            self.waitMsec(1000)
            
        except Exception as e :
            exc_type, exc_obj, exc_tb = sys.exc_info()
            print('queryRteDevices-Fatal',exc_tb.tb_lineno,e)

        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().removeLocoNetListener(0xFF,self.listn)
        return 0

class ReadRouteDetails(jmri.jmrit.automat.AbstractAutomaton) :
    def __init__(self, senderA, identityInfo, routesDataModel, numOfRoutes, entriesPerRte, singleRte, masterRouteTable, devID, devType, devSerial) :
        self.senderB = senderA
        self.numOfRoutes = numOfRoutes
        self.entriesPerRte = entriesPerRte
        self.rteNumber = singleRte
        self.identityInfo = identityInfo
        self.routesDataModel = routesDataModel
        self.listn = RtesResponseListener(self.routesDataModel, self.numOfRoutes, 
                            self.entriesPerRte, masterRouteTable, devID, devType, devSerial)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().addLocoNetListener(0xFF,self.listn)

    def readOneRte(self, lnMsg, IX) :
        loBits = IX & 0x7F
        hiBits = int(IX/128)
        lnMsg.setElement(4,loBits)
        lnMsg.setElement(5,hiBits)
        self.senderB.sendLocoNetMessage(lnMsg)
        self.waitMsec(100)

    def getStatus(self) :
        return self.status
        
    def handle(self) :
        self.isCS = False
        try :
            if (self.identityInfo == [0,0,0,0,0,0] ) :
                self.isCS = True
                lnMsg = jmri.jmrix.loconet.LocoNetMessage([ 0xEE, 0x10, 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ])
            else :
                lnMsg = jmri.jmrix.loconet.LocoNetMessage([ 0xEE, 0x10, 0x02, 0x0E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ])
                for iX in range (0,len(self.identityInfo)) :
                    lnMsg.setElement(9 + iX, self.identityInfo[iX])
                self.senderB.sendLocoNetMessage(lnMsg)
                self.waitMsec(100)
                lnMsg = jmri.jmrix.loconet.LocoNetMessage([ 0xEE, 0x10, 0x02, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ])
            if (self.rteNumber == -1) : # readall
                for IX in range(0,self.numOfRoutes*(self.entriesPerRte/4)) :
                    self.readOneRte(lnMsg, IX)
            else :
                startIX = ( self.rteNumber - 1) * (self.entriesPerRte/4)
                endIX = startIX + (self.entriesPerRte/4)
                for IX in range ( startIX, endIX ) :
                    self.readOneRte(lnMsg, IX)
            # deselect
            if (not self.isCS) :
                lnMsg = jmri.jmrix.loconet.LocoNetMessage([ 0xEE, 0x10, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ])
                self.senderB.sendLocoNetMessage(lnMsg)
                self.waitMsec(100)
        except Exception as e :
            exc_type, exc_obj, exc_tb = sys.exc_info()
            print('ReadRouteDetails-Fatal',exc_tb.tb_lineno,e)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().removeLocoNetListener(0xFF,self.listn)
        return 0
class ReadOldRouteDetails(jmri.jmrit.automat.AbstractAutomaton) :
    def __init__(self, senderA, routesDataModel, numOfRoutes, entriesPerRte, singleRte, masterRouteTable, devId, devType, devSerial) :
        self.senderB = senderA
        self.numOfRoutes = numOfRoutes
        self.entriesPerRte = entriesPerRte
        self.rteNumber = singleRte
        self.devId = devId
        self.devType = devType
        self.routesDataModel = routesDataModel
        self.masterRouteTable = masterRouteTable
        self.devSerial = devSerial
        self.listn = OldRtesResponseListener()
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().addLocoNetListener(0xFF,self.listn)

    def setDS64RteIndex(self,rteIndex) :
        indexs=[]
        base=25
        bit=0x01
        for IX in range(0,8) :
            if ( ( rteIndex & bit ) == bit ) :
                self.readOneOpSw(base+IX ,1, True) 
            else :
                self.readOneOpSw(base+IX ,0, True) 
            bit=bit<<1

    def readOneOpSw(self,opSw, value, write) :
        msg=jmri.jmrix.loconet.LocoNetMessage([0xD0, 0x62, 0x04, 0x73, 0x5A, 0x00]);
        el1 = 0x62
        loDevId = self.devId & 0x7F
        hiDevID = int(self.devId / 128)
        el1 = el1 | hiDevID
        msg.setElement(2,loDevId)
        msg.setElement(3, 0x73);  #only DS64
        loc = (opSw - 1) / 8;
        bit = (opSw - 1) - loc * 8;
        msg.setElement(4, loc * 16 + bit * 2 + value);
        if write :
            el1 = el1 | 0x10
            self.listn.setListening(OldRtesResponseListener.WRITE)
        else :
            self.listn.setListening(OldRtesResponseListener.READ)
        msg.setElement(1, el1)
        self.senderB.sendLocoNetMessage(msg)
        tCount = 10
        while tCount > 0 and self.listn.getResponse() == -1 :
            tCount-=1
            self.waitMsec(10)
        resp = self.listn.getResponse()
        self.listn.setListening(OldRtesResponseListener.OFF)
        return resp
    def readOneRoute(self, rteNum) :
        IX1= 0
        IX = 16 + (4 * (rteNum-1))
        for IX2 in range (IX,IX+4) :
            self.setDS64RteIndex(IX2)
            switchData=[]
            for IX3 in range(33,50,16) :
                word=0
                for IX4 in range(0,16) :
                    bit=self.readOneOpSw(IX3+IX4,0,False)
                    if bit == 1 :
                        word = word | (0x01 << IX4)
                        #print (hex(bit),hex(word),IX4,(0x01 << IX4))
                        #print ("IX2",IX2,"IX3",IX3,"IX4",IX4)
                switchData.append(word)
                for switch in  switchData :
                    if ((switch & 0xC000) == 0xC000 ) :
                        tTState=""
                        tTNum=0
                    else :
                        tTNum = (switch & 0x7F) + (( switch & 0x0F00) * 128)  + 1 
                        if ((switch & 0x2000 ) == 0x2000) :
                            tTState = "C"
                        else :
                            tTState = "T"
                    
                self.routesDataModel[rteNum].setValueAt(tTNum, IX1,1)
                self.routesDataModel[rteNum].setValueAt(tTState,IX1,2)
                self.masterRouteTable.put(self.devId, self.devType, self.devSerial, rteNum, IX1, tTNum, tTState)
                self.routesDataModel[self.rteNumber].fireTableDataChanged()
                IX1+=1
                    
    def handle(self) :
        try :
            if self.rteNumber == -1 :
                for IX in range(0,8) :
                     self.readOneRoute(IX)
            else :
                self.readOneRoute(self.rteNumber)
            
        except Exception as e :
            exc_type, exc_obj, exc_tb = sys.exc_info()
            print('ReadOldRouteDetails-Fatal',exc_tb.tb_lineno,e)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().removeLocoNetListener(0xFF,self.listn)
        return 0

class WriteRouteDetails(jmri.jmrit.automat.AbstractAutomaton) :
    def __init__(self, senderA, identityInfo, routesDataModel, numOfRoutes, entriesPerRte,
                    singleRte, masterRouteTable, devID, devType, devSerial) :
        self.senderB = senderA
        self.numOfRoutes = numOfRoutes
        self.entriesPerRte = entriesPerRte
        self.rteNumber = singleRte
        self.identityInfo = identityInfo
        self.routesDataModel = routesDataModel
        self.routeEntrys=[]
        for IX in range(0,self.entriesPerRte,4) :
            routeEntry=[]
            for IY in range (0,4) :
                self.setTurnout(routeEntry,self.routesDataModel.getValueAt(IX+IY,1),self.routesDataModel.getValueAt(IX+IY,2))
            self.routeEntrys.append(routeEntry)
        self.listn = RtesResponseListener(self.routesDataModel, self.numOfRoutes, self.entriesPerRte,
                     masterRouteTable, devID, devType, devSerial)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().addLocoNetListener(0xFF,self.listn)
        self.isCS = False
        if (self.identityInfo == [0,0,0,0,0,0] ) :
            self.isCS = True
    
    def setTurnout(self, lnHexString, turnOutNumber, turnOutTc) :
        if (turnOutNumber < 1 or turnOutTc == None) :
            lnHexString.append(0x7F)
            lnHexString.append(0x7F)
        else :
            toNum = turnOutNumber - 1
            toHi = int(toNum / 128)
            toLo = toNum - ( toHi * 128 )
            if ( turnOutTc == 'c' ) :
                toHi = toHi | 0x20
            toHi = toHi | 0x10     # bit is always seen on, no idea why
            lnHexString.append(toLo)
            lnHexString.append(toHi)

    def writeOneRte(self, IX, toS ) :
        if (self.isCS) :
            lnMsg = jmri.jmrix.loconet.LocoNetMessage(
                   [ 0xEE, 0x10, 0x01, 0x03, 0x00,
                     0x00, 0x00, 0x7F, 0x7F, 0x7F,
                     0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
                     0x00])
        else :
            lnMsg=jmri.jmrix.loconet.LocoNetMessage(
                   [ 0xEE, 0x10, 0x02, 0x03, 0x00, 
                     0x00, 0x00, 0x7F, 0x7F, 0x7F,
                     0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
                     0x00])
        loBits = IX & 0x7F
        hiBits = int(IX/128)
        lnMsg.setElement(4,loBits)
        lnMsg.setElement(5,hiBits)
        for IY in range(7, 15) :
            lnMsg.setElement(IY,toS[IY-7])
        self.senderB.sendLocoNetMessage(lnMsg)
        self.waitMsec(100)

    def handle(self) :
        try :
            if ( not self.isCS) :
                lnMsg = jmri.jmrix.loconet.LocoNetMessage([ 0xEE, 0x10, 0x02, 0x0E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ])
                for iX in range (0,6) :
                    lnMsg.setElement(9 + iX, self.identityInfo[iX])
                self.senderB.sendLocoNetMessage(lnMsg)
                self.waitMsec(100)
            startEntry = ( self.rteNumber - 1) * (self.entriesPerRte/4)
            endIX = startEntry + (self.entriesPerRte/4)
            entryNumber = startEntry
            IX=0
            for entry in self.routeEntrys :
                self.writeOneRte(startEntry+IX, entry) 
                IX+=1
            if ( not self.isCS) :
                # deselect
                lnMsg = jmri.jmrix.loconet.LocoNetMessage([ 0xEE, 0x10, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ])
                self.senderB.sendLocoNetMessage(lnMsg)
                self.waitMsec(100)
        except Exception as e :
            exc_type, exc_obj, exc_tb = sys.exc_info()
            print('WriteRouteDetails-Fatal',exc_tb.tb_lineno,e)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().removeLocoNetListener(0xFF,self.listn)
        return 0

class ReadWriteCVDetails(jmri.jmrit.automat.AbstractAutomaton) :
    def __init__(self, senderA, accessoryID, write, cvsDataModel, opSwDataModel, singleCVs,newValues) :
        self.senderA = senderA
        self.accID = accessoryID - 1 # Digitrax Accessories start from 0
        self.listn = CVResponseListener()
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().addLocoNetListener(0xFF,self.listn)
        self.write = write
        self.cvsDataModel=cvsDataModel
        self.opSwDataModel=opSwDataModel
        self.singleCVs=singleCVs
        self.newValues=newValues
 
    def sendMessage(self, cvNumber, cvValue, write) :
        # Digitrax accessory config packet - 10AAAAAA 0 1AAA1aa0 0 1110CWVV VVVVVVVV 0 DDDDDDDD 0 EEEEEEEE
        # a - two low bit of the address
        # A - address, C 0 read 1 write, 
        cvInt = int(cvNumber)
        decAdj = round( (float(cvNumber) - float(cvInt)) , 1 )
        if ( decAdj  > 0.0 ) :
            cvAddAddr = int(decAdj * 10.0)
        else :
            cvAddAddr = 0
        addr = int(self.accID + cvAddAddr) # - 2      
        print ( cvNumber, cvInt, cvAddAddr, decAdj, addr )
        toLsb = ( addr & 0x03 ) * 2      # extract bits 1,0 for second byte 0xxx0AA0
        addr = addr / 4        # shift down by two
        dhi = 1                # bit 7 of im1 is a one (10AAAAAA)
        im1 = addr & 0x3F      # extract six bits 7-2 for first byte 10AAAAAA
        im2 = addr / 64        # shift down by 6 bits
        im2 =  7 - im2         # invert the three bits by subtracting from seven
        im2 = im2 * 16         # shift up by 5 bits
        im2 = im2 + toLsb   + 8 
        dhi +=2
        if write :
             WR=8
        else :
             WR=0
        im3 = int(228) + WR
        if im3 > 127 : 
            im3 = im3 - 128
            dhi = dhi + 4
        im4 = int(cvNumber) - 1 
        if im4 > 127 : 
            im4 = im4 - 128
            dhi = dhi + 8
        im5 = int(cvValue)     
        if im5 > 127 : 
            im5 = im5 - 128
            dhi = dhi + 16
        numOfBytes = 5
        repeatCount = 4
        reps = repeatCount + (numOfBytes * 16)   # set length
        lnMsg = jmri.jmrix.loconet.LocoNetMessage([0xED,0x0B,0x7F,reps,dhi,im1,im2,im3,im4,im5,0])
        self.senderA.sendLocoNetMessage(lnMsg)
    def handle(self) :
        try :
            for IY in range(0,len(self.singleCVs)) :
                if (self.singleCVs[IY] == -1) :
                    for IX in range(0,self.cvsDataModel.getRowCount()) :
                        self.getSingleCV(self.cvsDataModel.getValueAt(IX,0),0,False)
                else :
                    self.getSingleCV(self.singleCVs[IY],self.newValues[IY],self.write)
        except Exception as e:
             exc_type, exc_obj, exc_tb = sys.exc_info()
             print('ReadWriteCVDetails-Fatal',exc_tb.tb_lineno,e)
        jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController().removeLocoNetListener(0xFF,self.listn)

    def getSingleCV(self, CV, value, write) :
        # get the grid row for output
        CVRow =self.cvsDataModel.getCVRow(CV)
        if (CVRow == -1 ) :
            return
        self.listn.setListen(True)
        if (write) :
            self.sendMessage(CV, value, write)
        else :
            self.sendMessage(CV,0,write)
        iLimit=20
        while self.listn.getResponse() == -1 and iLimit > 0 :
            self.waitMsec(100)
            iLimit-=1
        if write :
            if (self.listn.getResponse() != 218) :
                self.cvsDataModel.setValueAt('Maybe a Bad Write',CVRow,3)
        else :
            self.cvsDataModel.setValueAt(CV,CVRow,0)
            self.cvsDataModel.setValueAt(self.listn.getResponse(),CVRow,1)
            self.cvsDataModel.setValueAt(False,CVRow,2)
            self.opSwDataModel.setOpSwFromCV(self.listn.getResponse(),CV)
        self.listn.setListen(False)
        self.cvsDataModel.fireTableDataChanged()

# run one or two gui free processes sequentially
class HeavyLifter(jmri.jmrit.automat.AbstractAutomaton) :
    def __init__(self, frame, processA, processB) :
        self.processA = processA
        self.processB = processB
        self.frame = frame
    def handle(self) :
        if (self.frame != None) :
            self.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if ( self.processA != None ) :
           self.processA.start()
           self.waitMsec(100)
           while  ( self.processA.isRunning() ) :
               self.waitMsec(100)
        if ( self.processB != None ) :
           self.processB.start()
           self.waitMsec(100)
           while  ( self.processB.isRunning() ) :
               self.waitMsec(100)
        if (self.frame!=None) :
            self.frame.setCursor(Cursor.getDefaultCursor());
        
class LnRoutesCVOps(jmri.jmrit.automat.AbstractAutomaton) :
# WindowListener is a interface class and therefore all of it's
# methods should be implemented even if not used
    class WinListener(java.awt.event.WindowListener):
        f = None
        cleanUp = None

        def setCallBack(self, fr, c):
            self.f = fr
            self.cleanUp = c
            return
        
        def windowClosing(self, event):
            if (self.cleanUp != None) :
                self.cleanUp()
            self.f.dispose()      
            return
            
        def windowActivated(self,event):
            return

        def windowDeactivated(self,event):
            return

        def windowOpened(self,event):
            return

        def windowClosed(self,event):
            return
            
        def windowIconified(self, event):
            return
            
        def windowDeiconified(self, event):
            return

    def init(self) :
        w = self.WinListener()
        self.f = JFrame('Evolution Route And Stationary Programmer')
        testLabel=JLabel('WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW')
        fSize=Dimension(testLabel.getPreferredSize().width,testLabel.getPreferredSize().height*40)
        self.f.setPreferredSize(fSize)
        self.f.setLocation(MouseInfo.getPointerInfo().getLocation())
        self.f.addWindowListener(w)
        w.setCallBack(self.f, self.wrapUp)

        self.f.setLayout(BorderLayout())

     # The Devices
        self.deviceColNames = ('ID', 'Device', 'Serial', 'Ent/Rte', 'Routes', 'Rtes Read')
        self.deviceTable = JTable()
        self.devicesDataModel = devicesTableModel( self.deviceColNames, 0)
        self.deviceTable.setModel(self.devicesDataModel)
        self.deviceTable.setAutoCreateRowSorter(False)
        self.devicesDataModel.fireTableDataChanged()
        self.devicesPanel = JPanel(BorderLayout())
        self.devicesPanel.add(JScrollPane(self.deviceTable), BorderLayout.CENTER);
        #their identities
        self.identityInfo = []
     # Their CVs
        self.cvColNames = ('cv#', 'Value', 'Check','Note')
        self.cvTable = JTable()
        self.cvsDataModel=cvTableModel(self.cvColNames,0)
        self.cvsDataModel.setRowCount(44)
        self.cvTable.setModel(self.cvsDataModel)
        self.cvTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        self.cvTable.getColumnModel().getColumn(0).setMaxWidth(JLabel('WWWWWWWWW').getPreferredSize().width)
        self.cvTable.getColumnModel().getColumn(0).setPreferredWidth(JLabel('WWWWW').getPreferredSize().width)
        self.cvTable.getColumnModel().getColumn(1).setMaxWidth(JLabel('WWWWWWWWW').getPreferredSize().width)
        self.cvTable.getColumnModel().getColumn(1).setPreferredWidth(JLabel('WWWWWWW').getPreferredSize().width)
        self.cvTable.getColumnModel().getColumn(2).setMaxWidth(JLabel('WWWWWWWWW').getPreferredSize().width)
        self.cvTable.getColumnModel().getColumn(2).setPreferredWidth(JLabel('WWWWWWW').getPreferredSize().width)
        self.cvTable.getColumnModel().getColumn(3).setPreferredWidth(-1)
        self.cvsDataModel.fireTableDataChanged()
        self.cvsPanel = JPanel(BorderLayout()) 
        self.cvsPanel.add(JScrollPane(self.cvTable), BorderLayout.CENTER)
        self.cvsPanelBottom=JPanel(FlowLayout())
        self.readCheckedCVsButton=JButton('Read Checked')
        self.readCheckedCVsButton.actionPerformed = self.readCheckedCVs
        self.writeCheckedCVsButton=JButton('Write Checked')
        self.writeCheckedCVsButton.actionPerformed = self.writeCheckedCVs
        self.cvsPanelBottom.add(self.readCheckedCVsButton)
        self.cvsPanelBottom.add(self.writeCheckedCVsButton)
        self.cvsPanel.add(self.cvsPanelBottom,BorderLayout.SOUTH)
        
        self.thrownOrClosed=JComboBox()
        self.thrownOrClosed.addItem('c')
        self.thrownOrClosed.addItem('T')
        #Their Op Switchs
        self.opSwColNames = ('OpSw#', 'T/c', 'Note')
        self.opSwTable = JTable()
        self.opSwDataModel=OpSwTableModel(self.opSwColNames,0)
        self.opSwTable.setModel(self.opSwDataModel)
        self.opSwTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        self.opSwTable.getColumnModel().getColumn(0).setMaxWidth(JTextField('WWOpSw#WW').getPreferredSize().width)
        self.opSwTable.getColumnModel().getColumn(0).setPreferredWidth(JTextField('WOpSw#W').getPreferredSize().width)
        self.opSwTable.getColumnModel().getColumn(1).setMaxWidth(JTextField('WWWC/tWWW').getPreferredSize().width)
        self.opSwTable.getColumnModel().getColumn(1).setCellEditor(DefaultCellEditor(self.thrownOrClosed))
        self.opSwTable.getColumnModel().getColumn(2).setPreferredWidth(-1)
        self.opSwDataModel.fireTableDataChanged()
        self.opSwPanel = JPanel(BorderLayout()) 
        self.opSwPanel.add(JScrollPane(self.opSwTable), BorderLayout.CENTER)
        self.opSwPanelBottom=JPanel(FlowLayout())
        self.readOpSwButton=JButton('Read')
        self.readOpSwButton.actionPerformed = self.readOpSw
        self.writeOpSwButton=JButton('Write')
        self.writeOpSwButton.actionPerformed = self.writeOpSw
        self.opSwPanelBottom.add(self.readOpSwButton)
        self.opSwPanelBottom.add(self.writeOpSwButton)
        self.opSwPanel.add(self.opSwPanelBottom,BorderLayout.SOUTH)
        
        # The Tabs
        self.deviceDetailsTabs = JTabbedPane()

        self.readAllButton = JButton('Refresh Devices')
        self.readAllButton.actionPerformed = self.refreshDevices
        self.exportAllButton = JButton('Export Rts Read sofar')
        self.exportAllButton.actionPerformed = self.exportAllRoutes
        # The master route table
        self.masterRouteTable=AllRoutesTable()
        # hold rtes initialize before requests
        self.rteColNames = (' T ','Turnout#', 'C/T')
        self.routesTable=[]
        self.routesDataModel=[]
        self.readRteButtons=[]
        self.writeRteButtons=[]
        self.deleteRteButtons=[]
        self.statusRteLabels=[]
        self.thrownOrClosed=JComboBox()
        self.thrownOrClosed.addItem('c')
        self.thrownOrClosed.addItem('T')
        for IX in range(0,65) :
            self.routesTable.append(JTable())
            self.routesDataModel.append(routesTableModel( self.rteColNames, 8))
            self.routesTable[IX].setModel(self.routesDataModel[IX])
            self.routesTable[IX].setAutoCreateRowSorter(True)
            self.routesTable[IX].getColumnModel().getColumn(2).setCellEditor(DefaultCellEditor(self.thrownOrClosed));
            self.statusRteLabels.append(JLabel(''))
            self.readRteButtons.append(JButton('Read'))
            self.readRteButtons[IX].setName(str(IX))
            self.readRteButtons[IX].actionPerformed=self.readIndividualRoute
            self.writeRteButtons.append(JButton('Write'))
            self.writeRteButtons[IX].setName(str(IX))
            self.writeRteButtons[IX].actionPerformed=self.writeIndividualRoute
            self.deleteRteButtons.append(JButton('Delete Rte'))
            self.deleteRteButtons[IX].setName(str(IX))
            self.deleteRteButtons[IX].actionPerformed=self.deleteIndividualRoute
            self.routesDataModel[IX].fireTableDataChanged()
        self.cvDetailPanel=JPanel()
        self.rteDetailPanel=[]
        # 64 routes
        for IX in range(0,65) :   
            self.rteDetailPanel.append(JPanel())
        # Tab 0 is the CVs
        self.deviceDetailsTabs.addTab('CVs',self.cvsPanel)
        # Tab 1 is OpSws
        self.deviceDetailsTabs.addTab('OpSw',self.opSwPanel)

        self.deviceTable.mouseClicked = self.gridClicked

        panelButtons = JPanel(FlowLayout(FlowLayout.CENTER))
        panelButtons = JPanel(GridLayout(0,3))
        panelButtons.add(self.readAllButton)
        panelButtons.add(self.exportAllButton)

        self.f.add(panelButtons, BorderLayout.NORTH)
        
        self.deviceScrollTable = JScrollPane(self.deviceTable)
        self.deviceScrollTable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        self.deviceScrollTable.getViewport().setView((self.deviceTable))
        
        self.devicesPanel.add(self.deviceScrollTable)
        self.deviceDetailsTabs.addTab('CV',JLabel('CV'))
        
        splitCentrePane =JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               self.devicesPanel, self.deviceDetailsTabs)
        splitCentrePane.setOneTouchExpandable(True);
        splitCentrePane.setDividerLocation(150);
        
        self.f.add(splitCentrePane, BorderLayout.CENTER)
        self.f.pack()
        self.sender = jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0).getLnTrafficController()
        self.f.show()
        
        self.refreshDevices(None)

    def wrapUp(self) :
        print('Bye')

    def readCheckedCVs(self,event) :
        row = self.deviceTable.getSelectedRow()
        accid = self.devicesDataModel.getValueAt(row,0)

        cvList=[]
        cvValuesList=[]
        for IX in range(0,self.cvsDataModel.getRowCount()) :
            if self.cvsDataModel.getValueAt(IX,2) == True :
                cvList.append(self.cvsDataModel.getValueAt(IX,0))
                cvValuesList.append(self.cvsDataModel.getValueAt(IX,1))
        b = ReadWriteCVDetails(self.sender, accid, False, self.cvsDataModel, self.opSwDataModel, cvList, cvValuesList )
        b.setName('readCheckedCVs')
        b.start()
         
    def writeCheckedCVs(self,event) :
        row = self.deviceTable.getSelectedRow()
        accid = self.devicesDataModel.getValueAt(row,0)

        cvList=[]
        cvValuesList=[]
        for IX in range(0,self.cvsDataModel.getRowCount()) :
            if ( self.cvsDataModel.getValueAt(IX,2) == True ) :
                cvList.append(self.cvsDataModel.getValueAt(IX,0))
                cvValuesList.append(self.cvsDataModel.getValueAt(IX,1))
        b = ReadWriteCVDetails(self.sender,accid, True, self.cvsDataModel, self.opSwDataModel, cvList, cvValuesList )
        b.setName('WriteCheckedCVs')
        b.start()
        
    def readOpSw(self,event) :
        row = self.deviceTable.getSelectedRow()
        accid = self.devicesDataModel.getValueAt(row,0)
        if (self.cvsDataModel.getRowCount() > 5) :
            b = ReadWriteCVDetails(self.sender,accid, False, self.cvsDataModel, self.opSwDataModel, [11,12,13,14,15,16], [0,0,0,0,0,0] )
            b.setName('readOpSw')
            b.start()
        return
        
    def writeOpSw(self,event) :
        row = self.deviceTable.getSelectedRow()
        accid = self.devicesDataModel.getValueAt(row,0)
        cvList=[]
        cvValuesList=[]
        for IX in range(11,17) :
            orgCV=self.cvsDataModel.getValueAt(self.cvsDataModel.getCVRow(IX),1)
            startOpSw=((IX-11)*8)+1
            for IY in range(0,8) :
                op=self.opSwDataModel.getOpSw(IY+startOpSw)
                
                if op == 0 :
                    # turn OFF
                    theBit = 0X01 << IY
                    orgCV = orgCV & ~theBit
                    
                if op == 1 :
                    # turn ON
                    theBit = 0X01 << IY
                    orgCV = orgCV | theBit
                    
            cvList.append(IX)
            cvValuesList.append(orgCV)
        a = ReadWriteCVDetails(self.sender,accid, True, self.cvsDataModel, self.opSwDataModel, cvList, cvValuesList )
        a.setName('WriteCVs')
        b = ReadWriteCVDetails(self.sender,accid, False, self.cvsDataModel, self.opSwDataModel, cvList, cvValuesList )
        b.setName('ReadCVs')
        c=HeavyLifter(self.f,a,b)  
        c.start()              

    def exportAllRoutes(self,event) :
        
        for rteRec in self.masterRouteTable.getList() :
            print ( str(rteRec[AllRoutesTable.colID]),str(rteRec[AllRoutesTable.colType]),str(rteRec[AllRoutesTable.colSerial]),
                    str(rteRec[AllRoutesTable.colRte]),str(rteRec[AllRoutesTable.colSwitch]),str(rteRec[AllRoutesTable.colTorC]) )

    def refreshDevices(self,event) :
        self.devicesDataModel.setRowCount(0)
        self.identityInfo=[]
        self.cvsDataModel.setRowCount(0)
        self.opSwDataModel.setRowCount(0)
        self.deviceDetailsTabs.removeAll()
        self.deviceDetailsTabs.addTab('CV', self.cvsPanel)
        self.deviceDetailsTabs.addTab('OpSws',self.opSwPanel)
        b = queryRteDevices(self.sender, self.identityInfo, self.devicesDataModel)
        b.setName('queryRteDevices')
        b.start()

    # build a single route panel
    def buildRtePanel(self,IX,entriesPerRte) :
        self.rteDetailPanel[IX] = JPanel()
        self.rteDetailPanel[IX].setLayout(BorderLayout())
        self.routesDataModel[IX].setRowCount(entriesPerRte)
        self.routesDataModel[IX].setValueAt('Top',0,0)
        self.routesTable[IX].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablePanel=JPanel()
        tablePanel.add(self.routesTable[IX])
        sp=JScrollPane(tablePanel)
        hp=JPanel()
        hp.add(self.routesTable[IX].getTableHeader(), BorderLayout.NORTH)
        self.rteDetailPanel[IX].add(hp, BorderLayout.NORTH)
        self.rteDetailPanel[IX].add(sp,BorderLayout.CENTER)
        bb=JPanel(FlowLayout(FlowLayout.CENTER))
        bb.add(self.statusRteLabels[IX])
        self.statusRteLabels[IX].setText('')
        bb.add(self.readRteButtons[IX])
        bb.add(self.writeRteButtons[IX])
        bb.add(self.deleteRteButtons[IX])
        self.rteDetailPanel[IX].add(bb,BorderLayout.SOUTH)
        self.f.pack()

    # sets up fill tabs and route routines.
    # heavylifet does the job.
    def readDeviceDetails(self) :
        row = self.deviceTable.getSelectedRow()
        devType = self.devicesDataModel.getValueAt(row,1)
        if (devType == 'DS64' ) :
            # cant do those here
            return
        devID = self.devicesDataModel.getValueAt(row,0)
        devSerial = self.devicesDataModel.getValueAt(row,2)
        self.numOfRtes=self.devicesDataModel.getValueAt(row,4)
        self.entriesPerRte=self.devicesDataModel.getValueAt(row,3)
        self.deviceDetailsTabs.removeAll()
        self.deviceDetailsTabs.addTab('CV', self.cvsPanel)
        self.deviceDetailsTabs.addTab('OpSws',self.opSwPanel)
        for IX in range(1, self.numOfRtes + 1) :
            self.buildRtePanel(IX,self.entriesPerRte)
            self.deviceDetailsTabs.addTab('Rte:' + str(IX), self.rteDetailPanel[IX])
        a = ReadRouteDetails(self.sender, self.identityInfo[row],self.routesDataModel,
                self.numOfRtes, self.entriesPerRte, -1, self.masterRouteTable, devID, devType, devSerial )  #Read all routes
        a.setName('ReadRouteDetails')
        b = ReadWriteCVDetails(self.sender, devID, False, self.cvsDataModel, self.opSwDataModel, [-1], [-1])
        b.setName('ReadCvDetails')
        hl=HeavyLifter(self.f,a,b)
        hl.start()

    def readOldDeviceDetails(self) :
        row = self.deviceTable.getSelectedRow()
        devType = self.devicesDataModel.getValueAt(row,1)
        if (devType != 'DS64' ) :
            # only those here
            return
        devID = self.devicesDataModel.getValueAt(row,0)
        devType =  0x73
        devSerial = self.devicesDataModel.getValueAt(row,2)
        self.numOfRtes=self.devicesDataModel.getValueAt(row,4)
        self.entriesPerRte=self.devicesDataModel.getValueAt(row,3)
        self.deviceDetailsTabs.removeAll()
        self.deviceDetailsTabs.addTab('CV', self.cvsPanel)
        self.deviceDetailsTabs.addTab('OpSws',self.opSwPanel)
        for IX in range(1, self.numOfRtes + 1) :
            self.buildRtePanel(IX,self.entriesPerRte)
            self.deviceDetailsTabs.addTab('Rte:' + str(IX), self.rteDetailPanel[IX])
        a = ReadOldRouteDetails(self.sender, self.routesDataModel,
                self.numOfRtes, self.entriesPerRte, -1, self.masterRouteTable, devID - 1, devType, devSerial )  #Read all routes
        a.setName('ReadRouteDetails')
        b = ReadWriteCVDetails(self.sender, devID, False, self.cvsDataModel, self.opSwDataModel, [-1], [-1])
        b.setName('ReadCvDetails')
        hl=HeavyLifter(self.f,a,b)
        hl.start()

    def gridClicked(self,event) :
        if (event.getClickCount() == 1 ) :
            self.resetCVs()
            self.readDeviceDetails()
            self.readOldDeviceDetails()

    def resetCVs(self) :
        self.cvsDataModel.setRowCount(0)
        self.opSwDataModel.setRowCount(0)
        self.devCVs = DeviceAttributes()
        row = self.deviceTable.getSelectedRow()
        self.numOfRtes=self.devicesDataModel.getValueAt(row,1)
        IX=0
        for CV in self.devCVs.getCVNumbers(self.devicesDataModel.getValueAt(row,1)) :
            print (type(CV) )
            print CV
            if (not isinstance(CV,type([]))) :
                self.cvsDataModel.addRow([CV,0,False,CV])
            else:
                self.cvsDataModel.addRow([CV[0],0,False,CV[1]])
        for opSw in self.devCVs.getOpSws(self.devicesDataModel.getValueAt(row,1)) :
            self.opSwDataModel.addRow([opSw[0],'',opSw[1]])
       
            
    def readIndividualRoute(self,event) :
        row = self.deviceTable.getSelectedRow()
        devID = self.devicesDataModel.getValueAt(row,0)
        devType = self.devicesDataModel.getValueAt(row,1)
        devSerial = self.devicesDataModel.getValueAt(row,2)
        rteNum=int(event.getSource().getName())
        self.statusRteLabels[rteNum].setText('')
        self.numOfRtes=self.devicesDataModel.getValueAt(row,4)
        self.entriesPerRte=self.devicesDataModel.getValueAt(row,3)
        if (devType == 'DS64' ) :
            b = ReadOldRouteDetails(self.sender, self.routesDataModel,
                    self.numOfRtes, self.entriesPerRte, rteNum, self.masterRouteTable, devID - 1, devType, devSerial )  #Read all routes
        else :
            b = ReadRouteDetails(self.sender, self.identityInfo[row],self.routesDataModel,
                    self.numOfRtes, self.entriesPerRte, rteNum, self.masterRouteTable, devID, devType, devSerial) 
        b.start()
        
    def writeIndividualRoute(self,event) :
        tabIX = self.deviceDetailsTabs.getSelectedIndex()
        rteNum=int(event.getSource().getName())
        self.statusRteLabels[rteNum].setText('')
        row = self.deviceTable.getSelectedRow()
        devID = self.devicesDataModel.getValueAt(row,0)
        devType = self.devicesDataModel.getValueAt(row,1)
        if (devType == 'DS64') :
            #cant write DS64
            self.statusRteLabels[rteNum].setText('DS64''s are READ ONLY')
            return
        devSerial = self.devicesDataModel.getValueAt(row,2)
        if (self.routesTable[rteNum].isEditing()) :
            self.statusRteLabels[rteNum].setText('Finish editing table first')
            return
        row = self.deviceTable.getSelectedRow()
        # Validate for obvious erros
        ZeroFound=False
        rteData=self.routesDataModel[rteNum] 
        for IX in range (0,rteData.getRowCount()) :
            if (not ZeroFound and rteData.getValueAt(IX,1)==0) :
                ZeroFound=True
            elif ( ZeroFound and rteData.getValueAt(IX,1)!=0) :
                self.statusRteLabels[rteNum].setText('None Zero Switch entry after Zero Entry')
                return
            elif ( not ZeroFound and ( rteData.getValueAt(IX,2)==None or len(rteData.getValueAt(IX,2))==0)) :
                self.statusRteLabels[rteNum].setText('Switch missing C/T')
                return
        self.numOfRtes=self.devicesDataModel.getValueAt(row,4)
        self.entriesPerRte=self.devicesDataModel.getValueAt(row,3)
        b = WriteRouteDetails(self.sender, self.identityInfo[row],
                rteData,
                self.numOfRtes, self.entriesPerRte, rteNum, self.masterRouteTable, devID, devType, devSerial) 
        b.start()
        self.statusRteLabels[rteNum].setText('')
        
    def deleteIndividualRoute(self,event) :
        row = self.deviceTable.getSelectedRow()
        rteNum=int(event.getSource().getName())
        devID = self.devicesDataModel.getValueAt(row,0)
        devType = self.devicesDataModel.getValueAt(row,1)
        self.statusRteLabels[rteNum].setText('')
        if (devType == 'DS64') :
            #cant write DS64
            self.statusRteLabels[rteNum].setText('DS64''s are READ ONLY')
            return
        devSerial = self.devicesDataModel.getValueAt(row,2)
        rteTM=self.routesDataModel[rteNum]
        for IX in range (0,rteTM.getRowCount()) :
            rteTM.setValueAt( 0, IX,1)
            rteTM.setValueAt( None, IX, 2)
        rteTM.fireTableDataChanged()
        self.numOfRtes=self.devicesDataModel.getValueAt(row,4)
        self.entriesPerRte=self.devicesDataModel.getValueAt(row,3)
        b = WriteRouteDetails(self.sender, self.identityInfo[row],
                self.routesDataModel[rteNum],
                self.numOfRtes, self.entriesPerRte, rteNum, self.masterRouteTable, devID, devType, devSerial) 
        b.start()

    def handle(self):
        return 0

# create one of these
a = LnRoutesCVOps()

# set the name
a.setName('LnRoutesCVOps')

# and show the initial panel
a.start()



