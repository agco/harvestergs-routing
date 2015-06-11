package Helpers
import groovy.json.JsonOutput

/**
 * Created by stefanini on 07/05/15.
 */
class ValidationSchemaHelper {
    def static SchemaError(params){
        def obj =[errors:[]];

        for(item in params.messages)
        {
            obj.errors.add([
                id:UUID.randomUUID(),
                status:params.status?:500,
                title:params.title?:'Internal Server Error',
                detail: item.map.message._value,
                paths:[
                    level: item.map.level._value,
                    schema:item.map.schema._children.pointer._value,
                    instance:item.map.instance._children.pointer._value,
                    domain:item.map.domain._value,
                    keyword:item.map.keyword._value,
                    message:item.map.message._value,
                    found:item.map.found?item.map.found._value:null,
                    expected:item.map.expected?item.map.expected._children.toString():null
                ]
            ])
        }
        JsonOutput.toJson(obj)
    }
}
