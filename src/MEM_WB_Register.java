class MEM_WB_Register {

    public Instruction instruction;

    public int aluResult;

    public int memData;

    public int writeRegister;

    

    public void clear() {

        instruction = null;

        aluResult = memData = writeRegister = 0;

    }

}


