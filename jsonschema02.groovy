// Builder syntax to create a schema with properties,
// departing and destination airport and path it a 2-way flight.
def schema = new SchemaBuilder().path {
    properties {
        //name 'mrhaki'
        //name 'Hubert A. Klein Ikkink'
        foobar { }
    }
    from 'Schiphol, Amsterdam'
    to 'Kastrup, Copenhagen'
    retourFlight
}

assert schema.flight.from == new Airport(name: 'Schiphol', city: 'Amsterdam')
assert schema.flight.to == new Airport(name: 'Kastrup', city: 'Copenhagen')
//assert schema.properties.size() == 2
//assert schema.properties == [new Property(name: 'mrhaki'), new Property(name: 'Hubert A. Klein Ikkink')]
assert schema.retourFlight

println schema

// ----------------------------------------------
// Builder implementation and supporting classes.
// ----------------------------------------------
import groovy.transform.*

@Canonical
class Schema {
    Flight flight = new Flight()
    List<Property> properties = []
    Boolean retourFlight = false
}

@Canonical
class Property { 
    String name
    String type
    String description 
}

@Canonical
class Airport { String name, city }

@Canonical
class Flight { Airport from, to }

// The actual builder for schemas.
class SchemaBuilder {
    // Schema to path and fill with property values.
    Schema schema

    private Boolean propertiesMode = false

    Schema path(Closure definition) {
        println "path(Closure definition: )"
        schema = new Schema()

        runClosure definition

        schema
    }

    void properties(Closure names) {
        println "properties(Closure names: )"
        propertiesMode = true

        runClosure names

        propertiesMode = false
    }

    void name(String personName) {
        println "name(String personName: $personName)"
        if (propertiesMode) {
            schema.properties << new Property(name: personName)
        } else {
            throw new IllegalStateException("name() only allowed in properties context.")
        }
    }

    def methodMissing(String name, arguments) {
        println "methodMissing(String name: $name, arguments: $arguments)"
        // to and from method calls will set flight properties
        // with Airport objects.
        if (name in ['to', 'from']) {
            def airport = arguments[0].split(',')
            def airPortname = airport[0].trim()
            def city = airport[1].trim()
            schema.flight."$name" = new Airport(name: airPortname, city: city)
        }
    }

    def propertyMissing(String name) {
        println "propertyMissing(String name: $name)"
        // Property access of retourFlight sets schema
        // property retourFlight to true.
        if (name == 'retourFlight') {
            schema.retourFlight = true
        }
    }

    private runClosure(Closure runClosure) {
        println "runClosure(Closure runClosure: )"
        // Create clone of closure for threading access.
        Closure runClone = runClosure.clone()

        // Set delegate of closure to this builder.
        runClone.delegate = this

        // And only use this builder as the closure delegate.
        runClone.resolveStrategy = Closure.DELEGATE_ONLY

        // Run closure code.
        runClone()
    }

}