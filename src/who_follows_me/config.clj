(ns who-follows-me.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [taoensso.timbre :refer [warn]]))

(def wfm-dir
  (str (System/getProperty "user.home") "/.wfm"))

(def config-path
  (str wfm-dir "/config.edn"))

(def config
  (memoize #(-> (io/file config-path)
                slurp
                edn/read-string)))

(defn assert-get
  "get a value from the config, throws exception when not present"
  [k]
  (or (get (config) k)
      (throw (Exception.
              (format "please add %s in the %s config file"
                      k config-path)))))

(defn app-token []
  (assert-get :consumer-key))

(defn app-secret []
  (assert-get :consumer-secret))

(defn port []
  (assert-get :port))

(defn redir-url []
  (assert-get :redir-url))

(defn sentry-dsn []
  (or (get (config) :sentry-dsn)
      (warn "Sentry DSN not configured.")))

;;;; Scratch

(comment
  (redir-url)
  (sentry-dsn)
  )
