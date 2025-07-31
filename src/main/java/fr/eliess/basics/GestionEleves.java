package fr.eliess.basics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.eliess.dao.EleveDao;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestionEleves {

    private final List<Eleve> eleves = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private final String fichierEleves = "eleves.json";
    private final EleveDao eleveDao = new EleveDao();

    //Appeler cette méthode à la fin du programme, pour sauvegarder
    public void sauvegarderEleves() {

        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(fichierEleves)) {

            gson.toJson(eleves, writer);
            System.out.println("Liste des élèves sauvegardée.");

        } catch (IOException e) {

            System.out.println("Erreur lors de la sauvegarde : " + e.getMessage());

        }

    }

    //Appeler cette méthode au démarrage pour charger la liste
    public void chargerEleves() {

        Gson gson = new Gson();

        try (FileReader reader = new FileReader(fichierEleves)) {

            Type elevesListType = new TypeToken<ArrayList<Eleve>>(){}.getType();
            List<Eleve> loadedEleves = gson.fromJson(reader, elevesListType);

            if (loadedEleves != null) {

                eleves.clear();
                eleves.addAll(loadedEleves);
                System.out.println("Liste des élèves chargée.");

            }

        } catch (IOException e) {

            System.out.println("Aucun fichier d'élèves trouvé, démarrage avec liste vide.");

        }

    }

    public void demarrer() {

        chargerEleves(); // charge la liste au démarrage

        int choix;

        do {

            afficherMenu();
            choix = scanner.nextInt();
            scanner.nextLine(); // retour ligne

            switch (choix) {

                case 1 -> ajouterEleve();
                case 2 -> afficherEleves();
                case 3 -> modifierEleve();
                case 4 -> supprimerEleve();
                case 5 -> {

                    System.out.println("Fermeture du programme...");
                    sauvegarderEleves(); // sauvegarde à la fermeture

                }
                default -> System.out.println("Choix invalide !");

            }

        } while (choix != 5);

    }

    private void afficherMenu() {

        System.out.println("\n===== Menu Gestion des Élèves =====");
        System.out.println("1. Ajouter un élève");
        System.out.println("2. Afficher les élèves");
        System.out.println("3. Modifier un élève");
        System.out.println("4. Supprimer un élève");
        System.out.println("5. Quitter");
        System.out.print("Votre choix : ");

    }

    private void ajouterEleve() {

        System.out.print("Nom de l'élève : ");
        String nom = scanner.nextLine();

        System.out.print("Âge de l'élève : ");
        int age = scanner.nextInt();
        scanner.nextLine();

        Eleve eleve = new Eleve(nom, age);
        eleves.add(eleve); // toujours utile localement
        eleveDao.ajouterEleve(eleve); // en base

        System.out.println("Élève ajouté !");

    }

    private void afficherEleves() {

        List<Eleve> elevesEnBase = eleveDao.listeEleves();

        if (elevesEnBase.isEmpty()) {

            System.out.println("Aucun élève enregistré en base.");

        } else {

            System.out.println("\nListe des élèves :");

            for (Eleve eleve : elevesEnBase) {

                System.out.println(eleve.getId() + " - " + eleve);

            }
        }

    }

    private void modifierEleve() {

        afficherEleves();

        System.out.print("ID de l'élève à modifier : ");

        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Nouveau nom : ");
        String nom = scanner.nextLine();

        System.out.print("Nouvel âge : ");

        int age = scanner.nextInt();
        scanner.nextLine();

        Eleve eleve = new Eleve(id, nom, age);

        eleveDao.modifierEleve(eleve);

    }

    private void supprimerEleve() {

        afficherEleves();

        System.out.print("ID de l'élève à supprimer : ");

        int id = scanner.nextInt();
        scanner.nextLine();

        eleveDao.supprimerEleve(id);
    }

}
