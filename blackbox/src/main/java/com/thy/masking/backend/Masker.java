package com.thy.masking.backend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Masker extends BaseMasker {

    public static final String DEFAULT_SYNCH1 = "4702";
    public static final String DEFAULT_SYNCH2 = "b805";
    public static final String DEFAULT_SYNCH3 = "470a";
    public static final String DEFAULT_SYNCH4 = "b80d";
    public Masker(String folderPath) {
        super(folderPath);
        setSynchronizers(DEFAULT_SYNCH1, DEFAULT_SYNCH2, DEFAULT_SYNCH3, DEFAULT_SYNCH4);
    }




    @Override
    public void mask(List<Metric> metrics, String maskedFolder) {

        this.open();
        this.metricList.clear();
        this.metricList.addAll(metrics);

        byte[] all = readAllBytes();
        closeFile();
        int len = all.length;
        System.out.println(String.format("bytes read : %d", len));

        int startLocationtoAnalyze = -1;

        for (int i=0; i<len; i+=2) {
            byte[] wrd = new byte[2];
            System.arraycopy(all, i, wrd , 0 , 2);
            String hexWrd = String.format("%02x%02x", wrd[0], wrd[1]);
            if (hexWrd.equals(synchronizers.get(0))) {
                startLocationtoAnalyze = i;
                break;
            }
        }

        if (startLocationtoAnalyze == -1) {
            throw  new RuntimeException("No synchronizing word found. ");
        }

        int wordCountPerSubFrame = 512;
        int subFrameCountPerFrame = synchronizers.size();
        int bytesPerWord = 2;
        int readLen = subFrameCountPerFrame * wordCountPerSubFrame * bytesPerWord;
        int stataAllWords = 0;
        int statMaskedWords = 0;
        int statKeptWords = 0;

        int superFrameNo = 0;

        for (int i=startLocationtoAnalyze; i<len ; i+= readLen) {

            String hexWrd = String.format("%02x%02x", all[i], all[i+1]);
            int subFrameNo = synchronizers.indexOf(hexWrd);

            if (subFrameNo == -1) {
                break;
            }

            superFrameNo++;

            for (int sf = 0; sf<subFrameCountPerFrame; sf++) {
                int lenOfASubFrame = wordCountPerSubFrame * bytesPerWord;
                byte[] bytesOfSubFrameOriginal = new byte[lenOfASubFrame];
                byte[] bytesOfSubFrameMasked = new byte[lenOfASubFrame];
                int framePosToProcess =  i + sf * lenOfASubFrame;

                if (framePosToProcess + lenOfASubFrame > len-1) {
                    break;
                }

                System.arraycopy(all, framePosToProcess, bytesOfSubFrameOriginal , 0 , lenOfASubFrame);
                //copy first 2 bytes to keep synch word
                System.arraycopy(all, framePosToProcess, bytesOfSubFrameMasked , 0 , 2);

                int finalSubFrameNo = sf +1;

                for (int w = 0; w < wordCountPerSubFrame; w++) {
                    int finalW = w + 1;
                    stataAllWords++;

                    int finalSuperFrameNo = superFrameNo;
                    List<Metric> listOfMetricsToKeep = metrics.stream().filter(
                            m->     m.wordNo == finalW
                                    && (m.subFrameNo == 0 || (finalSubFrameNo) == m.subFrameNo)
                                    && (m.superFrameNo == 0 || (finalSuperFrameNo) == m.superFrameNo)
                    ).collect(Collectors.toList());

                    if (listOfMetricsToKeep.size()==0) {
                        statMaskedWords++;
                        continue;
                    }

                    statKeptWords++;


                    byte[] wordOriginal = new byte[bytesPerWord];
                    byte[] wordMasked = new byte[bytesPerWord];

                    System.arraycopy(bytesOfSubFrameOriginal, w * bytesPerWord, wordOriginal , 0 , bytesPerWord);

                    for (int b=0;b<bytesPerWord*8;b++) {
                        final int bitNo = b + 1;

                        //Whitelist
                        boolean toKeep = listOfMetricsToKeep.stream().anyMatch(m->bitNo>=m.leastSigBit && bitNo<=m.mostSigBit);
                        if (!toKeep) {
                            continue;
                        }

                        boolean currVal = Utils.getBitValue(wordOriginal, b);
                        Utils.setBit(wordMasked, b, currVal);

                    }


                    System.arraycopy(wordMasked, 0, bytesOfSubFrameMasked , w * bytesPerWord, bytesPerWord);


                }

                System.arraycopy(bytesOfSubFrameMasked, 0, all , framePosToProcess , lenOfASubFrame);



            }
        }



        System.out.println("---------------------------------------------------");
        System.out.println(String.format("all Words    ....: %d", stataAllWords));
        System.out.println(String.format("masked Words ....: %d", statMaskedWords));
        System.out.println(String.format("Kept Words   ....: %d", statKeptWords));
        System.out.println("---------------------------------------------------");

        FileOutputStream stream = null;
        try {


            Path pathMaskedFolder=Path.of(maskedFolder);

            if (Files.exists(pathMaskedFolder)) {
                clearDirectory(maskedFolder);
            }

            Files.createDirectories(Path.of(maskedFolder));
            copyDirectory(getPath(),maskedFolder);


            String maskedFilePath = maskedFolder+File.separator+DatReader.RAW_DATA_FILE_NAME;
            System.out.println(String.format("writing masked file : %s", maskedFilePath));


            stream = new FileOutputStream(maskedFilePath);
            stream.write(all);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {stream.close(); } catch (Exception e) { e.printStackTrace(); }
        }



    }

    public void clearDirectory(String fullPath) throws IOException {
        Path dir = Paths.get(fullPath); //path to the directory
        Files.walk(dir) // Traverse the file tree in depth-first order
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);  //delete each file or directory
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }




    public  void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
            throws IOException {
        File[] files = new File(sourceDirectoryLocation).listFiles();
        for (File file : files) {
            if (file.isDirectory()) continue;
            Files.copy(file.toPath(), Path.of(destinationDirectoryLocation+File.separator+file.getName()));
        }

    }





}
