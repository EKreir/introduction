package fr.eliess.dao;

import jakarta.persistence.EntityManager;

public abstract class GenericDAO<T> {

    protected EntityManager em;
    private Class<T> entityClass;

    public GenericDAO(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    public void create(T entity) {

        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();

    }

    public T read(Long id) {

        return em.find(entityClass, id);

    }

    public void update(T entity) {

        em.getTransaction().begin();
        em.merge(entity);
        em.getTransaction().commit();

    }

    public void delete(T entity) {

        em.getTransaction().begin();
        em.remove(em.contains(entity) ? entity : em.merge(entity));
        em.getTransaction().commit();

    }

    /*

     C’est une transaction JPA.

    em.getTransaction().begin() -> démarre une transaction.
    em.persist(entity) -> demande à Hibernate/JPA d’insérer l’entité en base
    (ça ne part pas directement en SQL, c’est stocké dans un "contexte persistant").
    em.getTransaction().commit() -> valide la transaction ->
    Hibernate/JPA génère la requête SQL INSERT INTO ... et l’envoie à la base.

    Sans begin() + commit(), JPA ne sait pas quand envoyer les requêtes.

    Hibernate accumule les changements (persist, update, delete) dans une queue interne ->
    tout est envoyé quand on fait commit().


    em.find(Student.class, 1L) -> Hibernate fait un SELECT * FROM student WHERE id=1.
    entityClass est passé au constructeur de GenericDAO -> ça permet à GenericDAO de fonctionner
    avec n’importe quelle entité.
    Ici, dans StudentDAO, entityClass vaut Student.class.

    Hibernate regarde d’abord dans son cache de 1er niveau (mémoire du EntityManager).
    Si l’entité demandée n’y est pas, Hibernate envoie un SELECT à la base.


    em.remove(entity) -> demande à JPA de supprimer l’objet de la base.
    MAIS -> JPA ne peut supprimer que des objets qu’il gère déjà (= attachés au EntityManager).
    em.contains(entity) -> teste si l’objet est géré.
    Si oui -> on peut le supprimer directement.
    Si non -> on fait em.merge(entity) pour rattacher l’objet au contexte -> ensuite on peut le supprimer.
    commit() envoie le vrai SQL -> DELETE FROM ....

    Exemple si on a un Student récupéré dans un autre EntityManager
    (ou créé à la main avec juste son id) -> il est détaché. Hibernate ne sait pas quoi en faire ->
    il faut le rattacher avec merge.

    */

}
