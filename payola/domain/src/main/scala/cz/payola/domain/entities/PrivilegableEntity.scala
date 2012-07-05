package cz.payola.domain.entities

import scala.collection._
import cz.payola.common.entities.privileges._
import cz.payola.common.entities.plugins.DataSource
import cz.payola.domain.entities.privileges.PublicPrivilege

trait PrivilegableEntity extends cz.payola.common.entities.PrivilegableEntity
{ self: Entity =>

    type PrivilegeType = Privilege[_  <: Entity]

    /**
      * Adds a new privilege to the entity.
      * @param privilege The privilege to add.
      * @param granter The user who is granting the privilege.
      * @throws IllegalArgumentException if the privilege can't be added to the entity.
      */
    def grantPrivilege(privilege: PrivilegeType, granter: User) {
        addRelatedEntity(privilege, privileges, storePrivilege)
    }

    /**
      * Removes the specified privilege from the entity.
      * @param privilege The privilege to be removed.
      * @param granter The user who granted the privilege.
      * @return The removed privilege.
      */
    def removePrivilege(privilege: PrivilegeType, granter: User): Option[PrivilegeType] = {
        removeRelatedEntity(privilege, privileges, discardPrivilege)
    }
}
