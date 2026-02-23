    ```mermaid
    classDiagram
        direction TB
        
        %% Main Simulator Class
        class MIPSSimulator {
            -RegisterFile regFile
            -Memory memory
            -ALU alu
            -ControlUnit control
            -IF_ID_Register ifId
            -ID_EX_Register idEx
            -EX_MEM_Register exMem
            -MEM_WB_Register memWb
            -int pc
            -int cycles
            -int stallCount
            -boolean halted
            +loadProgram(String[] instructions)
            +run()
            +runOneCycle()
            +isHalted() boolean
            +getCycles() int
            +getStallCount() int
            +getPipelineState() String[]
            -fetch()
            -decode()
            -execute()
            -memoryAccess()
            -writeback()
            -detectLoadUseHazard(Instruction) boolean
            -getForwardedValue(int, int) int
        }
        
        %% Hardware Components
        class ALU {
            +ADD : int
            +SUB : int
            +AND : int
            +OR : int
            +XOR : int
            +SLT : int
            +SLL : int
            +SRL : int
            -result : int
            -zero : boolean
            +execute(int op, int op1, int op2) int
            +isZero() boolean
            +getResult() int
        }
        
        class ControlUnit {
            +regWrite : boolean
            +memRead : boolean
            +memWrite : boolean
            +branch : boolean
            +aluSrc : boolean
            +regDst : boolean
            +memToReg : boolean
            +jump : boolean
            +aluOp : int
            +decode(String opcode)
            +getALUControl(String opcode) int
        }
        
        class RegisterFile {
            -registers : int[]
            -NUM_REGISTERS : int
            +read(int regNum) int
            +write(int regNum, int value)
            +printRegisters()
        }
        
        class Memory {
            -memory : int[]
            -MEMORY_SIZE : int
            +load(int address) int
            +store(int address, int value)
            +printMemory(int start, int end)
        }
        
        %% Instruction Classes
        class Instruction {
            -opcode : String
            -type : String
            -rs : int
            -rt : int
            -rd : int
            -immediate : int
            -shamt : int
            -address : int
            +getOpcode() String
            +getType() String
            +getRs() int
            +getRt() int
            +getRd() int
            +getImmediate() int
            +setRs(int)
            +setRt(int)
            +setRd(int)
        }
        
        class InstructionParser {
            +parse(String line)$ Instruction
            -parseRegister(String reg)$ int
        }
        
        %% Pipeline Registers
        class IF_ID_Register {
            +instruction : Instruction
            +pc : int
            +clear()
        }
        
        class ID_EX_Register {
            +instruction : Instruction
            +pc : int
            +readData1 : int
            +readData2 : int
            +immediate : int
            +rs : int
            +rt : int
            +rd : int
            +regWrite : boolean
            +branch : boolean
            +jump : boolean
            +clear()
        }
        
        class EX_MEM_Register {
            +instruction : Instruction
            +aluResult : int
            +readData2 : int
            +writeRegister : int
            +zero : boolean
            +branchTarget : int
            +clear()
        }
        
        class MEM_WB_Register {
            +instruction : Instruction
            +aluResult : int
            +memData : int
            +writeRegister : int
            +clear()
        }
        
        %% GUI Class
        class MIPSSimulatorGUI {
            -simulator : MIPSSimulator
            -programInput : JTextArea
            -logArea : JTextArea
            -registerTable : JTable
            -memoryTable : JTable
            -pipelineLabels : JLabel[]
            +loadProgram()
            +stepExecution()
            +runAll()
            +reset()
            +updateDisplay()
        }
        
        %% Relationships
        MIPSSimulator --> RegisterFile : uses
        MIPSSimulator --> Memory : uses
        MIPSSimulator --> ALU : uses
        MIPSSimulator --> ControlUnit : uses
        MIPSSimulator --> IF_ID_Register : contains
        MIPSSimulator --> ID_EX_Register : contains
        MIPSSimulator --> EX_MEM_Register : contains
        MIPSSimulator --> MEM_WB_Register : contains
        
        MIPSSimulator ..> InstructionParser : uses
        MIPSSimulator ..> Instruction : processes
        
        IF_ID_Register --> Instruction : holds
        ID_EX_Register --> Instruction : holds
        EX_MEM_Register --> Instruction : holds
        MEM_WB_Register --> Instruction : holds
        
        InstructionParser ..> Instruction : creates
        
        MIPSSimulatorGUI --> MIPSSimulator : controls
        
        ControlUnit ..> ALU : configures
    ```
