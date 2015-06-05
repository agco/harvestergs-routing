package com.agcocorp.harvestergs.routing

class PostResourceBuilder {
    private getAll
    private getById

    PostResourceBuilder(Closure getAll, getById) {
        this.getAll = getAll
        this.getById = getById
    }

    def build() {
        def resource = new Resource(this)
        resource
            .definitions
            .posts {
            attributes {
                title {
                    type 'string'
                }

                body {
                    type 'string'
                    description 'Post contents'
                }

                tags {
                    type 'array'
                    items {
                        type 'string'
                    }
                }
            }
            required 'body', 'title'
        }

        resource
            .paths
            ."/posts" {
            get { req, res ->
                return  getAll()
            }

            post { req, res ->
                return req.data
            }

            "/:id" {
                get    {req, res -> return getById(req.params(':id')) }
                patch  {req, res -> return req.data }
                delete {req, res -> return null }
            }
        }

        return resource
    }
}
