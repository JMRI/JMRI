# Shuts down JMRI cleanly, then tells the OS to restart itself.
# Does not restart JMRI after the OS restarts, that's up to OS scripting
# Typically run from a Logix or button
# See also jython/Restart.py for a script that just restarts JMRI

shutdown.restartOS()
