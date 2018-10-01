/*
 * Copyright (C) 2005-2017 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.truelicense.it.v2.xml

import javax.xml.bind._
import net.truelicense.api.{License, LicenseManagementContextBuilder}
import net.truelicense.it.core.ExtraData
import net.truelicense.it.v2.core.V2TestContext
import net.truelicense.v2.core.auth.V2RepositoryModel
import net.truelicense.v2.xml.V2Xml

/** @author Christian Schlichtherle */
trait V2XmlTestContext extends V2TestContext {

  final override def managementContextBuilder: LicenseManagementContextBuilder = {
    V2Xml builder JAXBContext.newInstance(classOf[License], classOf[ExtraData], classOf[V2RepositoryModel])
  }

  override def extraData: AnyRef = {
    val bean = new ExtraData
    bean.setMessage("This is some private extra data!")
    bean
  }
}