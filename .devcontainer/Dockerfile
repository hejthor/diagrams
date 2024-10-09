# Use a lightweight base image
FROM ubuntu:latest

# Install dependencies
RUN apt-get update && apt-get install -y \
    openjdk-11-jdk \
    curl \
    graphviz \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64

# Set up the working directory
WORKDIR /workspace

# Copy all files from the local directory to the container's working directory
COPY . /workspace/

# Define the default command
CMD ["/bin/bash"]