/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dk.reserve1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dkuz
 */
public class Test {

//    private static ExecutorService mainExecutorService;
    private static class Handler implements Runnable {

        private Socket socket;
        private BlockingQueue messageBus;

        public void setSocket(Socket s) {
            this.socket = s;
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
                            messageBus.put("startup");
                            break;
                        case "shutdown":
                            messageBus.put("shutdown");
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
                                clientOut.println("Number of records loaded: " + loadStocks(words[1]));
                            } else {
                                clientOut.println("Load stocks: Bad arguments");
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

        public void setBus(BlockingQueue messageBus) {
            this.messageBus = messageBus;
        }

//        private class ItemQ {
//        }
        private int loadStocks(String filename) {
            int n = 0;
            long start = new Date().getTime();
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String header = reader.readLine();
                System.out.println("Header: " + header);
                HashMap<ItemKey, ItemKValue> mapMain = new HashMap(1024);
                HashMap<ItemKValue, ItemQ> mapQty = new HashMap(1024);
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] w = line.split(",");
                    //System.out.println(line);
                    //System.out.println("product_id=" + w[0] + " mfr_id=" + w[1] + " qty=" + w[2] + " series=" + w[3].replace("\"", "") + " best_before=" + w[4].replace("\"", ""));
                    try {
                        ItemKey key = new ItemKey();
                        key.productId = Integer.parseInt(w[0]);
                        key.mfrId = Integer.parseInt(w[1]);
                        ItemKValue kvalue = new ItemKValue();
                        kvalue.mainKey = key;
                        kvalue.series = w[3].replace("\"", "");
                        kvalue.bestBefore = Integer.parseInt(w[4].replace("\"", ""));
                        ItemQ value = new ItemQ();
                        value.qty = Double.parseDouble(w[2]);
                        value.reserve = 0d;
                        mapMain.put(key, kvalue);
                        mapQty.put(kvalue, value);
                        n++;
                    } catch (NumberFormatException ex) {
                        Logger.getLogger(Test.class.getName()).log(Level.SEVERE, "Problem line: " + line, ex);
                    }
                }
                System.out.println("key numbers: " + mapMain.keySet().size());
                System.out.println("key Q numbers: " + mapQty.keySet().size());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Loaded in " + (new Date().getTime() - start) + " ms");
            return n;
        }
    }

    private static class RServerL1 implements Runnable {

        private final BlockingQueue messageBus;
        private final ExecutorService executorService;
        private final ServerSocket serverSocket;

        public RServerL1(BlockingQueue q, ExecutorService executorService, ServerSocket serverSocket) {
            this.messageBus = q;
            this.executorService = executorService;
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            try {
                //ServerSocket serverSocket = new ServerSocket(7722);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    Handler h = new Handler();
                    h.setSocket(clientSocket);
                    h.setBus(messageBus);
                    executorService.submit(h);
                }
            } catch (IOException ex) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class ItemKey {

        int productId;
        int mfrId;

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + this.productId;
            hash = 29 * hash + this.mfrId;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ItemKey other = (ItemKey) obj;
            if (this.productId != other.productId) {
                return false;
            }
            if (this.mfrId != other.mfrId) {
                return false;
            }
            return true;
        }
    }

    private static class ItemKValue {

        ItemKey mainKey;
        String series;
        int bestBefore;
//        double qty;
//        double reserve;

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 73 * hash + Objects.hashCode(this.mainKey);
            hash = 73 * hash + Objects.hashCode(this.series);
            hash = 73 * hash + this.bestBefore;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ItemKValue other = (ItemKValue) obj;
            if (this.bestBefore != other.bestBefore) {
                return false;
            }
            if (!Objects.equals(this.series, other.series)) {
                return false;
            }
            if (!Objects.equals(this.mainKey, other.mainKey)) {
                return false;
            }
            return true;
        }

    }

    private static class ItemQ {

        double qty;
        double reserve;

    }

    private static class BusListener implements Runnable {

        private final BlockingQueue messageBus;
        private final ExecutorService executorService;
        private /*volatile*/ RServerL1 localRServerL1;
        private /*volatile*/ ServerSocket serverSocket;
        private ConcurrentHashMap<ItemKey, ItemKValue> stocks;
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

    public static void main(String[] args) throws InterruptedException {

//        System.out.println("Reserve Level 1");
//        System.out.println("INPUT: 1) Stocks; 2) Prices; 3) Orders from customer");
////        System.out.println("OUTPUT: 1) Tasks to WMS (pick-lists)");
//        System.out.println("OUTPUT: 1) Orders to operators; 2) Loading protocols to customers");
        ExecutorService executorService = Executors.newCachedThreadPool();
        BlockingQueue messageBus = new LinkedBlockingQueue(1);

        messageBus.put("startup");
        BusListener busListener = new BusListener(messageBus, executorService);
        //new Thread(busListener).start();
        executorService.submit(busListener);
    }

}
