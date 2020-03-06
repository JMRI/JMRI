# Sample script to show how to receive CAN Frames
# with Timers + a User Interface Window

import datetime
import jmri
import java
import javax.swing

# Put a listener class in place.
class MyCanListener (jmri.jmrix.can.CanListener) :
    def __init__(self,startTime,textArea,tc):
        self.startTime = startTime
        self.lastFrame = startTime
        self.tA = textArea
        self.tc = tc
    def message(self, msg) :
        self.tA.append("\nReceived Outgoing Frame \n")
        
        myReporter = reporters.getBySystemName("MR3")
        currentRepObject = myReporter.getCurrentReport()
        
        currentReportObject = reporters.getByUserName("My Reporter 3").getCurrentReport()
        reporters.getByUserName("My Reporter 4").setReport("77") # set Report to String Value
        reporters.getByUserName("My Reporter 4").setReport(None) # set Report to Null
        
        currentBlockValueObject = blocks.getByUserName("My Block 1").getValue()
        blocks.getByUserName("My Block 1").setValue("12") # set Block to String Value
        blocks.getByUserName("My Block 1").setValue(None) # set Block Value to null
        
        self.tA.append(reporters.getByUserName("My Reporter 4").getCurrentReport())
        
        self.tA.append(reporters.getByUserName("My Reporter 4").getCurrentReport())
        
        return
    def reply(self, msg) :
        try:
            self.tA.append("\nReceived Incoming Frame \n")
            
            frame = jmri.jmrix.can.CanMessage(128) # New Outgoing CanMessage with header CAN ID 128
            frame.setNumDataElements(8)   # will load 8 bytes anyway by default, no harm in reaffirming
            frame.setElement(0, jmri.jmrix.can.cbus.CbusConstants.CBUS_DDWS) # Set DDWS OPC
            frame.setElement(1, 0xff) # device hi, 65535
            frame.setElement(2, 0xff) # device lo, 65535
            frame.setElement(3, 0x01) # data 1, 1
            frame.setElement(4, 0x02) # data 2, 2
            frame.setElement(5, 0x03) # data 3, 3
            frame.setElement(6, 0x04) # data 4, 4
            frame.setElement(7, 0x05) # data 5, 5
            self.tc.sendCanMessage(frame, self)
            # self prevents CanMessage being sent to def message(self, msg)
            # use None if you want this Instance to hear the sent message, eg
            # self.tc.sendCanMessage(frame, None)
            
            self.tA.append("CAN frame sent \n")
            return
            
        except:
            self.tA.append("\n * * * * * * * * * * * * * * * * * * * * * * * * * * \n")
            self.tA.append("\nException on Received Incoming Frame Script \n")
            return

buttonStart = javax.swing.JButton("Start CanListener ") # Initialse Listen button
buttonStop = javax.swing.JButton("Stop CanListener ") # Initialse Stop Listen button
buttonClear = javax.swing.JButton("Clear Feedback") # Initialse Clear Textarea button
feedbackPanel = jmri.util.swing.TextAreaFIFO(100) # max 100 lines of output

feedbackPanel.setVisible(True);
feedbackPanel.setEditable(False);
feedbackPanel.setRows(10);
feedbackPanel.setColumns(20);

tc = jmri.InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo).getTrafficController()

myCl = MyCanListener(datetime.datetime.now(),feedbackPanel,tc)

def startTc(event) :
    tc.addCanListener(myCl)
    feedbackPanel.append ("CAN Listener Added \n")

def stopTc(event) :
    tc.removeCanListener(myCl)
    feedbackPanel.append("CAN Listener Removed \n")

def clearText(event) :
    feedbackPanel.setText("")

# Creates the Java Swing frame
f = javax.swing.JFrame("CBUS CanListener") # New frame with title
f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS)) # Display frame in Y Axis

temppanel0 = javax.swing.JPanel()
temppanel1 = javax.swing.JPanel()
temppanel2 = javax.swing.JPanel()
temppanelscroll = javax.swing.JScrollPane()
temppanelscroll.getViewport().add(feedbackPanel)
temppanel3 = javax.swing.JPanel()

temppanel0.add(buttonStart)
temppanel1.add(buttonStop)
temppanel2.add(temppanelscroll)
temppanel3.add(buttonClear)

f.contentPane.add(temppanel0)
f.contentPane.add(temppanel1)
f.contentPane.add(temppanel3)
f.contentPane.add(temppanel2)

f.pack() # size window JFrame to panels
f.show() # display JFrame on screen

buttonStart.actionPerformed = startTc
buttonStop.actionPerformed = stopTc
buttonClear.actionPerformed = clearText
