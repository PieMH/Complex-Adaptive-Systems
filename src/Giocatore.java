
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

public class Giocatore {

    private final int NUMERO_CONOSCENTI = 20;                      // numero dei conoscenti
    Giocatore[] acquaintances = new Giocatore[NUMERO_CONOSCENTI];  // creazione array dei conoscenti
    private int life;                                              // vita
    private double wellness;                                       // benessere, specifica come hai vissuto la tua vita
    // gli stadi della fertilità sono invertiti perchè la vita parte da 100 e arriva a 0.
    final static int PUBERTA = 50;
    final static int MENOPAUSA = 80;
    int son_counter;                                               // regolatore numero figli all'anno
    int[] messageReceived = new int[NUMERO_CONOSCENTI];            // i messaggi che ho ricevuto da coloro che mi conoscono
    Personality carattere;                                         // il carattere di una persona è una classe che implementa l'interfaccia Personality
    int x_position, y_position;                                    // posizione che il giocatore occupa nell'arena
    // SCEGLIERE LA COMBINAZIONE DI CARATTERI DA VISUALIZZARE
    static String[] personalita = {"Personalita1", "Personalita2", "Personalita3", "Personalita4"};  //array delle personalità


    /**
     * Costruttore primario
     * @throws SecurityException if ?
     * @throws IllegalArgumentException if ?
     */
    Giocatore(int y, int x) {

        this.acquaintances[0] = this; //il giocatore conosce se stesso
        this.life = 100;             //la vita viene inizializzata a 100
        this.y_position = y;
        this.x_position = x;
        this.wellness = 75;
        son_counter = 2;

        //Viene scelta la personalità in modo randomico
        Random random = new Random();
        Class<? extends Personality> P;
        try {
            P = Class.forName(personalita[random.nextInt(personalita.length)]).asSubclass(Personality.class);
            Constructor<? extends Personality> costruttore = P.getConstructor(int.class, int.class, Giocatore.class);
            carattere = costruttore.newInstance(y_position, x_position, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Costruttore invocato per creare un figlio
     * @param carattere: carattere di this che si riproduce
     * @param wellness: wellness di this che viene trasmessa al figlio
     * @param i: indice di riga nell'arena
     * @param j: indice di colonna nell'arena
     */
    private Giocatore(Personality carattere, double wellness, int i, int j) throws NoSuchMethodException, SecurityException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        this.acquaintances[0] = this;
        this.life = 100;
        this.y_position = i;
        this.x_position = j;
        this.wellness = 75;
        this.son_counter = 3;

        // A seconda del benessere di mio padre nascerò avvantaggiato
        if (wellness < 60) {
            this.wellness = wellness - 10;
        }
        if(wellness > 70 && wellness < 90){
            this.wellness = wellness;
        }
        if(wellness > 90) {
            this.wellness = wellness + 10;
        }

        Class<? extends Personality> P = carattere.getClass();
        Constructor<? extends Personality> costruttore = P.getConstructor(int.class, int.class, Giocatore.class);
        this.carattere = costruttore.newInstance(x_position, y_position, this);
    }


    /**
     * Imposta la vita al valore passato in input
     * @param i: se minore di 1 viene considerato dead il personaggio, se maggiore di 100 viene impostato a 100
     */
    private void setLife(int i) {
        this.life = Integer.max(Integer.min(100, i), 0);
    }


    /**
     *
     * @return numero messaggi
     */
    int getNumeroMessaggi(){
        return this.NUMERO_CONOSCENTI;
    }


    /**
     * Uccide un personaggio
     */
    void die() {
        setLife(0);
    }


    /**
     * Diminuisce la vita di 1 al personaggio
     */
    void reduceLife(){
        this.life -= 1;
    }


    /**
     *
     * @param i vita da incrementare
     * @return vita incrementata del giocatore
     */
    int increaseLife(int i){ this.life+=i; return this.life;}


    /**
     *
     * @param i vita da decrementare
     * @return vita decrementata del giocatore
     */
    int decreaseLife(int i){ this.life-=i; return this.life;}


    /**
     * @return il valore della vita del personaggio
     */
    int getLife() {
        return life;
    }


    /**
     * Imposta il wellness
     * @param i: intero compreso tra ? e ? DA DECIDERE
     */
    void setWellness(double i) {
        this.wellness = i;
    }


    /**
     * incrementa il wellness del giocatore di i
     * @param i: intero
     */
    void increaseWellness(double i){
        this.wellness += i;
    }


    /**
     * diminuisce il wellness del giocatore di i
     * @param i: intero
     */
    void decreaseWellness(double i){
        this.wellness -= i;
    }


    /**
     * @return il wellness del giocatore
     */
    double getWellness() {
        return wellness;
    }


    /**
     * Imposta il messaggio ricevuto
     * @param message: messaggio che ho ricevuto
     * @param acquaintance: il giocatore che mi invia il messaggio
     */
    void setMessageReceived(int message, Giocatore acquaintance) {
        int index = 0;
        for (; index < acquaintances.length; index++) {
            if (acquaintances[index].equals(acquaintance)) break;
        }
        this.messageReceived[index] = message;
    }


    /**
     * Ritorna il messaggio ricevuto
     * @param index: indice dell'array dove ho ricevuto i messaggi
     * @return messaggio ricevuto (accedo al messaggio ricevuto)
     */
    int getMessageReceived(int index) {
        return messageReceived[index];
    }


    /**
     * Metodo per riprodursi (che sarà invocato se )
     * @return l'oggetto figlio alla classe TheGame
     * @param i: indice di riga nell'arena
     * @param j: indice di colonna nell'arena
     */
    Giocatore haveChildren(int i, int j) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.wellness += 10; //fare figli è il tuo scopo di vita, il tuo benessere aumenta
        return new Giocatore(this.carattere, this.wellness, i, j);
    }


    /**
     * requisiti per avere un figlio: son_counter minore di 1, vita compresa tra pubertà e MENOPAUSA,
     * avere una moglie (vedi getMyPromiscuity) in età fertile
     * @return true se posso avere un figlio
     */
    boolean canHaveChildren() {
        son_counter--;
        if (son_counter < 1 && life > Giocatore.PUBERTA && life < Giocatore.MENOPAUSA && acquaintances[carattere.getMyPromiscuity()] != null && wellness > 50 &&
                acquaintances[carattere.getMyPromiscuity()].life > Giocatore.PUBERTA && acquaintances[carattere.getMyPromiscuity()].life < Giocatore.MENOPAUSA) {
            son_counter = 2;
            return true;
        }
        return false;
    }


    /**
     * Comunica il suo messaggio, che varia a seconda della personalità, a tutti i suoi conoscenti (vivi)
     */
    void communicate() {
        carattere.act(this);
    }


    /**
     * USARE SOLO PER TEST: fa una stampa dell'array dei messaggi ricevuti
     */
    public void stampaMessaggi(){
        System.out.println("messaggi ricevuti di " + this);
        for(int i = 0; i< NUMERO_CONOSCENTI; i++){
            System.out.print(messageReceived[i]);

        }
        System.out.println();
    }


    /**
     *
     * @param s: nome della personalità
     * @return
     * quante persone di quella personalità conosce
     */
    int numPersonality(String s){
        int contatore = 0;
        for(int i = 0; i < NUMERO_CONOSCENTI; i++){
            if (this.acquaintances[i] != null && this.acquaintances[i].carattere.getMyPersonality().equals(s)){
                contatore++;
            }
        }
        return contatore;
    }


    /**
     * Inserisce in un posto vuoto (null o dead) dell'array dei conoscenti sia del giocatore da incontrare che
     * di quello che si sta incontrando. (Relazione biunivoca, io conosco te, tu conosci me)
     * @param gamerToMeet: giocatore da inserire nella rete dei conoscenti
     */
    void encounter(Giocatore gamerToMeet) {
        //se il giocatore passato è me stesso torna falso
        carattere.decreaseMeetCounter();
        if (carattere.getMeetCounter() > 0) return;
        if(gamerToMeet == this) {
            return;
        }
        int posLiberaMe = -1;
        //scorro tutto l'array per vedere se lo conosco o meno
        for (int i = 1; i < acquaintances.length; i++) { //escludo 0, la prima posizione, perchè è il giocatore stesso
            //se lo conosce esce direttamente, quindi sono sicuro che alla fine del ciclo non conosce il giocatore
            if (gamerToMeet == acquaintances[i]) {
                return;
            }
            //trova una posizione libera
            if (acquaintances[i] == null || (acquaintances[i] != null && acquaintances[i].getLife() <= 0)) {
                posLiberaMe = i;
                break;
            }
        }
        // se supero il controllo c'è spazio
        if (posLiberaMe != -1) {
            int posGiocatore2 = encounterAux(gamerToMeet);
            if(posGiocatore2 != -1) {
                this.acquaintances[posLiberaMe] = gamerToMeet;
                gamerToMeet.acquaintances[posGiocatore2] = this;
            }
        }
        carattere.setMeetCounter();
    }

    
    /**
     * devo verificare anche se il giocatore da incontrare ha una posizione libera
     * @param gamerToMeetVerify: giocatore da incontrare
     */
    private int encounterAux(Giocatore gamerToMeetVerify) {
        //la stessa verifica deve essere svolta per il giocatore da conoscere
        int poslibera = -1;
        for (int i = 1; i < acquaintances.length; i++) { //escludo 0, la prima posizione, perchè è il giocatore stesso
            if ((gamerToMeetVerify.acquaintances[i] == null || (gamerToMeetVerify.acquaintances[i] != null &&
                    gamerToMeetVerify.acquaintances[i].getLife() < 1))) {
/*
                if (this == gamerToMeetVerify.acquaintances[i]) {
                    break;
                }
*/
                poslibera = i;
                break;
            }
        }
        return poslibera;
    }


    /**
     * Metodo di stampa del giocatore nella console
     * @return stringa formattata formata dalla coppia (x,y) posizione del giocatore nell'arena e la sua vita
     */
/*    @Override
    public String toString() {
        return String.format("%d,%d|%5.1f", y_position, x_position, this.wellness);
    }*/
    public String toString() {
        StringBuilder pos = new StringBuilder("(pos ");
        pos.append(this.y_position);
        pos.append(",");
        pos.append(this.x_position);
        pos.append(" wellness ");
        pos.append(this.wellness);
        pos.append(" p: ");
        StringBuilder s = new StringBuilder(this.carattere.getClass().toString());
        s.delete(0, 6);
        pos.append(s);
        pos.append(")\n[");
        for (Giocatore gio: acquaintances) {
            if (gio != null && gio.getLife() > 0) {
                pos.append(gio.y_position);
                pos.append(",");
                pos.append(gio.x_position);
                pos.append("|");
            }
        }
        pos.append("]\n");
        StringBuilder message = new StringBuilder("{");
        for (int mess : messageReceived) {
            message.append(mess);
            message.append("     ");
        }
        message.append("}\n");
        pos.append(message);
        return pos.toString();
    }

}