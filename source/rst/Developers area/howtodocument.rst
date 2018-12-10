How to HACK this documentation
==============================
If you want to add your page to this documentation you need to add
your source file in the appropriate section.
I opted to create a structure that resample the various section of the documentation,
this is not strictly necessary, but for clarity sake, highly encourage.

This documentation uses a recursive index tree: every folder have a special ``index.rst`` files
that tell sphinx witch file, and in what order put it in the documentation tree.

Title convention
----------------
Sphinx is very smart, the document structure is deduced from how you use
non alphanumerical characters (like:  ``= - ` : ' " ~ ^ _ * + # < >``), you only need to be consistent.
Still, for clarity sakes we use this progression:

 * ``=`` over and underline for section titles
 * ``=`` underline for titles
 * ``-`` underline for paragraph
 * ``^`` for subparagraph

Wavedrom integration
--------------------
This documentation makes use of the ``sphinxcontrib-wavedrom`` plugin,
So you can specify a timing diagram, or a register description with the WaweJSON_ syntax like so:

.. code:: javascript

   .. wavedrom::

      { "signal": [
         { "name": "pclk", "wave": 'p.......' },
         { "name": "Pclk", "wave": 'P.......' },
         { "name": "nclk", "wave": 'n.......' },
         { "name": "Nclk", "wave": 'N.......' },
         {},
         { "name": 'clk0', "wave": 'phnlPHNL' },
         { "name": 'clk1', "wave": 'xhlhLHl.' },
         { "name": 'clk2', "wave": 'hpHplnLn' },
         { "name": 'clk3', "wave": 'nhNhplPl' },
         { "name": 'clk4', "wave": 'xlh.L.Hx' },
      ]}

and you get:

.. wavedrom::

   { "signal": [
      { "name": "pclk", "wave": 'p.......' },
      { "name": "Pclk", "wave": 'P.......' },
      { "name": "nclk", "wave": 'n.......' },
      { "name": "Nclk", "wave": 'N.......' },
      {},
      { "name": 'clk0', "wave": 'phnlPHNL' },
      { "name": 'clk1', "wave": 'xhlhLHl.' },
      { "name": 'clk2', "wave": 'hpHplnLn' },
      { "name": 'clk3', "wave": 'nhNhplPl' },
      { "name": 'clk4', "wave": 'xlh.L.Hx' },
   ]}

.. note::
   if you want the Wavedrom diagram to be present in the pdf export, you need to use the "non relaxed" JSON dialect.
   long story short, no javascript code and use ``"`` arround key value (Eg. ``"name"``).

you can describe register mapping with the same syntax:

.. code:: javascript

   {"reg":[
     {"bits": 8, "name": "things"},
     {"bits": 2, "name": "stuff" },
     {"bits": 6},
    ],
    "config": { "bits":16,"lanes":1 }
    }

.. wavedrom::

   {"reg":[
      {"bits": 8, "name": "things"},
      {"bits": 2, "name": "stuff" },
      {"bits": 6},
     ],
     "config": { "bits":16,"lanes":1 }
   }
New section
-----------
if you want to add a new  section you need to specify in the top index, the index file of the new section.
I suggest to name the folder like the section name, but is not required;
Sphinx will take the name of the section from the title of the index file.

example
^^^^^^^
I want to document the new feature in SpinalHDL, and I want to create a section for it; let's call it ``Cheese``

So I need to create a folder named ``Cheese`` (name is not important), and in it create a index file like:

.. code:: ReST

   ======
   Cheese
   ======

   .. toctree::
   :glob:

   introduction
   *

.. note::
   The ``.. toctree::`` directive accept some parameters, in this case ``:glob:``
   makes so you can use the ``*`` to include all the remaining files.

.. note::
   The file path is relative to the index file, if you want to specify the absolute path, you need to prepend ``/``

.. note::
   ``introduction.rst`` will be always the first on the list because it's specified in the index file.
   Other files will be included in alphabetical order.

Now I can add the ``introduction.rst`` and other files like ``cheddar.rst``, ``stilton.rst``, etc.

The only thing remaining to do is to add cheese to the top index file like so:


.. code:: ReST

   Welcome to SpinalHDL's documentation!
   =====================================

   .. toctree::
      :maxdepth: 2
      :titlesonly:

      rst/About SpinalHDL/index
      rst/Getting Started/index
      rst/Data types/index
      rst/Structuring/index
      rst/Semantic/index
      rst/Sequential logic/index
      rst/Design errors/index
      rst/Other language features/index
      rst/Libraries/index
      rst/Simulation/index
      rst/Examples/index
      rst/Legacy/index
      rst/Developers area/index
      rst/Cheese/index

that's it, now you can add all you want in cheese and the pages will show up in the documentation.


.. _WaveJSON: https://github.com/wavedrom/wavedrom/wiki/WaveJSON