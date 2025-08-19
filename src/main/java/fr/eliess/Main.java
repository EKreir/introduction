package fr.eliess;

import com.mysql.cj.jdbc.Driver;
import fr.eliess.basics.GestionEleves;
import fr.eliess.dao.ConnexionBDD;
import fr.eliess.dao.CourseDAO;
import fr.eliess.dao.StudentDAO;
import fr.eliess.model.Course;
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

        // ===== Instantiation des DAO =====
        StudentDAO studentDAO = new StudentDAO(em);
        CourseDAO courseDAO = new CourseDAO(em);

        try {

            // Création de quelques étudiants et cours
            Student alice = new Student("Rayan", 18);
            Student bob = new Student("Fahd", 24);

            Course math = new Course("Espagnol");
            Course physics = new Course("Technologie");

            // Persistance via DAO
            studentDAO.create(alice);
            studentDAO.create(bob);
            courseDAO.create(math);
            courseDAO.create(physics);

            // Lier les étudiants aux cours
            alice.getCourses().add(math);
            math.getStudents().add(alice);

            alice.getCourses().add(physics);
            physics.getStudents().add(alice);

            bob.getCourses().add(math);
            math.getStudents().add(bob);

            // MAJ des entités après les liaisons
            studentDAO.update(alice);
            studentDAO.update(bob);

            logger.info("Étudiants et cours persistés avec succès via DAO");

            // Lecture avec requête (toujours possible !)
            List<Student> students = em.createQuery(
                    "SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses", Student.class
            ).getResultList();

            // Affichage clair
            for (Student s : students) {
                System.out.print(s.getName() + " suit les cours : ");
                if (s.getCourses().isEmpty()) {
                    System.out.println("aucun cours");
                } else {
                    String courseTitles = s.getCourses().stream()
                            .map(Course::getTitle)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                    System.out.println(courseTitles);
                }
            }

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

    /*
    Requête avec LEFT JOIN FETCH :
    SELECT DISTINCT s -> récupère chaque Student une seule fois.
    LEFT JOIN FETCH s.courses -> récupère les cours de chaque étudiant en même temps, évitant le problème N+1.
    Résultat : liste de Student avec leurs courses chargés en mémoire.

    Récupération et affichage des titres :
    s.getCourses().stream() -> parcourt tous les cours de l’étudiant.
    .map(Course::getTitle) -> extrait le titre de chaque cours.
    .reduce((a, b) -> a + ", " + b) -> concatène tous les titres séparés par des virgules.
    .orElse("") -> si l’étudiant n’a aucun cours, renvoie une chaîne vide.
    System.out.println -> affiche la liste des titres.

    ================================================================

    Après modifications (ajout des cascades et suppression des méthodes addCourse() et addStudent()) :

    alice.getCourses().add(math);
    math.getStudents().add(alice);

    On met les 2 côtés de la relation à jour en mémoire
    Hibernate va détecter qu'on a modifié la relation
    et va générer les bonnes lignes dans la table de jointure student_course lors du commit().
    Même si on avait utilisé tes méthodes addCourse / addStudent,
    le résultat final dans la base de données aurait été identique.
    La différence est surtout dans la clarté et la sécurité du code.

    Résumé des modifications :

    Les constructeurs personnalisés simplifient la création d’objets et rendent le code plus lisible.
    JPA/Hibernate utilise toujours le constructeur sans argument
    pour instancier les entités depuis la base.
    Les changements côté code ne changent pas le comportement final si la logique métier
    et les relations sont respectées.

    ================================================================



    */
}