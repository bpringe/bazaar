(ns bazaar.processes.core-async
  (:require [bazaar.protocols :as p]
            [clojure.core.async :as a]))

(defn start-connections!
  [config state]
  (reduce-kv (fn [state k v]
               (if (satisfies? p/Connection v)
                 (assoc state k (p/start! v))))
             state
             config))

(defn stop-connections!
  [state]
  (reduce-kv (fn [state k v]
               (if (satisfies? p/Connection v)
                 (do (p/stop! v)
                     (dissoc state k))
                 state))
             state
             state))

(defn start-process-loop!
  [{:keys [name]} {:keys [in-conn out-conn] :as state}]
  (let [input-chan (p/get-output-channel in-conn)
        output-chan (p/get-input-channel out-conn)]
    (println "Starting process loop for process" name)
    (a/go-loop []
      (when-let [msg (a/<! input-chan)]
        (println "[" name "] - message received:" msg)
        (a/>! output-chan msg)
        (println "Message sent to output channel")
        (recur))))
  state)

(defrecord CoreAsync [config]
  p/Lifecycle
  (start! [this]
          (assoc this :state (->> {}
                                  (start-connections! config)
                                  (start-process-loop! config))))
  (stop! [this]
         (assoc this :state (->> (:state this)
                                 stop-connections!)))
  p/Proc
  (get-name [this] (:name config)))