package fr.eliess;

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

import java.util.List;
import java.util.stream.Collectors;

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

        logger.info("🔧 Chargement du fichier persistence.xml : {}",
                Main.class.getClassLoader().getResource("META-INF/persistence.xml"));

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("studentPU");
        EntityManager em = emf.createEntityManager();

        StudentDAO studentDAO = new StudentDAO(em);
        CourseDAO courseDAO = new CourseDAO(em);
        TeacherDAO teacherDAO = new TeacherDAO(em);

        var tx = em.getTransaction();

        try {
            tx.begin(); // Une seule transaction pour tout

            // =========================
            // 1️⃣ Création des profils et étudiants
            // =========================
            StudentProfile profileRayan = new StudentProfile("123 Rue Principale", "0601020304");
            StudentProfile profileFahd = new StudentProfile("456 Avenue Centrale", "0605060708");

            Student rayan = new Student("Rayan", 18);
            rayan.setProfile(profileRayan);
            profileRayan.setStudent(rayan);

            Student fahd = new Student("Fahd", 24);
            fahd.setProfile(profileFahd);
            profileFahd.setStudent(fahd);

            // =========================
            // 2️⃣ Création des cours et du professeur
            // =========================
            Course maths = new Course("Maths");
            Course physics = new Course("Physique");

            Teacher mrSmith = new Teacher("Mr. Smith");
            mrSmith.addCourse(maths);
            mrSmith.addCourse(physics);

            // =========================
            // 3️⃣ Associations étudiants ↔ cours
            // =========================
            rayan.addCourse(maths);
            rayan.addCourse(physics);
            fahd.addCourse(physics);

            // =========================
            // 4️⃣ Persistance
            // =========================
            teacherDAO.create(mrSmith);
            studentDAO.create(rayan);
            studentDAO.create(fahd);

            // =========================
            // 5️⃣ Modification : profil de Rayan + ajout d'un cours
            // =========================
            rayan.getProfile().setAddress("999 Nouvelle Adresse");
            rayan.getProfile().setPhone("0611223344");

            Course english = new Course("Anglais");
            rayan.addCourse(english);
            courseDAO.create(english);

            // =========================
            // 6️⃣ Affichage final
            // =========================
            displayAllStudents(studentDAO);
            displayTeacherWithCourses(teacherDAO, mrSmith.getId());

            tx.commit(); // commit unique pour tout le bloc
            logger.info("💾 Toutes les opérations effectuées avec succès");

        } catch (Exception e) {
            logger.error("❌ Erreur, rollback en cours", e);
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
            emf.close();
            logger.info("🧹 EntityManager fermé");
        }
    }

    private static void displayAllStudents(StudentDAO studentDAO) {
        List<Student> students = studentDAO.findAllWithCourses();
        System.out.println("\n📚 Liste des étudiants :");
        for (Student s : students) {
            String profileInfo = (s.getProfile() != null)
                    ? s.getProfile().getAddress() + " | " + s.getProfile().getPhone()
                    : "aucun profil";
            String courses = s.getCourses().stream()
                    .map(Course::getTitle)
                    .collect(Collectors.joining(", "));
            System.out.println(s.getName() + " (Profil: " + profileInfo + ") suit : " +
                    (courses.isEmpty() ? "aucun cours" : courses));
        }
    }

    private static void displayTeacherWithCourses(TeacherDAO teacherDAO, Long teacherId) {
        Teacher teacher = teacherDAO.findWithCourses(teacherId);
        String courses = teacher.getCourses().stream()
                .map(Course::getTitle)
                .collect(Collectors.joining(", "));
        System.out.println("\n👨‍🏫 Professeur : " + teacher.getName());
        System.out.println("Enseigne : " + (courses.isEmpty() ? "aucun cours" : courses));
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

    Points clés

    Bidirectionnel One-to-One :
    Student garde profile_id (côté propriétaire).
    StudentProfile a mappedBy = "profile" pour naviguer vers l’étudiant.

    Liaisons mises à jour avant persistance :
    alice.setProfile(profileAlice)
    profileAlice.setStudent(alice)

    Persistance sécurisée :
    CascadeType.ALL sur le profile permet d’insérer le StudentProfile automatiquement.
    Évite les NullPointerException lors de l’accès à getProfile().

    Affichage des profils et cours fonctionne correctement.

    =======================================================================

    Points importants

    Une seule transaction tx.begin() … tx.commit() pour toutes les opérations.

    Rollback si quelque chose échoue.
    Les StudentProfile sont persistés automatiquement grâce à cascade = CascadeType.ALL sur la relation One-to-One.

    Aucun flush() manuel nécessaire ici, Hibernate le fait au commit().

    =====================================================================

    On a simplifié les logs et les commentaires -> on garde juste l’essentiel.

    On a ajouté profile.setStudent(student) pour bien relier les deux côtés du OneToOne.

    On utilise tes méthodes utilitaires (addCourse, addCourse côté prof) plutôt que de bricoler les deux côtés à la main.

    Avec ça, on devrait pouvoir voir clairement dans la console :

    chaque étudiant avec son profil et ses cours,

    le professeur avec ses cours.

    ======================================================================

    Points clés de cette version :

    Une seule transaction tx.begin() -> tx.commit() pour tout : création, modification et affichage.

    La persistance du nouveau cours se fait avec courseDAO.create(english)
    et JPA/Hibernate gère la liaison avec Rayan.

    Les méthodes utilitaires pour afficher étudiants et professeurs restent claires et réutilisables.

    Plus besoin de récupérer Rayan depuis la DB avant de modifier son profil
     il est déjà attaché au EntityManager car créé dans la même transaction.



    */
