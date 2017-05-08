package com.ericbandiero.ratsandmice.betas;

import android.util.Log;

import com.ericbandiero.ratsandmice.AppConstant;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ${"Eric Bandiero"} on 3/18/2016.
 */
public class MyTestArray extends ArrayList {
    /**
     * Constructs a new instance of {@code ArrayList} with the specified
     * initial capacity.
     *
     * @param capacity the initial capacity of this {@code ArrayList}.
     */
    public MyTestArray(int capacity) {
        super(capacity);
    }

    /**
     * Constructs a new {@code ArrayList} instance with zero initial capacity.
     */
    public MyTestArray() {
    }

    /**
     * Constructs a new instance of {@code ArrayList} containing the elements of
     * the specified collection.
     *
     * @param collection the collection of elements to add.
     */
    public MyTestArray(Collection collection) {
        super(collection);
    }

    /**
     * Removes all elements from this {@code ArrayList}, leaving it empty.
     *
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        super.clear();
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "Cleared out array");
    }
}
