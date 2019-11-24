#!/bin/bash

# be verbose and exit on any non-zero exit code
# see https://docs.travis-ci.com/user/customizing-the-build/#Implementing-Complex-Build-Steps
set -ev

# set defaults if not already set
PRINT_SUMMARY=${PRINT_SUMMARY:-true}
RUN_ORDER=${RUN_ORDER:-filesystem}

HEADLESS=${HEADLESS:-false}
SKIPINTERMITTENT=${SKIPINTERMITTENT:-true}
STATIC=${STATIC:-false}

# ensure Jython can cache JAR classes
PYTHON_CACHEDIR="${HOME}/jython/cache"
mkdir -p ${PYTHON_CACHEDIR}

export MAVEN_OPTS=-Xmx1536m

# execute a specific set of tests
if [[ "${HEADLESS}" == "true" ]] ; then
    if [[ "${STATIC}" == "true" ]] ; then
        # compile with ECJ for warnings or errors
        #mvn -P test-warnings-check clean compile 
        mvn antrun:run -Danttarget=tests-warnings-check
        # run SpotBugs only on headless, failing build if bugs are found
        # SpotBugs configuration is in pom.xml
        mvn clean test -U -P travis-spotbugs --batch-mode
        # run Javadoc
        mvn javadoc:javadoc -U --batch-mode
        # check html
        mvn exec:exec -P travis-scanhelp
    else
        # run headless tests
        mvn test -U -P travis-headless --batch-mode \
            -Dsurefire.printSummary=${PRINT_SUMMARY} \
            -Dsurefire.runOrder=${RUN_ORDER} \
            -Dant.jvm.args="-Djava.awt.headless=${HEADLESS}" \
            -Djava.awt.headless=${HEADLESS} \
            -Djmri.skipTestsRequiringSeparateRunning=${SKIPINTERMITTENT} \
            -Dcucumber.options="--tags 'not @Ignore' --tags 'not @Headed'" \
            -Dpython.cachedir=${PYTHON_CACHEDIR}
    fi
else
    if [[ "${SKIPINTERMITTENT}" == "true" ]] ; then
        # run full GUI test suite and fail on coverage issues
        #       skipping XML Schema validation in long-running task, still done in headless
        mvn verify -U -P travis-coverage --batch-mode \
            -Dsurefire.printSummary=${PRINT_SUMMARY} \
            -Dsurefire.runOrder=${RUN_ORDER} \
            -Dant.jvm.args="-Djava.awt.headless=${HEADLESS}" \
            -Djava.awt.headless=${HEADLESS} \
            -Djmri.skipTestsRequiringSeparateRunning=${SKIPINTERMITTENT} \
            -Djmri.skipschematests=true \
            -Dcucumber.options="--tags 'not @Ignore'" \
            -Dpython.cachedir=${PYTHON_CACHEDIR}
    else        
        # run the SKIPINTERMITTENT tests separately
        mvn antrun:run -Danttarget=tests
        mvn antrun:run -Danttarget=run-sh
        ./scripts/run_flagged_tests_separately
    fi
fi

