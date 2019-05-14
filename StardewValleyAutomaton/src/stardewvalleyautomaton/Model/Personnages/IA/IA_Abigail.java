/*
 * ia d'abigail
 */
package stardewvalleyautomaton.Model.Personnages.IA;

import java.util.ArrayList;
import stardewvalleyautomaton.Model.Carte;
import stardewvalleyautomaton.Model.Cases.Case;
import stardewvalleyautomaton.Model.Gestionnaires.GestionnaireDesObjets;
import stardewvalleyautomaton.Model.Gestionnaires.GestionnaireDesPersonnages;
import stardewvalleyautomaton.Model.Graphe;
import stardewvalleyautomaton.Model.Objets.Enum_Objet;
import static stardewvalleyautomaton.Model.Objets.Enum_Objet.*;
import stardewvalleyautomaton.Model.Objets.Machine_Fromage;
import stardewvalleyautomaton.Model.Objets.Objet;
import stardewvalleyautomaton.Model.Objets.Oeuf;
import stardewvalleyautomaton.Model.Personnages.*;
import static stardewvalleyautomaton.Model.Personnages.Enum_Personnage.*;
import static stardewvalleyautomaton.Model.Personnages.IA.Enum_Action.*;
import static stardewvalleyautomaton.Model.Personnages.IA.Enum_CycleJour.*;

/**
 *
 * @author simonetma
 */
public class IA_Abigail extends IA {
    private Case target;
    private Enum_Personnage persoCible;
    private Enum_Objet objetCible;
    private boolean init = false;
    private int[] cheminCourant;
    private int SommetPerso = 265, fatigue, faim, soif;
    private int timer;
    private int deplacement =0;
    private String nom;
    
    @Override
    protected void setActionValide() {
        this.addActionValide(attendre);
        this.addActionValide(moveLeft);
        this.addActionValide(moveRight);
        this.addActionValide(moveTop);
        this.addActionValide(moveBottom);
        this.addActionValide(traire);
        this.addActionValide(produireFromage);
        this.addActionValide(collecterOeuf);
    }
    
    //IA D'ABIGAIL (A IMPLEMENTER) ---------------------------------------------

    /**
     * Permet d'obtenir l'action que doit réaliser Abigail
     * @return l'action qu'Abigail doit faire
     */
    @Override
    public Enum_Action action() {
        Enum_Action action = attendre;
        
        //Initialisation des attributs uniquement la première fois
        if(!init){
            initialisation();
        }
        
        //Calcul du sommet sur lequel se trouve Abigail
        SommetPerso = getGraphe().getSommet(this.personnage().getCase());
        if(aDuLait() && aSoif()){ //Si Abigail a soif et a du lait
            boireDuLait(); //On la fait boire du lait
        }
        if (aDuFromage() && aFaim()){ //Si Abigail a du fromage et qu'elle a faim
            mangerFromage(); //On la fait manger du fromage
        }
        
        //Permet de définir une cible dans le cas où il n'y en a pas
        if (target == null){
            if(getCycleJour() == Soir){ //Si il fait nuit 
                allerVersMaison(); //Abigail cible sa maison
            }else if(!aDuLait() && lait() && Gerant.get().peutAllerVersVache(this)){ //Si Abigail n'a pas de lait sur elle, que la vache en a produit et qu'elle a l'autorisation
                arreterVache(); //On stoppe la vache
                allerVersVache(); //Abigail cible la vache
            } else if(aDuLait() && !aDuFromage() && !aSoif()){ //Si Abigail a du lait sur elle mais pas de fromage et n'a pas soif
                allerVersObjet(Machine_Fromage); //Abigail cible la machine à fromage
            } else if(isThereOeuf() && faim <100){ //S'il y a un oeuf disponible et que la faim de Abigail n'est pas a 100
                allerVersObjet(Oeuf); //Abigail cible l'oeuf disponible le plus proche
            }
            deplacement = calculFatigue(cheminCourant); //On calcule la fatigue prévisionnelle d'Abigail
        } 
        
        //Gère les actions s'il y a une cible
        if (target != null){
            System.out.println("Déplacement : " + deplacement);
            affEtat();
            if(deplacement < 100){ //Si le déplacement ne l'amènerait pas à plus de 100 de fatigue
                if (!onTarget()){ //Si Abigail n'est pas sur la cible, elle se dirige vers elle
                    action = moveComplexe();
                } else { //Sinon Abigail est sur la cible, elle choisit l'action à réaliser
                    if(objetCible == Oeuf){ //Si la cible est un oeuf elle le ramasse
                        action = collecterOeuf; //Abigail va collecter l'oeuf
                        Gerant.get().setOeuf(this, null); //Abigail informe le gérant qu'elle ne cible plus d'oeuf
                        faim +=5; //Sa faim augmente de 5
                    } else if(persoCible == Vache){ //Si la cible est une vahe, elle la trait
                        action = traire; //Abigail va traire la vache
                        libererVache(); //Elle libère la vache
                        if(aDuLait()){ //Si elle a récupérer le lait (i.e. si elle en a)
                            Gerant.get().validerTraite(); //Elle informe le gérant qu'elle a trait la vache
                        }
                    } else if(objetCible == Machine_Fromage){ //Si la cible est une machine à fromage, elle l'utilise
                        action = produireFromage;
                    }
                    deplacement = 0; //On remet le déplacement à 0
                    target = null; //On réinitialise la cible et les variables qui lui permettent de savoir le type de cible
                    objetCible = null;
                    persoCible = null;
                    affEtat(); //On affiche son état
                }
            }else{ //Dans le cas où la fatigue serait supérieure à 100
                action = attendre; //Abigail fait une pause
                deplacement = calculFatigue(cheminCourant); //On recalcule la fatigue prévisionnelle
            }
        }
        
        //Gestion de la pause
        if ((action == attendre)&&(fatigue > 0)){ //Si Abigail doit attendre et que sa fatigue est strictement positive
            fairePause(); //Abigail fait une pause ce qui lui permet de faire baisser sa fatigue
            affEtat(); //On affiche son état
        }
        avanceTemps(); //On avance l'heure de la journée
        return action;
    }
    
    /**
    * Permet d'initialiser les variables ainsi que de récupérer le graphe de la carte
    */
    private void initialisation() {
        cheminCourant = new int[900];
        for(int i=0;i<900;i++) cheminCourant[i] = -1;
        init = true;
        target = null;
        fatigue = soif = faim = 0;
        timer = 60;
        affEtat();
    }
    
    /******************************************/
    /* Méthodes permettant la gestion du temps*/
    /******************************************/
    
    /**
     * Fait avancer le temps en jeu
     */
    private void avanceTemps(){
        if ((getCycleJour() == Soir)&& Gerant.get().avanceRapide()){ //Système permettant l'avance rapide du temps la nuit
            timer += 5;
        }
        timer++;
        timer %= 240;
    }
    
    /**
     * Permet de savoir en fonction du temps en jeu le moment de la journée
     * @return Matin Après-Midi ou Soir en fonction du temps du jeu
     */
    private Enum_CycleJour getCycleJour(){
        Enum_CycleJour res;
        if(timer < 60){
            res = Soir;
        } else if (timer < 130){
            res = Matin;
        } else if (timer < 210){
            res = ApresMidi;
        } else {
            res = Soir;
        }
        return res;
    }
    
    /**************************************************************/
    /* Méthodes permettant le ciblage et le déplacement d'Abigail */
    /**************************************************************/
    
    /**
    * Renvoie le graphe de la carte
    * @return le graphe du jeu
    */
   private Graphe getGraphe(){
       return Carte.getGraphe();
   }
    
    /**
     * Permet de définir les variables target, cheminCourant et objetCible
     * @param obj le type d'objet vers lequel aller
     */
    private void allerVersObjet(Enum_Objet obj){
        if(obj == Machine_Fromage){ //Si on cible la machine à fromage
            target = getMachineFromage().getCase(); //On récupère la case de la machine à fromage
            target = Carte.get().getCase(target.getLigne(), target.getColonne()-1);//On décale la cible pour aller juste à côté
        } else if(obj == Oeuf){ //Sinon si on cible un oeuf
            Oeuf cible = getOeufPlusProche(); //On récupère l'oeuf disponible le plus proche
            target = cible.getCase(); //On définit la case cible
            Gerant.get().setOeuf(this, cible); //On informe le gérant que l'on cible cet oeuf
        }
        cheminCourant = getChemin(target); //On récupère le chemin permettant d'aller jusqu'à la cible
        objetCible = obj; //On définit le type de cible
        System.out.println(nom+" se déplace vers : " + obj);
    }
    
    /**
     * Permet de définir les variables target, cheminCourant et persoCible pour aller vers la vache
     */
    private void allerVersVache(){
        target = getVache().getCase(); //On récupère la case de la vache
        target = Carte.get().getCase(target.getLigne(), target.getColonne() + 1); //On définit la case juste à droite de la vache pour pouvoir la traire
        cheminCourant = getChemin(target); //On récupère le chemin pour aller jusqu'à la vache
        persoCible = Vache; //On définit le type de cible
        System.out.println(nom +" se déplace vers la vache");
    }
    
    /**
     * Lorsque c'est le soir abigail rentre se réposer chez elle, cette fonction al ramène a la case devant la maison
     */
    private void allerVersMaison(){
        target = Carte.get().getCase(8, 25); //On définit la cible comme étant la case devant la maison
        cheminCourant = getChemin(target); //On récupère le chemin pour aller jusqu'à la maison   
        System.out.println(nom+" va se reposer");
    }
    
   /**
    * Permet d'obtenir l'objet le plus proche
    * @param obj le type de l'objet à chercher
    * @return l'objet le plus proche
    */
   private Machine_Fromage getMachineFromage(){
       Machine_Fromage res = null;
       int ind = 0;
       boolean trouve = false;
       ArrayList<Objet> liste = GestionnaireDesObjets.getListeDesObjets(); //On récupère la liste des objets
       while((!trouve) && (ind < liste.size())){ //On recherche la machine à fromage
           if(liste.get(ind).getType() == Machine_Fromage){ //Si c'est la machine à fromage
               res = (Machine_Fromage) liste.get(ind); //On renvoit la machine à fromage
               trouve = true; //On sort de la boucle
           } else {
               ind++; //Sinon on augmente l'indice
           }
       }
       return res;
   }
   
   /**
    * Permet de savoir si un oeuf est disponible
    * @return true si un oeuf est disponible
    */
   private boolean isThereOeuf(){
        return (Gerant.get().getListeOeuf(this).size() > 0); //On vérifie si la liste des oeufs disponibles est remplie ou non
   } 
   
   /**
    * Permet de recupérer l'oeuf le plus proche disponible sur le map
    * @return l'oeuf le plus proche à aller chercher
    */
   private Oeuf getOeufPlusProche(){
       Oeuf res = null;
       int[] chemin;
       int distanceMin = 1000, distance, SommetObjet;
       for(Oeuf o : Gerant.get().getListeOeuf(this)){ //On cherche l'oeuf qui fatiguera le moins Abigail
            SommetObjet = getGraphe().getSommet(o.getCase());
            chemin = getGraphe().Dijkstra(SommetPerso, SommetObjet); //On récupère le chemin potentiel
            distance = getGraphe().poidsChemin(chemin, SommetPerso); //On calcule le poids du chemin 
            if(distance < distanceMin){ //Si la distance est inférieure à la distance minimale trouvée précedemment
                distanceMin = distance; //On met à jour la distance minimale
                res = o; //On prévoit de renvoyer cet oeuf
            }
       }
       return res;
   }
   
    /**
     * Permet de calculer et de retourner le chemin le moins fatigant vers une case
     * @param laCase la case vers laquelle aller
     * @return le chemin sous la forme d'un tableau d'entier contenant le successeur de chaque sommet
     */
    public int[] getChemin(Case laCase){
       int SommetCase = getGraphe().getSommet(laCase);
       return getGraphe().Dijkstra(SommetPerso, SommetCase);
   }
   
   /**
    * Permet de retourner le mouvement nécessaire à Abigail pour parcourir le chemin
    * @return le mouvement à réaliser
    */
   private Enum_Action moveComplexe(){
       Enum_Action action = attendre;
       int destination = cheminCourant[SommetPerso];
       if(!estEpuise()){ //Si Abigail n'est pas épuisée
            if(SommetPerso == destination - 1){ //On vérifie si le sommet suivant est à droite
                action = moveRight;
            } else if(SommetPerso == destination + 1){ //à gauche
                action = moveLeft;
            } else if(SommetPerso == destination + 30){ //en haut
                action = moveTop;
            } else if(SommetPerso == destination - 30){ //ou en bas
                action = moveBottom;
            }
            augmenteFatigue(SommetPerso,destination); //On augmente la fatigue d'Abigail
            if(soif <100) soif++; //On augmente la soif d'Abigail
       }
       return action;
   }
   
   /*********************************************/
   /* Méthodes permettant la getion de la vache */
   /*********************************************/
   
   /**
    * Permet d'obtenir l'instance de la vache
    * @return la vache
    */
   private Vache getVache(){
       boolean sortie = false;
       int i=0,
       max = GestionnaireDesPersonnages.getListeDesPersonnages().size();
       while((i<max)&& !sortie){
           if(GestionnaireDesPersonnages.getListeDesPersonnages().get(i).getType() == Vache)
           {
               sortie = true;
           } else {
               i++;
           }
       }
       return (Vache)GestionnaireDesPersonnages.getListeDesPersonnages().get(i);
   }
   
   /**
     * Permet d'empecher la vache de bouger pour pouvoir la rejoindre
     */
    private void arreterVache(){
        Vache vache = getVache();
        IA_Vache ia = (IA_Vache)vache.getIA();
        ia.doitAttendre();
    }
    
    /**
     * Permet de faire bouger à nouveau la vache
     */
    private void libererVache(){
        Vache vache = getVache();
        IA_Vache ia = (IA_Vache)vache.getIA();
        ia.peutBouger();
    }
   
     /**
    * Permet de savoir si la vache a produit du lait ou pas
    * @return true si la vache à produit du lait
    */
   private boolean lait(){
       return getVache().lait();
   }
   
   /********************************************************/
   /* Méthodes permettant la gestion des besoins d'Abigail */
   /********************************************************/
   
   /**
    * Permet d'augmenter la fatigue d'abigail par rapport à la case où elle se rend
    * @param SommetPerso la case actuelle d'abigail 
    * @param Destination la destination d'abigail
    */
   private void augmenteFatigue(int SommetPerso, int Destination){
       int temp = 0;
       if(estFatigue()){ //Si Abigail est fatiguée, les cases la fatigue suivant leur type
            switch (getGraphe().getMatrice(SommetPerso, Destination)){ //On récupère le poids de l'arc en question
                       case 1 : temp++;break;
                       case 2 : temp += 2;break;
                       case 3 : temp += 3;break;
            }
       }else{
           temp++; //Sinon Abigail verra sa fatigue augemnter de 1
       }
       if(getCycleJour() == ApresMidi){ //Si on est l'après midi
           temp *= 2; //La fatigue est doublée
       }
       fatigue += temp; //On augmente enfin la fatigue
   }
   
   /**
    * Permet de connaître la fatigue d'Abigail si elle suivait le chemin donné
    * @param Succ le tableau de successeur
    * @return la fatigue d'Abigail à l'arrivée
    */
   public int calculFatigue(int[] Succ){
       int res = fatigue;
       int courant = SommetPerso; 
       int time = timer; //On sauvegarde l'heure qu'il est
       while(Succ[courant] != -1){ //On parcourt le chemin
           if(res < 50){ //On vérifie la fatigue qu'aurait Abigail à ce moment
                if(getCycleJour() == ApresMidi){ //On vérifie l'heure qu'il serait
                    res +=2;
                }else{
                    res++;
                }  
           }else{
               if(getCycleJour() == ApresMidi){ //Même chose dans le cas où Abigail serait fatiguée
                    res += (getGraphe().getMatrice(courant,Succ[courant]))*2;
               }else{
                    res += (getGraphe().getMatrice(courant,Succ[courant]));
               }
           }
           courant = Succ[courant];
           avanceTemps(); //On fait avancer le temps
        }
       timer = time; //On remet l'heure de départ
       return res;
   }
   
    /**
     * Permet a Abigail de manger du fromage et de réinitialiser sa faim à 0
     */
    private void mangerFromage() {
        if(personnage().getType() == Abigail){
            ((Abigail)personnage()).mangerFromage();
        } else {
           ((Doppelganger)personnage()).mangerFromage();
        }
        System.out.println(nom+" mange du fromage");
        faim = 0;
        affEtat();
    }
    
    /**
     * Permet a Abigail d'étancher sa soif en buvant le lait qu'elle porte
     */
    private void boireDuLait() {
       if(personnage().getType() == Abigail){
            ((Abigail)personnage()).mangerFromage();
        } else {
           ((Doppelganger)personnage()).mangerFromage();
        }
        System.out.println(nom+" boit du lait");
        soif = 0;
        affEtat();
    }
    
    /**
     * Permet à Abigail de se reposer
     */
    private void fairePause(){
        System.out.println(nom+" fait une pause");
        if(fatigue >= 50){
            fatigue -= 50;
        } else {
            fatigue = 0;
        }
    }
    
    /***********************************************************************/
    /* Méthodes permettant de gérer les informations sur l'état d'abigail */
    /***********************************************************************/
        
    /**
     * Permet de modifier le nom d'abigail car elle a une soeur
     * @param nom : le nouveau nom 
     */
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    /**
    * Permet de savoir si Abigail possède du lait sur elle
    * @return true si Abigail a du lait
    */
   public boolean aDuLait(){
       boolean res; 
       if(personnage().getType() == Abigail){
            res = ((Abigail)personnage()).lait();
        } else {
           res = ((Doppelganger)personnage()).lait();
        }
        return res;
       
   }
   
   /**
    * Permet de savoir si Abigail possède du fromage sur elle
    * @return true si Abigail a du fromage
    */
   private boolean aDuFromage(){
       boolean res; 
       if(personnage().getType() == Abigail){
            res = ((Abigail)personnage()).fromage();
        } else {
           res = ((Doppelganger)personnage()).fromage();
        }
        return res;
   }
   
   /**
    * Permet de savoir si Abigail se trouve sur la case ciblée actuellement
    * @return true si Abigail est sur la cible
    */
   private boolean onTarget(){
       return ((target.getColonne() == this.personnage().getCase().getColonne())&&(target.getLigne() == this.personnage().getCase().getLigne()));
   }
   
   /**
    * Permet de savoir si Abigail à faim
    * @return vrai si la valeur de la faim est au dessus de 70
    */
   public boolean aFaim(){
       return faim >= 70;
   }
   
   /**
    * Permet de savoir si ABigail a soif
    * @return vrai si la valeur de la soif est au dessu de 70
    */
   public boolean aSoif(){
       return soif >= 70;
   }
   
   /**
    * Permet de savoir si Abigail est fatigué
    * @return vrai si la fatigue est supérieur à 50
    */
   private boolean estFatigue(){
       return fatigue >= 50;
   }
   
   /**
    * Permet de savoie quand abigail est epuise et donc ne peux plus bouger
    * @return vrai si la fatigue est à 100
    */
   private boolean estEpuise(){
       return fatigue >= 100;
   }
   
   /**
     * Permet d'obtenir la case ciblée par Abigail
     * @return la case ciblée
     */
    public Case getTarget() {
        return target;
    }
    
    /**
     * Renvoie la valeur de la case sur laquel abigail se trouve actuellement
     * @return la valeur de la case;
     */
    public int getSommetPerso() {
        return SommetPerso;
    }
    
    /**
     * Permet d'afficher les différentes informations d'abigail tel que la fatigue la faim la soif, le moment de la journée avec l'heure,
     * et si elle possèd du lait et du fromage 
     */
    private void affEtat(){
        String info = "Heure : " + timer/10 + "h (" + getCycleJour() + ")";
        String Sfatigue = "Fatigue : " + this.fatigue;
        if(fatigue >= 100){
            Sfatigue += " /!\\/!\\/!\\ Est morte de fatigue (normalement)";
        } else if (estEpuise()){
            Sfatigue += " /!\\/!\\ Epuisement";
        } else if(estFatigue()) {
            Sfatigue += " /!\\ Est fatiguée";
        }
        String Ssoif = "Soif : " + this.soif;
        if(soif >= 100){
            Ssoif += " /!\\/!\\/!\\ Est morte de soif (normalement)";
        } else if(aSoif()){
            Ssoif += " /!\\ A soif";
        }
        String Sfaim = "Faim : " + this.faim;
        if(faim >= 100){
            Sfaim += " /!\\/!\\/!\\ Est morte de faim (normalement)";
        } else if(aFaim()){
            Sfaim += " /!\\ Est affamée";
        }
        System.out.println(nom + "\n" + info + "\n" + Sfatigue + "\n" + Ssoif + "\n" + Sfaim + "\nLait : "+aDuLait()+ "\tFromage : " + aDuFromage() + "\n");
    }
}   
       
