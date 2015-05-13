// Builder syntax to create a schema with properties,
// departing and destination airport and path it a 2-way flight.

def schema = new defBuilder().Comment {
    properties {
        id {
            type 'Integer'
            description 'The comment id'
        }
        name {
            type 'String'
            description 'The comment name'
        }
    }
    required 'id', 'name'
}

//assert schema.properties.size() == 2
//assert schema.properties == [new Property(name: 'mrhaki'), new Property(name: 'Hubert A. Klein Ikkink')]
//assert schema.retourFlight

println schema

// ----------------------------------------------
// Builder implementation and supporting classes.
// ----------------------------------------------
import groovy.transform.*

class defBuilder {
    Map<String, Schema> schemas = [:]

    def methodMissing(String name, args) {
        println "defBuilder.MethodMissing: $name"
        def schema = new Schema()
        schemas[name] = schema
        defBuilder.runClosure(args[0], schema, this)
        schemas
    }

    static def runClosure(Closure cl, Object delegate, Object owner) {
        println "defBuilder.runClosure"
        def code = cl.rehydrate(delegate, owner, owner)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    static def setProperty(Object obj, String property, Object value, boolean throwOnMiss = true) {
        if (obj.hasProperty(property)) {
            obj[property] = value
            return true
        }
        if (throwOnMiss) {
            throw new MissingPropertyException(property, obj.class)
        }
    }
}


@Canonical
class Schema {
    PropertyList propList = new PropertyList()

    List<String> required = []

    def methodMissing(String name, args) {
        println "Schema.methodMissing $name"
        switch (name) {
            case "properties":
                defBuilder.runClosure(args[0], propList, this);
                break;
            case "required":
                this.required = args
        }
    }
}

@Canonical
class PropertyList {
    List<Property> properties = []

    def methodMissing(String name, args) {
        println "PropertyList.methodMissing $name"
        def prop = new Property(name: name)
        defBuilder.runClosure(args[0], prop, this)
        properties << prop
    }
}

@Canonical
class Property { 
    String name
    String type
    String description

    def methodMissing(String name, args) {
        println "Property.methodMissing $name"
        defBuilder.setProperty(this, name, args[0])
    }
}

