###########################################################################
# Dispatcher System
###########################################################################

import java
from java.awt import Dimension
from javax.swing import JButton, JFrame,JPanel,BoxLayout,Box
from javax.swing import JLabel, JMenu, JMenuItem, JMenuBar
from javax.swing import JFileChooser,JTextField, BorderFactory
from javax.swing import SwingWorker, SwingUtilities
from javax.swing import WindowConstants
from java.awt import Color, Font
import jmri

from xml.etree.ElementTree import ElementTree
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import SubElement
import xml.etree.ElementTree as ET
import subprocess

import sys
my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
sys.path.append(my_path_to_jars) # add the jar to your path
from org.jgrapht.graph import DefaultWeightedEdge
from org.jgrapht.graph import DirectedWeightedMultigraph
import threading
import time
import webbrowser



start_file = ""
run_file = ""
directory = ""

#*****************
# Set Program locations, and include code
#*****************
CreateIcons = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateIcons.py')
execfile(CreateIcons)

#*****************
frame = JFrame()
frame.setTitle("Dispatch System")
#frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) #comment as don't want to close down JMRI
frame.setSize(700, 550)

panel = JPanel()
panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
frame.add(panel)

#*****Menu*******
bar = JMenuBar()
jmri.util.HelpUtil.helpMenu(bar, 'html.scripthelp.DispatcherSystem.DispatcherSystem' , True)
frame.setJMenuBar(bar)
#
logLevel = 0

###info
import os

def strip_end(text, suffix):
    if not text.endswith(suffix):
        return text
    return text[:len(text)-len(suffix)]

def btnpanelLocation_action(event):
    global icons_file
    global run_file
    global start_file
    #print "clicked"
    chooser = jmri.configurexml.LoadStoreBaseAction.getUserFileChooser()
    returnVal = chooser.showOpenDialog(frame)
    current_file = str(chooser.getSelectedFile())
    #print current_file
    filepath = os.path.dirname(current_file)
    root = os.path.splitext(os.path.basename(current_file))
    old_filename = root[0]
    filetype  = root[1]
    #print old_filename
    if "run" not in old_filename and "icons" not in old_filename:
        #print "run not in filepath"
        start_file = current_file
        icons_file = filepath + "/" + old_filename + "_icons" + filetype
        run_file = filepath + "/" + old_filename + "_run" + filetype
    else:
        stripped_filename = strip_end(old_filename,"_icons")
        stripped_filename = strip_end(stripped_filename,"_run")
        start_file = filepath + "/" + stripped_filename + filetype
        icons_file = filepath + "/" + stripped_filename + "_icons" + filetype
        run_file = filepath + "/" + stripped_filename + "_run" + filetype
    label_panel_location.text = start_file
    #row13b2.text = icons_file
    row11b2.text = run_file
    #os.rename(r'filepath/old_filename.file type',r'file path/NEW file name.file type')

row0 = JPanel()
row0.setLayout(BoxLayout(row0, BoxLayout.X_AXIS))
txt = JTextField(140)
txt.setMaximumSize( txt.getPreferredSize() );
txt.setBorder(BorderFactory.createCompoundBorder(
                   BorderFactory.createLineBorder(Color.red),
                   txt.getBorder()));
label_panel_location = JLabel()
btnpanelLocation = JButton("Set Panel Location", actionPerformed = btnpanelLocation_action)
row0.add(Box.createVerticalGlue())
row0.add(Box.createRigidArea(Dimension(20, 0)))
row0.add(btnpanelLocation)
row0.add(Box.createRigidArea(Dimension(20, 0)))
row0.add(label_panel_location)
row0.add(Box.createRigidArea(Dimension(20, 0)))

row12 = JPanel()
row12.setLayout(BoxLayout(row12, BoxLayout.X_AXIS))
row12b1 = JLabel("Dispatcher System: Modifies panels to produce a running system")
row12b1.add( Box.createHorizontalGlue() );
row12b1.setAlignmentX( row12b1.LEFT_ALIGNMENT )
msg = ""
row12b2 = JLabel(msg)
row12b2.setAlignmentX( row12b1.RIGHT_ALIGNMENT )

row12.add(Box.createVerticalGlue())
row12.add(Box.createRigidArea(Dimension(20, 0)))
row12.add(row12b1)
row12.add(Box.createRigidArea(Dimension(20, 0)))
row12.add(row12b2)

row22 = JPanel()
row22.setLayout(BoxLayout(row22, BoxLayout.X_AXIS))
row22b1 = JLabel("We Start with the Original Panel")
row22b1.add( Box.createHorizontalGlue() );
row22b1.setAlignmentX( row22b1.LEFT_ALIGNMENT )
row22b2 = JLabel("")    #start_filename
row22b2.setAlignmentX( row22b2.RIGHT_ALIGNMENT )

row22.add(Box.createVerticalGlue())
row22.add(Box.createRigidArea(Dimension(20, 0)))
row22.add(row22b1)
row22.add(Box.createRigidArea(Dimension(20, 0)))
row22.add(row22b2)


row32 = JPanel()
row32.setLayout(BoxLayout(row32, BoxLayout.X_AXIS))
row32b1 = JLabel("Stage1: Add the icons to move the train")
row32b1.add( Box.createHorizontalGlue() );
row32b1.setAlignmentX( row32b1.LEFT_ALIGNMENT )
row32b2 = JLabel("")     #start_filename + "_icons"

row32.add(Box.createVerticalGlue())
row32.add(Box.createRigidArea(Dimension(20, 0)))
row32.add(row32b1)
row32.add(Box.createRigidArea(Dimension(20, 0)))
row32.add(row32b2)

row42 = JPanel()
row42.setLayout(BoxLayout(row42, BoxLayout.X_AXIS))
row42b1 = JLabel("Stage2: Add the transits and train-info files")
row42b1.add( Box.createHorizontalGlue() );
row42b1.setAlignmentX( row42b1.LEFT_ALIGNMENT )
row42b2 = JLabel("")     #start_filename + "_icons"

row42.add(Box.createVerticalGlue())
row42.add(Box.createRigidArea(Dimension(20, 0)))
row42.add(row42b1)
row42.add(Box.createRigidArea(Dimension(20, 0)))
row42.add(row42b2)

row52 = JPanel()
row52.setLayout(BoxLayout(row52, BoxLayout.X_AXIS))
row52b1 = JLabel("Stage3: Modify the Dispatcher Options so the trains move")
row52b1.add( Box.createHorizontalGlue() );
row52b1.setAlignmentX( row52b1.LEFT_ALIGNMENT )
row52b2 = JLabel("")     #start_filename + "_icons"

row52.add(Box.createVerticalGlue())
row52.add(Box.createRigidArea(Dimension(20, 0)))
row52.add(row52b1)
row52.add(Box.createRigidArea(Dimension(20, 0)))
row52.add(row52b2)

row62 = JPanel()
row62.setLayout(BoxLayout(row62, BoxLayout.X_AXIS))
row62b1 = JLabel("Stage3: Modify the Dispatcher Options so the trains move")
row62b1.add( Box.createHorizontalGlue() );
row62b1.setAlignmentX( row62b1.LEFT_ALIGNMENT )
row62b2 = JLabel("")     #start_filename + "_icons"

row62.add(Box.createVerticalGlue())
row62.add(Box.createRigidArea(Dimension(20, 0)))
row62.add(row62b1)
row62.add(Box.createRigidArea(Dimension(20, 0)))
row62.add(row62b2)

row03 = JPanel()
row03.setLayout(BoxLayout(row03, BoxLayout.X_AXIS))
row03b1 = JLabel("*******************************************************************")
row03b1.add( Box.createHorizontalGlue() );
row03b1.setAlignmentX( row03b1.LEFT_ALIGNMENT )
row03b2 = JLabel("")

row03.add(Box.createVerticalGlue())
row03.add(Box.createRigidArea(Dimension(20, 0)))
row03.add(row03b1)
row03.add(Box.createRigidArea(Dimension(20, 0)))
row03.add(row03b2)

row13 = JPanel()
row13.setLayout(BoxLayout(row13, BoxLayout.X_AXIS))
row13b1 = JLabel("*******************************************************************")
row13b1.add( Box.createHorizontalGlue() );
row13b1.setAlignmentX( row13b1.LEFT_ALIGNMENT )
row13b2 = JLabel("")

row13.add(Box.createVerticalGlue())
row13.add(Box.createRigidArea(Dimension(20, 0)))
row13.add(row13b1)
row13.add(Box.createRigidArea(Dimension(20, 0)))
row13.add(row13b2)

row43 = JPanel()
row43.setLayout(BoxLayout(row43, BoxLayout.X_AXIS))
row43b1 = JLabel("*******************************************************************")
row43b1.add( Box.createHorizontalGlue() );
row43b1.setAlignmentX( row43b1.LEFT_ALIGNMENT )
row43b2 = JLabel("")

row43.add(Box.createVerticalGlue())
row43.add(Box.createRigidArea(Dimension(20, 0)))
row43.add(row43b1)
row43.add(Box.createRigidArea(Dimension(20, 0)))
row43.add(row43b2)

row33 = JPanel()
row33.setLayout(BoxLayout(row33, BoxLayout.X_AXIS))
row33b1 = JLabel("*******************************************************************")
row33b1.add( Box.createHorizontalGlue() );
row33b1.setAlignmentX( row33b1.LEFT_ALIGNMENT )
row33b2 = JLabel("")

row33.add(Box.createVerticalGlue())
row33.add(Box.createRigidArea(Dimension(20, 0)))
row33.add(row33b1)
row33.add(Box.createRigidArea(Dimension(20, 0)))
row33.add(row33b2)

###row1L1
def CreateIcons_action(event):
    global f1
    initialPanelFilename = start_file
    finalPanelFilename = icons_file

    msg = "About to create file " + finalPanelFilename + "\n from " + initialPanelFilename
    msg = msg + "\n  *****************************************************"
    msg = msg + "\nPanel " + initialPanelFilename + " should be open for this stage to work"
    msg = msg + "\n  *****************************************************"
    msg = msg + "\nContinue?"
    myAnswer = JOptionPane.showConfirmDialog(None, msg)
    if myAnswer == JOptionPane.YES_OPTION:
        pass
    elif myAnswer == JOptionPane.NO_OPTION:
        msg = 'Stopping'
        JOptionPane.showMessageDialog(None, msg, 'Stopping', JOptionPane.WARNING_MESSAGE)
        return
    elif myAnswer == JOptionPane.CANCEL_OPTION:
        msg = 'Stopping'
        JOptionPane.showMessageDialog(None, msg, 'Stopping', JOptionPane.WARNING_MESSAGE)
        return
    elif myAnswer == JOptionPane.CLOSED_OPTION:
        #print "You closed the window. How rude!"
        return
    p = processXML(initialPanelFilename, finalPanelFilename )


def initialise_panel_location():
    global icons_file
    global run_file
    global start_file
    global directory
    global start_filename
    global loaded_filename
    #print "clicked"
    chooser = jmri.configurexml.LoadStoreBaseAction.getUserFileChooser()

    robot = java.awt.Robot()
    #press the save tab
    KeyEvent = java.awt.event.KeyEvent
    #button.requestFocus();
    #robot.delay(1000)
    robot.keyPress(KeyEvent.VK_TAB)
    robot.delay(10)
    robot.keyRelease(KeyEvent.VK_TAB)
    robot.delay(10)
    robot.keyPress(KeyEvent.VK_SPACE)
    robot.delay(10)
    robot.keyRelease(KeyEvent.VK_SPACE)
    robot.delay(10)
    robot.keyPress(KeyEvent.VK_ENTER)
    robot.delay(10)
    robot.keyRelease(KeyEvent.VK_ENTER)
    robot.delay(10)
    returnVal = chooser.showOpenDialog(frame)
    current_file = str(chooser.getSelectedFile())
    #print current_file
    filepath = os.path.dirname(current_file)
    directory = filepath
    root = os.path.splitext(os.path.basename(current_file))
    old_filename = root[0]
    filetype  = root[1]
    #print old_filename
    if "run" not in old_filename and "icons" not in old_filename:
        #print "run not in filepath"
        start_file = current_file
        icons_file = filepath + "/" + old_filename + "_icons" + filetype
        run_file = filepath + "/" + old_filename + "_run" + filetype
        start_filename = old_filename
        loaded_filename = old_filename
        stage_to_run = "Stage 1"
    else:
        stripped_filename = strip_end(old_filename,"_icons")
        stripped_filename = strip_end(stripped_filename,"_run")
        start_filename = stripped_filename
        start_file = filepath + "/" + stripped_filename + filetype
        icons_file = filepath + "/" + stripped_filename + "_icons" + filetype
        run_file = filepath + "/" + stripped_filename + "_run" + filetype
        if "icons" in old_filename:
            loaded_filename = stripped_filename + "_icons"
            stage_to_run = "Stage 2"
        else:
            loaded_filename = stripped_filename + "_run"
            stage_to_run = "Stage 3 then operate the trains"
    label_panel_location.text = start_file
    #row13b2.text = icons_file
    row11b2.text = run_file
    msg = "Panel Directory: " + str(directory)
    row12b2.text = msg
    row42b2.text = start_filename + filetype
    row32b2.text = "Produces: " + start_filename + "_icons" + filetype + " (from " + start_filename + filetype + ")"
    row42b2.text = "Produces: " + start_filename + "_run" + filetype + " (from " + start_filename + "_icons" + filetype + ")"
    row62b1.text = "You have " + loaded_filename + filetype + " loaded. You may run " + stage_to_run
    row62b1.setFont(row62b1.getFont().deriveFont(Font.BOLD, 13));

    row15b1.text = "When finished you need to restart JMRI and load the file created in Stage1: " + start_filename + "_icons" + filetype + " instead of " + start_filename + filetype
    row45b1.text = "When finished you need to restart JMRI and load the file created in Stage2: " + start_filename + "_run" + filetype + " instead of " + start_filename + "_icons" + filetype


row1 = JPanel()
row1.setLayout(BoxLayout(row1, BoxLayout.X_AXIS))
row1b1 = JLabel("Insert Icons:           Runs file CreateIcons.py")
row1b1.add( Box.createHorizontalGlue() );
row1b1.setAlignmentX( row1b1.LEFT_ALIGNMENT )
row1b2 = JButton("Stage1", actionPerformed = CreateIcons_action)

row1.add(Box.createVerticalGlue())
row1.add(Box.createRigidArea(Dimension(20, 0)))
row1.add(row1b2)
row1.add(Box.createRigidArea(Dimension(20, 0)))
row1.add(row1b1)

row11 = JPanel()
row11.setLayout(BoxLayout(row11, BoxLayout.X_AXIS))
row11b1 = JLabel("Stage2 just adds transits and traininfo files to the currently loaded system: ")
row11b1.add( Box.createHorizontalGlue() );
row11b1.setAlignmentX( row11b1.LEFT_ALIGNMENT )
row11b2 = JLabel("")

row11.add(Box.createVerticalGlue())
row11.add(Box.createRigidArea(Dimension(20, 0)))
row11.add(row11b1)
row11.add(Box.createRigidArea(Dimension(20, 0)))
row11.add(row11b2)

row15 = JPanel()
row15.setLayout(BoxLayout(row15, BoxLayout.X_AXIS))
row15b1 = JLabel("You need to restart JMRI and load the file created in Stage 1: ")
row15b1.add( Box.createHorizontalGlue() );
row15b1.setAlignmentX( row15b1.LEFT_ALIGNMENT )
row15b2 = JLabel("")

row15.add(Box.createVerticalGlue())
row15.add(Box.createRigidArea(Dimension(20, 0)))
row15.add(row15b1)
row15.add(Box.createRigidArea(Dimension(20, 0)))
row15.add(row15b2)

row45 = JPanel()
row45.setLayout(BoxLayout(row45, BoxLayout.X_AXIS))
row45b1 = JLabel("You need to restart JMRI and load the file created in Stage 1: ")
row45b1.add( Box.createHorizontalGlue() );
row45b1.setAlignmentX( row45b1.LEFT_ALIGNMENT )
row45b2 = JLabel("")

row45.add(Box.createVerticalGlue())
row45.add(Box.createRigidArea(Dimension(20, 0)))
row45.add(row45b1)
row45.add(Box.createRigidArea(Dimension(20, 0)))
row45.add(row45b2)

initialise_panel_location()
robot = java.awt.Robot()
KeyEvent = java.awt.event.KeyEvent


def setAdvancedRouting():
    jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).enableAdvancedRouting(True)

setAdvancedRouting()

def CreateTransits_action(event):
    #print "in create_transits"
    global g
    global le
    global DisplayProgress_global
    global logLevel
    
    
    #the displayProgress is in CreateTransits
    CreateTransits = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateTransits.py')
    exec(open (CreateTransits).read())
    DisplayProgress_global = DisplayProgress
    progress = 0
    dpg=DisplayProgress_global()
    dpg.Update("creating signal mast logic")

    initialPanelFilename = icons_file
    finalPanelFilename = run_file

    #print "Setting up Graph"
    my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
    import sys
    sys.path.append(my_path_to_jars) # add the jar to your path
    CreateGraph = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateGraph.py')
    exec(open (CreateGraph).read())
    le = LabelledEdge
    g = StationGraph()
    
    progress = 10
    dpg.Update(str(progress)+ "% complete")
    
    if logLevel > 0: print "updating logic"
    CreateSignalLogic = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateSignalLogicAndSections.py')
    exec(open (CreateSignalLogic).read())
    usl = Update_Signal_Logic()
    #print "updating logic stage1"
    
    ans = usl.create_autologic_and_sections()
     
    if ans == True: 
        print "updating logic stage2"
        usl.update_logic(run_file)
        
        progress = 15
        dpg.Update(str(progress)+ "% complete")

        #print "Creating Transits"
        CreateTransits = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateTransits.py')
        exec(open (CreateTransits).read())

        #print "about to run CreateTransits"
        ct = CreateTransits()
        
        ct.run_transits(icons_file, run_file)
        #print "ran CreateTransits"

    dpg.killLabel()

def show_options_message(msg):
    JOptionPane.showMessageDialog(None, msg);

def show_options_pane():
    DispatcherFrame = jmri.jmrit.dispatcher.DispatcherFrame()
    OptionsMenu = jmri.jmrit.dispatcher.OptionsMenu(DispatcherFrame)
    robot = java.awt.Robot()
    #press the save tab
    KeyEvent = java.awt.event.KeyEvent
    #button.requestFocus();
    #robot.delay(10)
    robot.keyPress(KeyEvent.VK_WINDOWS)
    robot.keyPress(KeyEvent.VK_F10)
    robot.delay(10)
    robot.keyRelease(KeyEvent.VK_F10)
    robot.delay(10)
    robot.keyRelease(KeyEvent.VK_WINDOWS)
    robot.delay(10)
    robot.keyPress(KeyEvent.VK_DOWN)
    robot.delay(10)
    robot.keyRelease(KeyEvent.VK_DOWN)
    robot.delay(10)
    robot.keyPress(KeyEvent.VK_DOWN)
    robot.delay(10)
    robot.keyRelease(KeyEvent.VK_DOWN)
    robot.delay(10)
    robot.keyPress(KeyEvent.VK_DOWN)
    robot.delay(10)
    robot.keyRelease(KeyEvent.VK_DOWN)
    robot.delay(10)
    robot.keyPress(KeyEvent.VK_SPACE)
    robot.delay(10)
    robot.keyRelease(KeyEvent.VK_SPACE)
    robot.delay(10)



def ChangeOptions_action(event):

    y = threading.Timer(0.1, function = show_options_pane)
    y.start()

    msg = "You need to set the following: \n\n Use connectivity from Layout panels\n Trains from Roster\n Layout has block detection hardware\n Automatically allocate Sections to Active Trains\n Automatically set turnouts when a Section is allocated\n\n You also need to set SignalMasts/SML (top RH)\n and the Layout scale\n\nSave your Options in the Menu in the Dispatcher Frame after checking.\n "
    x = threading.Timer(2.0, function=show_options_message, args=(msg,))
    x.start()

row2 = JPanel()
row2.setLayout(BoxLayout(row2, BoxLayout.X_AXIS))
row2b1 = JLabel("Create Transits and TrainInfo Fies:           Runs file CreateTransits.py")
row2b1.add( Box.createHorizontalGlue() );
row2b1.setAlignmentX( row2b1.LEFT_ALIGNMENT )

row2b2 = JButton("Stage2", actionPerformed = CreateTransits_action)

row2.add(Box.createVerticalGlue())
row2.add(Box.createRigidArea(Dimension(20, 0)))
row2.add(row2b2)
row2.add(Box.createRigidArea(Dimension(20, 0)))
row2.add(row2b1)

row4 = JPanel()
row4.setLayout(BoxLayout(row4, BoxLayout.X_AXIS))
row4b1 = JLabel("Change Dispatcher Options:           Enables Trains to Run Automatically")
row4b1.add( Box.createHorizontalGlue() );
row4b1.setAlignmentX( row4b1.LEFT_ALIGNMENT )

row4b2 = JButton("Stage3", actionPerformed = ChangeOptions_action)

row4.add(Box.createVerticalGlue())
row4.add(Box.createRigidArea(Dimension(20, 0)))
row4.add(row4b2)
row4.add(Box.createRigidArea(Dimension(20, 0)))
row4.add(row4b1)

row14 = JPanel()
row14.setLayout(BoxLayout(row14, BoxLayout.X_AXIS))
row14b1 = JLabel("You should be now set up to run the dispatcher system")
row14b1.add( Box.createHorizontalGlue() );
row14b1.setAlignmentX( row14b1.LEFT_ALIGNMENT )
row14b2 = JLabel("")

row14.add(Box.createVerticalGlue())
row14.add(Box.createRigidArea(Dimension(20, 0)))
row14.add(row14b1)
row14.add(Box.createRigidArea(Dimension(20, 0)))
row14.add(row14b2)

###top panel
def RunDispatcher_action(event):
    global DispatchMaster
    RunDispatch = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/RunDispatch.py')
    DispatchMaster = DispatchMaster
    exec(open (RunDispatch).read())

row3 = JPanel()
row3.setLayout(BoxLayout(row3, BoxLayout.X_AXIS))
b1 = JLabel("You my now run the dispatcher system from the panel")
b1.setAlignmentX( b1.LEFT_ALIGNMENT )
#b2 = JButton("Run", actionPerformed = RunDispatcher_action)
row3.add(Box.createVerticalGlue())
#row3.add(Box.createRigidArea(Dimension(20, 0)))
#row3.add(b2)
row3.add(Box.createRigidArea(Dimension(20, 0)))
row3.add(b1)

def leftJustify( panel ):
    b = Box.createHorizontalBox()
    b.add( panel )
    b.add( Box.createHorizontalGlue() )
    # (Note that you could throw a lot more components
    # and struts and glue in here.)
    return b

panel.add(leftJustify(row12))
panel.add(leftJustify(row62))
panel.add(leftJustify(row03))

#stage1
panel.add(leftJustify(row32))
panel.add(leftJustify(row1))
panel.add(leftJustify(row15))
panel.add(leftJustify(row43))

#panel.add(leftJustify(row11))

#stage2
panel.add(leftJustify(row42))
panel.add(leftJustify(row2))
panel.add(leftJustify(row45))
panel.add(leftJustify(row33))

#stage3
panel.add(leftJustify(row52))
panel.add(leftJustify(row4))
panel.add(leftJustify(row13))

panel.add(leftJustify(row14))
panel.add(leftJustify(row3))


frame.setVisible(True)
