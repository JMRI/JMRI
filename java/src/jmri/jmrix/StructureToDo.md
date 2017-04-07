# To Do list:  jmri.jmrix

This list was created as part of structuring the C/MRI system, Spring 2017

Some of these things are by system, e.g. the C/MRI specific actions that might be needed in lots of other systems

- [ ] Documentation
  - [ ] Startup cases
  - [ ] Revisit the "new system" and "new type" documentation.  Needed?

C/MRI:
- [ ] Regularize access to the Nodes:  Via a NodeList class, obtained from the SystemConnectionMemo
  - [ ] Create class and tests
  - [ ] Have all connection types load from, into SysConnMemo
  - [ ] Store from ConfigXML properly, all C/MRI adapters
  - [ ] Pass in for access all classes:
java/src/jmri/jmrix/cmri//serial/assignment/ListFrame.java
java/src/jmri/jmrix/cmri//serial/diagnostic/DiagnosticFrame.java
java/src/jmri/jmrix/cmri//serial/networkdriver/configurexml/ConnectionConfigXml.java
java/src/jmri/jmrix/cmri//serial/nodeconfig/NodeConfigFrame.java
java/src/jmri/jmrix/cmri//serial/SerialAddress.java
java/src/jmri/jmrix/cmri//serial/serialdriver/configurexml/ConnectionConfigXml.java
java/src/jmri/jmrix/cmri//serial/SerialLight.java
java/src/jmri/jmrix/cmri//serial/SerialNode.java
java/src/jmri/jmrix/cmri//serial/SerialSensorManager.java
java/src/jmri/jmrix/cmri//serial/SerialTrafficController.java
java/src/jmri/jmrix/cmri//serial/SerialTurnout.java
    

- [ ] The correct terminology is "PortAdapter", hence "Adapter". ("Controller" as a name should denote TrafficController et al)
  - [ ] Use e.g. NetBeans refactor to rename the base classes: 
    - [ ] AbstractPortController
    - [ ] AbstractNetworkPortController
    - [ ] AbstractSerialPortController
    - [ ] AbstractStreamPortController
  - [ ] The test scaffold class
    - [ ] AbstractPortControllerScaffold
  - [ ] and their test classes
    - [ ] AbstractPortControllerTestBase
    - [ ] AbstractSerialPortControllerTestBase
    - [ ] AbstractNetworkPortControllerTestBase
    - [ ] AbstractStreamPortControllerTestBase
  - [ ] As time allows, rename in the specific implementatio
  
C/MRI long term?
- [ ] SerialNode constants to enum, including storage via digit or enum-entry-name
- [ ] SerialAddress static action - should all this static stuff be here, or moved to e.g. List object which has info about e.g. system name?
   ```
   java/src/jmri/jmrix/cmri//serial/SerialAddress.java:    public static int getNodeAddressFromSystemName(String systemName) {
   java/src/jmri/jmrix/cmri//serial/SerialAddress.java:    public static AbstractNode getNodeFromSystemName(String systemName,SerialTrafficController tc) {
   ```

- [ ] Really long term, convert this to a more regular top-to-bottom structure stored by ConfigureXml, instead of the hybrid "Config object creates port which creates what's needed above it"
