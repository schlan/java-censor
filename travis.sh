#!/usr/bin/env bash

boxOut(){
    local s="$*"
    tput setaf 3
    echo -e " =${s//?/=}=\n| $(tput setaf 4)$s$(tput setaf 3) |\n =${s//?/=}=\n"
    tput sgr 0
}

isPullRequest() {
    [[ "$TRAVIS_PULL_REQUEST" == "false" ]] && return 1 || return 0
}

exitOnFailedBuild() {
    if [ $? -ne 0 ]; then
        boxOut "BUILD FAILED: $1"
        exit 1
    fi
}

deployArtifacts() {
    boxOut "Deploying artefact"
    ./gradlew :java-censor-plugin:uploadArchives
    exitOnFailedBuild "Deploying artefact."
}

runTests() {
    boxOut "Run tests"
    ./gradlew test
    exitOnFailedBuild "Error running tests."
}

if isPullRequest ; then
    boxOut "This is a PR."
    runTests
else
    boxOut "This is a branch build."
    runTests
    deployArtifacts
fi
exit 0