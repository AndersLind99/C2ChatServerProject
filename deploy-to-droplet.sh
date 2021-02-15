#!/usr/bin/env bash

# Don't change this name UNLESS you know what you are doing
SERVER_NAME="chat-server"
DROPLET_URL="URL for your droplet"


echo "##############################"
echo "Building the Project          "
echo "##############################"

# If you have setup maven on your system, you can uncomment the line below
# this will compile your code, and copy the jar-file to the deploy folder
# mvn package


echo "##############################"
echo "Deploying The project..."
echo "##############################"

scp -r ./deploy/* root@$DROPLET_URL:/var/$SERVER_NAME