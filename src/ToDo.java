import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, PACKAGE, TYPE_PARAMETER, TYPE_USE})
@Retention(SOURCE)
@ToDo(
        aggiornatoDa = "Pietro versione 2.0 il 18/05"
)
public @interface ToDo {
    update daAggiornare() default update.TO_ADD_THINGS;
    enum update {ALGORITHM, DATA_STRUCTURE, TO_ADD_THINGS}

    String aggiornatoDa() default "Aggiornato da ? versione ? il ?/?";

    problem problema() default problem.TO_DISCUSS;
    enum problem {TO_DISCUSS, OBSOLETE, BUG, TEMPORARY}

    priority priorita() default priority.MEDIUM;
    enum priority {HIGH, MEDIUM, LOW}

    String altra_comunicazione() default "Scrivere il commento per questo metodo";
}