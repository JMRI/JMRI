# Start the PanelPro.bat Java program
# ($Revision:)
# The -Dsun.java2d.noddraw option cures a Windows-specific incompatibility
# with DirectX
java -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ -Djava.class.path=".;classes;jmri.jar;lib\collections.jar;lib\log4j.jar;lib\jdom-jdk11.jar;lib\crimson.jar;lib\comm.jar;lib/jython.jar;$VFS" -Dsun.java2d.noddraw apps.JmriDemo.JMRIdemo

