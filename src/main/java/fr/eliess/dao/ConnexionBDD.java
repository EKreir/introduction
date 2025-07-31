package fr.eliess.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionBDD {

    private static final String URL = "jdbc:mysql://localhost:3306/javaexos?serverTimezone=UTC";
    private static final String UTILISATEUR = "eliess";
    private static final String PASSWORD = "Eliess2001#@!";

    public static Connection getConnexion() throws SQLException {

        return DriverManager.getConnection(URL, UTILISATEUR, PASSWORD);

    }

}
