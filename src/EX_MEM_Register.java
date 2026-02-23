class EX_MEM_Register {

    public Instruction instruction;

    public int aluResult;

    public int readData2;

    public int writeRegister;

    public boolean zero;

    public int branchTarget;

    

    public void clear() {

        instruction = null;

        aluResult = readData2 = writeRegister = branchTarget = 0;

        zero = false;

    }

}


