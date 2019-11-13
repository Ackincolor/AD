#!/usr/bin/env bash
curl -X POST -H "Content-Type: application/json" \
        --data @curl-test-client-input-1.json \
        --cacert ssl/ca/certs/ca.cert.pem \
        https://darkops:42308/convert
