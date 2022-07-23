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

    # msg = "About to create file " + finalPanelFilename + "\n from " + initialPanelFilename
    # msg = msg + "\n  *****************************************************"
    # msg = msg + "\nPanel " + initialPanelFilename + " should be open for this stage to work"
    # msg = msg + "\n  *****************************************************"
    # msg = msg + "\nContinue?"
    # myAnswer = JOptionPane.showConfirmDialog(None, msg)
    # if myAnswer == JOptionPane.YES_OPTION:
    #     pass
    # elif myAnswer == JOptionPane.NO_OPTION:
    #     msg = 'Stopping'
    #     JOptionPane.showMessageDialog(None, msg, 'Stopping', JOptionPane.WARNING_MESSAGE)
    #     return
    # elif myAnswer == JOptionPane.CANCEL_OPTION:
    #     msg = 'Stopping'
    #     JOptionPane.showMessageDialog(None, msg, 'Stopping', JOptionPane.WARNING_MESSAGE)
    #     return
    # elif myAnswer == JOptionPane.CLOSED_OPTION:
    #     #print "You closed the window. How rude!"
    #     return
    # print "Processing panels"
    #stage0
    saveOrigPanel()
    #stage1
    p = processPanels()
    print "Processed panels"
    #stage2
    CreateTransits()
    print "Created Transits"

def saveOrigPanel():
    global backup_file
    global backup_filename
    [backup_filename, backup_file] = get_backup_filename()
    store_panel(backup_file)

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

    return [backup_filename, backup_file]

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
    # else:
    #     stripped_filename = strip_end(old_filename,"_icons")
    #     stripped_filename = strip_end(stripped_filename,"_run")
    #     start_filename = stripped_filename
    #     start_file = filepath + "/" + stripped_filename + filetype
    #     icons_file = filepath + "/" + stripped_filename + "_icons" + filetype
    #     run_file = filepath + "/" + stripped_filename + "_run" + filetype
    #     if "icons" in old_filename:
    #         loaded_filename = stripped_filename + "_icons"
    #         stage_to_run = "Stage 2"
    #     else:
    #         loaded_filename = stripped_filename + "_run"
    #         stage_to_run = "Stage 3 then operate the trains"
    label_panel_location.text = start_file
    #row13b2.text = icons_file
    #row11b2.text = run_file
    msg = "Panel Directory: " + str(directory)
    rowTitle_1_2.text = msg
    #row42b2.text = start_filename + filetype
    [backup_filename, backup_file] = get_backup_filename()
    #rowStage1Title_2.text = "Modifies: " + start_filename + "  Creates backup: " + backup_filename
    #row42b2.text = "Produces: " + start_filename + "_run" + filetype + " (from " + start_filename + "_icons" + filetype + ")"
    rowTitle_2_1.text = "You have " + loaded_filename + filetype + " loaded. You may run " + stage_to_run
    rowTitle_2_1.text = "Dispatcher System: Modifies current loaded panel to produce a running system"
    rowTitle_2_1.setFont(rowTitle_2_1.getFont().deriveFont(Font.BOLD, 13));

    # row15b1.text = "When finished you need to restart JMRI and load the file created in Stage1: " + start_filename + "_icons" + filetype + " instead of " + start_filename + filetype
    # row45b1.text = "When finished you need to restart JMRI and load the file created in Stage2: " + start_filename + "_run" + filetype + " instead of " + start_filename + "_icons" + filetype
    # if stage_to_run == "Stage 1":
    #     #inhibit Stage 1 button
    #     stage1Button.setEnabled(True)
    #     stage2Button.setEnabled(True)
    #     stage3Button.setEnabled(True)
    #
    # elif stage_to_run == "Stage 2":
    #     #inhibit Stage 2 button
    #     stage1Button.setEnabled(True)
    #     stage2Button.setEnabled(True)
    #     stage3Button.setEnabled(True)
    #
    # elif stage_to_run == "Stage 3 then operate the trains":
    #     #inhibit Stage 3 button
    #     stage1Button.setEnabled(True)
    #     stage2Button.setEnabled(True)
    #     stage3Button.setEnabled(True)


def CreateTransits_action(event):
    CreateTransits()

def CreateTransits():
    #print "in create_transits"
    global g
    global le
    global DisplayProgress_global
    global logLevel


    #the displayProgress is in CreateTransits
    CreateTransits = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateTransits.py')
    exec(open (CreateTransits).read())
    DisplayProgress_global = DisplayProgress
    progress = 5
    dpg=DisplayProgress_global()
    dpg.Update("creating transits: " + str(progress)+ "% complete")

    # initialPanelFilename = icons_file
    # finalPanelFilename = run_file
    initialPanelFilename = start_file

    #print "Setting up Graph"
    my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
    import sys
    sys.path.append(my_path_to_jars) # add the jar to your path
    CreateGraph = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateGraph.py')
    exec(open (CreateGraph).read())
    le = LabelledEdge
    g = StationGraph()

    progress = 10
    dpg.Update("creating transits: " + str(progress)+ "% complete")

    if logLevel > 0: print "updating logic"
    #     CreateSignalLogic = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateSignalLogicAndSections.py')
    #     exec(open (CreateSignalLogic).read())
    #     usl = Update_Signal_Logic()
    #print "updating logic stage1"

    #     ans = usl.create_autologic_and_sections()
    ans = True

    if ans == True:
        #print "updating logic stage2"
        #         usl.update_logic(run_file)

        progress = 15
        dpg.Update("creating transits: " + str(progress)+ "% complete")

        #print "Creating Transits"
        CreateTransits = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateTransits.py')
        exec(open (CreateTransits).read())

        #print "about to run CreateTransits"
        ct = CreateTransits()

        ct.run_transits(start_file, backup_file)
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

def setAdvancedRouting():
    jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).enableAdvancedRouting(True)

def RunDispatcher_action(event):
    global DispatchMaster
    RunDispatch = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/RunDispatch.py')
    DispatchMaster = DispatchMaster
    exec(open (RunDispatch).read())

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

start_file = ""
run_file = ""
directory = ""

logLevel = 0


#*****************
# Set Program locations, and include code
#*****************
CreateIcons = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateIcons.py')
execfile(CreateIcons)

#*****************
frame = jmri.util.JmriJFrame('Dispatch System');
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

# rowTitle_1_old = JPanel()
# rowTitle_1_old.setLayout(BoxLayout(rowTitle_1_old, BoxLayout.X_AXIS))
# rowTitle_1_1 = JLabel("")
# rowTitle_1_1.add(Box.createHorizontalGlue());
# rowTitle_1_1.setAlignmentX(rowTitle_1_1.LEFT_ALIGNMENT)
# msg = ""
# rowTitle_1_2 = JLabel(msg)
# rowTitle_1_2.setAlignmentX(rowTitle_1_1.RIGHT_ALIGNMENT)
#
# rowTitle_1_old.add(Box.createVerticalGlue())
# rowTitle_1_old.add(Box.createRigidArea(Dimension(20, 0)))
# rowTitle_1_old.add(rowTitle_1_1)
# rowTitle_1_old.add(Box.createRigidArea(Dimension(20, 0)))
# rowTitle_1_old.add(rowTitle_1_2)

# row22 = JPanel()
# row22.setLayout(BoxLayout(row22, BoxLayout.X_AXIS))
# row22b1 = JLabel("We Start with the Original Panel")
# row22b1.add( Box.createHorizontalGlue() );
# row22b1.setAlignmentX( row22b1.LEFT_ALIGNMENT )
# row22b2 = JLabel("")    #start_filename
# row22b2.setAlignmentX( row22b2.RIGHT_ALIGNMENT )
#
# row22.add(Box.createVerticalGlue())
# row22.add(Box.createRigidArea(Dimension(20, 0)))
# row22.add(row22b1)
# row22.add(Box.createRigidArea(Dimension(20, 0)))
# row22.add(row22b2)


rowTitle_22 = JPanel()
rowTitle_22.setLayout(BoxLayout(rowTitle_22, BoxLayout.X_AXIS))
rowStage1Title_1 = JLabel("Stage1x: ")
rowStage1Title_1 = JLabel("    Modifies: " + start_filename + "  Creates backup: " + backup_filename)
rowStage1Title_1.add(Box.createHorizontalGlue());
rowStage1Title_1.setAlignmentX(rowStage1Title_1.LEFT_ALIGNMENT)
rowStage1Title_2 = JLabel("")     #start_filename + "_icons"

rowTitle_22.add(Box.createVerticalGlue())
rowTitle_22.add(Box.createRigidArea(Dimension(20, 0)))
rowTitle_22.add(rowStage1Title_1)
rowTitle_22.add(Box.createRigidArea(Dimension(20, 0)))
rowTitle_22.add(rowStage1Title_2)

# row42 = JPanel()
# row42.setLayout(BoxLayout(row42, BoxLayout.X_AXIS))
# row42b1 = JLabel("Stage2: Add the transits and train-info files")
# row42b1.add( Box.createHorizontalGlue() );
# row42b1.setAlignmentX( row42b1.LEFT_ALIGNMENT )
# row42b2 = JLabel("")     #start_filename + "_icons"
#
# row42.add(Box.createVerticalGlue())
# row42.add(Box.createRigidArea(Dimension(20, 0)))
# row42.add(row42b1)
# row42.add(Box.createRigidArea(Dimension(20, 0)))
# row42.add(row42b2)

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

# row33 = JPanel()
# row33.setLayout(BoxLayout(row33, BoxLayout.X_AXIS))
# row33b1 = JLabel("*******************************************************************")
# row33b1.add( Box.createHorizontalGlue() );
# row33b1.setAlignmentX( row33b1.LEFT_ALIGNMENT )
# row33b2 = JLabel("")
#
# row33.add(Box.createVerticalGlue())
# row33.add(Box.createRigidArea(Dimension(20, 0)))
# row33.add(row33b1)
# row33.add(Box.createRigidArea(Dimension(20, 0)))
# row33.add(row33b2)

###row1L1






rowStage1Button = JPanel()
rowStage1Button.setLayout(BoxLayout(rowStage1Button, BoxLayout.X_AXIS))
rowrowStage1Button_2 = JLabel("Sets Up everything to run trains using dispatcher")
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

# row11 = JPanel()
# row11.setLayout(BoxLayout(row11, BoxLayout.X_AXIS))
# row11b1 = JLabel("Stage2 just adds transits and traininfo files to the currently loaded system: ")
# row11b1.add( Box.createHorizontalGlue() );
# row11b1.setAlignmentX( row11b1.LEFT_ALIGNMENT )
# row11b2 = JLabel("")
#
# row11.add(Box.createVerticalGlue())
# row11.add(Box.createRigidArea(Dimension(20, 0)))
# row11.add(row11b1)
# row11.add(Box.createRigidArea(Dimension(20, 0)))
# row11.add(row11b2)

# row15 = JPanel()
# row15.setLayout(BoxLayout(row15, BoxLayout.X_AXIS))
# row15b1 = JLabel("You need to restart JMRI and load the file created in Stage 1: ")
# row15b1.add( Box.createHorizontalGlue() );
# row15b1.setAlignmentX( row15b1.LEFT_ALIGNMENT )
# row15b2 = JLabel("")
#
# row15.add(Box.createVerticalGlue())
# row15.add(Box.createRigidArea(Dimension(20, 0)))
# ##row15.add(row15b1)
# row15.add(Box.createRigidArea(Dimension(20, 0)))
# ##row15.add(row15b2)

# row45 = JPanel()
# row45.setLayout(BoxLayout(row45, BoxLayout.X_AXIS))
# row45b1 = JLabel("You need to restart JMRI and load the file created in Stage 1: ")
# row45b1.add( Box.createHorizontalGlue() );
# row45b1.setAlignmentX( row45b1.LEFT_ALIGNMENT )
# row45b2 = JLabel("")
#
# row45.add(Box.createVerticalGlue())
# row45.add(Box.createRigidArea(Dimension(20, 0)))
# row45.add(row45b1)
# row45.add(Box.createRigidArea(Dimension(20, 0)))
# row45.add(row45b2)

#initialise_panel_location(stage1Button, stage2Button, stage3Button)
robot = java.awt.Robot()
KeyEvent = java.awt.event.KeyEvent

setAdvancedRouting()

# row2 = JPanel()
# row2.setLayout(BoxLayout(row2, BoxLayout.X_AXIS))
# row2b1 = JLabel("Create Transits and TrainInfo Fies:           Runs file CreateTransits.py")
# row2b1.add( Box.createHorizontalGlue() );
# row2b1.setAlignmentX( row2b1.LEFT_ALIGNMENT )
#
# row2b2 = JButton("Stage2", actionPerformed = CreateTransits_action)
# stage2Button_old = row2b2
#
# row2.add(Box.createVerticalGlue())
# row2.add(Box.createRigidArea(Dimension(20, 0)))
# row2.add(row2b2)
# row2.add(Box.createRigidArea(Dimension(20, 0)))
# row2.add(row2b1)

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

# row14 = JPanel()
# row14.setLayout(BoxLayout(row14, BoxLayout.X_AXIS))
# row14b1 = JLabel("You should be now set up to run the dispatcher system")
# row14b1.add( Box.createHorizontalGlue() );
# row14b1.setAlignmentX( row14b1.LEFT_ALIGNMENT )
# row14b2 = JLabel("")
#
# row14.add(Box.createVerticalGlue())
# row14.add(Box.createRigidArea(Dimension(20, 0)))
# row14.add(row14b1)
# row14.add(Box.createRigidArea(Dimension(20, 0)))
# row14.add(row14b2)

initialise_panel_location(stage1Button, stage2Button)

###top panel


# row3 = JPanel()
# row3.setLayout(BoxLayout(row3, BoxLayout.X_AXIS))
# b1 = JLabel("You my now run the dispatcher system from the panel")
# b1.setAlignmentX( b1.LEFT_ALIGNMENT )
# #b2 = JButton("Run", actionPerformed = RunDispatcher_action)
# row3.add(Box.createVerticalGlue())
# #row3.add(Box.createRigidArea(Dimension(20, 0)))
# #row3.add(b2)
# row3.add(Box.createRigidArea(Dimension(20, 0)))
# row3.add(b1)




# panel.add(leftJustify(rowTitle_1_old))
panel.add(leftJustify(rowTitle_2))
panel.add(leftJustify(rowTitle_22))
panel.add(leftJustify(row_Title_3))

#stage1

panel.add(leftJustify(rowStage1Button))
#panel.add(leftJustify(row15))
panel.add(leftJustify(rowStage1Separator))

#panel.add(leftJustify(row11))

#stage2
#panel.add(leftJustify(row42))
#panel.add(leftJustify(row2))
#panel.add(leftJustify(row45))
#panel.add(leftJustify(row33))

#stage3
#panel.add(leftJustify(rowStage2Title))
panel.add(leftJustify(rowStage2))
panel.add(leftJustify(rowStage2Separator))

#panel.add(leftJustify(row14))
#panel.add(leftJustify(row3))

frame.pack()
frame.setVisible(True)
