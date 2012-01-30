#!/usr/bin/env bash

HERE="$(cd $(dirname "$0"); pwd)"

function usage() {
	cat <<EOF
$0 <properties file name>

Builds and runs the VOMS attribute client.

See client.properties in this directory for an example of the
properties.

Optionally, set M2_REPO in the shell environment to the location of
the desired maven repository to use.
EOF
}

if [ "$#" -ne 1 ]
then
	usage
	exit 1
fi

PROPERTIES_FILE="$1"

if [ -z "$M2_REPO" ]
then
    ARGS=()
else
    ARGS=("-Dmaven.repo.local=$M2_REPO")
fi

cd "$HERE"
mvn exec:java -B "${ARGS[@]}" -Dexec.mainClass="com.galois.grid2.voms.client.VOMSAttributeClient" -Dexec.args="$PROPERTIES_FILE"
