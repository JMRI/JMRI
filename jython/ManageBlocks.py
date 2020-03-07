# Listens for a change to the rAction memory variable and performs a number of actions on layout blocks created by the Layout Editor in PanelPro 
#
# Author: Dave Sand
#
# Requires JRMI version 3.3.5 or higher
#
# This script is designed to run as a persistent listener.  It should only be called once from an initializatin Logix.
#   Subsequent calls occur when the rAction memory variable is changed.
#
# Actions:
#     Sets UseExtraColor to true or false to manage changing block colors to indicate reserved status
#     Change normal track color
#     Change occupied track color
#
# The program uses the following memory variables:
#     rAction : reserve/release/releaseAll/setTrackColor/setOccupiedColor --- Trigger for the listener in this program, set to Done when processing is complete.  rAction is case sensitive.
#     rBLocks : block(s) to be changed.  The block names are separated by " ".  Block names are case sensitive.
#     rDirection ( optional ) : W or E to indicate direction when using separate colors.  Not case sensitive.
#     rColor ( optional ) : JMRI (JAVA) Color name to be assigned to the supplied blocks.  Not case sensitive.
#     rIndex ( optional ) : Relative index for the target panel.  The default is 0, which is the first panel in the panel menu list.
#     rResult : Status message returned.
#
# Warnings:
#     Color changes will persist if the panels are saved.  An initalization process may be required.
#     If both rDirection and rColor are specified along with reserve/release rAction values, rColor will be used to set the BlockExtraColor value

from java.awt import Color
import jmri
import java
import java.awt
import java.beans

debug = False

# Set the colors as desired if using directional colors, use only the 13 JMRI colors (the colors below are just examples)
colors = {'W': Color.WHITE, 'E': Color.BLACK, 'Reset': Color.RED}

# Do not change the following statement!!!
colorNames = {'black': Color.BLACK, 'blue': Color.BLUE, 'cyan': Color.CYAN,
    'dark_gray': Color.DARK_GRAY, 'gray': Color.GRAY, 'green': Color.GREEN, 'light_gray': Color.LIGHT_GRAY, 'magenta': Color.MAGENTA,
    'orange': Color.ORANGE, 'pink': Color.PINK, 'red': Color.RED, 'white': Color.WHITE, 'yellow': Color.YELLOW}

class ManageBlocks(java.beans.PropertyChangeListener):

    # Initialization
    def __init__(self):

        if debug: print "init script, attach listener"        
        # Retrieve rAction memory variable
        rAction = memories.provideMemory('rAction')
        if rAction == "None" :
            print "Memory variable rAction not found!"
            return
            
        # Attach listener to rAction memory variable
        rAction.addPropertyChangeListener(self)
        
    def propertyChange(self, event):
        rAction = event.newValue
        if debug: print "event occurred, newValue = " + rAction        

        # Ignore event triggered by our own setting to Done
        if rAction == 'Done':
            return

        msg = []
        # Process memory variables that supply the necessary input from the Logix
        rBlocks = memories.provideMemory("rBlocks").getValue()
        try:
            rIndex = int(memories.provideMemory("rIndex").getValue())
        except:
            rIndex = 0       # Default to first panel if rIndex is not defined
        # print "index is %d" % rIndex
        rDirection = memories.provideMemory("rDirection").getValue()
        if rDirection:
            rDirection = rDirection.upper()
            if not colors.get(rDirection):
                if debug: msg.append('Direction "' + rDirection + '" not found')
                rDirection = None
        rColor = memories.provideMemory("rColor").getValue()
        if rColor:
            rColor = rColor.lower()
            if not colorNames.get(rColor):
                if debug: msg.append('Color name "' + rColor + '" not found')
                rColor = None

        # Process the rAction command                
        if rAction == 'releaseAll':
            # Init access to layout editor panels
            PanelMenu = jmri.InstanceManager.getDefault(jmri.jmrit.display.PanelMenu)
            layouts = PanelMenu.getLayoutEditorPanelList()
            
            cnt = 0
            for seg in layouts[rIndex].trackList.toArray():
                layoutBlock = seg.getLayoutBlock()
                if layoutBlock:      # Not every segment has a layout block assigned
                    # Layout block found
                    if rDirection:
                        layoutBlock.setBlockExtraColor(colors.get('Reset'))
                    if rColor:
                        layoutBlock.setBlockExtraColor(colorNames.get(rColor))
                    if layoutBlock.getUseExtraColor():
                        # Use Extra color is active, turn it off
                        layoutBlock.setUseExtraColor(0)
                        cnt += 1                        
            if debug: print '%d blocks released' % cnt
            msg.append('%d blocks released' % cnt)
            
        elif rAction == 'reserve' or rAction == 'release' or rAction == 'setTrackColor' or rAction == 'setOccupiedColor':
            blocklist = rBlocks.split()
            for blockName in blocklist:
                if debug: print 'block name is ' + blockName
                # find a matching block: reserve/release/setTrackColor based on rAction
                layoutBlock = layoutblocks.getLayoutBlock(blockName)
                if not layoutBlock:
                    msg.append("Block '" + blockName + "' not found")
                    continue
    
                if rAction == 'reserve':
                    if rDirection:
                        layoutBlock.setBlockExtraColor(colors.get(rDirection))
                    if rColor:
                        layoutBlock.setBlockExtraColor(colorNames.get(rColor))
                    layoutBlock.setUseExtraColor(1)
                    msg.append('reserved ' + blockName)
                elif rAction == 'release':
                    if rDirection:
                        layoutBlock.setBlockExtraColor(colors.get('Reset'))
                    if rColor:
                        layoutBlock.setBlockExtraColor(colorNames.get(rColor))
                    layoutBlock.setUseExtraColor(0)
                    msg.append('released ' + blockName)
                elif rAction == 'setTrackColor':
                    if debug: print 'new color is ' + rColor
                    if rColor:
                        if debug: print 'set new color'
                        layoutBlock.setBlockTrackColor(colorNames.get(rColor))
                        # Clear color request, prevent interaction with reserve/request 
                        memories.getMemory("rColor").setValue('')

                        # Refresh panel to display new color
                        PanelMenu = jmri.InstanceManager.getDefault(jmri.jmrit.display.PanelMenu)
                        layouts = PanelMenu.getLayoutEditorPanelList()
                        layouts[rIndex].redrawPanel()

                        msg.append(blockName + ' set to ' + rColor)
                elif rAction == 'setOccupiedColor':
                    if debug: print 'new color is ' + rColor
                    if rColor:
                        if debug: print 'set new color'
                        layoutBlock.setBlockOccupiedColor(colorNames.get(rColor))
                        # Clear color request, prevent interaction with reserve/request 
                        memories.getMemory("rColor").setValue('')

                        # Refresh panel to display new color
                        PanelMenu = jmri.InstanceManager.getDefault(jmri.jmrit.display.PanelMenu)
                        layouts = PanelMenu.getLayoutEditorPanelList()
                        layouts[rIndex].redrawPanel()

                        msg.append(blockName + ' set to ' + rColor)

                    
            
        # Set the rResult memory variable with the results and any other messages          
        memories.provideMemory("rResult").setValue(' : '.join(msg))

        # Set rAction to Done so that subsequent Logix change triggers will work
        memories.getMemory("rAction").setValue('Done')

if debug: print 'start init process'
mblks = ManageBlocks()
