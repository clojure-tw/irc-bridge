# irc-bridge
[![Build Status](https://travis-ci.org/clojure-tw/irc-bridge.svg?branch=master)](https://travis-ci.org/clojure-tw/irc-bridge)
[![License](http://img.shields.io/badge/license-Eclipse-blue.svg?style=flat)](https://www.eclipse.org/legal/epl-v10.html)

A simple irc bot to bridge message from gitter/irc, this bot is designed for
Clojure Taiwan Community.

Since I didn't have the token for `clojuretwbot`, the slack integration is put
on hold.

這是一個非常簡單的 IRC 機器人，用來同步 `clojure-tw` 群組的 `gitter` 訊息以及在 freenode.net #clojure.tw 的 `IRC` 訊息，最初設計時有考量加上 slack 支援，但是由於該功能被 clojurians 的管理者關掉，因此這個機器人無法和 slack 上的 `clojure-taiwan` 頻道進行同步。

## Configuration

The configuration is in edn format, you can see `config-example.edn` which has
following config:

設定檔採用了 `edn` 格式，你可以參考 `config-example.edn` 這個檔案，基本上你只需要修改 `gitter` 的資訊以及 `irc` 的頻道名稱即可

```clojure
    {:gitter {:rom-id "xaxc876119x6c7xx89xxb3xxe"
              :api-key "xxx0axxe2xxbxxf2ba2xxx7ffxxxa278xxx8f47"}
     :irc    {:server "irc.freenode.net"
              :port 6667
              :channel "#test-bot"
              :debug true ; enable this to see raw-stdout
             }}
```

## Usage

You need to modify the config then run

    lein run -- config.edn

## License

Copyright © 2015 Yen-Chin, Lee <<coldnew.tw@gmail.com>>

Distributed under the Eclipse Public License either version 1.0 or any later version.
