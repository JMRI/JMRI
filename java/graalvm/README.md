This directory contains GraalVM-specific code that require a GraalVM JDK to build.

It creates the ib/jmri.script.jsr223graalpython.jar file to contain its contents for running under a regular Java 11 JRE.

To recreate the jar file:
```
jdk11g
ant graal-compile
cd target/graalvm-classes
jar cf ../../lib/jmri.script.jsr223graalpython.jar .
cd ../..
```
