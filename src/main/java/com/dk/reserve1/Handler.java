/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dk.reserve1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dkuz
 */
public class Handler implements Runnable {

    private final Socket socket;
    private final Globals globals;
    //private BlockingQueue messageBus;

//        public void setSocket(Socket s) {
//            this.socket = s;
//        }
//
    private Handler() {
        this.socket = null;
        this.globals = null;
    }

    public Handler(Socket socket, Globals globals) {
        this.socket = socket;
        this.globals = globals;
    }

    @Override

    public void run() {
        System.out.println("this: " + this + " socket: " + socket);
        try (PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (true) {
                String inputLine = clientIn.readLine();
                if (inputLine.isEmpty()) {
                    continue;
                }
                boolean breakWhile = false;
                String[] words = inputLine.split(" ");
//                    switch (inputLine) {
                switch (words[0]) {
                    case "exit":
                    case "quit":
                        breakWhile = true;
                        break;
                    case "startup":
                        globals.messageBus.put("startup");
                        break;
                    case "shutdown":
                        globals.messageBus.put("shutdown");
                        breakWhile = true;
                        break;
                    case "help":
                        clientOut.println("Available commands are:");
                        clientOut.println("  exit");
                        clientOut.println("  quit");
                        clientOut.println("  startup");
                        clientOut.println("  shutdown");
                        clientOut.println("  load_stocks");
                        clientOut.println();
                        break;
                    case "load_stocks":
                        if (words.length == 2) {
                            clientOut.println("Load stocks from: " + words[1]);
                            globals.loadStocks(clientOut, words[1]);
                        } else {
                            clientOut.println("load_stocks(): Bad arguments");
                        }
                        break;
                    case "list_keys":
                        globals.listKeys(clientOut);
                        break;
                    case "reserve":
                        if (words.length == 4) {
                            try {
                                globals.reserveStocks(clientOut, Integer.parseInt(words[1]), Integer.parseInt(words[2]), Double.parseDouble(words[3]));
                            } catch (Exception ex) {
                                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            clientOut.println("reserve(): Bad arguments");
                        }
                        break;
                    default:
                        clientOut.println("ECHO: " + inputLine);
                        System.out.println(new Date().toString() + " ip: " + socket.getInetAddress().getHostAddress() + " socket: " + socket + " msg: " + inputLine);
                }
                if (breakWhile) {
                    break;
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//        public void setBus(BlockingQueue messageBus) {
//            this.messageBus = messageBus;
//        }
//        private class ItemQ {
//        }
}
