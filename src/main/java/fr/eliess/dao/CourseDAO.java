package fr.eliess.dao;

import fr.eliess.dto.CourseWithCountDTO;
import fr.eliess.model.Course;
import jakarta.persistence.EntityManager;

import java.util.List;

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

    // === 🔹 Named Native Query ===
    /** Liste des cours avec nombre d’étudiants */
    public List<CourseWithCountDTO> findAllWithStudentCount() {
        return em.createNamedQuery("Course.findAllWithStudentCount", CourseWithCountDTO.class)
                .getResultList();
    }

    // méthode qui exploite la nouvelle requête native
    @SuppressWarnings("unchecked")
    public List<CourseWithCountDTO> findCoursesWithMinStudents(long minCount) {
        return em.createNamedQuery("Course.findCoursesWithMinStudents") // pas de resultClass ici
                .setParameter("minCount", minCount)
                .getResultList();
    }

}
