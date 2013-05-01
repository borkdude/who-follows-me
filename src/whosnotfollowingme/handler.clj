(ns whosnotfollowingme.handler
  (:use compojure.core, ring.adapter.jetty)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [whosnotfollowingme.view :as view]
            [ring.util.response :as resp]
            [ring.middleware.session :as session]))

(defroutes app-routes
  (GET "/" [] (view/welcome-page))
  (GET "/logs" [] {:status 200
                   :header {"Content-Type" "text/plain"} 
                   :body (view/logfile)})
  (GET "/whofollowsme" [user] (view/whofollowsme user))
  (GET "/whosnotfollowingme" [oauth_token oauth_verifier :as {session :session}]
       (if (not (and oauth_token oauth_verifier))
         (resp/redirect "/whosnotfollowingme/auth")
         (let [access-token-response (view/access-token-response (session :consumer) 
                                                                 (session :request-token)
                                                                 oauth_verifier)]
           (view/whosnotfollowingme access-token-response))))   
  (GET "/whosnotfollowingme/auth" {session :session headers :headers}
       (let [[consumer-req twitter-url] (view/authorize 
                                          (str "http://" (headers "host") "/whosnotfollowingme"))]
         {:session (merge session consumer-req)
          :status 302 :headers {"Location" twitter-url} 
          :body nil})) 
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
    session/wrap-session))

(defn -main [& args]
  (run-jetty app {:port 8080}))