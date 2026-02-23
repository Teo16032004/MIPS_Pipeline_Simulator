    // ==================== MEMORY ====================

    class Memory {

        private int[] memory;

        private static final int MEMORY_SIZE = 4096; // 4KB for demo



        public Memory() {

            memory = new int[MEMORY_SIZE];

        }



        public int load(int address) {

            int index = address / 4; // Word-aligned

            if (index < 0 || index >= MEMORY_SIZE) {

                throw new IllegalArgumentException("Memory address out of bounds: " + address);

            }

            return memory[index];

        }



        public void store(int address, int value) {

            int index = address / 4; // Word-aligned

            if (index < 0 || index >= MEMORY_SIZE) {

                throw new IllegalArgumentException("Memory address out of bounds: " + address);

            }

            memory[index] = value;

        }



        public void printMemory(int start, int end) {

            System.out.println("\n=== Memory Contents ===");

            for (int i = start; i <= end; i += 4) {

                int value = load(i);

                if (value != 0) {

                    System.out.printf("Addr %d: %d (0x%08X)\n", i, value, value);

                }

            }

        }

    }


