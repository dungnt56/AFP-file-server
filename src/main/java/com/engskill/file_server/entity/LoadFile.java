package com.engskill.file_server.entity;

import java.io.InputStream;

public class LoadFile {
    private final byte[] inputStream;
    private final String filename;
    private final String fileType;

    public LoadFile(byte[] inputStream, String filename, String fileType) {
        this.inputStream = inputStream;
        this.filename = filename;
        this.fileType = fileType;
    }

    public byte[] getInputStream() {
        return inputStream;
    }

    public String getFilename() {
        return filename;
    }

    public String getFileType() {
        return fileType;
    }
}
