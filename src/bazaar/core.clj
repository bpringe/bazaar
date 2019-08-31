(ns bazaar.core)

(comment

  (require '[clojure.core.async :as a])

  ;;;; pipe tests
  
  (def cx (a/chan))

  (def cy (a/chan))

  (def cz (a/chan))

  (a/pipe cx cy)

  (a/pipe cx cz)

  (a/>!! cx "hello world 1")
  (a/>!! cx "hello world 2")
  (a/>!! cx "hello world 3")

  (a/<!! cy)

  (a/<!! cz) ;; this will block because pipe only sends from cx to cy, and not also to cz

  ;;;; pub/sub tests
  
  (def cx (a/chan))

  (def cy (a/chan))

  (def cz (a/chan))
  
  (def cx-pub (a/pub cx (fn [_] "out.cx")))
  
  (a/sub cx-pub "out.cx" cy)
  
  (a/sub cx-pub "out.cx" cz)
  
  (a/>!! cx "testing")
  
  (a/<!! cy)
  
  (a/<!! cz))
