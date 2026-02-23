// ==================== REGISTER FILE ====================

class RegisterFile {

    private int[] registers;

    private static final int NUM_REGISTERS = 32;

    

    public RegisterFile() {

        registers = new int[NUM_REGISTERS];

        registers[0] = 0; // $zero always 0

    }

    

    public int read(int regNum) {

        if (regNum < 0 || regNum >= NUM_REGISTERS) {

            throw new IllegalArgumentException("Invalid register number: " + regNum);

        }

        return registers[regNum];

    }

    

    public void write(int regNum, int value) {

        if (regNum < 0 || regNum >= NUM_REGISTERS) {

            throw new IllegalArgumentException("Invalid register number: " + regNum);

        }

        if (regNum != 0) { // Cannot write to $zero

            registers[regNum] = value;

        }

    }

    

    public void printRegisters() {

        System.out.println("\n=== Register File ===");

        for (int i = 0; i < NUM_REGISTERS; i++) {

            if (registers[i] != 0) {

                System.out.printf("$%-2d: %d (0x%08X)\n", i, registers[i], registers[i]);

            }

        }

    }

}


