package cases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@AnnotationDenseCommand.Command(name = "copy", aliases = {"cp", "duplicate"})
public class AnnotationDenseCommand implements Runnable {
    @Option(names = {"-f", "--force"}, defaultValue = "false")
    private boolean force;

    @Override
    public void run() {
        System.out.println(force ? "forcing" : "dry-run");
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Command {
        String name();
        String[] aliases() default {};
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Option {
        String[] names();
        String defaultValue() default "";
    }
}
