# Sample script showing how to build and terminate a train. Used in operations.
# Allows user to select in a window which train to build and terminate.
#
# Author: Daniel Boudreau, copyright 2019
# Part of the JMRI distribution

import jmri
import javax.swing

class buildAndTerminate(jmri.jmrit.automat.AbstractAutomaton) :
    def handle(self):
        # Build and terminate selected train
        getTrain().display("Build and Terminate Train?")
        return False   # all done, don't repeat again
  
class getTrain(javax.swing.JFrame) : 
    def display(self, msg) :
        print "Create panel"
             # create panel to allow user to select which train to build and termionate
        b = javax.swing.JButton("Build")
        bat = javax.swing.JButton("Build and Terminate")
        bt = javax.swing.JButton("Terminate")
        
        b.actionPerformed = self.whenBuildButtonClicked
        bat.actionPerformed = self.whenBuildAndTerminateButtonClicked
        bt.actionPerformed = self.whenTerminateButtonClicked
        
        self.tm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        self.combobox = self.tm.getTrainComboBox()

        # create a frame to hold the button, put button in it, and display
        self.contentPane.setLayout(javax.swing.BoxLayout(self.contentPane, javax.swing.BoxLayout.Y_AXIS))
        self.contentPane.add(javax.swing.JLabel("Select Train"))
        self.contentPane.add(self.combobox)
        panel = javax.swing.JPanel()
        panel.setLayout(javax.swing.BoxLayout(panel, javax.swing.BoxLayout.X_AXIS))
        panel.add(b)
        panel.add(bat)
        panel.add(bt)
        
        self.contentPane.add(panel)
        self.pack()
        self.setSize(400, self.getHeight())
        self.setLocation(500,500)
        self.setTitle(msg)
        self.show()
        
    def whenBuildAndTerminateButtonClicked(self, event) :
        train = self.combobox.getSelectedItem()
        if (train != None):
            trainName = train.getName()
            print "Build and terminate train", trainName   
            # Build train
            train.build()
            built = train.isBuilt()
            train.setBuildEnabled(False)    # deselect build option (Checkbox in Trains window)
            if (built == True):
              print "Train", trainName, "has been built"
              
              train.terminate() # now terminate the train
              print "Train", trainName, "has been terminated"
            else:
              print "Train", trainName, "build failed"
        else:
            print "Need to select a train"
        return False              # all done, don't repeat again
    
    def whenBuildButtonClicked(self, event) :
        train = self.combobox.getSelectedItem()
        if (train != None):
            trainName = train.getName()
            print "Build train", trainName   
            # Build train
            train.build()
            built = train.isBuilt()
            if (built == True):
              print "Train", trainName, "has been built"
            else:
              print "Train", trainName, "build failed"
        else:
            print "Need to select a train to build"
        return False              # all done, don't repeat again
    
    def whenTerminateButtonClicked(self, event) :
        train = self.combobox.getSelectedItem()
        if (train != None):
            trainName = train.getName()
            print "Terminate train", trainName   
            built = train.isBuilt()
            if (built == True):
              train.terminate() # now terminate the train
              print "Train", trainName, "has been terminated"
            else:
              print "Train", trainName, "not built!"
        else:
            print "Need to select a train to terminate"
        return False              # all done, don't repeat again
        
buildAndTerminate().start()    # create one of these, and start it running
