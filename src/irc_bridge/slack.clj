(ns irc-bridge.slack
  {:author "Yen-Chin, Lee"
   :doc "A can't really work slack bot integration."}
  (:require [clj-http.client :as client]
            [http.async.client :as http]
            [clojure.data.json :as json]
            [clojure.core.async :refer [chan go go-loop >! <! timeout alt! put! <!!] :as async]))

(comment
  "I can't make my bot get the team token in clojurians and lazy to ask the admin for bot integration."
  "Add this funtion one day...")