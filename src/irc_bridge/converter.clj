(ns irc-bridge.converter
  {:author "Yen-Chin, Lee"
   :doc ""}
  (:require [clojure.string :as str]))

(defn irc->gitter
  [{:keys [nickname message type] :as ch}]
  (case type
    :action (str "`ircbot` * " nickname " " message)
    ;; default
    (str "`ircbot` <" nickname ">: " message)))

(defn gitter->irc
  [{:keys [nickname message type] :as ch}]
  (map #(str %1 %2)
       (repeat (str "<" nickname ">: "))
       (clojure.string/split-lines message)))