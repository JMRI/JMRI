# SetBlockValues.py -- Find occupied blocks without values and prompt for the value, if any.
# Author:  Dave Sand, based on a script by Jason Janzen

# Setup:
#  - Set the SensorName variable to the name of the sensor which will be used to trigger processing.
#  - Adapt the LoadTrainRows function.  This can be either a hard coded list of trains or use program
#    logic to select the information from other locations within JMRI.
#  - Use an initialization process to load the script one time.  Once loaded, the script waits on the sensor to become active.

# Process:
#  - The startup process validates the sensor name and builds the combobox dropdown list.
#  - The handle procedure waits for the sensor to go Active.
#  - When the sensor becomes Active, the program looks at the state of all blocks.  If a block is active
#      but has no block value, a dialog box is presented to select the train to be used for the block value.
#      The selection uses a combo box that was configured during setup.
#  - The title for the dialog box contains the block name.  Due to limited space the program defaults
#      to the 'display name' which is either the user name or the system name if a user name is not present.

# Note: If the sensor name (SetBlocks) or the train list file name (Trains.txt) is changed, copy this script to the user files location.
#
# Version 1.0 -- Initial release
# Version 2.0 -- Updated for getSystemNameList() depreciation -- PNU -- 01/05/22
# Version 2.1 -- Fix javax.swing imports -- DAS -- 07/07/24

import jmri
import java
from javax.swing import JComboBox, JOptionPane

# Sensor name used to trigger the block values process
SensorName = 'SetBlocks'

# External file that contains the train names for the external file demo.
trainFile = jmri.util.FileUtil.getUserFilesPath() + "Trains.txt"

class SetBlockValues(jmri.jmrit.automat.AbstractAutomaton):
    def init(self):

        # Retrieve SetBlocks sensor
        self.objSensor = sensors.getSensor(SensorName)
        if self.objSensor is None:
            msg = "SetBlocks sensor, '{}', is not defined.".format(SensorName)
            JOptionPane.showConfirmDialog(None, msg, 'SetBlockValues Error', JOptionPane.PLAIN_MESSAGE)
            return

        # Create a drop down combo box for train selection
        self.cboTrains = JComboBox()
        self.cboTrains.setEditable(True)
        self.cboTrains.addItem('')          # First row is empty for manual entry
        self.LoadTrainRows()

        return

    def LoadTrainRows(self):
        # Populate the combo dropdown box.
        #  - Create the train list here.  Two methods are show below.  It may also be possible to get the information from other parts of JMRI.

        # - - - Build manually line by line - - - #
#        self.cboTrains.addItem('1234')
#        self.cboTrains.addItem('4321')

        # - - - Build from an external file - - - #
        try:
            with open(trainFile) as file:
                for line in file:
                    self.cboTrains.addItem(line.strip())
        except IOError:
            msg = 'Unable to open train file: {}'.format(trainFile)
            ret = JOptionPane.showConfirmDialog(None, msg, 'SetBlockValues File Error', JOptionPane.PLAIN_MESSAGE)
        return

    def handle(self):
        self.waitSensorActive(self.objSensor)

        for objBlock in blocks.getNamedBeanSet():
            if objBlock.getSensor() == None:
                continue
            if objBlock.getState() != objBlock.OCCUPIED:
                continue
            if objBlock.getValue() == None or objBlock.getValue().strip() == '':
                titleLine = 'Enter value for {}'.format(objBlock.getDisplayName())
                ret = JOptionPane.showConfirmDialog(None, self.cboTrains, titleLine, JOptionPane.PLAIN_MESSAGE)
                if ret >= 0:
                    objBlock.setValue(self.cboTrains.getSelectedItem())

        self.objSensor.setKnownState(INACTIVE)
        return True

# End class

print 'Load SetBlockValues v2.1'

# Check for existing occurance of this program
try:
    setBV
except NameError:
    setBV = SetBlockValues()
    setBV.setName("SetBlockValues")
    setBV.start()
else:
    msg = "The 'SetBlockValues' script is already running."
    JOptionPane.showConfirmDialog(None, msg, 'SetBlockValues Error', JOptionPane.PLAIN_MESSAGE)
