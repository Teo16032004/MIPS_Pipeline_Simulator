class ID_EX_Register {

    public Instruction instruction;

    public int pc;

    public int readData1;

    public int readData2;

    public int immediate;

    public int rs, rt, rd;

    // Control signals (saved from decode stage)
    public boolean regWrite;
    public boolean memRead;
    public boolean memWrite;
    public boolean branch;
    public boolean aluSrc;
    public boolean regDst;
    public boolean memToReg;
    public boolean jump;

    public void clear() {

        instruction = null;

        pc = 0;

        readData1 = readData2 = immediate = 0;

        rs = rt = rd = 0;

        // Clear control signals
        regWrite = memRead = memWrite = branch = aluSrc = regDst = memToReg = jump = false;

    }

}
