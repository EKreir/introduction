package fr.eliess.model;

import fr.eliess.dto.CourseWithCountDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"students", "teacher"})
/* génère automatiquement une méthode toString() pour la classe
exclude = "students" : n'inclut pas les champs students dans le toString()
pour éviter les boucles infinies dans les relations bidirectionnelles (Student <--> Course)
*/
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "Course.findAllWithStudentCount",
                query = "SELECT c.id, c.title, COUNT(sc.student_id) AS student_count " +
                        "FROM Course c " +
                        "LEFT JOIN student_course sc ON c.id = sc.course_id " +
                        "GROUP BY c.id, c.title",
                resultSetMapping = "CourseWithCountMapping"
        ),
        @NamedNativeQuery(
                name = "Course.findCoursesWithMinStudents",
                query = "SELECT c.id, c.title, COUNT(sc.student_id) AS student_count " + // 👈 alias cohérent
                        "FROM Course c " +
                        "LEFT JOIN student_course sc ON c.id = sc.course_id " +
                        "GROUP BY c.id, c.title " +
                        "HAVING COUNT(sc.student_id) >= :minCount",
                resultSetMapping = "CourseWithCountMapping" // 👈 réutilisation du même mapping
        )
})
@SqlResultSetMapping(
        name = "CourseWithCountMapping",
        classes = @ConstructorResult(
                targetClass = CourseWithCountDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "title", type = String.class),
                        @ColumnResult(name = "student_count", type = Long.class) // 👈 alias uniforme
                }
        )
)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY) // indique que l'autre côté de la relation possède la vraie clé étrangère
    private Set<Student> students = new HashSet<>();
    // Ici, Student a courses et Course a students. Le champ students dans Course est le propriétaire de la relation.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id") // foreign key DB
    private Teacher teacher;

    public Course(String title) {
        this.title = title;
    }


    public void addStudent(Student student) {
        if (students.add(student)) {
            student.getCourses().add(this);
        }
    }

    /*

    Ici on utilise :

    @NamedNativeQuery pour définir la requête SQL native.

    @SqlResultSetMapping pour mapper le résultat dans un DTO.

    =====================================================================

    Explication :

    @NamedNativeQuery :

    Permet de définir une requête SQL native, pas du JPQL.

    Ici, on veut récupérer chaque cours + nombre d’étudiants (COUNT + GROUP BY).

    name -> identifiant unique.
    query -> la requête SQL exacte.

    resultSetMapping -> indique comment convertir
    le résultat SQL en objet Java.

    @SqlResultSetMapping :

    Définit la correspondance entre le résultat SQL et un DTO Java.

    @ConstructorResult -> crée directement un objet
    CourseWithCountDTO à partir des colonnes SQL.

    columns -> chaque colonne SQL
    correspond à un type et à un argument du constructeur du DTO.

    Exemple concret :

    List<CourseWithCountDTO> courses = em.createNamedQuery("Course.findAllWithStudentCount", CourseWithCountDTO.class)
                                        .getResultList();

    Hibernate va exécuter le SQL :

    SELECT c.id, c.title, COUNT(sc.student_id) AS student_count
    FROM course c
    LEFT JOIN student_course sc ON c.id = sc.course_id
    GROUP BY c.id, c.title;

    Avantages de ces nouvelles implémentations

    Centralisation et réutilisabilité :

    Plus besoin de réécrire la même requête dans tous tes DAO ou services.

    Sécurité :

    Les paramètres dynamiques (:name, :age)
    réduisent le risque d’injection SQL.

    Lisibilité :

    Les requêtes sont déclarées directement dans l’entité,
    on sait exactement ce que fait l’entité côté DB.

    Performance :

    Hibernate prépare les NamedQuery dès le démarrage,
    ce qui peut améliorer les temps d’exécution.

    Interopérabilité avec DTO :

    Pour des rapports ou calculs (COUNT, GROUP BY),
    on peut directement utiliser des DTO sans surcharger les entités.

    */

}
