package SGS;

import java.awt.Color;

// 6 messaggi per 6 personalità

/**
 * Poco promiscua
 * Se ci sono molti di Personalità 1 ma pochi di Personalità 2 è ultra positiva
 * ma va bene anche se non ci sono
 * Capitalista fedele, mangia tanto e ha tanti consumi, produce cibo ma solo per se stesso o per trarne profitto
 */
public class Personalita4 implements Personality {

    private final int x_position;   // posizione che occupa nell'arena
    private final int y_position;
    private final Giocatore giocatore;
    private final Color colore = new Color(250, 100, 200);
    static int born, dead;
    static int totalborn, totaldead;
    private int meet_counter;
    public final String personality = "SGS.Personalita4";

    /**
     * Costruttore primario
     * serve a richiamare il costruttore di SGS.Giocatore dato che deve istanziare
     * la posizione nell'arena del carattere associato al giocatore
     * @param x indice di riga
     * @param y indice di colonna
     */
    public Personalita4(int y, int x, Giocatore giocatore) {
        this.x_position = x;
        this.y_position = y;
        this.giocatore = giocatore;
        meet_counter = MEET;
    }

    @Override
    public Color getMyColor() {
        return this.colore;
    }

    @Override
    public String getMyPersonality(){ return this.personality;}

    @Override
    //il messaggio che mando al mondo di default
    public int getMyMessage(){
        return 4;
    }

    //dopo quanti incontri (sempre > 1) posso fare figli
    @Override
    public int getMyPromiscuity(){
        return 5;
    }

    /**
     * Reagisce al messaggio ricevuto che a seconda della personalità influenza salute, benessere e altri fattori da decidere in seguito
     * @param mess, indPers: il messaggio e l'indice della persona che l'ha inviato
     * @return il messaggio da inviare
     */
    @Override
    public int react(int mess, int indPers, int mode) {
        if (mode == 0) {
            if (giocatore.numPersonality("SGS.Personalita4") > 4) {
                // comportamento positivo
                switch (mess) {
                    // Riceve un abbraccio da un suo simile
                    case 1 -> {
                        giocatore.increaseWellness(0.5);
                        return 1;
                    }
                    // Ringiovanisce grazie ad dieta che gli hanno consigliato
                    case 2 -> {
                        giocatore.increaseWellness(1.5);
                        return 1;
                    }
                    // Scopre la sua ragazza con un altro
                    case 3 -> {
                        giocatore.decreaseWellness(1);
                        return 2;
                    }
                    // Reputa il messaggio come una ruffianata, gli piace e manda segnali positivi
                    case 4 -> {
                        giocatore.increaseWellness(0.5);
                        return 6;
                    }
                    // Tranquille chiacchiere tra amici
                    case 5 -> {
                        giocatore.increaseWellness(1);
                        return 5;
                    }
                    // Il giocatore con personalità "" reputa il messaggio 6 come una scocciatura, ma gli piace essere adulato cerca di liberarsene
                    case 6 -> {
                        giocatore.increaseWellness(1);
                        return 3;
                    }
                }
            } else {
                // comportamento negativo
                switch (mess) {
                    // Il giocatore con personalità "" reputa il messaggio 1 come un insulto debilitante rivolto alla sua persona, si deprime.
                    case 1 -> {
                        giocatore.increaseWellness(1.5);
                        return 1;
                    }
                    // Il giocatore con personalità "" reputa il messaggio 2 come una presa in giro, risponde con un'offesa grave perchè è molto permaloso
                    case 2 -> {
                        giocatore.acquaintances[indPers].decreaseWellness(1.5);
                        return 1;
                    }
                    case 3 -> {
                        giocatore.increaseWellness(1);
                        return 2;
                    }
                    case 4 -> {
                        giocatore.increaseWellness(0.5);
                        return 6;
                    }
                    case 5 -> {
                        giocatore.increaseWellness(1);
                        giocatore.acquaintances[indPers].increaseWellness(2);
                        return 4;
                    }
                    case 6 -> {
                        giocatore.increaseWellness(1);
                        return 5;
                    }
                }
            }
        }
        else {
            Personality acquaintance = giocatore.acquaintances[indPers].carattere;
            if (acquaintance instanceof Personalita1) {
                giocatore.increaseWellness(0.2);
                return -1;
            }
            else if (acquaintance instanceof Personalita2) {
                giocatore.decreaseWellness(0.5);
                return -1;
            }
            else if (acquaintance instanceof Personalita3) {
                giocatore.increaseWellness(0.2);
                return -1;
            }
            else if (acquaintance instanceof Personalita4) {
                giocatore.increaseWellness(0.5);
                return 1;
            }
        }
        // In caso di errore invia 0, come se non avesse parlato
        return 0;
    }

    @Override
    public int eat() {
        return 2;
    }

    @Override
    public int produceOrWaste() {
        return 2;
    }

    @Override
    public void newborn() {
        born++;
        totalborn++;
    }

    @Override
    public void dead() {
        dead++;
        totaldead++;
    }

    @Override
    public void decreaseMeetCounter() {
        meet_counter--;
    }

    @Override
    public int getMeetCounter() {
        return meet_counter;
    }

    @Override
    public void setMeetCounter() {
        meet_counter = MEET;
    }
}