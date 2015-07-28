package com.agcocorp.harvestergs.routing

class DummyResourceBuilder {
    def build() {
        def resource = new ResourceDefinition('dummy')
            .attributes {
                reason string.description('This is a dummy resource, with no paths.')
            }

        return resource
    }
}
