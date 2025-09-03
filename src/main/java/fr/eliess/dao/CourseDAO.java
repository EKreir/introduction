package fr.eliess.dao;

import fr.eliess.model.Course;
import jakarta.persistence.EntityManager;

public class CourseDAO extends GenericDAO<Course> {

    public CourseDAO(EntityManager em) {
        super(em, Course.class);
    }

    public Course findWithStudents(Long id) {
        return em.createQuery(
                "SELECT c FROM Course c LEFT JOIN FETCH c.students WHERE c.id = :id",
                Course.class
        ).setParameter("id", id)
         .getSingleResult();
    }

}
