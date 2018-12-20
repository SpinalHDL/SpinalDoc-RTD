=========
SpinalDoc
=========

How to build this documentation
===============================

With pipenv
-----------
Requirements (system)

* make

Requirements (Python 3):

* pipenv (will automatically download all the project requirements from pypi)

Create a virtual environment with pipenv (will use the Pipfile for installing the necessary packages)

.. code:: shell

   pipenv install

then you can build the documentation

.. code:: shell

   pipenv run make html

if you want run ``make`` multiple times, prepone ``pipenv run`` on each command can be annoying,
you can spawn a subshell with

.. code:: shell

   pipenv shell

and then you can use ``make`` the usual way

.. code:: shell

   make html     # for html
   make latex    # for latex
   make latexpdf # for latex (will require latexpdf installed)
   make          # list all the available output format

all the outputs will be in docs folder (for html: docs/html)

without pipenv/virtualenv
-------------------------
Requirements (system):

* make

Requirements (Python 3):

* sphinx
* sphinx-rtd-theme
* sphinxcontrib-wavedrom

After installing the requirements you can run

.. code:: shell

   make html     # for html
   make latex    # for latex
   make latexpdf # for latex (will require latexpdf installed)
   make          # list all the available output format

Continuous Integration(CI)
==========================

This repo use Travis for his CI needs.
If you want have a gh-pages preview on your fork, you need to activate your repo on Travis admin page.
After that you only need add ``GITHUB_TOKEN`` as Environment Variable with your Github personal token (you only need grant repo/public_repo access)
More details here:

* `Defining variables <https://docs.travis-ci.com/user/environment-variables/#defining-variables-in-repository-settings>`_
* `Deploy to gh-pages <https://docs.travis-ci.com/user/deployment/pages/>`_