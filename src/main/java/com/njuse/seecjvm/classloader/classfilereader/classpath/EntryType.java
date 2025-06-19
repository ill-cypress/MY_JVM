package com.njuse.seecjvm.classloader.classfilereader.classpath;

/**
 * the number presents different privileges of entry
 * The larger the number, the higher the privileges
 */
public class EntryType {
    private int value;
    public final static int LOW = 0x1;
    public final static int MIDDLE = 0x3;
    public final static int HIGH = 0x7;

    public EntryType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
