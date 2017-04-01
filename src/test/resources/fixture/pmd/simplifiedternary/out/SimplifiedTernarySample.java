public class SimplifiedTernarySample {
    public boolean p2() {
        return false;
    }

    public boolean p3() {
        return false;
    }

    public boolean bar1(boolean condition) {
        return condition || p3();
    }

    public boolean bar1a(boolean condition) {
        return condition || p3();
    }

    public boolean bar2(boolean condition) {
        return !condition || p2();
    }

    public boolean bar2a(boolean condition) {
        return !condition || p2();
    }

    public boolean bar3(boolean condition) {
        return !condition || p3();
    }

    public boolean bar4(boolean condition) {
        return condition && p2();
    }

    public boolean bar5(boolean condition) {
        return condition;
    }

    public boolean bar6(boolean condition) {
        return !condition;
    }

    public String f1(boolean condition) {
        return condition ? "A" + "B" : "C" + "D";
    }

    public String f1a(boolean condition) {
        return condition ? ("A" + "B") : ("C" + "D");
    }
}
