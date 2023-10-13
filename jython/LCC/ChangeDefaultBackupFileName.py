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
        
        softwareVersion = ""
        if (rep.getCdiRep() != None and rep.getCdiRep().getIdentification() != None) :
            softwareVersion = rep.getCdiRep().getIdentification().getSoftwareVersion()
        
        timeFormat = java.time.format.DateTimeFormatter.ofPattern("uu-MM-dd-HH-mm-ss")
        time = java.time.LocalDateTime.now().format(timeFormat)
        
        # assemble desired filename from above parts
        result = "config-"+nodeName+"-"+time+".txt"
        
        # replace spaces with underscores and return the result
        return result.replace(" ", "_")
        
org.openlcb.cdi.swing.CdiPanel.fileNameGenerator = MyNameGenerator()
