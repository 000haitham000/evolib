# EvoLib Version 2.1.0 - 10/26/2019
EvoLib is an open source implementation of NSGA-II and Unified NSGA-III
(U-NSGA-III). NSGA-II is a well known Evolutionary Multiobjective Optimization (EMO) algorithm
than can handle up to two objectives efficiently.
U-NSGA-III is an improved version of NSGA-III that enhances the performance of the original
algorithm in single objective optimization problems while maintaining its high
performance in multi and many objective optimization problems.

## DEPENDENCIES
1. Tx2Ex: An open source mathematical expressions parser (Apache L2 License)
2. Apache Commons Lang3 (Apache L2 License)

## FEATURES
1. Single, multi and many objectives 
2. XML inputs
3. Layered reference directions
4. Generate detailed simply formatted outputs
5. Generate detailed Matlab plotting script
6. Easily extensible pure object oriented design
7. Detailed comments provided with source code

## GETTING STARTED
To modify EvoLib directly, clone it to your local file system using Git, then have fun.
A good start point is to run optimization.SampleScript (a sample script for
optimization problems involving only real-valued variables). If you want to use EvoLib
in your own project as a dependency, the recommended approach is to install your local
clone of EvoLib into your local Maven repository and use it from wherever as a Maven dependency.
The latter approach is usually the case if your problem involves custom variables
and consequently needs custom operators. For a detailed example on how to setup and develop
such projects, take a look at EvoLib-Demo (https://github.com/000haitham000/evolib-demo) 

## BUILT USING
Java SE Development Kit (JDK) 8 or later
(http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## VERSIONING
We use [SemVer](http://semver.org/) for versioning.

## AUTHORS
Haitham Seada (http://haithamseada.com/)

## LICENSE
This project is licensed under the Apache License Version 2.0
(http://www.apache.org/licenses/LICENSE-2.0). A simple explanation of the
license in layman's terms can be found at
(http://www.apache.org/foundation/license-faq.html#WhatDoesItMEAN).