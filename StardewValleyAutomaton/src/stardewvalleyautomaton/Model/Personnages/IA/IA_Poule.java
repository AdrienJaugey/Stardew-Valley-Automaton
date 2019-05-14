/*
 * ia d'une poule
 */
package stardewvalleyautomaton.Model.Personnages.IA;

import java.util.ArrayList;
import java.util.Random;
import stardewvalleyautomaton.Model.Carte;
import stardewvalleyautomaton.Model.Cases.Case;
import static stardewvalleyautomaton.Model.Objets.Enum_Objet.Barriere;
import static stardewvalleyautomaton.Model.Personnages.IA.Enum_Action.*;

/**
 *
 * @author Matthieu
 */
public class IA_Poule extends IA {


    private int nbAction = 1;
    
    
    @Override
    protected void setActionValide() {
        this.addActionValide(attendre);
        this.addActionValide(moveLeft);
        this.addActionValide(moveRight);
        this.addActionValide(moveTop);
        this.addActionValide(moveBottom);
        this.addActionValide(pondre);
    }
    
    @Override
    public Enum_Action action() {
        Enum_Action resultat;
        
        //liste toutes les actions que la poule peut faire
        ArrayList<Enum_Action> actionPossible = new ArrayList<>();
        actionPossible.add(attendre);
        Case positionActuelle = this.personnage().getCase();
        int ligne = positionActuelle.getLigne();
        int colonne = positionActuelle.getColonne();
        
       if(colonne-1 >= 0) {
            if(PeutPasser(ligne,(colonne-1))==true) {
                actionPossible.add(moveLeft);
            }
        }
        if(colonne+1 < Carte.get().taille()) {
            if(PeutPasser(ligne,(colonne+1))==true) {
                actionPossible.add(moveRight);
            }
        }
        if(ligne-1 >= 0) {
            if(PeutPasser((ligne-1),colonne)==true) {
                actionPossible.add(moveTop);
            }
        }
        if(ligne+1 < Carte.get().taille()==true) {
            if(PeutPasser((ligne+1),colonne)) {
                actionPossible.add(moveBottom);
            }
        }
        
        //choisie une action au hasard
        Random random = new Random();
        int alea = random.nextInt(actionPossible.size());
        resultat = actionPossible.get(alea);
        
        
        //GESTION DE LA PONTE
        if(resultat == attendre) {
            if(random.nextInt(10)==0) {
                resultat = pondre;
            }
        }
        
        return resultat;
    }
    
    /**
     * Permet de savoir si une case est entourée de deux barrières sur la même ligne ou colonne et donc de savoir si la poule peut passer
     * @param l la ligne de la case à tester
     * @param c la colonne de la case à tester
     * @return true si la poule peut passer, false sinon
     */
     public boolean PeutPasser(int l, int c){  
      Case laCase = Carte.get().getCase(l, c);
      boolean res = laCase.estLibre(); 
      
      if(res){
          //On récupère les cases adjacentes
          Case caseg = Carte.get().getCase(l, c-1); //gauche
          Case cased = Carte.get().getCase(l, c+1); //droite
          Case caseh = Carte.get().getCase(l+1, c); //haut
          Case caseb = Carte.get().getCase(l-1, c); //bas
          
          if((caseg.getObjet() != null) && (cased.getObjet() != null)){ //Si la case de gauche et celle de droite on un objet
            if((caseg.getObjet().getType() == Barriere) && (cased.getObjet().getType() == Barriere)){ //Si ces deux objets sont des barrières
              res = false; //La poule ne pourra pas passer
            }  
          }
          if((caseh.getObjet() != null) && (caseb.getObjet() != null)){ //Même chose avec la case du haut et du bas
            if((caseh.getObjet().getType()==Barriere) && (caseb.getObjet().getType()== Barriere)){
              res = false;
            }  
          }
      }
      return res;
   }
  }
       
                   
    

