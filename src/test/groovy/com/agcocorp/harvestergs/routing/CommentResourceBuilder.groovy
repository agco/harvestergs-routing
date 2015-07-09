package com.agcocorp.harvestergs.routing

class CommentResourceBuilder {
    private getAll
    private getById

    CommentResourceBuilder(Closure getAll, getById) {
        this.getAll = getAll
        this.getById = getById
    }

    def build() {
        def resource = new APIResource('comment')
            .attributes {
                body string.description('Comments contents').required
                author {
                    name string.required.pattern(/.+ .+/)
                    email email.required
                    url uri
                }
                tags arrayOf({
                    name string.required
                    size integer.readOnly
                })
                kind enumOf([ classic, picture, howto ])
            }
            .relationships {
                post posts.description('Owning post').required
            }
            .paths {
                "/comments" {
                    get { req, res ->
                        return this.getAll()
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
                        // todo: remove the need for the 'this' prefix when using closures.
                        get    {req, res -> return this.getById(req.params(':id')) }
                        patch  {req, res -> return req.data }
                        //.document { docs -> docs.operationId = "commentUpdate"; docs }
                        delete {req, res -> return null }
                    }
                }
            }
        return resource
    }
}
