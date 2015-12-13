(defproject irc-bridge "0.1.0-SNAPSHOT"
  :description "clojure-tw's irc-bot to redirect message to slack/irc/gitter."
  :url "https://github.com/coldnew/irc-bridge"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [clj-http "2.0.0"]
                 [com.taoensso/timbre "4.1.4"]
                 [org.clojure/data.json "0.2.6"]
                 [irclj "0.5.0-alpha4"]
                 [cheshire "5.5.0"]
                 [http.async.client "0.5.2"]]
  :main irc-bridge.core
  :aot [irc-bridge.core])
