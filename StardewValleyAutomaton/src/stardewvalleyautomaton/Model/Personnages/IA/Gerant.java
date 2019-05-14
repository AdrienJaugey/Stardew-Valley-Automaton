package stardewvalleyautomaton.Model.Personnages.IA;

import java.util.ArrayList;
import stardewvalleyautomaton.Model.Carte;
import stardewvalleyautomaton.Model.Gestionnaires.GestionnaireDesObjets;
import stardewvalleyautomaton.Model.Gestionnaires.GestionnaireDesPersonnages;
import stardewvalleyautomaton.Model.Objets.Enum_Objet;
import stardewvalleyautomaton.Model.Objets.Objet;
import stardewvalleyautomaton.Model.Objets.Oeuf;
import static stardewvalleyautomaton.Model.Personnages.Enum_Personnage.Vache;
import stardewvalleyautomaton.Model.Personnages.Vache;

/**
 *
 * @author Adrien JAUGEY
 */
public class Gerant {
    //Variables permettant de gérer les choix concernant Abigail
    private IA_Abigail abigail;
    private Oeuf oeufAbi;
    private boolean traiteAbi;
    
    //Variables permettant de gérer les choix concernant la soeur d'Abigail
    private IA_Abigail doppelganger;
    private Oeuf oeufDop;
    private boolean traiteDop;
    
    private static Gerant instance;

    private Gerant() {
        //Initialisation
        oeufAbi = oeufDop = null; 
        traiteAbi = traiteDop = false;
    }
    
    /**
     * Permet d'obtenir l'instance du Gerant et de la créer si besoin
     * @return le Gerant
     */
    public static Gerant get(){
        if(instance == null){
            instance = new Gerant(); //On crée le Gerant si celui-ci n'existe pas
        }
        return instance;
    }
    
    /****************************************************/
    /* Méthodes permettant de gérer Abigail et sa soeur */
    /****************************************************/
    
    /**
     * Permet de définir l'ia considérée comme Abigail
     * @param abigail l'ia à considérer comme Abigail
     */
    public void setAbigail(IA_Abigail abigail) {
        this.abigail = abigail; //On définit abigail
        this.abigail.setNom("Abigail"); //On lui donne son nom
    }
    
    /**
     * Permet de définir l'ia considérée comme la soeur d'Abigail
     * @param doppelganger l'ia à considérer comme la soeur d'Abigail
     */
    public void setDoppelganger(IA_Abigail doppelganger) {
        this.doppelganger = doppelganger; //On définit la soeur d'Abigail
        this.doppelganger.setNom("Soeur d'Abigail"); //On lui donnne son nom
    }
    
    /**
     * Permet d'obtenir le sommet sur lequel se trouve Abigail
     * @return le sommet d'Abigail
     */
    private int getPosAbigail(){
        return this.abigail.getSommetPerso();
    }
    
    /**
     * Permet d'obtenir le sommet sur lequel se trouve la soeur d'Abigail
     * @return le sommet de la soeur d'Abigail
     */
    private int getPosDoppelganger(){
        return this.doppelganger.getSommetPerso();
    }
    
    /**
     * Permet de savoir si l'ia est Abigail ou sa soeur
     * @param ia l'ia à tester
     * @return true si Abigail et false si sa soeur
     */
    private boolean isAbigail(IA_Abigail ia){
        return ia == abigail;
    }
    
    /**********************************************************/
    /* Méthodes permettant la gestion de la récolte des oeufs */
    /**********************************************************/
    
    /**
     * Permet de renvoyer la liste des oeufs que peut cibler l'IA_Abigail qui la demande
     * @param demandeur l'IA_Abigail qui fait la demande
     * @return la liste des oeufs
     */
    public ArrayList<Oeuf> getListeOeuf(IA_Abigail demandeur){
        ArrayList<Oeuf> liste = new ArrayList<>(); //On crée une liste temporaire qui va stocker les oeufs à cibler
        Oeuf exclu;
        if(isAbigail(demandeur)){
            exclu = oeufDop; //Si l'ia qui demande est Abigail, on exclu l'oeuf ciblé par sa soeur
        } else {
            exclu = oeufAbi; //Même chose pour sa soeur avec l'oeuf d'Abigail
        }
        for(Objet o : GestionnaireDesObjets.getListeDesObjets()){ //Pour chaque objet de la liste des objets
            if((o != exclu)&&(o.getType() == Enum_Objet.Oeuf)){ //Si celui n'est pas l'oeuf exclu et si c'est un oeuf
                liste.add((Oeuf)o); //On l'ajoute à la liste à retourner
            }
        }
        return liste;
    }
    
    /**
     * Permet de définir l'oeuf actuellement ciblé par Abigail ou sa soeur
     * @param demandeur l'IA_Abigail qui demande à définir son oeuf cible
     * @param oeuf l'oeuf ciblé
     */
    public void setOeuf(IA_Abigail demandeur, Oeuf oeuf) {
        if(isAbigail(demandeur)){
            this.oeufAbi = oeuf; //Si c'est Abigail qui a fait la demande, on définit son oeuf cible à la valeur voulue
        } else {
            this.oeufDop = oeuf; //Même chose avec sa soeur
        }
    }
    
    /***********************************************************/
    /* Méthodes permettant la gestion de la traite de la vache */
    /***********************************************************/
    
    /**
     * Renvoit l'instance de l'IA_Abigail la plus proche de la vache
     * @return l'IA_Abigail la plus proche de la vache
     */
    private IA_Abigail getPlusProcheVache(){
        int SommetVache = Carte.getGraphe().getSommet(getVache().getCase());    //On récupère le sommet de la vache
        int[] cheminAbi = Carte.getGraphe().Dijkstra(getPosAbigail(), SommetVache); //On récupère le chemin que devrait faire Abigail pour aller jusqu'à la vache
        int fatigueAbi = abigail.calculFatigue(cheminAbi); //On stocke la fatigue qu'aurait Abigail si elle suivait le chemin
        int[] cheminDop = Carte.getGraphe().Dijkstra(getPosDoppelganger(), SommetVache); //Même chose pour le chemin de la soeur
        int fatigueDop = doppelganger.calculFatigue(cheminDop); //Même chose pour la fatigue de la soeur
        if(fatigueAbi > fatigueDop){ 
            return doppelganger; //Si Abigail sera plus fatiguée que sa soeur, on renvoit l'instance de l'ia de sa soeur
        } else {
            return abigail; //Même chose avec la soeur d'Abigail
        }
    }
    
    /**
     * Permet de "dire" à l'ia qui demande si elle peut aller vers la vache ou pas
     * @param demandeur l'ia qui demande
     * @return true si elle peut aller vers la vache, sinon false
     */
    public boolean peutAllerVersVache(IA_Abigail demandeur) {
        boolean res;
        //Cette méthode est appellée par une ia qui n'a pas de cible, n'a pas de lait
        if(!traiteAbi && !traiteDop){ //Si aucune des deux ne se dirige déjà vers la vache
            if(abigail.getTarget()!= doppelganger.getTarget()){ //Si seulement l'une des deux ne fait rien
                traiteAbi = (abigail.getTarget() == null); //Si Abigail ne fait rien alors elle va chercher la vache
                traiteAbi = (abigail.getTarget() == null); //Même chose 
            } else {
                if(abigail.aDuLait()!= doppelganger.aDuLait()){ //Si seulement l'une des deux à du lait
                    traiteAbi = !abigail.aDuLait();
                    traiteDop = !doppelganger.aDuLait();
                } else {
                    if(abigail.aSoif()!= doppelganger.aSoif()){  //Si seulement l'une des deux à soif
                        traiteAbi = abigail.aSoif();
                        traiteDop = doppelganger.aSoif();
                    } else if(abigail.aFaim() != doppelganger.aFaim()){ //Si seulement l'une des deux à faim
                        traiteAbi = abigail.aFaim();
                        traiteDop = doppelganger.aFaim();
                    }else if(isAbigail(getPlusProcheVache())){ //En dernier recours, on décide que c'est celle qui est le plus proche qui va vers la vache
                        traiteAbi = true;
                    } else {
                        traiteDop = true;
                    }
                }
            }
        } 
        if(isAbigail(demandeur)){
            res = traiteAbi; //Si c'est Abigail qui a fait la demande, on lui renvoit sa réponse
        } else{
            res = traiteDop; //Même chose pour la soeur d'Abigail
        }
        return res;
    }
    
    /**
     * Permet de réinitialiser les variables de traite
     */
    public void validerTraite(){
        traiteAbi = traiteDop = false;
    }
    
    /**
    * Permet d'obtenir l'instance de la vache
    * @return la vache
    */
    private Vache getVache(){
       boolean sortie = false;
       int i=0,
       max = GestionnaireDesPersonnages.getListeDesPersonnages().size();
       while((i<max)&& !sortie){ //On parcourt la liste des personnages tant que la vache n'a pas été trouvée ou que l'indice n'est pas supérieur au dernier indice
           if(GestionnaireDesPersonnages.getListeDesPersonnages().get(i).getType() == Vache) //Si le personnage actuel est la vache
           {
               sortie = true; //On sort de la boucle
           } else {
               i++; //Si ce n'est pas la vache on incrémente l'indice
           }
       }
       return (Vache)GestionnaireDesPersonnages.getListeDesPersonnages().get(i); //On récupère la vache depuis la liste et on la renvoit
    }
    
    /*******************************************************************/
    /* Méthode permettant la gestion de l'avance rapide lors de la nuit*/
    /*******************************************************************/
    
    /**
     * Permet de savoir si Abigail et sa soeur sont toutes les deux devant la maison
     * @return true si elle y sont
     */
    public boolean avanceRapide(){
        return (getPosAbigail() == Carte.getGraphe().getSommet(8, 25)) && (getPosDoppelganger() == Carte.getGraphe().getSommet(8, 25));
    }
}
