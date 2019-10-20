(ns bazaar.workflow.core
  (:require [bazaar.processes.core-async :refer [->CoreAsyncProcess]]
            [bazaar.connections.local.core-async :refer [->CoreAsyncConnection]]
            [clojure.spec.alpha :as s]))

;;;; Specs

(s/def ::process (fn [x]
                   (and (var? x) (fn? (var-get x)))))

(s/def ::edge (s/coll-of (s/or :process ::process
                               :workflow ::workflow)
                         :kind vector?
                         :count 2
                         :distinct true))

;;;; TODO: Possibly refactor this, but it works ¯\_(ツ)_/¯
(s/def ::workflow (fn [x]
                    (and (var? x)
                         (let [value (var-get x)]
                           (and (vector? value)
                                (> (count value) 0)
                                (every? #(or (s/valid? ::process %)
                                             (s/valid? ::edge %)
                                             (s/valid? ::workflow %))
                                        value))))))

(s/def ::workflow-element (s/or :workflow ::workflow
                                :process ::process
                                :edge ::edge))

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

(defn var->keyword
  [v]
  (-> v meta :name keyword))

(defn create-base-process
  [process]
  (->CoreAsyncProcess {:name (var->keyword process)
                       :handler-fn (var-get process)}))

(defn assoc-process!
  [process workflow-path processes]
  (let [process-name (var->keyword process)
        process-path (conj workflow-path process-name)]
    (swap! processes assoc process-path (-> (create-base-process process)
                                            (assoc :path process-path)))))

(defn create-base-processes!
  [workflow workflow-path processes]
  (let [workflow-name (var->keyword workflow)
        workflow-path (conj workflow-path workflow-name)]
    (doseq [workflow-element (var-get workflow)]
      (condp s/valid? workflow-element
        ::process (assoc-process! workflow-element workflow-path processes)
        ::edge (doseq [edge-element workflow-element]
                 (condp s/valid? edge-element
                   ::process (assoc-process! edge-element workflow-path processes)
                   ::workflow (create-base-processes! edge-element workflow-path processes)
                   (throw (Exception. (str "Edge element " edge-element " is not a process or workflow. Workflow path: " workflow-path)))))
        ::workflow (create-base-processes! workflow-element workflow-path processes)
        (throw (Exception. (str "Workflow element " workflow-element " is not a process, edge, or workflow. Workflow path: " workflow-path)))))))

(defn create-base-processes
  [workflow]
  (let [processes (atom {})]
    (create-base-processes! workflow [] processes)
    @processes))

(defn create-out-conn
  [process]
  (->CoreAsyncConnection {:pub-topic (str "out." (->> (:path process)
                                                      (map name)
                                                      (clojure.string/join ".")))}))

(defn create-out-conns
  [processes]
  (reduce-kv (fn [processes k process]
               (->> process
                    create-out-conn
                    (assoc process :out-conn)
                    (assoc processes k)))
             {}
             processes))

(defn create-in-conns!
  [workflow workflow-path processes]
  (let [edges (filter ::edge (var-get workflow))]
    (doseq [[source-node destination-node] edges]
      )))

(defn create-in-conns
  [workflow processes]
  (let [processes (atom processes)]
    (create-in-conns! workflow [] processes)
    @processes))

(defn get-processes
  [workflow]
  (->> workflow
       create-base-processes
       create-out-conns))
