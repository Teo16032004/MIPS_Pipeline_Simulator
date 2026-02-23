// ==================== PIPELINE REGISTERS ====================

class IF_ID_Register {

    public Instruction instruction;

    public int pc;

    

    public void clear() {

        instruction = null;

        pc = 0;

    }

}


