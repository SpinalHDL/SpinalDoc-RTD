How to use a local SpinalHDL clone as project dependency
========================================================

The default way to use SpinalHDL is by using the released version of it as is
automatically downloaded by sbt or mill. Living on the cutting edge, you might
want to use the upstream and non released ``dev`` branch from git directly. Be
it just because you want to use a new shiny feature or because you want to test
your own extension to Spinal (please consider upstreaming it by opening a PR)
within a project.

As an example of where this is used you can also refer to `VexiiRiscv`_.


Create local git clone of SpinalHDL
-----------------------------------

.. code-block:: sh

   cd /somewhere
   git clone --depth 1 -b dev https://github.com/SpinalHDL/SpinalHDL.git

In the command above you can replace ``dev`` by the name of the branch you want
to checkout. ``--depth 1`` prevents from downloading the repository history.


Configure buildsystem
---------------------

Depending on which tool you use, instruct either sbt or mill to load the local
git folder as dependency:


Configure sbt (update ``build.sbt``)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: scala

   ThisBuild / version := "1.0"              // change as needed
   ThisBuild / scalaVersion := "2.12.18"     // change as needed
   ThisBuild / organization := "org.example" // change as needed

   val spinalRoot = file("/somewhere/SpinalHDL")
   lazy val spinalIdslPlugin = ProjectRef(spinalRoot, "idslplugin")
   lazy val spinalSim = ProjectRef(spinalRoot, "sim")
   lazy val spinalCore = ProjectRef(spinalRoot, "core")
   lazy val spinalLib = ProjectRef(spinalRoot, "lib")

   lazy val projectname = (project in file("."))
   .settings(
      Compile / scalaSource := baseDirectory.value / "hw" / "spinal",
   ).dependsOn(spinalIdslPlugin, spinalSim, spinalCore, spinalLib)

   scalacOptions += (spinalIdslPlugin / Compile / packageBin / artifactPath).map { file =>
     s"-Xplugin:${file.getAbsolutePath}"
   }.value

   fork := true


Configure mill (update ``build.sc``)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: scala

   import mill._, scalalib._

   import $file.^.SpinalHDL.build
   import ^.SpinalHDL.build.{core => spinalCore}
   import ^.SpinalHDL.build.{lib => spinalLib}
   import ^.SpinalHDL.build.{idslplugin => spinalIdslplugin}

   val spinalVers = "1.10.2a"
   val scalaVers = "2.12.18"

   object projectname extends RootModule with SbtModule {
      def scalaVersion = scalaVers
      def sources = T.sources(
         this.millSourcePath / "hw" / "spinal"
      )

      def idslplugin = spinalIdslplugin(scalaVers)
      def moduleDeps = Seq(
         spinalCore(scalaVers),
         spinalLib(scalaVers),
         idslplugin
      )
      def scalacOptions = super.scalacOptions() ++ idslplugin.pluginOptions()
   }


Note the line ``import $file.^.SpinalHDL.build``. It is using ammonite REPL
magic ``$file`` to look up the ``build.sc`` of SpinalHDL. (The ``^`` moves up
one directory from the current.) This is assuming the following directory
structure:

.. code-block:: scala

   /somewhere
   |-SpinalHDL   # <-- cloned spinal git
   | |-build.sc
   |-projectname
   | |-build.sc  # <-- your project, mill is ran from here


Done
----

Note the addition to ``scalacOptions``. Without it, compiling any Spinal project
might produce countless ``SCOPE VIOLATION`` or ``HIERARCHY VIOLATION`` errors
because the ``idslplugin`` of spinal is not actually invoked.

After the changes, the next compilation of your project will take a considerable
amount of time (~2 minutes). This is only for the first compile. After this,
your project should compile as usual.


.. _VexiiRiscv: https://github.com/SpinalHDL/VexiiRiscv/blob/dev/build.sbt
