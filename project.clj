(defproject herring "0.1.0-SNAPSHOT"
  :description "A tiny AMQP Auth service"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.novemberain/monger "1.5.0"]
                 [com.novemberain/langohr "1.4.1"]
                 [environ "0.4.0"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [org.clojure/data.json "0.2.3"]]

  :plugins [[lein-environ "0.4.0"]]

  :profiles {
    :production {:env {:broker-host "none"
                       :db-url "none"}}
    :dev {:env {:broker-host "localhost"
                :broker-port 5672
                :db-url "mongodb://localhost/herring"}}}

  :main herring.core)
