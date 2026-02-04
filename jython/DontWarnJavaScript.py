# JavaScript/ECMAscript support is being deprecated and may soon be removed.
# 
# As part of this, use of that support will fire a dialog box to warn people.
# Since that can be really annoying if it happens on every JMRI start, we
# provide this script to disable the warning.

import jmri
jmri.script.JmriScriptEngineManager.dontWarnJavaScript = True

import org.slf4j
log = org.slf4j.LoggerFactory.getLogger("JavaScript Interpreter")
log.warn("The following warning has been suppressed:")
log.warn("*** Scripting with JavaScript/ECMAscript is being deprecated ***")
log.warn("*** and may soon be removed.  If you are using this, please  ***")
log.warn("*** contact us on the jmriusers group for assistance.        ***")
