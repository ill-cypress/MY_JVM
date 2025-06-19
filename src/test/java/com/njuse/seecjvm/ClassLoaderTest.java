package com.njuse.seecjvm;

import com.njuse.seecjvm.classloader.ClassLoader;
import com.njuse.seecjvm.classloader.classfilereader.ClassFileReader;
import com.njuse.seecjvm.memory.MethodArea;
import com.njuse.seecjvm.memory.jclass.InitState;
import com.njuse.seecjvm.memory.jclass.JClass;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref.ClassRef;
import com.njuse.seecjvm.runtime.Vars;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static junit.framework.TestCase.*;

public class ClassLoaderTest {
    private ClassLoader loader = ClassLoader.getInstance();
    private static String testPath = String.join(File.separator,"src","test","testpath");
    @BeforeClass
    public static void setEnv(){

        ClassFileReader.setBootClasspath(String.join(File.separator,testPath,"jre","lib"));
        ClassFileReader.setExtClasspath(String.join(File.separator,testPath,"jre","lib","ext"));
    }

    @Before
    public void resetMethodArea(){
        MethodArea.setClassMap(new LinkedHashMap<>());
    }

    @Test
    public void load1() {
        try{
            int a=0;
            Set<String> expectedClasses = new LinkedHashSet<>(Arrays.asList("java/lang/Object","testsrc/InterfaceParent", "testsrc/Parent", "testsrc/InterfaceChild", "testsrc/Child"));
            ClassFileReader.setUserClasspath(String.join(File.separator,testPath,"user","test1"));
            loader.loadClass("testsrc/Child",null);
            Set<String> realClasses = MethodArea.getClassMap().keySet();
            verify(expectedClasses,realClasses);
        }catch(ClassNotFoundException e){
            assertEquals(true,false);
        }

    }

    @Test
    public void load2()  {
        try{
            Set<String> expectedClasses = new LinkedHashSet<>(Arrays.asList("java/lang/Object","testsrc/Parent","testsrc/InterfaceParent","testsrc/InterfaceChild","testsrc/Child"));
            ClassFileReader.setUserClasspath(String.join(File.separator,testPath,"user","test2"));
            loader.loadClass("testsrc/Child",null);
            Set<String> realClasses = MethodArea.getClassMap().keySet();
            verify(expectedClasses,realClasses);
        }catch(ClassNotFoundException e){
            assertEquals(true,false);
        }

    }

    @Test
    public void load3()  {
        try{
            Set<String> expectedClasses = new LinkedHashSet<>(Arrays.asList("java/lang/Object","testsrc/InterfaceParent","testsrc/Parent","testsrc/InterfaceChild","testsrc/Child"));
            ClassFileReader.setUserClasspath(String.join(File.separator,testPath,"user","test3"));
            loader.loadClass("testsrc/Child",null);
            Set<String> realClasses = MethodArea.getClassMap().keySet();
            verify(expectedClasses,realClasses);
        }catch(ClassNotFoundException e){
            assertEquals(true,false);
        }

    }

    @Test
    public void load4() throws IllegalAccessException {
        try{
            ClassFileReader.setUserClasspath(String.join(File.separator,testPath,"user","test4"));
            JClass clazz = loader.loadClass("testsrc/Child",null);
            Set<String> expectedClassesBefore = new LinkedHashSet<>(Arrays.asList("java/lang/Object", "testsrc/Child"));
            Set<String> realClassesBefore = MethodArea.getClassMap().keySet();
            verify(expectedClassesBefore,realClassesBefore);
            ClassRef ref = (ClassRef) clazz.getRuntimeConstantPool().getConstants()[1];
            ref.resolveClassRef();
            Set<String> expectedClassesAfter = new LinkedHashSet<>(Arrays.asList("java/lang/Object","testsrc/Child","testsrc/InterfaceParent","testsrc/Parent"));
            Set<String> realClassesAfter = MethodArea.getClassMap().keySet();
            verify(expectedClassesAfter,realClassesAfter);
            assertEquals(InitState.PREPARED,clazz.getInitState() );
            assertEquals(1,MethodArea.getClassMap().get("java/lang/Object").getLoadEntryType().getValue());
        }catch(ClassNotFoundException e){
            assertEquals(true,false);
        }



    }

    private void verify(Set<String> expected,Set<String> real){
        assert expected!=null && real!=null;
        assertEquals(expected.size(), real.size());
        Iterator<String> iteratorExpected = expected.iterator();
        Iterator<String> iteratorReal = real.iterator();
        while(iteratorExpected.hasNext()){
            assertEquals(iteratorExpected.next(),iteratorReal.next());
        }
    }

    @Test
    public void load5()  {
        try{
            ClassFileReader.setUserClasspath(String.join(File.separator,testPath,"user","test5"));
            JClass clazz = loader.loadClass("testsrc/Child",null);
            Vars staticVars = clazz.getStaticVars();
            assertEquals(0.0D,staticVars.getDouble(0));
            assertEquals(5,staticVars.getInt(2));
        }catch(ClassNotFoundException e){
            assertEquals(true,false);
        }


    }

    @Test(expected = IllegalAccessException.class)
    public void load6() throws IllegalAccessException {
        try{
            ClassFileReader.setUserClasspath(String.join(File.separator,testPath,"user","test6"));
            JClass clazz = loader.loadClass("testsrc/Child",null);
            Set<String> expectedClassesBefore = new LinkedHashSet<>(Arrays.asList("java/lang/Object", "testsrc/Child"));
            Set<String> realClassesBefore = MethodArea.getClassMap().keySet();
            verify(expectedClassesBefore,realClassesBefore);
            ClassRef ref = (ClassRef) clazz.getRuntimeConstantPool().getConstants()[1];
            ref.resolveClassRef();
        }catch(ClassNotFoundException e){
            assertEquals(true,false);
        }

    }
}
