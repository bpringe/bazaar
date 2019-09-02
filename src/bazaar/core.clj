(ns bazaar.core)

(def p1 {:name :p1
         :handler (fn [msg] (assoc (:data msg) :p1 true))
         :in-conn {}})

(comment
  
  ;;;; Test connection
  
  (require '[bazaar.connections.local.core-async :refer [->CoreAsync]]
           '[bazaar.protocols :as p]
           '[clojure.core.async :as a])
  
  (def conn (p/start! (->CoreAsync {:hello "world"})))

  (def from-channel (-> conn :state :from-channel))
  
  (def to-channel (-> conn :state :to-channel))
  
  (a/put! from-channel "hello world")

  (a/<!! to-channel)


  ;;;; pub/sub tests
  
  (require '[clojure.core.async :as a])
  
  (def cx (a/chan))

  (def cy (a/chan))

  (def cz (a/chan))
  
  (def cx-pub (a/pub cx (fn [_] "out.cx")))
  
  (a/sub cx-pub "out.cx" cy)
  
  (a/sub cx-pub "out.cx" cz)
  
  (a/>!! cx "testing")
  
  (a/<!! cy)
  
  (a/<!! cz))
