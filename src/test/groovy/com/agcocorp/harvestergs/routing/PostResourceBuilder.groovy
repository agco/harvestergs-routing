package com.agcocorp.harvestergs.routing

class PostResourceBuilder {
    private getAll
    private getById

    PostResourceBuilder(Closure getAll, getById) {
        this.getAll = getAll
        this.getById = getById
    }

    def build() {
        def resource = new ResourceDefinition('post')
            .attributes {
                id string.description('url-encoded version of the tile, for easy permalinks')
                title string.required
                body string.description('Post contents').required
                tags arrayOf(string)
                createdOn datetime
                published bool
                coordinates {
                    latitude number
                    longitude number
                }
            }
            .paths {
                "/posts" {
                    get { req, res ->
                        return "Hello World!"
                    }

                    post { req, res ->
                        return req.data
                    }.skipValidation.skipAuth

                    "/:id" {
                        get { req, res -> return getById(req.params(':id')) }
                        patch { req, res -> return req.data }
                        delete { req, res -> return null }
                    }
                }
            }

        return resource
    }
}
