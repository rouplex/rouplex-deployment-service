rouplex-deployment-service
==========================

This repo provides various components such as java libraries, web resources and a web service / application that can 
be used for host deployment and management in public clouds such as Amazon EC2, Google Cloud, Azure.

The deployment service can deploy clusters of hosts, maintain leases of thereof, replenish the clusters depending on 
their load indicators (coming soon), or destroy them on command, or if they expire. Various configurations are 
available on a cluster level, where the deployment service can be taking out or replace hosts that fail reporting 
their state for example.

This first implementation provides the basic functionality and is not high availability. Requests are fulfilled
by a single running instance, and all could be lost if that instance goes down for some reason. The upcoming 
implementation is addressing this as well as high availablity via the use of (SWF) workflows.

# Description #
This project is managed using maven and is composed of a few modules which follow the Rouplex model.

1. For each sub-service, there is a module containing the service definition expressed as a java interface and related
POJOs/DTOs. This module is suffixed by "-api" and includes minimal dependencies (using only jax-rs-annotations) for 
pain-free dependency management on the client side. The build artifact is a slim jar that will be used by both a client
and a provider of the service. We try to model our services as REST, in which case we add the appropriate annotations
for the client/server adapters to take advantage of. One of these modules is rouplex-deployment-service-api.

1. The related implementation is found in a module of same name suffixed with "-provider". This module will produce a
jar which can be used as a component of a bigger deployment. In our example, rouplex-deployment-service-provider would
be the corresponding module.

1. The library obtained at (2) is also provided as a web resource ready for inclusion in a web app. This module will
produce a jar including various jersey dependencies. In our example, rouplex-deployment-service-provider-jersey would
be the corresponding module.

1. The resource obtained at (3) (normally along others), is then included in a web app. This module will produce a war 
file ready for deployment in application servers. In our example, rouplex-deployment-webapp would be the corresponding 
module.

# Versioning #
We use semantic versioning, in its representation x.y.z, x stands for API update, y for dependencies update, and z for
build number.

# Build #
1. Java 8 is required to build the project. Make sure the installation is successful by typing `java -version` on a 
shell window; the command output should show the version.

1. Maven is required to build the project. Make sure the installation is successful by typing `mvn -version` on a 
shell window; the command output should be showing the installation folder.

1. On a shell window, and from the folder containing this README file, type `mvn clean install` and if successful, you
will have the built artifacts in appropriate 'target' folders located under the appropriate modules. The same jars 
will be installed in your local maven repo.

# Test #
`mvn test` will execute all the tests and the console output should show success upon finishing.

# Run #
To run locally and mostly for debugging purposes, type `cd rouplex-deployment-webapp; mvn tomcat7:run` on a
shell window to start the server. Then type `mvn exec:java` on a separate window to start a browser client (pointing at
http://localhost:8080/webjars/swagger-ui/2.2.5/index.html?url=http://localhost:8080/rest/swagger.json)
Refer to the API section for details on requests and related responses.

# Deploy #
To deploy and run remotely on an App server you must make sure you follow these steps:

1. Java8 will be needed to run the deployment service on your host(s). You can get it via `wget --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u102-b14/jdk-8u102-linux-x64.rpm; sudo yum localinstall jdk-8u102-linux-x64.rpm`

1. An application container is required to run the service. You can download tomcat if none is available on your host.
`wget http://archive.apache.org/dist/tomcat/tomcat-8/v8.5.12/bin/apache-tomcat-8.5.12.tar.gz; tar -xvf apache-tomcat-8.5.12.tar.gz`

1. A server key and certificate is required to run the test servers. You can create your own or you can copy the
keystore at rouplex-deployment-service/rouplex-deployment-webapp/src/test/resources/server-keystore somewhere on your
host. Let say you copied it on $TOMCAT_HOME/conf/server-keystore. The keystore password is "kotplot" without the quotes.

1. The test servers must be configured to find the geoLocation of the keystore. That can be done by editing
(or creating) $TOMCAT_HOME/bin/setenv.sh file to add the line containing the system properties used by JVM for this purpose `export JAVA_OPTS="-Djavax.net.ssl.keyStore=$TOMCAT_HOME/conf/server-keystore -Djavax.net.ssl.keyStorePassword=kotplot"`

1. The application container must be started ($TOMCAT_HOME/catalina.sh start is one way of doing it) for a dynamic
deployment (or one can opt for a static deployment, equivalent, but out of the scope of this guide)

1. You must now deploy the deployment service to the application container. Point your browser at
`http://domain.com:8080/manager/html` and you should see the tomcat manager page.
  * If you get permission denied, it is because your manager by default is configured to allow only local connections.
  You can override that behaviour by editting manager's config `vi $TOMCAT_HOME/webapps/manager/META-INF/context.xml`
  and lifting the restriction by commenting out the valve, or restrict to your public ip address (not shown).
```xml
<Context antiResourceLocking="false" privileged="true" >
    <!-- <Valve className="org.apache.catalina.valves.RemoteAddrValve" allow="127\.\d+\.\d+\.\d+|::1|0:0:0:0:0:0:0:1" /> -->
</Context>
```
  * The role, username and password for the admin are set via `vi $TOMCAT_HOME/conf/tomcat-users.xml`. Make sure you have something like
```xml
<tomcat-users>
<role rolename="manager-gui"/>
<user username="tomcat" password="<password>" roles="manager-gui"/>
</tomcat-users>
```
1. Deploy the deployment service by uploading it in tomcat via deploy button (context path will be: "/" with no quotes).

1. Use the browser to get to url (http://domain.com:8080/webjars/swagger-ui/2.2.5/index.html?url=http://domain.com:8080/rest/swagger.json)

# Contribution guidelines #

* Writing tests
* Code review
* Other guidelines

# Who do I talk to? #

* Repo owner or admin
andimullaraj@gmail.com
