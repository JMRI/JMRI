We can't now require everybody to install a GraalVM JDK, so we have a workaround:

 - This `java/graalvm` directory contains GraalVM-specific code that requires a GraalVM JDK to build.
 - The code lives in the `jmri.script.jsr223graalpython` package
    - Eventually, this source will move to `java/src/jmri/script/jsr223graalpython`
 - To let JMRI run without building in the meantime, we distribute this code in a `lib/jmri.script.jsr223graalpython.jar` file


To recreate that jar file to e.g. use updates you've made locally:

```
ant graal-compile
cd target/graalvm-classes
jar cf ../../lib/jmri.script.jsr223graalpython.jar .
cd ../..
```
