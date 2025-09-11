package fr.eliess.dao;

import fr.eliess.model.Course;
import fr.eliess.model.Teacher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

public class TeacherDAO extends GenericDAO<Teacher> {

    public TeacherDAO(EntityManager em) {
        super(em, Teacher.class);
    }

    // ===============================
    // Requêtes avec JOIN FETCH
    // ===============================

    public Teacher findWithCourses(Long id) {
        return em.createQuery(
                        "SELECT t FROM Teacher t LEFT JOIN FETCH t.courses WHERE t.id = :id",
                        Teacher.class
                ).setParameter("id", id)
                .getSingleResult();
    }

    // Méthode spécifique pour récupérer un prof avec ses cours et leurs étudiants
    public Teacher findWithCoursesAndStudents(Long id) {
        return em.createQuery(
                "SELECT DISTINCT t FROM Teacher t " +
                "LEFT JOIN FETCH t.courses c " +
                "LEFT JOIN FETCH c.students " +
                "WHERE t.id = :id",
                Teacher.class
        ).setParameter("id", id)
         .getSingleResult();
    }

    // ===============================
    // Requêtes dynamiques (Criteria API)
    // ===============================

    /**
     * Recherche dynamique de professeurs par filtres.
     *
     * @param name          Nom partiel ou complet du prof (nullable)
     * @param courseTitle   Nom d'un cours enseigné (nullable)
     * @return Liste des enseignants correspondant aux filtres
     */
    public List<Teacher> findByFilters(String name, String courseTitle) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Teacher> cq = cb.createQuery(Teacher.class);
        Root<Teacher> teacher = cq.from(Teacher.class);

        teacher.fetch("courses", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        // Filtre sur le nom du prof
        if (name != null && !name.isBlank()) {
            predicates.add(cb.like(cb.lower(teacher.get("name")), "%" + name.toLowerCase() + "%"));
        }

        // Filtre sur le cours enseigné
        if (courseTitle != null && !courseTitle.isBlank()) {
            Join<Teacher, Course> course = teacher.join("courses", JoinType.INNER);
            predicates.add(cb.equal(course.get("title"), courseTitle));
        }

        cq.select(teacher).distinct(true).where(predicates.toArray(new Predicate[0]));

        return em.createQuery(cq).getResultList();
    }

    // Sous-requête avec EXISTS
    public List<Teacher> findTeachersWithoutCourses() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Teacher> cq = cb.createQuery(Teacher.class);
        Root<Teacher> teacher = cq.from(Teacher.class);

        // Sous-requête : existe-t-il un cours lié à ce prof ?
        Subquery<Course> subquery = cq.subquery(Course.class);
        Root<Course> course = subquery.from(Course.class);
        subquery.select(course)
                .where(cb.equal(course.get("teacher"), teacher));

        // Condition : NOT EXISTS
        cq.select(teacher).where(cb.not(cb.exists(subquery)));

        return em.createQuery(cq).getResultList();
    }

    /*

    Ici :

    Pas de CRUD (hérités de GenericDAO).
    On garde les méthodes spécifiques avec JOIN FETCH.
    On ajoute une méthode findByFilters en Criteria API
    (recherche par nom et/ou par cours enseigné).

    */

}
