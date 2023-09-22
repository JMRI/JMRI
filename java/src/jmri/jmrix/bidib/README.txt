BiDiB® stands for BiDirectional Bus for the digital control of a model train. The term BiDiB® itself refers to the protocol technology, which can be implemented on various physical connections, such as Ethernet, USB or the BiDiBus, which is particularly optimized for the needs of model railroaders and system wiring.

BiDiB is not a commercial product and not connected to any particularly hardware. It is a protocol definition which can be implemented by anyone including commercial manufacturers.

BiDiB started in 2010 and was developed by Wolfgang Kufer (opendcc.de). For more information BiDiB see bidib.org.

BiDiB features:

- Bidirectional data transfer. Thus suitable for controlling locos, accessories and lights as well as for receiving occupancy feedback and other input.
- Binary data transfer secured by a CRC checksum.
- The Interface ("root node") of a BiDiB network is directly connected to JMRI using a serial-over-USB connection with either 9600 Baud, 115200 Baud or 1MBaud.
- A node on the network can implement various functions. One node can be the command station controlling the track-signal (probably DCC).
- Nodes supporting macro functionality can be configured to e.g. implement a complete signal head accepting just the requested aspect.
- BiDiB supports notifying of loco addresses if RailCom® enabled decoders are used (JMRI Reporters).
- BiDiB notifications are event driven. No polling is used.

The BiDiB Adaption to JMRI was developed by (c) Eckart Meyer, 2019-2023
