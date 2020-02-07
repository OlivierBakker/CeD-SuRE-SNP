package nl.umcg.suresnp.pipeline.utils;

import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;

import java.util.Collection;
import java.util.Map;

public class BedUtils {


    public static Collection<BedRecord>[] intersectSortedBedRecords(Collection<BedRecord> setOne, Collection<BedRecord> setTwo) {

        Collection<BedRecord> a;
        Collection<BedRecord> b;

        if (setOne.size() >= setTwo.size()) {
            a = setOne;
            b = setTwo;
        } else {
            a = setTwo;
            b = setTwo;
        }


        for (BedRecord curA : a) {

        }

        return null;

    }



}
