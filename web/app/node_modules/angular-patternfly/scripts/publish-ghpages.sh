#!/usr/bin/env bash

set -o errexit -o nounset

####################################
# repo specific variables should be set in .travis.yml
####################################
# TRIGGER_REPO_SLUG="patternfly/patternfly"
# TRIGGER_REPO_BRANCH="master"
####################################
####################################

SCRIPT=`basename $0`
ACTION="Manual"
REPO_NAME="origin"
SOURCE_BRANCH=`git rev-parse --abbrev-ref HEAD`
QUIET=false
USE_SITE_REPO=false
PUSH_REPO=false
PUSH_BRANCH="gh-pages"
BOWER_WEB_FILES_ONLY=false

RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[1;32m'
NC='\033[0m' # No Color

echoHeader () {
  echo
  echo -e "${YELLOW}################################################################################${NC}"
  echo -e "${GREEN}${@}${NC}"
  echo -e "${YELLOW}################################################################################${NC}"
}

confirm () {
  if $QUIET; then
    true
  else
    # call with a prompt string or use a default
    QUESTION="${1:-Are you sure? [y/N]} "
    echo -e -n $QUESTION
    read -r RESPONSE
    case $RESPONSE in
      [yY][eE][sS]|[yY])
        true
        ;;
      *)
        false
        ;;
    esac
  fi
}

setUserInfo () {
  git config --global user.name "patternfly-build"
  git config --global user.email "patternfly-build@redhat.com"
  git config --global push.default simple
}

getDeployKey () {
  # Get the deploy key by using Travis's stored variables to decrypt deploy_key.enc
  ENCRYPTED_KEY_VAR="encrypted_${ENCRYPTION_LABEL}_key"
  ENCRYPTED_IV_VAR="encrypted_${ENCRYPTION_LABEL}_iv"
  ENCRYPTED_KEY=${!ENCRYPTED_KEY_VAR}
  ENCRYPTED_IV=${!ENCRYPTED_IV_VAR}
  openssl aes-256-cbc -K $ENCRYPTED_KEY -iv $ENCRYPTED_IV -in deploy_key.enc -out deploy_key -d
  chmod 600 deploy_key
  eval `ssh-agent -s`
  ssh-add deploy_key
}

checkTriggerRepo () {
  echo "$TRAVIS_REPO_SLUG $TRIGGER_REPO_SLUG $TRIGGER_REPO_BRANCH"
  if [ "${TRAVIS_REPO_SLUG}" = "${TRIGGER_REPO_SLUG}" ]; then
    echo "This action is running against ${TRIGGER_REPO_SLUG}."
    if [ -z "${TRAVIS_TAG}" -a "${TRAVIS_BRANCH}" != "${TRIGGER_REPO_BRANCH}" ]; then
      echo "This commit was made against ${TRAVIS_BRANCH} and not the ${TRIGGER_REPO_BRANCH} branch. Aborting."
      exit 1
    fi
  else
    echo "This action is not running against ${TRIGGER_REPO_SLUG}. Aborting."
    exit 1
  fi
}

inferRepo () {
  if [[ "${REPO_NAME}" == *.git ]]; then
    PUSH_REPO=$REPO_NAME
  else
    REPO=`git config remote.${REPO_NAME}.url`
    # Use the externally set global PUSH_REPO by default
    PUSH_REPO="${REPO/https:\/\/github.com\//git@github.com:}"
  fi
  echo "Inferred REPO ${PUSH_REPO}"
}

confirmRepo () {
  confirm "${YELLOW}Push ${SITE_FOLDER} to repo ${PUSH_REPO}/${PUSH_BRANCH}? [y/N] ${NC}"
  return $?
}

checkRemoteBranchExists () {
  EXISTING=`git ls-remote --heads ${PUSH_REPO} ${PUSH_BRANCH}`
  echo $EXISTING
}

cleanSite () {
  if [ -d "github.io" ]; then
    rm -rf github.io
  fi
}

cleanBranch () {
  # check if the branch exists without triggering errexit
  git rev-parse --verify --quiet gh-pages-deploy 1> /dev/null && rc=$? || rc=$?
  if [ "$rc" = 0 ]; then
    CURRENT_BRANCH=`git rev-parse --abbrev-ref HEAD`
    if [ "$CURRENT_BRANCH" = "gh-pages-deploy" ]; then
      git checkout $SOURCE_BRANCH
    fi
    git branch -D gh-pages-deploy
  fi
}

cloneSite () {
  git clone --branch ${PUSH_BRANCH} $PUSH_REPO github.io
}

copySite () {
  echo rsync -q -rav --delete --exclude .git ${SITE_FOLDER} github.io
  rsync -q -rav --delete --exclude .git ${SITE_FOLDER}/ github.io/
  if $BOWER_WEB_FILES_ONLY; then
    find ${SITE_FOLDER}/components -type f -not -regex ".*/.*\.\(html\|js\|css\|less\|otf\|eot\|svg\|ttf\|woff\|woff2\)" -print0 | xargs -0 rm
  fi
}

pushSite () {
  SHA=`git rev-parse HEAD`
  git -C github.io add . -A
  if [ -z "$(git -C github.io status --porcelain)" ]; then
    echo -e "${YELLOW}Site directory clean, no changes to commit.${NC}"
  else
    echo -e "${YELLOW}Changes in site directory, committing changes.${NC}"
    git -C github.io commit -q -a -m "Added files from commit #${SHA} "
    echo -e "Pushing commit ${SHA} to repo ${PUSH_REPO}."
    confirmRepo && rc=$? || rc=$?
    if [ "$rc" = 0 ]; then
      git -C github.io push $PUSH_REPO ${PUSH_BRANCH}:${PUSH_BRANCH}
    fi
  fi
}

splitSite () {
  git checkout -b gh-pages-deploy
  git add -f ${SITE_FOLDER}
  git commit -q -m "Added ${SITE_FOLDER} folder"

  SHA=`git subtree split --prefix ${SITE_FOLDER} gh-pages-deploy`
  echo -e "Pushing commit ${SHA} to repo ${PUSH_REPO}."
  confirmRepo && rc=$? || rc=$?
  if [ "$rc" = 0 ]; then
   git push ${PUSH_REPO} ${SHA}:refs/heads/${PUSH_BRANCH} --force
  fi
}

deploySite () {
  if [ "$SOURCE_BRANCH" = "gh-pages-deploy" ]; then
    echo -e "${RED}Error: cannot deploy from the current branch.  Please checkout a different branch.${NC}"
    exit -1
  fi

  git checkout ${SOURCE_BRANCH}
  inferRepo $REPO_NAME
  EXISTING=`checkRemoteBranchExists`
  if [ -n "$EXISTING" ]; then
    echo -e "${GREEN}### ${PUSH_BRANCH} branch exists, pushing updates${NC}"
    cleanSite
    cloneSite
    copySite
    pushSite
    cleanSite
  else
    echo -e "${GREEN}### ${PUSH_BRANCH} branch does not exist, splitting branch${NC}"
    cleanBranch
    splitSite
    cleanBranch
  fi
}

manualDeploy () {
  deploySite
}

travisDeploy () {
  checkTriggerRepo
  setUserInfo
  getDeployKey
  deploySite
}

checkSiteFolderExists () {
  if [ -z $SITE_FOLDER ]; then
    echo -e "${RED}Error: Please specify a folder to publish.${NC}"
    usage
    exit -1
  fi

  if [ ! -d ${SITE_FOLDER} ]; then
    echo -e "${RED}Error: The '${SITE_FOLDER}' folder does not exsit.  Run the build script to generate it.${NC}"
    exit -1
  fi
}

parseOpts() {
  while getopts htwb:r: OPT "$@"; do
    case $OPT in
      h) usage; exit 0;;
      t) ACTION="Travis"
         QUIET=true
         ;;
      r) REPO_NAME=$OPTARG;;
      w) BOWER_WEB_FILES_ONLY=true;;
      b) PUSH_BRANCH=$OPTARG;;
    esac
  done

  shift $((OPTIND-1))
  SITE_FOLDER="${1:- }"
}

usage () {
cat <<- EEOOFF

    This script will publish files to the ${PUSH_BRANCH} branch of your repo.

    $SCRIPT [option] folder

    Example: $SCRIPT

    OPTIONS:
    h       Display this message
    t       Perform a deploy from travis, using a travis encrypted key
    w       Remove non-web files from the SITE_FOLDER/components folder prior to publishing
    b       Remote branch this script will publish to
            default: gh-pages
    r       Git repo this script will publish to
            eg.: origin, upstream, bleathem, git@github.com:bleathem/bleathem.github.io.git
            default: origin

EEOOFF
}

main () {
  parseOpts "$@"
  checkSiteFolderExists
  echoHeader "${ACTION} deploy of ${SOURCE_BRANCH}:${SITE_FOLDER} to ${PUSH_BRANCH}"
  case $ACTION in
    Manual)
      manualDeploy "$@"
    ;;
    Travis)
      travisDeploy
  esac
}

main "$@"
