package stardewvalleyautomaton.Model;

import stardewvalleyautomaton.Model.Cases.Case;


/**
 *
 * @author Adrien JAUGEY
 */
public class Graphe{
    private final int matrice[][];
    private final int nbSommet;
    
    /**
     * Constructeur du graphe
     * @param nbSommets le nombre de sommet dans le graphe
     */
    public Graphe(int nbSommets){
        nbSommet = nbSommets;
        matrice = new int[nbSommets][nbSommets];
        for(int i = 0;i<nbSommet;i++){
            for(int j = 0;j<nbSommet;j++){
                matrice[i][j] = 0; //On initialise toutes les cases de la matrice à 0
            }
        }
    }
    
    /**
     * Permet d'ajouter une arete entre deux sommets
     * @param s1 le premier sommet
     * @param s2 le second sommet
     * @param poids le poids de l'arete
     */
    public void ajouterArete(int s1, int s2, int poids){
        matrice[s1][s2] = matrice[s2][s1] = poids;
    }
    
    /**
     * Permet d'ajouter un arc entre deux sommets
     * @param s1 le premier sommet
     * @param s2 le deuxieme sommet
     * @param poids le poids de l'arc
     */
    public void ajouterArc(int s1, int s2, int poids){
        matrice[s1][s2] = poids;
    }
    
    /**
     * Renvoit la valeur de la matrice d'adjacence pour du point s1 vers s2
     * @param s1 le premier sommet
     * @param s2 le deuxième sommet
     * @return la valeur de la matrice d'adjacence en s1,s2
     */
    public int getMatrice(int s1, int s2){
        return matrice[s1][s2];
    }

    /**
     * Renvoit le chemin le plus court pour aller du point départ au point arrivée
     * @param depart le point de départ 
     * @param arrivee le point d'arrivée
     * @return le chemin de successeur
     */
    public int[] Dijkstra(int depart, int arrivee){
        boolean Mark[] = new boolean[nbSommet];
        int d[] = new int[nbSommet];
        int pred[] = new int[nbSommet];
        initialisation(Mark,d,pred);
        d[depart] = 0;
        
        int a = getMinNonMarque(Mark,d);
        while(a != -1){
            Mark[a] = true;
            for(int b =0;b<nbSommet;b++){
                relachement(d,pred,a,b);
            }
            a = getMinNonMarque(Mark,d);
        }
        return getSucc(pred,arrivee);
    }
    
    /**
     * Permet d'initialiser les tableaux Mark, d et pred de Dijkstra 
     * @param Mark le tableau de booléen correspondant aux points traités
     * @param d le tableau d'entier contenant la distance depuis le point de départ
     * @param pred le tableau contenant le prédecesseur de chaque point
     */
    private void initialisation(boolean[] Mark,int[] d, int[] pred){
        for(int i = 0; i < nbSommet; i++){
            Mark[i] = false; //On initialise Mark à faux
            d[i] = 100000; //On initialise d à une valeur assez grande
            pred[i] = -1; //On initilise pred à -1
        }
    }
    
    /**
     * Permet de procéder au relâchement dans Dijkstra
     * @param d le tableau contenant les distances
     * @param pred le tableau de précédents
     * @param a un sommet
     * @param b un sommet
     */
    private void relachement(int[] d,int[] pred, int a, int b){
        if(((d[b]) > (d[a] + matrice[a][b]))&& (matrice[a][b] != 0)){ //Si la distance de b est strictement supérieure à la distance de a plus celle de a à b
                    d[b] = d[a] + matrice[a][b]; //On met à jour la distance de b
                    pred[b] = a; //On définit a comme prédecesseur de b
        }
    }
    
    /**
     * Utilisé dans Dijkstra pour récupérer le sommet non marqué de d minimum
     * @param Mark le tableau des points visités de Dijkstra
     * @param d le tableau des distances de Dijkstra
     * @return le sommet non marqué minimum de d
     */
    private int getMinNonMarque(boolean[] Mark, int[] d){
        int dMin = 2000001; //On initialise la distance minimale à un grand nombre
        int indMin = -1; //On initialise le sommet ayant la distance minimale à -1 (ce qui équivaut
        for(int i = 0;i<nbSommet;i++){ //Pour tout les sommets
            if(!Mark[i]){ //Si celui-ci n'est pas marqué
                if(d[i] < dMin){ //Si sa distance est strictement inférieure à la plus petite pour l'instant trouvée
                    dMin = d[i]; //On met à jour la distance minimale
                    indMin = i; //On met à jour l'indice du sommet ayant la distance minimale
                }
            }
        }
        return indMin;
    }
    
    /**
     * Permet d'inverser le tableau pred en sortie de Dijkstra
     * @param pred le tableau de précédent
     * @param arrivee le numéro du sommet correspondant à l'arrivée
     * @return le tableau de succésseur
     */
    public int[] getSucc(int[] pred,int arrivee){
        int[] res = new int[nbSommet]; //On crée un tableau d'entier
        for(int i = 0;i<nbSommet;i++) res[i] = -1; //On initialise ce tableau à -1
        int courant = arrivee; //On définit le sommet courant à la valeur du sommet d'arrivée
        while(pred[courant] != -1){ //Tant que le sommet courant à un prédecesseur
            res[pred[courant]] = courant; //Le case d'indice le prédecesseur du point courant prend la valeur du point courant
            courant = pred[courant]; //On met à jour le point courant à son prédecesseur
        }
        return res;
    }
    
    /**
     * Permet d'obtenir le poids d'un chemin
     * @param Succ le tableau de succésseur
     * @param depart le sommet de départ
     * @return le poids du chemin
     */
    public int poidsChemin(int[] Succ, int depart){
        int res = 0; //On initialise le poids à 0
        int courant = depart; //On définit le sommet courant au sommet de départ
        while(Succ[courant] != -1){ //Tant que le sommet courant a un succésseur
            res += matrice[courant][Succ[courant]]; //Le poids s'incrémente du poids de l'arc du sommet courant vers celui qui suit
            courant = Succ[courant]; //On met à jour le sommet courant à son succésseur
        }
        return res;
    }
    
    @Override
    public String toString(){
        String res = "";
        for(int i = 0; i<nbSommet;i++){
           for(int j = 0; j<nbSommet;j++){
                res += matrice[i][j] + " ";
            }
            res += "\n";
        }
        
        
        return res;
    }
    
    /**
     * Permet d'obtenir le numéro d'un sommet
     * @param ligne la ligne de la case
     * @param colonne la colonne de la case
     * @return le numéro du sommet dans le graphe
     */
    public int getSommet(int ligne, int colonne){
        return (ligne)*30 + colonne;
    }
    
    /**
     * Permet d'obtenir le numéro d'un sommet
     * @param laCase la case dont on veut le numéro
     * @return le numéro dans le graphe
     */
    public int getSommet(Case laCase){
        return getSommet(laCase.getLigne(), laCase.getColonne());
    }
    
    /**
     * Permet d'obtenir la ligne correspondant à un sommet du graphe
     * @param sommet le sommet du graphe
     * @return la ligne de ce sommet
     */
    public int getLigne(int sommet){
        return (sommet/30);
    }
    
    /**
     * Permet d'obtenir la colonne d'un sommet du graphe
     * @param sommet le sommet du graphe
     * @return la colonne de ce sommet
     */
    public int getColonne(int sommet){
        return (sommet%30);
    }
}
