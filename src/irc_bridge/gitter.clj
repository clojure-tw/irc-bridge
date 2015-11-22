(ns irc-bridge.gitter
  {:author "Yen-Chin, Lee"
   :doc ""}
  (:require [clj-http.client :as client]
            [http.async.client :as http]
            [clojure.data.json :as json]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]
            ))

(defonce channel (chan))

(defonce state (atom nil))

(defn send-message!
  "Send message to gitter."
  ([message]
   (send-message! @state message))
  ([{:keys [rom-id api-key]} message]
   (client/post (str "https://api.gitter.im/v1/rooms/" rom-id "/chatMessages")
                {:content-type :json
                 :accept :json
                 :headers {"Authorization" (str "Bearer " api-key)}
                 :conn-timeout (* 10 1000)
                 :body (json/write-str {:text message})})))

(defn- listen-to-gitter-event
  [{:keys [rom-id api-key]}]
  (with-open [conn (http/create-client)]
    (let [resp (http/stream-seq conn
                                :get (str "https://stream.gitter.im/v1/rooms/" rom-id "/chatMessages")
                                :headers {"Authorization" (str "Bearer " api-key) "Connection" "keep-alive"}
                                :timeout -1)]
      (doseq [s (http/string resp)]
        (when-not (clojure.string/blank? s)
          (let [{:keys [fromUser text]} (json/read-str s :key-fn keyword)
                username (:username fromUser)]
            (put! channel {:nickname username :message text})))))))

(defn event-listener
  [{:keys [rom-id api-key] :as config}]
  ;; save config to state, we only modify it once
  (reset! state config)
  ;; start in thread
  (future (listen-to-gitter-event config)))
