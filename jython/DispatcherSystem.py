import java
import jmri

RunDispatcherSystem = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/DispatcherSystem.py')
execfile(RunDispatcherSystem)