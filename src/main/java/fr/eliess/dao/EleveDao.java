package fr.eliess.dao;

import fr.eliess.basics.Eleve;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EleveDao {

    public void ajouterEleve(Eleve eleve) {

        String sql = "INSERT INTO eleves (nom, age) VALUES (?, ?)";

        try (Connection conn = ConnexionBDD.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, eleve.getNom());
            stmt.setInt(2, eleve.getAge());
            stmt.executeUpdate();

        } catch (SQLException e) {

            System.err.println("Erreur ajout élève : " + e.getMessage());

        }

    }

    public List<Eleve> listeEleves() {

        List<Eleve> liste = new ArrayList<>();
        String sql = "SELECT * FROM eleves";

        try (Connection conn = ConnexionBDD.getConnexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                int age = rs.getInt("age");

                liste.add(new Eleve(id, nom, age));
            }

        } catch (SQLException e) {

            System.err.println("Erreur lecture élèves : " + e.getMessage());

        }

        return liste;
    }

    public void modifierEleve(Eleve eleve) {

        String sql = "UPDATE eleves SET nom = ?, age = ? WHERE id = ?";

        try (Connection conn = ConnexionBDD.getConnexion();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, eleve.getNom());
            stmt.setInt(2, eleve.getAge());
            stmt.setInt(3, eleve.getId());

            int lignes = stmt.executeUpdate();

            if (lignes > 0) {

                System.out.println("Elève modifié en base.");

            } else {

                System.out.println("Aucun élève trouvé avec cet ID.");

            }

        } catch (SQLException e) {

            System.err.println("Erreur modification élève : " + e.getMessage());

        }

    }

    public void supprimerEleve(int id) {

        String sql = "DELETE FROM eleves WHERE id = ?";

        try (Connection conn = ConnexionBDD.getConnexion();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int lignes = stmt.executeUpdate();

            if (lignes > 0) {

                System.out.println("Elève supprimé de la base.");

            } else {

                System.out.println("Aucun élève trouvé avec cet ID.");

            }

        } catch (SQLException e) {

            System.err.println("Erreur suppression élève : " + e.getMessage());

        }

    }

}
