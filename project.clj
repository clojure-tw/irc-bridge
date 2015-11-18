(defproject irc-bridge "0.1.0-SNAPSHOT"
  :description "clojure-tw's irc-bot to redirect message to slack/irc/gitter."
  :url "https://github.com/coldnew/irc-bridge"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-http "2.0.0"]
                 [com.taoensso/timbre "4.1.4"]
                 [org.clojure/data.json "0.2.6"]
                 [irclj "0.5.0-alpha4"]]
  :main irc-bridge.core)
