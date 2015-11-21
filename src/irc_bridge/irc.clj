(ns irc-bridge.irc
  {:author "Yen-Chin, Lee"
   :doc ""}
  (:require [irclj.core :as irc]
            [irclj.events :as events]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]
            )
  )

(defonce channel (chan))

;; FIXME: actuall I don't like this
(defonce conn (atom nil))

(defn send-message!
  "Send message to irc."
  [{:keys [channel] :as irc} message]
  (irclj.core/message @conn channel message))

(defn- callback-privmsg
  [irc {:keys [nick text] :as m}]
  (try
    (put! channel {:nickname nick :message text})
    ;;(catch java.net.SocketException e
    (catch Throwable e
      (println "SocketException, trying to reconnect in xxx ms")
      )))

(defn- callback-row-log
  [irc type s]
  (irclj.events/stdout-callback irc type s))

(defn connect
  [{:keys [server port nickname channel ssl?] :as irc}]
  (irc/connect server port nickname
               :ssl? ssl?
               :callbacks {:privmsg callback-privmsg
                           :raw-log callback-row-log}))

(defn event-listener
  [{:keys [server port nickname channel ssl?] :as irc}]
  (let [c (connect irc)]
    ;; save the connect info in atom, we need it for sending message
    (reset! conn c)
    ;; join to channel
    (irclj.core/join @conn channel)
    ;; tick the irc server to prevent get "Connection reset by peer" error.
    (while true
      (irc/message @conn server "Hi")
      (Thread/sleep 50000))
    ))
