(ns herring.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [herring.env :refer [config]]
            [herring.util :refer [datetime]]))


(mg/connect-via-uri! (config :db-url))


;; Create User
(defn create-user [username password-hash]
  (let [doc {:_id username, :pass password-hash, :created datetime}]
    (mc/insert "users" doc)))


;; Retrieve User
(defn get-user [username]
  (mc/find-one-as-map "users" {:_id username}))
