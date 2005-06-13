REM Start the LocoTools Java program ($Revision: 1.9 $)

java -noverify -Dsun.java2d.d3d=false -Djava.security.policy=security.policy -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw -Djava.class.path=".;classes;jmriplugins.jar;jmri.jar;comm.jar;Serialio.jar;log4j.jar;collections.jar;jh.jar;crimson.jar;jdom-jdk11.jar;jython.jar;MRJAdapter.jar" apps.LocoTools.LocoTools %*
