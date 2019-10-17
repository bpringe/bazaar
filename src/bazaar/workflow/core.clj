(ns bazaar.workflow.core
  (:require [bazaar.processes.core-async :as pca]))

;;;; Example data structure for processes

(def workflow-1
  [[:p1 :p2]])

(def workflow-2
  [[:workflow-1 :p3]])

(def workflow-3
  [[:workflow-2 :p4]])

;; For workflow-3
(def processes-map {[:workflow-3 :workflow-2 :workflow-1 :p1] {#_p1_map}
                    [:workflow-3 :workflow-2 :workflow-1 :p2] {#_p2_map}
                    [:workflow-3 :workflow-2 :p3] {#_p3_map}
                    [:workflow-3 :p4] {#_p4_map}})

;;;; Helper functions

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