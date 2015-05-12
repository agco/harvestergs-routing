// Builder syntax to create a reservation with passengers,
// departing and destination airport and make it a 2-way flight.
/*
def reservation = new RouteBuilder().make {
    passengers {
        name 'mrhaki'
        name 'Hubert A. Klein Ikkink'
    }
    from 'Schiphol, Amsterdam'
    to 'Kastrup, Copenhagen'
    retourFlight
}

assert reservation.flight.from == new Airport(name: 'Schiphol', city: 'Amsterdam')
assert reservation.flight.to == new Airport(name: 'Kastrup', city: 'Copenhagen')
assert reservation.passengers.size() == 2
assert reservation.passengers == [new Person(name: 'mrhaki'), new Person(name: 'Hubert A. Klein Ikkink')]
assert reservation.retourFlight

*/

def schema = new RouteBuilder().Category {
    properties {
        name {
            type 'String'
            description 'Foo bar'
        }
    }
}

println JsonOutput.toJson(schema)

// ----------------------------------------------
// Builder implementation and supporting classes.
// ----------------------------------------------
import groovy.json.JsonOutput
import groovy.transform.*
import groovy.util.BuilderSupport

@Canonical
class JsonSchema {
    String resourceName
    def properties = [:]
}

@Canonical
class propertySchema {

}

class RouteBuilder extends BuilderSupport {
    @java.lang.Override
    protected void setParent(java.lang.Object parent, java.lang.Object child) {
        println "setParent: parent: [$parent] child: [$child]"
        // todo: this is not very robust and is working on the assumption the underlying objects are maps.
        // review and refactor
        if (parent.class) {
            parent[child.name] = child.value
        }
        else {
            parent.value[child.name] = child.value
        }
    }

    @java.lang.Override
    protected java.lang.Object createNode(java.lang.Object methodName) {
        println "createNode: [$methodName]"
        if (! getCurrent()) {
            return new JsonSchema(resourceName: methodName)
        }

        return [name: methodName, value: [:]]
    }

    @java.lang.Override
    protected java.lang.Object createNode(java.lang.Object methodName, java.lang.Object objArg) {
        println "createNode: methodName: [$methodName] [$objArg]"
        return [name: methodName, value: objArg]
    }

    @java.lang.Override
    protected java.lang.Object createNode(java.lang.Object methodName, java.util.Map mapArg) {
        println "createNode: [$methodName] [$mapArg]"
        "dummy"
    }

    @java.lang.Override
    protected java.lang.Object createNode(java.lang.Object methodName, java.util.Map mapArg, java.lang.Object objArg) {
        println "createNode: [$methodName] [$mapArg] [$objArg]"
        "dummy"
    }
}