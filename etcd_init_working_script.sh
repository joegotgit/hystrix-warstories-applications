#!/bin/bash

# This script configures the systems, so it will work initially.

myip=192.168.178.31

#baseurl=http://192.168.178.31:2379/v2/keys/hystrix
baseurl=http://$myip:2379/v2/keys/hystrix

echo --- remove hystrix keys from etcd server ---
curl -L -X DELETE $baseurl?recursive=true

echo --- loading hystrix properties with default values ---
#curl -L -X PUT $baseurl/server.etcd.baseurl -d value="http://etcd:2379"
curl -L -X PUT $baseurl/etcd.status -d value="value from etcd"

# Chaos Monkey
curl -L -X PUT $baseurl/chaos.monkey.active -d value="false"
curl -L -X PUT $baseurl/chaos.monkey.timeout -d value="3000"

# Default Service URLs, invalid at startup
curl -L -X PUT $baseurl/service.address.customer -d value="http://$myip:8082/customer/find/accountnumber/"
curl -L -X PUT $baseurl/service.address.connote -d value="http://$myip:8081/connote/create"

# Transport Service Hystrix Timeouts
curl -L -X PUT $baseurl/hystrix.command.ConnoteClientCmdKey.execution.isolation.thread.timeoutInMilliseconds -d value="4000"
curl -L -X PUT $baseurl/hystrix.command.CustomerClientCmdKey.execution.isolation.thread.timeoutInMilliseconds -d value="4000"

# Fallbacks enabled
curl -L -X PUT $baseurl/hystrix.command.ConnoteServiceCmdKey.fallback.enabled -d value="true"
curl -L -X PUT $baseurl/hystrix.command.CustomerServiceCmdKey.fallback.enabled -d value="true"

echo --- DONE ---
