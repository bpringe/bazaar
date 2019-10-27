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

(defn create-in-conn
  [process]
  (->CoreAsyncConnection {}))

(defn create-connections
  [processes]
  (reduce-kv (fn [processes k process]
               (assoc processes k (update process 
                                          :config 
                                          merge 
                                          {:in-conn (create-in-conn process)
                                           :out-conn (create-out-conn process)})))
             {}
             processes))

(defn get-exit-process-key
  [workflow-element workflow-path]
  (condp s/valid? workflow-element
    ::process (conj workflow-path (var->keyword workflow-element))
    ::workflow (let [workflow-path (conj workflow-path (var->keyword workflow-element))
                     last-element (-> workflow-element var-get flatten last)]
                 (get-exit-process-key last-element workflow-path))
    (throw (Exception. (str "Workflow element " workflow-element " is not a process or workflow")))))

(defn get-entry-process-key
  [workflow-element workflow-path]
  (condp s/valid? workflow-element
    ::process (conj workflow-path (var->keyword workflow-element))
    ::workflow (let [workflow-path (conj workflow-path (var->keyword workflow-element))
                     first-element (-> workflow-element var-get flatten first)]
                 (get-entry-process-key first-element workflow-path))
    (throw (Exception. (str "Workflow element " workflow-element " is not a process or workflow")))))

;; TODO: This may be able to be abstracted better. It's just that the way
;;       subscriptions work for each type of connection may be different, but the
;;       commonalities are: get in-conn config, if subs exist, conj, else create new sub collection.
(defn add-subscription
  [process topic]
  (update-in process [:config :in-conn :config :sub-topics] 
             (fn [sub-topics]
               (if sub-topics
                 (conj sub-topics topic)
                 [topic]))))

(defn add-subscriptions!
  [workflow workflow-path processes workflows-visited]
  (swap! workflows-visited conj workflow)
  (let [edges (filter #(s/valid? ::edge %) (var-get workflow))]
    (doseq [[source-node destination-node] edges]
      (let [source-exit-process-key (get-exit-process-key source-node workflow-path)
            destination-entry-process-key (get-entry-process-key destination-node workflow-path)
            pub-topic (-> (get @processes source-exit-process-key) :config :out-conn :config :pub-topic)]
        (swap! processes update destination-entry-process-key add-subscription pub-topic)
        (doseq [workflow (filter #(s/valid? ::workflow %) [source-node destination-node])]
          (when-not (contains? @workflows-visited workflow)
            (add-subscriptions! workflow
                                (conj workflow-path (var->keyword workflow))
                                processes
                                workflows-visited)))))))

(defn add-subscriptions
  [workflow processes]
  (let [processes (atom processes)]
    (add-subscriptions! workflow [(var->keyword workflow)] processes (atom #{}))
    @processes))

(defn get-processes
  [workflow]
  (->> workflow
       create-base-processes
       create-connections
       (add-subscriptions workflow)))
