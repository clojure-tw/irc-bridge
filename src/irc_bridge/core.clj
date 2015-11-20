(ns irc-bridge.core
  (:require [clojure.edn :as edn]
            [taoensso.timbre :as timbre :refer (debug info warn error fatal)]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [irclj.events :as events]
            [irclj.core :as irc])
  (:gen-class))

(defn parse-config
  "Parse irc-bot config."
  [url]
  (edn/read-string (slurp url)))

;; TODO: error handling
(defn send-gitter-message!
  "Send message to gitter."
  [{:keys [rom-id api-key]} message]
  (client/post (str "https://api.gitter.im/v1/rooms/" rom-id "/chatMessages")
               {:content-type :json
                :accept :json
                :headers {"Authorization" (str "Bearer " api-key)}
                :conn-timeout (* 10 1000)
                :body (json/write-str {:text message })})
  (info (str "send-gitter-message!: " message)))

(def irc-chan    (chan))
(def gitter-chan (chan))
(def slack-chan  (chan))

(defn events-listener
  [{:keys [gitter irc] :as config}]
  (info "Start events-listener")
  ;; irc -> gitter, slack
  (go-loop []
    (let [msg (<! irc-chan)]
      (send-gitter-message! gitter msg)
      (recur)))
  ;; gitter -> irc, slack
  ;; slack -> irc, gitter

  config)

(defn connect-irc
  [{:keys [gitter irc]}]
  (let [{:keys [server port nickname ssl?]} irc]
    (info (str "Connect to " server ":" port ", nickname: " nickname))
    (irc/connect server port nickname
                 :ssl? ssl?
                 :callbacks {:privmsg
                             (fn [irc {:keys [nick text] :as m}]
                               (try
                                 (put! irc-chan (str "`ircbot` <" nick ">: " text))
                                 ;;(catch java.net.SocketException e
                                 (catch Throwable e
                                   (info "SocketException, trying to reconnect in xxx ms")
                                   ))
                               )
                             :raw-log events/stdout-callback})))

(defn create-bot
  [{:keys [gitter irc] :as config}]
  (let [conn (connect-irc config)
        {:keys [channel server]} irc]
    ;; join to channel
    (irc/join conn channel)
    ;; tick the irc server to prevent get "Connection reset by peer" error.
    (while true
      (info "tick")
      (irc/message conn server "Hi")
      (Thread/sleep 50000))))

(defn -main [& args]
  (let [arg1 (nth args 0)]
    (if arg1
      (-> (parse-config arg1)
          (events-listener)
          (create-bot))
      (fatal "Please specify config file."))))
