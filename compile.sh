#!/bin/bash

# Compile .java to .jar
./scripts/java.sh

# Execute preprocessors
./scripts/preprocessors.sh

# Execute platuml
./scripts/plantuml.sh