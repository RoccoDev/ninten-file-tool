package com.arbiter34.yaz0;

import com.arbiter34.byml.util.Pair;
import com.arbiter34.file.io.BinaryAccessFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Yaz0Encoder {
    private static final long HEADER_SIZE = 0x10;
    private static final int MAX_SEARCH_SIZE = 0x111;
    private static final int MAX_BACK_REFERENCE = 0xFFF;
    private static final int REQUIRES_THIRD_BYTE = 0x11;

    public static BinaryAccessFile encode(final BinaryAccessFile in, final String outPath) throws IOException {
        final BinaryAccessFile out = new BinaryAccessFile(outPath, "rw");
        out.writeUnsignedInt(Yaz0Decoder.MAGIC_BYTES);
        out.writeUnsignedInt(in.length());
        out.write(new byte[8]);

        int headerPos = 0;
        long headerPointer = out.getFilePointer();
        short header = 0;
        out.seek(headerPointer + 1);
        while (in.getFilePointer() < in.length()) {
            if (headerPos == 8) {
                // mark current end of out
                long outMark = out.getFilePointer();
                // seek to current header
                out.seek(headerPointer);
                // write header
                out.writeByte(header);
                // seek to current end of out
                out.seek(outMark);

                // reset header and header pos and mark location
                header = 0;
                headerPointer = out.getFilePointer();
                headerPos = 0;
                out.seek(headerPointer + 1);
            }
            long searchStart = in.getFilePointer();
            byte[] searchBytes = new byte[MAX_SEARCH_SIZE];

            // Read max search chunk then seek back
            in.read(searchBytes);
            in.seek(searchStart);

            // Attempt to find largest common subarray between search chunk and out.getFilePointer() - MAX_BACK_REFERENCE
            Pair<Long, Integer> offsetBestPair = offsetOf(out, searchBytes);
            // Don't back reference if it'll take more bytes to represent than we're saving
            if (offsetBestPair.getRight() < 3) {
                    out.write(in.read());
                    header |= (0x80 >>> headerPos);
                    headerPos++;
                continue;
            }

            int best = offsetBestPair.getRight();
            final long offset = offsetBestPair.getLeft();
            if (best > 10) {
                best = best;
            }
            // We found bytes -> seek input over matched bytes
            in.seek(searchStart + best);

            int size = best;
            boolean found = false;
            int count = 0;
            while (size > 0) {
                byte[] next = new byte[size];
                count = 0;
                while (best + (count * next.length) < MAX_SEARCH_SIZE) {
                    in.read(next);
                    assert(best - (best - size) == next.length);
                    if (Arrays.equals(Arrays.copyOfRange(searchBytes, 0, best - (best - size)), next)) {
                        found = true;
                        count++;
                        continue;
                    }
                    break;
                }
                if (found) {
                    break;
                }
                size--;
            }
            if (size > 15) {
                size = size;
            }
            int backReference = (int)(out.getFilePointer() - offset + 2 - 1);
            best += size * count;
            in.seek(searchStart + best);
            headerPos++;    // 0 bit in header for reference byte
            if (best < REQUIRES_THIRD_BYTE) {
                int toWrite = ((best - 0x02) << 12) | (backReference & 0x0000000000000FFF);
                out.writeShort(toWrite);
            } else {
                out.writeShort((backReference & 0x0000000000000FFF));
                out.write(best);
            }
        }

        return out;
    }

    private static Pair<Long, Integer> offsetOf(final BinaryAccessFile file, byte[] searchBytes) throws IOException {
        long mark = file.getFilePointer();
        int backSearchSize = MAX_BACK_REFERENCE;
        while (file.getFilePointer() - backSearchSize < HEADER_SIZE) {
            backSearchSize--;
            if (backSearchSize == 0) {
                return Pair.of(-1L, -1);
            }
        }

        if (backSearchSize == MAX_BACK_REFERENCE) {
            backSearchSize = backSearchSize;
        }

        byte[] buffer = new byte[backSearchSize];
        // Seek back backSearchSize then read backSearchSize many bytes (restoring position)
        file.seek(file.getFilePointer() - buffer.length);
        file.read(buffer);

        // Find longest subarray offset and size
        Pair<Long, Integer> longest = lcSubarray(buffer, searchBytes);
        if (longest.getLeft() != -1) {
            // Index found in buffer is how many bytes into the search chunk we skip
            long offset = file.getFilePointer() - buffer.length + longest.getLeft();
            longest.setLeft(offset);
        }
        file.seek(mark);
        return longest;
    }

    private static Pair<Long, Integer> lcSubarray(final byte[] haystack, final byte[] needle) {
        long bestIndex = -1;
        int best = -1;
        int startIndex = 0;
        for (int i = 1; i < needle.length; i++) {
            final int indexOf = indexOf(haystack, Arrays.copyOf(needle, i), startIndex);
            if (indexOf == -1) {
                return Pair.of(bestIndex, best);
            }
            startIndex = indexOf;
            best = i;
            bestIndex = indexOf;
        }
        return Pair.of(bestIndex, best);
    }

    private static int indexOf(byte[] source, byte[] target, int start) {
        if (target.length > source.length) {
            return -1;
        }
        for (int i = start; i <= source.length - target.length; i++) {
            int targetPos = 0;
            while (source[i + targetPos] == target[targetPos]) {
                targetPos++;
                if (targetPos == target.length) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static Byte[] toObject(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        Byte[] newBytes = new Byte[bytes.length];
        for (int i = 0; i < newBytes.length; i++) {
            newBytes[i] = bytes[i];
        }
        return newBytes;
    }

    private static Pair<Long, Integer> longestSubarray(byte[] source, byte[] target) {
        if (source.length == 0 || target.length == 0) {
            return Pair.of(-1L, -1);
        }

        int m = source.length;
        int n = target.length;
        int cost = 0;
        int maxLen = 0;
        int location = -1;
        int[] p = new int[n];
        int[] d = new int[n];

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                // calculate cost/score
                if (source[i] != target[j]) {
                    cost = 0;
                } else {
                    if ((i == 0) || (j == 0)) {
                        cost = 1;
                    } else {
                        cost = p[j - 1] + 1;
                    }
                }
                d[j] = cost;

                if (cost > maxLen) {
                    location = i;
                    maxLen = cost;
                }
            } // for {}

            int[] swap = p;
            p = d;
            d = swap;
        }

        return Pair.of((long)location, maxLen);
    }
}
