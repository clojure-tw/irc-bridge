(ns irc-bridge.irc
  {:author "Yen-Chin, Lee"
   :doc ""}
  (:require [irclj.core :as irc]
            [irclj.events :as events]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]
            [clojure.string]
            [taoensso.timbre :as log :refer (debug info warn error fatal)]
            )
  )

(defonce channel (chan))
(defonce state (atom nil))

(defn- ->nickname
  "Generate nickname from type."
  [type]
  (case type
    :gitterbot (str "gitterbot" (rand-int 100))
    :slackbot  (str "slackbot"  (rand-int 100))
    (throw (Exception. "We only support gitterbot and slackbot type"))))

(defn send-message!
  "Send message to irc."
  ([from message] (send-message! @state from message))
  ([state from message]
   (let [{:keys [channel gitterbot slackbot]} state]
     (case from
       :gitter (irclj.core/message (:conn gitterbot) channel message)
       :slack  (irclj.core/message (:conn slackbot)  channel message)
       (throw (Exception. "We only support gitterbot and slackbot type"))))))

(defn- handle-privmsg
  [type irc {:keys [nick text] :as m}]
  ;; Since we host two bot in irc, only listen gitterbot's irc event
  (when (= type :gitterbot)
    (put! channel {:nickname nick :message text :type :default})))

(defn- handle-row-log
  [irc type s]
  (irclj.events/stdout-callback irc type s))

(defn- handle-ctcp-action
  [type irc {:keys [nick text] :as m}]
  (let [message (clojure.string/replace-first text #"ACTION" "")]
    ;; Since we host two bot in irc, only listen gitterbot's irc event
    (when (= type :gitterbot)
      (put! channel {:nickname nick :message message :type :action}))))

(declare start-irc-event!)
(defn- handle-exception
  "When we here, maybe we use the samce nickname, retry with another."
  [connect! type irc e]
  (log/error (.getMessage e))
  ;; kill old irc instance
  (irclj.core/kill irc)
  ;; wait a little to re-create irc listener
  (Thread/sleep 5000)
  (future-cancel (:event (@state type)))
  (start-irc-event! @state type))

(defn- join-and-listen-event!
  [conn {:keys [server channel] :as config}]
  (future
    ;; join to channel
    (irclj.core/join conn channel)
    ;; tick the irc server to prevent get "Connection reset by peer" error.
    (while true
      (irc/message conn server "Hi")
      (Thread/sleep 50000))))

(defn- connect!
  [{:keys [server port channel] :as config} type]
  (irc/connect server port (->nickname type)
               :callbacks {:privmsg      (partial handle-privmsg type)
                           :ctcp-action  (partial handle-ctcp-action type)
                           :on-exception (partial handle-exception type)
                           ;; :raw-log handle-row-log ; keep for debug
                           }))

(defn- start-irc-event!
  [config type]
  (let [conn   (connect! config type)
        event  (join-and-listen-event! conn config)]
    (swap! state assoc-in [type :conn]  conn)
    (swap! state assoc-in [type :event] event)))

(defn event-listener
  [{:keys [server port nickname channel ssl?] :as config}]
  ;; store default state in atom
  (reset! state config)
  ;; start the irc event listener
  (start-irc-event! config :gitterbot)
  (start-irc-event! config :slackbot))
