(ns whosnotfollowingme.api
  (:use
    [twitter.api.restful :only [followers-ids
                                friends-ids
                                users-lookup
                                def-twitter-restful-method
                                friendships-lookup]]
    [twitter.callbacks :only [callbacks-sync-single-debug]]
    [twitter.oauth :only [make-oauth-creds]]
    [clojure.set :only [difference]])
  (:require [clojure.string :as string]))

(defn- seq-to-comma-separated [vector]
  (string/join "," vector))

(defn- userinfos [idset creds]
  (let [groups-of-100-ids (partition-all 100 idset)]
    ;; the api only allows 100 ids at a time
    (mapcat #(:body (users-lookup :oauth-creds creds :params {:user_id (seq-to-comma-separated %)}))
            groups-of-100-ids)))

(defn- idset [twitter-fn & args]
  (set (:ids (:body
               (apply twitter-fn args)))))

(defn followers-minus-friends [app-token app-secret user-token user-secret]
  (let [creds (make-oauth-creds app-token
                                app-secret
                                user-token
                                user-secret)
        difference-ids (difference (idset followers-ids :oauth-creds creds)
                                   (idset friends-ids :oauth-creds creds))
        userinfos (userinfos difference-ids creds)]
    (map :screen_name userinfos)))

(defn- friend-ids-by-auth [oauth-creds]
  (idset friends-ids :oauth-creds oauth-creds))

(defn doesnt-follow-me-back [app-token app-secret user-token user-secret]
  (let [creds  (make-oauth-creds app-token
                                 app-secret
                                 user-token
                                 user-secret)
        friend-ids (friend-ids-by-auth creds)
        groups-of-100-ids (partition-all 100 friend-ids)
        userinfos (mapcat
                    #(:body (friendships-lookup :oauth-creds creds
                                                :params {:user_id (seq-to-comma-separated %)}))
                    groups-of-100-ids)]
    (for [userinfo userinfos
          :when (not ((set (:connections userinfo)) "followed_by"))]
      (:screen_name userinfo))))

