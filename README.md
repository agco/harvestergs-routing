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
```groovy
    // defining an API
    def api = new ApiDefinition()
        // the resources property is where all API resources are defined
        .resources {
            // creating a 'post' resource. The attributed property
            // defines the data contract for the resource, as per JSONAPI
            post.attributes {
                // defining a 'title' property. It is a string with a maximum
                // 100 chars length and is mandatory
                title string.required.maxLength(100)
                body string.required.maxLength(4000)
                status enumOf([ draft, published, howto ])
                tags arrayOf(string)
            // relationships (which establish links to other resources) are
            // defined in this special section
            }.relationships {
                // establishing a relationship to a person resource. It is
                // mandatory and is called 'person'
                author person.required
            // the paths section is where API operations are defined
            }.paths {
                // all paths must start with a '/' (slash)
                "/posts" {
                    // this tells the loader that clients can get and post against the
                    // /posts endpoint
                    // the req and res arguments come from java spark
                    get { req, res -> return "A post list should be returned here." }
                    post { req, res -> return "This closure should implement the creation of posts." }
                    // all closures in the section below correspond to http verbs at the
                    // /posts/:id endpoint
                    "/:id" {
                        get { req, res -> return "A specific post should be returned here." }
                        // please notice that, as per JSON API specs, PATCH is used for updates
                        patch { req, res -> return "Implement here the code to update posts." }
                        delete { req, res -> return "Implement here the code to delete a posts." }
                    }
                }
            }

            // defining another resource, called 'comment'. Pretty barebones,
            // in order to keep this example brief
            comment.attributes {
                body string
            }.relationships {
                author person
            }

            // third resource definition
            person.attributes {
                name string.required
                email email.required
            }
        }
        // you can globally set an auth closure for your api
        .auth { req, res ->
            if (!req.headers('Authorization')) {
                // 'error' has helper methods for sending error status codes
                error.unauthorized()
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
