(ns herring.db
  (:require [monger.core :as mg]
           [monger.collection :as mc]
           [monger.query :as mq]
           [herring.env :refer [config]]))


(mg/connect-via-uri! (:db-url config))

;; TODO
;; Create User
;; Authenticate User
