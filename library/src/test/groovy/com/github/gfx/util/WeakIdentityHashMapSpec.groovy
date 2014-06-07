package com.github.gfx.util;

import spock.lang.Specification

public class WeakIdentityHashMapSpec extends Specification {
    class Foo {
        int value = 10

        @Override
        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            Foo foo = (Foo) o

            if (value != foo.value) return false

            return true
        }

        @Override
        int hashCode() {
            return value
        }
    }

    Map<Foo, String> emptyMap
    Map<Foo, String> map
    Foo foo
    Foo bar
    Foo nonExisting

    void setup() {
        emptyMap = new WeakIdentityHashMap<>()
        map = new WeakIdentityHashMap<>()
        foo = new Foo()
        bar = new Foo()
        nonExisting = new Foo()
        map.put(foo, "aaa")
        map.put(bar, "bbb")
        System.gc()
    }

    def "new"() {
        when:
        map = new WeakIdentityHashMap<>()

        then:
        map.isEmpty()
    }

    def "new with map"() {
        when:
        def other = new IdentityHashMap<Foo, String>()
        other.put(foo, "aaa")
        other.put(bar, "bbb")
        map = new WeakIdentityHashMap<>(other)

        then:
        map.get(foo) == "aaa"
        map.get(bar) == "bbb"
    }

    def "new with zero capacity"() {
        when:
        map = new WeakIdentityHashMap<>(0)
        map.put(foo, "aaa")
        map.put(bar, "bbb") // will call rehash()

        then:
        map.size() == 2
    }

    def "new with negative capacity"() {
        when:
        map = new WeakIdentityHashMap<>(-1)

        then:
        thrown IllegalArgumentException
    }

    def "new with zero factor"() {
        when:
        map = new WeakIdentityHashMap<>(0, 0)

        then:
        thrown IllegalArgumentException
    }

    def "put/get"() {
        when:
        def foo = new Foo()
        map.put(foo, "bar")
        System.gc()

        then:
        map.get(foo) == "bar"
        map.get(nonExisting) == null
    }

    def "put again"() {
        when:
        map.put(foo, "put again")

        then:
        map.get(foo) == "put again"
        map.size() == 2
    }

    def "putAll"() {
        setup:
        def baz = new Foo()
        def bax = new Foo()
        def other = new IdentityHashMap<Foo, String>()
        other.put(baz, "ccc")
        other.put(bax, "ddd")

        when:
        map.putAll(other)
        System.gc()

        then:
        map.get(foo) == "aaa"
        map.get(bar) == "bbb"
        map.get(baz) == "ccc"
        map.get(bax) == "ddd"
    }

    def "equals for identity"() {
        expect:
        map.equals(map)
        !map.equals(emptyMap)
    }

    def "hashCode for identity"() {
        expect:
        map.hashCode() == map.hashCode()
        emptyMap.hashCode() == emptyMap.hashCode()
    }

    def "size"() {
        expect:
        emptyMap.size() == 0
        map.size() == 2
    }

    def "isEmpty"() {
        expect:
        emptyMap.isEmpty()
        !map.isEmpty()
    }

    def "weakness"() {
        when:
        map.put(new Foo(), "xxx")
        map.put(new Foo(), "yyy")
        System.gc()
        System.gc()
        System.gc()
        System.gc()

        then:
        map.size() == 2
    }

    def "identity"() {
        when:
        def foo = new Foo()
        foo.value = 10
        map.put(foo, "bar")
        foo.value = 20

        then:
        map.get(foo) == "bar"
    }

    def "keySet"() {
        expect:
        map.keySet().containsAll([foo, bar])
    }

    def "keySet().size()"() {
        expect:
        map.keySet().size() == 2
    }

    def "keySet().clear()"() {
        when:
        map.keySet().clear()

        then:
        map.isEmpty()
    }

    def "keySet().contains()"() {
        expect:
        map.keySet().contains(foo)
        !map.keySet().contains(nonExisting)
    }

    def "keySet() for each key"() {
        when:
        def keys = []
        for (Foo key : map.keySet()) {
            keys.add(key)
        }

        then:
        keys.size() == 2
        keys.contains(foo)
        keys.contains(bar)
    }

    def "values"() {
        expect:
        map.values().sort() == ["aaa", "bbb"]
    }

    def "values().clear()"() {
        when:
        map.values().clear()

        then:
        map.isEmpty()
    }

    def "values().contains()"() {
        expect:
        map.values().contains("aaa")
        map.values().contains("bbb")
        !map.values().contains("non existing value")
    }

    def "clear"() {
        when:
        map.clear()

        then:
        map.size() == 0
        map.get(foo) == null
        map.get(bar) == null
    }

    def "containsKey"() {
        expect:
        map.containsKey(foo)
        map.containsKey(bar)
        !map.containsKey(nonExisting)
    }

    def "containsValue"() {
        expect:
        map.containsValue("aaa")
        !map.containsValue("ccc")
        !map.containsValue(foo)
        !map.containsValue(null)
    }

    def "containsValue() if null entry with null value exists"() {
        setup:
        map.put(null, null)

        expect:
        map.containsValue(null)
    }

    def "remove()"() {
        when:
        map.remove(foo)

        then:
        !map.containsKey(foo)
    }

    def "remove(nonExisting)"() {
        expect:
        map.remove(nonExisting) == null
    }


    def "put/get if null key exists"() {
        when:
        map.put(null, "bar")
        System.gc()

        then:
        map.containsKey(null)
        map.get(null) == "bar"
    }

    def "put/get if null key doesn't exist"() {
        expect:
        !map.containsKey(null)
        map.get(null) == null
    }

    def "null value when exists"() {
        when:
        map.put(foo, null)

        then:
        map.containsKey(foo)
        map.containsValue(null)
    }

    def "null value when doesn't exist"() {
        expect:
        !map.containsValue(null)
    }

    def "entrySet()"() {
        when:
        def m = new IdentityHashMap<Foo, String>()
        for (Map.Entry<Foo, String> entry : map.entrySet()) {
            m.put(entry.key, entry.value)
        }

        then:
        m.size() == 2
        m.get(foo) == "aaa"
        m.get(bar) == "bbb"
    }

    def "remove null key"() {
        when:
        map.put(null, "bar")
        map.remove(null)

        then:
        !map.containsKey(null)
    }

    def "entrySet().size()"() {
        expect:
        map.entrySet().size() == 2
    }

    def "entrySet().clear()"() {
        when:
        map.entrySet().clear()

        then:
        map.entrySet().size() == 0
    }

    def "entrySet().remove() for existing entry"() {
        setup:
        def entry = map.entrySet().iterator().next()

        expect:
        map.entrySet().remove(entry)
        !map.entrySet().contains(entry)
    }

    def "entrySet().remove() for non-existing entry"() {
        expect:
        !map.entrySet().remove(null)
    }

    def "Entry<>#setValue()"() {
        setup:
        def entry = map.entrySet().iterator().next()
        def value = entry.value

        expect:
        entry.setValue("new value") == value
        entry.getValue() == "new value"
    }

    def "Iterator<>#remove()"() {
        setup:
        def iterator = map.entrySet().iterator()
        def entry = iterator.next()

        when:
        iterator.remove()

        then:
        !map.entrySet().contains(entry)
    }

    def "Iterator<>#remove() twice"() {
        setup:
        def iterator = map.entrySet().iterator()

        when:
        iterator.remove()
        iterator.remove()

        then:
        thrown IllegalStateException
    }

    def "Entry<>#equals()"() {
        setup:
        def entry = map.entrySet().iterator().next()

        expect:
        !entry.equals(null)
        !entry.equals(foo)
    }

    def "hash conflicts"() {
        setup:
        def a = new ArrayList<Foo>()
        for (int i = 0; i < 10000; i++) {
            def k = new Foo()
            map.put(k, "" + i)
            a.add(k)
        }

        when:
        System.gc()

        then:
        map.size() == 10002
        for (Foo key : a) {
            map.containsKey(key)
        }
    }

    def "removeAll() with hash conflicts"() {
        setup:
        def a = new ArrayList<Foo>()
        for (int i = 0; i < 10000; i++) {
            def k = new Foo()
            map.put(k, "" + i)
            a.add(k)
        }

        when:
        map.keySet().removeAll(a)

        then:
        map.size() == 2
        map.containsKey(foo)
        map.containsKey(bar)
    }

    def "remove() with hash conflicts"() {
        setup:
        def a = new ArrayList<Foo>()
        for (int i = 0; i < 10000; i++) {
            def k = new Foo()
            map.put(k, "" + i)
            a.add(k)
        }

        when:
        map.remove(foo)
        map.remove(bar)

        then:
        map.size() == 10000
        !map.containsKey(foo)
        !map.containsKey(bar)
    }
}