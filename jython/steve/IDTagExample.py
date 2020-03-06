import jmri
import java

myIdTag = jmri.implementation.DefaultIdTag("ID0413276BC1", "My Test Tag");
print myIdTag; # Outputs ID0413276BC1

if (isinstance(myIdTag,jmri.Reportable)) :
    myIdTag = myIdTag.toReportString()
print myIdTag; # Outputs My Test Tag

