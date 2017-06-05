public class ImmutableFieldSample {

    // should add final when it is not modified from method
    public static class FooA1 {
        private String name = "bar";
        public String getName() {
            return name;
        }
    }

    // should add final when it is not modified from method,
    public static class FooUnaryOpUsageA2 {
        private int count = 0;
        public int increment() {
            return -count;
        }
    }

    // should not add final when it is modified from method with this (1)
    public static class FooB1 {
        private String name = "bar";
        public void setName(String x) {
            this.name = x;
        }
    }

    // should not add final when it is modified from method with this (2)
    public static class FooB2 {
        private String name = null;
        public void setName(String x) {
            this.name = x;
        }
    }

    // should not add final when it is modified from method without this (1)
    public static class FooC1 {
        private String name = "bar";
        public void setName(String x) {
            name = x;
        }
    }

    // should not add final when it is modified from method without this (2)
    public static class FooC2 {
        private String name = null;
        public void setName(String x) {
            name = x;
        }
    }

    // should not add final when it is modified from method without this (3)
    public static class FooUnaryOpUsageC3 {
        private int count = 0;
        public void increment() {
            count++;
        }
    }

    // should not add final when it is set from foreign constructor only
    public static class FooToForeignConstructorC4 {
        private String s;
        public class Inner {
            public Inner() {
                s = "a";
            }
        }
    }

    // should not add final when it is set from constructor but has initial value (TODO for default inits)
    public static class FooToDo1 {
        private int count = 0;
        public FooToDo1() {
            count = 1;
        }
    }
}
