class TestParser {
    public static void main(String[] args) {
        Instruction instr = InstructionParser.parse("LW $4, 4($1)");
        System.out.println("Opcode: " + instr.getOpcode());
        System.out.println("rt (dest): " + instr.getRt());
        System.out.println("rs (base): " + instr.getRs());
        System.out.println("immediate (offset): " + instr.getImmediate());
    }
}
