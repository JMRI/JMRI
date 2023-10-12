import java;
import jmri;
import org.openlcb;

# Demonstrate how to override the default back-up file name generation.
#
# This example changes the default name when doing a "Backup..." on the CDI WindowsCommandSpec
# to config-<node name>-<date and time>.txt, a simpler form

class MyNameGenerator(org.openlcb.cdi.swing.CdiPanel.FileNameGenerator) :
    def generateFileName(self, rep, nodeName) :
        nodeID = rep.getRemoteNodeAsString()
        
        timeFormat = java.time.format.DateTimeFormatter.ofPattern("uu-MM-dd-HH-mm-ss")
        time = java.time.LocalDateTime.now().format(timeFormat)
        
        result = "config-"+nodeName+"-"+time+".txt"
        
        return result.replace(" ", "-")
        
org.openlcb.cdi.swing.CdiPanel.fileNameGenerator = MyNameGenerator()
