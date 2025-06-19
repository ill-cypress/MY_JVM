package com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref;

import com.njuse.seecjvm.classloader.ClassLoader;
import com.njuse.seecjvm.memory.jclass.JClass;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.RuntimeConstantPool;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.Constant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SymRef implements Constant {
    public RuntimeConstantPool runtimeConstantPool;
    public String className;    //format : java/lang/Object
    public JClass clazz;        //to restore the clazz after resolve (null at first )

    public void resolveClassRef() throws ClassNotFoundException, IllegalAccessException {
        //tod
        if(clazz!=null)return;

        /* step 1
         * Complete the method isAccessibleTo() in JClass
         * Make sure you know what is caller and what is callee.
         */
        //You can get a JClass from runtimeConstantPool.getClazz()
        //the class who own the runTimeConstantPool is caller
        JClass caller = runtimeConstantPool.getClazz();

        //className is the name of callee

        /* step2
         * Use ClassLoader.getInstance() to get the classloader
         * You should load class or interface C with initiating Loader of D
         */
        ClassLoader classLoader = ClassLoader.getInstance();
        JClass callee = classLoader.loadClass(className, caller.getLoadEntryType());

        /* step3
         * Check the permission and throw IllegalAccessException
         * Don't forget to set the value of clazz with loaded class
         */
        if(!callee.isAccessibleTo(caller)){
            throw new IllegalAccessException();
        }
        this.clazz = callee;
    }
}
