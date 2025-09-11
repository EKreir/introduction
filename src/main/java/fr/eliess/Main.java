package fr.eliess;

import fr.eliess.dao.CourseDAO;
import fr.eliess.dao.StudentDAO;
import fr.eliess.dao.TeacherDAO;
import fr.eliess.dto.CourseWithCountDTO;
import fr.eliess.model.*;
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
            // Création d'étudiants avec Address et Phones
            // =========================
            Student rayan = new Student("Rayan", 18);
            rayan.setAddress(new Address("123 Rue Principale", "Paris", "75001"));
            rayan.getPhones().add("0601020304");
            rayan.getPhones().add("0601122334");

            Student fahd = new Student("Fahd", 24);
            fahd.setAddress(new Address("456 Avenue Centrale", "Lyon", "69002"));
            fahd.getPhones().add("0605060708");

            // =========================
            // Profils
            // =========================
            StudentProfile profileRayan = new StudentProfile("123 Rue Principale", "0601020304");
            profileRayan.setStudent(rayan);
            rayan.setProfile(profileRayan);

            StudentProfile profileFahd = new StudentProfile("456 Avenue Centrale", "0605060708");
            profileFahd.setStudent(fahd);
            fahd.setProfile(profileFahd);

            // =========================
            // Création cours + prof
            // =========================
            Course maths = new Course("Maths");
            Course physics = new Course("Physique");
            Teacher mrSmith = new Teacher("Mr. Smith");
            mrSmith.addCourse(maths);
            mrSmith.addCourse(physics);

            // =========================
            // Associations étudiants ↔ cours
            // =========================
            rayan.addCourse(maths);
            rayan.addCourse(physics);
            fahd.addCourse(physics);

            // =========================
            // Persistance
            // =========================
            teacherDAO.create(mrSmith);
            studentDAO.create(rayan);
            studentDAO.create(fahd);

            // =========================
            // Test @Version et @PreUpdate
            // =========================
            rayan.getAddress().setStreet("999 Nouvelle Adresse"); // déclenche @PreUpdate
            rayan.getPhones().add("0611223344");

            // =========================
            // Affichage étudiants avec Address et Phones
            // =========================
            System.out.println("\n=== Étudiants avec Address et Phones ===");
            List<Student> students = studentDAO.findAllWithCourses();
            for (Student s : students) {
                String profileInfo = (s.getProfile() != null)
                        ? s.getProfile().getAddress() + " | " + s.getProfile().getPhone()
                        : "aucun profil";
                String courses = s.getCourses().stream()
                        .map(Course::getTitle)
                        .collect(Collectors.joining(", "));
                String phones = String.join(", ", s.getPhones());
                String address = (s.getAddress() != null)
                        ? s.getAddress().getStreet() + ", " + s.getAddress().getCity() + " " + s.getAddress().getZip()
                        : "aucune adresse";
                System.out.println(s.getName() + " (Profil: " + profileInfo + ", Address: " + address + ", Phones: " + phones + ") suit : " +
                        (courses.isEmpty() ? "aucun cours" : courses));
            }

            // =========================
            // Affichage des professeurs + leurs cours + étudiants
            // =========================
            System.out.println("\n=== Professeurs et leurs cours ===");
            Teacher teacherFull = teacherDAO.findWithCoursesAndStudents(mrSmith.getId());
            System.out.println("👨‍🏫 " + teacherFull.getName());
            teacherFull.getCourses().forEach(c ->
                    System.out.println("- " + c.getTitle() + " suivi par : " +
                            c.getStudents().stream()
                                    .map(Student::getName)
                                    .collect(Collectors.joining(", ")))
            );

            // =========================
            // Tests Criteria API
            // =========================
            System.out.println("\n=== Étudiants > 20 ans ===");
            List<Student> olderStudents = studentDAO.findByAgeGreaterThan(20);
            olderStudents.forEach(s -> System.out.println(s.getName() + " (" + s.getAge() + " ans)"));

            System.out.println("\n=== Étudiants nommés 'Rayan' et ≥18 ans ===");
            List<Student> filteredStudents = studentDAO.findByDynamicCriteria("Rayan", 18);
            filteredStudents.forEach(s -> System.out.println(s.getName() + " (" + s.getAge() + " ans)"));

            // =========================
            // Tests Named Queries
            // =========================
            System.out.println("\n=== Étudiants avec NamedQuery findByName ===");
            List<Student> studentsNamedRayan = studentDAO.findByName("Rayan");
            studentsNamedRayan.forEach(s ->
                    System.out.println(s.getName() + " (" + s.getAge() + " ans)")
            );

            System.out.println("\n=== Étudiants plus vieux que 20 ans (NamedQuery findOlderThan) ===");
            List<Student> olderStudent = studentDAO.findOlderThan(20);
            olderStudent.forEach(s -> System.out.println(s.getName() + " (" + s.getAge() + " ans)"));

            // =========================
            // NamedNativeQuery : CourseWithCountDTO
            // =========================
            System.out.println("\n=== Nombre d'étudiants par cours (DTO) ===");
            List<CourseWithCountDTO> coursesWithCounts = courseDAO.findAllWithStudentCount();
            coursesWithCounts.forEach(c ->
                    System.out.println(c.getTitle() + " → " + c.getStudentCount() + " étudiants")
            );

            System.out.println("\n=== Cours avec au moins 2 étudiants ===");
            List<CourseWithCountDTO> coursesMin2 = courseDAO.findCoursesWithMinStudents(2);
            coursesMin2.forEach(System.out::println);


            tx.commit(); // commit unique pour tout le bloc
            logger.info("💾 Toutes les opérations effectuées avec succès");

        } catch (Exception e) {
            logger.error(" Erreur, rollback en cours", e);
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
            emf.close();
            logger.info("🧹 EntityManager fermé");
        }
    }

    // === Affichages utilitaires ===
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

    ======================================================================

    Par défaut :

    @ManyToOne et @OneToOne → EAGER

    @OneToMany et @ManyToMany → LAZY

    Ça veut dire que certaines relations sont chargées tout de suite
    (même si tu ne les utilises pas),
    et d’autres seulement quand tu les appelles.


    Changements faits :

    Suppression des anciennes méthodes d’affichage (displayAllStudents, displayTeacherWithCourses) ->
    remplacées par :
    displayStudentWithProfileAndCourses → utilise studentDAO.findWithProfileAndCourses.
    displayTeacherWithCoursesAndStudents → utilise teacherDAO.findWithCoursesAndStudents.

    Tout se fait en une seule transaction pour éviter les problèmes de Lazy Loading.

    On verra en console :
    un étudiant (profil + liste des cours),
    un professeur (cours + étudiants de chaque cours).

    ==========================================================================

    Ajout de Criteria API (jakarta EE / Hibernate)

    Garde tout le flux existant (création -> persistance -> affichages classiques)
    et ajoute à la fin la section Test Criteria API

    On aura dans la console la liste complète des étudiants + professeurs comme avant,
    puis les résultats dynamiques via Criteria.

    Avec ça, la console affichera :

    Les étudiants > 20 ans.
    Les étudiants nommés "Rayan" et âgés ≥ 18 ans.
    Les étudiants avec au moins 18 ans (sans nom).
    Les étudiants nommés Fahd (sans âge).
    Tous les étudiants si tu passes null, null.

    ============================================================================

    Test Criteria API : étudiants + cours :

    Ce que ça fait :

    Hibernate construit une requête SQL unique pour récupérer les étudiants
    et leurs cours.

    Chaque étudiant apparaît une seule fois grâce à distinct(true).

    On évite le Lazy Loading multiple et le problème N+1.

    Tu peux comparer le résultat avec ton ancienne méthode JPQL findAllWithCourses() :
    le résultat sera identique,
    mais la requête est construite dynamiquement.

    */

}