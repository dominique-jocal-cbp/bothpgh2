package cbp.bothpgh2;

import org.hibernate.HibernateException;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * UserType Hibernate pour mapper des Enum Java sur des Enum carac PG / Other H2.
 * <p/>
 * <p> inspiré de lb@octagen.at, http://octagen.at/2014/10/postgresql-custom-data-types-enum-in-hibernate/
 * http://loongest.blogspot.com/2009/09/jpa-hibernate-enum-mapping-tutorial.html
 * mais dramatiquement simplifié; </p>
 * <p/>
 * <p>Principe:</p><ul>
 * <li>on rajoute le type énuméré souhaité dans H2 comme alias (domain) de VARCHAR.</li>
 * <li>en lecture, on demande aux 2 drivers JDBC la version String et on convertit en énuméré Java.</li>
 * <li>en écriture, on convertit l'énuméré Java en chaine, et on l'écrit en setObject pour PG et setString pour H2.</li>
 * </ul>
 * <p>Comme exemple d'usage, voir le zoo:
 * énuméré sql PG,
 * table sql PG avec énuméré,
 * énuméré Java et son user type Hibernate,
 * domain class avec énuméré,
 * mapping de l'énuméré dans les domain class,
 * aliasing de l'énuméré dans datasource de dev H2.</p>
 */
public abstract class GenericEnumType<E extends Enum<E>> implements UserType {

    protected GenericEnumType(Class<E> clazz) {
        this.clazz = clazz;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        String value = rs.getString(names[0]);
        if (!rs.wasNull()) {
            return Enum.valueOf(clazz, value);
        } else {
            return null;
        }
    }

    public void nullSafeSet(PreparedStatement ps, Object obj, int index, SessionImplementor session)
            throws HibernateException, SQLException {
        if (obj == null) {
            ps.setNull(index, Types.NULL);
        } else {
            if (session.getFactory().getDialect() instanceof H2Dialect) {
                // setObject() "marche aussi" avec H2 mais cela stoque l'adresse de la string au lieu de sa valeur;
                // la restitution en hibernate ou groovy sql est OK
                // mais KO en dbconsole, qui affiche l'adresse au lieu de la vaeur. Or pas de dev dans dbconsole !
                ps.setString(index, obj.toString());
            } else {
                ps.setObject(index, obj.toString(), Types.OTHER);
            }
        }
    }

    public int[] sqlTypes() {
        return new int[]{Types.OTHER};
    }

    public Class<E> returnedClass() {
        return clazz;
    }

    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return cached;
    }

    public Object deepCopy(Object obj) throws
            HibernateException {
        return obj;
    }

    public Serializable disassemble(Object obj) throws
            HibernateException {
        return (Serializable) obj;
    }

    public boolean equals(Object obj1, Object obj2) throws
            HibernateException {
        return obj1 == obj2 || !(obj1 == null || obj2 == null) && obj1.equals(obj2);

    }

    public int hashCode(Object obj) throws HibernateException {
        return obj.hashCode();
    }

    public boolean isMutable() {
        return false;
    }

    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return original;
    }

    private final Class<E> clazz;
}