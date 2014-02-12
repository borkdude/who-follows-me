(ns whosnotfollowingme.view
  (:require [oauth.client :as oauth]
            [twitter.api.restful :as api]
            [twitter.callbacks :as callbacks]
            [net.cgrand.enlive-html :as enlive]
            #_[me.raynes.laser :as laser]
            :reload)
  (:use
    whosnotfollowingme.api
    [whosnotfollowingme.utils :only [log *logfile* *app-token* *app-secret*]]))

(defn update-attr
  "Update attribute Enlive transformation. Found this solution here:
   http://stackoverflow.com/questions/12586849/append-to-an-attribute-in-enlive/12687199#12687199"
  [attr f & args]
  (fn [node]
    (apply update-in node [:attrs attr] f args)))

(enlive/deftemplate usernames-table-body
  "public/whoisnotfollowingme.html"
  [usernames]
  [:table.names :tbody :tr]
  (enlive/clone-for [username usernames]
                    [:td :a]
                    (enlive/do->
                      (update-attr :href str username)
                      (enlive/content username))))

;; laser solution posted here: https://www.refheap.com/paste/8091
#_(defn usernames-table-body [usernames]
  (let [html (slurp "resources/public/whoisnotfollowingme.html")]
    (laser/document
      (laser/parse html)
      (laser/child-of (laser/select-and (laser/element= :table)
                                        (laser/class= "names"))))))

(enlive/deftemplate who-follows-me
  "public/whofollowsme.html"
  [usernames]
  [:table.names :tbody :tr]
  (enlive/clone-for [username usernames]
                    [:td :a]
                    (enlive/do->
                      (update-attr :href str username)
                      (enlive/content username))))

(enlive/deftemplate welcome-page
  "public/index.html"
  [])

(defn logfile []
  (slurp *logfile*))

(defn whofollowsme [oauth-token-response]
  (who-follows-me (followers-minus-friends *app-token*
                                           *app-secret*
                                           (:oauth_token oauth-token-response)
                                           (:oauth_token_secret oauth-token-response))))

;; see https://github.com/mattrepl/clj-oauth how this works exactly
(defn authorize [callback]
  (let [consumer (oauth/make-consumer *app-token*
                                      *app-secret*
                                      "https://api.twitter.com/oauth/request_token"
                                      "https://api.twitter.com/oauth/access_token"
                                      "https://api.twitter.com/oauth/authorize"
                                      :hmac-sha1)
        request-token (oauth/request-token consumer callback)
        redir-url (oauth/user-approval-uri consumer
                                           (:oauth_token request-token))]
    [{:consumer consumer
      :request-token request-token} redir-url]))

(defn access-token-response [consumer request-token oauth-verifier]
  (oauth/access-token consumer
                      request-token
                      oauth-verifier))

(defn whosnotfollowingme [oauth-token-response]
  (usernames-table-body
    (doesnt-follow-me-back
      *app-token*
      *app-secret*
      (:oauth_token oauth-token-response)
      (:oauth_token_secret oauth-token-response))))
