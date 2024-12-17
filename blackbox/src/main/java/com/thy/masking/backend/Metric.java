package com.thy.masking.backend;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Metric {
    String metric;
    int wordNo;
    int superFrameNo;
    int subFrameNo;
    int leastSigBit;
    int mostSigBit;
    boolean keep;

    public String getMetric() {
        return this.metric;
    }


    public Metric(
            String metric,
            int wordNo,
            int superFrameNo,
            int subFrameNo,
            int leastSigBit,
            int mostSigBit
    ) {
        this.metric = metric;
        this.wordNo = wordNo;
        this.superFrameNo = superFrameNo;
        this.subFrameNo = subFrameNo;
        this.leastSigBit = leastSigBit;
        this.mostSigBit = mostSigBit;
        this.keep = false;
    }


    public static List<Metric> loadFromMaskConfig(String maskcfgfilepath) {
        List<Metric> list = new ArrayList<>();
        try {
            String content = Files.readString(Path.of(maskcfgfilepath));
            String[] lines = content.split("\n|\r");
            for (String line : lines) {
                if (line ==null || line.strip().length() == 0) {
                    continue;
                }
                String[] cols = line.split(";");
                int c = 0;
                String metric  = cols[c++];
                int superFrameNo  = Integer.parseInt(cols[c++]);
                int subFrameNo  = Integer.parseInt(cols[c++]);
                int wordNo  = Integer.parseInt(cols[c++]);
                int leastSigBit  = Integer.parseInt(cols[c++]);
                int mostSigBit  = Integer.parseInt(cols[c++]);


                Metric metricForMasking = new Metric(
                        metric,
                        wordNo,
                        superFrameNo,
                        subFrameNo,
                        leastSigBit,
                        mostSigBit
                );

                list.add(metricForMasking);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return list;
    }


    public boolean isKeep() {
        return keep;
    }

    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "metric='" + metric + '\'' +
                ", wordNo=" + wordNo +
                ", superFrameNo=" + superFrameNo +
                ", subFrameNo=" + subFrameNo +
                ", leastSigBit=" + leastSigBit +
                ", mostSigBit=" + mostSigBit +
                '}';
    }

}
