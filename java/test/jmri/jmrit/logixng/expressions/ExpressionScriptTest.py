import java
import java.beans
import jmri
import jmri.jmrit.logixng

s = sensors.provideSensor("IS1")

result.setValue( s.getState() == ACTIVE )
