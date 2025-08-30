package fr.eliess;

import com.mysql.cj.jdbc.Driver;
import fr.eliess.basics.GestionEleves;
import fr.eliess.dao.ConnexionBDD;
import fr.eliess.dao.CourseDAO;
import fr.eliess.dao.StudentDAO;
import fr.eliess.dao.TeacherDAO;
import fr.eliess.model.Course;
import fr.eliess.model.Student;
import fr.eliess.model.StudentProfile;
import fr.eliess.model.Teacher;
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

        // Code précédent temporairement désactivé de la connexion à MySQL sans JPA / Hibernate
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
        logger.info("🔧 Chargement du fichier persistence.xml : {}",
                Main.class.getClassLoader().getResource("META-INF/persistence.xml"));

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("studentPU");
        EntityManager em = emf.createEntityManager();

        // Instanciation des DAO
        StudentDAO studentDAO = new StudentDAO(em);
        CourseDAO courseDAO = new CourseDAO(em);
        TeacherDAO teacherDAO = new TeacherDAO(em);

        try {
            em.getTransaction().begin();

            // === Création des profils étudiants ===
            StudentProfile profileAlice = new StudentProfile("123 Rue Principale", "0601020304");
            StudentProfile profileBob = new StudentProfile("456 Avenue Centrale", "0605060708");

            // === Création des étudiants ===
            Student alice = new Student("Rayan", 18);
            alice.setProfile(profileAlice);

            Student bob = new Student("Fahd", 24);
            bob.setProfile(profileBob);

            // === Création des cours ===
            Course math = new Course("Espagnol");
            Course physics = new Course("Technologie");

            // === Création du professeur et association aux cours ===
            Teacher mrSmith = new Teacher("Mr. Smith");
            mrSmith.addCourse(math);
            mrSmith.addCourse(physics);

            // Persistance du professeur et des cours (cascade)
            teacherDAO.create(mrSmith);

            // === Lier étudiants aux cours ===
            alice.getCourses().add(math);
            math.getStudents().add(alice);

            alice.getCourses().add(physics);
            physics.getStudents().add(alice);

            bob.getCourses().add(math);
            math.getStudents().add(bob);

            // Persistance des étudiants (profile inclus via @Embedded)
            studentDAO.create(alice);
            studentDAO.create(bob);

            // Flush pour synchroniser la DB et éviter les NPE
            em.flush();

            // === Affichage des étudiants avec profils et cours ===
            List<Student> students = studentDAO.findAllWithCourses();
            for (Student s : students) {
                String profileInfo = (s.getProfile() != null)
                        ? s.getProfile().getAddress() + ", " + s.getProfile().getPhone()
                        : "aucun profil";
                System.out.print(s.getName() + " (Profil: " + profileInfo + ") suit les cours : ");
                String courseTitles = s.getCourses().stream()
                        .map(Course::getTitle)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("aucun cours");
                System.out.println(courseTitles);
            }

            // === Affichage du professeur et ses cours ===
            Teacher teacherFromDb = teacherDAO.findWithCourses(mrSmith.getId());
            System.out.println("\nProfesseur : " + teacherFromDb.getName());
            String teacherCourses = teacherFromDb.getCourses().stream()
                    .map(Course::getTitle)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("aucun cours");
            System.out.println("Enseigne les cours : " + teacherCourses);

            em.getTransaction().commit();

            logger.info("💾 Étudiants, profils, cours et professeur persistés avec succès");

        } catch (Exception e) {
            logger.error("Une erreur est survenue", e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
            emf.close();
            logger.info("🧹 Ressources EntityManager fermées");
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