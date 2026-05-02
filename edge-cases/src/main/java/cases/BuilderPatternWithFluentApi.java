package cases;

public final class BuilderPatternWithFluentApi {
    private final String host;
    private final int port;
    private final boolean tlsEnabled;
    private final int timeoutMillis;

    private BuilderPatternWithFluentApi(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.tlsEnabled = builder.tlsEnabled;
        this.timeoutMillis = builder.timeoutMillis;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public boolean tlsEnabled() {
        return tlsEnabled;
    }

    public int timeoutMillis() {
        return timeoutMillis;
    }

    public static Builder builder(String host) {
        return new Builder(host);
    }

    public static final class Builder {
        private final String host;
        private int port = 80;
        private boolean tlsEnabled;
        private int timeoutMillis = 1000;

        private Builder(String host) {
            this.host = host;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder tlsEnabled(boolean tlsEnabled) {
            this.tlsEnabled = tlsEnabled;
            return this;
        }

        public Builder timeoutMillis(int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        public BuilderPatternWithFluentApi build() {
            if (host == null || host.trim().isEmpty()) {
                throw new IllegalStateException("host must not be blank");
            }
            if (port <= 0 || port > 65535) {
                throw new IllegalStateException("port out of range: " + port);
            }
            if (timeoutMillis <= 0) {
                throw new IllegalStateException("timeoutMillis must be positive");
            }
            return new BuilderPatternWithFluentApi(this);
        }
    }
}
