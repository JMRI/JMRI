# Test the ThreadingExample.py script

print ("The ThreadingExample script will print a line about running on the GUI thread")
execfile("jython/ThreadingExample.py")

# just confirm that this runs OK in headless mode (when graphical, it prompts)
te = ThreadingExample()
te.start()

# time to run
from time import sleep
sleep(0.020)

# stops itself
