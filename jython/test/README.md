## Unit and Functional Testing for Scripts for JMRI Releases ##

See jmri_bindings_test.py  for sample test, including error reporting.

Any test files in this directory will be run (sequentially) by java/test/jmri/jmrit/jython/SampleScriptTest.java[https://github.com/JMRI/JMRI/blob/master/java/test/jmri/jmrit/jython/SampleScriptTest.java] during normal JUnit testing. No environment is set up; if a test needs a particular manager, it should create it before invoking the script-under-test.

Setting the jmri.skipjythontests property to "true" will omit loading and running these tests

Notes: 

- These run in a single (e.g. hot) interpreter.  As we add tests, we may start seeing interference, and will have to deal with it.  But for now, this is a basic, quick approach.

- When errors do happen, the reporting is ugly.  This can be improved (of course, we don't want errors in the first place)


