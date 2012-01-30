#!/usr/bin/env bash

# This script uploads the CAS WAR file to bootstrap.galois.com so it
# can be downloaded and deployed on the CAS server.

BOOTSTRAP=diesel.galois.com
REMOTE_PATH=/srv/www/bootstrap/html/grid2/
WAR=cas.war
ARCHIVE=target/$WAR

if [ ! -f "$ARCHIVE" ]
then
    echo "No war file found at '$ARCHIVE'"
    echo "Please run 'mvn clean package' first."
    exit 1
fi

echo "War file found at '$ARCHIVE'; uploading to:"
echo "  ${BOOTSTRAP}:${REMOTE_PATH}"

rsync "$ARCHIVE" ${BOOTSTRAP}:${REMOTE_PATH}

# It's OK if the chmod fails (which it will if the file was not
# previously owned by you)
ssh ${BOOTSTRAP} "chmod g+w ${REMOTE_PATH}/${WAR}" || true