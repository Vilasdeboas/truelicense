/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.truelicense.tests.swing

import global.namespace.truelicense.api.{ConsumerLicenseManager, License, LicenseManagementException}
import global.namespace.truelicense.swing.LicenseManagementWizard
import global.namespace.truelicense.tests.core.TestContext
import global.namespace.truelicense.tests.swing.LicenseManagementWizardITLike._
import global.namespace.truelicense.ui.LicenseWizardMessage
import global.namespace.truelicense.ui.LicenseWizardMessage._
import global.namespace.truelicense.ui.wizard.WizardMessage._
import org.netbeans.jemmy._
import org.netbeans.jemmy.operators._
import org.netbeans.jemmy.util._
import org.scalactic.source.Position
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike

import java.awt.{Component, EventQueue, GraphicsEnvironment}
import java.util.Date
import javax.swing._

trait LicenseManagementWizardITLike extends AnyWordSpecLike with BeforeAndAfter {
  this: TestContext =>

  private val laf = UIManager.getLookAndFeel
  private var installed: License = _
  private var manager: ConsumerLicenseManager = _
  private var wizard: LicenseManagementWizard = _
  private var dialog: JDialogOperator = _
  private var cancelButton, backButton, nextButton: AbstractButtonOperator = _

  JemmyProperties.setCurrentOutput(TestOut.getNullOutput) // shut up!

  before {
    new State {
      installed = (vendorManager generateKeyFrom licenseBean saveTo consumerStore).license
      manager = consumerManager
      EventQueue invokeLater (() => {
        UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
        wizard = newLicenseManagementWizard(manager)
        wizard.showModalDialog()
      })
      dialog = new JDialogOperator()
      cancelButton = waitButton(dialog, wizard_cancel)
      backButton = waitButton(dialog, wizard_back)
      nextButton = waitButton(dialog, wizard_next)
      // Defer test execution to allow asynchronous license certificate
      // verification to complete.
      Thread sleep 100
    }
  }

  after {
    cancelButton.doClick()
    dialog.isVisible shouldBe false
    wizard.getReturnCode shouldBe LicenseManagementWizard.CANCEL_RETURN_CODE
    UIManager setLookAndFeel laf
  }

  "A license wizard" when {
    "using a consumer license manager with an installed license key" when {
      "showing" should {
        "be modal" ifNotHeadless {
          dialog.isModal shouldBe true
        }

        "have a title which includes the licensing management subject" ifNotHeadless {
          dialog.getTitle should include(managementContext.subject)
        }

        "have its back button disabled" ifNotHeadless {
          backButton.isEnabled shouldBe false
        }

        "have its next button enabled" ifNotHeadless {
          nextButton.isEnabled shouldBe true
        }

        "have its cancel button enabled" ifNotHeadless {
          cancelButton.isEnabled shouldBe true
        }

        "show its welcome panel and hide the other panels" ifNotHeadless {
          welcomePanel.isVisible shouldBe true
          installPanel.isVisible shouldBe false
          displayPanel.isVisible shouldBe false
          uninstallPanel.isVisible shouldBe false
        }

        "have a visible, non-empty prompt on its welcome panel" ifNotHeadless {
          dialog.getQueueTool.waitEmpty()
          val prompt = waitTextComponent(welcomePanel, welcome_prompt)
          prompt.isVisible shouldBe true
          prompt.getText.isEmpty shouldBe false
        }

        "have both the install and display buttons enabled" ifNotHeadless {
          val installSelector = waitButton(welcomePanel, welcome_install)
          val displaySelector = waitButton(welcomePanel, welcome_display)
          installSelector.isEnabled shouldBe true
          displaySelector.isEnabled shouldBe true
        }

        "switch to the install panel when requested" ifNotHeadless {
          val installSelector = waitButton(welcomePanel, welcome_install)
          installSelector.isVisible shouldBe true
          installSelector.isEnabled shouldBe true
          installSelector.isSelected shouldBe false
          installSelector.doClick()
          nextButton.doClick()
          welcomePanel.isVisible shouldBe false
          installPanel.isVisible shouldBe true
          waitButton(installPanel, install_install).isEnabled shouldBe false
        }

        "switch to the display panel by default and display the license content" ifNotHeadless {
          val displaySelector = waitButton(welcomePanel, welcome_display)
          displaySelector.isVisible shouldBe true
          displaySelector.isEnabled shouldBe true
          displaySelector.isSelected shouldBe true
          nextButton.doClick()
          welcomePanel.isVisible shouldBe false
          displayPanel.isVisible shouldBe true

          def waitText(key: LicenseWizardMessage) =
            waitTextComponent(displayPanel, key).getText

          def format(date: Date) =
            display_dateTimeFormat(managementContext.subject, date)

          waitText(display_holder) shouldBe toString(installed.getHolder)
          waitText(display_subject) shouldBe toString(installed.getSubject)
          waitText(display_consumer) shouldBe (toString(installed.getConsumerType) + " / " + installed.getConsumerAmount)
          waitText(display_notBefore) shouldBe format(installed.getNotBefore)
          waitText(display_notAfter) shouldBe format(installed.getNotAfter)
          waitText(display_issuer) shouldBe toString(installed.getIssuer)
          waitText(display_issued) shouldBe format(installed.getIssued)
          waitText(display_info) shouldBe toString(installed.getInfo)
        }

        "switch to the uninstall panel when requested" ifNotHeadless {
          val uninstallSelector = waitButton(welcomePanel, welcome_uninstall)
          uninstallSelector.isVisible shouldBe true
          uninstallSelector.isEnabled shouldBe true
          uninstallSelector.isSelected shouldBe false
          uninstallSelector.doClick()
          nextButton.doClick()
          welcomePanel.isVisible shouldBe false
          uninstallPanel.isVisible shouldBe true
          waitButton(uninstallPanel, uninstall_uninstall).doClick()
          Thread sleep 100
          intercept[LicenseManagementException](manager.load())
        }
      }
    }
  }

  private implicit class WithText(text: String) {

    def ifNotHeadless(block: => Any)(implicit pos: Position): Unit = {
      if (GraphicsEnvironment.isHeadless) {
        text ignore block
      } else {
        text in block
      }
    }
  }

  private def welcomePanel = waitPanel("WelcomePanel")

  private def installPanel = waitPanel("InstallPanel")

  private def displayPanel = waitPanel("DisplayPanel")

  private def uninstallPanel = waitPanel("UninstallPanel")

  private def waitPanel(name: String) =
    new JComponentOperator(dialog, new ComponentChooser {
      val delegate = new NameComponentChooser(name)

      def checkComponent(comp: Component): Boolean =
        comp match {
          case panel: JPanel => delegate checkComponent panel
          case _ => false
        }

      def getDescription = "Chooses a JPanel by its name."
    })

  private def toString(obj: AnyRef) = {
    if (null ne obj) {
      obj.toString
    } else {
      ""
    }
  }
}

object LicenseManagementWizardITLike {

  private def newLicenseManagementWizard(manager: ConsumerLicenseManager) = {
    val wizard = new LicenseManagementWizard(manager)
    wizard.isUninstallButtonVisible shouldBe false
    wizard setUninstallButtonVisible true
    wizard.isUninstallButtonVisible shouldBe true
    wizard
  }

  private def waitButton(cont: ContainerOperator, key: Enum[_]) =
    new AbstractButtonOperator(cont, new NameComponentChooser(key.name))

  private def waitTextComponent(cont: ContainerOperator, key: Enum[_]) =
    new JTextComponentOperator(cont, new NameComponentChooser(key.name))
}
