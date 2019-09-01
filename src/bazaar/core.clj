(ns bazaar.core)

(def p1 {:name :p1
         :handler (fn [msg] (assoc (:data msg) :p1 true))
         :in-conn {}})

(comment
  
  ;;;; Test connection
  
  (require '[bazaar.connections.local.core-async :refer [->CoreAsync]]
           '[bazaar.protocols :as p])
  
  (def conn (->CoreAsync {:hello "world"}))


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
