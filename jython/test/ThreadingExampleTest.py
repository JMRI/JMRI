# Test the ThreadingExample.py script

print ("The ThreadingExample script will print a line about running on the GUI thread")
execfile("jython/ThreadingExample.py")

# just confirm that this runs OK in headless mode (when graphical, it prompts)
ThreadingExample().start()
