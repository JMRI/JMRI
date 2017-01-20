# Test the jython/serialinput/SerialPortDevice.py script to make sure it basically compiles & runs

execfile("jython/serialinput/SerialPortDevice.py")

a = SerialPortDevice()
# not opening a port 
if (a.parse("1,2,3,4") != ["1","2","3","4"]) : raise AssertionError('Parse array not created correctly')

