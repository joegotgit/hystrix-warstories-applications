#!/bin/bash

baseurl=http://192.168.178.31:2379/v2/keys/hystrix

curl -L -X PUT $baseurl/etcd.status -d value="value from etcd changed via put"
