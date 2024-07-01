###########################################################################
# Dispatcher System
###########################################################################

import java
from java.awt import Dimension
from javax.swing import JButton, JFrame,JPanel,BoxLayout,Box
from javax.swing import JLabel, JMenu, JMenuItem, JMenuBar
from javax.swing import JFileChooser,JTextField, BorderFactory
from javax.swing import SwingWorker, SwingUtilities
from javax.swing import WindowConstants, JDialog, JTextArea
from java.awt import Color, Font
from java.awt.event import WindowEvent
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
    # if "run" not in old_filename and "icons" not in old_filename:
    #     #print "run not in filepath"
    #     start_file = current_file
    #     icons_file = filepath + "/" + old_filename + "_icons" + filetype
    #     run_file = filepath + "/" + old_filename + "_run" + filetype
    # else:
    #     stripped_filename = strip_end(old_filename,"_icons")
    #     stripped_filename = strip_end(stripped_filename,"_run")
    #     start_file = filepath + "/" + stripped_filename + filetype
    #     icons_file = filepath + "/" + stripped_filename + "_icons" + filetype
    #     run_file = filepath + "/" + stripped_filename + "_run" + filetype
    start_file = current_file
    label_panel_location.text = start_file
    #row13b2.text = icons_file
    #row11b2.text = run_file
    #os.rename(r'filepath/old_filename.file type',r'file path/NEW file name.file type')

def CreateIcons_action(event):
    global f1
    initialPanelFilename = start_file
    finalPanelFilename = icons_file
    saveOrigPanel()
    result = processPanels()    # result is "Success" or "Failure"
    # stage2
    if str(result) == "Success":
        CreateTransits()
    else:
        title = "Error in Routine"
        msg = "Not creating Transits as failure in earlier routine"
        Query().displayMessage(msg,title)
    #print "Created Transits"

def saveOrigPanel():
    global backup_file
    global backup_filename
    get_backup_filename()
    store_panel(backup_file)

def get_start_filename():
    global start_filename
    global start_file
    chooser = jmri.configurexml.LoadStoreBaseAction.getUserFileChooser()
    #returnVal = chooser.showOpenDialog(frame)
    current_file = str(chooser.getSelectedFile())
    #print current_file
    filepath = os.path.dirname(current_file)
    directory = filepath
    root = os.path.splitext(os.path.basename(current_file))
    old_filename = root[0]
    filetype  = root[1]
    #print old_filename
    # if "run" not in old_filename and "icons" not in old_filename:
    #print "run not in filepath"
    start_file = current_file
    icons_file = filepath + "/" + old_filename + "_icons" + filetype
    run_file = filepath + "/" + old_filename + "_run" + filetype
    start_filename = old_filename
    loaded_filename = old_filename


def get_backup_filename():
    global backup_file
    global backup_filename
    panel_name = start_file

    filepath = os.path.dirname(panel_name)
    root = os.path.splitext(os.path.basename(panel_name))
    filename_root = root[0]
    filetype  = root[1]
    orig_panel_path = filepath + "/" + filename_root + "_backup" + filetype
    orig_panel_name = filename_root + "_backup"
    i = 0
    while os.path.exists(orig_panel_path):
        i+=1
        orig_panel_path = filepath + "/" + filename_root + "_backup" + "_" + str(i) + filetype
        orig_panel_name = filename_root + "_backup" + "_" + str(i)
        #print "orig_panel_path", orig_panel_path

    backup_file = orig_panel_path
    backup_filename = orig_panel_name


def store_panel(filename):
    #if self.logLevel > 1: print "storing orig file in " + filename
    file = java.io.File(filename)
    cm = jmri.InstanceManager.getNullableDefault(jmri.ConfigureManager)
    result = cm.storeUser(file)
    if result :
        msg = "store was successful"
    else:
        msg = "store failed"
    #if self.logLevel > 1: print(msg)

def initialise_panel_location(stage1Button, stage3Button):
    global icons_file
    global run_file
    global start_file
    global directory
    global start_filename
    global loaded_filename
    global backup_file
    global backup_filename
    #print "clicked"
    chooser = jmri.configurexml.LoadStoreBaseAction.getUserFileChooser()

    robot = java.awt.Robot()
    #press the save tab
    KeyEvent = java.awt.event.KeyEvent
    #button.requestFocus();
    #robot.delay(1000)
    # robot.keyPress(KeyEvent.VK_TAB)
    # robot.delay(10)
    # robot.keyRelease(KeyEvent.VK_TAB)
    # robot.delay(10)
    # robot.keyPress(KeyEvent.VK_SPACE)
    # robot.delay(10)
    # robot.keyRelease(KeyEvent.VK_SPACE)
    # robot.delay(10)
    # robot.keyPress(KeyEvent.VK_ENTER)
    # robot.delay(10)
    # robot.keyRelease(KeyEvent.VK_ENTER)
    # robot.delay(10)
    #returnVal = chooser.showOpenDialog(frame)
    current_file = str(chooser.getSelectedFile())
    #print current_file
    filepath = os.path.dirname(current_file)
    directory = filepath
    root = os.path.splitext(os.path.basename(current_file))
    old_filename = root[0]
    filetype  = root[1]
    #print old_filename
    # if "run" not in old_filename and "icons" not in old_filename:
    #print "run not in filepath"
    start_file = current_file
    icons_file = filepath + "/" + old_filename + "_icons" + filetype
    run_file = filepath + "/" + old_filename + "_run" + filetype
    start_filename = old_filename
    loaded_filename = old_filename
    stage_to_run = "Stage 1"

    label_panel_location.text = start_file

    #msg = "Panel Directory: " + str(directory)
    #rowTitle_1_2.text = msg
    get_backup_filename()
    #rowStage1Title_2.text = "Modifies: " + start_filename + "  Creates backup: " + backup_filename
    #row42b2.text = "Produces: " + start_filename + "_run" + filetype + " (from " + start_filename + "_icons" + filetype + ")"
    rowTitle_2_1.text = "You have " + loaded_filename + filetype + " loaded. You may run " + stage_to_run
    rowTitle_2_1.text = "Dispatcher System: Modifies current loaded panel to produce a running system"
    rowTitle_2_1.setFont(rowTitle_2_1.getFont().deriveFont(Font.BOLD, 13));


def CreateTransits_action(event):
    CreateTransits()

def CreateTransits():
    global g
    global le
    #global DisplayProgress_global
    global logLevel
    global dpg

    print( "in createTransits")
    #the displayProgress is in CreateTransits
    # CreateTransits = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateTransits.py')
    # exec(open (CreateTransits).read())
    #DisplayProgress_global = DisplayProgress
    # progress = 5
    # DisplayProgress()
    # dpg.Update("creating transits: " + str(progress)+ "% complete")

    # initialPanelFilename = icons_file
    # finalPanelFilename = run_file
    #initialPanelFilename = start_file

    #print "Setting up Graph"
    my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
    import sys
    sys.path.append(my_path_to_jars) # add the jar to your path
    CreateGraph = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateGraph.py')
    exec(open (CreateGraph).read())
    le = LabelledEdge
    g = StationGraph()

    # progress = 10
    # dpg.Update("creating transits: " + str(progress)+ "% complete")

    #if logLevel > 0: print "updating logic"
    #     CreateSignalLogic = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateSignalLogicAndSections.py')
    #     exec(open (CreateSignalLogic).read())
    #     usl = Update_Signal_Logic()
    #print "updating logic stage1"

    #     ans = usl.create_autologic_and_sections()
    #ans = True


    #print "updating logic stage2"
    #         usl.update_logic(run_file)

    #progress = 15
    #dpg.Update("creating transits: " + str(progress)+ "% complete")

    #print "Creating Transits"
    CreateTransits = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateTransits.py')
    exec(open (CreateTransits).read())

    global dpg
    dpg = DisplayProgress()

    #print "about to run CreateTransits"
    ct = CreateTransits()

    ct.run_transits()
    #print "ran CreateTransits"

    #dpg.killLabel()

def show_options_message(msg):
    dialog = JDialog(None, 'Confirm Dispatcher Options', False)

    panel = JPanel();
    panel.add(JTextArea(msg))

    dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    dialog.getContentPane().add(panel);
    dialog.pack();
    dialog.setVisible(True);

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

    msg = "You need to set the following: \n\n Use connectivity from Layout panels\n Trains from Roster\n Layout has block detection hardware\n Automatically allocate Sections to Active Trains\n Automatically set turnouts when a Section is allocated\n\n You also need to set SignalMasts/SML (top RH)\n and the Layout scale\n\nSave your Options in the Menu in the Dispatcher Frame after checking.\n " + \
        "\n IMPORTANT: After setting the options above \nyou also need to set the Layout Scale in Preferences:Warrants \nto ensure the trains stop correctly\n"

    x = threading.Timer(2.0, function=show_options_message, args=(msg,))
    x.start()

def setAdvancedRouting():
    jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).enableAdvancedRouting(True)

# def RunDispatcher_action(event):
#     global DispatchMaster
#     print "in RunDispatcher_action"
#     RunDispatch = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/RunDispatch.py')
#     DispatchMaster = DispatchMaster
#     exec(open (RunDispatch).read())

def leftJustify( panel ):
    b = Box.createHorizontalBox()
    b.add( panel )
    b.add( Box.createHorizontalGlue() )
    # (Note that you could throw a lot more components
    # and struts and glue in here.)
    return b
################################################################################################################
# main file
################################################################################################################
global start_filename
global backup_filename
start_file = ""
run_file = ""
directory = ""

logLevel = 0


#*****************
# Set Program locations, and include code
#*****************
CreateIcons = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateIcons.py')
execfile(CreateIcons)


# delete previous Dispatch System Frames

for frame1 in java.awt.Frame.getFrames():
    # print "frame", frame1.getName()
    if frame1.getName() == "Dispatch System":
        # print "deleting frame", frame.getName()
        frame1.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))


#*****************
frame = jmri.util.JmriJFrame('Dispatch System');
frame.setName("Dispatch System")
frame.addHelpMenu('html.scripthelp.DispatcherSystem.DispatcherSystem' , True)

panel = JPanel()
panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
frame.add(panel)

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


rowTitle_22 = JPanel()
rowTitle_22.setLayout(BoxLayout(rowTitle_22, BoxLayout.X_AXIS))
rowTitle_23 = JPanel()
rowTitle_23.setLayout(BoxLayout(rowTitle_23, BoxLayout.X_AXIS))
rowStage1Title_1 = JLabel("Stage1: ")
get_start_filename()
get_backup_filename()
rowStage1Title_1 = JLabel("    Modifies: " + start_filename + "  Creates backup: " + backup_filename)
rowStage1Title_1.add(Box.createHorizontalGlue());
rowStage1Title_1.setAlignmentX(rowStage1Title_1.LEFT_ALIGNMENT)
rowStage1Title_2 = JLabel("")
rowStage1Title_3 = JLabel("    Read Help to see how system works")     #start_filename + "_icons"

rowTitle_22.add(Box.createVerticalGlue())
rowTitle_22.add(Box.createRigidArea(Dimension(20, 0)))
rowTitle_22.add(rowStage1Title_1)
rowTitle_22.add(Box.createRigidArea(Dimension(20, 0)))
rowTitle_22.add(rowStage1Title_2)

rowTitle_23.add(Box.createVerticalGlue())
rowTitle_23.add(Box.createRigidArea(Dimension(20, 0)))
rowTitle_23.add(rowStage1Title_3)

rowStage2Title = JPanel()
rowStage2Title.setLayout(BoxLayout(rowStage2Title, BoxLayout.X_AXIS))
rowStage2Title_1 = JLabel("Stage2: Check the Dispatcher Options are set correctly (essential)")
rowStage2Title_1.add(Box.createHorizontalGlue());
rowStage2Title_1.setAlignmentX(rowStage2Title_1.LEFT_ALIGNMENT)
rowStage2Title_2 = JLabel("")     #start_filename + "_icons"

rowStage2Title.add(Box.createVerticalGlue())
rowStage2Title.add(Box.createRigidArea(Dimension(20, 0)))
rowStage2Title.add(rowStage2Title_1)
rowStage2Title.add(Box.createRigidArea(Dimension(20, 0)))
rowStage2Title.add(rowStage2Title_2)

rowTitle_2 = JPanel()
rowTitle_2.setLayout(BoxLayout(rowTitle_2, BoxLayout.X_AXIS))
rowTitle_2_1 = JLabel("Stage3: Modify the Dispatcher Options so the trains move")
rowTitle_2_1.add(Box.createHorizontalGlue());
rowTitle_2_1.setAlignmentX(rowTitle_2_1.LEFT_ALIGNMENT)
rowTitle_2_2 = JLabel("")     #start_filename + "_icons"

rowTitle_2.add(Box.createVerticalGlue())
rowTitle_2.add(Box.createRigidArea(Dimension(20, 0)))
rowTitle_2.add(rowTitle_2_1)
rowTitle_2.add(Box.createRigidArea(Dimension(20, 0)))
rowTitle_2.add(rowTitle_2_2)

row_Title_3 = JPanel()
row_Title_3.setLayout(BoxLayout(row_Title_3, BoxLayout.X_AXIS))
rowTitle_3_1 = JLabel("*******************************************************************")
rowTitle_3_1.add(Box.createHorizontalGlue());
rowTitle_3_1.setAlignmentX(rowTitle_3_1.LEFT_ALIGNMENT)
rowTitle_3_2 = JLabel("")

row_Title_3.add(Box.createVerticalGlue())
row_Title_3.add(Box.createRigidArea(Dimension(20, 0)))
row_Title_3.add(rowTitle_3_1)
row_Title_3.add(Box.createRigidArea(Dimension(20, 0)))
row_Title_3.add(rowTitle_3_2)

rowStage2Separator = JPanel()
rowStage2Separator.setLayout(BoxLayout(rowStage2Separator, BoxLayout.X_AXIS))
rowStage2Separator_1 = JLabel("*******************************************************************")
rowStage2Separator_1.add(Box.createHorizontalGlue());
rowStage2Separator_1.setAlignmentX(rowStage2Separator_1.LEFT_ALIGNMENT)
rowStage2Separator_2 = JLabel("")

rowStage2Separator.add(Box.createVerticalGlue())
rowStage2Separator.add(Box.createRigidArea(Dimension(20, 0)))
rowStage2Separator.add(rowStage2Separator_1)
rowStage2Separator.add(Box.createRigidArea(Dimension(20, 0)))
rowStage2Separator.add(rowStage2Separator_2)

rowStage1Separator = JPanel()
rowStage1Separator.setLayout(BoxLayout(rowStage1Separator, BoxLayout.X_AXIS))
rowStage1Separator_1 = JLabel("*******************************************************************")
rowStage1Separator_1.add(Box.createHorizontalGlue());
rowStage1Separator_1.setAlignmentX(rowStage1Separator_1.LEFT_ALIGNMENT)
rowStage1Separator_2 = JLabel("")

rowStage1Separator.add(Box.createVerticalGlue())
rowStage1Separator.add(Box.createRigidArea(Dimension(20, 0)))
rowStage1Separator.add(rowStage1Separator_1)
rowStage1Separator.add(Box.createRigidArea(Dimension(20, 0)))
rowStage1Separator.add(rowStage1Separator_2)

rowStage1Button = JPanel()
rowStage1Button.setLayout(BoxLayout(rowStage1Button, BoxLayout.X_AXIS))
rowrowStage1Button_2 = JLabel("Sets Up everything to run trains using Dispatcher")
rowrowStage1Button_2.setFont(rowTitle_2_1.getFont().deriveFont(Font.BOLD, 13));

rowrowStage1Button_2.add(Box.createHorizontalGlue());
rowrowStage1Button_2.setAlignmentX(rowrowStage1Button_2.LEFT_ALIGNMENT)
rowStage1Button_1 = JButton("Stage1", actionPerformed = CreateIcons_action)
stage1Button = rowStage1Button_1


rowStage1Button.add(Box.createVerticalGlue())
rowStage1Button.add(Box.createRigidArea(Dimension(20, 0)))
rowStage1Button.add(rowStage1Button_1)
rowStage1Button.add(Box.createRigidArea(Dimension(20, 0)))
rowStage1Button.add(rowrowStage1Button_2)

#initialise_panel_location(stage1Button, stage2Button, stage3Button)
robot = java.awt.Robot()
KeyEvent = java.awt.event.KeyEvent

setAdvancedRouting()

rowStage2 = JPanel()
rowStage2.setLayout(BoxLayout(rowStage2, BoxLayout.X_AXIS))
rowStage2_1 = JLabel("Check Dispatcher Options are set correctly enabling Trains to Run Automatically")
rowStage2_1.setFont(rowTitle_2_1.getFont().deriveFont(Font.BOLD, 13));
rowStage2_1.add(Box.createHorizontalGlue());
rowStage2_1.setAlignmentX(rowStage2_1.LEFT_ALIGNMENT)

rowStage2_2 = JButton("Stage2", actionPerformed = ChangeOptions_action)
stage2Button = rowStage2_2

rowStage2.add(Box.createVerticalGlue())
rowStage2.add(Box.createRigidArea(Dimension(20, 0)))
rowStage2.add(rowStage2_2)
rowStage2.add(Box.createRigidArea(Dimension(20, 0)))
rowStage2.add(rowStage2_1)

initialise_panel_location(stage1Button, stage2Button)
#rowStage1Title_1 = JLabel("    Modifies: " + start_filename + "  Creates backup: " + backup_filename)

#Title
panel.add(leftJustify(rowTitle_2))
panel.add(leftJustify(rowTitle_22))
panel.add(leftJustify(rowTitle_23))
panel.add(leftJustify(row_Title_3))

#stage1
panel.add(leftJustify(rowStage1Button))
panel.add(leftJustify(rowStage1Separator))

#stage2
panel.add(leftJustify(rowStage2))
panel.add(leftJustify(rowStage2Separator))

frame.setPreferredSize(Dimension(700, 300))

frame.pack()
frame.setVisible(True)
