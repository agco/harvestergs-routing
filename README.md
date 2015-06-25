harvestergs-routing
===================
*A DSL for defining [JSON API](http://jsonapi.org/)-compliant APIs*

how to use it
-------------

### DSL

The DSL follows verbiage from [JSON API](http://jsonapi.org), [JSON Schema](http://json-schema.org/)  specs wherever possible (keywords, structure, etc) for easier adoption.

When defining an API resource, the DSL expects up to three sections to be provided:
* *attributes:* defines the properties of the resource you are mapping. All
properties (except relationships) and their validation go here;
* *relationships:* define relationships to other resources. These will be
mapped to properties within the payload for validation and documentation
purposes, but receives some special handling by the loaders. This split also
makes the structure closer to JSON API definitions;
* *paths:* defines what actions (and under which endpoints) are defined for a
given resource. Paths may be nested (eg: "/:id" within "/paths"). Also maps
those actions to the closures you provide;

An example:
```
// defining a resource named "post"
def resource = new APIResource('post')
			// the 'attributes' section contains all properties of a post resource
            .attributes {
            	// all native json schema types are supported. 
            	// validation is specified in-loco
                title string.required
                // you can also provide descriptions for attributes
                // and relationships
                body string.description('Post contents').required
                // arrays are also supported. you can also define complex
                // types as array items
                tags arrayOf(string)
            }.relationships {
            	// defining a relationship to a 'person' resource, named
            	// 'author'
                author person
            }
            // in this section you define which routes exist and the closures
            // that define them
            .paths {
                "/posts" {
                	// defining the handling for GET verbs on the '/posts' endpoint
                    get { req, res ->
                    	// the req, res arguments come from java spark
                        return this.getAll()
                    }
                    post { req, res ->
                        return req.data
                    }.document { docs ->
                    	// out-of-box, the swagger loader provides templates 
                    	// for documentation. You can override that test by 
                    	// using the 'document' command. Please remember to
                    	// return the docs object
                        docs.description = "Custom description for posts.post"
                        docs
                    }
                    // nested path. in this example, this maps to "/posts/:id"
                    // the ":id" format comes from spark as well
                    "/:id" {
                        get { req, res -> return this.getById(req.params(':id')) }
                        patch { req, res -> return req.data }
                        delete { req, res -> return null }
                    }
                }
            }
```


*Please notice:* this DSL is a) still not feature complete and b) has the (small) potential for breaking changes. Version 1.0 will settle on a stable DSL and set of features.

### Loaders

Currently, the library contains two loaders:
* *SparkLoader:* uses [Spark](http://sparkjava.com/) to load the routes and
map them to handling closures. Elements in the DSL (such as the ":<key>" url
routing and the ```req, res``` arguments);
* *SwaggerLoader:* creates [Swagger](http://swagger.io/) documentation, using
a number of templates.

*Please notice:* more loaders (especially for the endpoint mapping) may be created in the future. Points to keep in mind are: a) this will probably cause the creation of specialized harvestergs libraries and b) the expectation is that the introduction of a loader will *not* cause breaking changes to the DSL.
