import java.awt.event.ActionListener;
import javax.swing.Timer;
import java.lang.reflect.*;
import java.util.*;

/**
 * Classe di gestione dell'algoritmo di gioco evolutivo SocialGameSystem
 */
@ToDo(
        aggiornatoDa = "Pietro versione 3.3.5 il 19/06"
)
class SocialGameSystem implements Game {
    private Map<Integer, Giocatore> currentAlive;  // dizionario di giocatori vivi momentaneo
    private int born;                              // n° giocatori nati nel ciclo corrente
    private int dead;                              // n° giocatori morti nel ciclo corrente
    static boolean random = false;                 // variabile di differenziazione dello spawn iniziale dei giocatori nell'arena
    static int n_starting_players = 2000;          // numero di giocatori inizialmente creati se random è true
    static int meeting_distance = 3;               // distanza di incontri tra giocatori
    private boolean reset;                         // booleana per segnalare se resettare la map o no
    private Random random_seed;                    // seme di generazione di numeri random
    private GUI gui;                               // istanza di interfaccia grafica di dialogo
    private int n_iterations = 0;                  // contatore del numero di cicli
    private final Object lock = new Object();      // lock per i thread gioco/GUI nella modifica contemporanemente allo scorrimento sul dizionario
    private static int delay = 100;                // ritardo di repaint del current frame
	private Food food;                             // istanza della classe Food, determina il fattore ambientale del gioco
	private Timer t;


    /**
     * Costruttore della classe SocialGameSystem, chiama la resetMap per istanziare il currentAlive nullo
     * @param gui: istanza dell'interfaccia grafica del gioco
     */
    SocialGameSystem(GUI gui) {
        resetMap();
        this.gui = gui;
    }


    /**
     * Metodo invocato alla chiamata al costruttore o al click del tasto Reset su gui.
     * Resetta la map CurrentAlive a tutta null, istanzia un nuovo oggetto {@code Food} 
     * e resetta alcune statistiche di gioco
     */
    public void resetMap() {
        currentAlive = new Hashtable<>();
        reset = true;
        food = new Food(this);
        n_iterations = 0;
        Class<? extends Personality> P;
        for (String s : Giocatore.personalita) {
            try {
                P = Class.forName(s).asSubclass(Personality.class);
                Field totalnati = P.getDeclaredField("totalborn");
                totalnati.setInt(null, 0);
                Field totalmorti = P.getDeclaredField("totaldead");
                totalmorti.setInt(null, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * thread motore di gioco
     * fa evolvere il dizionario di giocatori vivi, lo aggiorna e aggiorna i frame della gui
     */
    public void startGame() {
		ActionListener taskPerformer = e -> {
            if (gui.play) {
                if (reset) {
                    if (random) {
                        setMapRandom();    // setMapRandom
                    }
                    else {
                        scorriMatrice(0);  // setMap
                    }
                    reset = false;
                }
                scorriDizionario(0);  // evolve
                food.riserve = Integer.min(Integer.max(food.riserve, 0), GUI.HEIGHT*GUI.WIDTH*2);
                food.cresci();
                printStats(false, false, true);
                resetStats();
                scorriMatrice(1);     // updateFrame
                gui.currentFrame = gui.nextFrame;
                gui.nextFrame = new boolean[GUI.HEIGHT][GUI.WIDTH];
                gui.getPanel().repaint();
            }
		};
        t = new Timer(delay, taskPerformer);
		t.start();
    }

    /**
     * Metodo di scorrimento delle chiavi del dizionario di appoggio che si riferisce a quello presente
     * il metodo è thread-safe in quanto vige mutua esclusione (mutex) tra il thread della gui e quello del gioco sgs
     * evitando che si modifichino le chiavi del dizionario mentre sto operando su di esso.
     * La "evolve" chiamata lavora su una fotografia della currentAlive e le modifiche lato user cliccando sul pannello della gui
     * non creano inconsistenze ma avvengono solo alla fine dell'evoluzione del ciclo di gioco.
     * Per un aspetto più realistico del gioco le chiavi del dizionario vengono accedute casualmente una per una, evitando
     * che i giocatori alla fine del dizionario sfruttino il vantaggio di subire più raramente gli effetti ambientali (carenza di cibo)
     * @param scelta: scelta di funzione di scorrimento del dizionario
     */
    private void scorriDizionario(int scelta) {
        Map<Integer, Giocatore> nextAlive;                      // dizionario di appoggio
        random_seed = new Random();
        synchronized (lock) {                                   // mutex tra il thread della gui e quello del gioco
            nextAlive = new Hashtable<>(currentAlive);
            List<Integer> keyslist = new ArrayList<>(nextAlive.keySet());
            int n = nextAlive.size();
            for (int i = 0; i < n; i++) {
                int m = keyslist.size();
                int index = 0;
                if (m-1 > 0) {
                    index = random_seed.nextInt(m - 1);
                }
                Integer key = keyslist.remove(index);
                if (scelta == 0) {
                    evolve(key);
                } 
                else if (scelta == 1) {
                    printDizionario(key);
                }
            }
        }
    }


    /**
     * @param scelta: scelta di funzione di scorrimento della matrice del pannello della gui
     */
    private void scorriMatrice(int scelta) {
        for (int i = 0; i < GUI.HEIGHT; i++) {
            for (int j = 0; j < GUI.WIDTH; j++) {
                if (scelta == 0) {
                    setMap(i, j);
                } else if (scelta == 1) {
                    updateFrame(i, j);
                } else if (scelta == 2) {
                    printArena(i, j);
                }
            }
        }
    }


    /**
     * Imposta la currentAlive casualmente generando posizioni casuali nella matrice,
     * tanti quanti dice n_starting_players
     * anche questa è thread-safe per non andare in conflitto con setMap potenzialmente chiamata dal thread della gui
     * quando si clicca su pannello
     */
    private void setMapRandom() {
        synchronized (lock) {
            random_seed = new Random();
            int y, x;
            for (int n = 0; n < n_starting_players; n++) {
                y = random_seed.nextInt(GUI.HEIGHT);
                x = random_seed.nextInt(GUI.WIDTH);
                Integer k = key(y, x);
                if (currentAlive.containsKey(k)) {
                	n--;
                	continue;
                }
                Giocatore gio = new Giocatore(y, x);
                currentAlive.put(k, gio);
                gio.carattere.newborn();
                born += 1;
            }
        }
    }


    /**
     * Setta currentAlive in posizione i, j in base ai valori true o false della currentFrame della gui
     * Viene chiamata la prima volta e ogni qual volta si clicca e si trascina il mouse sul pannello
     * Si riferisce al presente e quindi al runtime dell'applicazione, proprio per questo potrebbe andare in clash con altri metodi
     * tra cui setMapRandom e scorriDizionario, per questo è sincronizzata e quindi thread-safe per mutex
     * @param i: indice di riga
     * @param j: indice di colonna
     */
    public void setMap(int i, int j) {
        synchronized (lock) {
            Integer k = key(i, j);
            if (gui.currentFrame[i][j]) {
                if (!currentAlive.containsKey(k)) {       // inserisci chiave solo se libera, quindi senza sovrascrivere
                    Giocatore gio = new Giocatore(i, j);
                    currentAlive.put(k, gio);
                    gio.carattere.newborn();
                    born += 1;
                }
            } else {
                Giocatore gio = currentAlive.remove(k);                   // se la chiave già non c'è remove non fa nulla
                if (gio != null) {
                	gio.carattere.dead();
                	gio.die();
                }
                dead += 1;
            }
        }
    }


    /**
     * Aggiorna il nextFrame della gui rispetto alla situazione attuale della currentAlive
     * @param i: indice di riga
     * @param j: indice di colonna
     */
    private void updateFrame(int i, int j) {
        gui.nextFrame[i][j] = (currentAlive.containsKey(key(i, j)));
    }


    /**
     * metodo che gestisce l'algoritmo di incontri
     * vengono generati casualmente nel range di distanza di incontri gli indici di una posizione nella matrice
     * se la posizione corrisponde a un giocatore vivo il giocatore attuale e questo nuovo si incontreranno sotto le
     * condizioni di "encounter". Posso quindi conoscere al massimo una persona nuova a ciclo, a anno di vita
     * @param gio: il giocatore attule su cui è invocato il metodo come se fosse il this del metodo
     * @param chiave: la chiave nel dizionario dei vivi di gio
     */
    private void meet(Giocatore gio, Integer chiave) {
        Giocatore amico;
        random_seed = new Random();
        int y = coordinates(chiave, 0);
        int x = coordinates(chiave, 1);
        int lowerbound_y = y - meeting_distance;
        int lowerbound_x = x - meeting_distance;
        int upperbound_y = y + meeting_distance;
        int upperbound_x = x + meeting_distance;
        int[] pos = posizioniRandom(lowerbound_y, upperbound_y, lowerbound_x, upperbound_x);
        amico = currentAlive.get(key(pos[0], pos[1]));
        if (amico != null) {
            gio.encounter(amico);
        }
    }


    /**
     * Algoritmo di evoluzione di gioco invocato su tutti e soli i giocatori vivi di currentAlive, passi evolutivi:
     * 1- invecchio
     * 2- incontro un nuovo giocatore
     * 3- comunico coi giocatori che conosco
     * 4- produco/spreco cibo e poi lo mangio
     * 5- provo a riprodurmi
     * il metodo è dentro un synchronized per cui va in mutua esclusione con gli altri metodi modificanti, praticamente avviene che
     * mi evolvo basandomi su una fotografia attuale del dizionario currentAlive e non su ciò che verrà in futuro
     * @param chiave: la chiave del dizionario dei vivi del giocatore attuale che si evolve
     */
    private void evolve(Integer chiave) {
        Giocatore attuale = currentAlive.get(chiave);
        attuale.reduceLife();                               // invecchia
        if (attuale.getLife() < 1) {     // se attule è già morto
            muori(chiave);
            return;
        }
        attuale.setWellness(Double.max(Double.min(attuale.getWellness(), 100), 0));
        int i = coordinates(chiave, 0);
        int j = coordinates(chiave, 1);
        assert (attuale.getLife() > 0): "Stai facendo comunicare un carattere dead";
        meet(attuale, chiave);                              // incontra una persona nuova
        attuale.communicate();                              // manda messaggi
        try {
            food.vieniProdottoOSprecato(attuale.carattere);  //
            food.vieniMangiato(attuale.carattere);
        } catch (Food.NoMoreFoodException finished) {
            muori(chiave);
            return;
        }
        assert (attuale.getLife() > 0): "Sono dead/non mi sento bene, non posso fare figli";
        if (attuale.canHaveChildren()) {                    // se il giocatore può avere un figlio
            int lowerbound_i = i - meeting_distance;
            int lowerbound_j = j - meeting_distance;
            int upperbound_i = i + meeting_distance;
            int upperbound_j = j + meeting_distance;
            int[] pos = posizioniRandom(lowerbound_i, upperbound_i, lowerbound_j, upperbound_j);
            int y = pos[0]; int x = pos[1];
            Giocatore son = currentAlive.get(key(y, x));
            if (son == null) {                      // se son è nullo vuoldire che c'è posto per inserire un figlio vero
                try {
                    son = attuale.haveChildren(y, x);
                    currentAlive.put(key(y, x), son);
                    son.carattere.newborn();
                    born += 1;
                }
                catch (Exception e) {    // eccezioni possibili dovute alla Reflection sui caratteri padre per il figlio
                    e.printStackTrace();
                }
            }
        }
        if (attuale.getLife() < 1) {                          // se attuale è dead
            muori(chiave);
        }
    }


    /**
     * Stampa il giocatore in posizione i, j in base al dizionario currentAlive
     * @param i: indice di riga
     * @param j: indice di colonna
     */
/*    private void printArena(int i, int j) {
        System.out.print(currentAlive.get(key(i, j)) + ((j == GUI.WIDTH - 1) ? "\n" : "\t"));
    }*/
    private void printArena(int i, int j) {
        Giocatore gio = currentAlive.get(key(i, j));
        if (gio != null) {
            System.out.print(gio);
        }
    }


    /**
     * stampa la chiave key e il giocatore associato a essa
     * @param key: chiave del giocatore da stampare
     */
    private void printDizionario(Integer key) {
        int i = coordinates(key, 0);
        int j = coordinates(key, 1);
        System.out.println("K:" + i + "," + j + " V:" + currentAlive.get(key));
    }


    /**
     * metodo di stampa delle strutture dati di gioco e delle statistiche di gioco
     * @param stampaArena: se stampare tutta la matrice di gioco
     * @param stampaDizionario: se stampare tutto il dizionario di currentAlive
     * @param stampaStatistiche: se stampare le statistiche di gioco
     */
    private void printStats(boolean stampaArena, boolean stampaDizionario, boolean stampaStatistiche) {
        if (stampaArena) {
            scorriMatrice(2);  // printArena
            System.out.println();
        }
        if (stampaDizionario) {
            scorriDizionario(1);  // printDizionario
            System.out.println();
        }
        if (stampaStatistiche) {
            // GENERICHE
            System.out.print("Numero di giocatori vivi: " + currentAlive.size());
            System.out.print("; nati in questo turno: " + born);
            System.out.print("; morti in questo turno: " + dead);
            System.out.print(", (morti per fame: " + food.morti_di_fame);
            System.out.print("); differenza: "); System.out.print(born-dead);
            System.out.println(".\n");
            // PERSONALITA'
            Class<? extends Personality> P;
            for (String s : Giocatore.personalita) {
                try {
                    P = Class.forName(s).asSubclass(Personality.class);
                    System.out.print(s.toUpperCase() + "-> ");
                    System.out.print("nati quest'anno: ");
                    Field nato = P.getDeclaredField("born");
                    System.out.print(nato.get(null));
                    System.out.print("; morti quest'anno: ");
                    Field morto = P.getDeclaredField("dead");
                    System.out.print(morto.get(null));
                    System.out.print("; TOT nati : ");
                    Field natotot = P.getDeclaredField("totalborn");
                    System.out.print(natotot.get(null));
                    System.out.print("; TOT morti : ");
                    Field mortotot = P.getDeclaredField("totaldead");
                    System.out.print(mortotot.get(null));
                    System.out.print("; vivi attuali: ");
                    System.out.print((Integer)natotot.get(null) - (Integer)mortotot.get(null));
                    System.out.println(".\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // CIBO
            System.out.print("Cibo totale " + food.riserve);
            System.out.print("; cibo prodotto " + food.prodotto);
            System.out.print("; cibo mangiato " + food.mangiato);
            System.out.print("; cibo sprecato " + food.sprecato);
//            System.out.print("; cibo andato a male " + food.andato_a_male);
            System.out.print("; cibo creato " + food.creato);
            System.out.print("; differenza: "); System.out.print(food.prodotto + food.creato - food.mangiato - food.sprecato - food.andato_a_male);
            System.out.println(".");
        }

        if (stampaArena || stampaDizionario || stampaStatistiche) {
            n_iterations++;
            System.out.println(String.format("****************************************************************************anno %d******************************************************************************", n_iterations));
        }
    }


    /**
     * @param y: indice di colonna
     * @param x: indice di riga
     * @return la chiave univoca associata alla coppia di coordinate y,x
     */
    static Integer key(int y, int x) {
        return (y * GUI.WIDTH) + x;
    }


    /**
     * Funzione inversa della key
     * @param chiave: una chiave del dizionario
     * @param col_row: 0 per sapere l'indice di colonna, 1 per sapere l'indice di riga
     * @return l'indice o di riga o di colonna associato alla chiave in funzione del parametro di input
     */
    private static int coordinates(Integer chiave, int col_row) {
        if (col_row == 0) {
            return chiave / GUI.WIDTH;
        }
        return chiave % GUI.WIDTH;
    }


    /**
     * metodo di generazione di una coppia di numeri pseudo-casuali all'interno dei range passati in input
     * inoltre sistema i numeri generati casualmente affinchè rientrino all'interno della dimensione della gui
     * @param lowerbound_i: estremo inferiore di riga
     * @param upperbound_i: estremo superiore di riga
     * @param lowerbound_j: estremo inferiore di colonna
     * @param upperbound_j: estremo superiore di colonna
     * @return la coppia di numeri casuali
     */
    private int[] posizioniRandom(int lowerbound_i, int upperbound_i, int lowerbound_j, int upperbound_j) {
        random_seed = new Random();
        int[] pos = new int[2];
        pos[0] = random_seed.nextInt(upperbound_i - lowerbound_i) + lowerbound_i;
        pos[1] = random_seed.nextInt(upperbound_j - lowerbound_j) + lowerbound_j;
        pos[0] = Integer.max(Integer.min(pos[0], GUI.HEIGHT - 1), 0);
        pos[1] = Integer.max(Integer.min(pos[1], GUI.WIDTH - 1), 0);
        return pos;
    }


    /**
     * Resetta tutti i contatori delle statistiche di gioco
     * I contatori delle personalità sono resettate parametricamente con la Reflection
     */
    private void resetStats() {
        born = 0;
        dead = 0;
        food.creato = 0;
        food.andato_a_male = 0;
        food.mangiato = 0;
        food.prodotto = 0;
        food.sprecato = 0;
        food.morti_di_fame = 0;
        Class<? extends Personality> P;
        for (String s : Giocatore.personalita) {
            try {
                P = Class.forName(s).asSubclass(Personality.class);
                Field nato = P.getDeclaredField("born");
                nato.setInt(null, 0);
                Field morto = P.getDeclaredField("dead");
                morto.setInt(null, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Toglie dal dizionario currentAlive il giocatore morto e aggiorna i contatori dei morti
     * @param chiave: la chiave del giocatore morto
     */
    private void muori(Integer chiave) {
        Giocatore gio = currentAlive.remove(chiave);
        gio.die();
        gio.carattere.dead();
        dead += 1;
    }


    // metodi getter che fanno override dell'interfaccia Game che questa classe implementa

    @Override
	public Timer getTimer() {
		return t;
	}


	@Override
	public int getDelay() {
		return delay;
	}


	@Override
	public Map<Integer, Giocatore> getCurrentAlive() {
		return currentAlive;
	}

}