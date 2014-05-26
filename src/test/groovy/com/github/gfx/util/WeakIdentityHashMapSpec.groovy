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

    Map<Foo, String> map;
    void setup() {
        map = new WeakIdentityHashMap<Foo, String>()
    }

    def "new"() {
        expect: map.size() == 0
    }

    def "put/get"() {
        when:
        def foo = new Foo()
        map.put(foo, "bar")
        System.gc()

        then:
        map.get(foo) == "bar"
    }

    def "weakness"() {
        when:
        map.put(new Foo(), "aaa")
        map.put(new Foo(), "bbb")
        System.gc()

        then:
        map.size() == 0
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

    def "Map interface"() {
        when:
        def foo = new Foo()
        def bar = new Foo()
        map.put(foo, "aaa")
        map.put(bar, "bbb")
        System.gc()

        then:
        map.size() == 2
        map.keySet().containsAll([foo, bar])
        map.values().sort() == ["aaa", "bbb"]
    }

    def "clear"() {
        when:
        def foo = new Foo()
        def bar = new Foo()
        map.put(foo, "aaa")
        map.put(bar, "bbb")
        map.clear()

        then:
        map.size() == 0
        map.get(foo) == null
        map.get(bar) == null
    }
}