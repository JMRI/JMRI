#
# LnScanner
#
# This script has been modified to read in message text strings
# to be displayed for each sensor and/or to be spoken
# it also plots a graph of the number of bytes transmitted
# on the LocoNet each second
#
# Author: Gerry Wolfson
#
# $Revision: 2.7 $ 4/07/16 added time stamp checkbox
# $Revision: 2.8 $ 4/13/16 added 25 second averaged line to stripchart
# $Revision: 2.9 $ 4/18/16 added doAvgPlot boolean to turn on/off average plotting
# $Revision: 3.0 $ 4/19/16 added doSecsPlot boolean to turn on/off seconds plotting
# Note: doAvgPlot & doSecsPlot both false turns off charting
# $Revision: 3.1 $ 4/22/16 (a) reorder plots: 30%, 45%, 60%, 80% first followed by seconds plot and then accummulated plot
#                          (b) put message to system console if message list file does not exist and exit
#                          (c) choose either logarithmic or linear vertical scale
# $Revision: 3.2 $ 4/25/16 (a) debug of long delay between spoken alerts
#                          (b) added my default startup test settings
#                          (c) pointed to latest message set V3
#                          (d) added missing globals for checkbox turn on for last state spoken after resync
# $Revision: 3.3 $ 4/28/16 added message string index display checkbox
# $Revision: 3.4 $ 5/15/16 added numeric report of message number and no text on empty strings
# $Revision: 3.5 $ 5/30/16 added JScrollPane to text area and set controls for scrolling
# $Revision: 3.6 $ 6/11/16 use sensors in JMRI instead of CSV file

# $Revision: 3.8 $ 6/08/16 added jmri.util.JmriMFrame to send gui to webserver
# $Revision: 3.8rhwX1 $ 6/20/16 rearranging MsgListener filters by moving sig head into upper group and added hack for parseMsg
# $Revision: 3.8rhwX2 $ 6/23/16 general code cleanup after hack fixed problem

# $Revision: 3.9 $ 6/12/16 added displayTxt to handle non-LocoNet msg display (no parsing)
# $Revision: 4.0 $ 6/15/16 added autoscroll CARET set command
# $Revision: 4.1 $ 6/18/16 incorporated RHW sensor comment usage features
# $Revision: 4.2 $ 8/16/16 added filtering to scanReport background task
# $Revision: 4.3 $ 8/18/16 commented out or remove unnecessary debug print traces
# $Revision: 4.4 $ 8/30/16 special handling of trolley hill hold with return from spencer loop and interchange normal
# $Revision: 4.5 $ 9/07/16 reordered logic and added delays to special handling of trolley hill hold
# $Revision: 4.6 $ 9/07/16 added repeat delay to while loop invoked when alert has been given on last loop


# This script displays LocoNet messages.
# The frame contains JTextFields, a scroll field, check boxes and a button.
# The frame contains Panel arranged such that only the scroll field moves
# when the window is resized.
# Switch, feedback and sensor messages can be filtered.
#
# Author: Bill Robinson with help from Bob Jacobsen
#
# $Revision: 1.0 $  1/8/07
# $Revision: 1.1 $  11/2/10 added second SN address field
# $Revision: 1.2 $  6/3/11 added SN address range fields and second SW address field
# $Revision: 1.3 $  9/2/12 added option to print SN address if its state changed
#
# editing boolean values: True False
#
import jmri
import java
import java.util.concurrent.TimeUnit

apNameVersion = "LocoNet Scanner - v4.6"
#sensorMsgFile = jmri.util.FileUtil.getUserFilesPath() + "LnSnrMsgList.csv"
#sensorMsgFile = jmri.util.FileUtil.getUserFilesPath() + "LnSnrMsgTxtV3.csv"
#sensorMsgFile = jmri.util.FileUtil.getUserFilesPath() + "LnSnrMsgTxtV4.csv"
#sensorMsgFile = jmri.util.FileUtil.getUserFilesPath() + "LnSnrMsgTxtV5.csv"
#sensorMsgFile = jmri.util.FileUtil.getUserFilesPath() + "LnSnrMsgTxtV6x.csv"
#sensorMsgFile = jmri.util.FileUtil.getUserFilesPath() + "LnSnrMsgTxtV7.csv"

userNamePrefix = 'Short Detector' # user name of sensors of interest start with this
readySensorSystemName = 'LS3000' # sensor used to indicate all short detectors are online

secs2avg = 25 #number of periods to include in each averaging window
doAvgPlot = False #plot averages for number of accummulated periods
doSecsPlot = False #plot each seconds total bytes received
doLogScale = True #select vertical scale to be logarithmic or linear

import javax.swing
aspectColor = " "
accumByteCnt = 0

#import sys
#import com.csvreader

from java.lang import Runnable
from javax.swing import SwingUtilities
from javax.swing.text import DefaultCaret
from java.awt import BasicStroke
from java.awt.geom import Ellipse2D

from org.slf4j import Logger
from org.slf4j import LoggerFactory

log = LoggerFactory.getLogger("LnScanner")

import array

global snrStatusArr

import time

mainStartTime = "[init started]" + time.strftime('%X %x %Z')

import thread
killed = False #run background task until set true
resyncInProgress = False #go true when resync button pressed
checkRestoreDone = True
#reset after 15 seconds and restore speak checkbox values
resyncStrtTime = time.clock() #seed time at app start
print 'seed value of resyncStrtTime = ', resyncStrtTime

#from threading import Thread
#from java.lang import InterruptedException, Runnable, Thread
#from java.beans import PropertyChangeListener
#from java.util.concurrent import ExecutionException
#from javax.swing import SwingWorker, SwingUtilities

# imports for stripchart portion
import os;
from org.jfree.chart import ChartColor
from org.jfree.chart import ChartFactory
from org.jfree.chart import ChartFrame
from org.jfree.chart import ChartPanel
from org.jfree.chart import ChartUtilities
from org.jfree.chart import JFreeChart
from org.jfree.chart.axis import LogarithmicAxis
from org.jfree.chart.axis import ValueAxis
#from org.jfree.chart.axis import LogAxis
from org.jfree.chart.plot import XYPlot
from org.jfree.chart.plot import ValueMarker
from org.jfree.chart.renderer.xy import XYLineAndShapeRenderer
from org.jfree.chart.renderer.xy import XYItemRenderer
from org.jfree.chart.renderer.xy import XYDotRenderer
from org.jfree.data.time import Millisecond
from org.jfree.data.time import TimeSeries
from org.jfree.data.time import TimeSeriesCollection
from org.jfree.data.xy import XYDataset
from org.jfree.ui import ApplicationFrame
from org.jfree.ui import RefineryUtilities
from org.jfree.util import ShapeUtilities

# *******************************************************************************
# ************* Setup the chart display form for showing LocoNet Bandwidth Used *
# *******************************************************************************
def createChart(dataset):
    global series
    global doAvgPlot
    global doSecsPlot

    chart = ChartFactory.createTimeSeriesChart('LocoNet Bandwidth Use (bytes/sec)', 'Time (mins:secs)', 'Message Traffic (bytes)', dataset, True, True, False)
    plot = chart.getXYPlot()
    xAxis = plot.getDomainAxis()
    xAxis.setFixedAutoRange(900000.0) #900 seconds, 15 min stripchart
    #xAxis.setFixedAutoRange(3600000.0) #3600 seconds, 1 hour stripchart
    xAxis.setRange(0.0, 2000.0)
    plot.setDomainAxis(xAxis)
    if doLogScale:
        yAxis = LogarithmicAxis('Message Traffic (bytes)')
        #max bytes = 2057 but should NEVER get anywhere close (network would be saturated)
        yAxis.setUpperBound(2000.0)
        yAxis.setLowerBound(0.0)
        yAxis.setAutoRange(False)
        #yAxis.setAllowNegativeFlag(True)
        plot.setRangeAxis(yAxis)
    else:
        yAxis = plot.getRangeAxis()
        #max bytes = 2057 but should NEVER get anywhere close (network would be saturated)
        yAxis.setUpperBound(2000.0)
        yAxis.setLowerBound(0.0)
        yAxis.setAutoRange(False)
        #yAxis.setAllowNegativeFlag(True)
        plot.setRangeAxis(yAxis)
    ##create 60% horizontal line across graph
    #marker60 = ValueMarker(1234)
    #marker60.setPaint(ChartColor.YELLOW)
    ##marker60.setLabel('|     60%') # text is black
    ##marker60.setStroke(BasicStroke(0x1f))
    ##marker60.setLabelPaint(ChartColor.YELLOW)
    #plot.addRangeMarker(marker60)
    ##create 80% horizontal line across graph
    #marker80 = ValueMarker(1646)
    #marker80.setPaint(ChartColor.LIGHT_RED)
    ##marker80.setLabel('|     80%') # text is black
    ##marker80.setStroke(BasicStroke(0x1f))
    ##marker80.setLabelPaint(ChartColor.RED)
    #plot.addRangeMarker(marker80)
    xAxis.setAutoRange(True) #forces plotting to start
    #validate all background color settings
    plot.setBackgroundPaint(ChartColor.DARK_BLUE)
    plot.setBackgroundPaint(ChartColor.DARK_CYAN)
    plot.setBackgroundPaint(ChartColor.DARK_GREEN)
    plot.setBackgroundPaint(ChartColor.DARK_MAGENTA)
    plot.setBackgroundPaint(ChartColor.DARK_RED)
    plot.setBackgroundPaint(ChartColor.DARK_YELLOW)
    plot.setBackgroundPaint(ChartColor.LIGHT_BLUE)
    plot.setBackgroundPaint(ChartColor.LIGHT_CYAN)
    plot.setBackgroundPaint(ChartColor.LIGHT_GREEN)
    plot.setBackgroundPaint(ChartColor.LIGHT_MAGENTA)
    plot.setBackgroundPaint(ChartColor.LIGHT_RED)
    plot.setBackgroundPaint(ChartColor.LIGHT_YELLOW)
    plot.setBackgroundPaint(ChartColor.BLUE)
    plot.setBackgroundPaint(ChartColor.CYAN)
    plot.setBackgroundPaint(ChartColor.GREEN)
    plot.setBackgroundPaint(ChartColor.MAGENTA)
    plot.setBackgroundPaint(ChartColor.RED)
    plot.setBackgroundPaint(ChartColor.YELLOW)
    plot.setBackgroundPaint(ChartColor.ORANGE)
    #plot.setBackgroundPaint(ChartColor.BROWN) # does not exist
    #plot.setBackgroundPaint(ChartColor.PURPLE) # does not exist
    plot.setBackgroundPaint(ChartColor.VERY_DARK_BLUE)
    plot.setBackgroundPaint(ChartColor.VERY_DARK_CYAN)
    plot.setBackgroundPaint(ChartColor.VERY_DARK_GREEN)
    plot.setBackgroundPaint(ChartColor.VERY_DARK_MAGENTA)
    plot.setBackgroundPaint(ChartColor.VERY_DARK_RED)
    plot.setBackgroundPaint(ChartColor.VERY_DARK_YELLOW)
    plot.setBackgroundPaint(ChartColor.VERY_LIGHT_BLUE)
    plot.setBackgroundPaint(ChartColor.VERY_LIGHT_CYAN)
    plot.setBackgroundPaint(ChartColor.VERY_LIGHT_GREEN)
    plot.setBackgroundPaint(ChartColor.VERY_LIGHT_MAGENTA)
    plot.setBackgroundPaint(ChartColor.VERY_LIGHT_YELLOW)
    plot.setBackgroundPaint(ChartColor.WHITE)
    plot.setBackgroundPaint(ChartColor.LIGHT_GRAY)
    plot.setBackgroundPaint(ChartColor.GRAY)
    plot.setBackgroundPaint(ChartColor.DARK_GRAY)
    plot.setBackgroundPaint(ChartColor.BLACK)
    #defaults to last background color if uncommented copy of another setBackgroundPaint is not next
    plot.setBackgroundPaint(ChartColor.LIGHT_GRAY)
    #plot.setBackgroundPaint(ChartColor.BLUE)
    plot.setBackgroundAlpha(1.0)
    plot.setDomainMinorGridlinePaint(ChartColor.GRAY)
    plot.setDomainGridlinePaint(ChartColor.DARK_GRAY)
    plot.setRangeMinorGridlinePaint(ChartColor.GRAY)
    plot.setRangeGridlinePaint(ChartColor.DARK_GRAY)
    renderer = XYLineAndShapeRenderer()
    renderer.setSeriesShape(0, Ellipse2D.Double()) #%30 = 617 bytes
    renderer.setSeriesShape(1, Ellipse2D.Double()) #%45 = 926 bytes
    renderer.setSeriesShape(2, Ellipse2D.Double()) #%60 = 1234 bytes
    renderer.setSeriesShape(3, Ellipse2D.Double()) #%80 = 1646 bytes
    if doSecsPlot:
        renderer.setSeriesShape(4, Ellipse2D.Double())
    if doAvgPlot:
        renderer.setSeriesShape(5, Ellipse2D.Double())

    #renderer.setSeriesShape(0,Ellipse2D.Double(-1.0,-1.0,1.0,1.0))
    #renderer.setSeriesShape(1,Ellipse2D.Double(-1.0,-1.0,1.0,1.0))
    #renderer.setSeriesShape(2,Ellipse2D.Double(-1.0,-1.0,1.0,1.0))

    # sets paint color for each series
    renderer.setSeriesPaint(0, ChartColor.RED)
    renderer.setSeriesPaint(1, ChartColor.ORANGE)
    renderer.setSeriesPaint(2, ChartColor.YELLOW)
    renderer.setSeriesPaint(3, ChartColor.GREEN)
    if doSecsPlot:
        renderer.setSeriesPaint(4, ChartColor.DARK_BLUE)
    if doAvgPlot:
        renderer.setSeriesPaint(5, ChartColor.VERY_DARK_GREEN)
    # sets line thickness for each series
    renderer.setSeriesStroke(0, BasicStroke(2.0))
    renderer.setSeriesStroke(1, BasicStroke(2.0))
    renderer.setSeriesStroke(2, BasicStroke(2.0))
    renderer.setSeriesStroke(3, BasicStroke(2.0))
    if doSecsPlot:
        renderer.setSeriesStroke(4, BasicStroke(1.0))
    if doAvgPlot:
        renderer.setSeriesStroke(5, BasicStroke(1.0))
    plot.setRenderer(renderer)
    #ChartUtilities.applyCurrentTheme(chart) #don't use, reverts to some other configuration of the layout!
    return chart

# ***********************************************************
# ************* Setup and run task in background to update  *
#               LocoNet BW Use graph at a 1 second interval *
# ***********************************************************
def plotUpdater():
    global killed
    global accumByteCnt
    global series
    global secs2avg
    global doAvgPlot
    global doSecsPlot

    print 'plotUpdate running'
    start_time = time.clock()
    print 'start_time = ', start_time

    # init for sliding window averaging
    avgByteCnt = 0
    oldValList = [0 for i in range(secs2avg)] # values to subtract on each sliding window sum
    oldestValPntr = 0
    #for i in range(secs2avg) :
    #    print 'oldValList(', i, ') = ', oldValList[i] #view inited list values
    while killed == False: #exit thread when killed is true
        time.sleep(0.01) #sleep for a 10 ms to allow foreground to run
        #time.sleep(0.1) #sleep for tenth of a second
        end_time = time.clock()
        elapsed_time = end_time - start_time
        # plot new points every 1 second
        if elapsed_time > 1:
            start_time = end_time
            #print 'elapsed_time = ', elapsed_time
            # prepare accummulated value for a single period
            plotByteCnt = accumByteCnt
            accumByteCnt = 0 #reset for foreground task
            
            # prepare plot point of average value for a given period
            #print 'oldestValPntr (before) = ', oldestValPntr
            avgByteCnt += plotByteCnt #add single point value to continuously accumulated value for averaging
            avgByteCnt -= oldValList[oldestValPntr] #reduce accummulated value by oldest period value
            oldValList[oldestValPntr] = plotByteCnt #save newest period value into oldest location
            oldestValPntr = (oldestValPntr + 1) % secs2avg # move pointer to next oldest value with wrap to zero
            accumAvgCnt = avgByteCnt / (secs2avg * 1.0) #now get floating point average of this period
            #print 'oldestValPntr (after) = ', oldestValPntr
            #print 'accumAvgCnt = ', accumAvgCnt
            
            # append all four to graph
            #print 'plotByteCnt = ', plotByteCnt, end_time
            hzline80.add(Millisecond(), 1646)
            hzline60.add(Millisecond(), 1234)
            hzline30.add(Millisecond(), 617)
            hzline15.add(Millisecond(), 309)
            if doSecsPlot:
                series.add(Millisecond(), plotByteCnt)
            if doAvgPlot:
                avgAccum.add(Millisecond(), accumAvgCnt)
    print 'plotUpdate stopped'
    return
    
# *******************************************************************************
# ************* Setup and start task in background to scan sensor status array  *
#               and report ACTIVE ones with a message string which is displayed *
#               and/or spoken.                                                  *
# *******************************************************************************
def scanReporter():
    global snrStatusArr
    global killed
    global resyncInProgress
    global resyncStrtTime
    global checkRestoreDone
    global lastSnSpkChgCbx
    global lastSigSpkChgCbx
    
    # repeat delay flag for alert given on last loop
    repeatDelayFlag = False
    
    scanPntr = 0
    retcode = 0
    #print snrStatusArr[0]
    #print scanPntr
    print 'scanReporter running'
    while killed == False: #exit thread when killed is true
        if repeatDelayFlag:
            time.sleep(10.0) #wait 10 seconds before scanning loop again
            repeatDelayFlag = False #clear delay flag for next pass thru loop
        else:
            #time.sleep(0.01) #sleep for a 10 ms to allow foreground to run
            time.sleep(0.0005) #sleep for a 0.5 ms to allow foreground to run

        # Check if first sensor and if so check if resync has just completed
        if ((time.clock() - resyncStrtTime) >= 15.0):
            resyncInProgress = False
        if (not(resyncInProgress) and not(checkRestoreDone)):
            print 'lastSnSpkChgCbx = ', lastSnSpkChgCbx
            print 'lastSigSpkChgCbx = ', lastSigSpkChgCbx
            checkRestoreDone = True
            snSpkChgCheckBox.setSelected(lastSnSpkChgCbx) #restore this checkbox to it's last known state
            sigSpkChgCheckBox.setSelected(lastSigSpkChgCbx) #restore this checkbox to it's last known state
            print 'time.clock() = ', time.clock()
            print 'resyncInProgress = ', resyncInProgress
            print 'lastSnSpkChgCbx = ', lastSnSpkChgCbx
            print 'lastSigSpkChgCbx = ', lastSigSpkChgCbx
        #print time.strftime('=> %X %x %Z')
        # Check all JMRI sensor status values and if ACTIVE display and/or speak
        for systemName in snrStatusArr.keys():
            if snrStatusArr[systemName] == ACTIVE:
                if int(systemName[2:]) >= java.lang.Integer.decode(rangeAdd1.text) and int(systemName[2:]) <= java.lang.Integer.decode(rangeAdd2.text):
                    msg = sensors.getSensor(systemName).getComment()
                    #print msg
                    #print time.strftime('%X %x %Z')
                    if msg and len(msg) > 0:
                        if indexCheckBox.isSelected():
                            print 'msg length of index: ', scanPntr, '(', systemName,') = ', len(msg)
                            scrollArea.setText(scrollArea.getText() + systemName + " = " + msg + "\n")
                        else:
                            scrollArea.setText(scrollArea.getText() + msg + "\n")
                    else:
                        msg = systemName + " has no message"
                        #msg = str(scanPntr) + " has no message"
                        scrollArea.setText(scrollArea.getText() + msg + "\n")
                        
                    # You can also speak the message by un-commenting one of the following "pid = " lines
                    if snSpkChgCheckBox.isSelected() :  
                        time.sleep(0.1) #add a 0.1 sec delay
                        #time.sleep(1.0) #add a sec delay
                        # this is where you select the voice synthesizer (speak, espeak, or nircmd)           
                        #pid = java.lang.Runtime.getRuntime().exec(["speak", msg])
                        #pid = java.lang.Runtime.getRuntime().exec(["C:\Program Files (x86)\eSpeak\command_line\espeak", msg])
                        pid = java.lang.Runtime.getRuntime().exec('nircmd speak text "' + msg +'" -2 100')
                        pid.waitFor()
                        repeatDelayFlag = True #set flag so loop will delay before next pass thru loop

                # if len(msg) > 0 :
                    # if indexCheckBox.isSelected() :
                        # print 'msg length of ', scanPntr, ' = ', len(msg)
                        # scrollArea.setText(str(scanPntr)+" = " + msg + "\n")
                    # else :
                        # scrollArea.setText(msg+"\n")
                # else :
                    # msg = str(scanPntr) + " has no message"
                    # scrollArea.setText(msg+"\n")
                
                    ##time.sleep(0.1)
                    #time.sleep(1.0)
                    # You can also speak the message by un-commenting the next line
                    ##if snSpkChgCheckBox.isSelected():
                        # this is where you select the voice synthesizer (speak, espeak, or nircmd)
                        #pid = java.lang.Runtime.getRuntime().exec(["speak", msg])
                        #pid = java.lang.Runtime.getRuntime().exec(["C:\Program Files (x86)\eSpeak\command_line\espeak", msg])
                        ##pid = java.lang.Runtime.getRuntime().exec('nircmd speak text "' + msg + '" -2 100')
                        ##pid.waitFor()
                        
    print 'scanReporter stopped'
    return False
    
# ******************************************************************************
#class Task(SwingWorker) :

#    def __init__(self):
#        SwingWorker.__init__(self)

#    def doInBackground(self) :
#        global snrStatusArr
#        global maxArr

#        scanPntr = 0
#        while(True) : #loop forever
#            #Sleep then check next sensor status array value and if "1" display and/or speak
#            try:
#                Thread.sleep(10)
#                if snrStatusArr[scanPntr] == 1 :
#                    msg = msgArr[scanPntr]
#                    scrollArea.setText(scrollArea.getText()+msg+"\n")
#                    # You can also speak the message by un-commenting the next line
#                    java.lang.Runtime.getRuntime().exec('nircmd speak text "' + msg +'" -2 100')
#                    #java.lang.Runtime.getRuntime().exec(["speak", msg])
#                    #java.lang.Runtime.getRuntime().exec(["C:\Program Files (x86)\eSpeak\command_line\espeak", msg])
#                    scanPntr += 1 #decrement counter
#                    if scanPntr > maxArr : #reset to zero if greater than max value
#                        scanPntr = 0
#            except InterruptedException, e :
#                pass
# ******************************************************************************

# *****************************************************************************************
# ************* Section to Listen to all sensors, printing a line when they change state. *
# *****************************************************************************************

# **********************************************
# Define routine to map status numbers to text *
# **********************************************
def stateName(state):
    if (state == ACTIVE):
        return "ACTIVE"
    if (state == INACTIVE):
        return "INACTIVE"
    if (state == INCONSISTENT):
        return "INCONSISTENT"
    if (state == UNKNOWN):
        return "UNKNOWN"
    return "(invalid)"
    
# ****************************************
# Define the sensor listener: Print some *
# information on the status change.      *
# ****************************************
class SensorListener(java.beans.PropertyChangeListener):

    def propertyChange(self, event):
        global snrStatusArr
        
        #tmsg = "event.propertyName = "+event.propertyName
        #scrollArea.setText(scrollArea.getText()+tmsg+"\n")
        #tmsg = "event.source.systemName = "+event.source.systemName
        #scrollArea.setText(scrollArea.getText()+tmsg+"\n")
        #print event.propertyName
        if (event.propertyName == "KnownState"):
            systemName = event.source.systemName
            mesg = "Sensor " + systemName
            mesg = mesg.replace(mesg[:2], '') #delete first two characters
            if (event.source.userName != None):
                mesg += " (" + event.source.userName + ")"
            mesg += " is now " + stateName(event.newValue)
            #mesg += " from "+stateName(event.oldValue)
            #mesg += " to "+stateName(event.newValue)
            # print mesg
            # display and/or speak if either range value is empty
            snrStatusArr[systemName] = event.newValue
        return
        
listener = SensorListener()

# **********************************************************************
# Define a Manager listener.  When invoked, a new                      *
# item has been added, so go through the list of items removing the    *
# old listener and adding a new one (works for both already registered *
# and new sensors)                                                     *
# **********************************************************************
class ManagerListener(java.beans.PropertyChangeListener):

    def propertyChange(self, event):
        list = event.source.getSystemNameList()
        for i in range(list.size()):
            event.source.getSensor(list.get(i)).removePropertyChangeListener(listener)
            event.source.getSensor(list.get(i)).addPropertyChangeListener(listener)
            
# Attach the sensor manager listener
sensors.addPropertyChangeListener(ManagerListener())

# **********************************
# ******************** End section *
# **********************************

# have the text field enable the button when OK NOT used
def whenAddressChanged(event):
    if (address.text != ""): #address only changed if a value was entered
        swAddrCheckBox.setEnabled(True)
    else:
        swAddrCheckBox.setSelected(False)
        swAddrCheckBox.setEnabled(False)
    return
    
# define what button does when clicked and attach that routine to the button
def whenEnterButtonClicked(event): #not used
    return
    
def whenResyncButtonClicked(event):
    global resyncInProgress
    global resyncStrtTime
    global checkRestoreDone
    global lastSnSpkChgCbx
    global lastSigSpkChgCbx

    # get current speak checkbox states, then uncheck
    lastSnSpkChgCbx = snSpkChgCheckBox.isSelected()
    snSpkChgCheckBox.setSelected(False)
    lastSigSpkChgCbx = sigSpkChgCheckBox.isSelected()
    sigSpkChgCheckBox.setSelected(False)
    resyncInProgress = True #set flag to resync in progress now
    checkRestoreDone = False #set flag to checkRestoreDone needed
    powermanager.setPower(jmri.PowerManager.ON) # send GPON to chief which will query all decoders
    resyncStrtTime = time.clock() #set resync starting time now
    
    print 'resyncStrtTime = ', resyncStrtTime
    print 'resyncInProgress = ', resyncInProgress
    print 'lastSnSpkChgCbx = ', lastSnSpkChgCbx
    print 'lastSigSpkChgCbx = ', lastSigSpkChgCbx
    
    # wait 15 secs for queries responses to complete, then return checkboxes to their entry states
    #time.sleep(15.0)
    #snSpkChgCheckBox.setSelected(lastSnSpkChgCbx)
    #sigSpkChgCheckBox.setSelected(lastSigSpkChgCbx)
    return
    
def whenClearButtonClicked(event):
    #print "got to here"
    scrollArea.setText("") #clear the text area
    return
    
def whenQuitButtonClicked(event): #not used
    return
    
# *************************************************************************
# WindowListener is a interface class and therefore all of it's           *
# methods should be implemented even if not used to avoid AttributeErrors *
# *************************************************************************
class WinListener(java.awt.event.WindowListener):

    def windowClosing(self, event):
        global killed
        
        #print "window closing"
        killed = True #this will signal scanReporter thread to exit
        time.sleep(2.0) #give it a chance to happen before going on
        jmri.jmrix.loconet.LnTrafficController.instance().removeLocoNetListener(0xFF, lnListen)
        list = sensors.getSystemNameList()
        
        for i in range(list.size()):    #remove each of the sensor listeners that were added
        
            sensors.getSensor(list.get(i)).removePropertyChangeListener(listener)
            # print "remove"
            
        fr.dispose()         #close the pane (window)
        return
        
    def windowActivated(self, event):
        return
        
    def windowDeactivated(self, event):
        return
        
    def windowOpened(self, event):
        return
        
    def windowClosed(self, event):
        #print 'window closed'
        return
        
    def windowIconified(self, event):
        return
        
    def windowDeiconified(self, event):
        return
        
# *************************************
#create a Llnmon to parse the message *
# *************************************
parseMsg = jmri.jmrix.loconet.locomon.Llnmon()
#workarounds for jython not knowing jmri sensor, turnout, or reporter objects
parseMsg.setLocoNetSensorManager(sensors)
parseMsg.setLocoNetTurnoutManager(turnouts)
parseMsg.setLocoNetReporterManager(reporters)

# **************************************************
#class to handle a listener event loconet messages *
#                                                  *
# OpCode values: 131 = 0x83 = OPC_GPON             *
#                229 = 0xE5 = OPC_PEER_XFER        *
#                                                  *
#                176 = 0xB0 = OPC_SW_REQ           *
#                177 = 0xB1 = OPC_SW_REP           *
#                178 = 0xB2 = OPC_INPUT_REP        *
#                237 = 0xED = OPC_IMM_PACKET       *
#                                                  *
# **************************************************
class MsgListener(jmri.jmrix.loconet.LocoNetListener):

    def message(self, msg):
        global aspectColor
        global accumByteCnt
        global address
        global address2
        global fbAddress
        global fbAddress2
        global command
        global command2
        
        newMsgOpCodeHex = msg.getOpCodeHex()
        newMsgLength = msg.getNumDataElements()
        accumByteCnt += newMsgLength
        # Note: accumByteCnt background task will read and be reset to zero and plotted every 1 second
        
        ##print ">>cmd = ", newMsgOpCodeHex, " msg len ",java.lang.Integer.toString(newMsgLength), "bytes"
        #print "byte 0 = ", java.lang.Integer.toHexString(msg.getElement(0))
        #print "command 1 field = ", java.lang.Integer(command.text)
        #print "command 1 field = ", command.text
        
        # Special handling for Trolley Hill Hold up warning if trolley entering block 
        # from Spencer loop and interchange turnout set to normal
        
        # if MESSAGE indicates STS has gone unoccupied (LS105 == Inactive) <- leaving STS
        # and hill hold is UP                          (LT31 == THROWN)    <- track blocked by hillhold
        # and (Trolley Interchange is NORMAL           (LT1 == CLOSED)     <- track not set for Interchange
        # and STS is unoccupied                        (LS102 == Inactive) <- coming out of loop
        # and single to mains is NORMAL                (LT2 == CLOSED)    <- spring turnout set for Town Main
        # give Warning message "Collision Warning! Trolley Hill Hold is Up!"
        if (msg.getOpCode() == 178) and (msg.sensorAddr() + 1 == 105) and ((msg.getElement(2) & 0x10) != 0x10): #watch for incoming message that STS is unoccupied
            print "STS has reported unoccupied"
            ##if (LT31 == THROWN)
            if turnouts.provideTurnout("31").getState() == THROWN:
                print "Hillhold is UP"
                ##if (LT1 == THROWN)
                if turnouts.provideTurnout("1").getState() == CLOSED:
                    print "RR Interchange is CLOSED"
                    self.waitMsec(500)         # time is in milliseconds
                    ##if (LS102 == INACTIVE)
                    if sensors.provideSensor("102").getState() == INACTIVE:
                        print "STN is reporting unoccupied"
                        ##if (LT2 == THROWN)
                        if turnouts.provideTurnout("2").getState() == CLOSED:
                            hhWarning = "Collision warning! Trolley Hillhold is UP!"
                            print hhWarning
                            pid = java.lang.Runtime.getRuntime().exec('nircmd speak text "' + hhWarning +'" -2 100')
                            pid.waitFor()
                            
        # Handle check boxes
        if swAddrCheckBox.isSelected() or fbAddrCheckBox.isSelected() or snAddrCheckBox.isSelected() or filterCheckBox.isSelected() or (msg.getOpCode() == 237 and msg.getElement(3) & 0xF0 == 0x30):
            if filterCheckBox.isSelected():
                if msg.getOpCode() == 176 or msg.getOpCode() == 177 or msg.getOpCode() == 178:
                    displayMsg(msg)
            else:
                if swAddrCheckBox.isSelected() and msg.getOpCode() == 176:    #msg.turnoutAddr() is an int and address.text is a string
                    if len(address.text) > 0 or len(address2.text) > 0:
                        if len(address.text) > 0: #if field let bank code stops code for some reason
                            if msg.turnoutAddr() == java.lang.Integer.decode(address.text):
                                displayMsg(msg)
                        if len(address2.text) > 0: #if field let bank code stops code for some reason
                            if msg.turnoutAddr() == java.lang.Integer.decode(address2.text):
                                displayMsg(msg)                         
                    else:                
                        displayMsg(msg)
                if fbAddrCheckBox.isSelected() and msg.getOpCode() == 177:    #msg.turnoutAddr() is an int and address.text is a string
                    if len(fbAddress.text) > 0 or len(fbAddress2.text) > 0:
                        if len(fbAddress.text) > 0: #if field let bank code stops code for some reason
                            if msg.turnoutAddr() == java.lang.Integer.decode(fbAddress.text):
                                displayMsg(msg)
                        if len(fbAddress2.text) > 0: #if field let bank code stops code for some reason
                            if msg.turnoutAddr() == java.lang.Integer.decode(fbAddress2.text):
                                displayMsg(msg)
                    else:                
                        displayMsg(msg)
                if snAddrCheckBox.isSelected() and msg.getOpCode() == 178:
                    if len(command.text) > 0 or len(command2.text) > 0 or len(rangeAdd1.text) > 0:
                        if len(command.text) > 0: #if field let bank code stops code for some reason
                            if msg.sensorAddr() + 1 == java.lang.Integer.decode(command.text):
                                displayMsg(msg)                         
                        if len(command2.text) > 0:
                            if msg.sensorAddr() + 1 == java.lang.Integer.decode(command2.text):
                                displayMsg(msg)                         
                        if len(rangeAdd1.text) > 0 and len(rangeAdd2.text) > 0:
                            if msg.sensorAddr() + 1 >= java.lang.Integer.decode(rangeAdd1.text) and msg.sensorAddr() + 1 <= java.lang.Integer.decode(rangeAdd2.text):
                                displayMsg(msg)
                    else:
                        displayMsg(msg)
                # Extended accessory decoder packet format 10AAAAAA 0AAA0AA1 000XXXXX
                # Address AAA AAAAAA AA
                if sigChgCheckBox.isSelected():
                    if msg.getOpCode() == 237 and msg.getElement(3) & 0xF0 == 0x30:
                    # if msg.getOpCode() == 237 :
                        DHI = msg.getElement(4)
                        raddrlo = msg.getElement(5)
                        raddrhi = msg.getElement(6)
                        if DHI & 0x02 == 0x02:
                            raddrhi = raddrhi | 0x80 #set bit 7
                        if DHI & 0x01 == 0x01:
                            raddrlo = raddrlo | 0x80
                        raddrlo = raddrlo << 2 #get hi bits of low address
                        raddrlo = raddrlo & 0xFC #clear low 2 bits
                        temp = raddrhi & 0x06
                        temp = temp >> 1
                        raddrlo = raddrlo | temp
                        raddrhi = raddrhi >> 4
                        raddrhi = ~ raddrhi  #complement
                        raddrhi = (raddrhi & 0x07) * 256
                        address = raddrlo + raddrhi + 1
                        aspect = msg.getElement(7)
                        aspectName(aspect)
                        scrollArea.setText(scrollArea.getText() + "Signal Head " + str(address) + " " + aspectColor + " " + str(aspect) + "\n")
                        if sigSpkChgCheckBox.isSelected():
                            pid = java.lang.Runtime.getRuntime().exec('nircmd speak text "' + "Signal Mast " + str(address) + " " + aspectColor + " " + '" -2 100')
                            pid.waitFor()
        else: #if no boxes are check just display the message
            # print "no check boxes"
            ###cnvMsg = parseMsg.displayMessage(msg)
            ###java.lang.Runtime.getRuntime().exec('nircmd speak text "' + cnvMsg +'" -2 100')
            #displayMsg(msg)
            if not(sigChgCheckBox.isSelected() or snChgCheckBox.isSelected()):
                print 'newMsgOpCodeHex = ', newMsgOpCodeHex
        return

# *************************************************************************
# Add the next Loconet messsage to the scroll area text                   *
# If the raw box is checked add the HEX message value in front of message *
# *************************************************************************
def displayMsg(msg):
    log.info("sensors is {}", sensors)
    log.info("msg is {}", msg)
    #scrollArea.setText(scrollArea.getText()+"got to displayMsg\n") #displayMessage adds a carriage return
    if timeCheckBox.isSelected():
        scrollArea.setText(scrollArea.getText() + time.strftime('%X:') + " ")
    if rawCheckBox.isSelected():
        #scrollArea.setText(scrollArea.getText() + "[" + msg.encode("utf-8") + "] ")
        scrollArea.setText(scrollArea.getText() + "[" + msg.toString() + "] ")
    scrollArea.setText(scrollArea.getText() + parseMsg.displayMessage(msg)) #displayMessage adds a carriage return
    #[replace with none parseMsg version if line above is commented and line below is uncommented]
    #scrollArea.setText(scrollArea.getText()+msg+"\n") #displayMessage adds a carriage return
    return
    
# *************************************************************************
# Add the next Loconet messsage translation to the scroll area text       *
# If the raw box is checked add the HEX message value in front of message *
# *************************************************************************
def displayTxt(msg) :
    #scrollArea.setText(scrollArea.getText()+"got to displayTxt\n") #displayMessage adds a carriage return
    if timeCheckBox.isSelected() :
        scrollArea.setText(scrollArea.getText()+time.strftime('%X:')+" ")           
    if rawCheckBox.isSelected() :
        #scrollArea.setText(scrollArea.getText()+"["+msg.toString()+"] ")
        #scrollArea.setText(scrollArea.getText()+"["+msg.encode("utf-8").toString()+"] ")
        scrollArea.setText(scrollArea.getText() + "[" + msg.encode("utf-8") + "] ")
    #scrollArea.setText(scrollArea.getText()+parseMsg.displayMessage(msg)) #displayMessage adds a carriage return
    #[replace with none parseMsg version if line above is commented and line below is uncommented]
    scrollArea.setText(scrollArea.getText() + msg + "\n") #displayMessage adds a carriage return
    #time.sleep(0.03) #sleep for a 30 ms to allow foreground to run
    
    return
    
# ******************************************************************************
def aspectName(num):
    global aspectColor
    
    if num == 0:
        aspectColor = "Stop Signal, Red"
        return
    if num == 1:
        aspectColor = "Yellow"
        return
    if num == 2:
        aspectColor = "Green"
        return
    if num == 3:
        aspectColor = "Lunar"
        return
    if num == 4:
        aspectColor = "Restricting, Flash Red"
        return
    if num == 5:
        aspectColor = "Flash Yellow"
        return
    if num == 6:
        aspectColor = "Flash Green"
        return
    if num == 7:
        aspectColor = "Flash Lunar"
        return
    if num == 8:
        aspectColor = "Dark"
        return
    # added for NVMR Layout
    if num == 29:
        aspectColor = "Clear, Green over Red"
        return
    if num == 27:
        aspectColor = "Approach Limited, Yellow over Flash Green"
        return
    if num == 20:
        aspectColor = "Limited Clear, Red over Flash Green"
        return
    if num == 25:
        aspectColor = "Approach Medium, Yellow over Green"
        return
    if num == 23:
        aspectColor = "Advance Approach, Flash Yellow over Red"
        return
    if num == 15:
        aspectColor = "Medium Clear, Red over Green"
        return
    if num == 22:
        aspectColor = "Approach Slow, Yellow over Yellow"
        return
    if num == 21:
        aspectColor = "Approach, Yellow over Red"
        return
    if num == 11:
        aspectColor = "Red over Flash Yellow, Medium Approach"
        return
    aspectColor = "Unknown Aspect Color"
    return
    
# ##########################################################################
# ************* Start of Main Setup
# ##########################################################################

# *************************************
# start to initialise the display GUI *
# *************************************

# =================================
# create buttons and define action
# =================================
enterButton = javax.swing.JButton("Start the Run")
enterButton.setEnabled(False)           #button starts as grayed out (disabled)
enterButton.actionPerformed = whenEnterButtonClicked

resyncButton = javax.swing.JButton("Resync")
resyncButton.actionPerformed = whenResyncButtonClicked

clearButton = javax.swing.JButton("Clear")
clearButton.actionPerformed = whenClearButtonClicked

quitButton = javax.swing.JButton("Quit")
quitButton.actionPerformed = whenQuitButtonClicked

# ====================================
# create checkboxes and define action
# ====================================
swAddrCheckBox = javax.swing.JCheckBox("Filter Sw address")
swAddrCheckBox.setToolTipText("Display all switch messages or with the address")
#swAddrCheckBox.setEnabled(False)           #button starts as grayed out (disabled)

snAddrCheckBox = javax.swing.JCheckBox("Filter Sn address")
snAddrCheckBox.setToolTipText("Display all sensor messages or with the address")
#snAddrCheckBox.setEnabled(False)           #button starts as grayed out (disabled)

fbAddrCheckBox = javax.swing.JCheckBox("Filter Fb address")
fbAddrCheckBox.setToolTipText("Display all feedback messages or with the address")

filterCheckBox = javax.swing.JCheckBox("Filter SW, FB & SN")
filterCheckBox.setToolTipText("Display switch, feedback or sensor messages")

rawCheckBox = javax.swing.JCheckBox("Show raw data")

timeCheckBox = javax.swing.JCheckBox("Show time stamp")

indexCheckBox = javax.swing.JCheckBox("Show msg string index")

snChgCheckBox = javax.swing.JCheckBox("Show Sn Change")
snChgCheckBox.setToolTipText("Display when a sensor state changes")

snSpkChgCheckBox = javax.swing.JCheckBox("Speak Sn Change")
snSpkChgCheckBox.setToolTipText("Speak when a sensor state changes")

sigChgCheckBox = javax.swing.JCheckBox("Show Signal Decode Only")
sigChgCheckBox.setToolTipText("Display when a Signal state changes")

sigSpkChgCheckBox = javax.swing.JCheckBox("Speak Signal Change")
sigSpkChgCheckBox.setToolTipText("Speak when a signal aspect changes")

# =====================================
# create text fields and define action
# =====================================
address = javax.swing.JTextField(5)    #sized to hold 5 characters, initially empty
#address.actionPerformed = whenAddressChanged   #if user hit return or enter
#address.focusLost = whenAddressChanged         #if user tabs away
address2 = javax.swing.JTextField(5)    #sized to hold 5 characters, initially empty

fbAddress = javax.swing.JTextField(5)
fbAddress2 = javax.swing.JTextField(5)

# create the another text field similarly
command = javax.swing.JTextField(5)    #sized to hold 5 characters
command.setToolTipText("Number from 1 to 2010")
#command.actionPerformed = whenCommandChanged
#command.focusLost = whenCommandChanged

# create the another text field
# before this text field works there must be a number in the command field
command2 = javax.swing.JTextField(5)    #sized to hold 5 characters
command2.setToolTipText("Number from 1 to 2010")
#command2.actionPerformed = whenCommandChanged
#command2.focusLost = whenCommandChanged

# create another text field for a range address
rangeAdd1 = javax.swing.JTextField(5)    #sized to hold 5 characters
rangeAdd1.setToolTipText("Start address")

# create another text field for a range address
rangeAdd2 = javax.swing.JTextField(5)    #sized to hold 5 characters
rangeAdd2.setToolTipText("End address")

# create a text area
scrollArea = javax.swing.JTextArea(10, 45)    #define a text area with it's size
scrollArea.getCaret().setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); # automatically scroll to last message
# scrollArea.setText("Put any init text here\n")
scrollField = javax.swing.JScrollPane(scrollArea) #put text area in scroll field
scrollField.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
scrollField.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS)

# ---------------------------------------------------------------------------------
# create a panel to put the scroll area in
# a borderlayout causes the scroll area to fill the space as the window is resized
# ---------------------------------------------------------------------------------
midPanel = javax.swing.JPanel()
# midPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1,8,1,8, java.awt.Color.white))
midPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 1, 8))
midPanel.setLayout(java.awt.BorderLayout())
midPanel.add(scrollField)

# ------------------------------------------------------------------------------------------
# create a frame to hold the buttons and fields
# also create a window listener. This is used mainly to remove the property change listener
# when the window is closed by clicking on the window close button
# ------------------------------------------------------------------------------------------
w = WinListener()
#fr = javax.swing.JFrame(apNameVersion)       #argument is the frames title
fr = jmri.util.JmriJFrame(apNameVersion) #use this in order to get it to appear on webserver
fr.contentPane.setLayout(javax.swing.BoxLayout(fr.contentPane, javax.swing.BoxLayout.Y_AXIS))
fr.addWindowListener(w)

# -------------------------------------------------
# put the text field on a line preceded by a label
# -------------------------------------------------
addressPanel = javax.swing.JPanel()
addressPanel.setLayout(java.awt.FlowLayout(2))    #2 is right align for FlowLayout
addressPanel.add(javax.swing.JLabel("Sw Addresses"))
addressPanel.add(address)
addressPanel.add(javax.swing.JLabel("or"))
addressPanel.add(address2)

# ---------------------------------------------------------------------------------------
fbAddressPanel = javax.swing.JPanel()
fbAddressPanel.setLayout(java.awt.FlowLayout(2))    #2 is right align for FlowLayout
fbAddressPanel.add(javax.swing.JLabel("Fb Addresses"))
fbAddressPanel.add(fbAddress)
fbAddressPanel.add(javax.swing.JLabel("or"))
fbAddressPanel.add(fbAddress2)

# ---------------------------------------------------------------------------------------
commandPanel = javax.swing.JPanel()
commandPanel.setLayout(java.awt.FlowLayout(2))    #2 is right align for FlowLayout
commandPanel.add(javax.swing.JLabel("Sn Addresses"))
commandPanel.add(command)
commandPanel.add(javax.swing.JLabel("or"))
commandPanel.add(command2)

# ---------------------------------------------------------------------------------------
rangePanel = javax.swing.JPanel()
rangePanel.setLayout(java.awt.FlowLayout(2))    #2 is right align for FlowLayout
rangePanel.add(javax.swing.JLabel("Sn Addr Range"))
rangePanel.add(rangeAdd1)
rangePanel.add(javax.swing.JLabel("to"))
rangePanel.add(rangeAdd2)

# ---------------------------------------------------------------------------------------
temppanel1 = javax.swing.JPanel()
temppanel1.setLayout(javax.swing.BoxLayout(temppanel1, javax.swing.BoxLayout.PAGE_AXIS))
temppanel1.add(addressPanel)
temppanel1.add(fbAddressPanel)
temppanel1.add(commandPanel)
temppanel1.add(rangePanel)

# ---------------------------------------------------------------------------------------
ckBoxPanel = javax.swing.JPanel()
ckBoxPanel.setLayout(javax.swing.BoxLayout(ckBoxPanel, javax.swing.BoxLayout.PAGE_AXIS))
ckBoxPanel.add(filterCheckBox)
ckBoxPanel.add(swAddrCheckBox)
ckBoxPanel.add(fbAddrCheckBox)
ckBoxPanel.add(snAddrCheckBox)
ckBoxPanel.add(rawCheckBox)
ckBoxPanel.add(timeCheckBox)
ckBoxPanel.add(indexCheckBox)
ckBoxPanel.add(snChgCheckBox)
ckBoxPanel.add(snSpkChgCheckBox)
ckBoxPanel.add(sigChgCheckBox)
ckBoxPanel.add(sigSpkChgCheckBox)

# ---------------------------------------------------------------------------------------
butPanel = javax.swing.JPanel()
butPanel.setLayout(javax.swing.BoxLayout(butPanel, javax.swing.BoxLayout.PAGE_AXIS))
butPanel.add(resyncButton)
butPanel.add(javax.swing.Box.createVerticalStrut(20)) #empty vertical space between buttons
butPanel.add(clearButton)

# ---------------------------------------------------------------------------------------
buttonPanel = javax.swing.JPanel()
buttonPanel.add(butPanel)

# ---------------------------------------------------------------------------------------
blankPanel = javax.swing.JPanel()
blankPanel.setLayout(java.awt.BorderLayout())

entryPanel = javax.swing.JPanel()
entryPanel.setLayout(javax.swing.BoxLayout(entryPanel, javax.swing.BoxLayout.LINE_AXIS))
entryPanel.add(temppanel1)
entryPanel.add(ckBoxPanel)
entryPanel.add(buttonPanel)
entryPanel.add(blankPanel)

# ---------------------------------------------------------------------------------------
# create the top panel
# it is a 1,1 GridLayout used to keep all controls stationary when the window is resized
# ---------------------------------------------------------------------------------------
topPanel = javax.swing.JPanel()
topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 8, 8))
topPanel.setLayout(java.awt.GridLayout(1, 1))
topPanel.add(entryPanel)

# -------------------------------------------------------------------
# create a bottom panel to give some space under the scrolling field
# -------------------------------------------------------------------
bottomPanel = javax.swing.JPanel()
# bottomPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1,8,1,8))

# ------------------------------------
# create a time series charting panel
# ------------------------------------
dataset = TimeSeriesCollection()

hzline80 = TimeSeries("80% Used", Millisecond)
hzline60 = TimeSeries("60% Used", Millisecond)
hzline30 = TimeSeries("30% Used", Millisecond)
hzline15 = TimeSeries("15% Used", Millisecond)
if doSecsPlot:
    series = TimeSeries("All LocoNet Traffic", Millisecond)
if doAvgPlot:
    avgAccum = TimeSeries(str(secs2avg) + " Second Byte Average", Millisecond)

#info for operator sent to system console
print '80% Used = 1646 bytes'
print '60% Used = 1234 bytes'
print '30% Used = 617 bytes'
print '15% Used = 309 bytes'

dataset.addSeries(hzline80)
dataset.addSeries(hzline60)
dataset.addSeries(hzline30)
dataset.addSeries(hzline15)
if doSecsPlot:
    dataset.addSeries(series)
if doAvgPlot:
    dataset.addSeries(avgAccum)

chart = createChart(dataset)
chartPanel = ChartPanel(chart)

# ----------------------------------
# Put contents in frame and display
# ----------------------------------
fr.contentPane.add(topPanel)
fr.contentPane.add(midPanel)
fr.contentPane.add(bottomPanel)
if doAvgPlot or doSecsPlot:
    fr.contentPane.add(chartPanel)
fr.pack()
#fr.show() #depreciated
fr.setVisible(True)

# ---------------------------------
#create and start LocoNet listener
# ---------------------------------
lnListen = MsgListener() #create and start LocoNet listener
jmri.jmrix.loconet.LnTrafficController.instance().addLocoNetListener(0xFF, lnListen)

# --------------------------------------------------------------------------------
# force first three points to min and max of plot range to set range (not needed)
# --------------------------------------------------------------------------------
#series.add(Millisecond(), 0.0)    #set first point to min scale range value
#time.sleep(0.1) # sleep for tenth of a second
#series.add(Millisecond(), 2000.0) #set second point to max scale range value
#time.sleep(0.1) # sleep for tenth of a second
#series.add(Millisecond(), 0.0)    #set third point to min scale range value

scrollArea.setText(scrollArea.getText() + mainStartTime + "\n")
scrollArea.setText(scrollArea.getText() + "[1]" + time.strftime('%X %x %Z') + "\n")

# **************************************************************
# populate Sensor Message ID List from "LnSnrMsgList.csv" file *
# **************************************************************
snrStatusArr = {} # dictionary of sensor systemNames and status
for systemName in sensors.getSystemNameList():
    sensor = sensors.getSensor(systemName)
    userName = sensor.userName
    # add sensors with a userName starting with userNamePrefix to snrStatusArr
    # and set the initial state for that sensor
    # (test that userName is not null first)
    if userName and userName.startswith(userNamePrefix):
        snrStatusArr[systemName] = sensor.knownState
        sensor.addPropertyChangeListener(listener)

scrollArea.setText(scrollArea.getText() + str(len(snrStatusArr)) + " short detectors\n")

scrollArea.setText(scrollArea.getText() + "[2]" + time.strftime('%X %x %Z') + "\n")
scrollArea.setText(scrollArea.getText() + "[3]" + time.strftime('%X %x %Z') + "\n")
scrollArea.setText(scrollArea.getText() + "[4]" + time.strftime('%X %x %Z') + "\n")
scrollArea.setText(scrollArea.getText() + "[5]" + time.strftime('%X %x %Z') + "\n")

scanPntr = 0 # set starting value of scanning pointer to point to first entry
scrollArea.setText(scrollArea.getText() + "[6]" + time.strftime('%X %x %Z') + "\n")
# init done
scrollArea.setText(scrollArea.getText()+"[init ended]"+time.strftime('%X %x %Z')+"\n")
scrollArea.setText(scrollArea.getText()+"Init done\n")
print 'init done'
# #########################################################################
# ************* Start of Background Tasks from Main and other final steps #
# #########################################################################

###time.sleep(15.0) #wait 15 secs to complete JMRI startup before launching background tasks
###
if doAvgPlot or doSecsPlot:
    thread.start_new_thread(plotUpdater, ())
time.sleep(1.0) #wait 1 seconds before starting scanReporter after plotUpdater started
###
thread.start_new_thread(scanReporter, ())
# ###############################################################################################
# send a LocoNet GPON to force reporting of all sensor status conditions so we can collect them #
# this can now go away since JMRI is holding all stati for use                                  #
# ###############################################################################################
print 'starting status querying'
print time.strftime('%X %x %Z')
powermanager.setPower(jmri.PowerManager.ON)
print time.strftime('%X %x %Z')
#time.sleep(15.0) # (handled elsewhere) wait 15 secs to complete sensor reporting
print 'status reporting done'
print time.strftime('%X %x %Z')
print 'main done'
print os.getcwd() # reports where to put jfreechart & jcommon jar files
    
# ########################################
# my default startup test settings - gaw #
# ########################################
rangeAdd1.text = '3000' #lower address boundary 3000
rangeAdd2.text = '4080' #upper address boundary 4080
snChgCheckBox.setSelected(True) #sensor change message display on
snSpkChgCheckBox.setSelected(True) #sensor change message spoken on
    
# ##############################################
# display and speak ready for business message #
# ##############################################
msg = sensors.getSensor(readySensorSystemName).comment
    
scrollArea.setText(scrollArea.getText() + msg + "\n")
# this is where you select the voice synthesizer (speak, espeak, or nircmd)
if snSpkChgCheckBox.isSelected():
    #pid = java.lang.Runtime.getRuntime().exec(["speak", msg])
    #pid = java.lang.Runtime.getRuntime().exec(["C:\Program Files (x86)\eSpeak\command_line\espeak", msg])
    pid = java.lang.Runtime.getRuntime().exec('nircmd speak text "' + msg + '" -2 100')
    pid.waitFor()

# ###########################
# ************* End of Main #
# ###########################
