package com.arbiter34.byml;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.nodes.Node;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Header implements Node {
    private static final int MAGIC_BYTES = 0x4259;
    private static final List<Integer> validVersions = Stream.of(0x01, 0x02)
                                                           .collect(Collectors.toList());

    private final int magicBytes;
    private final int version;
    private final long nodeNameTableOffset;
    private final long stringValueTableOffset;
    private final long pathValueTableOffset;
    private final long rootNodeOffset;

    private Header(int magicBytes, int version, long nodeNameTableOffset, long stringValueTableOffset,
                   long pathValueTableOffset, long rootNodeOffset) {
        this.magicBytes = magicBytes;
        this.version = version;
        this.nodeNameTableOffset = nodeNameTableOffset;
        this.stringValueTableOffset = stringValueTableOffset;
        this.pathValueTableOffset = pathValueTableOffset;
        this.rootNodeOffset = rootNodeOffset;
    }

    public static Header parse(final BinaryAccessFile file) throws IOException {
        if (file == null) {
            throw new UnsupportedOperationException("InputStream is not valid for parsing header.");
        }
        final int magicBytes = file.readShort();
        if (magicBytes != MAGIC_BYTES) {
            throw new IOException(String.format("Invalid magic bytes found. Found: %s Expected: %s", magicBytes, MAGIC_BYTES));
        }

        final int version = file.readShort();
        if (!validVersions.contains(version)) {
            throw new IOException(String.format("Invalid version found. Found: %s Expected: %s", version, validVersions));
        }

        final long nameTableOffset = file.readUnsignedInt();
        final long stringValueTableOffset = file.readUnsignedInt();
        //final long pathValueTableOffset = file.readUnsignedInt();
        final long rootNodeOffset = file.readUnsignedInt();
        return new Header(magicBytes, version, nameTableOffset, stringValueTableOffset, 0, rootNodeOffset);
    }

    public void write(final BinaryAccessFile outputStream) throws IOException {
        outputStream.writeShort(magicBytes);
        outputStream.writeShort(version);
        outputStream.writeUnsignedInt(nodeNameTableOffset);
        outputStream.writeUnsignedInt(stringValueTableOffset);
        outputStream.writeUnsignedInt(rootNodeOffset);
    }

    public int getVersion() {
        return version;
    }

    public long getNodeNameTableOffset() {
        return nodeNameTableOffset;
    }

    public long getStringValueTableOffset() {
        return stringValueTableOffset;
    }

    public long getRootNodeOffset() {
        return rootNodeOffset;
    }

    public long getPathValueTableOffset() {
        return pathValueTableOffset;
    }
}
