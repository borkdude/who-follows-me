 (defproject whosnotfollowingme "0.1.1"
   :description "FIXME: write this!"
   :dependencies [[org.clojure/clojure "1.5.1"]
                  [compojure "1.1.3"]
                  [ring/ring-jetty-adapter "1.1.6"]
                  [twitter-api "0.7.4"]
                  ;; pulls in [clj-oauth "1.2.13"]
                  [enlive "1.0.1"]]
                  ;;[me.raynes/laser "0.1.11"]  
   :ring {:handler whosnotfollowingme.handler/app}
   :main whosnotfollowingme.handler
   :profiles {:production
              {:ring
               {:open-browser? false, :stacktraces? false, :auto-reload? false}}}
   :plugins [[lein-ring "0.8.3"]]
   :min-lein-version "2.0.0")