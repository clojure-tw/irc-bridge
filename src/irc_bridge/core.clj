(ns irc-bridge.core
  (:require [clojure.edn :as edn]
            [taoensso.timbre :as timbre :refer (debug info warn error fatal)]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]
            ;; clean
            [irc-bridge.gitter :as gitter]
            [irc-bridge.irc    :as irc])
  (:gen-class))

(defn parse-config
  "Parse irc-bot config."
  [url]
  (edn/read-string (slurp url)))

(defn irc-events-dispatcher
  "irc -> gitter, slack"
  [{:keys [gitter irc] :as config}]
  (go-loop [{:keys [nickname message type]} (<! irc/channel)]
    (when-not (re-find #"gitterbot" nickname)
      ;; TODO: clean code
      (if (= type :action)
        (gitter/send-message! gitter (str "`ircbot` * " nickname " " message))
        (gitter/send-message! gitter (str "`ircbot` <" nickname ">: " message))
        ))
    (recur (<! irc/channel))))

;; TODO:
;; 1. multiline -> send to irc many times
;; 2. code block -> send to pastebin like system
(defn gitter-events-dispatcher
  "gitter -> irc, slack"
  [{:keys [gitter irc] :as config}]
  (go-loop [{:keys [nickname message]} (<! gitter/channel)]
    (when-not (re-find #"ircbot" message)
      (irc/send-message! irc (str "<" nickname ">: " message)))
    (recur (<! gitter/channel))))

(defn start-irc-bridge
  [{:keys [gitter irc] :as config}]
  ;; start listener in thread
  (future (gitter/event-listener gitter))
  (future (irc/event-listener irc))
  ;; start dispatcher for handling events
  (irc-events-dispatcher    config)
  (gitter-events-dispatcher config))

(defn -main [& args]
  (let [arg1 (nth args 0)]
    (if arg1
      (-> (parse-config arg1) (start-irc-bridge))
      (println "ERROR: Please specify config file."))))
