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
        
        if (nodeName is None or nodeName == "") :
            result = "config-Node-"+nodeID+"+"+time+".txt"
        
        # replace illegal characters with underscores and return the result
        import re
        illegal_chars = r'[ ,`\x27<>:"/\\|?*\x00-\x1f]'  # 0x27 is is single quote
        return re.sub(illegal_chars, "_", result)
        
org.openlcb.cdi.swing.CdiPanel.fileNameGenerator = MyNameGenerator()
