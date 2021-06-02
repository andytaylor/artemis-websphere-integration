# Artemis Websphere-Integration
An example of configuring the Artemis Resource Adapter in WebSphere.

## Create the Broker

From the broker registry firstly set ARTEMIS_HOME to point at an Artemis distribution

```
export ARTEMIS_HOME=path-to-distribution
```

then create the broker instance

```
mvn verify
```

then start the broker

```yaml
 ./target/server0/bin/artemis run
```

## Build the MDB

navigate to the `tst-mdb` directory and run 

```
mvn clean package
```
## Build and deploy the Websphere Transaction Manager locator

---
**WARNING**

Currently because of an issue with loading the WebSphere location manager this feature is currently not possible. The issue 
is that when the classloader is isolated an error occurs when loading in the `WSTransactionManagerLocator` class that is 
part of the transaction-manager-locator project. The error message is `java.lang.ClassCastException: com.ibm.ws.tx.jta.TranManagerSet incompatible with javax.transaction.TransactionManager`

---

The Artemis resource Adapter allows the configuration of the Transaction Manager used by the Application Server. This is 
used for 2 things:

1. To check the status of a transaction in certain states to better handle certain events such as timeouts and avoifing sending when th etransaction is alreay aborted
2. For outgoing connections to decide whether or not to participate in a running JTA transaction when a session is created.

To build the WebSphere Transaction Manager locator go to the `transaction-manager-locator` directory and run:

```
mvn clean install
```

and then before building the resource Adapter in the next chapter add the following dependency to the pom.xml of the RAR example.

```xml
<dependency>
   <groupId>org.rhmessaging.artemis.ra.websphere.tx</groupId>
   <artifactId>transaction-manager-locator</artifactId>
   <version>1.0.0</version>
  </dependency>
```

      
## Build the Resource Adapter

Checkout the Source Code for Artemis, navigate to `examples/features/sub-modules/artemis-ra-rar` and run

```
mvn clean package
```



## Install RAR to WebSphere

before installing the RAR you ill need to create a user identity as an alias to configure the MDB Activation and J2C connector factory with.
The user and password should match a user in Artemis.  

![add user](etc/adduseralias.png)

navigate to Resource Adapters, click on Install RAR and enter the location of the Example Artemis Rar you created earler

![Install rar 1](etc/installrar1.png)

Most of the information will be entered already as it is taken from the ra.xml. It is important to check the `Isolate this resource adapter`
so it uses its own classloader, failure to do this causes classloader issues when the RA is deployed. 

![Install rar 2](etc/installrar2.png)


Now navigate to ArtemisRA > J2C connection factories and click new to add a J2C Connection Factory which is the configuration for the Outgoing connection.

![Install rar 2](etc/installrar3.png)
 
the important elements here ate the `JNDI Name` which will be the connection factory you will look up in the MDB and adding 
the user alias we created at the beginning of this chapter to the security settings. Click Apply.

This will now make `Connection pool properties` and `Advanced connection factory properties` clickable on the right, 
if needed you can update the defaults for the the connection factory and connection pool.

Now navigate to `J2C administered objects` where we will add 2 queues used by the MDB, inQueue and outQueue. firstly add 
an inQueue:

![Install rar 2](etc/installrar4.png)

again the important element here is the `JNDI Name` which is used by the MDB and also the `Administered object class` should be of type Queue. 
Click apply then navigate to `J2C administered objects custom properties` on the right. 
We need to set the `Address` to be the actual address configured in the Broker. Click Apply.

![Install rar 2](etc/installrar5.png)

Now do the same for the outQueue

Now lets add the MDB Activation by navigating to `J2C activation specifications`

![Install rar 2](etc/installrar6.png)

configure the name and JNDI name of the Activation, this is used later when deploying the MDB. . Now navigate to `J2C activation specification custom properties` 
on the right and configure the `user` and `password` to the user configured in the broker and any other activation default you require. 

---
**NOTE**

we don't set the authentication alias on the incoming connection as this is set in the Activation itself

---

you can now save to the master configuration

## Deploying the MDB

From the Applications left hand menu click `New Applciation` and choose the test mdb jar in the target directory.



# Gotchas