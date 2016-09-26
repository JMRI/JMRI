# Create the data structures for the JavaOne demo layout
#
# Author: Bob Jacobsen, copyright 2006
# Part of the JMRI distribution

# create the blocks themselves
IB150 = jmri.Block("IB150")
IB151 = jmri.Block("IB151")
IB152 = jmri.Block("IB152")
IB153 = jmri.Block("IB153")
IB154 = jmri.Block("IB154")
IB155 = jmri.Block("IB155")
IB156 = jmri.Block("IB156")
IB157 = jmri.Block("IB157")
IB158 = jmri.Block("IB158")
IB159 = jmri.Block("IB159")
IB160 = jmri.Block("IB160")
IB161 = jmri.Block("IB161")
IB162 = jmri.Block("IB162")
IB163 = jmri.Block("IB163")
IB164 = jmri.Block("IB164")
IB165 = jmri.Block("IB165")

blocks = [IB150, IB151, IB152, IB153, IB154, IB155, IB156, IB157, 
          IB158, IB159, IB160, IB161, IB162, IB163, IB164, IB165]

# Load sensors
IB150.setSensor(sensors.provideSensor("150"))
IB151.setSensor(sensors.provideSensor("151"))
IB152.setSensor(sensors.provideSensor("152"))
IB153.setSensor(sensors.provideSensor("153"))
IB154.setSensor(sensors.provideSensor("154"))
IB155.setSensor(sensors.provideSensor("155"))
IB156.setSensor(sensors.provideSensor("156"))
IB157.setSensor(sensors.provideSensor("157"))
IB158.setSensor(sensors.provideSensor("158"))
IB159.setSensor(sensors.provideSensor("159"))
IB160.setSensor(sensors.provideSensor("160"))
IB161.setSensor(sensors.provideSensor("161"))
IB162.setSensor(sensors.provideSensor("162"))
IB163.setSensor(sensors.provideSensor("163"))
IB164.setSensor(sensors.provideSensor("164"))
IB165.setSensor(sensors.provideSensor("165"))

tracksensors = [
    "LS150", "LS151", "LS152", "LS153", "LS154", "LS155", "LS156", "LS157",
    "LS158", "LS159", "LS160", "LS161", "LS162", "LS163", "LS164", "LS165"
]

# Load connections
CW = jmri.Path.CW
CCW = jmri.Path.CCW

# Simple blocks, main first
IB150.addPath(jmri.Path(IB151, CCW, CW))
IB150.addPath(jmri.Path(IB163, CW, CCW))

IB151.addPath(jmri.Path(IB165, CCW, CW))
IB151.addPath(jmri.Path(IB150, CW, CCW))

IB152.addPath(jmri.Path(IB154, CCW, CW))
IB152.addPath(jmri.Path(IB165, CW, CCW))

IB153.addPath(jmri.Path(IB155, CCW, CW))
IB153.addPath(jmri.Path(IB165, CW, CCW))

IB154.addPath(jmri.Path(IB164, CCW, CW))
IB154.addPath(jmri.Path(IB152, CW, CCW))

IB156.addPath(jmri.Path(IB157, CCW, CW))
IB156.addPath(jmri.Path(IB164, CW, CCW))

IB155.addPath(jmri.Path(IB164, CCW, CW))
IB155.addPath(jmri.Path(IB153, CW, CCW))

IB157.addPath(jmri.Path(IB162, CCW, CW))
IB157.addPath(jmri.Path(IB156, CW, CCW))

IB158.addPath(jmri.Path(IB160, CCW, CW))
IB158.addPath(jmri.Path(IB162, CW, CCW))

IB159.addPath(jmri.Path(IB161, CCW, CW))
IB159.addPath(jmri.Path(IB162, CW, CCW))

IB160.addPath(jmri.Path(IB163, CCW, CW))
IB160.addPath(jmri.Path(IB158, CW, CCW))

IB161.addPath(jmri.Path(IB163, CCW, CW))
IB161.addPath(jmri.Path(IB159, CW, CCW))

# blocks with turnouts
LT200 = turnouts.provideTurnout("200")
LT201 = turnouts.provideTurnout("201")
LT202 = turnouts.provideTurnout("202")
LT203 = turnouts.provideTurnout("203")

IB162.addPath(jmri.Path(IB158, CCW, CW, jmri.BeanSetting(LT200, CLOSED)))
IB162.addPath(jmri.Path(IB159, CCW, CW, jmri.BeanSetting(LT200, THROWN)))
IB162.addPath(jmri.Path(IB157, CW, CCW))

IB163.addPath(jmri.Path(IB160, CW, CCW, jmri.BeanSetting(LT201, CLOSED)))
IB163.addPath(jmri.Path(IB161, CW, CCW, jmri.BeanSetting(LT201, THROWN)))
IB163.addPath(jmri.Path(IB150, CCW, CW))

IB164.addPath(jmri.Path(IB154, CW, CCW, jmri.BeanSetting(LT202, CLOSED)))
IB164.addPath(jmri.Path(IB155, CW, CCW, jmri.BeanSetting(LT202, THROWN)))
IB164.addPath(jmri.Path(IB156, CCW, CW))

IB165.addPath(jmri.Path(IB152, CCW, CW, jmri.BeanSetting(LT203, CLOSED)))
IB165.addPath(jmri.Path(IB153, CCW, CW, jmri.BeanSetting(LT203, THROWN)))
IB165.addPath(jmri.Path(IB151, CW, CCW))


