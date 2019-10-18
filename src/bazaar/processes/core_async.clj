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
  [{:keys [name handler-fn]} {:keys [in-conn out-conn] :as state}]
  (let [input-chan (p/get-output-channel in-conn)
        output-chan (p/get-input-channel out-conn)]
    (println "Starting process loop for process" name)
    (a/go-loop []
      (when-let [msg (a/<! input-chan)]
        (println (format "[%s] - Message received: %s" name msg))
        (let [handler-result (handler-fn msg)]
          (println (format "[%s] - Handler result: %s" name handler-result))
          (a/>! output-chan handler-result))
        (recur))))
  state)

(defrecord CoreAsyncProcess [config]
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