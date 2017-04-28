/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dk.reserve1;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dkuz
 */
public class BusListener implements Runnable {

    private final BlockingQueue messageBus;
    private final ExecutorService executorService;

    private /*volatile*/ RServerL1 localRServerL1;
    private /*volatile*/ ServerSocket serverSocket;
//        private ConcurrentHashMap<ItemKey, ItemValue> stocks;
    //private volatile Future f;

    public BusListener(BlockingQueue q, ExecutorService executorService) {
        this.messageBus = q;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(7722);
            while (true) {
                consume(messageBus.take());
            }
        } catch (InterruptedException ex) {
            if (!"end".equals(ex.getMessage())) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("END");
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executorService.shutdown();
    }

    void consume(Object x) throws InterruptedException {
        if (x == null) {
            return;
        }
        /*if (x instanceof HashMap) {
                activateNewStocks((HashMap<ItemKey, LinkedList<ItemValue>>) x);
            } else*/
        if (x instanceof String) {
            switch ((String) x) {
                case "startup":
                    System.out.println("BUS: STARTUP");
                    if (localRServerL1 == null) {
                        localRServerL1 = new RServerL1(messageBus, executorService, serverSocket);
                        /*f =*/ executorService.submit(localRServerL1);
                    } else {
                        System.out.println("BUS: already started");
                    }
                    break;
                case "shutdown":
                    System.out.println("BUS: SHUTDOWN");
                    if (localRServerL1 != null) {
//                        f.cancel(true);
//                        executorService.shutdown();
                        throw new InterruptedException("end");
                    } else {
                        System.out.println("BUS: already shutted down");
                    }
                    break;
            }
        }
    }

}
