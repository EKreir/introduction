package fr.eliess.dao;

import fr.eliess.model.Course;
import jakarta.persistence.EntityManager;

public class CourseDAO extends GenericDAO<Course> {

    public CourseDAO(EntityManager em) {
        super(em, Course.class);
    }

}
