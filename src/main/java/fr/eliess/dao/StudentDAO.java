package fr.eliess.dao;

import fr.eliess.model.Student;
import jakarta.persistence.EntityManager;

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

}
