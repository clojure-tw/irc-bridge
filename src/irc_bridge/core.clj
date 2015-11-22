(ns irc-bridge.core
  (:require [clojure.edn :as edn]
            [taoensso.timbre :as timbre :refer (debug info warn error fatal)]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]
            ;; modules
            [irc-bridge.gitter :as gitter]
            [irc-bridge.irc    :as irc]
            [irc-bridge.converter :refer [irc->gitter gitter->irc]])
  (:gen-class))

(defn parse-config
  "Parse irc-bot config."
  [url]
  (edn/read-string (slurp url)))

(defn irc-events-dispatcher
  "irc -> gitter, slack"
  [{:keys [gitter irc] :as config}]
  (go-loop [{:keys [nickname message type] :as ch} (<! irc/channel)]
    (when-not (re-find #"gitterbot" nickname)
      (gitter/send-message! (irc->gitter ch)))
    (recur (<! irc/channel))))

;; TODO:
;; 2. code block -> send to pastebin like system
(defn gitter-events-dispatcher
  "gitter -> irc, slack"
  [{:keys [gitter irc] :as config}]
  (go-loop [{:keys [nickname message] :as ch} (<! gitter/channel)]
    (when-not (re-find #"ircbot" message)
      ;; since irc can't support multi-line message
      (doseq [msg (gitter->irc ch)]
        (irc/send-message! :gitter msg))
      )
    (recur (<! gitter/channel))))

(defn start-irc-bridge
  [{:keys [gitter irc] :as config}]
  ;; start listener in thread
  (gitter/event-listener gitter)
  (irc/event-listener irc)
  ;; start dispatcher for handling events
  (irc-events-dispatcher    config)
  (gitter-events-dispatcher config))

(defn -main [& args]
  (let [arg1 (nth args 0)]
    (if arg1
      (-> (parse-config arg1) (start-irc-bridge))
      (println "ERROR: Please specify config file."))))
