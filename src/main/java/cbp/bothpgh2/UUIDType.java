package cbp.bothpgh2;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

import java.sql.*;
import java.util.UUID;

/**
 * Created by jocal on 25/11/15.
 * d'après http://stackoverflow.com/questions/25611285/hibernate-uuid-as-uuid-type
 *
 * Fonctionne avec H2 et PG.
 */
public class UUIDType extends AbstractSingleColumnStandardBasicType<UUID> {

    public String getName() {
        return "uuid-name";
    }

    public UUIDType() {
        super(ObjectWithWrapSqlTypeDescriptor.INSTANCE, UUIDTypeDescriptor.INSTANCE);
    }

    public static class ObjectWithWrapSqlTypeDescriptor implements SqlTypeDescriptor {
        public static final ObjectWithWrapSqlTypeDescriptor INSTANCE = new ObjectWithWrapSqlTypeDescriptor();

        public int getSqlType() {
            return Types.VARCHAR;
        }

        @Override
        public boolean canBeRemapped() {
            return true;
        }

        public <X> ValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
            return new BasicBinder<X>(javaTypeDescriptor, this) {
                @Override
                protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
                        throws SQLException {
                    st.setObject(index, javaTypeDescriptor.unwrap(value, UUID.class, options));
                }
            };
        }

        public <X> ValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor) {
            return new BasicExtractor<X>(javaTypeDescriptor, this) {
                @Override
                protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap(rs.getObject(name), options);
                }

                @Override
                protected X doExtract(CallableStatement statement, int index, WrapperOptions options)
                        throws SQLException {
                    return javaTypeDescriptor.wrap(statement.getObject(index), options);
                }

                @Override
                protected X doExtract(CallableStatement statement, String name, WrapperOptions options)
                        throws SQLException {
                    return javaTypeDescriptor.wrap(statement.getObject(name), options);
                }
            };
        }
    }
}