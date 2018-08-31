# Spitter

You can spit out up to 69 characters at your timeline. That's all.

More than 69 characters are too lengthy and probably much redundant.

Don't you think so?


## Features

Spitter is designed to be inconvenient to use Twitter as a social networking service, merely suitable to spit a fucking word. You will be free from any annoying ads and anything bothering you on your timeline.


## Usage

At first, you need JRE 8 to run Spitter. If not, install Java before you order pizza.

Then, download the files of this repository and run Spitter on your terminal by spelling the following command:

```
$ spit/out
```

The program will crash as soon as it starts up. Because nothing is given for Spitter to act as a Twitter app. on your account. First thing you have to do is to generate an access token in the Twitter Application Management. I don't want to hassle with that process, so I go ahead with the explanation about Spitter.

After you take some steps, you have four lengthy strings: a pair of *consumer key* &amp; *consumer secret* and a pair of *access token* &amp; *access token secret*. Then, create the key file whose name is simply `key` (without any extention) in the folder `spit` and put your key and token pairs in it as the following four lines:

```
Consumer key
Consumer secret
Access token
Access token secret
```

All right, let's run the program again. You will see nothing. Spitter shows no message as long as it works. If not, something goes wrong, but I don't care about it.

OK, let's spit something, for example, type `114514` and hit enter. Then, watch your timeline on a browser or some Twitter client, you will see the number `114514` tweeted by your account. By the way, the specific number `114514` is a Japanese much frequently used internet slang that means "You're welcome to fuck my ass".

It's a trifle matter that Spitter ignores any responses from Twitter and returns the control before your tweet is successfully put on your timeline. If you beat enter one after another, the order of the tweets may get into a mess.

When you want to quit the program, hit enter without any input.


## Some useful features

None.


----

All of sources, configuration files, README.md and Spitter's JAR is published under WTFPL. Each of other JAR is published under its original licence.

Copyright &copy; 2018 azapen6

This work is free. You can redistribute it and/or modify it under the
terms of the Do What The Fuck You Want To Public License, Version 2,
as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
