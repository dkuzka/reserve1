/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dk.reserve1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dkuz
 */
public class RServerL1 implements Runnable {

    //private final BlockingQueue messageBus;
    private final ExecutorService executorService;
    private final ServerSocket serverSocket;
    private final Globals globals;

    public RServerL1(BlockingQueue messageBus, ExecutorService executorService, ServerSocket serverSocket) {
        //this.messageBus = q;
        this.executorService = executorService;
        this.serverSocket = serverSocket;
//        this.globals = new Globals();
        this.globals = Globals.getInstance();
        globals.messageBus = messageBus;
    }

    @Override
    public void run() {
        try {
            //ServerSocket serverSocket = new ServerSocket(7722);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Handler h = new Handler(clientSocket, globals);
                //h.setSocket(clientSocket);
                //h.setBus(messageBus);
                executorService.submit(h);
            }
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
