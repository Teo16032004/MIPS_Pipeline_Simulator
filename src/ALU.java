// ==================== ALU ====================

class ALU {

    public static final int ADD = 0;

    public static final int SUB = 1;

    public static final int AND = 2;

    public static final int OR = 3;

    public static final int XOR = 4;

    public static final int SLT = 5;

    public static final int SLL = 6;

    public static final int SRL = 7;

    

    private int result;

    private boolean zero;

    

    public int execute(int operation, int operand1, int operand2) {

        switch (operation) {

            case ADD:

                result = operand1 + operand2;

                break;

            case SUB:

                result = operand1 - operand2;

                break;

            case AND:

                result = operand1 & operand2;

                break;

            case OR:

                result = operand1 | operand2;

                break;

            case XOR:

                result = operand1 ^ operand2;

                break;

            case SLT:

                result = (operand1 < operand2) ? 1 : 0;

                break;

            case SLL:

                result = operand1 << operand2;

                break;

            case SRL:

                result = operand1 >>> operand2;

                break;

            default:

                throw new IllegalArgumentException("Unknown ALU operation: " + operation);

        }

        zero = (result == 0);

        return result;

    }

    

    public boolean isZero() {

        return zero;

    }

    

    public int getResult() {

        return result;

    }

}


