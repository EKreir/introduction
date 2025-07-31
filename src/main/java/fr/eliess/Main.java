package fr.eliess;

import com.mysql.cj.jdbc.Driver;
import fr.eliess.basics.GestionEleves;
import fr.eliess.dao.ConnexionBDD;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        System.out.println("Bienvenue dans le projet Maven!");
        System.out.println("Driver MySQL : " + Driver.class.getName());

        GestionEleves gestion = new GestionEleves();
        gestion.demarrer();
/*
        try (Connection conn = ConnexionBDD.getConnexion()) {
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }*/
    }
}