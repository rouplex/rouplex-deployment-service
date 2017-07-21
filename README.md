rouplex-deployment-service
==========================

This repo provides a jersey web resource as well as web application built using rouplex-platform-jersey and for 
deploying services in ec2 and other public clouds.

# Description #
This service will can be used to deploy applications in clusters of hosts, be that Ec2 or others (Google Cloud) 
coming soon. The build artifact is a war that can be deployed in an application container such as tomcat.

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
To run locally and mostly for debugging purposes, type `cd rouplex-deployment-service-provider-jersey; mvn tomcat7:run` on a
shell window to start the server then `mvn exec:java` on a separate window to start a browser client (pointing at
http://localhost:8080/rouplex-deployment-service-provider-jersey/webjars/swagger-ui/2.2.5/index.html?url=http://localhost:8080/rouplex-deployment-service-provider-jersey/rouplex/swagger.json)
Refer to the API section for details on requests and related responses.

# Deploy #
To deploy and run remotely on an App server you must make sure you follow these steps:

1. Java8 will be needed to run the deployment service on your host(s). You can get it via `wget --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u102-b14/jdk-8u102-linux-x64.rpm; sudo yum localinstall jdk-8u102-linux-x64.rpm`

1. An application container is required to run the service. You can download tomcat if none is available on your host.
`wget http://archive.apache.org/dist/tomcat/tomcat-8/v8.5.12/bin/apache-tomcat-8.5.12.tar.gz; tar -xvf apache-tomcat-8.5.12.tar.gz`

1. A server key and certificate is required to run the test servers. You can create your own or you can copy the
keystore at rouplex-deployment-service/rouplex-deployment-service-provider/src/test/resources/server-keystore somewhere on your
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
1. Deploy the deployment service by uploading it in tomcat via deploy button (context path will be: rouplex-deployment-service-provider-jersey-1.0.0-SNAPSHOT/).

1. Use the browser to get to url (http://domain.com:8080/rouplex-deployment-service-provider-jersey-1.0.0-SNAPSHOT/webjars/swagger-ui/2.2.5/index.html?url=http://domain.com:8080/rouplex-deployment-service-provider-jersey-1.0-SNAPSHOT/rouplex/swagger.json)

# Configure Host (Optional) #

## Tomcat as an init.d service ##
1. As root user, copy the file at deployment-service-provider-jersey/config/initd.tomcat.template to your host's /etc/init.d/tomcat
1. Grant exec permission to /etc/init.d/tomcat
1. Exec shell command `sudo service tomcat restart` and the tomcat will be running with the new settings, now and on a
system reboot

The initd.tomcat.template is quite classic for starting tomcat servers, we are only adding a few CATALINA_OPS to set
appropriate values for the heap memory to be used, as well as provide a configuration value used by JMX listener.
```
# Use 80% of the free memory
free_mem_kb=`free -t | grep Mem | awk '{print $2}'`
use_mem_mb=$(( free_mem_kb * 4 / 5 / 1024 ))m

# Ec2 call to get the public ip address, which is needed to expose the jmx ip/port for jconsole to connect to
public_ipv4=`curl http://169.254.169.254/latest/meta-data/public-ipv4`

#CATALINA_OPS are the extra options for tomcat to get in
export CATALINA_OPTS="-Xmx$use_mem_mb -Djava.rmi.server.hostname=$public_ipv4"
```

# Contribution guidelines #

* Writing tests
* Code review
* Other guidelines

# Who do I talk to? #

* Repo owner or admin
andimullaraj@gmail.com
