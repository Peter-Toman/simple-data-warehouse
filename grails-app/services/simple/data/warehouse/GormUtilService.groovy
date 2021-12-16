package simple.data.warehouse

import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

class GormUtilService {

    MappingContext grailsDomainClassMappingContext

    List<String> getAllPersistentPropertyNames(Class clazz) {
        return getAllPersistentProperties(clazz).collect {
            it.name
        }
    }

    List<PersistentProperty> getAllPersistentProperties(Class clazz) {
        PersistentEntity persistentEntity = grailsDomainClassMappingContext.getPersistentEntity(clazz.name)
        return persistentEntity.getPersistentProperties()
    }
}
