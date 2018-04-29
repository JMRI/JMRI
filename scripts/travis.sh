#!/bin/bash

# be verbose and exit on any non-zero exit code
# see https://docs.travis-ci.com/user/customizing-the-build/#Implementing-Complex-Build-Steps
set -ev

# set defaults if not already set
PRINT_SUMMARY=${PRINT_SUMMARY:-true}
RUN_ORDER=${RUN_ORDER:-filesystem}
HEADLESS=${HEADLESS:-false}

export MAVEN_OPTS=-Xmx1536m

if [[ "${HEADLESS}" == "true" ]] ; then
    # compile with ECJ for warnings or errors
    mvn antrun:run -Danttarget=tests-warnings
    # run SpotBugs only on headless, failing build if bugs are found
    # SpotBugs configuration is in pom.xml
    mvn clean test -U -P travis-spotbugs --batch-mode
    # run javadoc, headless tests
    mvn javadoc:javadoc test -U -P travis-headless --batch-mode \
        -Dsurefire.printSummary=${PRINT_SUMMARY} \
        -Dsurefire.runOrder=${RUN_ORDER} \
        -Dant.jvm.args="-Djava.awt.headless=${HEADLESS}" \
        -Djava.awt.headless=${HEADLESS} \
        -Dcucumber.options="--tags 'not @Ignore' --tags 'not @firefox'"
else
    # run full GUI test suite and fail on coverage issues
    mvn verify -U -P travis-coverage --batch-mode \
        -Dsurefire.printSummary=${PRINT_SUMMARY} \
        -Dsurefire.runOrder=${RUN_ORDER} \
        -Dant.jvm.args="-Djava.awt.headless=${HEADLESS}" \
        -Djava.awt.headless=${HEADLESS} \
        -Dcucumber.options="--tags 'not @Ignore'"
fi
