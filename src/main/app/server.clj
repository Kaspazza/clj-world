(ns app.server
  (:require
    [app.parser :refer [api-parser]]
    [org.httpkit.server :as http]
    [hiccup.page :as h.page]
    [com.fulcrologic.fulcro.server.api-middleware :as server]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [ring.middleware.session :refer [wrap-session]]
    [ring.middleware.resource :refer [wrap-resource]]
    [clojure.string :as str]
    [ring.util.response :as resp]))

(defn generate-index []
  (h.page/html5 {}
        [:head {:lang "en"}
         [:meta {:charset "UTF-8"}]
         [:link {:href "/dist/output.css" :rel "stylesheet"}]
         [:link {:href "/css/styles.css" :rel "stylesheet"}]
         [:link {:href "/css/general.css" :rel "stylesheet"}]
         [:link {:href "/css/markdown.css" :rel "stylesheet"}]
         [:link {:href "favicon.ico" :rel "shortcut icon"}]]
        [:body
         [:div {:id "app"}]
         [:script {:src "/js/main/main.js"}]]))

(def not-found-handler
  (fn [_req]
    {:status 404
     :body   {}}))

(defn wrap-html-routes [ring-handler]
  (fn [{:keys [uri] :as req}]
    (if (or (str/starts-with? uri "/api")
          (str/starts-with? uri "/images")
          (str/starts-with? uri "/files")
          (str/starts-with? uri "/js")
          (str/starts-with? uri "/dist")
          (str/starts-with? uri "/css"))
      (ring-handler req)

      (-> (resp/response (generate-index))
        (resp/content-type "text/html")))))

(def middleware
  (-> not-found-handler
    (server/wrap-api {:uri "/api"
                      :parser api-parser})
    (server/wrap-transit-params {})
    (server/wrap-transit-response {})
    (wrap-session)
    (wrap-resource "public")
    wrap-content-type
    wrap-not-modified
    (wrap-html-routes)))

(defonce stop-fn (atom nil))

(defn start []
  (reset! stop-fn (http/run-server middleware {:port 3000})))

(defn stop []
  (when @stop-fn
    (@stop-fn)
    (reset! stop-fn nil)))
