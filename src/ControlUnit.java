// ==================== CONTROL UNIT ====================

class ControlUnit {

    public boolean regWrite;

    public boolean memRead;

    public boolean memWrite;

    public boolean branch;

    public boolean aluSrc;

    public boolean regDst;

    public boolean memToReg;

    public boolean jump;

    public int aluOp;

    

    public void decode(String opcode) {

        // Reset all signals

        regWrite = memRead = memWrite = branch = aluSrc = regDst = memToReg = jump = false;

        aluOp = 0;

        

        switch (opcode) {

            // R-Type instructions

            case "ADD": case "SUB": case "AND": case "OR": case "XOR": case "SLT":

            case "SLL": case "SRL":

                regWrite = true;

                regDst = true;

                aluOp = 2; // R-type

                break;

                

            // I-Type instructions

            case "ADDI":

                regWrite = true;

                aluSrc = true;

                aluOp = 0; // Add

                break;

                

            case "ORI":

                regWrite = true;

                aluSrc = true;

                aluOp = 1; // Or

                break;

                

            case "LW":

                regWrite = true;

                aluSrc = true;

                memRead = true;

                memToReg = true;

                aluOp = 0; // Add for address calculation

                break;

                

            case "SW":

                aluSrc = true;

                memWrite = true;

                aluOp = 0; // Add for address calculation

                break;

                

            case "BEQ":

                branch = true;

                aluOp = 1; // Subtract for comparison

                break;

                

            case "BGEZ":

                branch = true;

                aluOp = 5; // SLT for comparison

                break;

                

            // J-Type instructions

            case "J":

                jump = true;

                break;

                

            default:

                throw new IllegalArgumentException("Unknown opcode: " + opcode);

        }

    }

    

    public int getALUControl(String opcode) {

        switch (opcode) {

            case "ADD": case "ADDI": case "LW": case "SW":

                return ALU.ADD;

            case "SUB": case "BEQ":

                return ALU.SUB;

            case "AND":

                return ALU.AND;

            case "OR": case "ORI":

                return ALU.OR;

            case "XOR":

                return ALU.XOR;

            case "SLT": case "BGEZ":

                return ALU.SLT;

            case "SLL":

                return ALU.SLL;

            case "SRL":

                return ALU.SRL;

            default:

                return ALU.ADD;

        }

    }

}


