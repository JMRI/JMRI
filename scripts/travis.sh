#!/bin/bash

# be verbose and exit on any non-zero exit code
# see https://docs.travis-ci.com/user/customizing-the-build/#Implementing-Complex-Build-Steps
set -ev

export MAVEN_OPTS=-Xmx1536m

if [[ "$HEADLESS" == "true" ]] ; then
    # run FindBugs only on headless, failing build if bugs are found
    # FindBugs configuration is in pom.xml
    mvn test -U --batch-mode -DskipTests=true -Djava.awt.headless=${HEADLESS} findbugs:check
    # run headless tests
    mvn test -U -P travis --batch-mode -Dant.jvm.args="-Djava.awt.headless=${HEADLESS}" -Djava.awt.headless=${HEADLESS}
else
    # run full GUI test suite and fail on coverage issues
    mvn test -U -P travis-coverage --batch-mode -Dant.jvm.args="-Djava.awt.headless=${HEADLESS}" -Djava.awt.headless=${HEADLESS}
fi