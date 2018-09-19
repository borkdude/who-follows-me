(ns who-follows-me.auth
  (:require
   [oauth.client :as oa]
   [taoensso.timbre :refer [debug]]
   [taoensso.timbre :as timbre]
   [twitter.oauth :as oauth]
   [who-follows-me.config :refer [app-token
                                  app-secret
                                  redir-url]]))

(def app-consumer
  (memoize #(oa/make-consumer (app-token)
                              (app-secret)
                              "https://api.twitter.com/oauth/request_token"
                              "https://api.twitter.com/oauth/access_token"
                              "https://api.twitter.com/oauth/authorize"
                              :hmac-sha1)))

(defn authorize [callback]
  (let [request-token (oa/request-token (app-consumer) callback)
        redir-url (oa/user-approval-uri (app-consumer)
                                        (:oauth_token request-token))]
    {:request-token request-token
     :redir-url redir-url}))

(defn access-token
  [request-token oauth-verifier]
  (oa/access-token (app-consumer)
                   request-token
                   oauth-verifier))

(defn wrap-oauth
  [handler]
  (fn [req]
    (let [{:keys [session uri]} req
          oauth-verifier (get-in req [:params :oauth_verifier])
          {:keys [credentials
                  request-token]} session]
      (cond credentials
            ;; we are authorized
            (handler req)
            (and request-token oauth-verifier)
            ;; twitter called our callback url and we still have to get the
            ;; access token and secret
            (let [{:keys [:oauth_token :oauth_token_secret
                          :screen_name :user_id]}
                  (access-token request-token
                                oauth-verifier)
                  credentials (oauth/make-oauth-creds
                               (app-token)
                               (app-secret)
                               oauth_token
                               oauth_token_secret)]
              (timbre/info "User has identified:" screen_name user_id)
              {:session (assoc session
                               :credentials
                               credentials)
               :status 302
               :headers {"Location" (:uri req)}})
            :else
            ;; otherwise we are not authorized yet and we redirect to Twitter
            (let [{:keys [:request-token
                          :redir-url]}
                  (authorize
                   (str (redir-url) uri))]
              {:session (assoc session
                               :request-token request-token)
               :status 302 :headers {"Location" redir-url}})))))

(comment
  )
