(ns whosnotfollowingme.api
  (:use
    [twitter.api.restful :only [show-followers
                                show-friends
                                lookup-users
                                def-twitter-restful-method
                                profile-image-for-user]]
    [twitter.callbacks :only [callbacks-sync-single-debug]]
    [twitter.oauth :only [make-oauth-creds]]
    [clojure.set :only [difference]])
  (:require [clojure.string :as string]))

(defn- seq-to-comma-separated [vector]
  (string/join "," vector))

(defn- userinfos [idset]
  (let [groups-of-100-ids (partition-all 100 idset)]
    ;; the api only allows 100 ids at a time
    (mapcat #(:body (lookup-users :params {:user_id (seq-to-comma-separated %)}))
            groups-of-100-ids)))

(defn- idset [twitter-fn & args]
  (set (:ids (:body
               (apply twitter-fn args)))))

(defn followers-minus-friends [screenname]
  (let [difference-ids (difference (idset show-followers :params {:screen-name screenname})
                                   (idset show-friends :params {:screen-name screenname}))
        userinfos (userinfos difference-ids)]
    (map :screen_name userinfos)))

(def-twitter-restful-method lookup-friendships :get "friendships/lookup.json")

(defn- friend-ids-by-auth [oauth-creds]
  (idset show-friends :oauth-creds oauth-creds))

(defn doesnt-follow-me-back [app-token app-secret user-token user-secret]
  (let [creds  (make-oauth-creds app-token
                                 app-secret
                                 user-token
                                 user-secret)
        friend-ids (friend-ids-by-auth creds)
        groups-of-100-ids (partition-all 100 friend-ids)
        userinfos (mapcat
                    #(:body (lookup-friendships :oauth-creds creds
                                                :params {:user_id (seq-to-comma-separated %)}))
                    groups-of-100-ids)]
    (for [userinfo userinfos
          :when (not ((set (:connections userinfo)) "followed_by"))]
      (:screen_name userinfo))))

(defn profile-image-link [screenname]
  (-> 
    (profile-image-for-user :params {:screen_name screenname} :callbacks (callbacks-sync-single-debug))
    :headers
    :location))

