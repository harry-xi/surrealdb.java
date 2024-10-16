package com.surrealdb;

import com.surrealdb.signin.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Surreal extends Native implements AutoCloseable {

    static {
        Loader.loadNative();
    }

    public Surreal() {
        super(Surreal.newInstance());
    }

    private static native long newInstance();

    private static native boolean connect(long ptr, String connect);

    private static native String signinRoot(long ptr, String username, String password);

    private static native String signinNamespace(long ptr, String username, String password, String ns);

    private static native String signinDatabase(long ptr, String username, String password, String ns, String db);

    private static native boolean useNs(long ptr, String ns);

    private static native boolean useDb(long ptr, String ns);

    private static native long query(long ptr, String sql);

    private static native long queryBind(long ptr, String sql, Map<String, ?> params);

    private static native long createThingValue(long ptr, long thingPtr, long valuePtr);

    private static native long[] createTargetValues(long ptr, String target, long[] valuePtrs);

    private static native long[] insertTargetValues(long ptr, String target, long[] valuePtrs);

    private static native long insertRelationTargetValue(long ptr, String target, long valuePtr);

    private static native long[] insertRelationTargetValues(long ptr, String target, long[] valuePtrs);

    private static native long relate(long ptr, long from, String table, long to);

    private static native long relateContent(long ptr, long from, String table, long to, long valuePtr);

    private static native long updateThingValue(long ptr, long thingPtr, int update, long valuePtr);

    private static native long updateTargetValue(long ptr, String target, int update, long valuePtr);

    private static native long updateTargetsValue(long ptr, String[] targets, int update, long valuePtr);

    private static native long updateTargetValueSync(long ptr, String target, int update, long valuePtr);

    private static native long updateTargetsValueSync(long ptr, String[] targets, int update, long valuePtr);

    private static native long upsertThingValue(long ptr, long thingPtr, int update, long valuePtr);

    private static native long upsertTargetValue(long ptr, String target, int update, long valuePtr);

    private static native long upsertTargetsValue(long ptr, String[] targets, int update, long valuePtr);

    private static native long upsertTargetValueSync(long ptr, String target, int update, long valuePtr);

    private static native long upsertTargetsValueSync(long ptr, String[] targets, int update, long valuePtr);

    private static native long selectThing(long ptr, long thing);

    private static native long[] selectThings(long ptr, long[] things);

    private static native long selectTargetsValues(long ptr, String... targets);

    private static native long selectTargetsValuesSync(long ptr, String... targets);

    private static native boolean deleteThing(long ptr, long thing);

    private static native boolean deleteThings(long ptr, long[] things);

    private static native boolean deleteTarget(long ptr, String target);


    @Override
    final String toString(long ptr) {
        return getClass().getName() + "[ptr=" + ptr + "]";
    }

    @Override
    final int hashCode(long ptr) {
        return Objects.hashCode(ptr);
    }

    @Override
    final boolean equals(long ptr1, long ptr2) {
        return ptr1 == ptr2;
    }

    @Override
    final native boolean deleteInstance(long ptr);

    /**
     * Establishes a connection to the Surreal database using the provided connection string.
     *
     * @param connect the connection string used to establish the connection
     * @return the current instance of the {@code Surreal} class
     */
    public Surreal connect(String connect) {
        connect(getPtr(), connect);
        return this;
    }

    /**
     * Attempts to sign in to the Surreal system using the provided credentials.
     * The type of signin object determines the scope of the sign-in (Root, Namespace, or Database).
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealdb/security/authentication">authentication documentation</a>.
     * <p>
     *
     * @param signin the credentials for signing in, which can be an instance of Root, Namespace, or Database
     * @return a Token representing the session token after a successful sign-in
     * @throws SurrealException if the signin type is unsupported
     */
    public Token signin(Signin signin) {
        if (signin instanceof Database) {
            final Database db = (Database) signin;
            return new Token(signinDatabase(getPtr(), db.getUsername(), db.getPassword(), db.getNamespace(), db.getDatabase()));
        } else if (signin instanceof Namespace) {
            final Namespace ns = (Namespace) signin;
            return new Token(signinNamespace(getPtr(), ns.getUsername(), ns.getPassword(), ns.getNamespace()));
        } else if (signin instanceof Root) {
            final Root r = (Root) signin;
            return new Token(signinRoot(getPtr(), r.getUsername(), r.getPassword()));
        }
        throw new SurrealException("Unsupported sign in");
    }

    /**
     * Sets the namespace for the Surreal instance.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/use">use statement documentation</a>.
     * <p>
     *
     * @param ns the namespace to use
     * @return the current instance of the {@code Surreal} class
     */
    public Surreal useNs(String ns) {
        useNs(getPtr(), ns);
        return this;
    }

    /**
     * Sets the database for the current instance of the Surreal class.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/use">use statement documentation</a>.
     * <p>
     *
     * @param ns the database name to use
     * @return the current instance of the {@code Surreal} class
     */
    public Surreal useDb(String ns) {
        useDb(getPtr(), ns);
        return this;
    }

    /**
     * Executes a SurrealQL query on the database.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql">SurrealQL documentation</a>.
     * <p>
     *
     * @param sql the SurrealQL query to be executed
     * @return a Response object containing the results of the query
     */
    public Response query(String sql) {
        return new Response(query(getPtr(), sql));
    }

    /**
     * Executes a parameterized SurrealQL query on the database.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql">SurrealQL documentation</a>.
     * <p>
     *
     * @param sql    the SurrealQL query to be executed
     * @param params a map containing parameter values to be bound to the SQL query
     * @return a Response object containing the results of the query
     */
    public Response queryBind(String sql, Map<String, ?> params) {
        return new Response(queryBind(getPtr(), sql, params));
    }

    /**
     * Creates a record in the database with the given `RecordID` as the key and the provided content as the value.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/create">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>     the type of the content
     * @param thg     the RecordId associated with the new record
     * @param content the content of the created record
     * @return a new Value object initialized with the provided RecordId and content
     */
    public <T> Value create(RecordId thg, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = createThingValue(getPtr(), thg.getPtr(), valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Creates a record in the database with the given `RecordID` as the key and the provided content as the value.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/create">SurrealQL documentation</a>.
     * <p>
     *
     * @param type    The class type of the object to create
     * @param thg     The RecordId used with the new record
     * @param content The content of the created record
     * @return An instance of the specified type
     */
    public <T> T create(Class<T> type, RecordId thg, T content) {
        return create(thg, content).get(type);
    }

    /**
     * Creates records in the database with the given table and the provided contents as the values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/create">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of the contents
     * @param target   the target for which the records are created
     * @param contents the contents of the created records
     * @return a list of Value objects created based on the target and contents
     */
    @SafeVarargs
    public final <T> List<Value> create(String target, T... contents) {
        final long[] valueMutPtrs = contents2longs(contents);
        final long[] valuePtrs = createTargetValues(getPtr(), target, valueMutPtrs);
        return Arrays.stream(valuePtrs).mapToObj(Value::new).collect(Collectors.toList());
    }

    /**
     * Creates records in the database with the given table and the provided contents as the values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/create">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of objects to be created
     * @param type     the class of the type to be created
     * @param target   the target string used in the creation process
     * @param contents the contents to be used to create the objects
     * @return a list of objects of the specified type
     */
    @SafeVarargs
    public final <T> List<T> create(Class<T> type, String target, T... contents) {
        try (final Stream<Value> s = create(target, contents).stream()) {
            return s.map(v -> v.get(type)).collect(Collectors.toList());
        }
    }

    /**
     * Insert records in the database with the given table and the provided contents as the values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of the contents
     * @param target   the target for which the records are inserted
     * @param contents the contents of the inserted records
     * @return a list of Value objects inserted based on the target and contents
     */
    @SafeVarargs
    public final <T> List<Value> insert(String target, T... contents) {
        final long[] valueMutPtrs = contents2longs(contents);
        final long[] valuePtrs = insertTargetValues(getPtr(), target, valueMutPtrs);
        return Arrays.stream(valuePtrs).mapToObj(Value::new).collect(Collectors.toList());
    }

    /**
     * Insert records in the database with the given table and the provided contents as the values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of objects to be inserted
     * @param type     the class of the type to be inserted
     * @param target   the target string used in the insertion process
     * @param contents the contents to be used to insert the objects
     * @return a list of objects of the specified type
     */
    @SafeVarargs
    public final <T> List<T> insert(Class<T> type, String target, T... contents) {
        try (final Stream<Value> s = insert(target, contents).stream()) {
            return s.map(v -> v.get(type)).collect(Collectors.toList());
        }
    }

    /**
     * Inserts a relation to the specified table using the provided content.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert#insert-relation-tables">SurrealQL documentation</a>.
     * <p>
     *
     * @param target  the table where the relation is to be inserted
     * @param content the content to insert as the relation
     * @param <T>     a type that extends InsertRelation
     * @return a Value object representing the inserted relation
     */
    public <T extends InsertRelation> Value insertRelation(String target, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = insertRelationTargetValue(getPtr(), target, valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Inserts a relation of the specified type and table with the provided content.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert#insert-relation-tables">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>     the type of the relation that extends InsertRelation
     * @param type    the class object of the type T
     * @param target  the target identifier for the relation
     * @param content the content to be inserted in the relation
     * @return the inserted relation of type T
     */
    public <T extends InsertRelation> T insertRelation(Class<T> type, String target, T content) {
        return insertRelation(target, content).get(type);
    }

    /**
     * Inserts relations into the specified table with the provided contents.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert#insert-relation-tables">SurrealQL documentation</a>.
     * <p>
     *
     * @param target   the table into which the relations will be inserted
     * @param contents the contents of the relations to be inserted
     * @param <T>      the type of the insert relation
     * @return a list of values representing the result of the insert operation
     */
    @SafeVarargs
    public final <T extends InsertRelation> List<Value> insertRelations(String target, T... contents) {
        final long[] valueMutPtrs = contents2longs(contents);
        final long[] valuePtrs = insertRelationTargetValues(getPtr(), target, valueMutPtrs);
        return Arrays.stream(valuePtrs).mapToObj(Value::new).collect(Collectors.toList());
    }

    /**
     * Inserts multiple relations of a specified type into the target.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert#insert-relation-tables">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of InsertRelation
     * @param type     the class type of the InsertRelation
     * @param target   the table in which the relations are to be inserted
     * @param contents the array of InsertRelation objects to be inserted
     * @return a list of inserted relations of the specified type
     */
    @SafeVarargs
    public final <T extends InsertRelation> List<T> insertRelations(Class<T> type, String target, T... contents) {
        try (final Stream<Value> s = insertRelations(target, contents).stream()) {
            return s.map(v -> v.get(type)).collect(Collectors.toList());
        }
    }

    /**
     * Establishes a relation between two records identified by `from` and `to` within a specified table.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/relate">SurrealQL documentation</a>.
     * <p>
     *
     * @param from  the record identifier from which the relation originates
     * @param table the name of the table where the relation will be established
     * @param to    the record identifier to which the relation points
     * @return a new {@code Value} instance representing the relation
     */
    public Value relate(RecordId from, String table, RecordId to) {
        final long valuePtr = relate(getPtr(), from.getPtr(), table, to.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Establishes a relationship between two records in a specified table and returns the specified type of Relation.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/relate">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>  the type of Relation to return, which must extend the Relation class
     * @param type the class object of type T, used to specify the type of the returned Relation
     * @param from the RecordId representing the starting record of the relationship
     **/
    public <T extends Relation> T relate(Class<T> type, RecordId from, String table, RecordId to) {
        return relate(from, table, to).get(type);
    }

    /**
     * Establishes a relationship between two records within a specified table,
     * attaching the provided content to this relationship.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/relate">SurrealQL documentation</a>.
     * <p>
     *
     * @param from    The record ID that the relationship starts from.
     * @param table   The table in which the relationship is being created.
     * @param to      The record ID that the relationship points to.
     * @param content The content to attach to the relationship.
     * @return A Value object representing the newly created relationship.
     */
    public <T> Value relate(RecordId from, String table, RecordId to, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = relateContent(getPtr(), from.getPtr(), table, to.getPtr(), valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Establishes a relation between two records and retrieves it based on the specified relation type.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/relate">SurrealQL documentation</a>.
     * <p>
     *
     * @param <R>     the type of the relation
     * @param <T>     the type of the content associated with the relation
     * @param type    the class of the relation type
     * @param from    the record identifier of the source record
     * @param table   the name of the table where the relation is to be established
     * @param to      the record identifier of the target record
     * @param content the content to be associated with the relation
     * @return the established relation of the specified type
     */
    public <R extends Relation, T> R relate(Class<R> type, RecordId from, String table, RecordId to, T content) {
        return relate(from, table, to, content).get(type);
    }

    public <T> Value update(RecordId thg, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = updateThingValue(getPtr(), thg.getPtr(), upType.code, valueMut.getPtr());
        return new Value(valuePtr);
    }

    public <T> T update(Class<T> type, RecordId thg, UpType upType, T content) {
        return update(thg, upType, content).get(type);
    }

    public <T> Iterator<Value> update(String target, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new ValueIterator(updateTargetValue(getPtr(), target, upType.code, valueMut.getPtr()));
    }

    public <T> Iterator<Value> update(String[] targets, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new ValueIterator(updateTargetsValue(getPtr(), targets, upType.code, valueMut.getPtr()));
    }

    public <T> Iterator<T> update(Class<T> type, String target, UpType upType, T content) {
        return new ValueObjectIterator<>(type, update(target, upType, content));
    }

    public <T> Iterator<T> update(Class<T> type, String[] targets, UpType upType, T content) {
        return new ValueObjectIterator<>(type, update(targets, upType, content));
    }

    public <T> Iterator<Value> updateSync(String target, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new SynchronizedValueIterator(updateTargetValueSync(getPtr(), target, upType.code, valueMut.getPtr()));
    }

    public <T> Iterator<Value> updateSync(String[] targets, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new SynchronizedValueIterator(updateTargetsValueSync(getPtr(), targets, upType.code, valueMut.getPtr()));
    }

    public <T> Iterator<T> updateSync(Class<T> type, String target, UpType upType, T content) {
        return new ValueObjectIterator<>(type, updateSync(target, upType, content));
    }

    public <T> Iterator<T> updateSync(Class<T> type, String[] targets, UpType upType, T content) {
        return new ValueObjectIterator<>(type, updateSync(targets, upType, content));
    }

    public <T> Value upsert(RecordId thg, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = upsertThingValue(getPtr(), thg.getPtr(), upType.code, valueMut.getPtr());
        return new Value(valuePtr);
    }

    public <T> T upsert(Class<T> type, RecordId thg, UpType upType, T content) {
        return upsert(thg, upType, content).get(type);
    }

    public <T> Iterator<Value> upsert(String target, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new ValueIterator(upsertTargetValue(getPtr(), target, upType.code, valueMut.getPtr()));
    }

    public <T> Iterator<Value> upsert(String[] targets, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new ValueIterator(upsertTargetsValue(getPtr(), targets, upType.code, valueMut.getPtr()));
    }

    public <T> Iterator<T> upsert(Class<T> type, String target, UpType upType, T content) {
        return new ValueObjectIterator<>(type, upsert(target, upType, content));
    }

    public <T> Iterator<T> upsert(Class<T> type, String[] targets, UpType upType, T content) {
        return new ValueObjectIterator<>(type, upsert(targets, upType, content));
    }

    public <T> Iterator<Value> upsertSync(String target, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new SynchronizedValueIterator(upsertTargetValueSync(getPtr(), target, upType.code, valueMut.getPtr()));
    }

    public <T> Iterator<Value> upsertSync(String[] targets, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new SynchronizedValueIterator(upsertTargetsValueSync(getPtr(), targets, upType.code, valueMut.getPtr()));
    }

    public <T> Iterator<T> upsertSync(Class<T> type, String target, UpType upType, T content) {
        return new ValueObjectIterator<>(type, upsertSync(target, upType, content));
    }

    public <T> Iterator<T> upsertSync(Class<T> type, String[] targets, UpType upType, T content) {
        return new ValueObjectIterator<>(type, upsertSync(targets, upType, content));
    }

    @SafeVarargs
    private final <T> long[] contents2longs(T... contents) {
        final long[] ptrs = new long[contents.length];
        int index = 0;
        for (final T c : contents) {
            ptrs[index++] = ValueBuilder.convert(c).getPtr();
        }
        return ptrs;
    }

    public Optional<Value> select(RecordId recordId) {
        final long valuePtr = selectThing(getPtr(), recordId.getPtr());
        if (valuePtr == 0) {
            return Optional.empty();
        }
        return Optional.of(new Value(valuePtr));
    }

    public <T> Optional<T> select(Class<T> type, RecordId recordId) {
        return select(recordId).map(v -> v.get(type));
    }

    public List<Value> select(RecordId... things) {
        final long[] thingsPtr = things2longs(things);
        final long[] valuePtrs = selectThings(getPtr(), thingsPtr);
        try (final LongStream s = Arrays.stream(valuePtrs)) {
            return s.mapToObj(Value::new).collect(Collectors.toList());
        }
    }

    private long[] things2longs(RecordId... things) {
        final long[] ptrs = new long[things.length];
        int index = 0;
        for (final RecordId t : things) {
            ptrs[index++] = t.getPtr();
        }
        return ptrs;
    }

    public <T> List<T> select(Class<T> type, RecordId... things) {
        try (final Stream<Value> s = select(things).stream()) {
            return s.map(v -> v.get(type)).collect(Collectors.toList());
        }
    }

    public Iterator<Value> select(String targets) {
        return new ValueIterator(selectTargetsValues(getPtr(), targets));
    }

    public Iterator<Value> selectSync(String targets) {
        return new SynchronizedValueIterator(selectTargetsValuesSync(getPtr(), targets));
    }

    public <T> Iterator<T> select(Class<T> type, String targets) {
        return new ValueObjectIterator<>(type, select(targets));
    }

    public <T> Iterator<T> selectSync(Class<T> type, String targets) {
        return new ValueObjectIterator<>(type, selectSync(targets));
    }

    public void delete(RecordId recordId) {
        deleteThing(getPtr(), recordId.getPtr());
    }

    public void delete(RecordId... things) {
        final long[] thingsPtr = things2longs(things);
        deleteThings(getPtr(), thingsPtr);
    }

    public void delete(String target) {
        deleteTarget(getPtr(), target);
    }

    @Override
    public void close() {
        deleteInstance();
    }
}
