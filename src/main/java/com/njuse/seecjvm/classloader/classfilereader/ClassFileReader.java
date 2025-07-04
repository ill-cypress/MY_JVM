package com.njuse.seecjvm.classloader.classfilereader;

import com.njuse.seecjvm.classloader.classfilereader.classpath.*;
import com.njuse.seecjvm.util.PathUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;

/**
 * This class is the simulated implementation of Java Classloader.
 */
public class ClassFileReader {
    private static ClassFileReader reader = new ClassFileReader();
    private static final String FILE_SEPARATOR = File.separator;
    private static final String PATH_SEPARATOR = File.pathSeparator;

    private ClassFileReader() {
    }

    public static ClassFileReader getInstance() {
        return reader;
    }

    private static Entry bootClasspath = null;//bootstrap class entry
    private static Entry extClasspath = null;//extension class entry
    private static Entry userClasspath = null;//user class entry

    public static void setBootClasspath(String classpath) {
        bootClasspath = chooseEntryType(classpath);
    }

    public static void setExtClasspath(String classpath) {
        extClasspath = chooseEntryType(classpath);
    }

    public static void setUserClasspath(String classpath) {
        userClasspath = chooseEntryType(classpath);
    }

    /**
     * select Entry by type of classpath
     */
    public static Entry chooseEntryType(String classpath) {
        if (classpath.contains(PATH_SEPARATOR)) {
            return new CompositeEntry(classpath);
        }
        if (classpath.endsWith("*")) {
            return new WildEntry(classpath);
        }
        if (classpath.endsWith(".jar") || classpath.endsWith(".JAR")
                || classpath.endsWith(".zip") || classpath.endsWith(".ZIP")) {
            return new ArchivedEntry(classpath);
        }
        return new DirEntry(classpath);
    }

    /**
     * @param className class to be read
     * @param privilege privilege of relevant class
     * @return content of class file and the privilege of loaded class
     */
    public Pair<byte[], Integer> readClassFile(String className, EntryType privilege) throws IOException, ClassNotFoundException {
        String realClassName = className + ".class";
        realClassName = PathUtil.transform(realClassName);
        //tod
        byte[] bytes;
        if(className.equals("java/lang/Object")){
            bytes = bootClasspath.readClass(realClassName);
            if (bytes != null) {
                return new ImmutablePair<>(bytes, EntryType.LOW);
            }
        }else if(privilege!=null) {
            switch (privilege.getValue()) {
                case EntryType.MIDDLE:
                    bytes = extClasspath.readClass(realClassName);
                    if (bytes != null) {
                        return new ImmutablePair<>(bytes, EntryType.MIDDLE);
                    }
                    bytes = bootClasspath.readClass(realClassName);
                    if (bytes != null) {
                        return new ImmutablePair<>(bytes, EntryType.LOW);
                    }
                    break;
                case EntryType.LOW:
                    bytes = bootClasspath.readClass(realClassName);
                    if (bytes != null) {
                        return new ImmutablePair<>(bytes, EntryType.LOW);
                    }
                    break;
                default:
                    bytes = userClasspath.readClass(realClassName);
                    if (bytes != null) {
                        return new ImmutablePair<>(bytes, EntryType.HIGH);
                    }
                    bytes = extClasspath.readClass(realClassName);
                    if (bytes != null) {
                        return new ImmutablePair<>(bytes, EntryType.MIDDLE);
                    }
                    bytes = bootClasspath.readClass(realClassName);
                    if (bytes != null) {
                        return new ImmutablePair<>(bytes, EntryType.LOW);
                    }
                    break;
            }
        }else{
            bytes = userClasspath.readClass(realClassName);
            if (bytes != null) {
                return new ImmutablePair<>(bytes, EntryType.HIGH);
            }
            bytes = extClasspath.readClass(realClassName);
            if (bytes != null) {
                return new ImmutablePair<>(bytes, EntryType.MIDDLE);
            }
            bytes = bootClasspath.readClass(realClassName);
            if (bytes != null) {
                return new ImmutablePair<>(bytes, EntryType.LOW);
            }
        }
        /**
         * Add some codes here.
         *
         * You can pass realClassName to readClass()
         *
         * Read class file in privilege order
         * HIGH has the highest privileges and LOW has the lowest privileges.
         * If there is no relevant class loaded before, use default privilege.
         * Default privilege is HIGH
         *
         * Return the result once you read it.
         */
        throw new ClassNotFoundException();
    }
}
