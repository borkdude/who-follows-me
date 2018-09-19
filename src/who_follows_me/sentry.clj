(ns who-follows-me.sentry
  (:require
   [sentry-clj.core :as sentry]
   [who-follows-me.config :as c]))

;; Upgraded from https://github.com/ptaoussanis/timbre/blob/master/src/taoensso/timbre/appenders/3rd_party/sentry.clj

(def ^:private timbre->sentry-levels
  {:trace  :debug
   :debug  :debug
   :info   :info
   :warn   :warning
   :error  :error
   :fatal  :fatal
   :report :info})

(defn sentry-appender
  "Returns a sentry-clj Sentry appender.
  Requires the DSN (e.g. \"https://<key>:<secret>@sentry.io/<project>\")
  to be passed in, see Sentry documentation for details.
  Common options:
    * :tags, :environment and :release will be passed to Sentry
      as attributes, Ref. https://docs.sentry.io/clientdev/attributes/.
    * :event-fn can be used to modify the raw event before sending it
      to Sentry."

  [opts]
  (let [{:keys [min-level
                event-fn] :or {min-level :warn
                               event-fn identity}} opts
        base-event
        (->> (select-keys opts [:tags :environment :release])
             (filter (comp not nil? second))
             (into {}))]
    {:enabled?   true
     :async?     true
     :min-level  min-level
     :rate-limit nil
     :output-fn  :inherit
     :fn
     (fn [data]
       (let [{:keys [instant level output_ ?err msg_ ?ns-str context]} data
             event
             (as-> base-event event
               (merge event
                      {:message (force msg_)
                       :logger  ?ns-str
                       :level   (get timbre->sentry-levels level)}
                      (when ?err {:throwable ?err})
                      (when context {:extra context}))
               (event-fn event))]
         (sentry/send-event event)))}))


(defn force-init-sentry! []
  (when-let [dsn (c/sentry-dsn)]
    (reset! @#'sentry-clj.core/initialized false)
    (sentry/init! dsn)))

;;;; Scratch

(comment
  )
