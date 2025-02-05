package com.surrealdb;

import com.surrealdb.pojos.Dates;
import com.surrealdb.pojos.Numbers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeTests {


    @Test
    void testNumberTypes() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new record
            final Numbers n = new Numbers();
            n.longPrimitive = 1;
            n.longObject = 2L;
            n.intPrimitive = 3;
            n.intObject = 4;
            n.shortPrimitive = 5;
            n.shortObject = 6;
            n.floatPrimitive = 7.5f;
            n.floatObject = 8.5f;
            n.doublePrimitive = 9.5f;
            n.doubleObject = 10.5;
            n.bigDecimal = BigDecimal.valueOf(11.5f);
            // We ingest the record
            final Numbers created = surreal.create(Numbers.class, "number", n).get(0);
            // We check that the record are matching
            assertEquals(created, n);
        }
    }

    @Test
    void testDatesTypes() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new record`
            final Dates d = new Dates();
            d.dateTime = ZonedDateTime.ofInstant(Instant.now().minusSeconds(120), ZoneId.of("UTC"));
            d.duration = Duration.ofMinutes(5);
            // We ingest the record
            final Dates created = surreal.create(Dates.class, "date", d).get(0);
            // We check that the records are matching
            assertEquals(created, d);
        }
    }

    @Test
    void nullAndNone() {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            final HashMap<String,ValueMut> map = new HashMap<>();
            final ValueMut none = ValueMut.createNone();
            map.put("none", none);
            final ValueMut nullValue = ValueMut.createNull();
            map.put("null", nullValue);
            final Response res =  surreal.queryWithValue("return $none;return $null",map);
            assertTrue(res.take(0).isNone());
            assertTrue(res.take(1).isNull());
        }
    }

}
