package fr.eliess.dao;

import fr.eliess.model.Student;
import jakarta.persistence.EntityManager;

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

    public StudentDAO(EntityManager em) {
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

    */

}
