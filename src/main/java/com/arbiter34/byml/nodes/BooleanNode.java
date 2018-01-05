package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;

import java.io.IOException;
import java.util.Optional;

public class BooleanNode implements Node<Boolean> {
    public static final short NODE_TYPE = 0xD0;

    private boolean value;

    public BooleanNode(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    public static BooleanNode parse(long value) {
        return new BooleanNode(value == 1);
    }

    public void write(final BinaryAccessFile file) throws IOException {
        file.writeUnsignedInt(value ? 1L : 0L);
    }

    public boolean equals(boolean other) {
        return value == other;
    }

    @Override
    public short getNodeType() {
        return NODE_TYPE;
    }

    @Override
    public boolean eq(Boolean aBoolean) {
        return aBoolean != null && aBoolean.equals(value);
    }

    @Override
    public void setValue(Boolean aBoolean) {
        this.value = Optional.ofNullable(aBoolean).orElse(value);
    }

    @Override
    public Boolean getValue() {
        return value;
    }
}
