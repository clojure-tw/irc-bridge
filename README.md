# irc-bridge
[![Build Status](https://travis-ci.org/clojure-tw/irc-bridge.svg?branch=master)](https://travis-ci.org/clojure-tw/irc-bridge)
[![License](http://img.shields.io/badge/license-Eclipse-blue.svg?style=flat)](https://www.eclipse.org/legal/epl-v10.html)

A simple irc bot to bridge message from gitter/irc, this bot is designed for
Clojure Taiwan Community.

Since I didn't have the token for `clojuretwbot`, the slack integration is put
on hold.

## Usage

You need to modify the config then run

    lein run -- config.edn

## Configuration

The configuration is in edn format, you can see `config-example.edn` which has
following config:
```clojure
    {:gitter {:rom-id "xaxc876119x6c7xx89xxb3xxe"
              :api-key "xxx0axxe2xxbxxf2ba2xxx7ffxxxa278xxx8f47"}
     :irc    {:server "irc.freenode.net"
              :port 6667
              :channel "#test-bot"
              :debug true ; enable this to see raw-stdout
             }}
```
## License

Copyright © 2015 Yen-Chin, Lee <<coldnew.tw@gmail.com>>

Distributed under the Eclipse Public License either version 1.0 or any later version.
