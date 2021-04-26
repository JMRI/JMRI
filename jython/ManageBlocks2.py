# Listens for a change to the rAction memory variable and performs a number of actions on layout blocks created by the Layout Editor in PanelPro
#
# Author: Dave Sand
# Version: 2.4
#   2.0 Initial release of ManageBlocks2.
#   2.1 Added a memory variable (rDebug) to enable debug output on startup.  Also added two commands to rAction: Debug and NoDebug.  This enables changing the debug mode without reloading JMRI.
#   2.2 Changed the releaseAll process to use a layout block list rather then building the list looking at track segments.  This removes the requirement for a turnout to have at least one track segment in the same layout block as the turnout.
#   2.2.1 Fix error handling for initial rAction check.
#   2.3 Added the ability to use the panel name instead of the index number to select the panel to be redrawen after changing the track color.  The panel name is supplied using a new memory variable: rPName.
#   2.4 Support the PanelMenu/EditorManager changes as of JMRI 4.19.6.
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
#     rAction : reserve/release/releaseAll/setTrackColor/setOccupiedColor/Debug/NoDebug --- Trigger for the listener in this program, set to Done when processing is complete.  rAction is case sensitive.
#     rBLocks : block(s) to be changed.  The block names are separated by ",".  Block names are case sensitive.
#     rDirection ( optional ) : W or E to indicate direction when using separate colors.  Not case sensitive.
#     rColor ( optional ) : JMRI (JAVA) Color name to be assigned to the supplied blocks.  Not case sensitive.
#     rPName ( optional ) : Panel name to be redrawn after a track color change.  This is related to rIndex and takes precedence.  Name is case sensitive and has to match a Panels >> Show Panel entry.
#     rIndex ( optional ) : Relative index for the target panel.  The default is 0, which is the first panel in the panel menu list.  Also applies if there is no match for rPName.
#     rResult : Status message returned.
#     rDebug ( optional ) : If set to Yes (case sensitive) before loading, the program will print out additional information to the Script Output window if active, or the JMRI System Console.  Debugging can also be toggled after loading using the rAction commands.
#
# Warnings:
#     Color changes will persist if the panels are saved.  An initalization process may be required.
#     If both rDirection and rColor are specified along with reserve/release rAction values, rColor will be used to set the BlockExtraColor value


import jmri
import java
from java.awt import Color

debug = False

# Check for an initial debug request
rDebug = memories.getMemory('rDebug')
if rDebug is not None:
    if rDebug.getValue() == 'Yes':
        debug = True

# Set the colors as desired if using directional colors, use only the 13 JMRI colors (the colors below are just examples)

# colors = {'W': Color(255, 0, 255, 255), 'E': Color(0, 255, 0, 255), 'Reset': Color.BLACK}
# colors = {'W': Color(int('800080', 16)), 'E': Color(int('00ff00', 16)), 'Reset': Color.BLACK}
colors = {'W': Color.MAGENTA, 'E': Color.BLUE, 'Reset': Color.BLACK}

# Do not change the following statement!!!
colorNames = {'black': Color.BLACK, 'blue': Color.BLUE, 'cyan': Color.CYAN,
    'dark_gray': Color.DARK_GRAY, 'gray': Color.GRAY, 'green': Color.GREEN, 'light_gray': Color.LIGHT_GRAY, 'magenta': Color.MAGENTA,
    'orange': Color.ORANGE, 'pink': Color.PINK, 'red': Color.RED, 'white': Color.WHITE, 'yellow': Color.YELLOW}

# Custom colors
# colorNames['local control'] = Color(16515264);
colorNames['local control'] = Color(204, 255, 102, 255);

class ManageBlocks(java.beans.PropertyChangeListener):

    # Initialization
    def __init__(self):

        self.debug = debug
        if self.debug: print 'Init script, attach listener'

        # Retrieve rAction memory variable
        rAction = memories.getMemory('rAction')
        if rAction is None :
            print 'Memory variable not found: rAction'
            return

        if rAction.getValue() == 'Debug':
            self.debug = True

        # Get LE Panel list
        panelMenu = jmri.InstanceManager.getNullableDefault(jmri.jmrit.display.PanelMenu)
        if panelMenu is not None:
            if self.debug: print 'Get old style panel list'
            self.panels = panelMenu.getLayoutEditorPanelList()
        else:
            if self.debug: print 'Get new style panel list'
            editorManager = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
            self.panels = editorManager.getList(jmri.jmrit.display.layoutEditor.LayoutEditor)

        # Attach listener to rAction memory variable
        rAction.addPropertyChangeListener(self)

    def propertyChange(self, event):

        def redrawLayoutEditorPanel(self):
            # Redraw the affected layout editor panel.  Based on either rIndex or rPName
            # rPName has precedence.  If neither is supplied, the panel at index 0 will be refreshed.

            # Try using panel name
            rPName = memories.provideMemory('rPName').getValue()
            for panel in self.panels:
                if rPName == panel.getLayoutName():
                    if self.debug: print 'Redraw using panel name: {}'.format(rPName)
                    # Requested panel found
                    panel.redrawPanel()
                    return

            # Use index, default to 0 if necessary
            try:
                rIndex = int(memories.provideMemory('rIndex').getValue())
            except:
                rIndex = 0       # Default to first layout editor panel if rIndex is not defined or not numeric
            if self.debug: print 'Redraw using index: {}'.format(rIndex)
            self.panels[rIndex].redrawPanel()
            return

        # Process rAction change event
        rAction = event.newValue
        if self.debug: print 'Event occurred, newValue: {}'.format(rAction)

        # Ignore event triggered by our own setting to Done
        if rAction == 'Done':
            return

        if rAction == 'Debug':
            self.debug = True
            memories.provideMemory('rResult').setValue('Enable debug mode')
            memories.getMemory('rAction').setValue('Done')
            return

        if rAction == 'NoDebug':
            self.debug = False
            memories.provideMemory('rResult').setValue('Disable debug mode')
            memories.getMemory('rAction').setValue('Done')
            return

        msg = []

        # Process memory variables that supply the necessary input from the Logix
        rBlocks = memories.provideMemory('rBlocks').getValue()
        rDirection = memories.provideMemory('rDirection').getValue()
        if rDirection:
            rDirection = rDirection.upper()
            if not colors.get(rDirection):
                if self.debug: msg.append('Direction not found: {}'.format(rDirection))
                rDirection = None
        rColor = memories.provideMemory('rColor').getValue()
        if rColor:
            rColor = rColor.lower()
            if not colorNames.get(rColor):
                if self.debug: msg.append('Color name not found: {}'.format(rColor))
                rColor = None

        # Process the rAction command
        if rAction == 'releaseAll':
            # Get the system names of all layout blocks, reset each one
            cnt = 0
            for layoutBlock in layoutblocks.getNamedBeanSet():
                if rDirection:
                    layoutBlock.setBlockExtraColor(colors.get('Reset'))
                if rColor:
                    layoutBlock.setBlockExtraColor(colorNames.get(rColor))
                if layoutBlock.getUseExtraColor():
                    # Use Extra color is active, turn it off
                    layoutBlock.setUseExtraColor(False)
                    cnt += 1
            if self.debug: print '%d blocks released' % cnt
            msg.append('%d blocks released' % cnt)

        elif rAction in ['reserve', 'release', 'setTrackColor', 'setOccupiedColor']:
            blocklist = rBlocks.split(',')
            for blockName in blocklist:
                if self.debug: print 'block name is {}'.format(blockName)
                blockName = blockName.strip()   # removing spaces
                # find a matching block: reserve/release/setTrackColor based on rAction
                layoutBlock = layoutblocks.getLayoutBlock(blockName)
                if layoutBlock is None:
                    msg.append('Block not found: {}'.format(blockName))
                    continue

                if rAction == 'reserve':
                    if rDirection:
                        layoutBlock.setBlockExtraColor(colors.get(rDirection))
                    if rColor:
                        layoutBlock.setBlockExtraColor(colorNames.get(rColor))
                    layoutBlock.setUseExtraColor(True)
                    msg.append('reserved {}'.format(blockName))
                elif rAction == 'release':
                    if rDirection:
                        layoutBlock.setBlockExtraColor(colors.get('Reset'))
                    if rColor:
                        layoutBlock.setBlockExtraColor(colorNames.get(rColor))
                    layoutBlock.setUseExtraColor(False)
                    msg.append('released {}'.format(blockName))
                elif rAction == 'setTrackColor':
                    if self.debug: print 'New track color: {}'.format(rColor)
                    if rColor:
                        if self.debug: print 'Set new color'
                        layoutBlock.setBlockTrackColor(colorNames.get(rColor))
                        # Clear color request, prevent interaction with reserve/request
                        memories.provideMemory('rColor').setValue('')
                        # Refresh panel to display new color
                        redrawLayoutEditorPanel(self)
                        msg.append(blockName + '{} set to {}'.format(blockName, rColor))
                elif rAction == 'setOccupiedColor':
                    if self.debug: print 'New occupied color is {}'.format(rColor)
                    if rColor:
                        if self.debug: print 'Set new color'
                        layoutBlock.setBlockOccupiedColor(colorNames.get(rColor))
                        # Clear color request, prevent interaction with reserve/request
                        memories.provideMemory('rColor').setValue('')
                        # Refresh panel to display new color
                        redrawLayoutEditorPanel(self)
                        msg.append('{} set to {}'.format(blockName, rColor))

        # Set the rResult memory variable with the results and any other messages
        memories.provideMemory('rResult').setValue(' : '.join(msg))

        # Set rAction to Done so that subsequent Logix change triggers will work
        memories.getMemory('rAction').setValue('Done')


print 'Load ManageBlocks v2.4'

# Check for existing occurance of this program
try:
    mblks
except NameError:
    mblks = ManageBlocks()
    print 'ManageBlocks2 starting'
else:
    print 'ManageBlocks2 already running'
