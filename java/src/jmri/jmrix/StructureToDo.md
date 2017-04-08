# To Do list:  jmri.jmrix

This list was created as part of structuring the C/MRI system, Spring 2017

Some of these things are by system, e.g. the C/MRI specific actions that might be needed in lots of other systems

- [ ] Documentation
  - [ ] Startup cases
  - [ ] Revisit the "new system" and "new type" documentation.  Needed?
    

The next set should be after the PJC merge, to avoid lots of conflicts:
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
  - [ ] As time allows, rename in the specific implementation
  
C/MRI:
- [ ] Regularize access to the Nodes:  Via the NodeList class, obtained from the SystemConnectionMemo
  - [ ] Create class and tests
  - [ ] Have all connection types load from, into SysConnMemo
  - [ ] Store from ConfigXML properly, all C/MRI adapters
  - [ ] Pass in for access all classes (instead of passing the TC into the call SerialAddress call)

C/MRI long term:
- [ ] SerialNode constants to enum, including storage via digit or enum-entry-name (and schema)
- [ ] SerialAddress static action - more all the static stuff to NodeList
   ```
   java/src/jmri/jmrix/cmri//serial/SerialAddress.java:    public static int getNodeAddressFromSystemName(String systemName) {
   java/src/jmri/jmrix/cmri//serial/SerialAddress.java:    public static AbstractNode getNodeFromSystemName(String systemName,SerialTrafficController tc) {
   ```

- [ ] Really long term, convert this to a more regular top-to-bottom structure stored by ConfigureXml, instead of the hybrid "Config object creates port which creates what's needed above it"
