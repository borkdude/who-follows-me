(ns who-follows-me.handler
  (:gen-class)
  (:require
   [clojure.java.browse :refer [browse-url]]
   [compojure.core :refer [defroutes GET wrap-routes]]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.logger :as logger]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.session :as session]
   [ring.middleware.webjars :refer [wrap-webjars]]
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.3rd-party.rotor
    :refer [rotor-appender]]
   [who-follows-me.auth :as auth]
   [who-follows-me.config :as c]
   [who-follows-me.sentry :refer [force-init-sentry!
                                  sentry-appender]]
   [who-follows-me.twitter-api :as twitter]
   [who-follows-me.views :as views]))

(defroutes oauthed-routes
  (GET "/who-follows-me" [:as {session :session}]
       (let [creds (:credentials session)
             followers (twitter/followers-minus-friends creds)
             html (views/who-follows-me followers)]
         html))
  (GET "/who-follows-me-not" [:as {session :session}]
       (let [creds (:credentials session)
             non-followers (twitter/friends-minus-followers creds)
             html (views/who-follows-me-not non-followers)]
         html)))

(defroutes app-routes
  (wrap-routes oauthed-routes
               auth/wrap-oauth)
  (GET "/" [] views/welcome-page)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-webjars
      wrap-content-type
      handler/site
      session/wrap-session
      (logger/wrap-log-response
       {:log-fn (fn [{:keys [level throwable message]}]
                  (when throwable
                    (timbre/error throwable message)))})))

(defonce server (atom nil))

(defn stop-server []
  (when-let [s @server]
    (.stop s)))

(defn -main [& args]
  (force-init-sentry!)
  (timbre/merge-config!
   {:level :info
    :appenders {:rotor (rotor-appender
                        {:path (str (System/getProperty "user.home")
                                    "/.wfm/logs.log")
                         :max-size (* 512 1024)
                         :min-level :info})
                :sentry (sentry-appender
                         {:min-level :info})}})
  (timbre/handle-uncaught-jvm-exceptions!)
  (run-jetty app {:host "127.0.0.1"
                  :port (c/port) :join? false}))

(defn dev []
  (let [first? (nil? @server)]
    (stop-server)
    (reset! server (-main))
    (timbre/merge-config!
     {:level :debug})
    (when first? (browse-url (c/redir-url)))))

;;;; Scratch

(comment
  (dev)
  )
