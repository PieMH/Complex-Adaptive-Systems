package SGS;

import UI.GUI;

class Food {
    int riserve;
    int mangiato;
    int prodotto;
    int sprecato;
    int andato_a_male;
    int creato;
    int morti_di_fame;

    Food() {
        riserve = GUI.DIMENSION;
    }

    void vieniMangiato(Personality character) throws NoMoreFoodException{
        if (riserve < 0) {
            riserve = 0;
            throw new NoMoreFoodException();
        }
        int quanto = character.eat();
        riserve -= quanto;
        mangiato += quanto;
    }

    void vieniProdottoOSprecato(Personality character) throws NoMoreFoodException{
        if (riserve < 0) {
            riserve = 0;
            throw new NoMoreFoodException();
        }
        int quanto = character.produceOrWaste();
        if (quanto > 0) prodotto += quanto;
        else sprecato -= quanto;    // qui quanto è negativo perciò doppia negazione fa una somma
        riserve += quanto;
    }

    void cresci() {
        int quanto = ((GUI.HEIGHT * GUI.WIDTH) - SocialGameSystem.getCurrentAlive().size()) >> 2;   // 1° VARIANTE: più sono i giocatori minore è la crescita
//        int quanto = -(riserve - UI.GUI.DIMENSION * 2) >> 4;  // 2° VARIANTE: più sono le riserve in gioco minore è la crescita
        // shift a destra di n == dividere per 2^n
        riserve += quanto;
        creato += quanto;
    }

    class NoMoreFoodException extends Exception {
        NoMoreFoodException() {
            morti_di_fame++;
        }
    }
}
