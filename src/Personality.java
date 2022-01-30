import java.awt.Color;

public interface Personality{

    int MEET = 5;


    /**
     * @return il colore associato alla perosnalità
     */
    Color getMyColor();


    /**
     * a seconda dei messaggi ricevuti e della personalità risponde e viene influenzato nei suoi parametri
     * 6 messaggi per 6 personalità
     * Questa funzione risponde ad ogni messaggio nella rete dei conoscenti basandosi sul fatto che
     * il giocatore in posizione i degli acquaintances, mi può mandare un messaggio SOLO in posizione i (dell'array dei messaggi)
     */
    default void act(Giocatore me) {
        for (int iA = 1; iA < me.messageReceived.length; iA++) { // per ogni messaggio
            if (me.acquaintances[iA] != null && me.acquaintances[iA].getLife() > 0) {
                if (me.messageReceived[iA] == 0)  // 0 = non ho ricevuto messaggi, mando il messaggio di default
                    me.acquaintances[iA].setMessageReceived(getMyMessage(), me);
                else
                    me.acquaintances[iA].setMessageReceived(react(me.getMessageReceived(iA), iA, 1), me); // a seconda del messaggio avrò una diversa reazione dipendente della personalità
            }
        }
    }


    /**
     * funzione ausiliaria ad act, varia per ogni personalità
     * @param mess il messaggio ricevuto da un mio acquaintance
     * @param indPers l'indice della posizione dell'acquintance nel mio array di acquaintances che mi ha inviato il messaggio
     * @param mode viene modificato in {@see act} nel corpo del secondo {@code if}.
     *             determina il modo in cui i giocatori si scambiano i messaggi:
     *             mode 0: 12 tipi di messaggi diversi codificati da 1 a 6 (per due volte). Tutte le personalità
     *             sviluppano 6 tipi di messaggi diversi in funzione di un unico tipo specifico di carattere da cui
     *             li ho ricevuti e altri 6 per tutte le altre;
     *             mode 1: 8 tipi di messaggi diversi codificati con 0 o 1 (per quattro volte). Tutte le personalità
     *             sviluppano 2 tipi di risposte diverse ai messaggi ricevuti  in funzione di tutte e quattro
     *             le personalitè in gioco.
     * @return il messaggio di risposta codificato diversamente in ogni diversa modalità
     */
    int react(int mess, int indPers, int mode);


    /**
     * @return Il messaggio che ogni classe manda di default, potrebbe anche cambiare in seguito
     */
    int getMyMessage();


    /**
     * @return La mia promisquità (con quale persona che ho incontrato faccio figli
     * es: getMyPromisquity=2 -> farò un figlio con la seconda persona nei miei conoscenti )
     */
    int getMyPromiscuity();


    /**
     * @return la stringa contenente la personalità
     */
    String getMyPersonality();


    /**
     * @return la quantità di cibo mangiato
     */
    int eat();


    /**
     * @return la quantità di cibo prodotto (valore positivo)
     * o la quantità di cibo sprecato (valore negativo)
     */
    int produceOrWaste();


    /**
     * incrementa il contatore del numero di nati nell'anno presente e quello storico
     */
    void newborn();


    /**
     * incrementa il contatore del numero di morti nell'anno presente e quello storico
     */
    void dead();


    /**
     * diminuisce il valore di meet_counter ogni volta che un giocatore prova a conoscere una nuova persona
     */
    void decreaseMeetCounter();


    /**
     * resetta il meet_counter al valore statico e finale di MEET
     */
    void setMeetCounter();


    /**
     * @return il valore di meet_counter
     */
    int getMeetCounter();
}