#!/bin/bash

# be verbose and exit on any non-zero exit code
# see https://docs.travis-ci.com/user/customizing-the-build/#Implementing-Complex-Build-Steps
set -ev

export MAVEN_OPTS=-Xmx1536m

if [[ "$HEADLESS" == "true" ]] ; then
    # run FindBugs only on headless, failing build if bugs are found
    # FindBugs configuration is in pom.xml
    mvn test -U -P travis-findbugs --batch-mode
    # run headless tests
    mvn test -U -P travis-headless --batch-mode -Dsurefire.printSummary=${PRINT_SUMMARY} -Dsurefire.runOrder=${RUN_ORDER} -Dant.jvm.args="-Djava.awt.headless=${HEADLESS}" -Djava.awt.headless=${HEADLESS} -Dcucumber.options="--tags ~@Ignore --tags ~@firefox" 
else
    # run full GUI test suite and fail on coverage issues
    mvn javadoc:javadoc verify -U -P travis-coverage --batch-mode -Dsurefire.printSummary=${PRINT_SUMMARY} -Dsurefire.runOrder=${RUN_ORDER} -Dant.jvm.args="-Djava.awt.headless=${HEADLESS}" -Djava.awt.headless=${HEADLESS} -Dcucumber.options="--tags ~@Ignore" 
fi
