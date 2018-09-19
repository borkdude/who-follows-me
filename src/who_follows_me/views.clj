(ns who-follows-me.views
  (:require
   [hiccup.page :as p]))

(def head
  [:head
   (p/include-css "/assets/bootstrap/css/bootstrap.min.css")])

(def bootstrap-js
  (p/include-js "/assets/bootstrap/js/bootstrap.min.js"))

(def twitter-js
  (p/include-js "//platform.twitter.com/widgets.js"))

(def footer
  [:center
   [:footer "Created by "
    [:span
     [:a {:href "https://twitter.com/intent/user?screen_name=borkdude"}
      "@borkdude"] "."]]])

(defn layout
  [& contents]
  (p/html5
   head
   [:body
    [:div.container
     contents
     footer]
    bootstrap-js
    twitter-js]))

(def welcome-page
  (layout
   [:div.jumbotron
    [:h1 "Who is following me?"]
    [:p.lead "Click the button below to authenticate with your Twitter account and
        find out who follows you, but you are not following back."]
    [:a.btn.btn-primary
     {:href "who-follows-me"}
     "Go!"]]
   [:div.jumbotron
    [:p "Or click here to find out who you are following but is not following
      you back."]
    [:a.btn.btn-primary
     {:href "who-follows-me-not"}
     "Go!"]]))

(def header
  [:nav.navbar.navbar-light.bg-light
   [:a.navbar-brand {:href "/#"}
    "&laquo;"]])

(defn render-usernames
  [message usernames]
  (layout
   header
   [:p.lead.mt-3 message]
   [:table.names.table.table-striped
    [:thead
     [:tr #_[:th "Username"]]]
    [:tbody
     (for [u usernames]
       [:tr
        [:td
         [:a {:href
              (str "https://twitter.com/intent/user?screen_name="
                   u)}
          u]]])]]))

(defn who-follows-me
  [usernames]
  (render-usernames "These people are following you, but you are not following them back!"
                    usernames))

(defn who-follows-me-not
  [usernames]
  (render-usernames "You follow these people, but they are not following you back!"
                    usernames))
