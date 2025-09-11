package fr.eliess;

import fr.eliess.dao.CourseDAO;
import fr.eliess.dao.StudentDAO;
import fr.eliess.dao.TeacherDAO;
import fr.eliess.dto.CourseWithCountDTO;
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
            //️  Création des profils et étudiants
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
            // Création des cours et du professeur
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
            // Modification : profil de Rayan + ajout d'un cours
            // =========================
            rayan.getProfile().setAddress("999 Nouvelle Adresse");
            rayan.getProfile().setPhone("0611223344");

            Course english = new Course("Anglais");
            rayan.addCourse(english);
            courseDAO.create(english);

            // =========================
            // Affichage final (méthodes existantes)
            // =========================
            displayAllStudents(studentDAO);
            displayTeacherWithCourses(teacherDAO, mrSmith.getId());

            Student rayanFromDB = studentDAO.findWithProfileAndCourses(rayan.getId());
            System.out.println("\n👤 Étudiant (profil + cours) : " + rayanFromDB.getName());
            System.out.println("Profil : " + rayanFromDB.getProfile().getAddress()
                    + " | " + rayanFromDB.getProfile().getPhone());
            System.out.println("Cours : " + rayanFromDB.getCourses().stream()
                    .map(Course::getTitle)
                    .collect(Collectors.joining(", ")));

            Teacher teacherFull = teacherDAO.findWithCoursesAndStudents(mrSmith.getId());
            System.out.println("\n👨‍🏫 Professeur (cours + étudiants) : " + teacherFull.getName());
            teacherFull.getCourses().forEach(c ->
                    System.out.println("- " + c.getTitle() + " suivi par : " +
                            c.getStudents().stream()
                                    .map(Student::getName)
                                    .collect(Collectors.joining(", ")))
            );

            // =========================
            // Tests Criteria API
            // =========================
            System.out.println("\n===== Tests Criteria API =====");

            // Étudiants avec âge > 20
            List<Student> olderStudents = studentDAO.findByAgeGreaterThan(20);
            System.out.println("\n👴 Étudiants âgés de plus de 20 ans :");
            olderStudents.forEach(s ->
                    System.out.println(s.getName() + " (" + s.getAge() + " ans)")
            );

            // Étudiants nom = "Rayan" et âge >= 18
            List<Student> filteredStudents = studentDAO.findByDynamicCriteria("Rayan", 18);
            System.out.println("\n🎯 Étudiants nommés 'Rayan' avec au moins 18 ans :");
            filteredStudents.forEach(s ->
                    System.out.println(s.getName() + " (" + s.getAge() + " ans)")
            );

            // Étudiants uniquement avec âge >= 18 (nom = null)
            List<Student> onlyByAge = studentDAO.findByDynamicCriteria(null, 18);
            System.out.println("\n📌 Étudiants avec au moins 18 ans (sans filtrer par nom) :");
            onlyByAge.forEach(s ->
                    System.out.println(s.getName() + " (" + s.getAge() + " ans)")
            );

            // Étudiants uniquement avec nom = "Fahd" (âge = null)
            List<Student> onlyByName = studentDAO.findByDynamicCriteria("Fahd", null);
            System.out.println("\n📌 Étudiants avec le nom 'Fahd' (sans filtrer par âge) :");
            onlyByName.forEach(s ->
                    System.out.println(s.getName() + " (" + s.getAge() + " ans)")
            );

            // Tous les étudiants (aucun critère)
            List<Student> allStudentsDynamic = studentDAO.findByDynamicCriteria(null, null);
            System.out.println("\n Tous les étudiants (aucun critère appliqué) :");
            allStudentsDynamic.forEach(s ->
                    System.out.println(s.getName() + " (" + s.getAge() + " ans)")
            );

            // =========================
            // Test Criteria API : étudiants + cours
            // =========================

            List<Student> studentsCriteria = studentDAO.findAllWithCoursesCriteria();
            System.out.println("\n Liste des étudiants (Criteria API)");
            for (Student s : studentsCriteria) {
                String profileInfo = (s.getProfile() != null)
                        ? s.getProfile().getAddress() + " | " + s.getProfile().getPhone()
                        : "aucun profil";
                String courses = s.getCourses().stream()
                        .map(Course::getTitle)
                        .collect(Collectors.joining(", "));
                System.out.println(s.getName() + " (Profil: " + profileInfo + ") suit : " +
                        (courses.isEmpty() ? "aucun cours" : courses));
            }

            // =========================
            // Test Criteria API : étudiants + profil + cours
            // =========================

            List<Student> studentsProfileCourses = studentDAO.findAllWithProfileAndCoursesCriteria();
            System.out.println("\n Liste des étudiants (Criteria API : profil + cours) :");
            for (Student s : studentsProfileCourses) {
                String profileInfo = (s.getProfile() != null)
                        ? s.getProfile().getAddress() + " | " + s.getProfile().getPhone()
                        : "aucun profil";
                String courses = s.getCourses().stream()
                        .map(Course::getTitle)
                        .collect(Collectors.joining(", "));
                System.out.println(s.getName() + " (Profil: " + profileInfo + ") suit : " +
                        (courses.isEmpty() ? "aucun cours" : courses));
            }

            // =========================
            // Test Criteria API dynamique : âge + cours
            // =========================

            List<Student> studentsFiltered = studentDAO.findByAgeAndCourseTitle(20, "Physique");
            System.out.println("\n Etudiants de 20 ans ou + qui suivent 'Physique' :");
            studentsFiltered.forEach(s ->
                            System.out.println("- " + s.getName() + " (" + s.getAge() + " ans)")
                    );

            System.out.println(" === Méthode filtre (Student) === ");

            // Chercher les étudiants qui s'appellent "Rayan"
            List<Student> result1 = studentDAO.findByFilters("Rayan", null, null);
            System.out.println("\n🎯 Etudiants qui s'appellent 'Rayan' :");
            result1.forEach(s -> System.out.println("- " + s.getName() + " (" + s.getAge() + " ans)"));

            // Chercher les étudiants de plus de 20,ans
            List<Student> result2 = studentDAO.findByFilters(null, 20, null);
            System.out.println("\n🎯 Etudiants de plus de 20 ans :");
            result2.forEach(s -> System.out.println("- " + s.getName() + " (" + s.getAge() + " ans)"));

            // Chercher les étudiants qui suivent "Physique"
            List<Student> result3 = studentDAO.findByFilters(null, null, "Physique");
            System.out.println("\n🎯 Etudiants qui suivent 'Physique' :");
            result3.forEach(s -> System.out.println("- " + s.getName() + " (" + s.getAge() + " ans)"));

            // Chercher les étudiants appelés "Rayan", de plus de 18 ans, qui suivent "Math"
            List<Student> result4 = studentDAO.findByFilters("Rayan", 18, "Maths");
            System.out.println("\n🎯 Etudiants appelés 'Rayan', de plus de 18 ans, qui suivent 'Maths' :");
            result4.forEach(s -> System.out.println("- " + s.getName() + " (" + s.getAge() + " ans)"));

            // =========================
            // Tests Criteria API Teacher
            // =========================

            System.out.println("\n==== Test Criteria API (Teacher) ====");

            // Rechercher prof dont le nom contient "Smith"
            List<Teacher> teachersByName = teacherDAO.findByFilters("Smith", null);
            System.out.println("\n Professeurs avec 'Smith' dans le nom :");
            teachersByName.forEach(t ->
                    System.out.println("- " + t.getName())
            );

            // Rechercher prof qui enseignent "Physique"
            List<Teacher> teachersByCourse = teacherDAO.findByFilters(null, "Physique");
            System.out.println("\n Professeurs qui enseignent 'Physique' :");
            teachersByCourse.forEach(t ->
                            System.out.println("- " + t.getName() + " enseigne : " +
                                    t.getCourses().stream()
                                            .map(Course::getTitle)
                                            .collect(Collectors.joining(", ")))
                    );

            // Rechercher professeurs appelés "Mr. Smith" ET qui enseignent "Maths"
            List<Teacher> teachersByNameAndCourse = teacherDAO.findByFilters("Mr. Smith", "Maths");
            System.out.println("\n👨‍🏫 Professeurs appelés 'Mr. Smith' qui enseignent 'Maths' :");
            teachersByNameAndCourse.forEach(t ->
                            System.out.println("- " + t.getName() + " enseigne : " +
                                    t.getCourses().stream()
                                            .map(Course::getTitle)
                                            .collect(Collectors.joining(", ")))
                    );

            // =========================
            // Test Pagination + Tri
            // =========================

            System.out.println("\n===== Pagination & Tri =====");

            List<Student> p1 = studentDAO.findPaginated(1, 2, "age", true);
            System.out.println("Page 1 (tri par âge croissant) :");
            p1.forEach(s -> System.out.println("- " + s.getName() + " (" + s.getAge() + " ans)"));

            List<Student> p2 = studentDAO.findPaginated(2, 2, "age", true);
            System.out.println("📄 Page 2 (tri par âge croissant) :");
            p2.forEach(s -> System.out.println("- " + s.getName() + " (" + s.getAge() + " ans)"));

            List<Student> pageDesc = studentDAO.findPaginated(1, 3, "name", false);
            System.out.println("📄 Page 1 (tri par nom décroissant) :");
            pageDesc.forEach(s -> System.out.println("- " + s.getName() + " (" + s.getAge() + " ans)"));

            // =========================
            // Test Group By
            // =========================

            System.out.println("\n===== Nombre d'étudiants par cours =====");

            List<Object[]> stats = studentDAO.countStudentsByCourse();
            for (Object[] row : stats) {
                String courseTitle = (String) row[0];
                Long studentCount = (Long) row[1];
                System.out.println("- " + courseTitle + " : " + studentCount + " étudiant(s)");
            }

            // =========================
            // Test Group By + HAVING (Criteria API)
            // =========================
            System.out.println("\n===== Cours avec au moins 2 étudiants (Criteria API + HAVING) =====");

            List<Object[]> statsHaving = studentDAO.countStudentsByCourseHaving(2);
            for (Object[] row : statsHaving) {
                String courseTitle = (String) row[0];
                Long studentCount = (Long) row[1];
                System.out.println("- " + courseTitle + " : " + studentCount + " étudiant(s)");
            }

            // =========================
            // Test Criteria API : étudiants > âge moyen
            // =========================
            List<Student> olderThanAvg = studentDAO.findOlderThanAverage();
            System.out.println("\n Étudiants plus âgés que la moyenne :");
            olderThanAvg.forEach(s ->
                    System.out.println("- " + s.getName() + " (" + s.getAge() + " ans)")
            );

            // =========================
            // Test Criteria API : pagination + tri
            // =========================

            System.out.println("\n===== Pagination des étudiants (page 1, taille 2) =====");
            List<Student> page1 = studentDAO.findAllPaginated(1, 2);
            page1.forEach(s -> System.out.println("- " + s.getName()));

            System.out.println("\n===== Pagination des étudiants (page 2, taille 2) =====");
            List<Student> page2 = studentDAO.findAllPaginated(2, 2);
            page2.forEach(s -> System.out.println("- " + s.getName()));

            // =========================
            // Test Criteria API : pagination + tri étudiants + cours
            // =========================

            System.out.println("\n===== Pagination avec profils + cours (page 1, taille 2) =====");
            List<Student> pag1 = studentDAO.findAllWithProfileAndCoursesPaginated(1, 2);
            for (Student s : pag1) {
                String profileInfo = (s.getProfile() != null)
                        ? s.getProfile().getAddress() + " | " + s.getProfile().getPhone()
                        : "aucun profil";
                String courses = s.getCourses().stream()
                        .map(Course::getTitle)
                        .collect(Collectors.joining(", "));
                System.out.println(s.getName() + " (Profil: " + profileInfo + ") suit : " +
                        (courses.isEmpty() ? "aucun cours" : courses));
            }

            System.out.println("\n===== Pagination avec profils + cours (page 2, taille 2) =====");
            List<Student> pag2 = studentDAO.findAllWithProfileAndCoursesPaginated(2, 2);
            pag2.forEach(s -> {
                String courses = s.getCourses().stream()
                        .map(Course::getTitle)
                        .collect(Collectors.joining(", "));
                System.out.println(s.getName() + " suit : " + (courses.isEmpty() ? "aucun cours" : courses));
            });

            // =========================
            // Test Criteria API : sous-requête IN, NOT EXISTS, corrélée
            // =========================

            System.out.println("\n=== Sous-requête avec IN ===");
            List<Student> taughtBySmith = studentDAO.findStudentsByTeacherName("Mr. Smith");
            taughtBySmith.forEach(s -> System.out.println("- " + s.getName()));

            System.out.println("\n=== Sous-requête avec NOT EXISTS ===");
            List<Teacher> noCourses = teacherDAO.findTeachersWithoutCourses();
            noCourses.forEach(t -> System.out.println("- " + t.getName()));

            System.out.println("\n=== Sous-requête corrélée ===");
            List<Student> crowdedCourses = studentDAO.findStudentsInCrowdedCourses(2);
            crowdedCourses.forEach(s -> System.out.println("- " + s.getName()));

            // =========================
            // Test des Named Queries
            // =========================

            System.out.println("\n=== Tests des Named Queries ===");

            // Étudiants avec le nom "Rayan"
            List<Student> studentsNamedRayan = studentDAO.findByName("Rayan");
            studentsNamedRayan.forEach(s ->
                    System.out.println("👤 Étudiant trouvé : " + s.getName() + ", âge " + s.getAge())
            );

            // Étudiants plus vieux que 20 ans
            List<Student> olderStudent = studentDAO.findOlderThan(20);
            olderStudent.forEach(s ->
                    System.out.println("👴 Étudiant plus âgé que 20 ans : " + s.getName() + " (" + s.getAge() + " ans)")
            );

            // Cours avec le nombre d’étudiants
            List<CourseWithCountDTO> coursesWithCounts = courseDAO.findAllWithStudentCount();
            coursesWithCounts.forEach(c ->
                    System.out.println("📘 " + c.getTitle() + " → " + c.getStudentCount() + " étudiants")
            );

            //
            System.out.println("=== Cours avec au moins 2 étudiants ===");
            List<CourseWithCountDTO> coursesMin2 = courseDAO.findCoursesWithMinStudents(2);
            coursesMin2.forEach(System.out::println);

            System.out.println("=== Cours avec au moins 3 étudiants ===");
            List<CourseWithCountDTO> coursesMin3 = courseDAO.findCoursesWithMinStudents(3);
            coursesMin3.forEach(System.out::println);


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