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

If add all orders and process:

```
C1	1196	103	240	760	320
C2	3664	396	121	950	560
C3	1304	125	4	0	0
C4	2880	209	533	480	950
C5	673	71	5	400	100
C6	5523	438	317	100	0
C7	412	46	2	790	0
C8	3689	372	192	0	0
C9	4902	380	186	0	280
```

If add single order and process:

```
C1	1557	91	140	748	387
C2	1794	344	522	934	529
C3	609	197	4	0	0
C4	8711	59	97	449	581
C5	177	123	5	391	100
C6	6440	203	173	197	290
C7	101	61	29	760	56
C8	1798	526	258	0	0
C9	2239	536	372	1	267
```