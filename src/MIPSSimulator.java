// ==================== MIPS PIPELINE SIMULATOR ====================

class MIPSSimulator {

    private RegisterFile regFile;

    private Memory memory;

    private ALU alu;

    private ControlUnit control;

    // Pipeline registers

    private IF_ID_Register ifId;

    private ID_EX_Register idEx;

    private EX_MEM_Register exMem;

    private MEM_WB_Register memWb;

    // Program counter and instruction memory

    private int pc;

    private Instruction[] instructionMemory;

    private int cycles;

    private boolean halted;

    // Hazard detection and forwarding

    private int stallCount;

    private boolean enableHazardDetection;

    private boolean enableForwarding;

    // Track WB stage instruction for GUI display
    private Instruction lastWbInstruction;

    // Saved memWb state for forwarding (since MEM runs before EX)
    private MEM_WB_Register savedMemWb;

    // Saved instructions for GUI display (captures state BEFORE processing)
    private Instruction displayIF, displayID, displayEX, displayMEM, displayWB;

    public MIPSSimulator() {

        regFile = new RegisterFile();

        memory = new Memory();

        alu = new ALU();

        control = new ControlUnit();

        ifId = new IF_ID_Register();

        idEx = new ID_EX_Register();

        exMem = new EX_MEM_Register();

        memWb = new MEM_WB_Register();

        pc = 0;

        cycles = 0;

        halted = false;

        // Initialize hazard detection

        stallCount = 0;

        enableHazardDetection = true;

        enableForwarding = true;

        // Initialize saved memWb for forwarding
        savedMemWb = new MEM_WB_Register();

    }

    public void loadProgram(String[] instructions) {

        instructionMemory = new Instruction[instructions.length];

        for (int i = 0; i < instructions.length; i++) {

            instructionMemory[i] = InstructionParser.parse(instructions[i]);

        }

    }

    public void run() {

        System.out.println("=== Starting MIPS Pipeline Simulation ===\n");

        while (!halted) {

            cycles++;

            System.out.println("\n--- Cycle " + cycles + " ---");

            // Save memWb for forwarding before MEM stage overwrites it
            savedMemWb.instruction = memWb.instruction;
            savedMemWb.aluResult = memWb.aluResult;
            savedMemWb.memData = memWb.memData;
            savedMemWb.writeRegister = memWb.writeRegister;
            lastWbInstruction = memWb.instruction;

            // Execute pipeline stages in CORRECT reverse order: WB -> MEM -> EX -> ID -> IF
            writeback();

            memoryAccess();

            execute();

            decode();

            fetch();

            printPipelineState();

            // Check if pipeline is empty

            if (ifId.instruction == null && idEx.instruction == null &&

                    exMem.instruction == null && memWb.instruction == null) {

                halted = true;

            }

        }

        System.out.println("\n=== Simulation Complete ===");

        System.out.println("Total cycles: " + cycles);
        System.out.println("Total stalls: " + stallCount);

        regFile.printRegisters();

        memory.printMemory(0, 100);

    }

    private void fetch() {

        if (pc >= 0 && pc < instructionMemory.length && instructionMemory[pc] != null) {

            ifId.instruction = instructionMemory[pc];

            ifId.pc = pc;

            pc++;

            System.out.println("IF: Fetched " + ifId.instruction.getOpcode() + " at PC=" + ifId.pc);

        } else {

            ifId.instruction = null;

        }

    }

    private void decode() {

        if (ifId.instruction == null) {

            idEx.clear();

            return;

        }

        Instruction instr = ifId.instruction;

        control.decode(instr.getOpcode());

        // Hazard detection: Check for load-use hazard

        if (enableHazardDetection && detectLoadUseHazard(instr)) {

            // Stall: Insert bubble in ID/EX, keep IF/ID unchanged, don't increment PC

            idEx.clear();

            pc--; // Undo PC increment from fetch

            stallCount++;

            System.out.println("ID: STALL detected (Load-Use Hazard)");

            return;

        }

        idEx.instruction = instr;

        idEx.pc = ifId.pc;

        idEx.readData1 = regFile.read(instr.getRs());

        idEx.readData2 = regFile.read(instr.getRt());

        idEx.immediate = instr.getImmediate();

        idEx.rs = instr.getRs();

        idEx.rt = instr.getRt();

        idEx.rd = instr.getRd();

        // Save control signals to pipeline register (fixes data hazard issue)
        idEx.regWrite = control.regWrite;
        idEx.memRead = control.memRead;
        idEx.memWrite = control.memWrite;
        idEx.branch = control.branch;
        idEx.aluSrc = control.aluSrc;
        idEx.regDst = control.regDst;
        idEx.memToReg = control.memToReg;
        idEx.jump = control.jump;

        System.out.println("ID: Decoded " + instr.getOpcode());

    }

    private void execute() {

        if (idEx.instruction == null) {

            exMem.clear();

            return;

        }

        Instruction instr = idEx.instruction;

        // Use saved control signals from ID/EX register (fixes data hazard issue)
        // Don't call control.decode() here - use idEx.aluSrc, idEx.regDst, etc.

        // Apply forwarding if enabled

        int forwardedData1 = idEx.readData1;

        int forwardedData2 = idEx.readData2;

        if (enableForwarding) {

            int newData1 = getForwardedValue(idEx.rs, idEx.readData1);
            if (newData1 != idEx.readData1 && idEx.rs != 0) {
                System.out.println(
                        "  [HAZARD SOLVED] Forwarding $" + idEx.rs + ": " + idEx.readData1 + " -> " + newData1);
            }
            forwardedData1 = newData1;

            int newData2 = getForwardedValue(idEx.rt, idEx.readData2);
            if (newData2 != idEx.readData2 && idEx.rt != 0) {
                System.out.println(
                        "  [HAZARD SOLVED] Forwarding $" + idEx.rt + ": " + idEx.readData2 + " -> " + newData2);
            }
            forwardedData2 = newData2;

        }

        // Use saved control signal from idEx (not control.aluSrc)
        int aluInput2 = idEx.aluSrc ? idEx.immediate : forwardedData2;

        // Handle shift operations

        if (instr.getOpcode().equals("SLL") || instr.getOpcode().equals("SRL")) {

            aluInput2 = instr.getShamt();

        }

        int aluControl = control.getALUControl(instr.getOpcode());

        int aluOperand1 = (instr.getOpcode().equals("SLL") || instr.getOpcode().equals("SRL"))
                ? forwardedData2
                : forwardedData1;

        int aluResult = alu.execute(aluControl, aluOperand1, aluInput2);

        exMem.instruction = instr;

        exMem.aluResult = aluResult;

        exMem.readData2 = forwardedData2; // Use forwarded value for store operations

        // Use saved control signal from idEx for write register
        exMem.writeRegister = idEx.regDst ? idEx.rd : idEx.rt;

        exMem.zero = alu.isZero();

        exMem.branchTarget = idEx.pc + 1 + idEx.immediate;

        // Handle branches - use saved control signals

        if (idEx.branch) {

            if (instr.getOpcode().equals("BEQ") && exMem.zero) {

                pc = exMem.branchTarget;

                // Flush pipeline (control hazard)
                ifId.clear();

                System.out.println("  [CONTROL HAZARD] Branch taken - flushing pipeline");
                System.out.println("EX: Branch taken to PC=" + pc);

            } else if (instr.getOpcode().equals("BGEZ") && forwardedData1 >= 0) {

                pc = exMem.branchTarget;

                // Flush pipeline (control hazard)
                ifId.clear();

                System.out.println("  [CONTROL HAZARD] Branch taken - flushing pipeline");
                System.out.println("EX: Branch taken to PC=" + pc);

            }

        }

        // Handle jumps - use saved control signals

        if (idEx.jump) {

            pc = instr.getAddress();

            // Flush pipeline (control hazard)
            ifId.clear();

            System.out.println("  [CONTROL HAZARD] Jump taken - flushing pipeline");
            System.out.println("EX: Jump to PC=" + pc);

        }

        System.out.println("EX: " + instr.getOpcode() + " result=" + aluResult);

    }

    private void memoryAccess() {

        if (exMem.instruction == null) {

            memWb.clear();

            return;

        }

        Instruction instr = exMem.instruction;

        control.decode(instr.getOpcode());

        int memData = 0;

        if (control.memRead) {

            memData = memory.load(exMem.aluResult);

            System.out.println("MEM: Load from address " + exMem.aluResult + " = " + memData);

        } else if (control.memWrite) {

            memory.store(exMem.aluResult, exMem.readData2);

            System.out.println("MEM: Store " + exMem.readData2 + " to address " + exMem.aluResult);

        }

        memWb.instruction = instr;

        memWb.aluResult = exMem.aluResult;

        memWb.memData = memData;

        memWb.writeRegister = exMem.writeRegister;

    }

    private void writeback() {

        if (memWb.instruction == null) {

            return;

        }

        Instruction instr = memWb.instruction;

        control.decode(instr.getOpcode());

        if (control.regWrite) {

            int writeData = control.memToReg ? memWb.memData : memWb.aluResult;

            regFile.write(memWb.writeRegister, writeData);

            System.out.println("WB: Write " + writeData + " to $" + memWb.writeRegister);

        }

    }

    private void printPipelineState() {

        System.out.println("\nPipeline State:");

        System.out.println("  IF/ID:  " + (ifId.instruction != null ? ifId.instruction.getOpcode() : "empty"));

        System.out.println("  ID/EX:  " + (idEx.instruction != null ? idEx.instruction.getOpcode() : "empty"));

        System.out.println("  EX/MEM: " + (exMem.instruction != null ? exMem.instruction.getOpcode() : "empty"));

        System.out.println("  MEM/WB: " + (memWb.instruction != null ? memWb.instruction.getOpcode() : "empty"));

    }

    public RegisterFile getRegFile() {
        return regFile;
    }

    public Memory getMemory() {
        return memory;
    }

    // ==================== HAZARD DETECTION AND FORWARDING ====================

    /**
     * Detects load-use hazards: when a load instruction is in EX stage
     * and the current instruction in ID stage needs the loaded data
     */
    private boolean detectLoadUseHazard(Instruction currentInstr) {

        if (idEx.instruction == null) {
            return false;
        }

        String idExOpcode = idEx.instruction.getOpcode();

        // Check if instruction in EX stage is a load
        if (!idExOpcode.equals("LW")) {
            return false;
        }

        // Get the destination register of the load instruction
        int loadDestReg = idEx.rt; // LW writes to rt

        // Check if current instruction reads from the load destination
        int currentRs = currentInstr.getRs();
        int currentRt = currentInstr.getRt();

        // Check for data dependency
        if ((currentRs != 0 && currentRs == loadDestReg) ||
                (currentRt != 0 && currentRt == loadDestReg && !currentInstr.getOpcode().equals("SW"))) {
            System.out.println("  [HAZARD DETECTED] Load-Use on $" + loadDestReg + ": " + idExOpcode + " -> "
                    + currentInstr.getOpcode());
            return true;
        }

        return false;
    }

    /**
     * Detects general data hazards (RAW - Read After Write)
     */
    private boolean detectDataHazard(int sourceReg) {

        if (sourceReg == 0) {
            return false; // $0 is always 0, no hazard
        }

        // Check EX/MEM stage
        if (exMem.instruction != null) {
            control.decode(exMem.instruction.getOpcode());
            if (control.regWrite && exMem.writeRegister == sourceReg) {
                return true;
            }
        }

        // Check MEM/WB stage
        if (memWb.instruction != null) {
            control.decode(memWb.instruction.getOpcode());
            if (control.regWrite && memWb.writeRegister == sourceReg) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the forwarded value for a register if forwarding is possible
     * Implements EX-to-EX and MEM-to-EX forwarding
     * NOTE: Uses savedMemWb for MEM-to-EX forwarding since MEM stage runs before EX
     */
    private int getForwardedValue(int sourceReg, int originalValue) {

        if (sourceReg == 0) {
            return 0; // $0 is always 0
        }

        // EX-to-EX forwarding (from EX/MEM stage) - Higher priority
        if (exMem.instruction != null) {
            control.decode(exMem.instruction.getOpcode());
            if (control.regWrite && exMem.writeRegister == sourceReg) {
                // For load instructions, data isn't ready yet in EX/MEM, can't forward
                if (!exMem.instruction.getOpcode().equals("LW")) {
                    return exMem.aluResult;
                }
            }
        }

        // MEM-to-EX forwarding (from saved MEM/WB stage - before MEM stage overwrote
        // it)
        if (savedMemWb.instruction != null) {
            control.decode(savedMemWb.instruction.getOpcode());
            if (control.regWrite && savedMemWb.writeRegister == sourceReg) {
                int forwardedValue = control.memToReg ? savedMemWb.memData : savedMemWb.aluResult;
                return forwardedValue;
            }
        }

        return originalValue; // No forwarding needed
    }

    /**
     * Enable or disable hazard detection
     */
    public void setHazardDetection(boolean enable) {
        this.enableHazardDetection = enable;
    }

    /**
     * Enable or disable forwarding
     */
    public void setForwarding(boolean enable) {
        this.enableForwarding = enable;
    }

    /**
     * Get the stall count
     */
    public int getStallCount() {
        return stallCount;
    }

    /**
     * Run a single cycle of the pipeline (for GUI step mode)
     */
    public void runOneCycle() {
        if (halted)
            return;

        cycles++;
        System.out.println("\n--- Cycle " + cycles + " ---");

        // Save WB display BEFORE writeback consumes it
        displayWB = memWb.instruction;

        // Save memWb for forwarding before MEM stage overwrites it
        savedMemWb.instruction = memWb.instruction;
        savedMemWb.aluResult = memWb.aluResult;
        savedMemWb.memData = memWb.memData;
        savedMemWb.writeRegister = memWb.writeRegister;

        // Execute pipeline stages in CORRECT reverse order: WB -> MEM -> EX -> ID -> IF
        writeback();
        memoryAccess();
        execute();
        decode();
        fetch();

        // Save display state AFTER stages run (matches console output)
        // This correctly shows bubbles during stalls
        displayIF = ifId.instruction;
        displayID = idEx.instruction;
        displayEX = exMem.instruction;
        displayMEM = memWb.instruction;

        printPipelineState();

        // Check if pipeline is empty
        if (ifId.instruction == null && idEx.instruction == null &&
                exMem.instruction == null && memWb.instruction == null) {
            halted = true;
            System.out.println("\n=== Simulation Complete ===");
        }
    }

    /**
     * Check if simulation is halted
     */
    public boolean isHalted() {
        return halted;
    }

    /**
     * Get current cycle count
     */
    public int getCycles() {
        return cycles;
    }

    /**
     * Get pipeline state as string array for GUI display
     * Returns [IF, ID, EX, MEM, WB] with destination register for differentiation
     * Uses saved display values captured at correct points during cycle execution
     */
    public String[] getPipelineState() {
        return new String[] {
                getInstructionLabel(displayIF),
                getInstructionLabel(displayID),
                getInstructionLabel(displayEX),
                getInstructionLabel(displayMEM),
                getInstructionLabel(displayWB)
        };
    }

    /**
     * Helper method to create a label for an instruction with details
     * Shows opcode + destination register to differentiate similar instructions
     */
    private String getInstructionLabel(Instruction instr) {
        if (instr == null)
            return null;

        String opcode = instr.getOpcode();

        // For R-type instructions, show destination rd
        if (opcode.equals("ADD") || opcode.equals("SUB") || opcode.equals("AND") ||
                opcode.equals("OR") || opcode.equals("XOR") || opcode.equals("SLT") ||
                opcode.equals("SLL") || opcode.equals("SRL")) {
            return opcode + " $" + instr.getRd();
        }

        // For I-type with rt as destination (ADDI, ORI, LW)
        if (opcode.equals("ADDI") || opcode.equals("ORI") || opcode.equals("LW")) {
            return opcode + " $" + instr.getRt();
        }

        // For SW, show the source register being stored
        if (opcode.equals("SW")) {
            return opcode + " $" + instr.getRt();
        }

        // For branches, show compared registers
        if (opcode.equals("BEQ")) {
            return "BEQ $" + instr.getRs() + ",$" + instr.getRt();
        }
        if (opcode.equals("BGEZ")) {
            return "BGEZ $" + instr.getRs();
        }

        // For J, show target address
        if (opcode.equals("J")) {
            return "J " + instr.getAddress();
        }

        // Default: just opcode
        return opcode;
    }

}
