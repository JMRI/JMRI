# Sample script showing how to operate a grade crossing with block detection.
#
# This script requires an already configured audio buffer loaded with a .wav 
# file of the users choice. It also requires an audio source configured to use
# the audio buffer.
#
# This script will detect a train entering any approach block that is listed in 
# approachBloks and will play the sound (.wave file). The sound will play until
# the train exits all island blocks listed in islandBlocks or if the train backs
# out of the approach block or if the island block is not occupied with in the
# the delay setting (the sound will start again if the island is later occupied).
#
# The ability to control crossing gates and signals can easily be added the the 
# GradeCrossingListener.StartGradeCrossing and 
# GradeCrossingListener.StopGradeCrossing methods.
#
# Author: Keith Ruberson, copyright 2016
# Part of the JMRI distribution
#
import java
import java.awt
import java.awt.event
import java.beans
import java.util
import java.util.ArrayList as ArrayList
import jmri
import javax.swing.Timer as Timer
import java.awt.event.ActionListener as ActionListener

bell = audio.provideAudio("IAS1")

# Timer delay in seconds, adjust as necessary.
delay = 20 

# A list that holds each approach block by userName, should contain at least two
# blocks for each track crossing the grade, but may contain more. Adjust the 
# string names as necessary. The blocks named will be created if they do not
# already exist.
approachBlocks = ArrayList(java.util.Arrays.asList("LB1", "LB3", "LB7"))

# A list that holds each island block by userName, should contain one block
# for each track crossing the grade, but may contain more for more than one 
# track. Adjust the string names as necessary. The blocks named will be created
# if they do not already exist.
islandBlocks = ArrayList(java.util.Arrays.asList("LB2"))

class GradeCrossingListener(java.beans.PropertyChangeListener):
    
    # Event handler.
    def propertyChange(self, event):
        
        # Is this event for an approach block?       
        if (event.propertyName == "state" and approachBlocks.contains(event.source.userName)) :    
            print "change",event.propertyName
            print "source userName", event.source.userName
            
            # Has a train entered the approach block?
            if (event.newValue == jmri.Block.OCCUPIED):
                print "OCCUPIED"
                
                # Are all island block unoocupied? No, break. Yes, start the 
                # grade crossing.
                for i in range(islandBlocks.size()) :
                    if (blocks.getBlock(islandBlocks.get(i)).getState() == jmri.Block.OCCUPIED):
                        break
                    else : 
                        self.StartGradeCrossing()
                        
            # Has the train exited the approach block?        
            if (event.newValue == jmri.Block.UNOCCUPIED) :
                
                # Is any island block occupied? Yes, break. No, stop the grade
                # crossing.
                for i in range(islandBlocks.size()) :
                    if (blocks.getBlock(islandBlocks.get(i)).getState() == jmri.Block.OCCUPIED):
                        break
                    else :
                        print "UNOCCUPIED"
                        self.StopGradeCrossing()
               
        # Is this event for an island block?
        if (event.propertyName == "state" and islandBlocks.contains(event.source.userName)) :
            print "change",event.propertyName            
            print "source userName", event.source.userName
            
            # Has the train entered the island block?
            if (event.newValue == jmri.Block.OCCUPIED):
                print "OCCUPIED"
                self.StartGradeCrossing()                 
                timer.stop()
                print "Stopping Timer"
            
            # Has the train exited the island? Yes, stop the grade crossing. No,
            # break.
            for i in range(islandBlocks.size()) :
                    if (blocks.getBlock(islandBlocks.get(i)).getState() == jmri.Block.OCCUPIED):
                        break
                    else :
                        print "UNOCCUPIED"
                        self.StopGradeCrossing()
                 
            
    # Start the grade crossing method.                
    def StartGradeCrossing(self) :
        if (bell.getState() != jmri.Audio.STATE_PLAYING) :
            print "Grade Crossing Started"                    
            bell.fadeIn()
            
        if (not timer.isRunning()) :
            timer.start()
     
    # Stop the grade crossing method.
    def StopGradeCrossing(self):
        if (bell.getState() != jmri.Audio.STATE_STOPPED) :
            print "Grade Crossing Stopped"
            bell.fadeOut()
            
        if (timer.isRunning) :
            timer.stop()              

# Timer elapsed listener
class timerElapsed(ActionListener):
    
    # Handles the timer elapsed event
    def actionPerformed(self, e):
        for i in range(islandBlocks.size()) :
            if (blocks.getBlock(islandBlocks.get(i)).getState() == jmri.Block.OCCUPIED):
                break
            else :
                print "Timer Fired"
                listener.StopGradeCrossing()

# Create the grade crossing listener
listener = GradeCrossingListener()

# Create a timer that calls its elapsed listener after a delay. Stops the grade
# crossing after the delay if the island is not occupied by a train.
timer = Timer(1000 * delay, timerElapsed())

# Add a PropertyChangeListner for each approach block and create the block if it
# does not already exist.
for i in range(approachBlocks.size()) :
    blocks.provideBlock(approachBlocks.get(i)).addPropertyChangeListener(listener)
    
# Add a PropertyChangeListner for each island block and create the block if it
# does not already exist.
for i in range(islandBlocks.size()) :
    blocks.provideBlock(islandBlocks.get(i)).addPropertyChangeListener(listener)
   
