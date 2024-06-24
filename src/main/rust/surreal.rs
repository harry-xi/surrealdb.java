use std::collections::BTreeMap;
use std::ptr::null_mut;
use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::{JClass, JLongArray, JString};
use jni::sys::{jboolean, jlong, jlongArray, jsize, jstring};
use parking_lot::Mutex;
use surrealdb::{Error, Response, Surreal};
use surrealdb::engine::any::Any;
use surrealdb::opt::auth::Root;
use surrealdb::sql::Value;

use crate::{create_instance, get_long_array, get_rust_string, get_surreal_instance, get_value_instance, get_value_mut_instance, new_string, release_instance, TOKIO_RUNTIME};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_newInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jlong {
    create_instance::<Surreal<Any>>(Surreal::init())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    release_instance::<Surreal<Any>>(ptr);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_connect<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    addr: JString<'local>,
) -> jboolean {
    let surreal = get_surreal_instance!(&mut env, ptr, || false as jboolean);
    let addr = get_rust_string!(env, addr, || false as jboolean);
    // Connect
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.connect(addr).await }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinRoot<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    username: JString<'local>,
    password: JString<'local>,
) -> jstring {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, null_mut);
    // Convert the parameters
    let username = get_rust_string!(&mut env, username, null_mut);
    let password = get_rust_string!(&mut env, password, null_mut);
    // Signin
    match TOKIO_RUNTIME.block_on(async {
        surreal
            .signin(Root {
                username: &username,
                password: &password,
            })
            .await
    }) {
        Ok(jwt) => {
            let jwt = jwt.into_insecure_token();
            new_string!(&mut env, jwt, null_mut)
        }
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useNs<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    ns: JString<'local>,
) -> jboolean {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, || false as jboolean);
    // Convert the parameters
    let ns = get_rust_string!(&mut env, ns, || false as jboolean);
    // Call use_ns
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.use_ns(ns).await }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true.into()
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useDb<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    db: JString<'local>,
) -> jboolean {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, || false as jboolean);
    // Call use_db
    let db: String = match env.get_string(&db) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.use_db(db).await }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true.into()
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_query<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, || 0);
    // Retrieve the query
    let query: String = match env.get_string(&query) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    // Execute the query
    let res = surrealdb_query(&surreal, &query, None);
    // Check the response
    let res = match res {
        Ok(r) => r,
        Err(e) => return SurrealError::from(e).exception(&mut env, || 0),
    };
    // Build a response instance
    create_instance(Arc::new(Mutex::new(res)))
}

fn surrealdb_query(
    surreal: &Surreal<Any>,
    query: &str,
    params: Option<BTreeMap<String, &Value>>,
) -> Result<Response, Error> {
    TOKIO_RUNTIME.block_on(async {
        let q = surreal.query(query);
        if let Some(p) = params {
            q.bind(p).await
        } else {
            q.await
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_createTableValue<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    resource: JString<'local>,
    value_ptr: jlong,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let resource = get_rust_string!(&mut env, resource, ||0);
    let value = get_value_mut_instance!(&mut env, value_ptr, ||0);
    // Execute the query
    let query = format!("CREATE {resource} CONTENT $val");
    let params = BTreeMap::from([("val".to_string(), value)]);
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    let mut res = match res {
        Ok(res) => res,
        Err(e) => {
            return SurrealError::SurrealDB(e).exception(&mut env, || 0);
        }
    };
    // There is only one statement
    let mut res: Value = match res.take(0) {
        Ok(r) => r,
        Err(e) => return SurrealError::SurrealDB(e).exception(&mut env, || 0),
    };
    // There should be only one result

    if let Value::Array(ref mut a) = res {
        if a.len() == 1 {
            return create_instance(Arc::new(a.remove(0)));
        }
    }
    SurrealError::SurrealDBJni(format!("Unexpected result: {res}")).exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_createTableValues<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    table: JString<'local>,
    value_ptrs: JLongArray<'local>,
) -> jlongArray {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, null_mut);
    // Build the parameters
    let table = get_rust_string!(&mut env, table, null_mut);
    // Get the pointers
    let value_ptrs = get_long_array!(&mut env, &value_ptrs, null_mut);
    // Build the queries
    let mut queries = Vec::with_capacity(value_ptrs.len());
    let mut params = BTreeMap::new();
    for (idx, value_ptr) in value_ptrs.iter().enumerate() {
        queries.push(format!("CREATE {table} CONTENT $i{idx}"));
        let value = get_value_mut_instance!(&mut env, *value_ptr, null_mut);
        params.insert(format!("i{idx}"), value);
    }
    let query = queries.join(";\n");
    // Execute the query
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    let mut res = match res {
        Ok(res) => res,
        Err(e) => {
            return SurrealError::SurrealDB(e).exception(&mut env, null_mut);
        }
    };
    // Prepare the result
    let mut value_ptrs: Vec<jlong> = Vec::with_capacity(res.num_statements());
    // Iterate overt the statement
    for i in 0..res.num_statements() {
        let mut res: Value = match res.take(i) {
            Ok(r) => r,
            Err(e) => return SurrealError::SurrealDB(e).exception(&mut env, null_mut),
        };
        // There should be only one result per statement
        if let Value::Array(ref mut a) = res {
            if a.len() != 1 {
                return SurrealError::SurrealDBJni(format!("Unexpected result: {res}")).exception(&mut env, null_mut);
            }
            let val = a.remove(0);
            let value_ptr = create_instance(Arc::new(val));
            value_ptrs.push(value_ptr);
        }
    }
    build_jlong_array(&mut env, value_ptrs)
}

fn build_jlong_array(env: &mut JNIEnv, value_ptrs: Vec<jlong>) -> jlongArray {
    // Create a new jlongArray with the appropriate length
    let mut jarray = env.new_long_array(value_ptrs.len() as jsize).unwrap();
    // Set the values of the jlongArray
    env.set_long_array_region(&mut jarray, 0, &value_ptrs).unwrap();
    // Return the populated jlongArray
    jarray.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectThing<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptr: jlong,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let thing = get_value_instance!(&mut env, thing_ptr, ||0);
    // Execute the query
    let query = format!("SELECT * FROM {thing}");
    let res = surrealdb_query(&surreal, &query, None);
    // Check the result
    let mut res = match res {
        Ok(res) => res,
        Err(e) => {
            return SurrealError::SurrealDB(e).exception(&mut env, || 0);
        }
    };
    // There is only one statement
    let mut res: Value = match res.take(0) {
        Ok(r) => r,
        Err(e) => return SurrealError::SurrealDB(e).exception(&mut env, || 0),
    };
    // There should be only one result
    if let Value::Array(ref mut a) = res {
        if a.len() == 1 {
            return create_instance(Arc::new(a.remove(0)));
        }
    }
    0
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectThings<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptrs: JLongArray<'local>,
) -> jlongArray {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, null_mut);
    // Get the thing pointers
    let thing_ptrs = get_long_array!(&mut env, &thing_ptrs, null_mut);
    // Extract the things
    let mut things = Vec::with_capacity(thing_ptrs.len());
    for thing_ptr in thing_ptrs {
        let thing = get_value_instance!(&mut env, thing_ptr, null_mut);
        things.push(thing.to_string());
    }
    // Execute the query
    let query = format!("SELECT * FROM {}", things.join(","));
    let res = surrealdb_query(&surreal, &query, None);
    // Check the result
    let mut res = match res {
        Ok(res) => res,
        Err(e) => {
            return SurrealError::SurrealDB(e).exception(&mut env, null_mut);
        }
    };
    // There is only one statement
    let res: Value = match res.take(0) {
        Ok(r) => r,
        Err(e) => return SurrealError::SurrealDB(e).exception(&mut env, null_mut),
    };
    // Prepare the result
    if let Value::Array(a) = res {
        let mut value_ptrs: Vec<jlong> = Vec::with_capacity(a.len());
        for value in a {
            let value_ptr = create_instance(Arc::new(value));
            value_ptrs.push(value_ptr);
        }
        // Return the results
        return build_jlong_array(&mut env, value_ptrs);
    }
    SurrealError::SurrealDBJni(format!("Unexpected result: {res}")).exception(&mut env, null_mut)
}