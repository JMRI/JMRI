#Script to create a property change listener on the ID Tag Table

# A more advanced example of scripting, this script "listens" for new ID tags and pops up a window to allow 
# the user to enter a user name (typically loco or car/wagon number) and comment. Additionally, it listens for
# new or changed user names on existing RFID tags.  It does this by creating 
# a property change listener on the ID Tag Table. It will also update OperationsPro Locomotive Table or Car 
# Table if requested (check box on the popup window). It will flag loco/car numbers that do not exist, as well 
# as move an ID Tag from one loco to another or from one car to another and eliminate dupicate assignment of 
# ID Tag or cars or locomotives.

#If you are actively using RFID, recommended that this script be started as a JMRI Startup action, although it can be started at any time

#See https://www.jmri.org/help/en/html/hardware/rfid/GettingStartedWithRFID.shtml for more information.

# Script will display a window when:
#  (1) new ID Tag seen to allow entry of user name (typically loco or car/wagon number) and comment
#  (2) Id Tag User Name is changed 
# Multiple windows may be open at the same time for user convenience
# Will update Operations Locomotive Table or Car Table if requested (check box on the popup window)
# Will flag loco/car numbers that do not exist, as well as move an ID Tag from one loco to another or from one car to another
# Will eliminate dupicate assignment of RFID Tag or cars or locomotives 

#Companion scripts: ScanRFIDTagToLoco or ScanRFIDTagToWagon

# Illustrates user of JMRI property change listener, writing to script output window, adding messages to System Console, updating ID Tag Table, updating operations Locomotive and Car Tables, popping up a new window via Java swing with entry fields and radio buttons 
# Author: Jerry Grochow (c) 2023 (with acknowledgement to Tom Seitz)
# Uses basics from SensorLog.py and JButtonComplexExample (Original Author: Bob Jacobsen, copyright 2004, 2006)

# Software made available under GNU General Public License version 2 (https://www.jmri.org/COPYING)
# Please report any bugs or suggestions via https://groups.io/g/jmriusers/
# Part of the JMRI distribution

import jmri
import java
import javax.swing
import java.beans
from org.slf4j import LoggerFactory

DEBUG = False
DEBUGX= False  #Additional level of debug info put in Script Output Window, to avoid unnecessary log calls

AssignIdTagToRS_log = LoggerFactory.getLogger("jmri.jmrit.jython.exec.AssignIdTagToRS")
#Update version number and date in following statement:
AssignIdTagToRS_log.info("'AssignIdTagToRS' v42 10/13/2023 2305 loaded")

print "AssignIdTagToRS: will pop up a window when new RFID Tag detected \n or when new user name entered for existing RFID Tag.\n Optionally, will update Operations Locomotive and/or Car Tables.\n May be necessary to refresh ops tables to see effect."

#To move subsequent frames around screen
myFrameLocIncr = 0
myFrameLocOffset = 15                  #Number of pixels to offset next window (FUTURE: allow multiple open windows

################################################################################################################
# Create a frame to hold the button, set up for nice layout
class AssignIdTagToRSFrame:
 
    def __init__(self, newIdTag, existingIDTagUserName):
        self.newIdTag = newIdTag
        self.myFrame = None
        # To keep track of text field entry to enable the button
        self.myIdTagUserNameChanged = False
        self.myIdTagCommentChanged = False
        self.createFrame(existingIDTagUserName)
        # To keep track of whether to update either of the ops tables
        self.myIdTagUpdateLoco = False
        self.myIdTagUpdateCar = False

        #############################################################
    def createFrame (self, existingIDTagUserName) :
        global myFrameLocIncr, myFrameLocOffset
               
        # Create input fields
        self.createInputFields(existingIDTagUserName)
        # Create process buttons
        self.createProcessButtons()
        # Create the buttons that control Ops Tables Updates
        self.createOpsTablesSelectButtons()
        # Create text display lines      
        self.createTextElements()
        
        # Now put all the panels in a frame and display
        self.myFrame = javax.swing.JFrame("RFID Tag:  " + str(self.newIdTag))       # argument is the frames title
        self.myFrame.contentPane.setLayout(javax.swing.BoxLayout(self.myFrame.contentPane, javax.swing.BoxLayout.Y_AXIS))

        self.myFrame.contentPane.add(self.instrLine1)
        self.myFrame.contentPane.add(self.instrLine2)      
        self.myFrame.contentPane.add(self.createEntryLine())
        self.myFrame.contentPane.add(self.createCheckboxLine())
        self.myFrame.contentPane.add(self.createButtonLine())
        self.myFrame.contentPane.add(self.bottomLine)
 
        self.myFrame.pack()
        self.myFrame.setSize(700, 240)
        myFrameLocIncr += myFrameLocOffset              # Move any subsequent window
        x = self.myFrame.getX()
        y = self.myFrame.getY()
        #self.myFrame.setLocation(java.awt.Point.translate(myFrameLocIncr,self.myFrameLocIncr))
        self.myFrame.setLocation(x + myFrameLocIncr, y + myFrameLocIncr)
        self.myFrame.setVisible(True)
        return

    def createInputFields(self,existingIDTagUserName):
        # Input fields fields
        self.myIdTagUserName = javax.swing.JTextField(12)
        self.myIdTagUserName.setText(existingIDTagUserName)
        self.myIdTagComment = javax.swing.JTextField(20)
        # Set the text field actions
        self.myIdTagUserName.actionPerformed = self.whenMyIdTagUserNameChanged  # if user hit return or enter
        self.myIdTagUserName.focusLost = self.whenMyIdTagUserNameChanged        # if user tabs away
        self.myIdTagComment.actionPerformed = self.whenMyIdTagCommentChanged    # if user hit return or enter
        self.myIdTagComment.focusLost = self.whenMyIdTagCommentChanged          # if user tabs away
        return

    def createProcessButtons(self):
        self.processButton = javax.swing.JButton("Process")
        self.cancelButton = javax.swing.JButton("Cancel") 
        # create the button features
        self.cancelButton.setEnabled(True)              # button enabled
        self.cancelButton.actionPerformed   = self.whenMyCancelButtonClicked
        self.processButton.setEnabled(False)            # button disabled until user name entered
        self.processButton.actionPerformed    = self.whenMyProcessButtonClicked
        return

    def createOpsTablesSelectButtons(self):
        # Create the check box group features
        self.noOpsUpdateBox = javax.swing.JRadioButton("No Ops Table Update")
        self.noOpsUpdateBox.setSelected(True)           #pre-select the no Operations Table update box
        self.noOpsUpdateBox.actionPerformed = self.radioBtnCheck

        self.locoUpdateBox = javax.swing.JRadioButton("Locomotive Table")
        self.locoUpdateBox.actionPerformed  = self.radioBtnCheck

        self.carUpdateBox = javax.swing.JRadioButton("Car Table")
        self.carUpdateBox.actionPerformed   = self.radioBtnCheck
        #...and the checkbox group
        
        opsUpdateBoxGroup = javax.swing.ButtonGroup()
        opsUpdateBoxGroup.add(self.noOpsUpdateBox)
        opsUpdateBoxGroup.add(self.locoUpdateBox)
        opsUpdateBoxGroup.add(self.carUpdateBox)
        return

    def createTextElements(self):
        #Instructions at top
        self.instrLine1 = javax.swing.JPanel()
        self.instrLine1.add(javax.swing.JLabel("Enter User Name (Loco or Car/Wagon #) for RFID Tag " + str(self.newIdTag)))
        self.instrLine2 = javax.swing.JPanel()
        self.instrLine2.add(javax.swing.JLabel("Check box to also update Operations Locomotive or Car Table"))
               
        #Create bottom line
        self.bottomLine = javax.swing.JPanel()
        self.bottomLine.add(javax.swing.JLabel("Check Scripting Output window for messages"))

        return

    def createEntryLine(self):
        #Tom: how does this know which to lay out horizontally and which vertically??
        entryField1 = javax.swing.JPanel()
        entryField1.add(javax.swing.JLabel("RFID Tag User Name (Loco or Car/Wagon #):"))
        entryField1.add(self.myIdTagUserName)

        entryField2 = javax.swing.JPanel()
        entryField2.add(javax.swing.JLabel("Comment:"))
        entryField2.add(self.myIdTagComment)

        #Laid out vertically
        entryLine = javax.swing.JPanel()
        entryLine.add(entryField1)
        entryLine.add(entryField2)
        return entryLine

    def createCheckboxLine(self):
        #Laid out horizontally
        checkboxLine = javax.swing.JPanel()
        checkboxLine.add(self.noOpsUpdateBox)
        checkboxLine.add(self.locoUpdateBox)
        checkboxLine.add(self.carUpdateBox)
        return checkboxLine

    def createButtonLine(self):
        #Laid out horizontally
        buttonLine = javax.swing.JPanel()
        buttonLine.add(self.cancelButton)
        buttonLine.add(self.processButton)
        return buttonLine
        
    #############################################################
    # Change handlers for frame
    # When one of the radio buttons is selected
    def radioBtnCheck(self,event):
        # Process the events and update the label
        #Tom's version:
        self.myIdTagUpdateLoco = self.locoUpdateBox.isSelected()
        self.myIdTagUpdateCar = self.carUpdateBox.isSelected()
        # if self.noOpsUpdateBox.isSelected():
            # self.myIdTagUpdateCar  = False
            # self.myIdTagUpdataLoco = False
        # elif self.locoUpdateBox.isSelected():
            # self.myIdTagUpdateLoco = True
            # self.myIdTagUpdateCar  = False
        # elif self.carUpdateBox.isSelected():        
            # self.myIdTagUpdateCar  = True
            # self.myIdTagUpdataLoco = False
        # else:  # Shouldn't ever get here
            # print "ART180: ERR in radio button selection"
        
        if DEBUGX:
            print "ART207:", self.myIdTagUpdateLoco, self.myIdTagUpdateCar
        return

    def whenMyIdTagUserNameChanged(self, event) :                
        if (DEBUGX):
            print "ART186:", event
        if (self.myIdTagUserName.text is not "") :          # myIdTagUserName only changed if a value was entered
            self.myIdTagUserNameChanged = True
            self.processButton.setEnabled(True)         # enable button if user name entered
        return

    # have the comment text field enable the process button only if user name already entered
    def whenMyIdTagCommentChanged(self, event) :
        if (DEBUGX):
            print "ART195:", event
        if (self.myIdTagComment.text is not "") :
            self.myIdTagCommentChanged = True
        if (self.myIdTagCommentChanged and self.myIdTagUserNameChanged) :         # make sure user name entered
            self.processButton.setEnabled(True)
        return
        
    def whenMyCancelButtonClicked(self, event) :     
        global myFrameLocIncr, myFrameLocOffset
        if (DEBUGX):
            print "ART205:", event
        if (self.myFrame is not None):                      #Shut down window
            self.myFrame.dispose()
            myFrameLocIncr -= myFrameLocOffset
        return

    # define what Process button does when clicked - THIS DOES MOST OF THE WORK
    def whenMyProcessButtonClicked(self, event) :
        global myIdTagMgrListenerGlobal, myFrameLocIncr, myFrameLocOffset       
        if DEBUG:
            print "ART212:", self.newIdTag, "User entered IdTagUserName: ", self.myIdTagUserName.text, " IdTagComment: ", self.myIdTagComment.text, "Frame: ", self.myFrame
        #Temporarily remove listener
        #idtags = jmri.InstanceManager.getDefault(jmri.IdTagManager)     #Uncomment this line if using JMRI 5.5.3 or earlier
        idtags.removePropertyChangeListener(myIdTagMgrListenerGlobal)
        #Check if system name still available
        if (self.newIdTag is not None):       
            #If user name was updated:
            if self.myIdTagUserName.text is not "":
                # Update IdTag Table entry
                self.newIdTag.setUserName(self.myIdTagUserName.text)
                # See if either Ops Locommotive or Car Table to be updated
                self.assigned = False
                # See if Ops Table update requested
                if not (self.myIdTagUpdateCar or self.myIdTagUpdateLoco):
                    print "AssignId: Ops tables not updated for ", self.newIdTag.getUserName()                 
                else:
                    self.updateOpsTables()               

            # Update comment into IdTag Table
            if self.myIdTagComment.text is not "":
                self.newIdTag.setComment(self.myIdTagComment.text)

            #Get rid of open window
            if (self.myFrame is not None):
                self.myFrame.dispose()
                myFrameLocIncr -= myFrameLocOffset
                
        else:           # Shouldn't ever get here
                print "ART260: ERR System name not available"
                
        # Re-attach the ID Tab Table listener
        idtags.addPropertyChangeListener(myIdTagMgrListenerGlobal)
        return
    
    #############################################################
    def updateOpsTables(self):
        #Create abbreviations
        cars = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarManager)
        engines = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.engines.EngineManager)
        #Find in operations-car table and update
        if self.myIdTagUpdateCar:
            #Tom's: self.updateLocoCarTable("car", jmri.jmrit.operations.rollingstock.cars.CarManager)
            self.updateLocoCarTable("car", cars)
        #Find in operations-engine table and update
        elif self.myIdTagUpdateLoco:
            #Tom's: self.updateLocoCarTable("engine", jmri.jmrit.operations.rollingstock.engines.EngineManager)
            self.updateLocoCarTable("engine", engines)
        else:    # Shouldn't ever get here
            print ("ART249: ERR in Ops Table selection")
        #See if RFID Tag previously assigned to another car or loco and remove from any (should not happen, but just in case)
        if self.assigned:   #Check in both types of tables             
            for rs in cars.getByRfidList():
                self.findDupUN(rs, "car") 
            for rs in engines.getByRfidList():
                self.findDupUN(rs, "engine")
        return        
    
    def updateLocoCarTable(self, type_name, ops_table):
        for rs in ops_table.getByIdList():
            if type_name == "car":
                self.processNewUN(rs, "car")
            elif type_name == "engine":
                self.processNewUN(rs, "engine")
            if self.assigned:
                break
        else:
            print("AssignId: No such " + type_name + " number " + str(self.newIdTag.getUserName()))
        return
      
    def processNewUN(self, rs, type_name):
        if (DEBUGX):
            print "ART281:" , rs.getId(), rs.getNumber(), "[", self.newIdTag.getUserName(), "]", rs.getIdTag(), rs.getRfid() 
        if rs.getNumber() == self.newIdTag.getUserName():
            if rs.getIdTag() is not None:
                print "AssignId: REPLACED " ,  rs.getRfid(), "on   ", type_name, rs.getId() 
                #print("AssignId: REPLACED {} on {} {}".format(rs.getRfid(), type_name, rs.getId()))
            rs.setIdTag(self.newIdTag)
            rs.setRfid(self.newIdTag.getSystemName())
            print "AssignId: Assigned" ,  rs.getRfid(), "to   ", type_name, rs.getId()  
            #Tom's: print("AssignId: Assigned {} to {} {}".format(rs.getRfid(), type_name, rs.getId()))
            self.assigned = True
        return

        
    def findDupUN(self, rs, type_name):
        if (DEBUGX):
            print "ART296:" , rs.getId(), "[", rs.getNumber(),"]",  "[",self.newIdTag.getSystemName(), "]", rs.getRfid() 
        if rs.getRfid() == self.newIdTag.getSystemName():
            if rs.getNumber() != self.newIdTag.getUserName():
                rs.setIdTag(None)
                rs.setRfid(None)
                print "AssignId: REMOVED " ,  self.newIdTag.getSystemName(), "from ", type_name, rs.getId() 
                #Tom's: print("AssignId: REMOVED {} from {} {}".format(self.newIdTag.getSystemName(), type_name, rs.getId()))
        return


############################################################################################################
# Define a Manager listener for the IdTagManager.
class AssignIdTagManagerListener(java.beans.PropertyChangeListener):

  prevEventSource   = None
  prevEventProperty = None
  prevEventNewValue = None
  
  def propertyChange(self, event):

    if DEBUG:
        print "ART316: ", event.source, "/property=",event.propertyName, "/oldValue=", str(event.oldValue), "/newValue=", str(event.newValue)  
    if (event.source == self.prevEventSource and event.propertyName == self.prevEventProperty and event.newValue == self.prevEventNewValue): #Only process first event from this source (JMRI bug to be fixed 2023-08-14)
        return
    self.prevEventSource = event.source
    self.prevEventProperty = event.propertyName
    self.prevEventNewValue = event.newValue
    #idtags = jmri.InstanceManager.getDefault(jmri.IdTagManager)        #Uncomment this line if using JMRI 5.5.3 or earlier
    if (event.propertyName == "beans" and event.newValue is not None and event.oldValue == None):              #New RFID tag
        newIdTag = idtags.getBySystemName(str(event.newValue))
        if newIdTag is not None:
            if DEBUG:
                print "ART326: AssignIdTagManagerListener:  ", newIdTag, "/", event.propertyName, "/", event.newValue
            AssignIdTagToRSFrame(newIdTag, "")
    elif (event.propertyName == "DisplayListName" and event.newValue is not None):  #Added/changed user name to existing RFID Tag
        existingIdTag = idtags.getByUserName(str(event.newValue))
        if existingIdTag is not None:
            if DEBUG:
                print "ART332: AssignIdTagManagerListener:  ", existingIdTag, "/", event.propertyName, "/", event.newValue
            AssignIdTagToRSFrame(existingIdTag, str(event.newValue))    
    return


############################################################################################################
#----- MAINLINE --------------------------------------------------------------------------------
if globals().get("AssignIdTagToRS_running") is not None: # Script already loaded so exit
    AssignIdTagToRS_log.warn("'AssignIdTagToRS' already loaded and running. Restart JMRI before loading this script.")
else: # Continue running script
    AssignIdTagToRS_log.info("'AssignIdTagToRS' started.")
    #idtags = jmri.InstanceManager.getDefault(jmri.IdTagManager)     #Uncomment this line if using JMRI 5.5.3 or earlier
    # Remove a prior listener, if any
    if globals().get("myIdTagMgrListenerGlobal") is not None:
        if myIdTagMgrListenerGlobal is not None:
            idtags.removePropertyChangeListener(myIdTagMgrListenerGlobal)
            AssignIdTagToRS_log.info("'AssignIdTagToRS' prior IdTag Table listener removed.")
            myIdTagMgrListenerGlobal = None
    # Attach the new sensor manager listener
    myIdTagMgrListenerGlobal = AssignIdTagManagerListener()
    idtags.addPropertyChangeListener(myIdTagMgrListenerGlobal)
    AssignIdTagToRS_running = True               #So script won't be loaded twice
    AssignIdTagToRS_log.info("'AssignIdTagToRS' IdTag Table listener started.")

