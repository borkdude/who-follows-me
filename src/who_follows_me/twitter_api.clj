(ns who-follows-me.twitter-api
  (:require
   [clojure.core.memoize :refer [ttl]]
   [clojure.set :refer [difference]]
   [clojure.string :as str]
   [taoensso.timbre :refer [debug]]
   [twitter.api.restful :as twitter]))

(def default-ttl (* 1000 60 15))

(defn ttl-memo
  [f]
  (ttl f :ttl/threshold default-ttl))

(defn user-infos
  [creds ids]
  (let [groups-of-100-ids (partition-all 100 ids)]
    ;; the api only allows 100 ids at a time
    (mapcat #(:body
              (twitter/users-lookup :oauth-creds creds
                                    :params {:user_id
                                             (str/join "," %)}))
            groups-of-100-ids)))

(defn ids
  [response]
  (set (:ids (:body response))))

(defn friends-ids*
  [creds]
  (ids (twitter/friends-ids :oauth-creds creds)))

(def friends-ids
  (ttl-memo friends-ids*))

(defn followers-ids*
  [creds]
  (ids (twitter/followers-ids :oauth-creds creds)))

(def followers-ids
  (ttl-memo followers-ids*))

(defn followers-minus-friends*
  [creds]
  (let [difference-ids
        (difference (followers-ids creds)
                    (friends-ids creds))
        uis (user-infos creds difference-ids)]
    (map :screen_name uis)))

(def followers-minus-friends
  (ttl-memo followers-minus-friends*))

(defn friends-minus-followers*
  [creds]
  (let [difference-ids
        (difference (friends-ids creds)
                    (followers-ids creds))
        uis (user-infos creds difference-ids)]
    (map :screen_name uis)))

(def friends-minus-followers
  (ttl-memo friends-minus-followers*))

;;;; Scratch

(comment
  )
