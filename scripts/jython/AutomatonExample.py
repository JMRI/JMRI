# This is an example script for a JMRI "Automat" in Python

# init() is called exactly once at the beginning, and can 
# be used to do various initialization
def init():
    print "init was run!"
    return

# handle() is repeatedly called until it returns false
# Typically, it waits for a particular interesting condition to
# be valid, does the needed action, and then returns true to
# repeat. This sample just runs three times, printing a message,
# then quits.
def handle():
	global nloop
	print " handle count: ", nloop
	nloop = nloop + 1
	return (nloop != 3)  # run three times, then stop

nloop = 0
