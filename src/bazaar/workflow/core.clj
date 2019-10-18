(ns bazaar.workflow.core
  (:require [bazaar.processes.core-async :as pca]
            [clojure.spec.alpha :as s]))

;;;; Specs

(s/def ::process (fn [x]
                   (and (var? x) (fn? (var-get x)))))

(s/def ::edge (s/coll-of var? :kind vector? :count 2 :distinct true))

(s/def ::workflow (fn [x]
                    (and (var? x)
                         (let [value (var-get x)]
                           (and (vector? value)
                                (> (count value) 0)
                                (every? #(or (s/valid? ::process %)
                                             (s/valid? ::edge %)
                                             (s/valid? ::workflow %)) 
                                        value))))))

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

;;;; Builder functions

(defn get-var-name-as-keyword
  [v]
  (-> v meta :name keyword))

(defn create-base-process
  [process-fn]
  (let [metadata (meta process-fn)]
    (pca/->CoreAsync {:name (-> metadata :name keyword)
                      :handler-fn (var-get process-fn)})))

(defn create-base-processes
  [workflow-var]
  (let [workflow-name (get-var-name-as-keyword workflow-var)]
    ))

(defn get-processes
  [workflow]
  (-> workflow
      get-base-processes))

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