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
                body string.description('Comments contents').required.maxLength(4000).minLength(1)
                author {
                    name string.required.pattern(/.+ .+/)
                    email email.required
                    url uri
                }
                tags arrayOf({
                    name string.required.maxLength(10)
                    size integer.readOnly
                })
                coordinates {
                    latitude number.minimum(-180.0).maximum(180.0)
                    longitude number.minimum(-180.0).maximum(180.0)
                }
                kind enumOf([ classic, picture, howto ])
            }
            .relationships {
                post posts.description('Owning post').required
            }
            .paths {
                "/comments" {
                    get { req, res ->
                        return this.getAll()
                    }.skipAuth

                    post { req, res ->
                        return req.data
                    }.document { docs ->
                        docs.description = "Custom description for comments.post"
                        docs
                    }

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
