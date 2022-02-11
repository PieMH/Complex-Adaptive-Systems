import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, PACKAGE, TYPE_PARAMETER, TYPE_USE})
@Retention(SOURCE)
@ToDo(
        updatedBy = "Pietro on 11/02/2022 version 2.1"
)
public @interface ToDo {
    String toUpdate() default "Describe what to update";

    String updatedBy() default "<user> on dd/mm/aaaa version <n.n>";

    String problem() default "Describe the problem";

    priorityLevel priority() default priorityLevel.MEDIUM;
    enum priorityLevel {HIGH, MEDIUM, LOW}

    String other() default "Write a comment here";
}