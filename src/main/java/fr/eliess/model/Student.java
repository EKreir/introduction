package fr.eliess.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"courses", "profile"})
@NamedQueries({
        @NamedQuery(
                name = "Student.findByName",
                query = "SELECT s FROM Student s WHERE s.name = :name"
        ),
        @NamedQuery(
                name = "Student.findOlderThan",
                query = "SELECT s FROM Student s WHERE s.age > :age"
        )
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int age;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY) // <- cascade activé
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();

    /*
    Pour les relations ManyToMany.
    Crée une table de jointure entre Student et Course.
    name = "student_course" → nom de la table de jointure.
    joinColumns = @JoinColumn(name = "student_id") → colonne qui pointe vers Student.
    inverseJoinColumns = @JoinColumn(name = "course_id") → colonne qui pointe vers Course.
    */

    // initialise la collection pour éviter les NullPointerException

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudentProfile profile;

    @Embedded
    private Address address;

    @Version
    private int version;

    @ElementCollection
    @CollectionTable(name = "student_phone",
            joinColumns = @JoinColumn(name = "student_id")
    )
    @Column(name = "phone_number")
    private Set<String> phones = new HashSet<>();

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void addCourse(Course course) {
        if (courses.add(course)) {
            course.getStudents().add(this);
        }
    }

    @PrePersist
    public void beforeInsert() {
        System.out.println("Insertion de l’étudiant : " + name);
    }

    @PreUpdate
    public void beforeUpdate() {
        System.out.println("Mise à jour de l’étudiant : " + name);
    }

    /*

    On a ajouté deux Named Queries :

    Student.findByName →-> retourne les étudiants avec un nom donné.

    Student.findOlderThan -> retourne ceux plus vieux qu’un âge donné.

    ========================================================================

    Explication :

    @NamedQueries : regroupe plusieurs @NamedQuery pour une entité.

    Chaque @NamedQuery a :
    name -> identifiant unique que tu utilises dans le DAO pour exécuter la requête.
    query -> la requête JPQL "précompilée" qui sera exécutée par Hibernate.

    Exemple concret :

    Student.findByName
    Retourne tous les étudiants dont name = :name.

    Dans le DAO :

    em.createNamedQuery("Student.findByName", Student.class)
      .setParameter("name", "Rayan")
      .getResultList();

    Student.findOlderThan

    Retourne tous les étudiants dont age > :age.
    Paramètre :age dynamique.

    Avantage :
    les requêtes sont centralisées dans l’entité et réutilisables partout dans l’application.

    ======================================================================

    Explications imports :

    jakarta.persistence.ElementCollection ->
    indique que ce n’est pas une entité mais une collection d’éléments simples.

    CollectionTable -> nom de la table qui stockera les valeurs.

    JoinColumn -> colonne qui fait le lien avec Student.

    Column -> nom de la colonne dans la table collection.

    */

}
