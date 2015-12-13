(ns irc-bridge.telegram
  {:author "Yen-Chin, Lee"
   :doc "Listen telegram server and send message to channel."}
  (:require [clj-http.client :as client]
            [http.async.client :as http]
            [clojure.data.json :as json]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]))

(defonce channel (chan))
(defonce state (atom nil))

(defn send-message!
  ([message] (send-message! @state message))
  ([{:keys [chat-id token]} message]
   (client/post (str "https://api.telegram.org/bot" token "/sendMessage")
                {:content-type :json
                 :form-params {:chat_id chat-id
                               :text message}})))

(defn receive-message
  ([offset] (receive-message @state offset))
  ([{:keys [chat-id token]} offset]
   (let [req (-> (client/get (str "https://api.telegram.org/bot" token "/getUpdates")
                             {:query-params {:offset offset}})
                 :body
                 (json/read-str :key-fn keyword))
         result (-> req :result)]

     (println (str "---> " req))

     ;; when event come, send it to channel
     (if-not (nil? result)
       (put! channel result))

     ;; sleep for prevent busy polling
     (Thread/sleep 500)
     ;; return update_id
     ;; An update is considered confirmed as soon as getUpdates is called with an
     ;; offset higher than its update_id.
     (if-let [ofs (-> result last :update_id)]
       (inc ofs) 0))))

(defn- listen-to-telegram-event
  [{:keys [chat-id token]}]
  (loop [offset 0]
    (recur (receive-message offset))))

(defn event-listener
  [{:keys [rom-id api-key] :as config}]
  ;; save config to state, we only modify it once
  (reset! state config)
  ;; start in thread
  (future (listen-to-telegram-event config)))