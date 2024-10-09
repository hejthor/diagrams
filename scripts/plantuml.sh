#!/bin/bash

# Image
java -jar "resources/plantuml-1.2024.7.jar" -charset UTF-8 -tpng "input/*.puml" -o "../output"

# Vector
java -jar "resources/plantuml-1.2024.7.jar" -charset UTF-8 -tsvg "input/*.puml" -o "../output"

# Visio XML
# java -jar "resources/plantuml-1.2024.7.jar" -charset UTF-8 -tvdx "input/*.puml" -o "../output"

# Preprocessed PlantUML
# java -jar "resources/plantuml-1.2024.7.jar" -charset UTF-8 -preproc "input/*.puml" -o "../output"

# URL
# java -jar "resources/plantuml-1.2024.7.jar" -charset UTF-8 -computeurl "input/*.puml" -o "../output"