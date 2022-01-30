import java.awt.Color;

public class Personalita1 implements Personality {

    //Caratteristiche personalità :
    // Molto poco promisqua
    // Se ci sono suoi simili è positiva, altrimenti negativa
    //Mangia poco e produce cibo senza sprecarlo, Ambientalista

    private int x_position, y_position; // posizione che occupa nell'arena
    private Giocatore giocatore;
    private Color colore = new Color(255, 0, 0);
    static int born, dead;
    static int totalborn, totaldead;
    private int meet_counter;
    public final String PERSONALITY = "Personalita1";


    /**
     * Costruttore primario
     * serve a richiamare il costruttore di Giocatore dato che deve istanziare
     * la posizione nell'arena del carattere associato al giocatore
     *
     * @param y : indice di riga
     * @param x : indice di colonna
     */
    public Personalita1(int y, int x, Giocatore giocatore) {
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
    public String getMyPersonality() { return this.PERSONALITY;}


    @Override
    public int getMyMessage() {
        return 1;
    }


    //Più il numero è grande e meno sono promisquo
    @Override
    public int getMyPromiscuity() {
        return 4;
    }


    /**
     * Reagisce al messaggio ricevuto che a seconda della personalità influenza salute, benessere e alri fattori da decidere in seguito
     * @param mess, indPers: il messaggio e l'indice della persona che l'ha inviato
     * @return il messaggio da inviare
     */
    @Override
    public int react(int mess, int indPers, int mode) {
//        System.out.println("Vita: "+ giocatore.getLife() + "Wellness: " + giocatore.getWellness());
        if (mode == 0) {
            if (giocatore.numPersonality("Personalita1") > 4) {
                //comportamento positivo
                switch (mess) {
                    //Riceve un abbraccio da un suo simile
                    case 1: {
                        giocatore.increaseWellness(1);
                        return 1;
                    }
                    //Ringiovanisce grazie ad dieta che gli hanno consigliato
                    case 2: {
                        giocatore.increaseWellness(1);
                        return 2;
                    }
                    //Scopre la sua ragazza con un altro
                    case 3: {
                        giocatore.decreaseWellness(1);
                        return 3;
                    }

                    //Reputa il messaggio come una ruffianata, gli piace e manda segnali  positivi
                    case 4: {
                        giocatore.increaseWellness(2);
                        return 6;
                    }

                    //Tranquille chiacchere tra amici
                    case 5: {
                        giocatore.increaseWellness(1);
                        return 5;
                    }

                    //Il giocatore con personalità "" reputa il messaggio 6 come una scocciatura, ma gli piace essere adulato cerca di liberarsene
                    case 6: {
                        giocatore.increaseWellness(3);
                        return 3;
                    }
                }
            } else {
                //comportamento negativo
                switch (mess) {
                    //Il giocatore con personalità "" reputa il messaggio 1 come un insulto debilitante rivolto alla sua persona, si deprime.
                    case 1: {
                        giocatore.decreaseWellness(1);
                        return 1;
                    }
                    //Il giocatore con personalità "" reputa il messaggio 2 come una presa in giro, risponde con un'offesa grave perchè è molto permaloso
                    case 2: {
                        giocatore.acquaintances[indPers].decreaseWellness(1);
                        return 1;
                    }
                    case 3: {
                        giocatore.decreaseWellness(1);
                        return 4;
                    }
                    case 4: {
                        giocatore.decreaseWellness(1);
                        return 2;
                    }
                    case 5: {
                        giocatore.increaseWellness(2);
                        giocatore.acquaintances[indPers].decreaseWellness(2);
                        return 6;
                    }
                    case 6: {
                        giocatore.decreaseWellness(1);
                        return 5;
                    }
                }
            }
        }
        else {
            Personality acquaintance = giocatore.acquaintances[indPers].carattere;
            if (acquaintance instanceof Personalita1) {
                giocatore.increaseWellness(0.5);
                return 1;
            }
            else if (acquaintance instanceof Personalita2) {
                giocatore.decreaseWellness(0.5);
                return 1;
            }
            else if (acquaintance instanceof Personalita3) {
                giocatore.increaseWellness(0.2);
                return 1;
            }
            else if (acquaintance instanceof Personalita4) {
                giocatore.decreaseWellness(0.2);
                return 1;
            }
        }
        //In caso di errore invia 0, come se non avesse parlato
        return 0;
    }


    @Override
    public int eat() {
        return 1;
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