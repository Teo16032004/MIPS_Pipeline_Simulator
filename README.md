# MIPS Pipeline Simulator

A Java-based educational simulator for the MIPS pipeline architecture with hazard detection, forwarding, and a graphical user interface.

## Features

### Pipeline Simulation
- **5-stage pipeline**: IF → ID → EX → MEM → WB
- Cycle-by-cycle execution with pipeline register visualization
- Support for 32 MIPS registers and 4KB memory

### Hazard Detection & Resolution

#### Data Hazards (RAW - Read After Write)
- **Forwarding (Bypassing)**: EX-to-EX and MEM-to-EX forwarding paths
- Automatic detection and resolution without stalls when possible

#### Load-Use Hazards
- Automatic detection when instruction needs LW result immediately
- Pipeline stalling with bubble insertion
- Stall count tracking

#### Control Hazards
- **BEQ** (Branch on Equal): Conditional branching
- **BGEZ** (Branch on Greater/Equal Zero): Conditional branching
- **J** (Jump): Unconditional jump
- Pipeline flushing when branch/jump is taken

### Graphical User Interface
- Dark theme with modern styling
- Program input area with comment support
- Real-time pipeline stage visualization
- Register and memory tables with hex/decimal display
- Step-by-step or continuous execution
- Cycle and stall count tracking

## Supported Instructions

| Type | Instructions |
|------|-------------|
| **R-Type** | ADD, SUB, AND, OR, XOR, SLT, SLL, SRL |
| **I-Type** | ADDI, ORI, LW, SW, BEQ, BGEZ |
| **J-Type** | J |

## Quick Start

### Compile
```bash
cd src
javac *.java
```

### Run GUI
```bash
java MIPSSimulatorGUI
```

### Run Console Tests
```bash
java MIPSTest
```

## Example Program

```assembly
# Data hazard example with forwarding
ADDI $1, $0, 10
ADDI $2, $0, 20
ADD $3, $1, $2
SUB $4, $3, $1
# Memory operations with load-use hazard
SW $3, 0($0)
LW $5, 0($0)
ADD $6, $5, $1
```

## Project Structure

```
src/
├── MIPSSimulator.java      # Main simulator with pipeline stages
├── MIPSSimulatorGUI.java   # Swing GUI interface
├── MIPSTest.java           # Test cases
├── ALU.java                # Arithmetic Logic Unit
├── ControlUnit.java        # Control signal generation
├── Instruction.java        # Instruction representation
├── InstructionParser.java  # Assembly parser
├── RegisterFile.java       # 32 MIPS registers
├── Memory.java             # Data memory
├── IF_ID_Register.java     # Pipeline register
├── ID_EX_Register.java     # Pipeline register (with control signals)
├── EX_MEM_Register.java    # Pipeline register
└── MEM_WB_Register.java    # Pipeline register
```

## Technical Details

### Forwarding Logic
- **EX-to-EX**: Forwards ALU result from EX/MEM to current EX stage
- **MEM-to-EX**: Forwards data from MEM/WB to current EX stage
- Priority: EX-to-EX > MEM-to-EX

### Stalling Logic
- Detects load-use hazards in ID stage
- Inserts bubble (clears ID/EX register)
- Decrements PC to re-fetch stalled instruction

### Control Hazard Handling
- Branch/jump evaluated in EX stage
- Pipeline flush: clears IF/ID register
- PC updated to branch target

## Author

Teodor Suteu - Technical University of Cluj-Napoca
