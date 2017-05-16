package com.ericbandiero.ratsandmice.interfaces;

import java.util.List;
import java.util.SortedMap;

import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 3/28/2016.
 */
public interface IMarkable {
    public List<Integer> markData(List<Inspections> listDataHeader,SortedMap<Inspections, List<Inspections>> sortedMapChildren);
}
