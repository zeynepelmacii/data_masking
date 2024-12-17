package com.thy.masking.backend;

import java.util.ArrayList;
import java.util.List;

public abstract  class BaseMasker extends DatReader {

    protected List<Metric> metricList = new ArrayList<>();

    protected List<String> synchronizers =  new ArrayList<>();
    public BaseMasker(String folderPath) {
        super(folderPath);
    }


    protected void setSynchronizers(String synch1, String synch2, String synch3, String synch4) {
        synchronizers.clear();
        synchronizers.add(synch1);
        synchronizers.add(synch2);
        synchronizers.add(synch3);
        synchronizers.add(synch4);
    }



    public abstract void mask(List<Metric> metrics, String suffix);


}
