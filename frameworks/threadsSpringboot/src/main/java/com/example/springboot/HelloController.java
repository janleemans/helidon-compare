package com.example.springboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

//import com.example.springboot.DoSomethingInAThread.DoSomethingInAThread2;

import jakarta.websocket.server.PathParam;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;



@RestController
public class HelloController {

    private String runThreads(boolean vThreads, int loopCount, int sleepTime) {

        System.out.println( "Using vThreads: " + vThreads);
        long start = System.currentTimeMillis();
        try{
            CountDownLatch latch = new CountDownLatch(loopCount);
            for (int n=0; n<loopCount; n++) {
                if (vThreads){
                    System.out.println("Launch virtual "+n);
                    Thread.startVirtualThread(new DoSomethingInAThread(latch,sleepTime));
                }
                else {
                    System.out.println("Launch non-v "+n);
                    Thread t = new Thread(new DoSomethingInAThread(latch,sleepTime));
                    t.start();
                }
            }
            latch.await();
            System.out.println("In Main thread after completion of "+loopCount+" threads");
        }catch(Exception err){
            err.printStackTrace();
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Run time: " + timeElapsed);
        if (vThreads) {return(",running "+loopCount+" virtual threads in " + timeElapsed + " milisecs.");}
        else {return(",running "+loopCount+" normal threads in " + timeElapsed + " milisecs.");}
    }

    public class FileReader {
        public String readFileContents(String filePath) {
            try {
                Path file = Path.of(filePath);
                System.out.println("Reading the file");
                return Files.readString(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                return "Error reading file.";
            }
        }
    }

	@GetMapping("/greet")
	public String index() {
		return "Greetings from Spring Boot!";
	}

    @GetMapping("/greet/{name}")
    @ResponseBody
	public String index2(@PathVariable String name) {
        String timeMsg = name;
        String threadType = name.substring(0,1);
        String iters = "1";
        String sleepStr = "10";
        String hyphen = "-";

        int hyphenIndex = name.indexOf(hyphen);
        if (hyphenIndex >= 0) {
            iters = name.substring(1, hyphenIndex);
            sleepStr = name.substring(hyphenIndex + hyphen.length());
        }

        int iterInt = 1;
        int sleepTime = 10;
        DoSomethingInAThread2 aa;
        FileReader myReader = new FileReader();

        try{
            iterInt = Integer.parseInt(iters);
            sleepTime = Integer.parseInt(sleepStr);
            System.out.println("ThreadType=" + threadType+", iters="+iterInt+", sleep="+sleepTime);
        }
        catch (NumberFormatException ex){
            System.out.println("No number to run, using 10");
            iterInt = 1;
            sleepTime = 10;
        }


        if (threadType.equals("V")) {
            System.out.println("Run virtual");
            timeMsg = runThreads(true, iterInt, sleepTime); }
        else if (threadType.equals("N")) {
            System.out.println("Run normal");
            timeMsg = runThreads(false, iterInt, sleepTime); }
        else if (threadType.equals("S")) {
            System.out.println("Run straight");
            timeMsg = "Run Straight";
            aa = new DoSomethingInAThread2(sleepTime);
            aa.run(); }
        else if (threadType.equals("R")) {
            timeMsg = myReader.readFileContents("src/main/resources/textfile.txt");
        }
        else {System.out.println("Unknown command, no processing");}
        return timeMsg;
    }

    class DoSomethingInAThread implements Runnable{
        CountDownLatch latch;
        int sleepTime;
        Random random = new Random();
        public DoSomethingInAThread(CountDownLatch latch, int sleepTime){
            this.latch = latch;
            this.sleepTime = sleepTime;
        }
        public void run() {
            try{
                int a = random.nextInt(10000);
                int b = random.nextInt(10000);
                double c = a * b;
                Thread.sleep(sleepTime);
                System.out.println("In Thread, " + a + " times " + b + " gives " + c);
                latch.countDown();
            }catch(Exception err){
                err.printStackTrace();
            }
        }
    }

    class DoSomethingInAThread2 implements Runnable{
        int sleepTime;
        Random random = new Random();
        public DoSomethingInAThread2(int sleepTime){
            this.sleepTime = sleepTime;
        }
        public void run() {
            try{
                int a = random.nextInt(10000);
                int b = random.nextInt(10000);
                double c = a * b;
                Thread.sleep(sleepTime);
                System.out.println("In Thread, " + a + " times " + b + " gives " + c);
            }catch(Exception err){
                err.printStackTrace();
                }
            }
        }
}
