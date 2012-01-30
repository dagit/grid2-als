#!/usr/bin/env bash

PKI_DIR=../grid2-vo/pki
SRC_CERT=$PKI_DIR/generated/users/idservtest_cert.pem
DST_CERT=testcert.der

openssl x509 -in $SRC_CERT -out $DST_CERT -outform DER
