//THIS FILE SHALL BE DELETED (I JUST DON´T KNOW HOW TO DO THAT)
//IT IS RENAMED TO UhlenbrockPacketizer.java TO AVOID CONFUSION WITH jmri.jmrix.loconet.intellibox.IBLnPacketizer

//The patch in total consists of
// 1. Renaming jmri.jmrix.loconet.uhlenbrock.IBLnPacketizer to jmri.jmrix.loconet.uhlenbrock.UhlenbrockPacketizer
//    in order to avoid confusion with jmri.jmrix.loconet.intellibox.IBLnPacketizer
// 2. Re-implementing patch #978 - this time for IB-COM and Intellibox II - #978 was only implemented for the old
//    Intellibox. This has been done by using readByteProtected method instead of readByte in UhlenbrockPacketizer
//    in order to avoid missing bytes in the input stream now and then (resulting in unstable sensors and probaly
//    other problems as well).
// 3. A better implementation of patch #991 (i.e. the problem that turnouts do not work with IB-COM). The here
//    implemented solution is a proper instantiation of LocoNetThrottledTransmitter in
//    jmri.jmrix.loconet.uhlenbrock.UhlenbrockSystemConnectionMemo.configureManagers() by using the inherited method.
// 4. Enhancements to Loconet Monitor to parse the special IB-COM CV programming messages.
// 5. Implementation of CV programming trough IB-COM / Intellibox II. 
//    This is implemented in the following new files in the jmri.jmrix.loconet.uhlenbrock package:
//         - UhlenbrockProgrammerManager.java
//         - UhlenbrockSlotManaager.java
//    as well as a few changes in
//         - UhlenbrockAdapther.java
//         - UhlenbrockSystemConnectionMemo.java
//         - jmri.jmrix.loconet.LoconetSystemConnectionMemo.java
