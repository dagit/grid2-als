#!/usr/bin/env bash

set -e

HERE="$(cd "$(dirname "$0")" ; pwd)"
cd "$HERE"

function usage () {
    cat <<EOF
$0

This program runs Maven in order to download and install all of the
dependencies for the Java projects for the Grid 2.0 project.

It installs the artifacts locally, and as a side-effect it does all of
the necessary downloading. It also sets up the paths for eclipse.

It's safe to re-run this script for an existing project.
EOF
}

QUICK=no
for ARG in "$@"
do
    case "$ARG" in
        -h|--help)
            usage
            exit 0
            ;;
        -q|--quick)
            QUICK=yes
            ;;
        *)
            echo "Unknown argument: $ARG" 1>&2
            usage 1>&2
            exit 1
            ;;
    esac
done

set -e
PROJECTS=(
    voms-admin-soap-api
    voms-attribute-fetcher
    voms-attribute-client
    cas-server-support-voms-admin
    cas-server-support-account-linking
    cas-config
)

for PROJECT in "${PROJECTS[@]}"
do
    cat <<EOF

================================================================
Installing dependencies and eclipse settings for $PROJECT
----------------------------------------------------------------

EOF

    pushd $PROJECT
    if [ "$QUICK" = yes ]
    then
        mvn install -Dmaven.test.skip=true
    else
        mvn eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true
        mvn clean source:jar javadoc:jar install
    fi
    popd
done
