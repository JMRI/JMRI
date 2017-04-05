import java
import jmri

class MyListener(jmri.jmrix.loconet.locostats.LocoNetInterfaceStatsListener):
    def notifyChangedInterfaceStatus(self, o) :
	print "Got a status update"
        if (o.class == jmri.jmrix.loconet.locostats.PR2Status) :
            print "PR2 status: Serial # "+str(o.serial)+", Status "+str(o.status)+", current "+str(o.current)+", hardware "+str(o.hardware)+", software "+str(o.software)+"."
        elif (o.class == jmri.jmrix.loconet.locostats.PR3MS100ModeStatus):
            print "PR3 (MS100 mode) Status: good messages "+str(o.goodMsgCnt)+", bad messages "+str(o.badMsgCnt)+", status "+str(o.ms100status)+"."
        elif (o.class == jmri.jmrix.loconet.locostats.LocoBufferIIStatus):
            print "LocoBufferII Status: version "+str(o.version)+", breaks "+str(o.breaks)+", errors "+str(o.errors)+"."
        elif (o.class == jmri.jmrix.loconet.locostats.RawStatus):
            print "Status (Raw): "+str(o.raw[0])+" " + str(o.raw[1])+" " + str(o.raw[2])+" " + str(o.raw[3])+" " + str(o.raw[4])+" " + str(o.raw[5])+" " + str(o.raw[6])+" " + str(o.raw[7])+"."
        else:
            print "unexpected class " +o.class.toString()

myLocoNetConnection = jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0);
myLocoStatsFunc = jmri.jmrix.loconet.locostats.LocoStatsFunc(myLocoNetConnection)

myLocoStatsListener = MyListener()
myLocoStatsFunc.addLocoNetInterfaceStatsListener(myLocoStatsListener)


myLocoStatsFunc.sendLocoNetInterfaceStatusQueryMessage()
    

 