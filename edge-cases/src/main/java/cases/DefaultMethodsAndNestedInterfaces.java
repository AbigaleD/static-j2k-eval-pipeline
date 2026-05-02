package cases;

public interface DefaultMethodsAndNestedInterfaces {
    String name();

    default String describe() {
        return "handler:" + name();
    }

    default boolean accepts(String input) {
        return input != null && input.startsWith(name());
    }

    interface Matcher {
        boolean matches(String value);

        default Matcher and(final Matcher other) {
            return new Matcher() {
                @Override
                public boolean matches(String value) {
                    return Matcher.this.matches(value) && other.matches(value);
                }
            };
        }
    }

    static class PrefixHandler implements DefaultMethodsAndNestedInterfaces, Matcher {
        private final String prefix;

        PrefixHandler(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String name() {
            return prefix;
        }

        @Override
        public boolean matches(String value) {
            return accepts(value);
        }
    }
}
