package io.helidon.examples.quickstart.se;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import io.helidon.config.Config;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

import java.util.concurrent.*;
import java.util.Random;


class DoSomethingInAThread implements Runnable{
    CountDownLatch latch;
    Random random = new Random();
    public DoSomethingInAThread(CountDownLatch latch){
        this.latch = latch;
    }
    public void run() {
        try{
            int a = random.nextInt(10000);
            int b = random.nextInt(10000);
            double c = a * b;
            System.out.println("In Thread, multiply " + a + " with " + b + " gives " + c);
            Thread.sleep(100);
            latch.countDown();
        }catch(Exception err){
            err.printStackTrace();
        }
    }
}

/**
 * A simple service to greet you. Examples:
 * <p>
 * Get default greeting message:
 * {@code curl -X GET http://localhost:8080/greet}
 * <p>
 * Get greeting message for Joe:
 * {@code curl -X GET http://localhost:8080/greet/Joe}
 * <p>
 * Change greeting
 * {@code curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Howdy"}' http://localhost:8080/greet/greeting}
 * <p>
 * The message is returned as a JSON object
 */
class GreetService implements HttpService {


    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    /**
     * The config value for the key {@code greeting}.
     */
    private final AtomicReference<String> greeting = new AtomicReference<>();

    GreetService() {
        this(Config.global().get("app"));
    }

    GreetService(Config appConfig) {
        greeting.set(appConfig.get("greeting").asString().orElse("Ciao"));
    }

    /**
     * A service registers itself by updating the routing rules.
     *
     * @param rules the routing rules.
     */
    @Override
    public void routing(HttpRules rules) {
        rules
                .get("/", this::getDefaultMessageHandler)
                .get("/{name}", this::getMessageHandler)
                .put("/greeting", this::updateGreetingHandler);
    }

    /**
     * Return a worldly greeting message.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void getDefaultMessageHandler(ServerRequest request,
                                          ServerResponse response) {
        String timeMsg = "World";
        //timeMsg = runThreads(false, 1000);
        sendResponse(response, timeMsg);
    }

    private String runThreads(boolean vThreads, int loopcount) {

        System.out.println( "Using vThreads: " + vThreads);
        long start = System.currentTimeMillis();
        try{
            CountDownLatch latch = new CountDownLatch(loopcount);
            for (int n=0; n<loopcount; n++) {
                if (vThreads){
                    System.out.println("Launch virtual "+n);
                    Thread.startVirtualThread(new DoSomethingInAThread(latch));
                }
                else {
                    System.out.println("Launch non-v "+n);
                    Thread t = new Thread(new DoSomethingInAThread(latch));
                    t.start();
                }
            }
            latch.await();
            System.out.println("In Main thread after completion of "+loopcount+" threads");
        }catch(Exception err){
            err.printStackTrace();
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Run time: " + timeElapsed);
        if (vThreads) {return(",running "+loopcount+" virtual threads in " + timeElapsed + " milisecs.");}
        else {return(",running "+loopcount+" normal threads in " + timeElapsed + " milisecs.");}
    }

    /**
     * Return a greeting message using the name that was provided.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void getMessageHandler(ServerRequest request,
                                   ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        String timeMsg = name;
        String threadType = name.substring(0,1);
        String iters = name.substring(1);
        int aa = 0;
        try{
            aa = Integer.parseInt(iters);
            System.out.println("ThreadType=" + threadType+", iters="+aa);
        }
        catch (NumberFormatException ex){
            System.out.println("No number to run, using 10");
            aa = 10;
        }

        if (threadType.equals("V")) {
            System.out.println("Run virtual");
            timeMsg = runThreads(true, aa); }
        if (threadType.equals("N")) {
            System.out.println("Run normal");
            timeMsg = runThreads(false, aa); }
        sendResponse(response, timeMsg);
    }

    private void sendResponse(ServerResponse response, String name) {
        String msg = String.format("%s %s!", greeting.get(), name);

        JsonObject returnObject = JSON.createObjectBuilder()
                .add("message", msg)
                .build();
        response.send(returnObject);
    }

    private void updateGreetingFromJson(JsonObject jo, ServerResponse response) {

        if (!jo.containsKey("greeting")) {
            JsonObject jsonErrorObject = JSON.createObjectBuilder()
                    .add("error", "No greeting provided")
                    .build();
            response.status(Status.BAD_REQUEST_400)
                    .send(jsonErrorObject);
            return;
        }

        greeting.set(jo.getString("greeting"));
        response.status(Status.NO_CONTENT_204).send();
    }

    /**
     * Set the greeting to use in future messages.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void updateGreetingHandler(ServerRequest request,
                                       ServerResponse response) {
        updateGreetingFromJson(request.content().as(JsonObject.class), response);
    }

}
