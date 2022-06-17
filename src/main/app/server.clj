(ns app.server
  (:use compojure.core)
  (:require
    [app.parser :refer [api-parser]]
    [org.httpkit.server :as http]
    [hiccup.page :as h.page]
    [com.fulcrologic.fulcro.server.api-middleware :as server]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.middleware.resource :refer [wrap-resource]]
    [taoensso.timbre :as timbre]
    [ring.util.response :as response]
    [clojure.string :as str]
    [ring.util.response :as resp]))

(defn generate-index []
  (h.page/html5 {}
        [:head {:lang "en"}
         [:meta {:charset "UTF-8"}]
         [:link {:href "/dist/output.css" :rel "stylesheet"}]
         [:link {:href "styles.css" :rel "stylesheet"}]
         [:link {:href "favicon.ico" :rel "stortcut icon"}]]
        [:body
         [:div#app]
         [:script {:src "/js/main/main.js"}]]))


;(defroutes routing-handler
;  (GET "/" [] (generate-index))
;  (route/not-found "<h1>Page not found</h1>"))
(def not-found-handler
  (fn [req]
    {:status 404
     :body   {}}))

(defn wrap-html-routes [ring-handler]
  (fn [{:keys [uri] :as req}]
    (if (or (str/starts-with? uri "/api")
          (str/starts-with? uri "/images")
          (str/starts-with? uri "/files")
          (str/starts-with? uri "/js")
          (str/starts-with? uri "/dist"))
      (ring-handler req)

      (-> (resp/response (generate-index))
        (resp/content-type "text/html")))))

(def middleware
  (-> not-found-handler
    (server/wrap-api {:uri "/api"
                      :parser api-parser})
    (server/wrap-transit-params {})
    (server/wrap-transit-response {})
    (wrap-resource "public")
    (wrap-html-routes)
    wrap-content-type
    wrap-not-modified))

(defonce stop-fn (atom nil))

(defn start []
  (reset! stop-fn (http/run-server middleware {:port 3000})))

(defn stop []
  (when @stop-fn
    (@stop-fn)
    (reset! stop-fn nil)))
