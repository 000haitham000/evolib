EvoLib Version 2.0.0 - 8/8/2018
---------------------------------
EvoLib is an open source implementation of NSGA-II, Unified NSGA-III
(U-NSGA-III) and Balanced NSGA-III (B-NSGA-III).
NSGA-II is a well known Evolutionary Multiobjective Optimization (EMO) algorithm
than can handle up to two objectives efficiently.
U-NSGA-III and B-NSGA-III are two Evolutionary Multiobjective Optimization
(EMO) algorithms that can scale up to handle many objectives and down to handle
only one objective. B-NSGA-III builds on top of U-NSGA-III. If you are just
interested in applying a scalable EMO algorithm to solve several dimensional
versions of your optimization problem (each version has a different number
of objectives), U-NSGA-III will be sufficient. On the other hand, if you need 
to achieve an automatic balance between convergence and diversity through 
applying local search and using Karush Kuhn Tucker Proximity Measure (KKTPM)
with EMO, B-NSGA-III should be your choice. Notice that B-NSGA-III uses Matlab
fmincon(...) for its local search by default. Your optimization problem needs to
be coded in Matlab and exported as a JAR using Matlab Java Builder toolbox.

DEPENDENCIES
------------
(1) Tx2Ex: An open source mathematical expressions parser (Apache L2 License)
(2) Apache Commons Lang3 (Apache L2 License)

FEATURES
--------
(1) XML inputs
(2) Layered reference directions
(3) Generate detailed simply formatted outputs
(4) Generate detailed Matlab plotting script
(5) Easily extensible pure object oriented design
(6) Detailed comments provided with source code

GETTING STARTED
---------------
In order to use EvoLib clone the repository to you local git repository. Most
modern IDEs have this functionality readily available.

BUILT USING
-----------
Java SE Development Kit (JDK) 8 or later
(http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

VERSIONING
----------
We use [SemVer](http://semver.org/) for versioning.

AUTHORS
-------
Haitham Seada (http://haithamseada.com/)

LICENSE
-------
This project is licensed under the Apache License Version 2.0
(http://www.apache.org/licenses/LICENSE-2.0). A simple explanation of the
license in layman's terms can be found at
(http://www.apache.org/foundation/license-faq.html#WhatDoesItMEAN).