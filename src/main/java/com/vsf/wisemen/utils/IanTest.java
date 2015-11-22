package com.vsf.wisemen.utils;

public class IanTest {

    public static void main(String[] args) {
        //final submission variables
        final Thread thisThread = Thread.currentThread();
        final int timeToRun = 2000; // 52 seconds;

        //Programming competition timer
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(timeToRun);

                    //TODO chromosome to clg
                    System.out.println("------------------------------------------------------------------------------");
                    //TODO print clg file to disk

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //thisThread.interrupt();
                thisThread.stop();
            }
        }).start();

        //while (!Thread.interrupted()) continue generating
        while (!thisThread.isInterrupted())
        {
            System.out.println("printing "+thisThread.isInterrupted());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
