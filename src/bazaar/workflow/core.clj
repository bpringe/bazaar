(ns bazaar.workflow.core
  (:require [bazaar.processes.core-async :as pca]))

(defn get-base-process
  [process-fn]
  (let [metadata (meta process-fn)]
    (pca/->CoreAsync {:name (-> metadata :name keyword)
                      :handler-fn (var-get process-fn)})))

(defn get-processes
  [workflow]
  ())

;;;; Test workflow

(defn p1
  [data]
  (assoc data :p1 true))

(defn p2
  [data]
  (assoc data :p2 true))

(defn p3
  [data]
  (assoc data :p3 true))

(def w1
  [[#'p1 #'p2]
   [#'p2 #'p3]
   [#'p1 #'p3]])

(comment
  
  )