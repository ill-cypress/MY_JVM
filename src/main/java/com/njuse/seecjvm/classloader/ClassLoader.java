package com.njuse.seecjvm.classloader;

import com.njuse.seecjvm.classloader.classfileparser.ClassFile;
import com.njuse.seecjvm.classloader.classfileparser.constantpool.ConstantPool;
import com.njuse.seecjvm.classloader.classfilereader.ClassFileReader;
import com.njuse.seecjvm.classloader.classfilereader.classpath.EntryType;
import com.njuse.seecjvm.memory.MethodArea;
import com.njuse.seecjvm.memory.jclass.Field;
import com.njuse.seecjvm.memory.jclass.InitState;
import com.njuse.seecjvm.memory.jclass.JClass;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.RuntimeConstantPool;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.wrapper.DoubleWrapper;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.wrapper.FloatWrapper;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.wrapper.IntWrapper;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.wrapper.LongWrapper;
import com.njuse.seecjvm.runtime.Vars;
import com.njuse.seecjvm.runtime.struct.JObject;
import com.njuse.seecjvm.runtime.struct.NullObject;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClassLoader {
    private static ClassLoader classLoader = new ClassLoader();
    private ClassFileReader classFileReader;
    private MethodArea methodArea;

    private ClassLoader() {
        classFileReader = ClassFileReader.getInstance();
        methodArea = MethodArea.getInstance();
    }

    public static ClassLoader getInstance() {
        return classLoader;
    }

    /**
     * load phase
     *
     * @param className       name of class
     * @param initiatingEntry null value represents load MainClass
     */
    public JClass loadClass(String className, EntryType initiatingEntry) throws ClassNotFoundException {
        JClass ret;
        if ((ret = methodArea.findClass(className)) == null) {
            return loadNonArrayClass(className, initiatingEntry);
        }
        return ret;
    }

    private JClass loadNonArrayClass(String className, EntryType initiatingEntry) throws ClassNotFoundException {
        try {
            Pair<byte[], Integer> res = classFileReader.readClassFile(className, initiatingEntry);
            byte[] data = res.getKey();
            EntryType definingEntry = new EntryType(res.getValue());
            //define class
            JClass clazz = defineClass(data, definingEntry);
            //link class
            linkClass(clazz);
            return clazz;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
    }

    /**
     * define class
     *
     * @param data          binary of class file
     * @param definingEntry defining loader of class
     */
    private JClass defineClass(byte[] data, EntryType definingEntry) throws ClassNotFoundException {
        ClassFile classFile = new ClassFile(data);
        JClass clazz = new JClass(classFile);
        //tod
        //update load entry of the class
        clazz.setLoadEntryType(definingEntry);

        //load superclass recursively
        resolveSuperClass(clazz);

        //load interfaces of this class
        resolveInterfaces(clazz);

        //add to method area
        MethodArea methodArea = MethodArea.getInstance();
        String className = clazz.getName();
        methodArea.addClass(className, clazz);

        return clazz;
    }

    /**
     * load superclass before add to method area
     */
    private void resolveSuperClass(JClass clazz) throws ClassNotFoundException {
        //tod
        //Use the load entry(defining entry) as initiating entry of super class
        EntryType definingEntry = clazz.getLoadEntryType();
        String superClassName = clazz.getSuperClassName();
        if (superClassName != null && !superClassName.isEmpty()) {
            JClass father = loadClass(superClassName, definingEntry);
            clazz.setSuperClass(father);
        }
    }

    /**
     * load interfaces before add to method area
     */
    private void resolveInterfaces(JClass clazz) throws ClassNotFoundException {
        //tod
        //Use the load entry(defining entry) as initiating entry of interfaces
        EntryType definingEntry = clazz.getLoadEntryType();
        String[] InterfaceNames = clazz.getInterfaceNames();
        ArrayList<JClass> interfaces = new ArrayList<>();
        for (String interfaceName : InterfaceNames) {
            JClass interfaceClass = loadClass(interfaceName, definingEntry);
            interfaces.add(interfaceClass);
        }
        clazz.setInterfaces(interfaces.toArray(new JClass[interfaces.size()]));
    }

    /**
     * link phase
     */
    private void linkClass(JClass clazz) {
        verify(clazz);
        prepare(clazz);
    }

    /**
     * You don't need to write any code here.
     */
    private void verify(JClass clazz) {
        //do nothing
    }

    private void prepare(JClass clazz) {
        //tod
        /*
         * step1 (We do it for you here)
         *      count the fields slot id in instance
         *      count the fields slot id in class
         */
        calInstanceFieldSlotIDs(clazz);
        calStaticFieldSlotIDs(clazz);

        /*step2
         *      alloc memory for fields(We do it for you here) and init static vars
         */
        allocAndInitStaticVars(clazz);

        /*step3
         *      set the init state
         */
        clazz.setInitState(InitState.PREPARED);
    }

    /**
     * count the number of field slots in instance
     * long and double takes two slots
     * the field is not static
     */
    private void calInstanceFieldSlotIDs(JClass clazz) {
        int slotID = 0;
        if (clazz.getSuperClass() != null) {
            slotID = clazz.getSuperClass().getInstanceSlotCount();
        }
        Field[] fields = clazz.getFields();
        for (Field f : fields) {
            if (!f.isStatic()) {
                f.setSlotID(slotID);
                slotID++;
                if (f.isLongOrDouble()) slotID++;
            }
        }
        clazz.setInstanceSlotCount(slotID);
    }

    /**
     * count the number of field slots in class
     * long and double takes two slots
     * the field is static
     */
    private void calStaticFieldSlotIDs(JClass clazz) {
        int slotID = 0;
        Field[] fields = clazz.getFields();
        for (Field f : fields) {
            if (f.isStatic()) {
                f.setSlotID(slotID);
                slotID++;
                if (f.isLongOrDouble()) slotID++;
            }
        }
        clazz.setStaticSlotCount(slotID);

    }

    /**
     * primitive type is set to 0
     * ref type is set to null
     */
    private void initDefaultValue(JClass clazz, Field field) {
        //tod

        /* step 1
         *      get static vars of class
         */
        Vars vars = clazz.getStaticVars();

        /* step 2
         *      get the slotID index of field
         */
        int slotID = field.getSlotID();

        /*step 3
         *      switch by descriptor or some part of descriptor
         *      Handle basic type ZBCSIJDF and references (with new NullObject())
         */
        switch (field.descriptor) {
            case "Z":
            case "B":
            case "C":
            case "S":
            case "I":
                vars.setInt(slotID, 0);
                break;
            case "J":
                vars.setLong(slotID, 0L);
                break;
            case "F":
                vars.setFloat(slotID, 0.0f);
                break;
            case "D":
                vars.setDouble(slotID, 0.0d);
                break;
            default:
                vars.setObjectRef(slotID, new NullObject());
                break;
        }
    }

    /**
     * load const value from runtimeConstantPool for primitive type
     * String is not support now
     */
    private void loadValueFromRTCP(JClass clazz, Field field) {
        //static and final

        //tod
        /* step 1
         *      get static vars and runtime constant pool of class
         */
        Vars vars = clazz.getStaticVars();
        RuntimeConstantPool runtimeConstantPool = clazz.getRuntimeConstantPool();

        /* step 2
         *      get the slotID and constantValue index of field
         */
        int slotID = field.getSlotID();
        int constantValueIndex = field.getConstValueIndex();

        /* step 3
         *      switch by descriptor or some part of descriptor
         *      just handle basic type ZBCSIJDF, you don't have to throw any exception
         *      use wrapper to get value
         */
        switch (field.descriptor) {
            case "Z":
            case "B":
            case "C":
            case "S":
            case "I":
                int intVal = ((IntWrapper) runtimeConstantPool.getConstant(constantValueIndex)).getValue();
                vars.setInt(slotID, intVal);
                break;
            case "J":
                long longVal = ((LongWrapper) runtimeConstantPool.getConstant(constantValueIndex)).getValue();
                vars.setLong(slotID, longVal);
                break;
            case "F":
                float floatVal = ((FloatWrapper) runtimeConstantPool.getConstant(constantValueIndex)).getValue();
                vars.setFloat(slotID, floatVal);
                break;
            case "D":
                double doubleVal = ((DoubleWrapper) runtimeConstantPool.getConstant(constantValueIndex)).getValue();
                vars.setDouble(slotID, doubleVal);
                break;
            default:
                break;
        }
    }

    /**
     * the value of static final field is in com.njuse.seecjvm.runtime constant pool
     * others will be set to default value
     */
    private void allocAndInitStaticVars(JClass clazz) {
        clazz.setStaticVars(new Vars(clazz.getStaticSlotCount()));
        Field[] fields = clazz.getFields();
        for (Field f : fields) {
            //tod
            //Refer to manual for details.
            if(f.isStatic()){
                if(f.isFinal()){//static final
                    loadValueFromRTCP(clazz, f);
                }else{
                    initDefaultValue(clazz, f);
                }
            }
        }
    }
}
