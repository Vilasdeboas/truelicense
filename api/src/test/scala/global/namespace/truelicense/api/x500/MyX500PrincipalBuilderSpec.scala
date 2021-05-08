/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.truelicense.api.x500

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import javax.security.auth.x500.X500Principal

class MyX500PrincipalBuilderSpec extends AnyWordSpec {

  private val expected = {
    import scala.jdk.CollectionConverters._
    new X500Principal(
      "C=C,CN=CN,DC=DC,DNQ=DNQ,DNQUALIFIER=DNQUALIFIER,EMAILADDRESS=EMAILADDRESS,FOO=FOO,GENERATION=GENERATION,GIVENNAME=GIVENNAME,INITIALS=INITIALS,L=L,O=O,OU=OU,SERIALNUMBER=SERIALNUMBER,ST=ST,STREET=STREET,SURNAME=SURNAME,T=T,UID=UID",
      Map("FOO" -> "1.2.3.4.5.6.7.8.9").asJava)
  }

  "Using my X.500 principal builder class" should {
    "be compilable without adding type casts" in {
      new MyX500PrincipalBuilder()
        .addC("C")
        .addCN("CN")
        .addDC("DC")
        .addDNQ("DNQ")
        .addDNQUALIFIER("DNQUALIFIER")
        .addEMAILADDRESS("EMAILADDRESS")
        .addFOO("FOO")
        .addGENERATION("GENERATION")
        .addGIVENNAME("GIVENNAME")
        .addINITIALS("INITIALS")
        .addL("L")
        .addO("O")
        .addOU("OU")
        .addSERIALNUMBER("SERIALNUMBER")
        .addST("ST")
        .addSTREET("STREET")
        .addSURNAME("SURNAME")
        .addT("T")
        .addUID("UID")
        .build shouldBe expected
    }
  }
}
