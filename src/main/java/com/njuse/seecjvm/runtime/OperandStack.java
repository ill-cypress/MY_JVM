package com.njuse.seecjvm.runtime;

import com.njuse.seecjvm.runtime.struct.JObject;
import com.njuse.seecjvm.runtime.struct.Slot;
import lombok.Getter;
import lombok.Setter;

import java.util.EmptyStackException;

@Getter
@Setter
public class OperandStack {
    private int top;
    private int maxStackSize;
    private Slot[] slots;

    public OperandStack(int maxStackSize) {
        assert maxStackSize >= 0;
        this.maxStackSize = maxStackSize;
        slots = new Slot[maxStackSize];
        for (int i = 0; i < maxStackSize; i++) slots[i] = new Slot();
        top = 0;
    }

    /**
     * TOD：向操作数栈顶端push一个int型变量
     *
     * @param value 变量的值
     */
    public void pushInt(int value) {
        if(top >=maxStackSize) throw new StackOverflowError();
        slots[top].setValue(value);
        top++;
    }

    /**
     * TOD：从操作数栈顶端pop一个int型变量
     *
     * @return 返回这个int的值
     */
    public int popInt() {
        top--;
        if (top < 0) throw new EmptyStackException();
        int ret = slots[top].getValue();
        slots[top] = new Slot();
        System.out.println(ret);
        return ret;
    }

    public void pushFloat(float value) {
        if (top >= maxStackSize) throw new StackOverflowError();
        slots[top].setValue(Float.floatToIntBits(value));
        top++;
    }

    public float popFloat() {
        top--;
        if (top < 0) throw new EmptyStackException();
        float ret = Float.intBitsToFloat(slots[top].getValue());
        slots[top] = new Slot();
        System.out.println(ret);
        return ret;
    }

    /**
     * TOD：向操作数栈顶push一个 long 类型的变量
     *
     * @param value 变量的值
     */
    public void pushLong(long value) {
        if (top >= maxStackSize-1) throw new StackOverflowError();
        int lowFourBytes = (int) (value & 0xFFFFFFFFL);
        int highFourBytes = (int) (value >>> 32);
        slots[top++].setValue(lowFourBytes);
        slots[top++].setValue(highFourBytes);
    }

    /**
     * TOD：从操作数栈顶端pop一个long型变量
     *
     * @return 返回这个long的值
     */
    public long popLong() {
        if (top-2 < 0) throw new EmptyStackException();
        int highFourBytes = slots[--top].getValue();
        slots[top] = new Slot();
        int lowFourBytes = slots[--top].getValue();
        slots[top] = new Slot();
        long ret = ((long)highFourBytes << 32) | (lowFourBytes & 0xFFFFFFFFL);
        System.out.println(ret);
        return ret;
    }

    public void pushDouble(double value) {
        pushLong(Double.doubleToLongBits(value));
    }

    /**
     * TOD：从操作数栈顶端pop一个double型变量
     *
     * @return 返回这个double的值
     */
    public double popDouble() {
        if (top-2 < 0) throw new EmptyStackException();
        int highFourBytes = slots[--top].getValue();
        slots[top] = new Slot();
        int lowFourBytes = slots[--top].getValue();
        slots[top] = new Slot();
        double ret = Double.longBitsToDouble(((long)highFourBytes << 32) | (lowFourBytes & 0xFFFFFFFFL));
        System.out.println(ret);
        return ret;
    }

    public void pushObjectRef(JObject ref) {
        if (top >= maxStackSize) throw new StackOverflowError();
        slots[top].setObject(ref);
        top++;
    }

    public JObject popObjectRef() {
        top--;
        if (top < 0) throw new EmptyStackException();
        JObject ret = slots[top].getObject();
        slots[top] = new Slot();
        return ret;
    }

    public void pushSlot(Slot slot) {
        if (top >= maxStackSize) throw new StackOverflowError();
        slots[top] = slot;
        top++;
    }

    public Slot popSlot() {
        top--;
        if (top < 0) throw new EmptyStackException();
        Slot ret = slots[top];
        slots[top] = new Slot();
        return ret;
    }

}
