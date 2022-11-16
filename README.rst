=========
SpinalDoc
=========

This is the documentation repository for
`SpinalHDL <https://github.com/SpinalHDL/SpinalHDL>`_.

It is published on
`spinalhdl.github.io/SpinalDoc-RTD <https://spinalhdl.github.io/SpinalDoc-RTD/master/index.html>`_.

You can also find the API documentation on
`spinalhdl.github.io/SpinalHDL <https://spinalhdl.github.io/SpinalHDL/dev/spinal/index.html>`_.


How to build this documentation
===============================

With venv
---------
Requirements (system)

* make
* git


Create a virtual environment with pipenv (will use the Pipfile for installing the necessary packages)

.. code:: shell

   python3 -m venv .venv

then you can activate the virtual enviroment (in bash) and install the dependencies

.. code:: shell

   source .venv/bin/activate
   pip install -r requirements.txt

and then you can use ``make`` the usual way

.. code:: shell

   make html     # for html
   make latex    # for latex
   make latexpdf # for latex (will require latexpdf installed)
   make          # list all the available output format

all the outputs will be in docs folder (for html: docs/html)

With Docker
-----------

Requirements (system):

* docker
* git

To create the custom docker image (with python and its dependencies):

.. code:: shell

   docker build -t spinaldoc-rtd .

Then to build the docs:

.. code:: shell

   docker run -it --rm -v $PWD:/docs spinaldoc-rtd

You can still run custom commands in the docker, for instance to clean:

.. code:: shell

   docker run -it --rm -v $PWD:/docs spinaldoc-rtd make clean

Native
------
Requirements (system):

* make
* git

Requirements (Python 3):

* sphinx
* sphinx-rtd-theme
* sphinxcontrib-wavedrom
* sphinx-multiversion

After installing the requirements you can run

.. code:: shell

   make html     # for html
   make latex    # for latex
   make latexpdf # for latex (will require latexpdf installed)
   make          # list all the available output format

you can create build multiple version of the doc via

.. code:: shell

   sphinx-multiversion source docs/html

in the docs/html there will be a folder with the builded doc for each branch and tag

Deploying the generated doc by hands
----------------------------------------

.. code:: shell

   git clone https://github.com/SpinalHDL/SpinalDoc-RTD.git --branch gh-pages deploy_tmp
   rm -rf deploy_tmp/*
   cp -r docs/html/* deploy_tmp/
   cd deploy_tmp/
   git add --all
   git commit -am "deploy"
   git push
   cd ..
   rm -rf deploy_tmp


Continuous Integration(CI)
==========================

This repo use Travis for his CI needs.
If you want have a gh-pages preview on your fork, you need to activate your repo on Travis admin page.
After that you only need add ``GITHUB_TOKEN`` as Environment Variable with your Github personal token (you only need grant repo/public_repo access)
More details here:

* `Defining variables <https://docs.travis-ci.com/user/environment-variables/#defining-variables-in-repository-settings>`_
* `Deploy to gh-pages <https://docs.travis-ci.com/user/deployment/pages/>`_


