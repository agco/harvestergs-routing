package com.agcocorp.harvestergs.routing

class CommentResourceBuilder {
    private getAll
    private getById

    CommentResourceBuilder(Closure getAll, getById) {
        this.getAll = getAll
        this.getById = getById
    }

    def build() {
        def resource = new Resource(this)
        resource
            .definitions
            .Comment {
            attributes {
                body {
                    type 'string'
                    description 'Comments contents'
                }

                author {
                    type 'object'
                    attributes {
                        name { type 'string'}
                        email { type 'string'}
                        url { type 'string'}
                    }
                    required 'name', 'email'
                }

                tags {
                    type 'array'
                    items {
                        type 'object'
                        attributes {
                            name { type 'string' }
                            size { type 'integer' }
                        }
                        required 'name'
                    }
                }
            }
            required 'body'
        }

        resource
            .paths
            ."/comments" {
            get { req, res ->
                return  getAll()
            }

            post { req, res ->
                return req.data
            }.document { docs ->
                docs.description = "Description for comments.post"
                docs
            }
            .skipAuth
            .skipValidation

            "/:id" {
                get    {req, res -> return getById(req.params(':id')) }
                patch  {req, res -> return req.data }
                //.document { docs -> docs.operationId = "commentUpdate"; docs }
                delete {req, res -> return null }
            }
        }

        return resource
    }
}
