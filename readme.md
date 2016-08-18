# Hystrix Warstories Talk
Spring Boot App to demonstrate Netflix Hystrix.

To adapt the IP addres to your machine use this command in the directory of this project:
find . -type f -exec sed -i 's/192.168.178.31/<Your IP>/g' {} +

To build and start the example, maven, Java 8, docker-engine and docker-compose is needed.

1.) mvn clean package

2.) docker-compose up

3.) sh etcd_init_working_script.sh
to get a working configuration or
sh etcd_init_script.sh
to use the initial configuration for the presentation scripts.

4.) docker-compose restart
_

Code from my Hystrix Warstories Talk @ JAX 2016

# To check if the connection to the etcd is working:
All these commands:
curl http://192.168.178.31:8080/status
curl http://192.168.178.31:8081/status
curl http://192.168.178.31:8082/status
should return: ETCD Status: value from etcd
curl http://192.168.178.31:2379/v2/keys/
curl http://192.168.178.31:2379/v2/keys/hystrix
should return the current configurations

# How it works
There are three services:
* transport service (http://192.168.178.31:8080/create/<customernumber>)
* connote service (http://192.168.178.31:8081/connote/create)
* customer service (http://192.168.178.31:8082/customer/find/accountnumber/<customernumber>)

The customer service returns values for the following customer numbers (see ./customer-service/src/main/resources/data.sql): 4711, 1988, 9982, 2873, 8833, 1122, 2200, 1928
Otherwise it returns null.

The transport service will call the customer and the connote service.

The following configurations are available:
Transport service:
* service.address.connote=http://192.168.178.31:8081/connote/create/
* service.address.customer=http://192.168.178.31:8082/customer/find/accountnumber/

Connote service:
* chaos.monkey.active=false
* chaos.monkey.timeout=3000

Customer service:
* chaos.monkey.active=false
* chaos.monkey.timeout=3000

# To do
How the failure-scenarios are working

