package com.agcocorp.harvestergs.routing

class ErrorResourceBuilder {
    def build() {
        def resource = new ResourceDefinition('error')
            .attributes {
                id string
                message string
            }
            .paths {
                "/errors" {
                    get { req, res ->
                        return "Hello World!"
                    }

                    post { req, res ->
                        throw new NullPointerException()
                    }.skipValidation.skipAuth

                    "/:id" {
                        get { req, res -> return null }
                        patch { req, res -> return null }
                        delete { req, res -> return null }
                    }
                }
            }

        return resource
    }
}
