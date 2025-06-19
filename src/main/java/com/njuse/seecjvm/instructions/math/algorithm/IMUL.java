package com.njuse.seecjvm.instructions.math.algorithm;

import com.njuse.seecjvm.instructions.base.NoOperandsInstruction;
import com.njuse.seecjvm.runtime.OperandStack;
import com.njuse.seecjvm.runtime.StackFrame;

public class IMUL extends NoOperandsInstruction {

    /**
     * TOD：实现这条指令
     */
    @Override
    public void execute(StackFrame frame) {
        OperandStack stack = frame.getOperandStack();
        int x1 = stack.popInt();
        int x2 = stack.popInt();
        int ret = x1 * x2;
        stack.pushInt(ret);
    }
}
