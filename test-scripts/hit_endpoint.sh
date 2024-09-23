#!/bin/bash

curl -X POST localhost:8090/sign -H "Content-Type: application/json" -d "{\"message\": \"test\", \"sigValidityOverride\": 0}"