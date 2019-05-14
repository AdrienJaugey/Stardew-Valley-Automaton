/*
 * Abigail
 */
package stardewvalleyautomaton.Model.Personnages;

import java.util.ArrayList;
import stardewvalleyautomaton.Model.Carte;
import stardewvalleyautomaton.Model.Cases.Case;
import static stardewvalleyautomaton.Model.Objets.Enum_Objet.Machine_Fromage;
import static stardewvalleyautomaton.Model.Objets.Enum_Objet.Oeuf;
import static stardewvalleyautomaton.Model.Personnages.Enum_Personnage.Abigail;
import static stardewvalleyautomaton.Model.Personnages.Enum_Personnage.Vache;
import stardewvalleyautomaton.Model.Personnages.IA.Enum_Action;
import stardewvalleyautomaton.Model.Personnages.IA.IA;

/**
 *
 * @author simonetma
 */
public class Abigail extends Personnage {

    private boolean lait;                                                       //porte-t-elle du lait ?
    private boolean fromage;                                                    //porte-t-elle du fromage ?
    
    //CONSTRUCTEUR -------------------------------------------------------------
    public Abigail(Case _saCase,IA _ia) {
        super(_saCase,_ia);
        this.lait = false;
        this.fromage = false;
    }

    @Override
    public Enum_Personnage getType() {
        return Abigail;
    }
    
    //ACTIONS POSSIBLES --------------------------------------------------------
    //traire les vaches voisines
    public void traire() {
        //récupère les vaches autour d'elle
        ArrayList<Personnage> listeDesVaches = new ArrayList<>();
        int ligne = this.getCase().getLigne();
        int colonne = this.getCase().getColonne();
        if(Carte.get().getPersonnage(ligne-1, colonne) != null) {
            if(Carte.get().getPersonnage(ligne-1, colonne).getType() == Vache) {
                listeDesVaches.add(Carte.get().getPersonnage(ligne-1, colonne));
            }
        }
        if(Carte.get().getPersonnage(ligne+1, colonne) != null) {
            if(Carte.get().getPersonnage(ligne+1, colonne).getType() == Vache) {
                listeDesVaches.add(Carte.get().getPersonnage(ligne+1, colonne));
            }
        }
        if(Carte.get().getPersonnage(ligne, colonne-1) != null) {
            if(Carte.get().getPersonnage(ligne, colonne-1).getType() == Vache) {
                listeDesVaches.add(Carte.get().getPersonnage(ligne, colonne-1));
            }
        }
        if(Carte.get().getPersonnage(ligne, colonne+1) != null) {
            if(Carte.get().getPersonnage(ligne, colonne+1).getType() == Vache) {
                listeDesVaches.add(Carte.get().getPersonnage(ligne, colonne+1));
            }
        }
        
        //s'il n'y a pas de vache autour d'Abigail
        if(listeDesVaches.isEmpty()) {
            System.err.println("Il n'y a pas de vache ici !");
        }
        
        //traite des vaches voisines d'Abigail
        for(Personnage vache : listeDesVaches) {
            if(lait) {
                System.out.println("Abigail a déjà du lait sur elle");
            }
            else if(((Vache) vache).lait()) {
                ((Vache) vache).traire();
                this.lait = true;
            }
            else {
                System.out.println("La vache n'a pas de lait");
            }
        }
    }
    
    //produire du fromage
    public void produireFromage() {
        //repère si Abigail est à côté d'une machine
        boolean presenceMachine = false;
        int ligne = this.getCase().getLigne();
        int colonne = this.getCase().getColonne();
        if(Carte.get().getObjet(ligne-1, colonne) != null) {
            if(Carte.get().getObjet(ligne-1, colonne).getType() == Machine_Fromage) {
                presenceMachine = true;
            }
        }
        if(Carte.get().getObjet(ligne+1, colonne) != null) {
            if(Carte.get().getObjet(ligne+1, colonne).getType() == Machine_Fromage) {
                presenceMachine = true;
            }
        }
        if(Carte.get().getObjet(ligne, colonne-1) != null) {
            if(Carte.get().getObjet(ligne, colonne-1).getType() == Machine_Fromage) {
                presenceMachine = true;
            }
        }
        if(Carte.get().getObjet(ligne, colonne+1) != null) {
            if(Carte.get().getObjet(ligne, colonne+1).getType() == Machine_Fromage) {
                presenceMachine = true;
            }
        }
        
        //Abigail doit avoir du lait, pas de fromage et doit être proche d'une machine
        if(presenceMachine && this.lait && !this.fromage) {
            this.lait = false;
            this.fromage = true;
            System.out.println("Abigail a fait du fromage");
        }
        else {
            if(this.fromage) {
                System.err.println("Abigail a déjà du fromage sur elle !");
            }
            if(!this.lait) {
                System.err.println("Abigail n'a pas de lait !");
            }
            if(!presenceMachine) {
                System.err.println("Abigail n'est pas à côté de la machine !");
            }
        }
    }

    //Collecter un oeuf présent sur la case
    public void collecterOeuf() {
        boolean success = false;
        if(this.getCase().getObjet() != null) {
            if(this.getCase().getObjet().getType() == Oeuf) {
                this.getCase().removeObjet();
                success = true;
            }
        }
        
        if(success) {
            System.out.println("Abigail a ramassé un oeuf");
        }
        else {
            System.err.println("Il n'y a pas d'oeuf ici !");
        }
    }
    
    @Override
    public void actionSpeciale(Enum_Action action) {
        switch(action) {
            case traire: traire(); break;
            case produireFromage: produireFromage(); break;
            case collecterOeuf: collecterOeuf(); break;
        }
    }
    
    public boolean lait(){
        return lait;
    }
    
    public boolean fromage(){
        return fromage;
    }
    
    public void mangerFromage(){
        fromage = false;
    }
    
    public void boireLait(){
        lait = false;
    }
}
