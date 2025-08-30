package fr.eliess.dao;

import fr.eliess.model.Teacher;
import jakarta.persistence.EntityManager;

public class TeacherDAO extends GenericDAO<Teacher> {

    public TeacherDAO(EntityManager em) {
        super(em, Teacher.class);
    }

    // Méthode spécifique pour récupérer un prof avec ses cours
    public Teacher findWithCourses(Long id) {

        return em.createQuery(
                "SELECT t FROM Teacher t LEFT JOIN FETCH t.courses WHERE t.id = :id",
                Teacher.class
        ).setParameter("id", id)
                .getSingleResult();

    }

}
