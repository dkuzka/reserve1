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
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dkuz
 */
public class Globals {

    private static class OnDemanHolder{
        private static final Globals instance = new Globals();
    }
    static Globals getInstance() {
        return OnDemanHolder.instance;
    }

    public final ReadWriteLock globalLockRW = new ReentrantReadWriteLock();
    private HashMap<ItemKey, LinkedList<ItemValue>> stocks;
    public BlockingQueue messageBus;
    //public final ArrayBlockingQueue<ItemValue> lockedValues;
//    public final ConcurrentHashMap<ItemValue, Object> lockedValues;

    public Globals() {
//        lockedValues = new ArrayBlockingQueue<>(16);
//        lockedValues = new ConcurrentHashMap<>();
    }

    public void loadStocks(PrintWriter out, String filename) {
        int n = 0;
        long start = new Date().getTime();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String header = reader.readLine();
            //System.out.println("Header: " + header);
            HashMap<ItemKey, LinkedList<ItemValue>> mapItemStock = new HashMap(1024);
            while (reader.ready()) {
                String line = reader.readLine();
                String[] w = line.split(",");
                try {

                    int productId = Integer.parseInt(w[0]);
                    int mfrId = Integer.parseInt(w[1]);
                    String series = w[3].replace("\"", "");
                    int bestBefore = Integer.parseInt(w[4].replace("\"", ""));
                    double qty = Double.parseDouble(w[2]);
                    double res = 0d;

                    ItemKey key = new ItemKey();
                    key.productId = productId;
                    key.mfrId = mfrId;

                    ItemValue item;
                    LinkedList<ItemValue> newStocks = mapItemStock.get(key);
                    if (newStocks == null) {
                        newStocks = new LinkedList<>();
                        item = new ItemValue();
                        item.series = series;
                        item.bestBefore = bestBefore;
                        item.qty = qty;
                        item.reserve = res;
                        newStocks.add(item);
                        mapItemStock.put(key, newStocks);
                    } else {
                        Optional<ItemValue> opt = newStocks.stream().filter(e -> e.series.equals(series) && e.bestBefore == bestBefore).findFirst();
                        if (opt.isPresent()) {
                            item = opt.get();
                            item.qty += qty;
                        } else {
                            item = new ItemValue();
                            item.series = series;
                            item.bestBefore = bestBefore;
                            item.qty = qty;
                            item.reserve = res;
                            newStocks.add(item);
                        }
                    }
                    n++;
                } catch (NumberFormatException ex) {
//                        Logger.getLogger(Test.class.getName()).log(Level.SEVERE, "Problem line: " + line, ex);
                    //Logger.getLogger(Test.class.getName()).log(Level.SEVERE, "Problem line: " + line);
                    out.println("Problem line: " + line);
                }
            }
            //activateNewStocks(mapItemStock);
            //globals.messageBus.put(mapItemStock);
            activateNewStocks(mapItemStock);
//            System.out.println("loaded number: " + n);
            out.println("keys number: " + mapItemStock.keySet().size());
            out.println("total values number: " + mapItemStock.keySet().stream().map((k) -> mapItemStock.get(k).size()).reduce(0, Integer::sum));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }

//        System.out.println("Loaded in " + (new Date().getTime() - start) + " ms");
        out.println("Loaded " + n + " lines in " + (new Date().getTime() - start) + " ms");
    }

    private void activateNewStocks(HashMap<ItemKey, LinkedList<ItemValue>> mapItemStock) {
        Lock lock = globalLockRW.writeLock();
        while (true) {
            try {
                if (lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                    try {
                        stocks = mapItemStock;
                    } finally {
                        lock.unlock();
                    }
                    break;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void reserveStocks(PrintWriter out, int productId, int mfrId, double qty) {

        if (qty <= 0d) {
            out.println("Negative or zero amount");
            return;
        }

        Lock lock = globalLockRW.readLock();
        while (true) {
            try {
                if (lock.tryLock(10, TimeUnit.MILLISECONDS)) {
//                    try {
                    ItemKey key = new ItemKey();
                    key.productId = productId;
                    key.mfrId = mfrId;
                    LinkedList<ItemValue> stocksList = stocks.get(key);
                    double remainedQty = qty;
//                    double currentQty;
//                    double currentRes;
                    double blockQty;
                    for (ItemValue v : stocksList) {
                        long stamp = v.lock.readLock();
                        try {
//                            currentQty = v.qty;
//                            currentRes = v.reserve;
                            if (v.qty - v.reserve > 0d) {
                                stamp = v.lock.tryConvertToWriteLock(stamp);
                                if (stamp == 0L) {
                                    stamp = v.lock.writeLock();
                                }
                                if (v.qty - v.reserve >= remainedQty) {
                                    blockQty = remainedQty;
                                } else {
                                    blockQty = v.qty - v.reserve;
                                }
                                v.reserve += blockQty;
                                remainedQty -= blockQty;
                                out.println(v);
                                out.println("qty=" + v.qty);
                                out.println("res=" + v.reserve);
                                out.println("rem=" + remainedQty);
                                Thread.sleep(10 * 1000);
                            }
                        } finally {
                            v.lock.unlock(stamp);
                            out.println("unlock");
                        }
                        if (remainedQty <= 0d) {
                            break;
                        }
                    }
                    if (remainedQty > 0d) {
                        out.println("remaine to reserve = " + remainedQty);
                    }

                    break;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Globals.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                lock.unlock();
            }
        }
    }

    public void listKeys(PrintWriter out) {
        out.println("list_keys()");
        Lock lock = globalLockRW.readLock();
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                if (stocks == null) {
                    out.println("stocks not loaded");
                } else {
                    stocks.keySet().forEach((key) -> {
                        out.println(key);
                    });
                }
            } else {
                out.println("timeout");

            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Globals.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            lock.unlock();
        }
    }

}
