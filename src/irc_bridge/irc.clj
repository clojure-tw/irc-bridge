(ns irc-bridge.irc
  {:author "Yen-Chin, Lee"
   :doc ""}
  (:require [irclj.core :as irc]
            [irclj.events :as events]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]
            [clojure.string]
            )
  )

(defonce channel (chan))

;; FIXME: actuall I don't like this
(defonce conn (atom nil))

(defn send-message!
  "Send message to irc."
  [{:keys [channel] :as irc} message]
  (irclj.core/message @conn channel message))

(defn- handle-privmsg
  [irc {:keys [nick text] :as m}]
  (put! channel {:nickname nick :message text :type :default}))

(defn- handle-row-log
  [irc type s]
  (irclj.events/stdout-callback irc type s))

(defn- handle-ctcp-action
  [irc {:keys [nick text] :as m}]
  (let [message (clojure.string/replace-first text #"ACTION" "")]
    (put! channel {:nickname nick :message message :type :action})))

(defn connect
  [{:keys [server port nickname channel ssl?] :as irc}]
  (irc/connect server port nickname
               :ssl? ssl?
               :callbacks {:privmsg handle-privmsg
                           :raw-log handle-row-log
                           :ctcp-action handle-ctcp-action}))

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
