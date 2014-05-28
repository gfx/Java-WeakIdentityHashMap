package com.github.gfx.util;

import spock.lang.Specification

public class WeakIdentityHashMapSpec extends Specification {
    class Foo {
        int value = 10

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            Foo foo = (Foo) o

            if (value != foo.value) return false

            return true
        }

        int hashCode() {
            return value
        }
    }

    Map<Foo, String> emptyMap
    Map<Foo, String> map
    Foo foo
    Foo bar
    void setup() {
        emptyMap = new WeakIdentityHashMap<>()
        map = new WeakIdentityHashMap<>()
        foo = new Foo()
        bar = new Foo()
        map.put(foo, "aaa")
        map.put(bar, "bbb")
    }

    def "new"() {
        when:
        map = new WeakIdentityHashMap<>()

        then:
        map.isEmpty()
    }

    def "put/get"() {
        when:
        def foo = new Foo()
        map.put(foo, "bar")
        System.gc()

        then:
        map.get(foo) == "bar"
        map.get(new Foo()) == null
    }

    def "putAll"() {
        when:
        def baz = new Foo()
        def bax = new Foo()
        def other = new IdentityHashMap<Foo, String>()
        other.put(baz, "ccc")
        other.put(bax, "ddd")
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
        when:
        System.gc()

        then:
        map.keySet().containsAll([foo, bar])
    }

    def "values"() {
        when:
        System.gc()

        then:
        map.values().sort() == ["aaa", "bbb"]
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
        !map.containsKey(new Foo())
    }

    def "containsValue"() {
        expect:
        map.containsValue("aaa")
        !map.containsValue("ccc")
        !map.containsValue(foo)
    }


    def "remove"() {
        when:
        map.remove(foo)

        then:
        !map.containsKey(foo)
    }

    def "Entry<>"() {
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
}