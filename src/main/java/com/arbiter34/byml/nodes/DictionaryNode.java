package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.util.NodeUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DictionaryNode extends LinkedHashMap<String, Node> implements Node<Map<String, Node>> {
    public static final short NODE_TYPE = 0xC1;

    public DictionaryNode() {
    }

    public static DictionaryNode parse(final StringTableNode nodeNameTable, final StringTableNode stringValueTable,
                                       final BinaryAccessFile file) throws IOException {
        final long typeAndNumEntries = file.readUnsignedInt();
        if(((short)(typeAndNumEntries >>> 24) & 0x00FF) != NODE_TYPE) {
            throw new IOException(String.format("Invalid node type. Expected: %s Found: %s", NODE_TYPE, ((short)((typeAndNumEntries & 0xF000) >>> 24))));
        }
        final int numEntries = (int)(typeAndNumEntries & 0x00FFFFFF);

        final DictionaryNode instance = new DictionaryNode();
        for (int i = 0; i < numEntries; i++) {
            final long nameIndexAndType = file.readUnsignedInt();
            final int nameIndex = (int)(nameIndexAndType >>> 8);
            final short nodeType = (short)(nameIndexAndType & 0x000000FF);
            final long value = file.readUnsignedInt();
            final String key = nodeNameTable.getEntries().get(nameIndex);
            final Node node = NodeUtil.parseNode(nodeNameTable, stringValueTable, file, nodeType, value);
            instance.put(key, node);
        }
        return instance;
    }

    public void write(final StringTableNode nodeNameTable, final StringTableNode stringValueTable,
                      final BinaryAccessFile file) throws IOException {
        final int numEntries = this.size();
        byte[] bytes = new byte[4];
        bytes[0] = (byte)NODE_TYPE;
        bytes[1] = (byte)(numEntries >>> 16);
        bytes[2] = (byte)(numEntries >>> 8);
        bytes[3] = (byte)(numEntries);
        file.write(bytes);

        final long dictionaryStart = file.getFilePointer();
        long dictionaryEnd = dictionaryStart + (8 * numEntries);
        for (final String key : keySet()) {
            final Node node = get(key);
            final int nameIndex = nodeNameTable.getEntries().indexOf(key);
            bytes[0] = (byte)(nameIndex >>> 16);
            bytes[1] = (byte)(nameIndex >>> 8);
            bytes[2] = (byte)(nameIndex);
            bytes[3] = (byte)node.getNodeType();
            file.write(bytes);
            if (node instanceof ArrayNode || node instanceof DictionaryNode) {
                file.writeUnsignedInt(dictionaryEnd);
                long lastPosition = file.getFilePointer();
                file.seek(dictionaryEnd);
                NodeUtil.writeNode(nodeNameTable, stringValueTable, file, node);
                dictionaryEnd = file.getFilePointer();
                file.seek(lastPosition);
            } else {
                NodeUtil.writeNode(nodeNameTable, stringValueTable, file, node);
            }
        }
        file.seek(dictionaryEnd);
    }

    @Override
    @JsonGetter("nodeType")
    public short getNodeType() {
        return NODE_TYPE;
    }

    @Override
    public boolean eq(Map<String, Node> stringNodeMap) {
        return this.equals(stringNodeMap);
    }

    @Override
    public void setValue(Map<String, Node> stringNodeMap) {
        this.clear();
        Optional.ofNullable(stringNodeMap).ifPresent(this::putAll);
    }

    @Override
    public Map<String, Node> getValue() {
        return this;
    }
}
