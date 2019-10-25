(ns bazaar.processes.core-async
  (:require [bazaar.protocols :as p]
            [clojure.core.async :as a]))

(defn start-connections!
  [config state]
  (println "Starting connections for process" (:name config))
  (reduce-kv (fn [state k v]
               (if (satisfies? p/Connection v)
                 (assoc state k (p/start! v))
                 state))
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
  (println "Starting process loop for process" name)
  (println "Process state:" state)
  (let [input-chan (if in-conn 
                     (p/get-output-channel in-conn)
                     (throw (Exception. (str "No in-conn exists on process " name))))
        output-chan (when out-conn (p/get-input-channel out-conn))]
    (a/go-loop []
      (when-let [msg (a/<! input-chan)]
        (println (format "[%s] - Message received: %s" name msg))
        (let [handler-result (handler-fn msg)]
          (println (format "[%s] - Handler result: %s" name handler-result))
          (when output-chan
            (a/>! output-chan handler-result)))
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