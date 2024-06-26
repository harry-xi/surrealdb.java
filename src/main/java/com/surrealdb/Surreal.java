package com.surrealdb;

import com.surrealdb.signin.Jwt;
import com.surrealdb.signin.Root;
import com.surrealdb.signin.Signin;

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

    private static native boolean useNs(long ptr, String ns);

    private static native boolean useDb(long ptr, String ns);

    private static native long query(long ptr, String sql);

    private static native long queryBind(long ptr, String sql, Map<String, ?> params);

    private static native long createThingValue(long ptr, long thingPtr, long valuePtr);

    private static native long createTargetsValue(long ptr, String targets, long valuePtr);

    private static native long[] createTargetsValues(long ptr, String targets, long[] valuePtrs);

    private static native long updateThingValue(long ptr, long thingPtr, long valuePtr);

    private static native long updateTargetsValues(long ptr, String targets, long valuePtr);

    private static native long updateTargetsValuesSync(long ptr, String targets, long valuePtr);

    private static native long selectThing(long ptr, long thing);

    private static native long[] selectThings(long ptr, long[] things);

    private static native long selectTargetsValues(long ptr, String targets);

    private static native long selectTargetsValuesSync(long ptr, String targets);

    private static native boolean deleteThing(long ptr, long thing);

    private static native boolean deleteThings(long ptr, long[] things);

    private static native boolean deleteTargets(long ptr, String targets);


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

    public Surreal connect(String connect) {
        connect(getPtr(), connect);
        return this;
    }

    public Jwt signin(Signin signin) {
        if (signin instanceof Root) {
            final Root r = (Root) signin;
            return new Jwt(signinRoot(getPtr(), r.getUsername(), r.getPassword()));
        }
        throw new SurrealException("Unsupported signin");
    }

    public Surreal useNs(String ns) {
        useNs(getPtr(), ns);
        return this;
    }

    public Surreal useDb(String ns) {
        useDb(getPtr(), ns);
        return this;
    }

    public Response query(String sql) {
        return new Response(query(getPtr(), sql));
    }

    public Response queryBind(String sql, Map<String, ?> params) {
        return new Response(queryBind(getPtr(), sql, params));
    }

    public <T> Value create(Thing thg, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = createThingValue(getPtr(), thg.getPtr(), valueMut.getPtr());
        return new Value(valuePtr);
    }

    public <T> T create(Class<T> type, Thing thg, T content) {
        return create(thg, content).get(type);
    }

    public <T> Value create(String targets, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = createTargetsValue(getPtr(), targets, valueMut.getPtr());
        return new Value(valuePtr);
    }

    public <T> T create(Class<T> type, String targets, T content) {
        return create(targets, content).get(type);
    }

    public <T> List<Value> create(String targets, T... contents) {
        final long[] valueMutPtrs = contents2longs(contents);
        final long[] valuePtrs = createTargetsValues(getPtr(), targets, valueMutPtrs);
        return Arrays.stream(valuePtrs).mapToObj(Value::new).collect(Collectors.toList());
    }

    public <T> List<T> create(Class<T> type, String targets, T... contents) {
        try (final Stream<Value> s = create(targets, contents).stream()) {
            return s.map(v -> v.get(type)).collect(Collectors.toList());
        }
    }

    public <T> Value update(Thing thg, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = updateThingValue(getPtr(), thg.getPtr(), valueMut.getPtr());
        return new Value(valuePtr);
    }

    public <T> T update(Class<T> type, Thing thg, T content) {
        return update(thg, content).get(type);
    }

    public <T> Iterator<Value> update(String targets, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new ValueIterator(updateTargetsValues(getPtr(), targets, valueMut.getPtr()));
    }

    public <T> Iterator<T> update(Class<T> type, String targets, T content) {
        return new ValueObjectIterator<>(type, update(targets, content));
    }

    public <T> Iterator<Value> updateSync(String targets, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new SynchronizedValueIterator(updateTargetsValuesSync(getPtr(), targets, valueMut.getPtr()));
    }

    public <T> Iterator<T> updateSync(Class<T> type, String targets, T content) {
        return new ValueObjectIterator<>(type, updateSync(targets, content));
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

    public Optional<Value> select(Thing thing) {
        final long valuePtr = selectThing(getPtr(), thing.getPtr());
        if (valuePtr == 0) {
            return Optional.empty();
        }
        return Optional.of(new Value(valuePtr));
    }

    public <T> Optional<T> select(Class<T> type, Thing thing) {
        return select(thing).map(v -> v.get(type));
    }

    public List<Value> select(Thing... things) {
        final long[] thingsPtr = things2longs(things);
        final long[] valuePtrs = selectThings(getPtr(), thingsPtr);
        try (final LongStream s = Arrays.stream(valuePtrs)) {
            return s.mapToObj(Value::new).collect(Collectors.toList());
        }
    }

    private long[] things2longs(Thing... things) {
        final long[] ptrs = new long[things.length];
        int index = 0;
        for (final Thing t : things) {
            ptrs[index++] = t.getPtr();
        }
        return ptrs;
    }

    public <T> List<T> select(Class<T> type, Thing... things) {
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

    public void delete(Thing thing) {
        deleteThing(getPtr(), thing.getPtr());
    }

    public void delete(Thing... things) {
        final long[] thingsPtr = things2longs(things);
        deleteThings(getPtr(), thingsPtr);
    }

    public void delete(String targets) {
        deleteTargets(getPtr(), targets);
    }

    @Override
    public void close() {
        deleteInstance();
    }
}
