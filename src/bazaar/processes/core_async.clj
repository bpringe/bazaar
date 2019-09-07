(ns bazaar.processes.core-async
  (:require [bazaar.protocols :as p]
            [clojure.core.async :as a]))

(defn start-process-loop!
  [{:keys [in-conn out-conn] :as config}]
  (let [input-chan (p/get-to-channel in-conn)
        output-chan (p/get-from-channel out-conn)]
    (a/go-loop []
      (when-let [msg (a/<! input-chan)]
        (a/>! output-chan msg)
        (recur))))
  config)

(defrecord CoreAsync [config]
  p/Lifecycle
  (start! [this]
          (assoc this :state (start-process-loop! config)))
  (stop! [this] 
         (println "Stopping process")
         this))