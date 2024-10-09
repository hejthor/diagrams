#!/bin/bash

# Classes from JSON
java -jar "temp/classes-from-data.jar" -i "input/json/source.json" -o "temp"
java -jar "temp/classes-from-data.jar" -i "input/json/middle.json" -o "temp" -m
java -jar "temp/classes-from-data.jar" -i "input/json/target.json" -o "temp" -d "up"

# Classes from XML
java -jar "temp/classes-from-data.jar" -i "input/xml/data.xml" -o "temp"