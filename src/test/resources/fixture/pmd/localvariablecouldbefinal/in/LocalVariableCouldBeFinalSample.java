public class LocalVariableCouldBeFinalSample {

    public void unusedVarsAreFinal(Object c) {
        String a = "a";
    }

    public void assignedVarsAreNotFinal(Object c) {
        String a = "a";
        a = "b";
    }

    public void varsBeingAssignedAreFinal(Object c) {
        String a = "a";
        c = a;
    }

    public int readOnlyUsageIsFinal() {
        int count = 0;
        return -count;
    }

    public void postIncrementIsNotFinal() {
        int count = 0;
        count++;
    }

    public void preIncrementIsNotFinal() {
        int count = 0;
        ++count;
    }

    public void postDecrementIsNotFinal() {
        int count = 0;
        count--;
    }

    public void preDecrementIsNotFinal() {
        int count = 0;
        --count;
    }
}
