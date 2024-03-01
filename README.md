# helidon-compare

This repo allows you to compare startup speed of the 2 flavors of Helidon (SE and MP), a springboot application and using GraalVM on the MP flavor.
As an extra, you can also use the threadsSE framework to compare execution speed of threads against virtual threads.

## Build and run

In the folder frameworks you will find 4 java projects.  Position your shell inside each of the project folders and Build and run the framework of each one.  You should have JDK21 installed, as well as mvn 3.9.6

```bash
mvn package
java -jar target/helidon-quickstart-se.jar
```

## Call the application

The basic command that can be used against all 4 environments is a GET on port 8080 with as a parameter /greet:

```bash
curl -X GET http://localhost:8080/greet
    {"message":"Hello World!"}
```

See the Helidon tutorial and Quickstart documentation for more details

## Building a Native Image

The generation of native binaries requires an installation of GraalVM 22.1.0+.

Make sure you have GraalVM locally installed:

```bash
$GRAALVM_HOME/bin/native-image --version
```

Build the native image using the native image profile:

```bash
cd frameworks/helidonMP
mvn package -Pnative-image
```

This uses the helidon-maven-plugin to perform the native compilation using your installed copy of GraalVM. It might take a while to complete.
Once it completes start the application using the native executable (no JVM!):

```bash
./target/helidon-quickstart-mp
```

Yep, it starts fast. You can exercise the application’s endpoints as before.


## Comparing startup speeds of the different options

Make sure you stop all the servers you might have launched up till now before going forward: no response should come back when calling the service:

```bash
curl -X GET http://localhost:8080/greet
    curl: (7) Failed to connect to localhost port 8080 after 0 ms: Couldn't connect to server
```

Now you can use the python script called **measure.py** to compare the startup speed of the frameworks.  Use -h to get the accepted frameworks you can pass as a parameter:

```bash
python measure.py -h
usage: Test framework start time [-h] -f {HelidonSE,HelidonMP,SpringBoot,GraalMP}

options:
  -h, --help            show this help message and exit
  -f {HelidonSE,HelidonMP,SpringBoot,GraalMP}, --framework {HelidonSE,HelidonMP,SpringBoot,GraalMP}
```

Now you can run an interation of 10 startups, using each of the options :

```bash
helidon-compare % python measure.py -f HelidonMP

helidon-compare % python measure.py -f HelidonSE

helidon-compare % python measure.py -f SpringBoot

helidon-compare % python measure.py -f GraalMP
```

You can now compare the startup times ... guess which one is fastest!

# Comparing Virtual threads with normal threads

Using the framework called threadsSE you can use a special syntax in the GET request to specify the type of thread and the number of iterations you want the server to execute.

Make sure to first start the server by positioning yourself in the threadSE folder:

```bash
cd frameworks/threadsSE
java -jar target/helidon-quickstart-se.jar
```

Now you can test the service as before:

```bash
curl -X GET http://localhost:8080/greet
```

Or you can use the following syntax to specify execution of some math operation in a thread (virtual or normal) :

```bash
curl -X GET http://localhost:8080/greet/XYYYY
```

where X can be:

    ** V: virtual thread
    ** N: normal thread

and YYYY is the number of threads to run in parallel.

Example to run 3000 virtual threads:

```bash
curl -X GET http://localhost:8080/greet/V3000
{"message":"Hello , running 3000 virtual threads in 190 milisecs.!"}
```

Instead of the message "Hello World", you get the result of the test.

Or run 3000 normal threads:

```bash
curl -X GET http://localhost:8080/greet/N3000
{"message":"Hello , running 3000 virtual threads in normal threads in 552 milisecs.!"}
```

Increasing the counter will show the 10x factor difference in speed!

# Conclusions

Make sure to open the code of the various applications and experiment with extra parameters, different thread calculations or waits, etc, and compare the results!




