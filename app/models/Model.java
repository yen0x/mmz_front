package models;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;

import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.ejb.HibernateEntityManager;
import org.springframework.util.CollectionUtils;

import play.db.jpa.JPA;

@MappedSuperclass
public class Model {

  public void save() {
    JPA.em().persist(this);
  }

  public void update() {
    JPA.em().merge(this);
  }

  public void delete() {
    JPA.em().remove(this);
  }

  public static javax.persistence.Query createNamedQuery(String name) {
    return safeGetEntityManager().createNamedQuery(name);
  }

  public static <M> javax.persistence.TypedQuery<M> createNamedQuery(String name, Class<M> type) {
    return safeGetEntityManager().createNamedQuery(name, type);
  }

  public static javax.persistence.Query createSqlUpdate(String query) {
    return safeGetEntityManager().createNativeQuery(query);
  }

  public static javax.persistence.Query createQuery(String query) {
    return safeGetEntityManager().createQuery(query);
  }

  public static <M> javax.persistence.TypedQuery<M> createQuery(String query, Class<M> modelType) {
    return safeGetEntityManager().createQuery(query, modelType);
  }

  public static javax.persistence.Query createUpdateQuery(String query) {
    return createQuery(query);
  }

  public static <M> void deleteAll(List<M> objects) {
    if(CollectionUtils.isEmpty(objects)) return;
    for (M model : objects) {
      safeGetEntityManager().remove(model);
    }
  }

  public static int deleteAll(Class<?> entityType) {
    String query = String.format("delete %s bean", entityType.getSimpleName());
    return safeGetEntityManager().createQuery(query).executeUpdate();
  }

  // should only be used in static methods
  private static EntityManager safeGetEntityManager() {
    try {
      return JPA.em();
    } catch (Exception ex) {
      return JPA.em("default");
    }
  }

  public static class Finder<ID, T> {
    private final Class<T> type;

    public Finder(Class<ID> idType, Class<T> type) {
      this.type = type;
    }

    public List<T> all() {
      return query().findList();
    }

    public T first() {
      return query().setMaxRows(1).findUnique();
    }

    public List<T> findList() {
      return all();
    }

    public long findRowCount() {
      return query().findRowCount();
    }

    public Query<T> where() {
      return query();
    }

    public T byId(ID id) {
      return query().byId(id);
    }

    public Query<T> query() {
      Criteria criteria = session().createCriteria(this.type);
      return new Query<T>(this.type, criteria);
    }

    private Session session() {
      HibernateEntityManager hem = JPA.em().unwrap(HibernateEntityManager.class);
      return hem.getSession();
    }
  }

  public static class Query<T> {

    private final Class<T> type;
    private final Criteria criteria;

    public Query(Class<T> type, Criteria criteria) {
      this.type = type;
      this.criteria = criteria;
    }

    @SuppressWarnings("unchecked")
    public T byId(Object id) {
      return (T)this.criteria.add(Restrictions.idEq(id)).uniqueResult();
    }

    public Query<T> eq(String field, Object value) {
      Criteria eq = this.criteria.add(Restrictions.eq(field, value));
      return new Query<T>(this.type, eq);
    }

    public Query<T> ieq(String field, String value) {
      Criteria ieq = this.criteria.add(Restrictions.eq(field, value).ignoreCase());
      return new Query<T>(this.type, ieq);
    }

    public Query<T> ne(String field, Object value) {
      Criteria ne = this.criteria.add(Restrictions.ne(field, value));
      return new Query<T>(this.type, ne);
    }

    public Query<T> ilike(String field, String value) {
      Criteria ilike = this.criteria.add(Restrictions.ilike(field, value));
      return new Query<T>(this.type, ilike);
    }

    public Query<T> gt(String field, Object value) {
      Criteria gt = this.criteria.add(Restrictions.gt(field, value));
      return new Query<T>(this.type, gt);
    }

    public Query<T> le(String field, Object value) {
      Criteria le = this.criteria.add(Restrictions.le(field, value));
      return new Query<T>(this.type, le);
    }

    public Query<T> lt(String field, Object value) {
      Criteria lt = this.criteria.add(Restrictions.lt(field, value));
      return new Query<T>(this.type, lt);
    }

    public Query<T> or(Criterion first, Criterion second) {
      Criteria or = this.criteria.add(Restrictions.or(first, second));
      return new Query<T>(this.type, or);
    }

    public Query<T> and(Criterion first, Criterion second) {
      Criteria and = this.criteria.add(first).add(second);
      return new Query<T>(this.type, and);
    }

    @SuppressWarnings("rawtypes")
    public Query<T> in(String field, List values) {
      if(values.isEmpty()) {
        Criteria negate = this.criteria.add(Restrictions.sqlRestriction("1=0"));
        return new Query<T>(this.type, negate);
      }
      Criteria in = this.criteria.add(Restrictions.in(field, values));
      return new Query<T>(this.type, in);
    }

    public Query<T> fetch(String associated) {
      Criteria join = this.criteria.setFetchMode(associated, FetchMode.JOIN);
      return new Query<T>(this.type, join);
    }

    @SuppressWarnings("unchecked")
    public List<T> findList() {
      return this.criteria.list();
    }

    @SuppressWarnings("unchecked")
    public T findUnique() {
      return (T) this.criteria.uniqueResult();
    }

    public Long findRowCount() {
      return (Long) this.criteria.setProjection(Projections.count("id")).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public <ID> List<ID> findIds() {
      return this.criteria.setProjection(Projections.id()).list();
    }

    public PagedQuery<T> findPagingList(int size) {
      return new PagedQuery<T>(type, criteria, size);
    }

    public Query<T> orderByAsc(String field) {
      Criteria ordered = this.criteria.addOrder(Order.asc(field));
      return new Query<T>(this.type, ordered);
    }

    public Query<T> orderByDesc(String field) {
      Criteria ordered = this.criteria.addOrder(Order.desc(field));
      return new Query<T>(this.type, ordered);
    }

    public Query<T> setMaxRows(int rows) {
      Criteria limited = this.criteria.setMaxResults(rows);
      return new Query<T>(this.type, limited);
    }

    public Query<T> random() {
      Criteria random = this.criteria.add(Restrictions.sqlRestriction("1=1 order by random()"));
      return new Query<T>(this.type, random);
    }

    protected Criteria getCriteria() {
      return criteria;
    }
  }

  public static class PagedQuery<T> extends Query<T> {

    private int size;
    private int number;

    public PagedQuery(Class<T> type, Criteria criteria, int pageSize) {
      super(type, criteria);
      this.size = pageSize;
    }

    public PagedQuery(Class<T> type, Criteria criteria, int pageSize, int number) {
      this(type, criteria, pageSize);
      this.number = number;
    }

    public PagedQuery<T> getPage(int number) {
      this.number = number;
      return this;
    }

    @SuppressWarnings("unchecked")
    public List<T> getList() {
      int firstResult = number*size;
      return getCriteria().setFirstResult(firstResult).setMaxResults(size).list();
    }
  }
}