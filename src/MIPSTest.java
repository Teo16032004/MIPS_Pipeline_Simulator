// ==================== MAIN TEST CLASS ====================

class MIPSTest {

    public static void main(String[] args) {

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     MIPS PIPELINE SIMULATOR - HAZARD DETECTION TESTS      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Run all test cases
        testDataHazardWithForwarding();
        testLoadUseHazard();
        testComplexHazards();
        testBranchHazard();
        testJumpHazard();
        testWithoutHazardDetection();
    }

    /**
     * Test 1: Data hazards with forwarding
     * Demonstrates RAW hazards that can be resolved with forwarding
     */
    private static void testDataHazardWithForwarding() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 1: Data Hazards with Forwarding");
        System.out.println("=".repeat(60));
        System.out.println("This test demonstrates RAW (Read After Write) hazards");
        System.out.println("that are resolved using forwarding paths.\n");

        MIPSSimulator sim = new MIPSSimulator();

        // Initialize registers
        sim.getRegFile().write(1, 10);
        sim.getRegFile().write(2, 20);

        // Program with data hazards
        String[] program = {
                "ADD $3, $1, $2", // $3 = 10 + 20 = 30
                "SUB $4, $3, $1", // $4 = 30 - 10 = 20 (needs $3 from previous instruction)
                "ADD $5, $4, $3", // $5 = 20 + 30 = 50 (needs $4 and $3)
                "AND $6, $5, $2" // $6 = 50 & 20 = 16 (needs $5)
        };

        sim.loadProgram(program);
        sim.run();

        System.out.println("\n✓ Expected: Forwarding should handle all hazards without stalls");
        System.out.println("✓ Verify: $3=30, $4=20, $5=50, $6=16");
    }

    /**
     * Test 2: Load-use hazard
     * Demonstrates hazards that require stalling (cannot be resolved by forwarding
     * alone)
     */
    private static void testLoadUseHazard() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 2: Load-Use Hazard (Requires Stall)");
        System.out.println("=".repeat(60));
        System.out.println("This test demonstrates load-use hazards that require");
        System.out.println("pipeline stalls because data isn't available in time.\n");

        MIPSSimulator sim = new MIPSSimulator();

        // Initialize registers and memory
        sim.getRegFile().write(1, 0); // Base address
        sim.getMemory().store(0, 100); // Memory[0] = 100
        sim.getMemory().store(4, 200); // Memory[4] = 200

        // Program with load-use hazard
        String[] program = {
                "LW $2, 0($1)", // $2 = Memory[0] = 100
                "ADD $3, $2, $1", // $3 = 100 + 0 = 100 (HAZARD: needs $2 immediately)
                "LW $4, 4($1)", // $4 = Memory[4] = 200
                "SUB $5, $4, $2" // $5 = 200 - 100 = 100 (HAZARD: needs $4)
        };

        sim.loadProgram(program);
        sim.run();

        System.out.println("\n✓ Expected: 2 stalls (one for each load-use hazard)");
        System.out.println("✓ Verify: $2=100, $3=100, $4=200, $5=100");
        System.out.println("✓ Stall count should be: " + sim.getStallCount());
    }

    /**
     * Test 3: Complex hazards
     * Combination of different hazard types
     */
    private static void testComplexHazards() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 3: Complex Hazards (Mixed Types)");
        System.out.println("=".repeat(60));
        System.out.println("This test combines load-use hazards and data hazards.\n");

        MIPSSimulator sim = new MIPSSimulator();

        // Initialize
        sim.getRegFile().write(1, 5);
        sim.getRegFile().write(2, 10);
        sim.getMemory().store(20, 50);

        // Complex program
        String[] program = {
                "ADD $3, $1, $2", // $3 = 5 + 10 = 15
                "ADDI $4, $1, 15", // $4 = 5 + 15 = 20
                "LW $5, 0($4)", // $5 = Memory[20] = 50 (uses $4 from previous)
                "ADD $6, $5, $3", // $6 = 50 + 15 = 65 (HAZARD: load-use on $5)
                "SLL $7, $6, 1", // $7 = 65 << 1 = 130 (data hazard on $6)
                "SUB $8, $7, $5" // $8 = 130 - 50 = 80 (data hazard on $7)
        };

        sim.loadProgram(program);
        sim.run();

        System.out.println("\n✓ Expected: 1 stall (for load-use hazard on $5)");
        System.out.println("✓ Verify: $3=15, $4=20, $5=50, $6=65, $7=130, $8=80");
        System.out.println("✓ Stall count: " + sim.getStallCount());
    }

    /**
     * Test 4: Without hazard detection (for comparison)
     * Shows what happens when hazard detection is disabled
     */
    private static void testWithoutHazardDetection() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 4: Simulation WITHOUT Hazard Detection");
        System.out.println("=".repeat(60));
        System.out.println("This test shows incorrect results when hazard detection");
        System.out.println("and forwarding are disabled (for educational comparison).\n");

        MIPSSimulator sim = new MIPSSimulator();
        sim.setHazardDetection(false);
        sim.setForwarding(false);

        // Initialize
        sim.getRegFile().write(1, 10);
        sim.getRegFile().write(2, 20);

        // Simple program with hazards
        String[] program = {
                "ADD $3, $1, $2", // $3 = 30
                "SUB $4, $3, $1" // $4 should be 20, but will be wrong without forwarding
        };

        sim.loadProgram(program);
        sim.run();

        System.out.println("\n⚠ WARNING: Results will be INCORRECT without hazard handling!");
        System.out.println("⚠ $4 will likely be wrong (using old $3 value = 0)");
        System.out.println("⚠ This demonstrates why hazard detection is essential!");
    }

    /**
     * Test 5: BEQ Branch Hazard
     * Demonstrates control hazards with conditional branches
     */
    private static void testBranchHazard() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 5: BEQ Control Hazard");
        System.out.println("=".repeat(60));
        System.out.println("This test demonstrates control hazards with BEQ instruction.\n");

        MIPSSimulator sim = new MIPSSimulator();

        // Program with BEQ branch
        // Branch offset is relative to PC+1, so offset=1 skips 1 instruction
        String[] program = {
                "ADDI $1, $0, 5", // Index 0: $1 = 5
                "ADDI $2, $0, 5", // Index 1: $2 = 5
                "BEQ $1, $2, 1", // Index 2: Branch to (2+1)+1 = 4 (skip index 3)
                "ADDI $3, $0, 100", // Index 3: Should be SKIPPED (flushed)
                "ADDI $4, $0, 200", // Index 4: Branch target - $4 = 200
                "ADDI $5, $0, 999" // Index 5: $5 = 999
        };

        sim.loadProgram(program);
        sim.run();

        System.out.println("\n✓ Expected: BEQ should branch to index 4 (skip instruction at index 3)");
        System.out.println("✓ Verify: $1=5, $2=5, $3=0, $4=200, $5=999");
    }

    /**
     * Test 6: Jump Hazard
     * Demonstrates control hazards with unconditional jump
     */
    private static void testJumpHazard() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 6: J (Jump) Control Hazard");
        System.out.println("=".repeat(60));
        System.out.println("This test demonstrates control hazards with J instruction.\n");

        MIPSSimulator sim = new MIPSSimulator();

        // Program with J jump - J jumps directly to the specified instruction index
        String[] program = {
                "ADDI $1, $0, 10", // Index 0: $1 = 10
                "J 3", // Index 1: Jump to index 3 (skip index 2)
                "ADDI $2, $0, 20", // Index 2: Should be SKIPPED (flushed)
                "ADDI $3, $0, 30", // Index 3: Jump target - $3 = 30
                "ADDI $4, $0, 40" // Index 4: $4 = 40
        };

        sim.loadProgram(program);
        sim.run();

        System.out.println("\n✓ Expected: J should jump to index 3 (skip instruction at index 2)");
        System.out.println("✓ Verify: $1=10, $2=0, $3=30, $4=40");
    }

}
