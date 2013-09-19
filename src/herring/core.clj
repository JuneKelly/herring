(ns herring.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]))


;; Queues:
;; Create User
;; Request Authentication for User


;; Service startup
(defn start-herring
  "Start the herring service"
  []
  (println "Starting AMQP service..."))

(defn -main [& args]
  (println "Launching Herring...")
  (start-herring))
