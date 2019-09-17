(ns bazaar.processes.core-async
  (:require [bazaar.protocols :as p]
            [clojure.core.async :as a]))



(defn start-process-loop!
  [{:keys [in-conn out-conn]}]
  (let [input-chan (p/get-to-channel in-conn)
        output-chan (p/get-from-channel out-conn)]
    (a/go-loop []
      (when-let [msg (a/<! input-chan)]
        (a/>! output-chan msg)
        (recur)))))

(defrecord CoreAsync [config]
  p/Lifecycle
  (start! [this]
          ;; TODO: Start connections here and assoc to state of process
          (assoc this :state {:running? true}))
  (stop! [this] 
    (println "Stopping process")
    this)
  p/Proc
  (get-name [this] (:name config)))