(defproject who-follows-me "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring "1.7.0"]
                 [ring-logger "1.0.1"]
                 [ring-webjars "0.2.0"]
                 [org.webjars/bootstrap "4.1.3"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [twitter-api "1.8.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [io.sentry/sentry-clj "0.7.2"]
                 [org.clojure/core.memoize "0.7.1"]]
  :main who-follows-me.handler
  :min-lein-version "2.0.0")
