#!/bin/bash

baseurl=http://192.168.178.31:2379/v2/keys/hystrix

# Default Service URLs
clear
echo +++ Step 5 - Transport Hystrix Remote Service Fallbacks - enabled = true +++
echo
curl -L -X PUT $baseurl/hystrix.command.ConnoteServiceCmdKey.fallback.enabled -d value="true"
curl -L -X PUT $baseurl/hystrix.command.CustomerServiceCmdKey.fallback.enabled -d value="true"
echo
