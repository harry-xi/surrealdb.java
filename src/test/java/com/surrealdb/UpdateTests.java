package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static com.surrealdb.Helpers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateTests {

    @Test
    void updateThingValue() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Build an id
            final RecordId id = new RecordId("person", 1);
            // Let's create a person
            final Value created = surreal.create(id, tobie);
            // Update the person in SurrealDB
            final Value updated = surreal.update(id, UpType.MERGE, jaime);
            // Check the person has been updated
            assertEquals("Jaime", updated.get(Person.class).name);
        }
    }

    @Test
    void updateThingObject() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Build an id
            final RecordId id = new RecordId("person", 1);
            // Let's create a person
            final Person created = surreal.create(Person.class, id, tobie);
            // Update the person in SurrealDB
            final Person updated = surreal.update(Person.class, id, UpType.CONTENT, jaime);
            // Check the person has been updated
            assertEquals("Jaime", updated.name);
        }
    }

    @Test
    void updateTableValue() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create some records
            surreal.create(new RecordId("person", 1), tobie);
            surreal.create(new RecordId("person", 2), emmanuel);
            // We update the records
            final Iterator<Value> updated = surreal.update("person", UpType.CONTENT, jaime);
            // Check the updated values
            updated.forEachRemaining(value -> {
                assertEquals("Jaime", value.getObject().get("name").getString());
            });
            // Check the values in SurrealDB
            final Iterator<Value> selected = surreal.select("person");
            selected.forEachRemaining(value -> {
                assertEquals("Jaime", value.getObject().get("name").getString());
            });
        }
    }

    @Test
    void updateTableValueSync() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create some records
            surreal.create(new RecordId("person", 1), tobie);
            surreal.create(new RecordId("person", 2), emmanuel);
            // We update the records
            final Iterator<Value> updated = surreal.updateSync("person", UpType.CONTENT, jaime);
            // Check the updated values
            updated.forEachRemaining(value -> {
                assertEquals("Jaime", value.getObject().get("name").getString());
            });
            // Check the values in SurrealDB
            final Iterator<Value> selected = surreal.selectSync("person");
            selected.forEachRemaining(value -> {
                assertEquals("Jaime", value.getObject().get("name").getString());
            });
        }
    }

    @Test
    void updateTableObjects() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create some records
            surreal.create(new RecordId("person", 1), tobie);
            surreal.create(new RecordId("person", 2), emmanuel);
            // We update the records
            final Iterator<Person> updated = surreal.update(Person.class, "person", UpType.CONTENT, jaime);
            // Check the updated values
            updated.forEachRemaining(person -> {
                assertEquals("Jaime", person.name);
            });
            // Check the values in SurrealDB
            final Iterator<Person> selected = surreal.select(Person.class, "person");
            selected.forEachRemaining(person -> {
                assertEquals("Jaime", person.name);
            });
        }
    }

    @Test
    void updateTableObjectsSync() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create some records
            surreal.create(new RecordId("person", 1), tobie);
            surreal.create(new RecordId("person", 2), emmanuel);
            // We update the records
            final Iterator<Person> updated = surreal.updateSync(Person.class, "person", UpType.CONTENT, jaime);
            // Check the updated values
            updated.forEachRemaining(person -> {
                assertEquals("Jaime", person.name);
            });
            // Check the values in SurrealDB
            final Iterator<Person> selected = surreal.selectSync(Person.class, "person");
            selected.forEachRemaining(person -> {
                assertEquals("Jaime", person.name);
            });
        }
    }

}
