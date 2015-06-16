package com.agcocorp.harvestergs.routing

class CommentResourceBuilder {
    private getAll
    private getById

    CommentResourceBuilder(Closure getAll, getById) {
        this.getAll = getAll
        this.getById = getById
    }

    def build() {
        def resource = new ResourceDefinition('comment')
            .attributes {
                body string.description('Comments contents').required
                author {
                    name string.required
                    email string.required
                    url string
                }
                tags arrayOf({
                    name string.required
                    size integer
                })
            }
            .relationships {
                post posts
            }
            .paths {
                "/comments" {
                    get { req, res ->
                        return getAll()
                    }

                    post { req, res ->
                        return req.data
                    }.document { docs ->
                        docs.description = "Custom description for comments.post"
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
            }

        return resource
    }
}
