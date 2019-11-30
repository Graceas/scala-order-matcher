# scala-order-matcher

## 1. Setup the environment

- ### Installing Java

```
sudo apt-get update
sudo apt-get install default-jre default-jdk
```

- ### Installing SBT

Please follow the SBT installation instructions depending on your operating system ([Mac](https://www.scala-sbt.org/1.0/docs/Installing-sbt-on-Mac.html), [Windows](https://www.scala-sbt.org/1.0/docs/Installing-sbt-on-Windows.html), [Linux](https://www.scala-sbt.org/1.0/docs/Installing-sbt-on-Linux.html)).

## 2. Obtaining Source Codes

Cloning with HTTPS URLs (recommended)
```
git clone https://github.com/Graceas/scala-order-matcher.git
cd scala-order-matcher
```
or cloning with SSH URLs
```
git clone git@github.com:Graceas/scala-order-matcher.git
cd scala-order-matcher
```

## 3. Compilation and unit tests

For run a simulation:

```
sbt run
```

For run a tests:

```
sbt test
```

## 3. Results

Please check the 'result.txt' file after execute sbt run

If add all orders and process (`sbt "run async"`):

```
C1	1309	103	240	760	320
C2	4030	396	121	950	560
C3	1304	125	4	0	0
C4	3352	209	533	480	950
C5	673	71	5	400	100
C6	6519	438	317	100	0
C7	443	46	2	790	0
C8	3747	372	192	0	0
C9	5093	380	186	0	280
```

If add single order and process (`sbt run`):

```
C1	2324	91	138	733	353
C2	1820	344	525	964	565
C3	609	197	4	0	0
C4	10133	59	96	439	492
C5	170	123	5	401	99
C6	7067	203	161	197	285
C7	114	61	28	745	121
C8	1827	526	265	1	1
C9	2406	536	378	0	294
```