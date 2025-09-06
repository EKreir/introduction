package fr.eliess.dao;

import fr.eliess.model.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

public class StudentDAO extends GenericDAO<Student> {

    /*

    Ici on spécialise le DAO générique pour l’entité Student.
    GenericDAO<T> est une classe abstraite (générique), où T représente une entité JPA
    (par ex. Student, Course, Teacher, etc.).
    Quand on écrit StudentDAO extends GenericDAO<Student>, on fixe T = Student.
    Ça veut dire :
    Toutes les méthodes héritées de GenericDAO (create, read, update, delete) manipulent des Student.
    On n’a pas besoin de réécrire les méthodes.
    Derrière : c’est du polymorphisme générique -> une seule classe GenericDAO suffit pour toutes tes entités.
    On n’aura jamais à copier-coller du code pour CourseDAO, TeacherDAO, etc.

    */

    public  StudentDAO(EntityManager em) {
        super(em, Student.class);
    }

    // Recherche tous les étudiants par nom exact
    public List<Student> findByName(String name) {
        return em.createQuery(
                        "SELECT s FROM Student s WHERE s.name = :name", Student.class)
                .setParameter("name", name)
                .getResultList();
    }

    // Recherche les étudiants qui suivent un cours donné
    public List<Student> findByCourseTitle(String courseTitle) {
        return em.createQuery(
                        "SELECT DISTINCT s FROM Student s " +
                                "JOIN s.courses c " +
                                "WHERE c.title = :title", Student.class)
                .setParameter("title", courseTitle)
                .getResultList();
    }

    // Récupère tous les étudiants avec pagination
    public List<Student> findAllPaged(int page, int pageSize) {
        return em.createQuery("SELECT s FROM Student s ORDER BY s.name", Student.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    // Récupère tous les étudiants avec leurs cours (évite le problème N+1)
    public List<Student> findAllWithCourses() {
        return em.createQuery(
                "SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses", Student.class
        ).getResultList();
    }

    // Etudiant + profil
    public Student findWithProfile(Long id) {
        return em.createQuery(
                        "SELECT s FROM Student s LEFT JOIN FETCH s.profile WHERE s.id = :id",
                        Student.class
                ).setParameter("id", id)
                .getSingleResult();
    }

    // Etudiant + cours
    public Student findWithCourses(Long id) {
        return em.createQuery(
                        "SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.id = ;id",
                        Student.class
                ).setParameter("id", id)
                .getSingleResult();
    }

    // Etudiant + profil + cours
    public Student findWithProfileAndCourses(Long id) {
        return em.createQuery(
                        "SELECT DISTINCT s FROM Student s " +
                                "LEFT JOIN FETCH s.profile " +
                                "LEFT JOIN FETCH s.courses " +
                                "WHERE s.id = :id",
                        Student.class
                ).setParameter("id", id)
                .getSingleResult();
    }

    // Recherche avec Criteria API : âge supérieur à une valeur
    public List<Student> findByAgeGreaterThan(int age) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        Root<Student> root = cq.from(Student.class);

        cq.select(root).where(cb.greaterThan(root.get("age"), age));

        return em.createQuery(cq).getResultList();
    }

    // Recherche dynamique avec Criteria API
    public List<Student> findByDynamicCriteria(String name, Integer minAge) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        Root<Student> root = cq.from(Student.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            predicates.add(cb.equal(root.get("name"), name));
        }

        if (minAge != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));
        }

        cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.asc(root.get("name")));

        return em.createQuery(cq).getResultList();
    }

    /*

     Explications simples

    findByName :
    -> JPQL : SELECT s FROM Student s WHERE s.name = :name
    Student est l’entité (pas la table SQL).
    s est un alias.
    :name est un paramètre qu’on lie avec .setParameter.

    findByCourseTitle :
    -> Jointure avec JOIN s.courses c.
    Ça traverse la relation définie dans ton entité (@ManyToMany).
    DISTINCT évite les doublons si un étudiant suit plusieurs cours.

    findAllPaged :
    -> Tri + pagination.
    setFirstResult() = où commencer (offset).
    setMaxResults() = combien de lignes.
    Exemple : page 2, taille 5 → saute les 5 premiers ((2-1)*5=5), prend les 5 suivants.

    countStudents :
    -> Compte total des entités.
    Utile pour afficher page X/Y.

    méthodes pour étudiants, profils et cours :

    Pourquoi ?
    Parce que findByName, findByCourseTitle, findAllPaged -> vont ramener uniquement les données de l’étudiant
     pas ses relations (puisqu’elles sont LAZY).

    Donc si on veut tout afficher directement sans LazyInitializationException -> on doit passer par ces méthodes.

    ==========================================================================

    Implémentation de Criteria API (proposé par jakarta EE / Hibernate)


    Méthode findByAgeGreaterThan(int age) :

        public List<Student> findByAgeGreaterThan(int age) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

    On demande à l’EntityManager son CriteriaBuilder,
    qui est l’outil pour construire une requête SQL/JPQL en Java.

        CriteriaQuery<Student> cq = cb.createQuery(Student.class);

    On crée une requête typed (CriteriaQuery<Student>) :
    ça veut dire que le résultat attendu sera une liste de Student.

        Root<Student> root = cq.from(Student.class);

    Le root représente la table/entité Student dans la requête
    (équivalent de FROM student s en SQL/JPQL).
    En gros, root = alias de Student.

        cq.select(root).where(cb.greaterThan(root.get("age"), age));

    On construit la requête :
    select(root) → on veut récupérer les étudiants (Student).
    where(cb.greaterThan(...)) → condition : on garde uniquement ceux dont age > :age.

        return em.createQuery(cq).getResultList();

    On exécute la requête et on retourne la liste des résultats.

    Traduction SQL généré par Hibernate :

        SELECT * FROM students s WHERE s.age > :age

    Méthode findByDynamicCriteria(String name, Integer minAge) :

    Cette méthode montre comment ajouter dynamiquement des filtres (par nom, par âge…).

        public List<Student> findByDynamicCriteria(String name, Integer minAge) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        Root<Student> root = cq.from(Student.class);

    Comme avant : on prépare le CriteriaBuilder, la requête et le root.

        List<Predicate> predicates = new ArrayList<>();

    On crée une liste de conditions (prédicats).
    Pourquoi ?
    Parce que certaines conditions ne seront ajoutées que si les paramètres ne sont pas nuls.

        if (name != null && !name.isEmpty()) {
        predicates.add(cb.equal(root.get("name"), name));
    }

    Si name est fourni → on ajoute un filtre WHERE s.name = :name.

        if (minAge != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));
    }

    Si minAge est fourni → on ajoute un filtre WHERE s.age >= :minAge.

        cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));

    On applique toutes les conditions ensemble (AND).
    Si aucun paramètre n’est fourni → pas de filtre → tous les étudiants sont retournés.

        cq.orderBy(cb.asc(root.get("name")));

    On trie les résultats par ordre alphabétique (ORDER BY name ASC).

        return em.createQuery(cq).getResultList();
}

    On exécute et retourne les résultats.

    Exemple SQL généré si name="Rayan" et minAge=18 :

        SELECT * FROM student s
        WHERE s.name = 'Rayan' AND s.age >= 18
        ORDER BY s.name ASC;



    */

}
