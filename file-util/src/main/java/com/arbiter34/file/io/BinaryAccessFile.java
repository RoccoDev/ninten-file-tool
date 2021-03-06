package com.arbiter34.file.io;

import java.io.*;

public class BinaryAccessFile  extends RandomAccessFile {

    private final String path;

    public BinaryAccessFile(String name, String mode) throws FileNotFoundException {
        super(name, mode);
        path = name;
    }

    public BinaryAccessFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
        path = file.getPath();
    }

    public String getPath() {
        return path;
    }

    public void writeUnsignedInt(long v) throws IOException {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)(v >>> 24);
        bytes[1] = (byte)(v >>> 16);
        bytes[2] = (byte)(v >>> 8);
        bytes[3] = (byte)(v);
        write(bytes);
    }

    public long readUnsignedInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        long ret = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
        return (ret << 32) >>> 32;
    }

    public void clean() {
        try {
            close();
            new File(getPath()).delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
