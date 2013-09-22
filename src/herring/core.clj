(ns herring.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            [herring.db :as db]
            [clojure.data.json :as json])
  (:import [org.mindrot.jbcrypt BCrypt]))


;; Queues:
;; herring.create_user
;; herring.authenticate_user

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
  (if (= content-type "application/json")
    (do
      (let [data (json/read-str (String. payload "UTF-8"))
            username (get data "username")
            password (get data "password")
            response-queue (get data "responseQueue")
            response (json/write-str (authenticate-user username password))]
        (comment "need to send back the response")))
    (do
      (comment "send back response specifying application/json"))))


(defn start-auth-consumer [ch ex-name]
  (let [q-name "herring.auth"
        thread (Thread.
                 #(lc/subscribe ch q-name auth-handler))]
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
        ch   (lch/open conn)
        ex-name   "herring"]
    (println "Starting herring...")
    (le/declare ch ex-name "direct")
    (start-consumers ch ex-name)))
