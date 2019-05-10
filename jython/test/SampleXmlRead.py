# This is sample code to read an XML file from the JMRI
# distribution. It reads the decoderIndex.xml file, because
# we know that one is always there.
# Bob Jacobsen   (C) 2019

import org.jdom2
from jmri.jmrit import XmlFile
class myXmlFile(XmlFile) :   # XmlFile is abstract
    def nullMethod() :
        return

# get the base of the document
rootElement = myXmlFile().rootFromName("xml/decoderIndex.xml")

# get an element within that
next = rootElement.getChild("decoderIndex")
# and check an attribute from that
version = next.getAttributeValue("version")
if (int(version) < 900 ) :
    AssertionError('Did not expect a small version number: '+version)

# iterate over the next level, the <mfgList> of <manufacturer> elements, checking for a particular value
for child in next.getChild("mfgList").getChildren() : 
    if (child.getAttributeValue("mfg") == "JMRI") :
        if (child.getAttributeValue("mfgID") != "18") :
            AssertionError('Expected JMRI to be 18')



