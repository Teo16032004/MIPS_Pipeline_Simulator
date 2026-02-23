// ==================== INSTRUCTION PARSER ====================

class InstructionParser {

    public static Instruction parse(String line) {

        line = line.trim();

        if (line.isEmpty() || line.startsWith("#")) {

            return null;

        }

        

        String[] parts = line.split("[\\s,()]+");

        String opcode = parts[0].toUpperCase();

        

        Instruction instr = null;

        

        // R-Type: ADD $rd, $rs, $rt

        if (opcode.matches("ADD|SUB|AND|OR|XOR|SLT")) {

            instr = new Instruction(opcode, "R");

            instr.setRd(parseRegister(parts[1]));

            instr.setRs(parseRegister(parts[2]));

            instr.setRt(parseRegister(parts[3]));

        }

        // Shift: SLL/SRL $rd, $rt, shamt

        else if (opcode.matches("SLL|SRL")) {

            instr = new Instruction(opcode, "R");

            instr.setRd(parseRegister(parts[1]));

            instr.setRt(parseRegister(parts[2]));

            instr.setShamt(Integer.parseInt(parts[3]));

        }

        // I-Type: ADDI/ORI $rt, $rs, immediate

        else if (opcode.matches("ADDI|ORI")) {

            instr = new Instruction(opcode, "I");

            instr.setRt(parseRegister(parts[1]));

            instr.setRs(parseRegister(parts[2]));

            instr.setImmediate(Integer.parseInt(parts[3]));

        }

        // Load/Store: LW/SW $rt, offset($rs)

        else if (opcode.matches("LW|SW")) {

            instr = new Instruction(opcode, "I");

            instr.setRt(parseRegister(parts[1]));

            instr.setImmediate(Integer.parseInt(parts[2]));

            instr.setRs(parseRegister(parts[3]));

        }

        // Branch: BEQ $rs, $rt, offset

        else if (opcode.equals("BEQ")) {

            instr = new Instruction(opcode, "I");

            instr.setRs(parseRegister(parts[1]));

            instr.setRt(parseRegister(parts[2]));

            instr.setImmediate(Integer.parseInt(parts[3]));

        }

        // BGEZ: BGEZ $rs, offset

        else if (opcode.equals("BGEZ")) {

            instr = new Instruction(opcode, "I");

            instr.setRs(parseRegister(parts[1]));

            instr.setImmediate(Integer.parseInt(parts[2]));

        }

        // Jump: J address

        else if (opcode.equals("J")) {

            instr = new Instruction(opcode, "J");

            instr.setAddress(Integer.parseInt(parts[1]));

        }

        

        return instr;

    }

    

    private static int parseRegister(String reg) {

        reg = reg.replace("$", "").trim();

        return Integer.parseInt(reg);

    }

}


