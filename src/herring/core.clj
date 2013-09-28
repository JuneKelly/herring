(ns herring.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            [langohr.exchange  :as le]
            [herring.db :as db]
            [clojure.data.json :as json])
  (:import [org.mindrot.jbcrypt BCrypt]))


(def herring-exchange "herring")
(def response-exchange "herring_response")


;; Create User
(defn create-user [username password-hash]
  (if (not db/get-user username)
    (do
      (db/create-user username password-hash)
      {:created true,
       :username username})
    {:created false,
     :reason "user already exists",
     :username username}))


;; Request Authentication for User
(defn authenticate-user [username password-raw]
  (if-let [user (db/get-user username)]
    (if (BCrypt/checkpw password-raw (:pass user))
      {:authenticated true}
      {:authenticated false, :reason "password incorrect"})
    {:authenticated false, :reason "user does not exist"}))


;; Handlers
(defn auth-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (println "<< in auth-handler >>") ;; debug
  (if (= content-type "application/json")
    (do
      (let [data (json/read-str (String. payload "UTF-8"))
            username (get data "username")
            password (get data "password")
            response-key (get data "responseKey")
            response-payload (json/write-str (authenticate-user username password))]
        (println "RECEIVED:"
                 (clojure.string/join ", " [username
                                            password
                                            response-key
                                            response-payload]))
        (lb/publish ch
                    response-exchange
                    response-key
                    response-payload
                    :content-type "application/json")
        (lb/ack ch delivery-tag)))
    (do
      (println "WARNING: message with content-type" content-type "received"))))


(defn start-auth-consumer [ch ex-name]
  (let [q-name "herring.auth"
        thread (Thread.
                 #(lc/subscribe ch q-name auth-handler :auto-ack false))]
    (lq/declare ch q-name :exclusive false :auto-delete true)
    (lq/bind ch q-name ex-name :routing-key "herring.auth")
    (.start thread)))


;; Service startup
(defn start-consumers [ch ex-name]
  (do
    (println "Starting AMQP consumers...")
    (start-auth-consumer ch ex-name)))


(defn -main [& args]
  (println "Launching Herring...")
  (let [conn (rmq/connect)
        ch   (lch/open conn)]
    (println "Starting herring...")
    (le/declare ch herring-exchange "direct" :durable false :auto-delete true)
    (le/declare ch response-exchange "direct" :durable false :auto-delete true)
    (start-consumers ch herring-exchange)))
