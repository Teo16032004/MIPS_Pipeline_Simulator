// ==================== INSTRUCTION CLASS ====================

class Instruction {

    private String opcode;

    private int rs, rt, rd;

    private int immediate;

    private int shamt;

    private int address;

    private String type; // R, I, or J

    

    public Instruction(String opcode, String type) {

        this.opcode = opcode;

        this.type = type;

    }

    

    // Getters and setters

    public String getOpcode() { return opcode; }

    public String getType() { return type; }

    public int getRs() { return rs; }

    public int getRt() { return rt; }

    public int getRd() { return rd; }

    public int getImmediate() { return immediate; }

    public int getShamt() { return shamt; }

    public int getAddress() { return address; }

    

    public void setRs(int rs) { this.rs = rs; }

    public void setRt(int rt) { this.rt = rt; }

    public void setRd(int rd) { this.rd = rd; }

    public void setImmediate(int immediate) { this.immediate = immediate; }

    public void setShamt(int shamt) { this.shamt = shamt; }

    public void setAddress(int address) { this.address = address; }

    

    @Override

    public String toString() {

        return String.format("%s (Type: %s, Rs: %d, Rt: %d, Rd: %d, Imm: %d)", 

                           opcode, type, rs, rt, rd, immediate);

    }

}


