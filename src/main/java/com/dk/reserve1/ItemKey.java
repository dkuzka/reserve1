/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dk.reserve1;

/**
 *
 * @author dkuz
 */
public class ItemKey {

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

    @Override
    public String toString() {
        return "ItemKey{" + "productId=" + productId + ", mfrId=" + mfrId + '}';
    }
}
