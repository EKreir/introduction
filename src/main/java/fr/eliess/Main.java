package fr.eliess;

import com.mysql.cj.jdbc.Driver;
import fr.eliess.basics.GestionEleves;
import fr.eliess.dao.ConnexionBDD;
import fr.eliess.model.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {

        // Code précédent temporairement désactivé
        /*
        System.out.println("Bienvenue dans le projet Maven!");
        System.out.println("Driver MySQL : " + Driver.class.getName());

        GestionEleves gestion = new GestionEleves();
        gestion.demarrer();

        try (Connection conn = ConnexionBDD.getConnexion()) {
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        */

        // === TEST HIBERNATE / JPA ===
        logger.info("🔧 Chargement du fichier persistence.xml : {}", Main.class.getClassLoader().getResource("META-INF/persistence.xml"));

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("studentPU");
        EntityManager em = emf.createEntityManager();

        try {

            logger.info("🔄Démarrage de la transaction");
            em.getTransaction().begin();

            Student student = new Student("Bertrant", 23);
            em.persist(student);

            em.getTransaction().commit();

            logger.info("Étudiant persisté avec succès");

            List<Student> students = em.createQuery("SELECT s FROM Student s", Student.class).getResultList();
            students.forEach(System.out::println);

        } catch (Exception e) {
            logger.error("Une erreur est survenue", e);
            e.printStackTrace();
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
            emf.close();
            logger.info("🧹Ressources EntityManager fermées");
        }
    }
}