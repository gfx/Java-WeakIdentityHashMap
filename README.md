# WeakIdentityHashMap [![Build Status](https://travis-ci.org/gfx/Java-WeakIdentityHashMap.svg?branch=master)](https://travis-ci.org/gfx/Java-WeakIdentityHashMap) [![Coverage Status](https://coveralls.io/repos/gfx/Java-WeakIdentityHashMap/badge.png)](https://coveralls.io/r/gfx/Java-WeakIdentityHashMap)

## How To Use

In build.gradle:

```gradle
dependencies {
    compile 'com.github.gfx.util:weak-identity-hash-map:2.0.+'
}
```

In code:

```java
Activity activity = ...;
WeakIdentityHashMap<Activity, String> foo = new WeakIdentityHashMap<>()

 // Set an activity's field.
 // If activity is gone, this value is also gone, too.
foo.put(activity, "my field");

String value = foo.get(activity); // "my field"
```

## TEST COVERAGES

```
./gradlew check jacocoTestReport
open library/build/reports/jacoco/test/html/index.html
```


## PERFORMANCE

On Xperia A / Android 4.2.2:


```java
// in an Activity's onCreate()
Map<Activity, String> map = new WeakIdentityHashMap<>();
map.put(this, "foo");

long t0 = System.currentTimeMillis();

for (int i = 0; i < 100000; i++) {
    String a = map.get(this);
    assert a != null;
}
Log.d("XXX", "WeakIdentityHashMap0: " + (System.currentTimeMillis() - t0));

t0 = System.currentTimeMillis();

for (int i = 0; i < 200000; i++) {
    String a = map.get(this);
    assert a != null;
}
Log.d("XXX", "WeakIdentityHashMap1: " + (System.currentTimeMillis() - t0));
```

1.0.0:

```
D/XXX﹕ WeakIdentityHashMap1: 449
D/XXX﹕ WeakIdentityHashMap2: 1108
```

2.0.0:

```
D/XXX﹕ WeakIdentityHashMap1: 167
D/XXX﹕ WeakIdentityHashMap2: 290
```
