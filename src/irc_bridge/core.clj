(ns irc-bridge.core
  (:require [clojure.edn :as edn]
            [taoensso.timbre :as timbre :refer (debug info warn error fatal)]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [irclj.core :as irc]))

(defn parse-config
  "Parse irc-bot config."
  [url]
  (edn/read-string (slurp url)))

;; TODO: error handling
(defn message->gitter
  "Send message to gitter."
  [{:keys [rom-id api-key]} message]
  (client/post (str "https://api.gitter.im/v1/rooms/" rom-id "/chatMessages")
               {:content-type :json
                :accept :json
                :headers {"Authorization" (str "Bearer " api-key)}
                :conn-timeout (* 10 1000)
                :body (json/write-str {:text message })})
  (info (str "message->gitter: " message)))

;; TODO: quit connect safely
(defn connect-irc
  [{:keys [gitter irc]}]
  (let [server   (:server irc)
        port     (:port irc)
        nickname (:nickname irc)]
    (info (str "Connect to " server ":" port ", nickname: " nickname))
    (irc/connect server port nickname
                 :callbacks {:privmsg
                             (fn [irc {:keys [nick text]}]
                               (message->gitter gitter (str "`ircbot` <" nick ">: " text)))}
                 )))

(defn create-irc-bot [config]
  (let [conn (connect-irc config)
        channel (:channel (:irc config))]
    (info (str "Join to channel: " channel))
    (irc/join conn channel)))

(defn -main [& args]
  (let [arg1 (nth args 0)]
    (if arg1
      (-> (parse-config arg1)
          (create-irc-bot))
      (fatal "Please specify config file."))))
