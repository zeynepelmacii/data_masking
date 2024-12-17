package com.thy.masking.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DatReader {

    private String path;
    File file;
    InputStream inputStream;

    public DatReader(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }


    public static final String RAW_DATA_FILE_NAME="raw.dat";

    public void open() {
        String filePath = this.path+File.separator+RAW_DATA_FILE_NAME;
        file=new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException(String.format("File not found %s", filePath));
        }

        try {
            inputStream = new FileInputStream(path+File.separator+DatReader.RAW_DATA_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected  byte[] readAllBytes() {
        try {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void closeFile() {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
