package com.arbiter34.yaz0;

import com.arbiter34.file.io.BinaryAccessFile;

import java.io.IOException;
import java.util.UUID;

public class Yaz0Decoder {
    public static final long MAGIC_BYTES = 0x59617A30;

    public static BinaryAccessFile decode(final BinaryAccessFile in) throws IOException {
        final BinaryAccessFile out = new BinaryAccessFile("FileCache/" + UUID.randomUUID() + "-Decompressed", "rw");

        final long magicBytes = in.readUnsignedInt();
        if (magicBytes != MAGIC_BYTES) {
            throw new IOException(String.format("Invalid magic bytes found. Expected: %s Found %s", MAGIC_BYTES, magicBytes));
        }
        final long size = in.readUnsignedInt();
        final byte[] reserved = new byte[8];

        in.read(reserved);

        byte[] header = null;
        int headerPos = 0;
        while (in.getFilePointer() < in.length() && out.getFilePointer() < size) {
            if (header == null || headerPos == 8) {
                header = new byte[1];
                in.read(header);
                headerPos = 0;
            }
            if ((header[0] & (0x80 >>> headerPos)) > 0) {
                out.write(in.read());
            } else {
                int bytes = in.readUnsignedShort();
                long backReference = out.getFilePointer() - (bytes & 0x0000000000000FFF) - 1;

                int amountToCopy = (bytes >>> 12);
                if (amountToCopy == 0) {
                    amountToCopy = in.read() + 0x12;
                } else {
                    amountToCopy += 2;
                }

                while (amountToCopy-- > 0) {
                    long currentPosition = out.getFilePointer();
                    out.seek(backReference);
                    backReference += 1;
                    int b = out.read();
                    out.seek(currentPosition);
                    out.write(b);
                }
            }
            headerPos++;
        }
        out.seek(0);
        return out;
    }
}