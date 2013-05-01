(ns whosnotfollowingme.utils
  (:use clojure.java.io)
  (:import [java.util Properties]
           [java.io File]))

(defn load-config-file
  "this loads a config file from the classpath"
  [file-name]
  (let [file-reader (.. (Thread/currentThread)
                        (getContextClassLoader)
                        (getResourceAsStream file-name))
        props (Properties.)]
    (.load props file-reader)
    (into {} props)))

(def ^:dynamic *config* (load-config-file "app.config"))

(defn assert-get
  "get a value from the config or environment, otherwise throw an exception detailing the problem"
  [key-name]
  (or (get *config* key-name)
      (System/getenv key-name)
      (throw (Exception. (format "please define %s in the resources/app.config file or as an environment variable" key-name)))))

(defn optional-get
  [key-name]
  (or (get *config* key-name)
      (System/getenv key-name)))

(defn log-line [path line]
  (with-open [wrtr (writer path :append true)]
    (binding [*out* wrtr] (println line))))

(def ^:dynamic *logfile* (or (optional-get "app.logfile")
                             (do (let [user-home (str (System/getProperty "user.home")
                                                      "/.twitter-service")
                                       dir (File. user-home)]
                                   (.mkdirs dir)
                                   (str user-home "/logs.txt")))))

(def log (partial log-line *logfile*))

(def ^:dynamic *app-token* (assert-get "app.consumer.key"))
(def ^:dynamic *app-secret* (assert-get "app.consumer.secret"))