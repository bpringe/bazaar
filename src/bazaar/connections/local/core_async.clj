(ns bazaar.connections.local.core-async
  (:require [bazaar.protocols :refer [Connection Lifecycle]]
            [clojure.core.async :as a]))

(defonce topic-hub (atom {}))

(defn create-input-channel!
  [state]
  (assoc state :input-channel (a/chan)))

(defn create-publication!
  "If topic exists in topic-hub, returns topic data, else it creates the topic data, adds it to the topic-hub, and returns it."
  [topic]
  (if-let [topic-data (get @topic-hub topic)]
    topic-data
    (let [channel (a/chan)
          publication (a/pub channel (fn [_] topic))
          topic-data {:channel channel
                      :publication publication}]
      (swap! topic-hub assoc topic topic-data)
      topic-data)))

(defn subscribe-to-topics!
  [{:keys [sub-topics]} state]
  (doseq [topic sub-topics]
    (let [{:keys [publication]} (create-publication! topic)]
      (a/sub publication topic (:input-channel state))))
  state)

(defn create-output-channel!
  [{:keys [pub-topic]} state]
  (if pub-topic
    (let [{:keys [channel]} (create-publication! pub-topic)]
      (assoc state :output-channel channel))
    (assoc state :output-channel (a/chan))))

(defn start-process-loop!
  [{:keys [input-channel output-channel] :as state}]
  (a/go-loop []
    (when-let [msg (a/<! input-channel)]
      (a/>! output-channel msg)
      (recur)))
  state)

(defn close-input-channel!
  [{:keys [input-channel] :as state}]
  (a/close! input-channel)
  state)

(defn close-output-channel!
  [{:keys [output-channel] :as state}]
  (a/close! output-channel)
  state)

(defrecord CoreAsyncConnection [config]
  Connection
  (get-input-channel [this]
    (get-in this [:state :input-channel]))
  (get-output-channel [this]
    (get-in this [:state :output-channel]))
  Lifecycle
  (start! [this]
    (assoc this :state (->> {}
                            create-input-channel!
                            (subscribe-to-topics! config)
                            (create-output-channel! config)
                            start-process-loop!)))
  (stop! [this]
    (-> (:state this)
        close-input-channel!
        close-output-channel!)))

(comment
  (require '[bazaar.protocols :as p])
  
  (def conn (p/start! (->CoreAsyncConnection {:sub-topics ["out.p1"]})))
  
  (def sub-topic-channel (:channel (get @topic-hub "out.p1")))
  
  (a/>!! sub-topic-channel "hello world")
  
  (def output-channel (p/get-output-channel conn))
  
  (a/<!! output-channel)
  
  1)