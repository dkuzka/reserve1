/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dk.reserve1;

import java.util.concurrent.locks.StampedLock;

/**
 *
 * @author dkuz
 */
public class ItemValue {

    String series;
    int bestBefore;
    double qty;
    double reserve;
    StampedLock lock = new StampedLock();

}
