#!/bin/bash

# Define the path to the JSON library
LIBS_PATH="resources/libraries/json-20240303.jar"

# Create the temp directory if it doesn't exist
mkdir -p temp

# Unzip the JSON jar into the temp directory
unzip -q -o "$LIBS_PATH" -d temp

# Function to convert CamelCase to hyphen-lowercase
to_hyphen_lowercase() {
    echo "$1" | sed -E 's/([a-z0-9])([A-Z])/\1-\2/g' | tr '[:upper:]' '[:lower:]'
}

# Find and compile each .java file individually in resources/preprocessors
for java_file in $(find resources/preprocessors/ -name "*.java"); do
    # Extract the base filename without extension
    base_name=$(basename "$java_file" .java)

    # Convert the base name to hyphenated lowercase for the .jar filename
    jar_name=$(to_hyphen_lowercase "$base_name")

    # Compile the .java file into the temp directory
    javac -cp temp -d temp "$java_file"

    # Create a manifest file with the base class name as the Main-Class
    echo "Main-Class: $base_name" > temp/MANIFEST.MF

    # Package the compiled classes and the JSON library into a JAR with the new name
    jar cfm "temp/${jar_name}.jar" temp/MANIFEST.MF -C temp .

    # Clean up for the next .java file
    rm -f temp/MANIFEST.MF
done

# Clean up the unpacked JSON library files
rm -rf temp/META-INF
rm -rf temp/org