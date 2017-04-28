/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dk.reserve1;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author dkuz
 */
public class Test {

//    private static ExecutorService mainExecutorService;
//    private static class ItemQ {
//
//        double qty;
//        double reserve;
//
//    }
//
    volatile static boolean stop;
    volatile static int nRead;
    volatile static int nWrite;

    public static void main(String[] args) throws InterruptedException {

//        ItemValue v1 = new ItemValue();
//        ItemValue v2 = new ItemValue();
//        System.out.println(v1.hashCode());
//        System.out.println(v2.hashCode());
//        System.exit(0);
//        StampedLock lockS = new StampedLock();
//        ReentrantLock lockRE = new ReentrantLock();
//        Condition c = lockRE.newCondition();
//
//        ReadWriteLock lockRW = new ReentrantReadWriteLock();
//
//        Runnable read;
//        read = () -> {
//            while (true) {
//                if (stop) {
//                    break;
//                }
//                Lock lock = lockRW.readLock();
//                if (lock.tryLock()) {
//                    try {
//                        nRead++;
////                        System.out.println("[Read] this: " + Thread.currentThread());
//                    } finally {
//                        lock.unlock();
//                    }
//                }
////                try {
////                    Thread.sleep(113);
////                } catch (InterruptedException ex) {
////                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
////                }
//            }
//        };
//        Runnable write = () -> {
//            while (true) {
//                if (stop) {
//                    break;
//                }
//                Lock lock = lockRW.writeLock();
//                try {
//                    if (lock.tryLock(1, TimeUnit.MILLISECONDS)) {
//                        try {
//                            nWrite++;
////                        System.out.println("[Write] this: " + Thread.currentThread());
//                        } finally {
//                            lock.unlock();
//                        }
//                    }
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                try {
//                    Thread.sleep(117);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        };
//        for (int i = 0; i < 100; i++) {
//            stop = false;
//            nRead = 0;
//            nWrite = 0;
//            Thread t1 = new Thread(read);
//            Thread t2 = new Thread(read);
//            Thread t3 = new Thread(write);
//            t1.start();
//            t2.start();
//            t3.start();
//
//            Thread.sleep(1000);
//            stop = true;
//            t1.join();
//            t2.join();
//            t3.join();
//            System.out.println("nRead: " + nRead + " nWrite: " + nWrite);
//        }
////        System.out.println("Reserve Level 1");
////        System.out.println("INPUT: 1) Stocks; 2) Prices; 3) Orders from customer");
//////        System.out.println("OUTPUT: 1) Tasks to WMS (pick-lists)");
////        System.out.println("OUTPUT: 1) Orders to operators; 2) Loading protocols to customers");
        PrintWriter out = new PrintWriter(System.out);
        Globals.getInstance().loadStocks(out, "/home/dkuz/export/export.csv");
        out.flush();

        ExecutorService executorService = Executors.newCachedThreadPool();
        BlockingQueue messageBus = new LinkedBlockingQueue(1);

        messageBus.put("startup");
        BusListener busListener = new BusListener(messageBus, executorService);
        //new Thread(busListener).start();
        executorService.submit(busListener);
    }

}
