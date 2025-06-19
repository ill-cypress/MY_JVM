package com.njuse.seecjvm.instructions.comparison;

import com.njuse.seecjvm.instructions.base.NoOperandsInstruction;
import com.njuse.seecjvm.runtime.OperandStack;
import com.njuse.seecjvm.runtime.StackFrame;

public class DCMPL extends NoOperandsInstruction {

    /**
     * TOD：实现这条指令
     */
    @Override
    public void execute(StackFrame frame) {
        OperandStack stack = frame.getOperandStack();
        double x2 = stack.popDouble();
        double x1 = stack.popDouble();
        if(Double.isNaN(x1) || Double.isNaN(x2)){
            stack.pushInt(-1);
            return;
        }
        if(x1 > x2){
            stack.pushInt(1);
        }else if(x1 < x2){
            stack.pushInt(-1);
        }else{
            stack.pushInt(0);
        }
    }
}
