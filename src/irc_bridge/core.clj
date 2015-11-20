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
;; http://www.dslreports.com/faq/4798
;; https://github.com/ormiret/spacebot/blob/master/src/spacebot/core.clj
;; https://github.com/oskarth/breakfast/blob/9d703b1da9de7bf89fc6f1f4aebce0ac7813122c/src/clj/breakfast/server.clj
(defn connect-irc
  [{:keys [gitter irc]}]
  (let [server   (:server irc)
        port     (:port irc)
        nickname (:nickname irc)
        ssl?     (:ssl? irc)]
    (info (str "Connect to " server ":" port ", nickname: " nickname))
    (irc/connect server port nickname
                 :ssl? ssl?
                 :callbacks {:privmsg
                             (fn [irc {:keys [nick text]}]
                               (try
                                 (message->gitter gitter (str "`ircbot` <" nick ">: " text))
                                 ;;(catch java.net.SocketException e
                                 (catch Throwable e
                                   (info "SocketException, trying to reconnect in xxx ms")
                                   ))
                               )
                             }
                 )))

(defn create-irc-bot [config]
  (let [conn (connect-irc config)
        channel (:channel (:irc config))]
    ;; enable keepalive on the socket, otherwise if it times out we never notice and the bot just hangs
    ;; (-> @conn :connection :socket (.setKeepAlive true))
    (irc/join conn channel)
    (info (str "Join to channel: " channel))
    ;; tick the irc server to prevent get "Connection reset by peer" error.
    (while true
      (do
        (info "tick")
        (irc/message conn (:server (:irc config)) "Hi")
        (Thread/sleep 50000)
        ))
    ))

(defn -main [& args]
  (let [arg1 (nth args 0)]
    (if arg1
      (-> (parse-config arg1)
          (create-irc-bot))
      (fatal "Please specify config file."))))
