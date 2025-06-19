package com.njuse.seecjvm.instructions.control;

import com.njuse.seecjvm.instructions.base.BranchInstruction;
import com.njuse.seecjvm.runtime.StackFrame;

public class GOTO extends BranchInstruction {

    /**
     * TOD：实现这条指令
     */
    @Override
    public void execute(StackFrame frame) {
        int PC = frame.getNextPC();
        frame.setNextPC(PC+offset-3);
    }
}
